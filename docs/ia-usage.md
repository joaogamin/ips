# Uso de IA e Divisão de Tarefas — Sistema de Precificação de Notebooks

## 1. Como Rodar Localmente

### Pré-requisitos

| Ferramenta | Versão mínima |
| --- | --- |
| Java (JDK) | 21+ |
| Maven | 3.9+ (ou usar o `mvnw` do projeto) |
| Node.js | 18+ |
| npm | 9+ |
| Docker Desktop | Qualquer versão recente |

---

### 1.1 Infraestrutura (PostgreSQL + RabbitMQ)

Na raiz do projeto, execute:

```bash
docker compose -f backend/docker-compose.yml up -d
```

Isso sobe:

- **PostgreSQL 15** em `localhost:5432` — banco `ips_db`, usuário `ips_user`, senha `ips_pass`
- **RabbitMQ 3** em `localhost:5672` — usuário `ips_user`, senha `ips_pass`
- **RabbitMQ Management UI** em `http://localhost:15672`

---

### 1.2 Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

Na **primeira execução**, o Hibernate cria as tabelas automaticamente (`ddl-auto: update`) e o `data.sql` insere os parâmetros iniciais de precificação.

O backend estará disponível em: `http://localhost:8080`

**Executar testes:**

```bash
./mvnw test
```

---

### 1.3 Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

O frontend estará disponível em: `http://localhost:4200`

**Executar testes:**

```bash
npm test
```

**Build de produção:**

```bash
npm run build
```

---

### 1.4 Variáveis de Ambiente (opcional)

Todas as configurações possuem defaults para desenvolvimento local. Para sobrescrever:

| Variável | Default | Descrição |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | Porta do backend |
| `POSTGRES_HOST` | `localhost` | Host do PostgreSQL |
| `POSTGRES_PORT` | `5432` | Porta do PostgreSQL |
| `POSTGRES_DB` | `ips_db` | Nome do banco |
| `POSTGRES_USER` | `ips_user` | Usuário do banco |
| `POSTGRES_PASSWORD` | `ips_pass` | Senha do banco |
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta do RabbitMQ |
| `RABBITMQ_USER` | `ips_user` | Usuário do RabbitMQ |
| `RABBITMQ_PASS` | `ips_pass` | Senha do RabbitMQ |

---

### 1.5 Arquivos HTTP (Rest Client)

O diretório `backend/http/` contém arquivos `.http` para testar os endpoints manualmente via VS Code REST Client ou IntelliJ HTTP Client:

| Arquivo | Endpoints cobertos |
| --- | --- |
| `parametros.http` | GET todos, PUT atualizar |
| `notebooks.http` | GET lista, GET por id, POST lote |
| `dashboard.http` | GET painel de vendas |

---

## 2. Divisão de Tarefas: IA vs. Desenvolvimento Manual

### 2.1 Critério de Classificação

- **IA (assistida):** código gerado ou substancialmente redigido por ferramentas de IA generativa (Claude Code), com revisão e validação manual posterior.
- **Manual:** código inteiramente escrito, arquitetado e validado pelo desenvolvedor, sem geração automática de conteúdo.
- **Revisão crítica:** independente da origem, toda lógica de cálculo financeiro e de mensageria foi revisada e validada manualmente antes de cada commit.

---

### 2.2 Tabela de Tarefas

| Artefato | Modo | Observação |
| --- | --- | --- |
| **Arquitetura geral** (separação de camadas, escolha RabbitMQ, estratégia BigDecimal) | Manual | Decisões tomadas antes da escrita de qualquer código |
| **Algoritmo de precificação** (`PricingCalculatorService`) | Manual + Revisão crítica | Lógica de negócio revisada passo a passo contra os critérios do desafio; fórmulas de ICMS e frete verificadas manualmente |
| `PrecisionMathUtil` | Manual | Escrito à mão para garantir controle absoluto sobre arredondamento |
| Configuração RabbitMQ (`RabbitMQConfig`, DLQ) | Manual | Decisão de usar DLQ e routing key explícita foi do desenvolvedor |
| `LotImportProducer` / `LotImportConsumer` | Manual | Fluxo assíncrono arquitetado manualmente |
| Entidades JPA (`Notebook`, `CostParameter`) | Manual | |
| Repositórios Spring Data | Manual | |
| `CostParameterService` | Manual | |
| `NotebookService` | Manual | |
| Scripts SQL (`schema.sql`, `data.sql`) | Manual | Valores de parâmetros verificados contra o enunciado |
| DTOs de request/response (backend) | IA assistida | Geração de boilerplate de records Java |
| `SalesDashboardService` | IA assistida + Revisão crítica | Lógica de acumulação revisada; precisão verificada com testes unitários |
| `SalesDashboardController` | IA assistida | |
| DTOs do dashboard (`SalesDashboardDto`, etc.) | IA assistida | |
| `GlobalExceptionHandler`, `CorsConfig` | IA assistida | |
| Testes unitários Spring (todos os `*ServiceTest`) | IA assistida + Revisão crítica | Cenários e asserções revisados pelo desenvolvedor; valores de stub calculados manualmente |
| Models e services Angular | IA assistida | |
| `ParametrosComponent` (lógica e template) | Manual | |
| `LotImportComponent` (lógica e template) | Manual | |
| `NotebookListComponent` | Manual | |
| `DashboardComponent` | IA assistida + Revisão | Layout e lógica de células revisados |
| Testes unitários Angular (`*.spec.ts`) | IA assistida + Revisão | |
| Estilos CSS (componentes) | IA assistida | |
| `app-routing.module.ts`, `app.module.ts` | Manual | |

---

### 2.3 Resumo Percentual (estimado)

| Modo | % do esforço total |
| --- | --- |
| Manual / Decisão arquitetural | ~45% |
| IA assistida com revisão crítica | ~40% |
| IA assistida (boilerplate, estrutura) | ~15% |

---

### 2.4 Ferramentas de IA Utilizadas

- **Claude Code (Anthropic)** — geração de boilerplate, testes unitários, componentes Angular e documentação técnica.

Toda saída de IA foi revisada contra os requisitos do desafio antes de ser incorporada ao projeto.

---

### 2.5 Processo: Desenvolvimento Orientado a Spec com IA

O projeto foi desenvolvido com um processo de **spec-driven development** assistido por IA, documentado em `specs/`:

- Os requisitos do desafio foram decompostos em 5 feature specs numeradas (`specs/changes/001` a `005`), cada uma com objetivo, contexto técnico, critérios de aceite explícitos e tasks verificáveis com evidência registrada.
- O agente de IA operava com contexto estruturado de memória persistente (`specs/memory/`) — produto, estrutura, contexto técnico — e playbooks de execução (`specs/skills/`) para tarefas recorrentes como criação de controllers, repositories e schemas SQL.
- Toda decisão arquitetural (BigDecimal, RabbitMQ assíncrono, parâmetros no banco) foi tomada e documentada pelo desenvolvedor antes da escrita de qualquer código; o agente executava dentro dessas decisões, não as tomava.

Esse processo demonstra a "supervisão manual da arquitetura e dos algoritmos de cálculo" exigida nas diretrizes do desafio (item 5).
