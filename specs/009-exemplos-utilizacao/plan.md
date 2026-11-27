# Implementation Plan: Módulo vitrine de exemplos (`brasil4j-exemplos`)

- **Branch**: `009-exemplos-utilizacao` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)
- **Base Git**: `517280f` (`008-nucleo-monetario`) em worktree isolada `work/009-exemplos-utilizacao`

## Summary

Adicionar o módulo `brasil4j-exemplos` sem código de produção e sem deploy, contendo exemplos executáveis e comentados nos pacotes de teste Java 8 e Kotlin para Identidade, Geografia e Finanças, além de um guia navegável em `brasil4j-exemplos/README.md`.

## Technical Context

- **Language/Version**: Java 8 (`release 8`) e Kotlin `2.3.10` (`jvmTarget = 1.8`, `-Xjdk-release=8`).
- **Primary Dependencies**: Escopo `test` apenas (`brasil4j-core`, `brasil4j-kotlin`, `kotlin-stdlib`, `junit-jupiter`).
- **Testing**: JUnit Jupiter `5.12.2` via Maven Surefire em JDK 8 e JDK 21.
- **Constraints**: Zero arquivos em `src/main`; desativado automaticamente em `-Pcentral` + `maven.deploy.skip=true` / `gpg.skip=true`; apenas vetores sintéticos/públicos.

## Constitution Check

- [x] **I. SDD**: Spec, plano, modelo, contrato e tarefas registrados antes do módulo.
- [x] **II. DDD**: Nenhuma regra de domínio alterada; módulo atua exclusivamente como consumidor exemplar da API pública.
- [x] **III. TDD**: Exemplos escritos como testes determinísticos autoexplicativos verificados em JDK 8 e JDK 21.
- [x] **IV. Platform**: Compatível com Java 8 e JDK 21, preservando cobertura, DocLint, Dokka, bytecode 52 e bundle Central.
- [x] **V. Provenance**: Uso estrito de vetores sintéticos e códigos públicos já registrados em `docs/fontes.md`.

## Project Structure

```text
brasil4j-exemplos/
├── pom.xml
├── README.md
└── src/test/
    ├── java/io/github/brasil4j/exemplos/java/
    │   ├── IdentidadeExemplosTest.java
    │   ├── GeografiaExemplosTest.java
    │   └── FinancasExemplosTest.java
    └── kotlin/io/github/brasil4j/exemplos/kotlin/
        ├── IdentidadeGeografiaKotlinExemplosTest.kt
        └── FinancasKotlinExemplosTest.kt
```
