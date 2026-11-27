---

description: "Task list for Spec 008 — Núcleo monetário, rateio sem perda e conversão cambial"
---

# Tasks: Núcleo monetário, rateio sem perda e conversão cambial

**Input**: Design documents from `/specs/008-nucleo-monetario/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/api.md](contracts/api.md), [quickstart.md](quickstart.md)

**Tests**: MANDATORY (constitution, principle III — TDD). One invariant group per Red → Green → Refactor cycle; record evidence.

**Organization**: Tasks are grouped by user story and broken down by invariant so each TDD cycle is focused and verifiable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Every constrained field from `data-model.md` is explicitly cited by `Entity.field`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare package directories in the isolated worktree without modifying existing modules' public APIs.

- [X] T001 Create package directories `brasil4j-core/src/main/java/io/github/brasil4j/financas`, `brasil4j-core/src/test/java/io/github/brasil4j/financas`, `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/financas`, and `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/financas` per `plan.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish `Moeda` (with TOCTOU-safe snapshot and zero-regex `<clinit>`), `MetodoArredondamento` (with `FLOOR`/`CEILING` neighbor enforcement), `PoliticaResiduo`, and `AritmeticaMonetaria` DoS bounds.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 Write failing unit tests for `Moeda` invariants (`BRL`, `USD`, `EUR`, `iso`, `of`, `fromJdkCurrency`, `mesmaMoeda`, single-read anti-TOCTOU on malicious external `Moeda`, null symbol rejection) in `brasil4j-core/src/test/java/io/github/brasil4j/financas/MoedaArredondamentoTest.java`
- [X] T003 Implement `Moeda`, `ValidacaoMoeda`, and package-private `MoedaPadrao` (using pure char loops without static `Pattern` and strict `<clinit>` DAG) enforcing `data-model.md` constraints for `Moeda.codigo`, `Moeda.casasDecimais`, and `Moeda.simbolo` in `brasil4j-core/src/main/java/io/github/brasil4j/financas/Moeda.java`, `ValidacaoMoeda.java`, and `MoedaPadrao.java`, verifying T002 passes green
- [X] T004 Extend `MoedaArredondamentoTest.java` with failing tests for `MetodoArredondamento` (`ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`, `PARA_CIMA`, `TETO`, `PISO`, `fromRoundingMode`, `"-0.004"`, custom valid/invalid neighbor policies, malicious `BigDecimal` subclasses), `PoliticaResiduo`, and input length/precision/scale DoS bounds in `brasil4j-core/src/test/java/io/github/brasil4j/financas/MoedaArredondamentoTest.java`
- [X] T005 Implement `MetodoArredondamento`, package-private `ModoArredondamentoPadrao`, `PoliticaResiduo`, and base validation/neighbor/scaling helpers in `AritmeticaMonetaria` enforcing `data-model.md` constraints for `MetodoArredondamento.modo` and `PoliticaResiduo.tipo` in `brasil4j-core/src/main/java/io/github/brasil4j/financas/MetodoArredondamento.java`, `ModoArredondamentoPadrao.java`, `PoliticaResiduo.java`, and `AritmeticaMonetaria.java`, verifying T004 passes green and refactoring

**Checkpoint**: Currency, rounding, anti-TOCTOU, and DoS bounds verified green.

---

## Phase 3: User Story 1 - Representar e operar valores monetários com segurança de moeda e arredondamento explícito (Priority: P1) 🎯 MVP

**Goal**: Deliver sealed `Dinheiro` (`public abstract class`) and final `Real`, `Dolar`, `Euro`, and `QuantiaMonetaria` with canonical routing, symmetrical equality, strict/rounded construction, and exact/single-rounding arithmetic.

**Independent Test**: Run `DinheiroNucleoTest` verifying construction, rounding, DoS bounds, equality symmetry, policy propagation, single-rounding division across large magnitudes, and cross-currency rejection.

### Tests & Implementation Cycles for User Story 1 (MANDATORY — TDD, principle III) ⚠️

