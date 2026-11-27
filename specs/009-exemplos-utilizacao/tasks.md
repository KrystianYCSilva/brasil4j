---

description: "Task list for Spec 009 — Módulo vitrine brasil4j-exemplos"
---

# Tasks: Módulo vitrine de exemplos (`brasil4j-exemplos`)

**Input**: Design documents from `/specs/009-exemplos-utilizacao/`

- [X] T001 Configure `<profile><id>exemplos</id>` (`activeByDefault=true`) in `pom.xml` and create `brasil4j-exemplos/pom.xml` enforcing `data-model.md` constraints for `ModuloExemplos.producao` and `ModuloExemplos.publicacao`
- [X] T002 [P] [US1] Create commented Java 8 example suites `IdentidadeExemplosTest.java`, `GeografiaExemplosTest.java`, and `FinancasExemplosTest.java` enforcing `data-model.md` constraints for `ModuloExemplos.vetores` in `brasil4j-exemplos/src/test/java/io/github/brasil4j/exemplos/java/`
- [X] T003 [P] [US2] Create commented Kotlin example suites `IdentidadeGeografiaKotlinExemplosTest.kt` and `FinancasKotlinExemplosTest.kt` enforcing `data-model.md` constraints for `ModuloExemplos.vetores` in `brasil4j-exemplos/src/test/kotlin/io/github/brasil4j/exemplos/kotlin/`
- [X] T004 [P] Create navigable guide `brasil4j-exemplos/README.md` and link it from root `README.md`
- [X] T005 [US3] Verify reactor execution in JDK 8 and JDK 21 (`clean verify`), verify exclusion under `-Pcentral`, run independent review with Claude, and commit on branch `009-exemplos-utilizacao`
  - **Evidência (`2026-11-27`)**:
    - `clean verify` Corretto 8 (`1.8.0_512`): `BUILD SUCCESS` (`49.015 s`), 4 módulos verdes (`brasil4j`, `brasil4j-core`, `brasil4j-kotlin`, `brasil4j-exemplos`), DocLint `javadoc:jar` limpo, 13 testes de exemplos aprovados (`5` relatórios `TEST-*.xml`).
    - `clean verify` Corretto 21 (`21.0.13`): `BUILD SUCCESS` (`57.298 s`), 4 módulos verdes, Dokka `2.2.0` limpo, `49` arquivos `.class` conferidos com `major <= 52` (Java 8) e 13 testes de exemplos aprovados.
    - Exclusão sob `-Pcentral`: `activeByDefault` desativa `exemplos` quando `central` é ativado no POM raiz, reforçado por `<profile><id>central</id>` com `<alwaysFail>` em `brasil4j-exemplos/pom.xml` e verificação de `TEST-*.xml` em `.github/workflows/verify.yml`.
    - Revisão independente (Claude): parecer *Aprovado com ressalvas*, com todos os 13 apontamentos incorporados e documentados em `specs/009-exemplos-utilizacao/analysis.md`.
