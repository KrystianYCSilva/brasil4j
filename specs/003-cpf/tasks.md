---
description: "Tarefas de CPF"
---

# Tasks: CPF

**Input**: [Design documents](plan.md)  
**Prerequisites**: [spec](spec.md), [research](research.md), [data model](data-model.md), [contracts](contracts/api.md)  
**Tests**: TDD antes do código de domínio.

## Format: [ID] [P?] [Story] Description

As tarefas indicam história, caminho e requisito. As fases seguem ordem sequencial.

## Path Conventions

Java fica em brasil4j-core e Kotlin em brasil4j-kotlin.

## Phase 1: Setup

**Goal**: Rastrear a referência do CPF

- [x] T001 [US1] Registrar regra, modo padrão e vetor publicado em `docs/fontes.md` (FR-001, FR-002).

## Phase 2: Foundational

**Goal**: Escrever os testes antes da API

- [x] T002 [US1] Observar red em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/cpf/CpfTest.java` (FR-001 a FR-004).

## Phase 3: User Story 1

**Goal**: Interpretar e formatar

- [x] T003 [US1] Implementar valor e formatter em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/cpf/` (FR-001, FR-003, FR-004).

## Phase 4: User Story 2

**Goal**: Conferir o DV por modo

- [x] T004 [US2] Implementar FORMAT_ONLY e NUMERIC no mesmo pacote, com teste do vetor público e mutação (FR-002).

## Phase 5: User Story 3

**Goal**: Expor a value class

- [x] T005 [US3] Observar red e implementar `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Cpf.kt` (FR-005).

## Phase 6: Polish & Cross-Cutting Concerns

**Goal**: Verificar a entrega

- [x] T006 [US3] Rodar contratos empacotados, cobertura e JavaDoc em JDK 8 e 21 (FR-001 a FR-005).

## Dependencies & Execution Order

As fases seguem a ordem listada. Os testes da história precedem sua implementação e as verificações finais usam JDK 8 e 21.

## Implementation Strategy

Entregar e verificar cada história em ordem. Marcar a tarefa somente após conferir seu resultado.

## Evidências de execução

- **Red.** Os testes de contrato falharam antes de existirem o valor Java e o adaptador Kotlin. O teste de regressão posterior reproduziu o atalho de verificação de formato no modo interno.
- **Green.** O valor com DV divergente passou em FORMAT_ONLY e falhou em NUMERIC. Os testes cobriram o exemplo público, zeros, máscaras e a ocultação em Java e Kotlin. Na implementação inicial, foram 183 testes no reator em cada JDK, com os limites de cobertura atendidos.
- **Refactor.** A estratégia de formato passou a validar a entrada diretamente. A estratégia numérica também valida a apresentação antes de conferir o DV. Os dois módulos mantiveram igualdade e representação conforme a spec.
