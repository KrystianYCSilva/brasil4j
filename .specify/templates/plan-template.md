# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]

**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command; its definition describes the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION]

**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]

**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files or N/A]

**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIFICATION]

**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEEDS CLARIFICATION]

**Project Type**: [e.g., library/cli/web-service/mobile-app/compiler/desktop-app or NEEDS CLARIFICATION]

**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k lines/sec, 60 fps or NEEDS CLARIFICATION]

**Constraints**: [domain-specific, e.g., <200ms p95, <100MB memory, offline-capable or NEEDS CLARIFICATION]

**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50 screens or NEEDS CLARIFICATION]

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
| Responsibilities and types | [types introduced or changed, one responsibility each] | | DQ-7…DQ-11 |
| Patterns | [applied pattern → problem it solves] | [pattern considered → why not] | DP-* |
| Public API impact | [new/changed members; binary, source and behavioral compatibility] | | JA-8…JA-14, KA-4…KA-12 |
| Consumer extensibility | [how a consumer adds a document/rule/format] | | DQ-16 |
| Architecture | [packages and dependencies touched] | | AR-* |
| Test strategy | [contract, characterization, equivalence, JarIT, mutation impact] | | TQ-* |

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure: feature modules, UI flows, platform tests]
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
