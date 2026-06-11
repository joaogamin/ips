# Sistema de Precificação de Notebooks Importados

Microsserviço e interface visual para o cálculo automatizado do preço de venda unitário de notebooks importados, com conversão de moeda (Devcoin), taxas alfandegárias, frete dinâmico e tributação estadual por estado brasileiro.

Requisitos originais: [`specs/memory/desafio-tecnico.md`](specs/memory/desafio-tecnico.md)

---

## Arquitetura

```
┌─────────────────────┐        HTTP/REST        ┌──────────────────────────┐
│   Angular 17        │ ──────────────────────► │   Spring Boot 3 (8080)   │
│   (porta 4200)      │                         │   Java 21 + Maven        │
└─────────────────────┘                         └──────────┬───────────────┘
                                                           │
                                          ┌────────────────┴────────────────┐
                                          │                                  │
                               ┌──────────▼──────────┐      ┌──────────────▼──────────┐
                               │   PostgreSQL 15      │      │   RabbitMQ 3            │
                               │   (porta 5432)       │      │   (porta 5672 / 15672)  │
                               └─────────────────────-┘      └─────────────────────────┘
```

O fluxo de compra de lote é **assíncrono**: o endpoint `POST /api/notebooks/lote` publica na fila `lot-import.queue` e retorna `202 Accepted` imediatamente. O consumer processa e persiste em background.

---

## Estrutura do repositório

| Diretório | Conteúdo | Documentação |
| --- | --- | --- |
| `backend/` | API Spring Boot — motor de precificação, endpoints REST, mensageria | [backend/README.md](backend/README.md) |
| `frontend/` | SPA Angular — parâmetros, compra de lote, painel de vendas | [frontend/README.md](frontend/README.md) |
| `docs/` | Spike técnica, divisão de tarefas IA × manual, instruções de execução | [docs/ia-usage.md](docs/ia-usage.md) · [docs/spike.md](docs/spike.md) |
| `specs/` | Contexto interno de desenvolvimento orientado a spec com IA | — |

---

## Quick start

**1. Infraestrutura** (na raiz do repositório):

```bash
docker compose -f backend/docker-compose.yml up -d
```

**2. Backend:**

```bash
cd backend
.\mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # Linux/Mac
```

**3. Frontend:**

```bash
cd frontend
npm install
npm start
```

Acesse `http://localhost:4200`. Para detalhes de variáveis de ambiente, testes e validação dos serviços, consulte o README de cada projeto.

---

## O desafio: regras de negócio

O sistema implementa seis regras encadeadas de precificação, aplicadas com `BigDecimal` em precisão absoluta durante todo o cálculo — arredondamento para 2 casas decimais apenas na apresentação final.

| # | Regra | Detalhe |
| --- | --- | --- |
| 1 | **Conversão de moeda** | `1,00 D$ = R$ 3,7259000231` (Devcoin → Real) |
| 2 | **Taxa de importação** | 17,31% sobre o valor convertido |
| 3 | **Frete dinâmico** | 4 faixas baseadas no valor acumulado (custo + importação): grátis acima de R$ 5.000; fixo de R$ 28,55 entre R$ 3.500–4.999; R$ 36,21 + 0,75% entre R$ 1.500–3.499; R$ 47,83 + 0,99% abaixo de R$ 1.500 |
| 4 | **Margem de lucro bruto** | 25% sobre o custo final acumulado |
| 5 | **Tributação por estado** | ICMS individual: SP 11,87% · GO 7,01% · RS 25,00% · AM 8,76% · BA 14,55% |
| 6 | **Reinvestimento em TI** | 1/3 do lucro bruto de cada unidade vendida |

---

## Objetivos atingidos

- Motor de precificação com as 6 regras implementadas exclusivamente via `BigDecimal` — sem `double` ou `float` em nenhuma etapa do cálculo.
- Entrada de lote desacoplada do ciclo request/response via RabbitMQ: `POST /api/notebooks/lote` retorna `202` e o consumer persiste em background com DLQ declarada para falhas.
- Parâmetros de custo (taxas, alíquotas, frete) persistidos no banco e editáveis em runtime, sem necessidade de redeploy.
- Painel de vendas consolidado: preço unitário por estado, totais por estado, lucro bruto e percentual de reinvestimento em TI.
- Cobertura de testes unitários nos dois lados: Spring (Mockito, valores numéricos concretos, `compareTo` para precisão) e Angular (Jasmine/Karma com spies).
- Golden master test travando os valores calculados contra os parâmetros reais do desafio.
- Ambiguidade da Regra 5 (ordem ICMS × margem) identificada, documentada e tornada parametrizável — ver [`docs/spike.md`](docs/spike.md#6-ambiguidade-na-regra-5-ordem-icms--margem).

---

## Melhorias futuras

- **DLQ efetivamente alcançável:** o consumer atual faz ACK mesmo em falha (catch-all silencia a exceção). Remover o catch e deixar a exceção propagar ativa o retry com backoff e a DLQ declarada.
- **Lock otimista no estoque:** `@Version` em `Notebook` ou `UPDATE ... SET quantidade = quantidade + :qty` atômico elimina a race condition em cenários de alta concorrência de mensagens.
- **Validação de parâmetros ausentes:** `loadCostParameters` deve lançar `BusinessException` descritiva em vez de `NullPointerException` quando um parâmetro faltar no banco.
- **Fonte única de schema:** migrar de `ddl-auto: update` para `ddl-auto: validate`, deixando `sql/schema.sql` como única autoridade do DDL.
- **Idempotência de mensagem:** incluir `messageId` (UUID) no `LotImportMessage` e verificar IDs já processados para evitar dupla soma de estoque em reprocessamento após falha.
- **Observabilidade:** adicionar `spring-boot-starter-actuator` com `/health` expondo status de DB e RabbitMQ.
- **Teste de integração de mensageria:** Testcontainers (RabbitMQ) cobrindo o fluxo produce → consume → persistência end-to-end.
