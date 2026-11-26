---
description: "Tarefas de Fundação JVM"
---

# Tasks: Fundação JVM

**Input**: [Design documents](plan.md)  
**Prerequisites**: [spec](spec.md), [research](research.md), [data model](data-model.md), [contracts](contracts/api.md)  
**Tests**: TDD antes do código de domínio.

## Format: [ID] [P?] [Story] Description

As tarefas indicam história, caminho e requisito. As fases seguem ordem sequencial.

## Path Conventions

Java fica em brasil4j-core e Kotlin em brasil4j-kotlin.

## Phase 1: Setup

**Goal**: Preparar arquivos do reator

- [x] T001 [US1] Introduzir POM pai e módulos em `pom.xml`, `brasil4j-core/pom.xml` e `brasil4j-kotlin/pom.xml` (FR-001).

## Phase 2: Foundational

**Goal**: Configurar plataforma comum

- [x] T002 [US1] Fixar Wrapper, Enforcer, release 8 e compilação Kotlin em `.mvn/wrapper/` e nos POMs (FR-002).

## Phase 3: User Story 1

**Goal**: Validar coordenadas e JARs

- [x] T003 [US1] Executar build em ambos os JDKs e inspecionar dependências e JARs (FR-001, FR-002).

## Phase 4: User Story 2

**Goal**: Instrumentar qualidade

- [x] T004 [US2] Configurar CI com SHAs fixos, bytecode, JavaDoc, Dokka no JDK 21 e JaCoCo em `.github/workflows/verify.yml`, `scripts/check-bytecode.py` e nos POMs (FR-002, FR-003).

## Phase 5: Polish & Cross-Cutting Concerns

**Goal**: Manter rastreabilidade

- [x] T005 [US2] Atualizar `README.md`, `.context/`, `docs/` e a constituição, preservando a matriz do C1 (FR-004).

## Dependencies & Execution Order

As fases seguem a ordem listada. Os testes da história precedem sua implementação e as verificações finais usam JDK 8 e 21.

## Implementation Strategy

Entregar e verificar cada história em ordem. Marcar a tarefa somente após conferir seu resultado.

## Evidências de execução

- O Spec Kit resolveu as três specs com pesquisa, modelo, contratos, quickstart e tarefas. A análise cruzada mapeou os requisitos às tarefas e validou os links.
- O Maven Wrapper produziu os dois JARs usando Corretto 8u512 e 21.0.13. O verificador conferiu todas as classes com bytecode 52. O núcleo possui zero dependências de runtime e o módulo Kotlin usa o núcleo e a biblioteca padrão.
- O Git confirmou a exclusão de arquivos locais de IA e temporários. O hash da matriz histórica do C1 coincide com a versão anterior à implementação.
### Prontidão da v0.1.0

- Corretto 8u512 e 21.0.13: 193 testes por JDK, DocLint, JaCoCo e bytecode 52.
- Dokka no JDK 21 gerou a referência Kotlin. Em cópia descartável, omitir o KDoc de Cpf.toJava fez o verify reprovar por documentação ausente.
- POMs e consumo pelos JARs confirmam o núcleo sem dependências de runtime e o módulo Kotlin com núcleo e biblioteca padrão.
- Claude e Antigravity revisaram os mesmos 81 arquivos originais. Os ajustes do parecer Claude entraram nesta rodada.
