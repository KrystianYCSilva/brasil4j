---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `specs/[NNN-name]/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: MANDATORY (constitution, principle III — TDD). Each invariant gets a test written first, run red for the expected reason, then green and refactor; record red/green/refactor evidence.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Core: `brasil4j-core/src/main/java/io/github/brasil4j/[package]/`; tests in `brasil4j-core/src/test/java/io/github/brasil4j/[package]/`
- Kotlin adapter: `brasil4j-kotlin/src/main/kotlin/`; tests in `brasil4j-kotlin/src/test/kotlin/`
- Adjust to the plan.md structure

<!--
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Types from data-model.md
  - Public API from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create `feature/NNN-name` from `develop` and record the base commit (VII.1)
- [ ] T002 Characterization tests for behavior the change touches; baseline `clean verify` on JDK 8 and 21 (TQ-2)
- [ ] T003 Design review of the plan's Design Decisions against `.context/standards/review-checklist.md` (VI); fix the plan before code

- [ ] T004 [P] When applicable: abstract contract test for every new public interface, extended by each implementation (TQ-3)
- [ ] T005 [P] When applicable: equivalence test against the old algorithm before replacing it; frozen vectors before deletion (TQ-2)
- [ ] T006 When applicable: ArchUnit rule update for any new package or dependency (TQ-9, AR-*)
- [ ] T007 When applicable, after the last code task: japicmp vs latest release tag and mutation threshold (IV.4, IV.5)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust to the plan):

- [ ] T008 Create the package and `package-info.java` under `brasil4j-core/src/main/java/io/github/brasil4j/[package]/`
- [ ] T009 [P] Define the public interface/value type in `brasil4j-core/src/main/java/io/github/brasil4j/[package]/[Type].java` (JA-*)
- [ ] T010 [P] Shared test fixtures/frozen vectors in `brasil4j-core/src/test/java/io/github/brasil4j/[package]/`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (MANDATORY — TDD, principle III.1) ⚠️

> Write these tests FIRST and run them red for the expected reason

- [ ] T011 [P] [US1] Behavior test for [rule] in brasil4j-core/src/test/java/io/github/brasil4j/[package]/[Type]Test.java
- [ ] T012 [P] [US1] Kotlin adapter test in brasil4j-kotlin/src/test/kotlin/[package]/[Type]Test.kt

### Implementation for User Story 1

- [ ] T013 [P] [US1] Implement [Type] in brasil4j-core/src/main/java/io/github/brasil4j/[package]/[Type].java
- [ ] T014 [US1] Validate input and signal invalid values via exceptions/results, no logging (AR-8)
- [ ] T015 [US1] Add Kotlin extension in brasil4j-kotlin/src/main/kotlin/[package]/[Type].kt (KA-*)
- [ ] T016 [US1] Refactor with the suite green; record red/green/refactor evidence

**Checkpoint**: User Story 1 is functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (MANDATORY — TDD, principle III.1) ⚠️

> Write these tests FIRST and run them red for the expected reason

- [ ] T017 [P] [US2] Behavior test for [rule] in brasil4j-core/src/test/java/io/github/brasil4j/[package]/[Type]Test.java
- [ ] T018 [P] [US2] Kotlin adapter test in brasil4j-kotlin/src/test/kotlin/[package]/[Type]Test.kt

### Implementation for User Story 2

- [ ] T019 [P] [US2] Implement [Type] in brasil4j-core/src/main/java/io/github/brasil4j/[package]/[Type].java
- [ ] T020 [US2] Validate input and signal invalid values via exceptions/results, no logging (AR-8)
- [ ] T021 [US2] Add Kotlin extension in brasil4j-kotlin/src/main/kotlin/[package]/[Type].kt (KA-*)
- [ ] T022 [US2] Refactor with the suite green; record red/green/refactor evidence

**Checkpoint**: User Story 2 is functional and testable independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (MANDATORY — TDD, principle III.1) ⚠️

> Write these tests FIRST and run them red for the expected reason

- [ ] T023 [P] [US3] Behavior test for [rule] in brasil4j-core/src/test/java/io/github/brasil4j/[package]/[Type]Test.java
- [ ] T024 [P] [US3] Kotlin adapter test in brasil4j-kotlin/src/test/kotlin/[package]/[Type]Test.kt

### Implementation for User Story 3

- [ ] T025 [P] [US3] Implement [Type] in brasil4j-core/src/main/java/io/github/brasil4j/[package]/[Type].java
- [ ] T026 [US3] Validate input and signal invalid values via exceptions/results, no logging (AR-8)
- [ ] T027 [US3] Add Kotlin extension in brasil4j-kotlin/src/main/kotlin/[package]/[Type].kt (KA-*)
- [ ] T028 [US3] Refactor with the suite green; record red/green/refactor evidence

**Checkpoint**: User Story 3 is functional and testable independently

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation: README, CHANGELOG, `docs/`, contracts and quickstart; new examples in `brasil4j-exemplos` without changing existing ones (TQ-8)
- [ ] TXXX Final gates: `clean verify` JDK 8 and 21, bytecode, japicmp vs latest release tag, mutation (IV)
- [ ] TXXX Independent LLM review with `.context/workflows/llm-review.md`; fix MUST FIX with TDD and record verdicts in `analysis.md` (VI.9)
- [ ] TXXX Open the PR from `feature/*` into `develop` with all required checks green (VII.2)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation (III.1)
- Types before their consumers
- Core before the Kotlin adapter
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Types within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Behavior test for [rule] in brasil4j-core/src/test/java/io/github/brasil4j/[package]/[Type]Test.java"
Task: "Kotlin adapter test in brasil4j-kotlin/src/test/kotlin/[package]/[Type]Test.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
