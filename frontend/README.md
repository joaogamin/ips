# IPS Frontend — Sistema de Precificação de Notebooks

Interface Angular 17 para o sistema de precificação dinâmica de notebooks importados. Permite cadastrar parâmetros de custo, registrar compras de lote e consultar o painel de vendas com preços calculados por estado.

## Stack

| Tecnologia | Versão |
| --- | --- |
| Angular | 17.3.x |
| TypeScript | 5.x |
| Tailwind CSS | 3.x |
| Karma / Jasmine | — |

## Pré-requisitos

- Node.js 18+
- npm 9+
- Backend rodando em `http://localhost:8080`

## Instalação e execução

```bash
cd frontend
npm install
npm start
```

Acesse em `http://localhost:4200`.

## Executar testes unitários

```bash
npm test
```

## Build de produção

```bash
npm run build
```

Os artefatos são gerados em `dist/`.

## Telas e rotas

| Rota | Componente | Descrição |
| --- | --- | --- |
| `/parametros` | `ParametrosComponent` | Visualização e edição das taxas (Devcoin, importação, frete, ICMS, margem) |
| `/notebooks` | `NotebookListComponent` | Listagem de notebooks cadastrados com estoque atual |
| `/notebooks/importar` | `LotImportComponent` | Registro de compra de lote (nome + custo em D$ + quantidade) |
| `/dashboard` | `DashboardComponent` | Painel de vendas: preços por estado, totais e reinvestimento em TI |

## Estrutura de diretórios relevante

```text
src/app/
  core/
    models/          # Interfaces TypeScript dos recursos da API
    services/        # HttpClient wrappers (cost-parameter, notebook, dashboard)
    interceptors/    # ErrorInterceptor global
  features/
    parametros/      # ParametrosComponent
    notebooks/       # NotebookListComponent, LotImportComponent
    dashboard/       # DashboardComponent
  app-routing.module.ts
  app.module.ts
```

## Variáveis de ambiente

A URL base da API é configurada em `src/environments/`:

| Arquivo | `apiUrl` |
| --- | --- |
| `environment.ts` (dev) | `http://localhost:8080/api` |
| `environment.prod.ts` | `http://localhost:8080/api` |
