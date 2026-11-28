# Implementation Plan: Calculadora financeira funcional, percentuais com gross-up e tabela progressiva

**Branch**: `feature/011-calculadora-percentual-faixas` | **Date**: 2026-11-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/011-calculadora-percentual-faixas/spec.md`

## Summary

Implementar a segunda entrega do contexto `financas` (`Spec 011`): `@FunctionalInterface CalculoFinanceiro`, `CalculadoraFinanceira`, `Percentual` (incluindo *gross-up* reversível nos sete modos padrão e métodos genéricos `<T extends Dinheiro>`) e `TabelaProgressiva<T extends Dinheiro>` (`ModoApuracaoProgressiva`, `FaixaProgressiva`, `FaixaApurada`, `ApuracaoProgressiva` com memória reconciliada por maiores restos sobre partes fracionárias) em `brasil4j-core`, DSLs Kotlin em `brasil4j-kotlin` (em *explicit API mode*) e suítes de exemplos em `brasil4j-exemplos`.

## Technical Context

**Language/Version**: Java 8 core (bytecode 52, JDK 8/21) + Kotlin adapter (`-Xexplicit-api=strict`, `jvmTarget = 1.8`)

**Primary Dependencies**: Nenhuma em `brasil4j-core` (`AR-1`); `kotlin-stdlib` em `brasil4j-kotlin` (`AR-2`)

**Testing**: JUnit Jupiter + ArchUnit (`ArchitectureTest`), JaCoCo (`>= 90%` linhas / `>= 85%` branches), PIT mutation (`-Pmutation`), `japicmp`, `*JarIT` (`.context/standards/testing.md`)

**Target Platform**: JVM library (JDK 8 e JDK 21)

**Project Type**: library (`brasil4j-core`, `brasil4j-kotlin`, `brasil4j-exemplos`)

**Performance Goals**: Guardas anti-DoS `O(1)` (`scale` e `bitLength`) antes de `stripTrailingZeros()`, `setScale(0)` ou `precision()`

**Constraints**: Mudança puramente aditiva (100% compatível em fonte, binário e comportamento com `v0.2.0` e `0.3.0-SNAPSHOT`); zero dependências externas em `brasil4j-core`

**Scale/Scope**: Pacote `io.github.brasil4j.financas` (8 novos tipos públicos) e `io.github.brasil4j.kotlin.financas` (extensões e DSLs)

## Constitution Check

- [x] I. SDD (I.1–I.3): spec, pesquisa, modelo, contrato, plano, tarefas e análise pré-código concluídos; impacto e compatibilidade registrados.
- [x] II. DDD (II.1–II.3): invariantes encapsuladas nos próprios objetos de valor e motores matemáticos; zero tabelas legais acopladas; `financas` depende apenas do JDK (`AR-5`).
- [x] III. TDD (III.1–III.3): ciclos red/green/refactor registrados em `analysis.md`; consumo pelos JARs empacotados em Java e Kotlin (`FinancasJarIT`).
- [x] IV. Platform (IV.1–IV.6): Java 8 + bytecode 52, JDK 8/21, JaCoCo >= 90/85, `japicmp` contra `v0.2.0`, limiar PIT de mutação, DocLint e Dokka limpos.
- [x] V. Provenance (V.1–V.6): convenções matemáticas registradas como `CONVENCAO_TECNICA` (`F-21` em `docs/fontes.md`), sem atribuição normativa não confirmada.
- [x] VII. Delivery (VII.1–VII.4): branch `feature/011-calculadora-percentual-faixas` a partir de `develop`, PR para `develop` com checks obrigatórios e `CHANGELOG.md`.

### Design Quality Gate (VI — lenses from `.context/standards/review-checklist.md`)

- [x] VI.1 Clean code and SOLID (`DQ-1…DQ-11`) — tipos pequenos com responsabilidade única, cláusulas de guarda, zero flags booleanas (`ModoApuracaoProgressiva` explícito).
- [x] VI.2 DRY / KISS / YAGNI / reuse (`DQ-12…DQ-15`) — reuso de `AritmeticaMonetaria` e auxiliar único `distribuirUnidadesMaioresRestos`.
- [x] VI.3 Contracts and extensibility (`DQ-16…DQ-18`) — `@FunctionalInterface CalculoFinanceiro`, `MetodoArredondamento` injetável e `Moeda` aberta em `CalculadoraFinanceira` e `TabelaProgressiva`.
- [x] VI.4 Patterns (`DP-1…DP-9`) — padrões aplicados e rejeitados justificados abaixo.
- [x] VI.5 Java library API (`JA-1…JA-15`) — classes `final` imutáveis, fábricas estáticas expressivas, gancho `criarMesmoSubtipo` restrito a `package-private`.
- [x] VI.6 Kotlin API and functional style (`KA-1…KA-13`, `KF-1…KF-4`) — delegação integral ao núcleo, `-Xexplicit-api=strict`, validação com `require`/`requireNotNull`.
- [x] VI.7 Architecture (`AR-1…AR-8`) — verificado automaticamente por `ArchitectureTest`.
- [x] VI.8 Test quality (`TQ-1…TQ-11`) — teste de contrato abstrato para `CalculoFinanceiro`, propriedades parametrizadas, `*JarIT` e mutação PIT.
- [x] VI.9 Independent LLM review — 2 rodadas adversariais independentes com o Claude (14 achados no plano + 8 achados no código, todos resolvidos em `analysis.md`).

## Design Decisions

| Topic | Decision | Alternatives rejected and why | Rule IDs |
| --- | --- | --- | --- |
| Clean code | Guardas `O(1)` de escala/bits antes de normalizar `BigDecimal`; enum `ModoApuracaoProgressiva` no lugar de `boolean arredondarPorFaixa` | Parâmetro booleano opaco; normalização antes de checar limites (vulnerável a DoS com `1E+999999999`) | `DQ-1…DQ-6` |
| DRY / KISS / YAGNI / reuse | Centralizar reconciliação de maiores restos (`distribuirUnidadesMaioresRestos`) e arredondamento intermediário em `AritmeticaMonetaria` | Duplicar ordenação de restos entre `Dinheiro.alocar` e `TabelaProgressiva` | `DQ-12…DQ-15` |
| Java OO shape | Tipos `final` imutáveis (`CalculadoraFinanceira`, `Percentual`, `TabelaProgressiva`, `FaixaProgressiva`, `FaixaApurada`, `ApuracaoProgressiva`) com `equals`/`hashCode`/`toString` por valor | Classes abertas à herança ou mutáveis; expor `criarMesmoSubtipo` como `public` em `Dinheiro` | `JA-1…JA-7` |
| Kotlin adapter and functional style | Extensões puras (`.porCento()`, `.aplicar()`), `calcularReais` e DSL `@FinancasDsl EscopoTabelaProgressiva` com `public` explícito | Reimplementar aritmética decimal no módulo Kotlin | `KA-1…KA-4`, `KF-1…KF-4` |
| Responsibilities and types | Separar definição estática da faixa (`FaixaProgressiva`), memória individual (`FaixaApurada`) e consolidação (`ApuracaoProgressiva`) | Misturar parâmetros da tabela com resultado de uma base específica | `DQ-7…DQ-11` |
| Patterns | **Strategy** (`CalculoFinanceiro`, `MetodoArredondamento`), **Builder de uso único** (`TabelaProgressiva.Builder<T>`) | 40 sobrecargas concretas em `Percentual` (substituídas por 8 métodos genéricos `<T extends Dinheiro>`) | `DP-1…DP-9` |
| Public API impact | Puramente aditivo: 8 novos tipos públicos em `financas` e novas extensões em `kotlin.financas`; 100% compatível binária/fonte/comportamental | Alterar assinaturas publicadas da `Spec 008` | `JA-8…JA-14`, `KA-4…KA-12` |
| Consumer extensibility | Consumidor injeta `CalculoFinanceiro` arbitrário, `MetodoArredondamento` customizado, `Moeda` própria e faixas externas | Embutir tabelas fiscais/previdenciárias fixas na biblioteca | `DQ-16` |
| Architecture | Tudo contido em `io.github.brasil4j.financas` (depende só do JDK) e `io.github.brasil4j.kotlin.financas` | Criar subpacotes cíclicos ou depender de `identidade`/`geografia` | `AR-1…AR-8` |
| Test strategy | `CalculoFinanceiroContractTest` (`TQ-3`), 567 combinações de reversibilidade do *gross-up*, todos os 7 modos em `NO_TOTAL`, `*JarIT` e PIT `-Pmutation` | Testar apenas `HALF_UP` ou omitir fronteiras exatas `L_k` e escalas extremas | `TQ-1…TQ-11` |

## Project Structure

### Documentation (this feature)

```text
specs/011-calculadora-percentual-faixas/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/api.md
├── checklists/requirements.md
├── tasks.md
└── analysis.md
```

### Source Code (repository root)

```text
brasil4j-core/src/main/java/io/github/brasil4j/financas/
brasil4j-core/src/test/java/io/github/brasil4j/financas/
brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/financas/
brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/financas/
brasil4j-exemplos/src/test/java/io/github/brasil4j/exemplos/java/
brasil4j-exemplos/src/test/kotlin/io/github/brasil4j/exemplos/kotlin/
```

**Structure Decision**: Evolução aditiva do pacote `io.github.brasil4j.financas` em `brasil4j-core` e de `Financas.kt` em `brasil4j-kotlin`, mantendo dependência única para os consumidores (`AR-1`, `AR-2`, `AR-5`).
