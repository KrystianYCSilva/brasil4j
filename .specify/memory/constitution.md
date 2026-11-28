# brasil4j Constitution

Rules are numbered so plans, tasks and reviews can cite them (for example `VI.4`). Detail lives in `.context/`; this file only states the rules. Human-facing docs (specs, README, `docs/`) stay in Brazilian Portuguese; AI-facing files are in English.

## Core Principles

### I. SDD: contract before code

- **I.1** Every increment defines observable behavior in a spec, records decisions in a plan, derives tasks and passes a consistency analysis before implementation.
- **I.2** Public API changes record consumer impact and binary/source/behavioral compatibility in the plan.
- **I.3** The plan's Design Quality Gate (principle VI) and Design Decisions section are mandatory.

### II. DDD: invariants and boundaries

- **II.1** Brazilian domain values (Identity, Geography, Finance) are immutable value objects; the rule lives in the type that owns the invariant.
- **II.2** Registration, ownership, status and external effects belong to consumers.
- **II.3** A new bounded context requires an identified demand and its own contract. Contexts do not depend on each other; shared building blocks (`verificacao`, `mascara`) depend on no context. [Architecture](../../.context/patterns/architecture.md)

### III. TDD: one invariant per cycle

- **III.1** Write a deterministic behavior test, watch it fail for the expected reason, make the smallest change that passes, then refactor with the suite green. Record red/green/refactor.
- **III.2** Cover boundaries, modes, invalid input and regressions; characterize current behavior before refactoring it.
- **III.3** Exercise the packaged JARs as Java and Kotlin consumers.

### IV. Platform and build gates

- **IV.1** Runtime minimum is Java 8: Java 8 APIs, bytecode 52, Maven Wrapper and Enforcer; `clean verify` passes on JDK 8 and 21.
- **IV.2** `brasil4j-core` depends only on the JDK at runtime; `brasil4j-kotlin` depends only on the core and the Kotlin stdlib.
- **IV.3** JaCoCo from the domain: ≥90% lines and ≥85% branches per module.
- **IV.4** japicmp against the latest release tag fails on binary or source incompatibility.
- **IV.5** Mutation score of the Java core does not drop below the threshold in `.context/standards/testing.md`.
- **IV.6** Public docs are part of the contract: DocLint on JDK 8/21, Dokka on JDK 21; generated docs stay in build directories.

### V. Provenance and scope

- **V.1** Independent implementation under Apache-2.0.
- **V.2** Every rule records authority, revision and evidence in `docs/fontes.md`; tests use official or verified synthetic vectors.
- **V.3** Validate presentation before normalization, preserve leading zeros and make tolerances explicit.
- **V.4** CPF and NIS are hidden in the default text representation; library diagnostics never contain input, canonical value or formatted output.
- **V.5** A rule is called "official" only with a confirmed normative source (technical note, ordinance, normative instruction, official manual or layout) recording authority, version or validity and scope. Otherwise describe it as a formula published in a named document with its stated scope, or as a technical convention observed in named systems.
- **V.6** A rule with restricted or unconfirmed scope is only an explicit consumer option, never default behavior.

### VI. Design and test quality

Reviewers and planners apply the lenses in [`review-checklist.md`](../../.context/standards/review-checklist.md); each finding cites a rule ID.

- **VI.1 Clean code and SOLID.** Intent-revealing names, small single-purpose units, guard clauses, no boolean mode flags; SRP, OCP, LSP, ISP and DIP applied as in [design-quality](../../.context/standards/design-quality.md).
- **VI.2 DRY, KISS, YAGNI and reuse.** One source per rule; extend by composition over inheritance; no speculative abstraction; the simplest design that expresses the rule wins.
- **VI.3 Contracts and extensibility.** The library exposes public contracts for value, parsing, rule, result and presentation, and builds its own types with the same public blocks a consumer would use. Injectable strategies over static utilities.
- **VI.4 Design patterns.** A pattern is applied only to solve a named problem (variation, coupling, duplication or consumer extensibility); the plan names patterns considered, applied and rejected. [Catalog](../../.context/patterns/design-patterns.md)
- **VI.5 Java library API.** Minimal visibility, immutability, explicit compatibility rules and staged deprecation as in [java-library-api](../../.context/standards/java-library-api.md).
- **VI.6 Kotlin library API and functional style.** The adapter delegates to the core; explicit API mode; idiomatic null safety, immutability and pure functions as in [kotlin-library-api](../../.context/standards/kotlin-library-api.md).
- **VI.7 Architecture conformance.** Package and module dependency rules are verified by automated tests.
- **VI.8 Test quality.** Tests assert behavior, not implementation; every public interface has a reusable contract test; refactors are guarded by characterization or equivalence tests; mutation testing measures test strength. [Testing](../../.context/standards/testing.md)
- **VI.9 Independent review.** Before integration, a second model reviews the change with the checklist lenses. [LLM review](../../.context/workflows/llm-review.md)

### VII. Delivery

- **VII.1** Git flow: work happens on `feature/*` branches from `develop`; releases go `release/*` → PR → `main` → tag `vX.Y.Z`; hotfixes go `hotfix/*` from `main`. [Git flow](../../.context/workflows/git-flow.md)
- **VII.2** No direct pushes to `main` or `develop`: every change enters through a pull request with all required checks green, without bypass.
- **VII.3** Required checks are the CI jobs that enforce principle IV; rulesets and repository settings are versioned in `.github/` and applied by script.
- **VII.4** Every release has a tag, a GitHub Release and a CHANGELOG entry; the version on `develop` is the next `-SNAPSHOT`.

## Governance

- **Amendments.** One writer per round. Apply the Spec Kit procedure, record version and impact, and align affected versioned artifacts in their own step. `AGENTS.md` and the `.context` index guide reading. Remove the temporary impact report before committing.
- **Versioning.** MAJOR for incompatible removal or redefinition, MINOR for a new or materially expanded principle or guidance, PATCH for wording without change of meaning.
- **Compliance.** Review this constitution before research and after design. Analyze spec, plan and tasks before implementation. Justify exceptions in the plan. Record execution evidence, sources and ISO 8601 dates.

**Version**: 0.6.0 | **Ratified**: 2026-11-26 | **Last Amended**: 2026-11-28
