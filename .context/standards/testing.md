# Testing (III, VI.8, IV.3, IV.5)

## Process

- **TQ-1 TDD** One invariant per cycle: failing test for the expected reason → smallest change → refactor green. Record each step in the spec's `analysis.md`.
- **TQ-2 Characterize before refactoring** Pin current behavior (messages, exception types, `toString`, edge cases) before changing structure; replace an algorithm only after an equivalence test against the old one (freeze vectors when the old code is removed).

## Quality

- **TQ-3 Contract tests** Every public interface has an abstract contract test (`DocumentParserContractTest<D>`) that each implementation, including a consumer-style example, extends (LSP).
- **TQ-4 Assert behavior** Assert observable results and contracts, never private state; no assertion-free tests; no conditional logic inside tests.
- **TQ-5 Parametrize** Cases sharing one rule use `@ParameterizedTest` with named, traceable vectors (`docs/fontes.md`); synthetic data only.
- **TQ-6 Exact failures** Assert exact exception types (`assertThrowsExactly`) and messages where they are contract.
- **TQ-7 Consumer tests** `*JarIT` tests run against packaged JARs (Failsafe) in Java and Kotlin and assert `codeSource`.
- **TQ-8 Examples are tests** `brasil4j-exemplos` is didactic and must pass unchanged unless the change is a documented break.
- **TQ-9 Architecture tests** Package and module rules from [architecture](../patterns/architecture.md) run as ArchUnit tests.
- **TQ-10 Mutation testing** PIT (`-Pmutation`, JDK 21) on the Java core; the build fails below the threshold. Heavy vector suites (`*EquivalenceTest`) are excluded from mutation runs. Threshold: `pitest.mutationThreshold` in `brasil4j-core/pom.xml` (raise only). Baseline 2026-11-28: 89.5% (826/923); lowest packages `financas`, `identidade.cnpj`, `identidade.nis` (~84–86%).
- **TQ-11 Smells to reject** Tests that depend on order or time, sleep, network or the file system outside `src/test/resources`; duplicated setup instead of fixtures; tests named after methods instead of behavior.

## Gates

JaCoCo ≥90% lines and ≥85% branches per module; JDK 8 and 21; DocLint and Dokka clean.
