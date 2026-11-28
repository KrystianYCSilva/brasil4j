# Implementation Plan: Contratos, polimorfismo e composição para documentos

- **Identificador Spec Kit** `010-contratos-documentos`
- **Data** 2026-11-27
- **Entrada** [Spec 010](spec.md)
- **Base Git** `18bfe9c` (Spec 007), com a emenda da constituição v0.5.0 em `2edda1e`.

## Summary

O plano introduz no núcleo contratos públicos para o valor de documento, a interpretação, a regra, o resultado e a apresentação. CPF, CNPJ e NIS passam a ser construídos pelos mesmos blocos que o consumidor usa: um builder de interpretadores, uma máscara declarativa e um módulo 11 configurável. Os blocos de DV e de máscara ficam em pacotes compartilhados entre contextos. A API publicada é preservada: os utilitários estáticos viram fachadas deprecadas e as exceções e mensagens não mudam. O NIS continua estrutural por padrão. Os validadores de DV e a regra de algarismos repetidos ficam disponíveis como opção, nomeados pela fonte.

## Technical Context

| Aspecto | Decisão |
| --- | --- |
| Plataforma | Java 8 como runtime mínimo, APIs Java 8 e bytecode 52. Kotlin 2.3.10 com target 1.8 e `-Xjdk-release=8`. |
| Dependências | Reator com Maven Wrapper 3.9.14. Em produção, o núcleo usa só o JDK. O Kotlin depende do núcleo e da stdlib. |
| Operação | Interpretação local, síncrona e sem estado. Cada interpretação lê o texto uma vez. |
| Testes | JUnit 5.12.2 com Surefire e Failsafe nos dois módulos. Suíte de contrato abstrata e vetores sintéticos e C1. |
| Compatibilidade | Aditiva. O japicmp 0.26.1 (`--include-synthetic`, erro em quebra binária ou de fonte) compara com `v0.2.0` e com `5fd9dae`. Contra `18bfe9c`, a comparação confirma que só houve adições sobre a 007. |

## Constitution Check

| Princípio | Antes da pesquisa | Após o desenho |
| --- | --- | --- |
| I · SDD | Spec clarificada, com checklist completo. | Contrato, modelo e impacto definidos. `speckit-tasks` e `speckit-analyze` vêm antes do código. |
| II · DDD | As regras pertencem aos tipos de Identidade. Os blocos genéricos não conhecem documentos. | `verificacao` e `mascara` não dependem de `identidade`. `CodigoMunicipio` fica fora. |
| III · TDD | Aceites observáveis nas quatro histórias. | Um ciclo por invariante. A caracterização e a equivalência vêm antes da substituição dos cálculos. |
| IV · Plataforma | Os gates do reator já existem. | JDK 8 e 21, bytecode 52, JaCoCo ≥ 90% de linhas e ≥ 85% de branches, DocLint com `@deprecated`, Dokka e japicmp. |
| V · Proveniência | Fontes de CPF (e-Financeira), CNPJ (IN RFB 2.119/2022), Caixa v016 e SIPREV RV_03 identificadas. | Os validadores do NIS são nomeados pela fonte e pelo alcance, sem o rótulo "oficial". `docs/fontes.md` ganha entradas para Caixa FGTS v016, convenção legada e SIPREV RV_03. Os diagnósticos omitem o documento. |
| Engenharia JVM (v0.5.0) | O objetivo de contratos e de extensão está explícito. | Cada padrão GoF está nomeado e justificado em [research.md](research.md). Os tipos nativos usam o builder público. |

Nenhuma exceção à constituição.

## Project Structure

### Documentation

- [Spec](spec.md) e [checklist](checklists/requirements.md)
- [Pesquisa](research.md), [modelo](data-model.md), [contrato](contracts/api.md) e [guia de verificação](quickstart.md)

### Source Code · Destinos

| Artefato | Caminho |
| --- | --- |
| Contratos de Identidade | `brasil4j-core/src/main/java/io/github/brasil4j/identidade/`: `Document`, `MaskedDocument`, `CheckDigitDocument`, `AbstractDocument`, `DocumentParser`, `DocumentParsers`, `ValidationResult`, `ValidationRule`, `Rejection`, `RejectionReason`, `DocumentFormatter` |
| Verificação | `brasil4j-core/src/main/java/io/github/brasil4j/verificacao/`: `CheckDigitAlgorithm`, `Modulus11` |
| Máscara | `brasil4j-core/src/main/java/io/github/brasil4j/mascara/`: `DocumentMask` |
| Documentos | `identidade/cpf`, `identidade/cnpj`, `identidade/nis` (inclui `NisValidationMode` e `NisRules`), `identidade/composicao` |
| Kotlin | `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/`: `Cpf.kt`, `Cnpj.kt`, `Nis.kt` e `DocumentParsers.kt` (extensões genéricas) |
| Testes | Pacotes espelhados nos dois módulos: `DocumentParserContractTest`, `Modulus11EquivalenceTest`, `Modulus11PisTest`, `DocumentMaskTest`, `CustomDocumentExampleTest` e JarIT |
| Removidos | `cpf/CpfCheckDigits`, `cnpj/CnpjCheckDigits` (package-private), depois da equivalência |

## Phase 0 · Outline & Research

As [decisões D1–D8](research.md) cobrem os contratos e os padrões, o resultado sem exceção, a compatibilidade de exceções e assinaturas, a equivalência dos cálculos, a máscara, o NIS e sua proveniência, o `DocumentCheck` e o Kotlin.

