# IPS Backend — Sistema de Precificação de Notebooks

API REST em Spring Boot 3 para cálculo automatizado do preço de venda de notebooks importados, com conversão de moeda (Devcoin), taxas alfandegárias, frete dinâmico e tributação estadual (SP, GO, RS, AM, BA).

## Stack

| Tecnologia | Versão |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.3.5 |
| Spring Data JPA | — |
| PostgreSQL Driver | — |
| RabbitMQ (AMQP) | — |
| Maven (Wrapper) | 3.9.x |

## Pré-requisitos

- Java 21+
- Docker e Docker Compose (para PostgreSQL e RabbitMQ)

## Subindo a infraestrutura

Execute na **raiz do repositório** (`c:\dev\temp\IPS`):

```bash
docker compose -f backend/docker-compose.yml up -d
```

Confirme que ambos os containers estão `healthy`:

```bash
docker compose -f backend/docker-compose.yml ps
```

## Rodando o backend

```bash
cd backend
./mvnw spring-boot:run        # Linux/Mac
.\mvnw.cmd spring-boot:run    # Windows
```

O servidor sobe em `http://localhost:8080`.

## Variáveis de ambiente

Todas têm valores padrão compatíveis com o `docker-compose.yml` local.

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | Porta HTTP do servidor |
| `POSTGRES_HOST` | `localhost` | Host do PostgreSQL |
| `POSTGRES_PORT` | `5432` | Porta do PostgreSQL |
| `POSTGRES_DB` | `ips_db` | Nome do banco |
| `POSTGRES_USER` | `ips_user` | Usuário do banco |
| `POSTGRES_PASSWORD` | `ips_pass` | Senha do banco |
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta AMQP |
| `RABBITMQ_USER` | `ips_user` | Usuário do RabbitMQ |
| `RABBITMQ_PASS` | `ips_pass` | Senha do RabbitMQ |
| `RABBITMQ_VHOST` | `/` | Virtual host |

## Compilar sem subir o servidor

```bash
.\mvnw.cmd compile
```

## Rodar testes

```bash
.\mvnw.cmd test
```

## Estrutura de pacotes

```text
com.pricing/
  costparameter/
    CostParameter.java
    CostParameterController.java
    CostParameterRepository.java
    CostParameterService.java
    dto/
      CostParameterResponse.java
      UpdateCostParameterRequest.java
  notebook/
    Notebook.java
    NotebookController.java
    NotebookRepository.java
    NotebookService.java
    dto/
      LotPurchaseRequest.java
      NotebookResponseDto.java
    messaging/
      LotImportConsumer.java
      LotImportMessage.java
      LotImportProducer.java
    pricing/
      CostParameters.java
      PricingCalculatorService.java
      PricingResult.java
  dashboard/
    SalesDashboardController.java
    SalesDashboardService.java
    dto/
      NotebookSalesSummary.java
      SalesDashboardDto.java
      StateTotalsDto.java
  shared/
    config/
      CorsConfig.java             # CORS permitindo localhost:4200
      RabbitMQConfig.java         # Filas, exchanges e DLQ declarativos
    dto/
      ApiErrorResponse.java       # Envelope padrão de erros da API
    exception/
      GlobalExceptionHandler.java # @RestControllerAdvice central
      BusinessException.java
      ResourceNotFoundException.java
    util/
      PrecisionMathUtil.java      # Wrapper BigDecimal (scale=10, HALF_UP)
```

## Mensageria RabbitMQ

| Recurso | Nome |
| --- | --- |
| Exchange principal | `lot-import.exchange` |
| Fila principal | `lot-import.queue` |
| Routing key | `lot-import.routing-key` |
| Dead Letter Exchange | `lot-import.dlx` |
| Dead Letter Queue | `lot-import.dlq` |

## Scripts SQL

| Arquivo | Conteúdo |
| --- | --- |
| `sql/schema.sql` | DDL — criação de tabelas (`notebooks`, `cost_parameters`) e índices |
| `src/main/resources/data.sql` | DML — seed dos parâmetros iniciais de precificação (Spring Boot) |

`sql/schema.sql` é montado no PostgreSQL via volume no `docker-compose.yml` e executado na criação do container. `data.sql` é executado pelo Spring Boot no startup com `ON CONFLICT DO NOTHING`, garantindo idempotência.

## Validando os serviços localmente

### PostgreSQL

#### Via Docker (sem dependências externas)

```bash
docker exec -it ips_db psql -U ips_user -d ips_db
```

Comandos úteis dentro do psql:

```sql
SELECT version();   -- confirma conexão e versão
\dt                 -- lista tabelas
\q                  -- sai
```

#### Via GUI (DBeaver, TablePlus, DataGrip)

| Campo | Valor |
| --- | --- |
| Host | `localhost` |
| Port | `5432` |
| Database | `ips_db` |
| User | `ips_user` |
| Password | `ips_pass` |

#### Via psql local (se instalado)

```bash
psql -h localhost -p 5432 -U ips_user -d ips_db
```

---

### RabbitMQ

#### Management UI — caminho mais direto

Acesse <http://localhost:15672> no browser.

| Campo | Valor |
| --- | --- |
| Usuário | `ips_user` |
| Senha | `ips_pass` |

A UI mostra filas, exchanges, conexões ativas e permite publicar mensagens manualmente.

> As filas e exchanges (`lot-import.*`) só aparecem após o Spring Boot iniciar pela primeira vez, pois são declaradas pela `RabbitMQConfig` no startup.

#### Via curl

```bash
# verifica saúde do broker
curl -s -u ips_user:ips_pass http://localhost:15672/api/health/checks/alarms

# lista filas e quantidade de mensagens
curl -s -u ips_user:ips_pass http://localhost:15672/api/queues
```

#### Via rabbitmqctl no container

```bash
docker exec -it ips_rabbitmq rabbitmqctl list_queues
docker exec -it ips_rabbitmq rabbitmqctl list_exchanges
```
