---

description: "Task list for Spec 011 — Calculadora financeira, percentuais e tabela progressiva"
---

# Tasks: Calculadora financeira funcional, percentuais com gross-up e tabela progressiva

**Input**: Design documents from `/specs/011-calculadora-percentual-faixas/`

- [X] T001 Revisar plano e contrato `contracts/api.md` com o Claude e consolidar os 14 achados nos artefatos da Spec 011 (`2026-11-28`)
- [X] T002 Executar análise pré-código de consistência entre `spec.md`, `plan.md`, `contracts/api.md` e `tasks.md` registrando em `analysis.md` (`2026-11-28`)
- [X] T003 [US1] TDD (`CalculadoraFinanceiraTest`): limites de `MathContext` (`1..90`), guarda anti-DoS (`scale`/`bitLength`/`precision <= 180`), propagação de exceções e rejeição de nulos/subclasses (`2026-11-28`)
- [X] T004 [US1] TDD (`CalculadoraFinanceiraTest`): avaliação em `Real`/`Dolar`/`Euro`/`Moeda`, zero sem sinal negativo e caracterização de modos simétricos vs. direcionais sobre dízimas em `DECIMAL128` (`2026-11-28`)
- [X] T005 [US2] TDD (`PercentualTest`): fábricas (`de(String/BigDecimal/long)`, `deFator`, `dePontosBase`), guarda anti-DoS pré-normalização (`A1`), bordas de escala `38..42`, `Long.MIN_VALUE`, álgebra pura e `toString()` plano (`2026-11-28`)
- [X] T006 [US2] TDD (`PercentualTest`): operações genéricas `<T extends Dinheiro> aplicarSobre/acrescentarEm/descontarDe` preservando subtipo e reconciliação contábil nos 7 modos padrão + políticas customizadas (`2026-11-28`)
- [X] T007 [US2] TDD (`PercentualTest`): `calcularBasePorDentro` em `[0%, 100%)`, rejeições fora do intervalo e propriedade de reversibilidade incondicional nos 7 modos padrão (`567` combinações) (`2026-11-28`)
- [X] T008 [US3] TDD (`TabelaProgressivaTest`): construção com `Builder<T>` de uso único (`IllegalStateException` após `faixaAcima`), borda de `1.000` faixas, validação de ordem estrita, moeda em `paraMoeda` e alíquotas `>= 0%` (`2026-11-28`)
- [X] T009 [US3] TDD (`TabelaProgressivaTest`): apuração em fronteiras `(L_{k-1}, L_k]`, base zero, tabelas abertas e com teto (`0%`), reconciliação por maiores restos (`A2`) em `NO_TOTAL` nos 7 modos padrão + política customizada, `POR_FAIXA`, moedas `0` e `3` casas, `equals`/`hashCode`/`toString` (`B2`) e `getAliquotaEfetiva` (`B4`) (`2026-11-28`)
- [X] T010 [P] [US4] TDD (`FinancasTest.kt`, `FinancasJarIT.java`, `FinancasJarIT.kt`): extensões Kotlin `.porCento()`, `.pontosBase()`, `<T : Dinheiro> T.aplicar/acrescentar/descontar/basePorDentro` e DSLs `calcular*` e `tabelaProgressiva*` (`B1` validado) (`2026-11-28`)
- [X] T011 [P] [US4] Criar `CalculadoraPercentualExemplosTest.java` e `CalculadoraPercentualKotlinExemplosTest.kt` em `brasil4j-exemplos` e atualizar documentação (`README.md`, `docs/arquitetura.md`, `docs/fontes.md`, `brasil4j-exemplos/README.md`) (`2026-11-28`)
- [X] T012 Executar `clean verify` completo em JDK 8 e JDK 21 (4 módulos + bytecode 52), rodar revisão adversarial de código com o Claude, atualizar `analysis.md` e commitar na branch `011-calculadora-percentual-faixas` (`2026-11-28`)