- [X] T006 [US1] Cycle 1 (Construction, Canonical Routing & Equality): Write failing tests for `de`, `exato`, `deInteiro`, `deCentavos`, `deUnidadesMinimas`, `zero`, canonical `Real`/`Dolar`/`Euro`/`QuantiaMonetaria` routing, `BRL/USD/EUR` symbol normalization, DoS bounds, and `equals`/`hashCode`/`toString` symmetry in `brasil4j-core/src/test/java/io/github/brasil4j/financas/DinheiroNucleoTest.java`
- [X] T007 [US1] Cycle 1 Green: Implement sealed `Dinheiro` (`abstract class` with package-private constructor) and final `Real`, `Dolar`, `Euro`, and `QuantiaMonetaria` construction and identity enforcing `data-model.md` constraints for `Dinheiro.moeda`, `Dinheiro.valor`, and `Dinheiro.arredondamento` in `brasil4j-core/src/main/java/io/github/brasil4j/financas/Dinheiro.java`, `Real.java`, `Dolar.java`, `Euro.java`, and `QuantiaMonetaria.java`
- [X] T008 [US1] Cycle 2 (Arithmetic, Single-Rounding Division & Policy Propagation): Add failing tests to `DinheiroNucleoTest.java` for `somar`, `subtrair`, `abs`, `negar`, `comArredondamento`, `multiplicar(long)`, exact `multiplicar(BigDecimal)`, single-rounding `dividir(long)` (including large ~10^30 unit values and custom sticky policies), `compararMesmaMoeda`/`compareTo`, `emUnidadesMinimas` overflow, and cross-currency rejection in `brasil4j-core/src/test/java/io/github/brasil4j/financas/DinheiroNucleoTest.java`
- [X] T009 [US1] Cycle 2 Green & Refactor: Implement exact and single-rounding sticky arithmetic in `AritmeticaMonetaria.java`, `Dinheiro.java`, `Real.java`, `Dolar.java`, `Euro.java`, and `QuantiaMonetaria.java`, verifying all US1 tests pass green

**Checkpoint**: User Story 1 complete and green.

---

## Phase 4: User Story 2 - Parcelar e alocar valores proporcionalmente sem perda de centavos (Priority: P2)

**Goal**: Deliver `ratear` (`PRIMEIRAS_PARCELAS` / `ULTIMAS_PARCELAS`) and `alocar` (Hamilton largest-remainder with `BigInteger`) across all four monetary classes.

**Independent Test**: Run `RateioAlocacaoTest` including parameterized `1..120` installments across `0`, `2`, and `3` decimal currencies (`SC-002`), negative values, zero weights, DoS weight bounds, and policy retention.

### Tests & Implementation for User Story 2 (MANDATORY — TDD, principle III) ⚠️

- [X] T010 [US2] Write failing unit and parameterized tests (`1..120` installments across `BRL`, `JPY`, `KWD`; positive, negative, zero; zero weights `[0, 1, 1, 1]`; extreme weight scale rejection; policy retention) in `brasil4j-core/src/test/java/io/github/brasil4j/financas/RateioAlocacaoTest.java`
- [X] T011 [US2] Implement `ratear` and exact `BigInteger` Hamilton `alocar` in `brasil4j-core/src/main/java/io/github/brasil4j/financas/AritmeticaMonetaria.java` and wire covariant overrides in `Dinheiro.java`, `Real.java`, `Dolar.java`, `Euro.java`, and `QuantiaMonetaria.java`, verifying T010 passes green and refactoring

**Checkpoint**: User Stories 1 and 2 independently verified green.

---

## Phase 5: User Story 3 - Converter valores entre moedas por taxa de câmbio explícita e reversível (Priority: P3)

**Goal**: Deliver `TaxaCambio` with exact involutive `inverter()`, plain non-negative scale normalization, strict `converter`, bidirectional pair helpers (`paraDolar`, `paraReal`, `paraEuro`), and clean-JVM `<clinit>` + JAR integration tests.

**Independent Test**: Run `TaxaCambioTest` and `FinancasJarIT` (including clean-JVM class initialization checks for `Moeda.BRL`, `Real.ZERO`, `Dinheiro.de`, and `TaxaCambio.umDolarEmReais`).

### Tests & Implementation for User Story 3 (MANDATORY — TDD, principle III) ⚠️

