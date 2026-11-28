# Implementation Plan: [FEATURE]

**Branch**: `feature/[NNN-name]` | **Date**: [DATE] | **Spec**: [link]

**Input**: Feature specification from `specs/[NNN-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command; its definition describes the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 8 core (bytecode 52, JDK 8/21) + Kotlin adapter [or NEEDS CLARIFICATION]

**Primary Dependencies**: [none in core (AR-*); Kotlin stdlib in the adapter]

**Testing**: JUnit + ArchUnit, JaCoCo, PIT mutation, japicmp, JarIT (`.context/standards/testing.md`)

**Target Platform**: JVM library (Maven Central)

**Project Type**: library (`brasil4j-core`, `brasil4j-kotlin`)

**Performance Goals**: [domain-specific or N/A]

**Constraints**: [compatibility class, no new core dependencies, or NEEDS CLARIFICATION]

**Scale/Scope**: [packages and public API touched]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Cite rule IDs from `.specify/memory/constitution.md`. An unchecked item needs a justification in Complexity Tracking.

- [ ] I. SDD (I.1–I.3): spec clarified, plan and tasks derived, analysis run; consumer impact and compatibility recorded.
- [ ] II. DDD (II.1–II.3): rule lives in the type that owns the invariant; context boundaries respected.
- [ ] III. TDD (III.1–III.3): red/green/refactor planned per invariant; characterization before refactor; JAR consumer tests.
- [ ] IV. Platform (IV.1–IV.6): Java 8 + bytecode 52, JDK 8/21, JaCoCo ≥90/85, japicmp vs latest release tag, mutation threshold, DocLint/Dokka.
- [ ] V. Provenance (V.1–V.6): sources in `docs/fontes.md`; "official" only with a confirmed normative source.
- [ ] VII. Delivery (VII.1–VII.4): `feature/*` branch from `develop`, PR with required checks, CHANGELOG.

### Design Quality Gate (VI — lenses from `.context/standards/review-checklist.md`)

- [ ] VI.1 Clean code and SOLID (DQ-1…DQ-11) — evidence in Design Decisions.
- [ ] VI.2 DRY / KISS / YAGNI / reuse (DQ-12…DQ-15) — existing blocks reused or reason given.
- [ ] VI.3 Contracts and extensibility (DQ-16…DQ-18) — how a consumer extends it without editing the library.
- [ ] VI.4 Patterns (DP-1…DP-9) — considered, applied and rejected patterns listed below.
- [ ] VI.5 Java library API (JA-1…JA-15) — visibility, immutability, compatibility class of every API change.
- [ ] VI.6 Kotlin API and functional style (KA-1…KA-13, KF-1…KF-4).
- [ ] VI.7 Architecture (AR-1…AR-8) — new packages/dependencies reflected in the ArchUnit test.
- [ ] VI.8 Test quality (TQ-1…TQ-11) — contract tests for new interfaces, equivalence for replaced algorithms.
- [ ] VI.9 Independent LLM review planned before the PR.

## Design Decisions

| Topic | Decision | Alternatives rejected and why | Rule IDs |
| --- | --- | --- | --- |
| Clean code | [naming, small functions, no dead code, error signalling] | | DQ-1…DQ-6 |
| DRY / KISS / YAGNI / reuse | [existing blocks reused; nothing speculative] | | DQ-12…DQ-15 |
| Java OO shape | [visibility, immutability, final/value types, static factories] | | JA-1…JA-7 |
| Kotlin adapter and functional style | [extensions over core, nullability, pure functions] | | KA-1…KA-3, KF-1…KF-4 |
| Responsibilities and types | [types introduced or changed, one responsibility each] | | DQ-7…DQ-11 |
| Patterns | [applied pattern → problem it solves] | [pattern considered → why not] | DP-* |
| Public API impact | [new/changed members; binary, source and behavioral compatibility] | | JA-8…JA-14, KA-4…KA-12 |
| Consumer extensibility | [how a consumer adds a document/rule/format] | | DQ-16 |
| Architecture | [packages and dependencies touched] | | AR-* |
| Test strategy | [contract, characterization, equivalence, JarIT, mutation impact] | | TQ-* |

## Project Structure

### Documentation (this feature)

```text
specs/[NNN-name]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
brasil4j-core/src/main/java/io/github/brasil4j/[package]/
brasil4j-core/src/test/java/io/github/brasil4j/[package]/
brasil4j-kotlin/src/main/kotlin/[package]/
brasil4j-kotlin/src/test/kotlin/[package]/
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
