# Esboço de Spike Técnica — Sistema de Precificação de Notebooks Importados

## 1. Contexto e Motivação

Necessidade de um motor de precificação dinâmica para notebooks importados com faturamento em Devcoin (D$), sujeito a regras tributárias brasileiras por estado, frete dinâmico e margem comercial. A principal preocupação técnica é a **precisão decimal absoluta** em todas as etapas do cálculo.

---

## 2. Riscos Identificados

- **Arredondamento prematuro:** uso de `double`/`float` em qualquer etapa do cálculo introduz erros acumulativos inaceitáveis em sistemas financeiros.
- **Concorrência no estoque:** múltiplas requisições simultâneas de compra de lote poderiam gerar race conditions na soma do estoque sem controle transacional.
- **Acoplamento síncrono no cadastro de lote:** processar a compra de lote de forma síncrona no ciclo request/response tornaria o endpoint lento e frágil sob carga.
- **Rigidez dos parâmetros:** taxas fixas no código impediriam ajustes operacionais sem redeploy.

---

## 3. Decisões Arquiteturais

### 3.1 Precisão Matemática
- **Decisão:** `BigDecimal` com `MathContext(20, HALF_UP)` e scale interno de 10 casas em todos os cálculos. Arredondamento para 2 casas apenas no DTO de resposta via `PrecisionMathUtil.round()`.
- **Alternativa descartada:** `double` — inaceitável para valores monetários.
- **Alternativa considerada:** `BigDecimal` com scale fixo de 2 em todo fluxo — descartada pois perderia precisão em produtos de taxas percentuais.

### 3.2 Mensageria para Cadastro de Lote
- **Decisão:** `POST /api/notebooks/lote` publica na fila `lot-import.queue` (RabbitMQ) e retorna HTTP 202 imediatamente. O `LotImportConsumer` processa de forma assíncrona com `@RabbitListener`.
- **Motivação:** desacopla o endpoint do processamento; protege o banco de escritas concorrentes (o consumer processa sequencialmente); habilita retry natural via DLQ.
- **Dead Letter Queue:** `lot-import.dlq` recebe mensagens que falharam após exaustão de retries, evitando perda silenciosa.

### 3.3 Parâmetros de Custo no Banco
- **Decisão:** taxas (Devcoin, importação, ICMS, frete, margem) persistidas na tabela `cost_parameters` e carregadas a cada cálculo.
- **Motivação:** permite que o analista atualize parâmetros sem redeploy; `ON CONFLICT DO NOTHING` no `data.sql` garante seed idempotente.
- **Alternativa descartada:** constantes hardcoded no serviço — inviável para um sistema de precificação real.

### 3.4 Cálculo de Preço por Estado
- **Decisão:** ICMS aplicado *por cima* (`precoEstado = precoSemIcms / (1 - aliquota)`), não *por dentro*, conforme mecânica de ICMS/ST brasileiro.
- **Margem de lucro:** aplicada sobre o custo final acumulado (custo + importação + frete), antes do ICMS.

### 3.5 Painel de Vendas
- **Decisão:** `SalesDashboardService` acumula totais por estado e lucro bruto com precisão interna (`BigDecimal`), arredondando apenas nos DTOs de resposta; não recalcula as regras de precificação, delegando ao `PricingCalculatorService`.

### 3.6 Banco de Dados
- **Decisão:** PostgreSQL com DDL versionado em `sql/schema.sql`; `hibernate.ddl-auto: update` para criação automática na primeira execução.
- **Alternativa considerada:** Flyway/Liquibase — mais robusto para ambientes de produção, mas com overhead desnecessário para o escopo do desafio.

---

## 4. Fluxo de Precificação (ordem de aplicação)

```
custoDev (D$)
  × taxaConversao                     → custoBRL
  × (1 + aliquotaImportacao)          → valorAcumulado
  + frete(valorAcumulado)             → custoFinal
  / (1 - margemLucro)                 → precoSemIcms
  - custoFinal                        → lucroBruto
  / 3                                 → reinvestimentoTI
  / (1 - aliquotaEstado)              → precoFinalEstado
```

