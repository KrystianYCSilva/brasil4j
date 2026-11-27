# Tasks: Contratos, polimorfismo e composição para documentos

**Base** `43c604d` (sobre `18bfe9c`) · **Data** 2026-11-27

**Entradas** [Spec](spec.md), [plano](plan.md), [pesquisa](research.md), [modelo](data-model.md), [contrato](contracts/api.md) e [guia](quickstart.md).

## Formato e convenções

- Cada tarefa usa `- [ ] Tnnn [P?] [USn?]`. `[P]` indica autoria paralela em arquivos distintos depois das dependências indicadas.
- Os caminhos são relativos à raiz do reator. `core` é `brasil4j-core/src/main/java/io/github/brasil4j`, `core-test` é `brasil4j-core/src/test/java/io/github/brasil4j` e `kt` é `brasil4j-kotlin/src`.
- Cada par RED/GREEN cobre uma invariante. Registrar em `specs/010-contratos-documentos/analysis.md` o comando, a falha esperada, a correção mínima e a decisão de refatoração. Depois de cada GREEN, revisar com a suíte do módulo verde.
- **Nenhuma asserção existente pode ser alterada.** Casos já satisfeitos são registrados como regressões verdes.
- Cada etapa do plano termina com uma tarefa de commit (`git commit`, com coautoria).

## Fase 1 · Preparação (etapa 1 · caracterização)

- [ ] T001 Executar `.\mvnw.cmd -B clean verify` na base em JDK 8 e 21. Registrar versões e contagens de Surefire e Failsafe em `specs/010-contratos-documentos/analysis.md`.
- [ ] T002 Criar `core-test/identidade/CharacterizationTest.java` com regressões verdes do comportamento atual:
  - `toString` de CPF (`***.***.***-**`), NIS (`[NIS oculto]`) e CNPJ (canônico);
  - mensagens "CPF inválido.", "CNPJ inválido." e "NIS inválido." com IAE exata;
  - modo nulo gerando IAE em `Cpf.parse` e `Cnpj.parse`;
  - rejeição de `00000000000000` nos modos com DV e aceitação em `FORMAT_ONLY`;
  - máscara parcial e minúsculas rejeitadas.
- [ ] T003 Commit da etapa 1.

## Fase 2 · Fundação (bloqueia todas as histórias)

### Etapa 2 · Resultado, rejeição e regra

