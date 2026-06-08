# Desafio Técnico Sênior: Sistema de Precificação de Notebooks Importados

## 1. Escopo e Contexto de Negócio
A nossa empresa de e-commerce está expandindo a operação e passará a importar lotes de notebooks para revenda no Brasil. Para garantir a viabilidade financeira e a margem de lucro estipulada pelo setor comercial, precisamos de um microsserviço e uma interface visual que realizem o cálculo automatizado e exato do preço de venda unitário de cada produto, considerando flutuações de moedas, taxas alfandegárias, fretes dinâmicos e variações fiscais por estado brasileiro.

---

## 2. Especificação Ágil: Product Backlog Item (PBI)

### US-001: Motor de Precificação Dinâmica de Ativos Importados
**Como** Analista de Precificação Comercial  
**Quero** registrar os lotes de notebooks comprados em moeda internacional, aplicar as regras tributárias e logísticas brasileiras  
**Para** visualizar o preço de venda unitário ideal para cada estado-alvo e controlar o estoque disponível.

### Cenário de Negócio & Regras de Cálculo (Critérios de Aceite)

1. **Moeda Base e Conversão:**
   * O preço de custo do lote de notebooks é faturado na criptomoeda fictícia **Devcoin (D$)**.
   * O sistema deve utilizar o valor estático de conversão de **1,00 D$ = R$ 3,7259000231**.
   * *Premissa:* O valor convertido para Real (R$) deve manter precisão decimal absoluta durante todo cálculo, mas apresentado o valor final com arredondamento em 2 casas decimais.

2. **Taxa de Importação:**
   * Logo após a conversão para Real (R$), deve ser aplicada uma taxa de importação compulsória de **17,31%** sobre o valor de custo convertido.

3. **Regra de Frete Dinâmico:**
   O custo do frete é calculado por unidade baseando-se no **Valor Total Acumulado da Compra (Custo Convertido + Taxa de Importação)**:
   * **Acima de R$ 5.000,00:** Frete Grátis (R$ 0,00).
   * **Entre R$ 3.500,00 e R$ 4.999,99:** Frete fixo de **R$ 28,55**.
   * **Entre R$ 1.500,00 e R$ 3.499,99:** Frete fixo de **R$ 36,21** + taxa adicional de **0,75%** calculada sobre o Valor Total Acumulado da Compra.
   * **Inferior a R$ 1.500,00:** Frete fixo de **R$ 47,83** + taxa adicional de **0,99%** calculada sobre o Valor Total Acumulado da Compra.

4. **Tributação por Estado (ICMS/ST):**
   O produto final será comercializado exclusivamente em 5 estados. O preço de venda deve ser individualizado por estado, adicionando o respectivo imposto sobre o valor já acrescido do lucro:
   * **SP:** 11,87%
   * **GO:** 7,01%
   * **RS:** 25,00%
   * **AM:** 8,76%
   * **BA:** 14,55%

5. **Margem de Lucro Bruto:**
   * Sobre o custo final acumulado (Custo + Importação + Frete + Tributação por Estado), o setor comercial exige uma margem de **lucro bruto de 25%** na venda final ao consumidor.

6. **Gestão de Estoque e Funcionalidades Esperadas:**
   A aplicação deve expor e processar três fluxos principais através de uma interface clara (com distribuição de telas a critério do desenvolvedor):
   * **Cadastro de Parâmetros de Custos:** Permitir a configuração/visualização das taxas (Devcoin, Alíquotas de Importação, Frete e Impostos estaduais) para garantir flexibilidade.
   * **Informar Compra de Lote (Entrada):** Registrar a entrada de um produto (Notebook), informando o valor de custo em Devcoin (D$) e a quantidade adquirida. Caso o produto já exista, a quantidade deve ser somada ao estoque atual.
   * **Painel de Vendas (Consulta):** Apresentar a listagem dos notebooks cadastrados, exibindo o saldo atual em estoque e o valor unitário final de venda calculado para cada um dos 5 estados (SP, GO, RS, AM, BA).

---

## 3. Stack Tecnológica & Premissas de Engenharia
* **Backend:** Java 21+ utilizando Spring Boot 3+ (Spring Data JPA, Web, Validation).
* **Mensageria:** Uso obrigatório de **RabbitMQ**. A forma de uso fica a critério do desenvolvedor.
* **Frontend:** Angular 17+. O layout, número de telas e navegação ficam a seu critério técnico e de UX.
* **Banco de Dados:** Banco relacional de sua escolha, mantendo um modelo de dados enxuto e normalizado.
* **Precisão Matemática (Fator Crítico):** Erros de arredondamento em sistemas financeiros são inaceitáveis.

---

## 4. Entregável Adicional: Esboço de Spike Técnica
Como engenheiro sênior, além do código, você deve anexar um pequeno **esboço de Spike Técnica** desta PBI junto à sua documentação. 
Descreva de maneira sucinta, em Markdown no formato de tópicos.

---

## 5. Diretrizes para o Uso de Inteligência Artificial
É permitido e incentivado o uso de ferramentas de IA generativa (ex: Cursor AI, GitHub Copilot, ChatGPT, Claude, Gemini) para acelerar a escrita de boilerplates, componentes visuais ou testes unitários, mas também visto com bons olhos o desenvolvimento manual para melhor conhecer seus conhecimentos e critérios técnicos. Contudo, a arquitetura e a revisão crítica dos algoritmos de cálculo e concorrência devem ser supervisionadas manualmente, demonstrando o seu papel como Engenheiro Sênior focado em segurança e governança de código.

---

## 6. Instruções de Entrega e Acesso ao Repositório
* **Prazo Limite Impreterível:** **12/06/2026**

**Fluxo de Trabalho no GitHub:**
1. **Não faça um Fork público** deste repositório para evitar que sua solução fique exposta para outros candidatos.
2. Na página inicial deste repositório modelo, clique no botão **"Use this template"** e selecione **"Create a new repository"**.
3. Crie o repositório sob a sua conta com a visibilidade configurada como **Privado**.
4. Adicione o usuário do avaliador (`multicanal-prd`) como colaborador do seu repositório privado para podermos analisar seu código.
5. Desenvolva sua solução contendo a cobertura de testes unitários automatizados (tanto para a lógica de precificação no Spring quanto para os componentes do Angular).
6. **Documentação Exigida (`docs/*`):** Escreva um texto contendo o seu **Esboço de Spike** (conforme item 4), a divisão de tarefas entre o desenvolvimento manual e por IA, além de orientações, configurações e comandos necessários para rodar a aplicação localmente.
7. Crie Pull Request (PR) e faça o seu próprio review (para demonstrar conceitos de reivew) e faça merge para a `main` a partir do PR.
8. Faça o **Push** final de todas as suas branches até a data **12/06/2026**.

