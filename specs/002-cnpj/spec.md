# Feature Specification: CNPJ

**Feature Branch**: `002-cnpj` (identificador lógico)  
**Created**: 2026-11-25  
**Status**: Approved  
**Input**: Validar CNPJ numérico e alfanumérico com estratégias selecionáveis, formatar e expor um objeto de valor Java e Kotlin.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Interpretar CNPJ no modo padrão (Priority: P1)

Como desenvolvedor, quero obter um CNPJ canônico a partir das duas apresentações aceitas.

**Why this priority**: É a entrada habitual do cadastro de empresas.

**Independent Test**: Executar a matriz do C1 com os dois tipos de base.

**Acceptance Scenarios**:

1. Dada a entrada 12.ABC.345/01DE-35, quando interpretada, então se obtém 12ABC34501DE35 com DV 35.

---

### User Story 2 - Escolher o nível de verificação (Priority: P2)

Como integrador, quero restringir a base a números ou verificar apenas apresentação.

**Why this priority**: Consumidores têm requisitos diferentes de compatibilidade.

**Independent Test**: Exercitar os modos com a mesma entrada.

**Acceptance Scenarios**:

1. Dado DV divergente, quando escolhido FORMAT_ONLY, então a entrada válida em formato é aceita. Quando escolhido um modo de DV, ela é rejeitada.

---

### User Story 3 - Consumir valor e apresentação em Kotlin (Priority: P3)

Como desenvolvedor Kotlin, quero a mesma regra do núcleo em uma value class e em extensões.

**Why this priority**: O consumo em outra linguagem deve preservar igualdade e comportamento.

**Independent Test**: Usar o JAR do módulo Kotlin com o JAR do núcleo.

**Acceptance Scenarios**:

1. Dadas as formas canônica e com máscara da mesma inscrição, quando interpretadas em Kotlin, então produzem valores iguais e a mesma representação canônica.

### Edge Cases

- Zeros à esquerda e DVs 00 são preservados. A sequência de 14 zeros passa apenas no modo de formato.
- DV com letra, espaços, minúsculas, Unicode, pontuação deslocada e máscara parcial são inválidos em todos os modos.
- O tipo descreve a presença de letras e independe do modo escolhido.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** — Aceitar apenas base ASCII A–Z ou 0–9 com 12 posições e dois DVs ASCII numéricos, nas formas canônica ou máscara exata XX.XXX.XXX/XXXX-DD.
- **FR-002** — Fornecer ALPHANUMERIC padrão com DV para os dois tipos de base, NUMERIC com DV e base exclusivamente numérica, e FORMAT_ONLY com verificação exclusiva da apresentação. O bloqueio de 14 zeros aplica-se aos modos de DV.
- **FR-003** — Expor parse, validador e formatter. O formatter textual usa o padrão. Um valor construído por qualquer modo pode aplicar sua máscara.
- **FR-004** — Expor canônico, base, raiz, ordem, DVs e tipo em um valor imutável. A igualdade usa o canônico.
- **FR-005** — Retornar false para entradas nulas ou inválidas no validador. Parsing e formatador falham com IllegalArgumentException sem expor a entrada. Modo nulo é erro de configuração.
- **FR-006** — Expor value class, propriedades, conversão para Java e extensões Kotlin com modo padrão ou explícito.

### Key Entities

- Cnpj é um valor imutável definido pelo canônico de 14 caracteres. Base = raiz de oito e ordem de quatro. Os dois DVs são os caracteres finais. CnpjType representa a composição da base. CnpjValidationMode representa uma política de entrada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** — A matriz original de 27 casos é reproduzida no modo padrão.
- **SC-002** — Java e Kotlin demonstram o valor e os modos em JDK 8 e 21.

## Assumptions

- O ciclo é sequencial. Os serviços consumidores controlam adoção e compatibilidade cadastral.
- As evidências usam casos públicos ou sintéticos. A [tabela de fontes](../../docs/fontes.md) identifica a autoridade de cada regra.
