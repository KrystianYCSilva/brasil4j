# AGENTS.md — brasil4j

Brazilian domain library for the JVM: immutable value objects and rules for CPF, CNPJ, NIS (Identity), IBGE municipality codes and UFs (Geography) and money, rounding and exchange (Finance). Java 8 core plus an idiomatic Kotlin adapter. Public, Apache-2.0.

## T0 directives

1. Read the [constitution](.specify/memory/constitution.md) before changing code, specs or docs; cite its rule IDs (`VI.4`) in plans and reviews.
2. Open only the `.context` files the task needs (index below). They are authoritative over general knowledge.
3. Spec Kit is the delivery workflow: specify → clarify → plan → tasks → analyze → implement. Plans pass the Design Quality Gate; reviews apply the [review checklist](.context/standards/review-checklist.md).
4. TDD for every behavior; the packaged JARs are tested as Java and Kotlin consumers.
5. Never call a rule "official" without a confirmed normative source in `docs/fontes.md` (V.5).
6. Git flow: work on `feature/*` from `develop`; no direct push to `main` or `develop`; every change goes through a PR with all checks green.
7. AI-facing files are in English; specs, README and `docs/` are in Brazilian Portuguese. Talk to the maintainer in Brazilian Portuguese.
8. Secrets only in environment variables; never in files, logs or prompts.

## Stack

Java 8 (`release 8`, bytecode 52) · Kotlin 2.x (JVM target 1.8, explicit API) · Maven Wrapper 3.9.x · JUnit 5 · JaCoCo · japicmp · PIT · ArchUnit · DocLint · Dokka. Modules: `brasil4j-core` (JDK-only runtime), `brasil4j-kotlin` (core + stdlib), `brasil4j-exemplos` (tests only, not published).

## Commands

```sh
./mvnw -B clean verify                      # tests, JarITs, JaCoCo, DocLint, Dokka (JDK 21); run on JDK 8 and 21
./mvnw -B -pl brasil4j-core test -Dtest=CpfTest
./mvnw -B -pl brasil4j-core -Pmutation test-compile org.pitest:pitest-maven:mutationCoverage
./mvnw -B test -pl brasil4j-exemplos -am    # didactic examples
python3 scripts/check-bytecode.py           # after verify: Java 8 bytecode
python3 scripts/check-api-compatibility.py <baseline> . <japicmp.jar> ~/.m2/repository
```

On Windows use `.\mvnw.cmd`. `*IT` tests run only in `verify` (Failsafe, against the JARs).

## Context index

| File | Use |
| --- | --- |
| [.context/README.md](.context/README.md) | Task → file map |
| [standards/review-checklist.md](.context/standards/review-checklist.md) | 12 review lenses and gates (plans and reviews) |
| [standards/design-quality.md](.context/standards/design-quality.md) | Clean code, SOLID, DRY/KISS/YAGNI, reuse, contracts |
| [standards/java-library-api.md](.context/standards/java-library-api.md) | Java API shape, compatibility, deprecation |
| [standards/kotlin-library-api.md](.context/standards/kotlin-library-api.md) | Kotlin adapter API and functional style |
| [standards/testing.md](.context/standards/testing.md) | TDD, test quality, mutation, architecture tests |
| [patterns/design-patterns.md](.context/patterns/design-patterns.md) | Patterns in use and the opportunity checklist |
| [patterns/architecture.md](.context/patterns/architecture.md) | Module and package dependency rules |
| [workflows/git-flow.md](.context/workflows/git-flow.md) | Branches, PRs, releases, tags, required checks |
| [workflows/llm-review.md](.context/workflows/llm-review.md) | Independent review by another model |

Product docs (PT-BR): [README](README.md), [architecture](docs/arquitetura.md), [sources](docs/fontes.md), [requirements](docs/requisitos.md), specs in `specs/NNN-*/`.

## Layout

`brasil4j-core/src/main/java/io/github/brasil4j/{identidade,verificacao,mascara,geografia,financas}` · `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/` · `specs/` (Spec Kit artifacts per increment) · `.github/` (CI, rulesets) · `scripts/` (build checks, GitHub settings).
