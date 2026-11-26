---
description: "Tarefas de CNPJ"
---

# Tasks: CNPJ

**Input**: [Design documents](plan.md)  
**Prerequisites**: [spec](spec.md), [research](research.md), [data model](data-model.md), [contracts](contracts/api.md)  
**Tests**: TDD antes do código de domínio.

## Format: [ID] [P?] [Story] Description

As tarefas indicam história, caminho e requisito. As fases seguem ordem sequencial.

## Path Conventions

Java fica em brasil4j-core e Kotlin em brasil4j-kotlin.

## Phase 1: Setup

**Goal**: Ler fontes e matriz

- [x] T001 [US1] Conferir `docs/fontes.md` e `docs/descoberta/c1/matriz-cnpj.md` (FR-001, FR-002).

## Phase 2: Foundational

**Goal**: Preparar suíte Java

- [x] T002 [US1] Escrever casos e obter red em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/cnpj/CnpjTest.java` (FR-001 a FR-005).

## Phase 3: User Story 1

**Goal**: Interpretar no modo padrão

- [x] T003 [US1] Implementar Cnpj, CnpjFormatter, CnpjValidator e DV em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/cnpj/` (FR-001, FR-003 a FR-005).

## Phase 4: User Story 2

**Goal**: Introduzir modos explícitos

- [x] T004 [US2] Integrar as estratégias padrão, NUMERIC e FORMAT_ONLY no mesmo pacote e confirmar casos positivos e negativos (FR-002).

## Phase 5: User Story 3

**Goal**: Entregar o consumo Kotlin

- [x] T005 [US3] Escrever testes Kotlin, observar red e implementar `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Cnpj.kt` (FR-006).

## Phase 6: Polish & Cross-Cutting Concerns

**Goal**: Verificar os artefatos

- [x] T006 [US3] Rodar contratos empacotados, cobertura e JavaDoc em JDK 8 e 21 (FR-001 a FR-006).

## Dependencies & Execution Order

As fases seguem a ordem listada. Os testes da história precedem sua implementação e as verificações finais usam JDK 8 e 21.

## Implementation Strategy

Entregar e verificar cada história em ordem. Marcar a tarefa somente após conferir seu resultado.

## Evidências de execução

- **Red.** Os contratos Java e Kotlin foram escritos antes das APIs e falharam pela ausência dos tipos e extensões. O teste de regressão posterior demonstrou que a estratégia interna antiga aceitava texto bruto inválido.
- **Green.** A suíte executou por JAR os 27 vetores históricos e os 26 vetores sintéticos de A a Z. Os testes diretos da estratégia rejeitaram texto arbitrário, nulo e máscaras parciais. JDK 8 e 21 passaram.
- **Refactor.** Cada estratégia agora interpreta e valida toda a entrada antes de devolver o canônico. O cálculo em comum manteve `char - '0'`, documentado como ASCII menos 48. A [pesquisa](research.md) registra a comparação com os resultados do código oficial.