---

## 5. Estrutura de Mensageria RabbitMQ

```
Producer → [pricing.exchange] --lot.import--> [lot-import.queue] → Consumer
                                                      ↓ (falha)
                                              [lot-import.dlx] → [lot-import.dlq]
```

### 5.1 Trade-off de Idempotência

O consumer opera em semântica **at-least-once**: em caso de falha após a persistência mas
antes do ACK, a mensagem pode ser reprocessada e o mesmo lote somado duas vezes ao estoque.

**Por que aceitar este risco no escopo do desafio:**

- A fila tem `concurrency: 1`, o que elimina corridas simultâneas sobre o mesmo notebook.
- O retry está configurado para 3 tentativas com backoff exponencial (1 s → 2 s → 4 s),
  cobrindo falhas transitórias (timeout de DB, restart do broker) sem reprocessamento duplicado
  em condições normais.
- Falhas persistentes vão para a DLQ, onde podem ser inspecionadas e reprocessadas
  manualmente — nenhuma mensagem é perdida silenciosamente.

**O que seria necessário para idempotência completa:**

Incluir um `messageId` único (UUID) em `LotImportMessage`, persistir IDs já processados em
tabela `processed_messages` e verificar antes de aplicar ao estoque. Esse padrão
(idempotent consumer) adiciona uma escrita extra por mensagem e complexidade operacional
(limpeza periódica da tabela) que está fora do escopo do desafio.

---

## 6. Ambiguidade na Regra 5: Ordem ICMS × Margem

O critério 5 do `desafio-tecnico.md` afirma: *"Sobre o custo final acumulado (Custo + Importação +
Frete + **Tributação por Estado**), o setor comercial exige uma margem de 25%"*. A formulação
coloca o ICMS **dentro** da base sobre a qual incide a margem, mas o documento **não especifica
explicitamente** a sequência de aplicação. Existem duas interpretações válidas:

| Aspecto | Modo A (padrão) | Modo B (literal) |
| ------- | --------------- | --------------- |
| **Fórmula** | `precoSemIcms = custoFinal / (1 − m)` → ICMS por cima | `custoComIcms = custoFinal / (1 − t)` → `preco = custoComIcms / (1 − m)` |
| **Preço ao consumidor** | **Idêntico** — `custoFinal / ((1−m)(1−t))` | **Idêntico** — `custoFinal / ((1−t)(1−m))` |
| **Lucro bruto** | Mesmo nos 5 estados (calculado pré-ICMS) | Varia por estado (RS > BA > SP > AM > GO) |
| **Insight** | Lucro reflete margem pura antes de impostos | Lucro reflete o que sobra após absorver o ICMS no custo |

> **Nota:** os preços finais são matematicamente equivalentes nos dois modos (multiplicação comutativa).
> A escolha afeta apenas a contabilização do lucro — não o valor cobrado do consumidor.

**Decisão arquitetural:** em vez de escolher uma interpretação arbitrária, o comportamento foi
tornando **parametrizável** via `cost_parameters.icms_na_base_margem` (`0` = Modo A; `1` = Modo B),
reutilizando a infraestrutura de parâmetros existente sem nova tabela ou endpoint. O default é
Modo A para preservar os valores originalmente validados.

Isso demonstra a "revisão crítica do algoritmo de cálculo" solicitada no desafio (item 5,
Diretrizes de IA) e explicita o trade-off em vez de silenciá-lo.

---

## 7. Cobertura de Testes

| Camada | Estratégia |
|--------|-----------|
| `PricingCalculatorService` | Testes unitários com valores reais do desafio; verificação de precisão por `compareTo` |
| `NotebookService` | Mockito; verifica delegação e soma de estoque |
| `SalesDashboardService` | Mockito; verifica acumulação de totais por estado e cálculo de reinvestimento |
| `CostParameterService` | Mockito; testa carregamento e validação |
| `LotImportProducer` | Mockito; verifica publicação com exchange e routing key corretos |
| Componentes Angular | Jasmine/Karma com spies nos serviços; testa fluxo de dados e estados de UI |