- [ ] T004 [P] RED em `core-test/identidade/RejectionTest.java`: fábricas, código não vazio, igualdade e `toString` sem conteúdo.
- [ ] T005 [P] RED em `core-test/identidade/ValidationResultTest.java`: estado único, `map`, `fold`, as três formas de `orElseThrow` (IAE exata, mensagem padrão "Documento inválido.") e `toString`.
- [ ] T006 [P] RED em `core-test/identidade/ValidationRuleTest.java`: `of`, `accept`, `and` com curto-circuito e ordem, e NPE em argumentos nulos.
- [ ] T007 GREEN em `core/identidade/RejectionReason.java`, `Rejection.java`, `ValidationResult.java` (construtor package-private) e `ValidationRule.java`, conforme o [contrato](contracts/api.md#iogithubbrasil4jidentidade).
- [ ] T008 Commit da etapa 2.

### Etapa 3 · Algoritmo de DV

- [ ] T009 RED em `core-test/verificacao/Modulus11Test.java`: validação de nulo, comprimento e domínio (inclusive caracteres abaixo de `'0'`), `digitCount` e `verify`.
- [ ] T010 RED em dois testes, um por pacote, para acessar as classes package-private: `core-test/identidade/cpf/CpfModulus11EquivalenceTest.java` compara `Modulus11.cpf()` com `CpfCheckDigits` e `core-test/identidade/cnpj/CnpjModulus11EquivalenceTest.java` compara `Modulus11.cnpj()` com `CnpjCheckDigits`. Usar os vetores existentes e 10 000 bases geradas com semente fixa. Gravar as bases e os DVs esperados em `brasil4j-core/src/test/resources/verificacao/cpf-equivalencia.csv` e `cnpj-equivalencia.csv`.
- [ ] T011 GREEN em `core/verificacao/CheckDigitAlgorithm.java` e `core/verificacao/Modulus11.java`, com builder e presets `cpf()` e `cnpj()`. O JavaDoc cita e-Financeira e IN RFB 2.119/2022.
- [ ] T012 REFACTOR: `CpfValidationMode` e `CnpjValidationMode` passam a usar `Modulus11`. O bloqueio de 14 zeros vira uma regra canônica explícita. Converter os dois testes de equivalência para ler os CSVs congelados. Remover `core/identidade/cpf/CpfCheckDigits.java` e `core/identidade/cnpj/CnpjCheckDigits.java`. Toda a suíte fica verde.
- [ ] T013 Commit da etapa 3.

### Etapa 4 · Máscara

- [ ] T014 RED em `core-test/mascara/DocumentMaskTest.java`:
  - slots `9`, `A` e `X`;
  - `unmask` de canônico e de máscara exata, e rejeição de máscara parcial, minúsculas, espaços e nulo;
  - `apply`;
  - `canonicalLength`;
  - `applySecure` ocultando prefixo e sufixo de slots e preservando literais; soma maior ou igual aos slots oculta tudo; IAE para quantidade negativa e para caractere dígito ou letra;
  - padrão sem slot gerando IAE.
- [ ] T015 GREEN em `core/mascara/DocumentMask.java`.
- [ ] T016 REFACTOR: `CpfFormatter.canonicalize`, `CnpjFormatter.canonicalize`, `Cpf.applyMask` e `Cnpj.applyMask` passam a delegar a `DocumentMask` (`999.999.999-99` e `XX.XXX.XXX/XXXX-99`). A suíte e a caracterização ficam verdes.
- [ ] T017 Commit da etapa 4.

### Etapa 5 · Contratos de valor

- [ ] T018 RED em `core-test/identidade/AbstractDocumentTest.java`: igualdade por classe e canônico, NPE para canônico nulo e `toString` oculto por padrão. Incluir:
  - asserções de `instanceof`: `Cpf` e `Cnpj` como `MaskedDocument`, `SecurityMaskedDocument` e `CheckDigitDocument`; `Nis` como `SecurityMaskedDocument`, mas não como `MaskedDocument` nem `CheckDigitDocument`;
  - `Cnpj.hasValidCheckDigits()`, falso para 14 zeros e para DV divergente;
  - máscara de segurança (US1-4): `01234567890` → `***.345.678-**`, com `#` → `###.345.678-##`; `12ABC34501DE35` → `**.ABC.345/01DE-**`; `01234567898` → `***3456789*`;
  - sem ocultação: `applyMask()` do CPF e do CNPJ inalterado e `getCanonicalValue()` do NIS;
  - IAE para caractere de máscara dígito ou letra;
  - `toString` dos três inalterado.
- [ ] T019 GREEN em `core/identidade/Document.java`, `MaskedDocument.java`, `SecurityMaskedDocument.java`, `CheckDigitDocument.java` e `AbstractDocument.java`. Ajustar `Cpf`, `Cnpj` e `Nis` para estender `AbstractDocument`, mantendo os `toString` atuais. Acrescentar `Cnpj.hasValidCheckDigits()` e `applySecurityMask()`/`applySecurityMask(char)` nos três, via `DocumentMask.applySecure`. O NIS usa a máscara canônica `99999999999`, interna e sem pontuação.
- [ ] T020 Gate de compatibilidade: empacotar o candidato e rodar `scripts/check-api-compatibility.py` contra `v0.2.0` e `5fd9dae`, cada baseline em um diretório separado. Se houver incompatibilidade, manter em `Cpf` e `Cnpj` overrides finos que delegam. Registrar o relatório em `analysis.md`.
- [ ] T021 Commit da etapa 5.

### Etapa 6a · Interpretador e builder

- [ ] T022 Criar `core-test/identidade/DocumentParserContractTest.java`, abstrato, com casos de contrato: nulo e `""` como `MISSING`; espaços e lixo como `INVALID_FORMAT`; nenhuma exceção em `parse`; `parseOrThrow` com IAE exata e mensagem sem a entrada; `isValid` coerente com `parse`; `withRule` e `withPolicy` encadeados, sem execução depois de uma rejeição nativa; `toString` sem a entrada.
- [ ] T023 RED em `core-test/identidade/DocumentParsersTest.java`: builder com `mask`, `normalizer` (exclusivos), `canonicalRule`, `checkDigits`, `documentRule` e `errorMessage`; `IllegalStateException` sem normalização; NPE em nulos; ordem do pipeline conforme o [modelo](data-model.md#pipeline-do-interpretador-construído-pelo-builder).
- [ ] T024 GREEN em `core/identidade/DocumentParser.java` e `core/identidade/DocumentParsers.java`. As decorações preservam a mensagem de erro.
- [ ] T025 Commit da etapa 6a.

## Fase 3 · US2 · Depender de contratos (P1)

**Teste independente:** um componente recebe `DocumentParser` por parâmetro e é exercitado com CPF e CNPJ em todos os modos e com um dublê. Verifica os motivos e a ausência de exceção.

- [ ] T026 [P] [US2] RED em `core-test/identidade/cpf/CpfParserContractTest.java`, que estende `DocumentParserContractTest`, para `Cpf.parser()` e cada modo. Acrescentar os cenários US2-1 (`28001238939`) e IAE para modo nulo.
- [ ] T027 [P] [US2] RED em `core-test/identidade/cnpj/CnpjParserContractTest.java` para `Cnpj.parser()` e cada modo. Acrescentar os cenários US2-2, `UNSUPPORTED_BASE` no modo numérico, US2-3 (14 zeros) e IAE para modo nulo.
- [ ] T028 [US2] GREEN em `core/identidade/cpf/Cpf.java`, `CpfValidationMode.java`, `core/identidade/cnpj/Cnpj.java` e `CnpjValidationMode.java`. Cada constante de modo compõe um parser construído pelo `DocumentParsers`. `parse(input, mode)` delega a `parser(mode).parseOrThrow`. `validatedCanonical` fica como ponte package-private.
- [ ] T029 [US2] RED/GREEN em `core-test/identidade/ParserInjectionTest.java`: componente sintético com `DocumentParser<Cpf>` injetado e um dublê que rejeita com código próprio.
- [ ] T030 [US2] Commit da etapa 6b.

## Fase 4 · US1 · Documento próprio do consumidor (P1)

**Teste independente:** a `Matricula` sintética do [guia](quickstart.md#documento-do-consumidor) produz os quatro desfechos e participa de uma lista com CPF, CNPJ e NIS.

- [ ] T031 [US1] RED em `core-test/identidade/CustomDocumentExampleTest.java`: `Matricula` montada só com blocos públicos (`AbstractDocument`, `MaskedDocument`, `DocumentMask`, `Modulus11.builder()`, `DocumentParsers`). Cenários US1-1 e US1-2, igualdade e `toString` oculto. Uma subclasse `MatriculaParserContractTest` aplica o contrato.
- [ ] T032 [US1] GREEN mínimo no que faltar em `Modulus11.Builder` ou `DocumentParsers`, sem código específico de `Matricula` na biblioteca.
- [ ] T033 [P] [US1] RED em `core-test/identidade/DocumentFormatterTest.java`: `canonical`, `masked`, `secure()`/`secure(char)` e `redacted` (argumentos negativos geram IAE; soma maior ou igual ao comprimento oculta tudo). Incluir a `Matricula` com máscara de segurança própria (US1-4).
- [ ] T034 [US1] GREEN em `core/identidade/DocumentFormatter.java`. Commit da etapa 8.
- [ ] T035 [US1] RED em `core-test/identidade/composicao/DocumentCheckOfTest.java`:
  - `DocumentCheck.of` com `Matricula.PARSER` numa lista com `cpf`, `cnpj` e `nis` (US1-3);
  - `getRejection` presente nas duas rejeições;
  - `POLICY` do interpretador mapeado para `REJECTED_BY_POLICY`;
  - NPE para parser nulo.
- [ ] T036 [US1] GREEN em `core/identidade/composicao/DocumentCheck.java` e `DocumentCheckResult.java`. `evaluate` usa `parse` sem `try/catch`. As fábricas `cpf`, `cnpj` e `nis` delegam a `of` e mantêm NPE para modo nulo. As suítes da 007 ficam verdes sem alteração.
- [ ] T037 [US1] Commit da etapa 9.

## Fase 5 · US3 · Regras e validadores de DV do NIS (P2)

**Teste independente:** os vetores C1 07, 09 e 15–21 nas três configurações e com a guarda opcional produzem os desfechos de [D6](research.md#d6--nis-e-proveniência).

- [ ] T038 [P] [US3] RED em `core-test/verificacao/Modulus11PisTest.java`: `pisCaixaFgtsV016()` (o resto 1 resulta em `compute` vazio) e `pisLegacyMod11()`, com os vetores de D6.
- [ ] T039 [P] [US3] RED em `core-test/identidade/nis/NisParserContractTest.java`, que estende o contrato:
  - padrão estrutural aceitando 09, 16, 18 e 20;
  - `CAIXA_FGTS_V016` e `LEGACY_MOD11` conforme D6;
  - `withRule(NisRules.repeatedDigits())` rejeitando o 07 e aceitando o 21;
  - regra do consumidor com código `PREFIXO_IMPORTACAO` (US3-4).
- [ ] T040 [US3] GREEN nos presets PIS em `core/verificacao/Modulus11.java` e em `core/identidade/nis/NisValidationMode.java`, `NisRules.java` e `Nis.java` (`parser()`, `parser(mode)`; `parse` delegando). O JavaDoc cita fonte e alcance, sem a palavra "oficial".
- [ ] T041 [US3] Atualizar `docs/fontes.md` com entradas para a fórmula publicada da Caixa FGTS v016 (alcance PIS/Pasep no FGTS), para a convenção legada (Declarações e UEM, matriz C1) e para a RV_03 do SIPREV (alcance SIPREV). Registrar a divergência no caso 17 e a extensão ao NIS/NIT como não confirmada. Registrar também o layout da máscara de segurança como escolha de projeto (decisão do usuário em 2026-11-27), sem rótulo de norma.
- [ ] T042 [US3] Commit da etapa 7.

## Fase 6 · US4 · Migração sem quebra e Kotlin (P3)

**Teste independente:** as suítes existentes ficam verdes sem alteração, o japicmp não registra incompatibilidades e as fachadas indicam o substituto.

- [ ] T043 [P] [US4] RED em `kt/test/kotlin/io/github/brasil4j/kotlin/identidade/ContractExtensionsTest.kt`: `toCpfOrNull`, `toCnpjOrNull`, `toNisOrNull`, `DocumentParser.parseOrNull`, `String.toNis(mode)`, `Cnpj.hasValidCheckDigits` e `applySecurityMask(maskChar = '*')` em `Cpf`, `Cnpj` e `Nis`.
- [ ] T044 [US4] GREEN em `kt/main/kotlin/io/github/brasil4j/kotlin/identidade/Cpf.kt`, `Cnpj.kt`, `Nis.kt` e `DocumentParsers.kt`. `Nis.kt` passa a delegar a `Nis.parser()` e preserva NPE e a propagação de exceções. KDoc completo.
- [ ] T045 [US4] Estender `core-test/identidade/composicao/DocumentCheckJarIT.java` e `kt/test/kotlin/io/github/brasil4j/kotlin/identidade/DocumentCheckJarIT.kt` para consumir `DocumentParser`, `ValidationResult` e as extensões a partir dos JARs, conferindo o `codeSource`. Commit da etapa 10.
- [ ] T046 [US4] Marcar com `@Deprecated` e `@deprecated` (indicando o substituto) `CpfValidator`, `CpfFormatter`, `CnpjValidator`, `CnpjFormatter`, `NisValidator` e `NisFormatter`, conforme o [contrato](contracts/api.md#fachadas-deprecadas). Os testes que exercitam as fachadas recebem `@SuppressWarnings("deprecation")`, sem alterar asserções.
- [ ] T047 [US4] Gate final de compatibilidade contra `v0.2.0`, `5fd9dae` e `18bfe9c`. Registrar os relatórios em `analysis.md`. Commit da etapa 11.

## Fase 7 · Acabamento (etapa 12)

- [ ] T048 [P] Atualizar `README.md` com uso por contrato, uma seção "Criando seu próprio documento" e o NIS com validadores opcionais. Atualizar `docs/arquitetura.md` com os novos pacotes e um diagrama de classes Mermaid.
- [ ] T049 [P] Atualizar `CHANGELOG.md` ("Não lançado") com contratos, blocos compartilhados, validadores opcionais do NIS e deprecações.
- [ ] T050 Buscar `oficial` em `brasil4j-*/src` e `docs` e conferir que cada ocorrência remete a uma fonte normativa confirmada em `docs/fontes.md` (SC-006).
- [ ] T051 Rodar `.\mvnw.cmd -B clean verify` em JDK 8 e 21. Conferir JaCoCo ≥ 90% de linhas e ≥ 85% de branches por módulo, DocLint e Dokka, e registrar as contagens em `analysis.md`.
- [ ] T052 Revisão independente do diff com `agy -p --mode plan --sandbox` (sem shell) e pelo agente `quality-gatekeeper`. Registrar os achados e o tratamento de cada um em `analysis.md`. Commit da etapa 12.
- [ ] T053 Coordenação: rebasear sobre `main` depois do merge de `008-nucleo-monetario` e `009-exemplos-utilizacao`, resolvendo os arquivos compartilhados e migrando para a nova API os exemplos da 009 que usem fachadas.

## Dependências

- A Fase 1 vem antes da Fase 2. Dentro da Fase 2, a ordem é estrita: etapa 2 → 3 → 4 → 5 → 6a.
- A US2 (Fase 3) depende da 6a. A US1 (Fase 4) depende da 6a e usa CPF, CNPJ e NIS nas listas, por isso vem depois da US2. A US3 depende da 6a e da T028. A US4 depende das anteriores.
- O Kotlin (T043–T045) depende de T040. As deprecações (T046) vêm depois de T044.

## Paralelismo

- T004, T005 e T006 são testes em arquivos distintos.
- T026 e T027.
- T033, em paralelo com T031 e T032.
- T038 e T039.
- T048 e T049.
- As tarefas GREEN que tocam os mesmos tipos de documento não rodam em paralelo.

## Estratégia

O MVP é a Fase 2 mais as US2 e US1: contratos, builder e documento do consumidor. A US3 e a US4 completam a entrega. Cada etapa deixa o reator verde e compatível.