## Phase 1 · Design & Contracts

O [contrato](contracts/api.md) define assinaturas e semântica. O [modelo](data-model.md) define estados e fluxo do pipeline. O [guia](quickstart.md) mostra exemplos e comandos.

### Sequência de entrega

Cada etapa segue TDD (vermelho, verde, refatoração) e tem um commit próprio. As tarefas executam essas etapas agrupadas por história. A etapa 6 se divide em 6a (fundação) e 6b (US2). As etapas 8 e 9 (US1) vêm antes da 7 (US3), pois a US1 tem prioridade maior e não depende do DV do NIS. Ver [tasks.md](tasks.md#dependências).

1. Caracterização: `toString`, mensagens, tipos exatos de exceção, modo nulo e 14 zeros.
2. `Rejection`, `RejectionReason`, `ValidationResult` e `ValidationRule`.
3. `CheckDigitAlgorithm` e `Modulus11` para CPF e CNPJ, com equivalência contra os cálculos atuais. Depois, remoção dos `*CheckDigits`.
4. `DocumentMask`, substituindo os `canonicalize` e os `applyMask` por `substring`.
5. Contratos de valor e `AbstractDocument`. Gate do japicmp nesta etapa.
6. `DocumentParser`, o builder, `parser(mode)` e os modos reimplementados.
7. NIS: `Modulus11.pisCaixaFgtsV016()`, `pisLegacyMod11()`, `NisRules.repeatedDigits()`, `NisValidationMode` e `docs/fontes.md`.
8. `DocumentFormatter`.
9. `DocumentCheck.of` e `getRejection`.
10. Kotlin: migrar `Nis.kt`, criar as extensões e `Cnpj.hasValidCheckDigits`.
11. `@Deprecated` nas fachadas, com `@deprecated` no JavaDoc.
12. README, `arquitetura.md` com diagrama Mermaid, CHANGELOG e revisão final com `agy`.

### Testes e rastreabilidade

| Camada | Estratégia | Rastreabilidade |
| --- | --- | --- |
| Contrato | `DocumentParserContractTest<D>` abstrato: nulo, vazio, espaços e lixo viram rejeição sem exceção. O `toString` de resultado e rejeição não contém a entrada. `parseOrThrow` lança IAE exata. | US1/US2 · FR-003/004 · SC-002 |
| Extensão | `CustomDocumentExampleTest`: documento sintético montado só com blocos públicos, os quatro desfechos e uso numa lista com `DocumentCheck`. | US1 · FR-001/002/006/011 · SC-001 |
| Cálculos | Equivalência de CPF e CNPJ em vetores atuais mais 10 000 bases geradas com semente fixa e congeladas. 14 zeros. Domínio inválido. | FR-007/008 · SC-003 |
| NIS | Vetores C1 07, 09, 15–21 no modo padrão, na Caixa FGTS v016 com e sem `repeatedDigits` e no legado. Regra do consumidor com código. | US3 · FR-005/009 · SC-004 |
| Regressão | Suítes existentes sem alteração de asserções. JarIT Java e Kotlin. | US4 · FR-013 · SC-005 |
| Consumidor (009) | `brasil4j-exemplos` verde sem alteração em cada etapa, provando compatibilidade pelas fachadas. Novas suítes de exemplos para os contratos na etapa 12. | US1/US4 · FR-013 · SC-005 |
| Proveniência | Busca por "oficial" em `src` e `docs`. Cada ocorrência remete a `fontes.md`. | FR-009 · SC-006 |

### Compatibilidade

| Base | Commit | Uso |
| --- | --- | --- |
| `v0.2.0` | `8e94704` | API publicada de CPF, CNPJ e geografia |
| Spec 006 | `5fd9dae` | API de NIS e assinaturas Kotlin |
| Spec 007 | `18bfe9c` | `DocumentCheck`, para confirmar que a 010 só adiciona |

A comparação reconstrói as baselines em diretórios separados e roda `scripts/check-api-compatibility.py` com JDK 21. Se a migração de `getCanonicalValue`, `equals` e `hashCode` para a superclasse gerar alerta, `Cpf` e `Cnpj` mantêm overrides finos que delegam.

### Correção de compatibilidade para a v0.3.0

Seguir a convenção Java: `IllegalArgumentException` para argumento inadequado, que `ValidationRule.checkDigits` converte em `INVALID_FORMAT`, e `IllegalStateException` para objeto em estado inadequado, que se propaga. O `Modulus11` confere o mapeamento do resto no `build()` para os restos de 0 a 10, de modo que o erro de configuração aparece cedo e para qualquer entrada. Com vários DVs, o alfabeto precisa conter todo dígito que o mapeamento produz. Caracterizar os casos em Java, rodar o reator em JDK 8 e 21 e comparar os JARs com a `v0.2.0` e com a base da correção.

Pela convenção, qualquer `IllegalArgumentException` lançada pelo algoritmo, inclusive subclasses, se torna `INVALID_FORMAT`. Algoritmos do consumidor devem usar `IllegalStateException` para distinguir defeitos de configuração.

### Coordenação com 008 e 009

Constituição, README, CHANGELOG, `arquitetura.md` e `fontes.md` são compartilhados. A 010 rebaseia sobre `main` depois do merge de `008-nucleo-monetario` e `009-exemplos-utilizacao`. Os exemplos da 009 que usam fachadas deprecadas migram na etapa 12. Os pacotes `verificacao` e `mascara` ficam disponíveis para a 008.
