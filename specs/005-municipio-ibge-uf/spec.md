# Feature Specification: Código IBGE de município e UF

**Feature Branch**: `005-municipio-ibge-uf` (identificador da spec)
**Created**: 2026-11-26
**Status**: Approved — aceite de Krystian em 2026-11-26
**Input**: Recorte estrutural aprovado para descoberta no C1, com consumidores em Obrigações Core e Censo Superior/UEM.

**Aditamento**: Em 2026-11-26, Krystian solicitou nome, sigla, capital, DDDs e busca por nome na mesma v0.2.0. Na API, sigla representa o acrônimo da UF.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Interpretar o código municipal (Priority: P1)

Como mantenedor de um cadastro ou integração, quero interpretar um código IBGE como texto para preservar seu valor e decompor o prefixo de UF.

**Why this priority**: O valor pode ser compartilhado entre cadastro e consulta mantendo a forma canônica.

**Independent Test**: Interpretar os quatro exemplos do C1 e comparar valor e prefixo.

**Acceptance Scenarios**:

1. Dado `3550308`, ao interpretar o código, obter `3550308` e prefixo `35`.
2. Dado um prefixo sem UF vigente, como em `0012345`, ao interpretar o código, preservar o texto e o prefixo `00`.
3. Dado nulo, vazio, sinal, espaço, pontuação, dígitos Unicode ou comprimento diferente de sete, a interpretação falha.

### User Story 2 - Conferir coerência com a UF (Priority: P2)

Como mantenedor de cadastro, quero mapear o prefixo para uma UF conhecida e comparar a UF informada com a estrutura do código.

**Why this priority**: O Core confere a UF informada e o Censo consulta por código e, opcionalmente, UF.

**Independent Test**: Conferir as 27 UFs contra a fonte e comparar `3550308` com SP e RJ.

**Acceptance Scenarios**:

1. Dado `4115200`, resolver PR e confirmar a coerência com PR.
2. Dado `0012345`, a resolução fica vazia e a conferência retorna falso tanto com `null` quanto com uma UF informada. Dado `3550308` e RJ, a resolução contém SP e a conferência retorna falso.
3. Dado o vetor sintético `3599999`, o parser preserva o texto, resolve SP e retorna verdadeiro ao comparar com SP. O resultado vale para a estrutura e o prefixo.

### User Story 3 - Consultar dados da UF (Priority: P2)

Como consumidor de cadastros, quero consultar dados geográficos estáveis e localizar uma UF pelo nome completo.

**Independent Test**: Conferir os dados das 27 UFs nas fontes e buscar `São Paulo`, `sao paulo` e um nome inválido.

**Acceptance Scenarios**:

1. SP retorna nome e capital `São Paulo`, sigla `SP`, código IBGE `35` e DDDs `11` a `19`.
2. A busca aceita diferença de caixa, acentos e espaços externos. Nulo, vazio, sigla e nome desconhecido retornam ausência.
3. A lista de DDDs é imutável, ordenada e contém códigos distintos da UF. `61` está em GO e DF.

### Edge Cases

- A validação é estrutural. O consumidor controla existência, vigência, jurisdição histórica e adaptação de localidade estrangeira.
- O código é textual e preserva zeros à esquerda. O adaptador numérico do Censo fica na borda do serviço. Ao receber um `Long`, a apresentação original já foi descartada. Recompor o texto com `String.valueOf(long)` permite conferir os códigos canônicos com UF reconhecida, mas `0012345` perde zeros no caminho numérico. A spec preserva a assinatura atual do serviço e testa a diferença em isolamento.

## Requirements *(mandatory)*

- **FR-001** — Interpretar exatamente sete dígitos ASCII sem normalização e preservar valor canônico e prefixo.
- **FR-002** — Resolver o prefixo conhecido para uma das 27 UFs usando código textual explícito e comparar com UF informada.
- **FR-003** — Preservar a distinção de entrada inválida, prefixo desconhecido e divergência de UF. Igualdade por valor, hash e representação textual usam o valor canônico.
- **FR-004** — Expor o contrato Java e Kotlin conforme o plano e confirmar os casos do C1 pelos JARs em JDK 8 e 21.

- **FR-005** — Expor nome oficial, sigla, capital e DDDs de cada UF. Buscar por nome completo, sem distinção de caixa ou acentos e com remoção de espaços externos.

### Key Entities

- **Código de município IBGE** — objeto de valor estrutural com sete caracteres.
- **UF** — enum das unidades federativas com código IBGE textual e sigla estável.

## Success Criteria *(mandatory)*

- **SC-001** — Os quatro vetores dos consumidores, conferidos na tabela oficial, preservam o texto e produzem a UF esperada.
- **SC-002** — Vetores positivos, sintéticos, de apresentação inválida, UF divergente, prefixo desconhecido com UF nula e representação textual canônica passam nos contratos Java e Kotlin.
- **SC-003** — Código e prefixo de todas as 27 UFs conferem com o [snapshot de referência](research.md#snapshot-uf), sem duplicidade.
- **SC-004** — Os dados das 27 UFs conferem com IBGE e Anatel/MCom. Os testes cobrem busca normalizada, rejeição de nomes incompletos e imutabilidade dos DDDs.

## Assumptions

A aceitação do código na biblioteca indica apenas sua estrutura. DV, enum de região, catálogo municipal e integrações ficam para incrementos próprios. DDD é dado de referência agregado por UF e pode ser compartilhado.