- [X] T012 [P] [US3] Write failing unit tests for `TaxaCambio` (`de`, `umDolarEmReais`, `umEuroEmReais`, exact involutive `inverter()`, `TRUNCAMENTO`/`PISO` round-trip without cent loss, `100` plain scale without `1E+2`, `origem == destino` rejection, `equals`/`hashCode`/`toString`) and bidirectional `paraDolar`/`paraReal`/`paraEuro` in `brasil4j-core/src/test/java/io/github/brasil4j/financas/TaxaCambioTest.java`
- [X] T013 [P] [US3] Write packaged JAR integration tests covering US1–US3 and spawning isolated clean-JVM entry-point checks (`<clinit>` order safety) in `brasil4j-core/src/test/java/io/github/brasil4j/financas/FinancasJarIT.java`
- [X] T014 [US3] Implement `TaxaCambio` enforcing `data-model.md` constraints for `TaxaCambio.moedaOrigem`, `TaxaCambio.moedaDestino`, and `TaxaCambio.cotacao` in `brasil4j-core/src/main/java/io/github/brasil4j/financas/TaxaCambio.java` and wire conversion methods in `Dinheiro.java`, `Real.java`, `Dolar.java`, `Euro.java`, and `QuantiaMonetaria.java`, verifying T012 and T013 pass green

**Checkpoint**: Full Java 8 monetary core (`US1`–`US3`) verified by unit and packaged JAR integration tests.

---

## Phase 6: User Story 4 - Operar valores monetários de forma idiomática em consumidores Kotlin (Priority: P4)

**Goal**: Deliver complete Kotlin extensions and operators (`+`, `-`, commutative `*`, `/`, unary `-`, `alocarPorPesos`, explicit `reais`/`reaisInteiros`/`centavosReais`) in `brasil4j-kotlin`.

**Independent Test**: Run `FinancasTest` and `FinancasJarIT` in `brasil4j-kotlin` on JDK 8 and JDK 21.

### Tests & Implementation for User Story 4 (MANDATORY — TDD, principle III) ⚠️

- [X] T015 [P] [US4] Write failing Kotlin unit tests covering builders, commutative operators (`2 * real` and `real * 2`), `QuantiaMonetaria` and `Dinheiro` operators, `alocarPorPesos`, and FX conversions in `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/financas/FinancasTest.kt`
- [X] T016 [P] [US4] Write failing Kotlin packaged JAR integration tests in `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/financas/FinancasJarIT.kt`
- [X] T017 [US4] Implement Kotlin extensions and operators with complete KDoc in `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/financas/Financas.kt`, verifying T015 and T016 pass green

**Checkpoint**: Both Java and Kotlin modules complete and green.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Provenance and architecture documentation in the worktree, multi-JDK verification, bytecode check, compatibility gate, and independent code review with Claude.

- [X] T018 [P] Register ISO 4217 and decimal tie-to-even / Hamilton conventions in `docs/fontes.md` (explicitly noting pending section check on licensed ABNT NBR 5891 copy) and update `docs/arquitetura.md` and `README.md` inside the `008-nucleo-monetario` worktree
- [X] T019 Run full quality gates (`mvnw.cmd clean verify` on JDK 8 and JDK 21, JaCoCo ≥90%/≥85%, DocLint, Dokka, `scripts/check-bytecode.py`, and `scripts/check-api-compatibility.py` against the active baseline tag/commit)
- [X] T020 Run independent read-only code review of the complete `008-nucleo-monetario` diff with Claude (`llm-call.ps1 -Cli claude -Mode read`), address any valid findings, re-verify builds, and update Constitution Check / `tasks.md` evidence

---

## Dependencies & Execution Order

- **Phase 1 (`T001`)** → **Phase 2 (`T002`–`T005`)** → **Phase 3 (`T006`–`T009`)** → **Phase 4 (`T010`–`T011`)** → **Phase 5 (`T012`–`T014`)** → **Phase 6 (`T015`–`T017`)** → **Phase 7 (`T018`–`T020`)**.

## Parallel Example

```text
Phase 5 tests: T012 (TaxaCambioTest.java) + T013 (FinancasJarIT.java)
Phase 6 tests: T015 (FinancasTest.kt) + T016 (FinancasJarIT.kt)
```

## Implementation Strategy

Execute strict Red → Green → Refactor per cycle (`T002→T003`, `T004→T005`, `T006→T007`, `T008→T009`, `T010→T011`, `T012/T013→T014`, `T015/T016→T017`), keeping every JavaDoc `@param`/`@return`/`@throws` and KDoc comment DocLint/Dokka-compliant from the start.
