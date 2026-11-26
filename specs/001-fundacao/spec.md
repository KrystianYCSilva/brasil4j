# Feature Specification: Fundação JVM

**Feature Branch**: `001-fundacao` (identificador lógico)  
**Created**: 2026-11-25  
**Status**: Approved  
**Input**: Estruturar dois artefatos Maven, com namespace neutro, verificação Java 8/21 e documentação de API.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Publicar artefatos locais consistentes (Priority: P1)

Como mantenedor, quero produzir os dois JARs com as mesmas coordenadas, para que consumidores possam escolher o artefato apropriado.

**Why this priority**: O build e o namespace sustentam os próximos incrementos.

**Independent Test**: Executar verify e inspecionar coordenadas, dependências de runtime e JARs.

**Acceptance Scenarios**:

1. Dado o reator, quando executado em JDK 8 e 21, então produz os dois JARs de io.github.brasil4j.

---

### User Story 2 - Conferir contratos e qualidade (Priority: P2)

Como mantenedor, quero detectar regressões de Java 8 e documentação de API durante a verificação.

**Why this priority**: A detecção protege os consumidores e sustenta a evolução do contrato.

**Independent Test**: Rodar as etapas de CI e conferir verificações dos JARs e JavaDoc.

**Acceptance Scenarios**:

1. Dado código de domínio, quando verificado, então bytecode, JavaDoc e cobertura satisfazem os limites da constituição.

### Edge Cases

- Falha de ferramenta incompatível é detectada pelo Enforcer.
- A primeira fundação pode empacotar módulos vazios. A verificação de bytecode exige classes a partir das specs de domínio.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** — Produzir os JARs brasil4j-core e brasil4j-kotlin sob io.github.brasil4j. O núcleo depende do JDK e Kotlin acrescenta somente o núcleo e sua biblioteca padrão.
- **FR-002** — Provar compatibilidade com APIs Java 8 e bytecode 52 em JDK 8 e 21.
- **FR-003** — Verificar JavaDoc com DocLint em JDK 8 e 21 e KDoc das APIs Kotlin com Dokka no verify do JDK 21. Nas capacidades de domínio, aplicar 90% de cobertura de linhas e 85% de branches.
- **FR-004** — Manter rastreabilidade do SDD, requisitos, arquitetura e matriz histórica do C1.

### Key Entities

- O reator e seus dois artefatos são unidades de entrega. A fundação não introduz valores de domínio.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** — Dois JARs produzidos em ambos os JDKs.
- **SC-002** — Todos os requisitos têm evidência verificável nas etapas do build.

## Assumptions

- O ciclo é sequencial. Os serviços consumidores controlam adoção e compatibilidade cadastral.
- As evidências usam casos públicos ou sintéticos. A [tabela de fontes](../../docs/fontes.md) identifica a autoridade de cada regra.
