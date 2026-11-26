# Feature Specification: CPF

**Feature Branch**: `003-cpf` (identificador lógico)  
**Created**: 2026-11-25  
**Status**: Approved  
**Input**: Criar objeto de valor, validação estrutural padrão, DV numérico opcional, formatação e extensão Kotlin.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Normalizar CPF sem exigir DV (Priority: P1)

Como consumidor, quero interpretar CPF que tenha formato válido, inclusive quando a conferência de DV foi delegada.

**Why this priority**: É o comportamento padrão solicitado.

**Independent Test**: Interpretar CPF mascarado de DV divergente e recuperar canônico.

**Acceptance Scenarios**:

1. Dado 280.012.389-39, quando usado o padrão, então se obtém o canônico 28001238939.

---

### User Story 2 - Conferir DV sob demanda (Priority: P2)

Como integrador, quero conferir o módulo 11 quando o contrato externo exigir.

**Why this priority**: A verificação é opcional e tem efeito observável.

**Independent Test**: Executar o vetor de referência e uma mutação do DV.

**Acceptance Scenarios**:

1. Dadas entradas com o mesmo formato e DVs 38 e 39 para a base 280012389, quando usado NUMERIC, então apenas a primeira passa.

---

### User Story 3 - Usar o valor em Kotlin com representação segura (Priority: P3)

Como integrador Kotlin, quero um valor equivalente ao Java que preserve seus dados, inclusive zeros.

**Why this priority**: A API idiomática precisa manter a mesma semântica do núcleo.

**Independent Test**: Converter o valor Kotlin para Java e verificar igualdade e representação.

**Acceptance Scenarios**:

1. Dado CPF de formato válido, quando convertido em texto implicitamente, então a saída é ***.***.***-**.

### Edge Cases

- Nulo, vazio, Unicode, espaços e máscara parcial falham estruturalmente em ambos os modos.
- Um DV divergente passa somente na conferência de formato. O valor continua devolvendo o DV informado.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** — Aceitar 11 dígitos ASCII em forma canônica ou na máscara exata XXX.XXX.XXX-XX, preservar zeros e bloquear outras apresentações.
- **FR-002** — Aplicar FORMAT_ONLY por padrão e NUMERIC para acrescentar conferência do módulo 11. Sequências repetidas usam as mesmas regras matemáticas.
- **FR-003** — Fornecer parse, isValid, format e unformat. O formato inválido produz false ou IllegalArgumentException conforme a API, sem reproduzir a entrada. Modo nulo é erro de configuração.
- **FR-004** — Expor canônico, base, DVs informados, applyMask, hasValidCheckDigits, igualdade e hash pelo canônico e toString com ocultação total.
- **FR-005** — Oferecer a value class Kotlin e extensões delegadas ao Java, com o mesmo padrão e modo explícito.

### Key Entities

- Cpf é um valor imutável identificado por 11 caracteres canônicos. A base tem nove caracteres e os DVs informados são os dois últimos. CpfValidationMode modela a decisão de conferir o algoritmo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** — A conferência numérica reproduz o exemplo publicado no manual da e-Financeira.
- **SC-002** — Java e Kotlin concordam no modo padrão, no modo explícito, no canônico e na representação segura.

## Assumptions

- O ciclo é sequencial. Os serviços consumidores controlam adoção e compatibilidade cadastral.
- As evidências usam casos públicos ou sintéticos. A [tabela de fontes](../../docs/fontes.md) identifica a autoridade de cada regra.
