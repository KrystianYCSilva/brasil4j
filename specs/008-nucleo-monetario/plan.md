# Implementation Plan: Núcleo monetário, rateio sem perda e conversão cambial

- **Branch**: `008-nucleo-monetario` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)
- **Input**: [spec.md](spec.md) e [recorte.md](../../docs/descoberta/financeiro/recorte.md)
- **Base Git e Coordenação Paralela (`AGENTS.md`)**:
  - **Responsável**: Antigravity (com revisão independente de plano e código pelo Claude).
  - **Isolamento**: Desenvolvimento e verificações ocorrem exclusivamente na worktree `work/008-nucleo-monetario` (branch `008-nucleo-monetario`, criada sobre `5fd9dae` / `v0.2.0`), sem tocar a árvore principal onde o Codex desenvolve a `Spec 007`.
  - **Ordem de integração**: A branch entrará na fila após o merge da `Spec 007` em `main`, fazendo rebase sobre a ponta resultante e executando `japicmp` contra a última tag/commit publicado antes do fechamento.

## Summary

Introduzir o pacote `io.github.brasil4j.financas` em `brasil4j-core` e `io.github.brasil4j.kotlin.financas` em `brasil4j-kotlin`, entregando `Moeda` (interface aberta com constantes `BRL`, `USD`, `EUR`, fábrica ISO e captura anti-TOCTOU sem regex estática), `MetodoArredondamento` (interface funcional com validação de vizinhos `FLOOR`/`CEILING` e seis modos padrão), `Dinheiro` (`public abstract class` selada no pacote com subtipos finais `Real`, `Dolar`, `Euro` e `QuantiaMonetaria`), divisão exata sem arredondamento duplo, parcelamento `ratear` e alocação exata de Hamilton `alocar`, além de `TaxaCambio` com inversão exata involutiva.

## Technical Context

- **Language/Version**: Java 8 (`maven.compiler.release = 8`, bytecode `52`) no núcleo; Kotlin `2.3.10` (`jvmTarget = 1.8`, `-Xjdk-release=8`) no módulo Kotlin.
- **Primary Dependencies**: Zero dependências de runtime em `brasil4j-core` (apenas `java.math` e `java.util`); apenas `brasil4j-core` e `kotlin-stdlib` em `brasil4j-kotlin`.
- **Storage**: N/A (objetos de valor em memória, não serializáveis por `Serializable`).
- **Testing**: JUnit Jupiter `5.12.2` via Maven Surefire (`*Test`) e Maven Failsafe (`*JarIT`, incluindo teste de inicialização de classes em sub-processo JVM limpo) em Corretto 8 e Corretto 21.
- **Target Platform**: Qualquer JVM compatível com Java 8+.
- **Project Type**: Biblioteca multi-módulo Maven (`brasil4j-core`, `brasil4j-kotlin`).
- **Performance Goals**: Validação lexical por laços diretos de caracteres (zero `Pattern` estático), divisão em passo único e alocação proporcional exata com `BigInteger`.
- **Constraints**: Limites anti-DoS (`<= 64` caracteres em `String`, precisão `<= 38` e escala em `[-18, 38]` em `BigDecimal`, até `10_000` pesos em `alocar`); cobertura JaCoCo ≥90% linhas e ≥85% branches; zero avisos em DocLint (JDK 8/21) e Dokka (JDK 21); zero regressão binária/fonte no `japicmp`.
- **Scale/Scope**: 9 tipos públicos Java, 3 auxiliares `package-private` Java (`MoedaPadrao`, `ModoArredondamentoPadrao`, `AritmeticaMonetaria`), 1 arquivo Kotlin e suítes unitárias + Failsafe nos dois módulos.

## Constitution Check

*GATE: Verificado após revisão independente do plano (evidências de execução e registro em `fontes.md` serão confirmadas na Phase 7 antes do fechamento).*

- [x] **I. SDD (Pré-código)**: `spec.md`, `research.md`, `data-model.md`, `contracts/api.md`, `quickstart.md`, `tasks.md` e `analysis.md` alinhados e revisados independentemente com o Claude.
- [x] **II. DDD (Pré-código)**: Hierarquia selada `Dinheiro` em `brasil4j-core` detém todas as invariantes; regras tributárias e cotações externas ficam nos consumidores. Note-se para governança futura a conveniência de emenda MINOR opcional na constituição para citar explicitamente Geografia e Finanças ao lado de Identidade Brasileira.
- [x] **III. TDD (Comprovado na execução)**: Ciclos Red → Green → Refactor executados em `T002`–`T017` + rodada adicional TDD sobre os 13 achados adversariais de código do Claude, incluindo `*JarIT` e sonda concorrente `CyclicBarrier` de `<clinit>`.
- [x] **IV. Platform (Comprovado na execução)**: `clean verify` verde em Corretto 8 e Corretto 21, bytecode 52 verificado em todas as 30 classes de `brasil4j-core` e 11 classes de `brasil4j-kotlin`, JaCoCo >98% linhas / >95% branches, DocLint limpo em JDK 8 e Dokka 2.2.0 limpo em JDK 21; zero alteração em classes públicas existentes (`japicmp` 100% aditivo).
- [x] **V. Provenance (Comprovado no T018)**: Entradas `F-13` e `F-14` registradas em `docs/fontes.md` com data `2026-11-27` e ressalva explícita de conferência direta de seção na norma impressa ABNT NBR 5891:2014.

## Project Structure

### Documentation (this feature)

```text
specs/008-nucleo-monetario/
├── plan.md              # Este plano de implementação
├── research.md          # Decisões D1–D7 revisadas
├── data-model.md        # Entidades e restrições canônicas de campos
├── quickstart.md        # Guia e cenários de verificação rápida
├── analysis.md          # Relatório de consistência Spec Kit + revisão Claude
├── checklists/
│   └── requirements.md  # Checklist de qualidade da especificação
├── contracts/
│   └── api.md           # Assinaturas públicas Java e Kotlin
└── tasks.md             # Lista granular de tarefas TDD rastreáveis
```

### Source Code (repository root)

```text
brasil4j-core/
├── src/main/java/io/github/brasil4j/financas/
│   ├── Moeda.java
│   ├── MoedaPadrao.java                # package-private (snapshot imutável sem regex estática)
│   ├── MetodoArredondamento.java
│   ├── ModoArredondamentoPadrao.java   # package-private enum das 6 constantes padrão
│   ├── PoliticaResiduo.java
│   ├── AritmeticaMonetaria.java        # package-private (limites, escala, sticky division, Hamilton)
│   ├── Dinheiro.java                   # public abstract class com construtor package-private
│   ├── Real.java
│   ├── Dolar.java
│   ├── Euro.java
│   ├── QuantiaMonetaria.java
│   └── TaxaCambio.java
└── src/test/java/io/github/brasil4j/financas/
    ├── MoedaArredondamentoTest.java
    ├── DinheiroNucleoTest.java
    ├── RateioAlocacaoTest.java
    ├── TaxaCambioTest.java
    └── FinancasJarIT.java

brasil4j-kotlin/
├── src/main/kotlin/io/github/brasil4j/kotlin/financas/
│   └── Financas.kt
└── src/test/kotlin/io/github/brasil4j/kotlin/financas/
    ├── FinancasTest.kt
    └── FinancasJarIT.kt
```

**Structure Decision**: Pacotes `io.github.brasil4j.financas` e `io.github.brasil4j.kotlin.financas` nos dois artefatos existentes do reator, mantendo todos os auxiliares internos `package-private` fora da superfície monitorada pelo `japicmp`.
