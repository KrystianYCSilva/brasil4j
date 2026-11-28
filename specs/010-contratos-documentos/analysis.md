# Registro de execução · Spec 010

**Base** `18bfe9c` mais os commits de documentação da 010 · **Executor** Claude Code · **Início** 2026-11-27

## Ponto de partida (T001)

O `clean verify` em modo offline, no Corretto 21.0.13 com o Wrapper do reator, passou com 480 testes e zero falhas, o mesmo total registrado no fechamento da Spec 007. A rodada em Corretto 8u512 fica para o gate final (T051), para não disputar os diretórios `target/` durante as etapas.

## Rebase sobre a Spec 009 (2026-11-27)

A pedido do usuário, a 010 foi rebaseada sobre `009-exemplos-utilizacao` (`ad3f6e7`, que contém a 008 em `c0a91af`), sem conflitos. O estado anterior ficou em `backup/010-antes-rebase`. Depois do rebase, o `clean verify` no Corretto 21.0.13 passou com 554 testes: núcleo 392, Kotlin 134 e exemplos 28. O `brasil4j-exemplos` usa as fachadas de CPF, CNPJ e NIS e passa a ser gate de regressão do consumidor em cada etapa.

## Etapas

| Etapa | RED (falha esperada) | GREEN (mudança mínima) | Refatoração |
| --- | --- | --- | --- |
| 1 · Caracterização | Não se aplica: as regressões foram escritas sobre o comportamento atual e passaram verdes de imediato. | `CharacterizationTest` fixa o `toString` dos três tipos, as mensagens com IAE exata, o modo nulo, o bloqueio de 14 zeros e a rejeição de máscara parcial e minúsculas. | — |
| 2 · Resultado, rejeição e regra | `RejectionTest`, `ValidationResultTest` e `ValidationRuleTest` não compilavam: `cannot find symbol class Rejection`. | `RejectionReason`, `Rejection`, `ValidationResult` (construtor package-private, `Valid`/`Invalid` privados) e `ValidationRule` com `of`, `accept` e `and`. Os 3 testes passaram. | A mensagem padrão "Documento inválido." ficou numa constante interna reutilizada pelo parser. |
| 3 · Algoritmo de DV | `Modulus11Test` e os dois testes de equivalência não compilavam: `cannot find symbol class Modulus11`. | `CheckDigitAlgorithm` e `Modulus11` (builder, `cpf()`, `cnpj()`). A equivalência ao vivo conferiu 1 000 000 de combinações base+DV por documento contra `CpfCheckDigits` e `CnpjCheckDigits`, sem divergência. | Um gerador temporário congelou com os algoritmos antigos 10 000 bases por documento em `src/test/resources/verificacao/*-equivalencia.csv`, e os testes passaram a ler esses CSVs. Os modos e `Cpf.hasValidCheckDigits` usam `Modulus11`. O bloqueio de 14 zeros virou `CnpjValidationMode.hasCheckDigits`. `CpfCheckDigits` e `CnpjCheckDigits` foram removidos. Núcleo, Kotlin e exemplos verdes. Os presets PIS foram retirados desta etapa para entrar com RED próprio na etapa 7. |
| 4 · Máscara | `DocumentMaskTest` não compilava: `cannot find symbol class DocumentMask`. | `DocumentMask` com slots `9`, `A` e `X`, `unmask` (canônico ou máscara completa exata), `apply` e `applySecure` (prefixo e sufixo ocultos, literais preservados, caractere de ocultação não alfanumérico). | Os `canonicalize` de `CpfFormatter` e `CnpjFormatter` e os `applyMask` de `Cpf` e `Cnpj` delegam a `Cpf.MASK` (`999.999.999-99`) e `Cnpj.MASK` (`XX.XXX.XXX/XXXX-99`). As quatro expressões regulares e os `substring` saíram. Núcleo, Kotlin e exemplos verdes, sem alteração de asserções. |
| 5 · Contratos de valor | `AbstractDocumentTest` não compilava: `cannot find symbol class AbstractDocument`. | `Document`, `MaskedDocument`, `SecurityMaskedDocument` (default `applySecurityMask()` com `*`), `CheckDigitDocument` e `AbstractDocument` (`equals`/`hashCode` finais por classe e canônico; `toString` "[documento oculto]"). `Cpf` e `Cnpj` implementam as três capacidades; `Nis` só a máscara de segurança. Máscara de segurança: CPF 3+2, CNPJ 2+2, NIS 3+1 sem pontuação. `Cnpj.hasValidCheckDigits()` respeita F-05. | `getCanonicalValue` foi mantido como override fino nos três tipos, preservando o JavaDoc. `Nis.hasCanonicalFormat` usa a máscara canônica interna. Núcleo, Kotlin e exemplos verdes. **Gate japicmp (T020):** com autorização do usuário, baixei o japicmp 0.26.1 da Maven Central e conferi o SHA-256 `4b4f0301…f858`. A primeira rodada contra `v0.2.0` e `5fd9dae` falhou com `METHOD_NOW_FINAL` em `equals` e `hashCode` de `Cpf`, `Cnpj` e `Nis`. Esses métodos deixaram de ser `final` em `AbstractDocument`, com o contrato documentado. A segunda rodada passou contra as duas baselines: apenas `MODIFIED` compatíveis no núcleo e nenhuma mudança no Kotlin. |
| 6a · Interpretador e builder | `DocumentParsersTest` e a suíte abstrata `DocumentParserContractTest` não compilavam: `cannot find symbol class DocumentParser`. | `DocumentParser` (default `parseOrThrow`, `isValid`, `withRule`, `withPolicy`) e `DocumentParsers.builder` com o pipeline do modelo: ausência, normalização, regras canônicas, DV, fábrica e regras de documento. A decoração preserva a mensagem configurada. Rodaram 14 casos de contrato (`DocumentParsersTest$Contrato`) e 7 do builder. | Um canônico fora do domínio do algoritmo vira `INVALID_FORMAT`, sem exceção. Fábrica ou normalizador que devolvem nulo são erro de configuração (NPE). |
| 6b · US2 interpretadores nativos | `CpfParserContractTest`, `CnpjParserContractTest` e `ParserInjectionTest` não compilavam: `cannot find symbol method parser(CpfValidationMode)`. | Cada constante de `CpfValidationMode` e `CnpjValidationMode` passou a ser uma Strategy que configura o `DocumentParsers.Builder`. `Cpf` e `Cnpj` mantêm um interpretador por modo em `EnumMap`, com `parser()`/`parser(mode)` (IAE para modo nulo). `parse(input, mode)` delega a `parseOrThrow`. O CNPJ ganhou as regras canônicas de base numérica (`UNSUPPORTED_BASE`) e de 14 zeros (`INVALID_CHECK_DIGITS`). | `validatedCanonical` ficou como ponte package-private sobre o parser. Os `canonicalize` dos formatadores foram removidos. Núcleo, Kotlin e exemplos verdes, sem alteração de asserções. |
| 8 · Documento do consumidor e apresentação (US1) | `CustomDocumentExampleTest` e `DocumentFormatterTest` não compilavam: `cannot find symbol variable DocumentFormatter`. | `DocumentFormatter` (`extends Function`) com `canonical`, `masked`, `secure`/`secure(char)` e `redacted`. A `Matricula` sintética, montada só com `AbstractDocument`, `DocumentMask`, `Modulus11.builder()` e `DocumentParsers`, passou na suíte de contrato e nos cenários US1-1, US1-2 e US1-4, sem nenhum código específico na biblioteca. | A conferência do caractere de ocultação saiu da interface (onde seria pública em Java 8) para o auxiliar package-private `MaskChars`. |
| 9 · `DocumentCheck` aberto (US1) | `DocumentCheckOfTest` e `NisParserContractTest` não compilavam: `cannot find symbol method of(String, DocumentParser)`. | `DocumentCheck.of(input, DocumentParser<D>)`. `evaluate()` usa `parse` uma vez, sem `try/catch`. `POLICY` do interpretador mapeia para `REJECTED_BY_POLICY` e os demais motivos para `REJECTED_BY_VALIDATION`. `DocumentCheckResult.getRejection()`. `cpf`, `cnpj` e `nis` delegam a `of` e mantêm NPE para modo nulo. Antecipou-se `Nis.parser()` estrutural, com contrato próprio, porque `DocumentCheck.nis` precisava dele. | A política do item manteve a semântica de substituição da 007. As suítes da 007 (incluindo os JarIT) ficaram verdes sem alteração. |
| 7 · Validadores opcionais do NIS (US3) | `Modulus11PisTest` e o novo `NisParserContractTest` não compilavam: `cannot find symbol class NisValidationMode`. | `Modulus11.pisCaixaFgtsV016()` e `pisLegacyMod11()`; `NisValidationMode { FORMAT_ONLY, CAIXA_FGTS_V016, LEGACY_MOD11 }` como Strategy do builder; `NisRules.repeatedDigits()` (SIPREV RV_03, código `REPEATED_DIGITS`) e `NisRules.checkDigits(algoritmo)`; `Nis.parser(mode)`. Os vetores C1 07, 09 e 15–21 produziram os desfechos da D6 nas duas fórmulas. O padrão continua estrutural e aceita 09, 16, 18 e 20. | `Nis.parse`, `parseOrNull` e `hasCanonicalFormat` delegam ao interpretador estrutural. `docs/fontes.md` ganhou F-15 a F-18 com fonte e alcance, sem rótulo de oficial. |
| 10 · Kotlin e consumo pelos JARs (US4) | `ContractExtensionsTest.kt` não compilava: `Unresolved reference 'toCpfOrNull'`. | `toCpfOrNull`, `toCnpjOrNull` e `toNisOrNull`, que devolvem as value classes; `DocumentParser<D>.parseOrNull`; `String.toNis(mode)`; `Cnpj.hasValidCheckDigits()`; `applySecurityMask(maskChar = '*')` nas três value classes. `Nis.kt` passou a delegar a `Nis.parser()`, sem fachadas. `ContratosJarIT` (Java e Kotlin) consome contratos e extensões pelos JARs, com `codeSource` conferido. `clean verify` no JDK 21 passou: DocLint, Dokka, JaCoCo e bytecode. | Um `install` acidental gravou `io/github/brasil4j/brasil4j-core/0.2.0` no `~/.m2` local, que não existia antes. A remoção foi bloqueada pela política de ações destrutivas e aguarda decisão do usuário. Os builds seguintes usam só o reator. |
| 11 · Fachadas deprecadas e gate final (US4) | Não se aplica: a deprecação não muda comportamento. A rede foi a suíte existente, que precisava continuar verde sem alteração. | `CpfValidator`, `CpfFormatter`, `CnpjValidator`, `CnpjFormatter`, `NisValidator` e `NisFormatter` receberam `@Deprecated` e `@deprecated`, com o substituto e a remoção prevista para a 1.0. Os validadores passaram a Facades sobre `parser(mode)`. `clean verify` no JDK 21 passou, com 7 avisos de deprecação vindos de testes e exemplos, que continuam inalterados. **japicmp final** (T047), no JDK 21, contra `v0.2.0`, `5fd9dae` e `18bfe9c`: as três saídas foram 0, só com `MODIFIED` compatíveis. O "field removed" dos enums é o campo privado `strategy`. | `Nis.parseOrNull` e `Nis.hasCanonicalFormat` ficaram sem uso e foram removidos. |
| 12 · Documentação e exemplos (T048, T049, T054) | Não se aplica. | README com a Spec 010 na lista, uso por contrato e a seção "Criando seu próprio documento". `docs/arquitetura.md` com os pacotes `identidade`, `verificacao` e `mascara`, os padrões GoF e o diagrama de classes Mermaid. CHANGELOG. `ContratosExemplosTest.java` (4 casos) e `ContratosKotlinExemplosTest.kt` (3 casos) em `brasil4j-exemplos`, com o índice do README do módulo atualizado. Os exemplos existentes não foram alterados. | T045 foi feito em arquivos novos (`ContratosJarIT` Java e Kotlin) em vez de estender os `DocumentCheckJarIT`, para não tocar nas suítes da 007. |

## Verificação final (2026-11-28)

- **T051 · Build.** `clean verify` offline passou em Corretto 21.0.13 e em Corretto 8u512 (Standard Doclet 1.8.0_512). Zero falhas e erros:

  | Módulo | Surefire | Failsafe |
  | --- | --- | --- |
  | Núcleo | 20 557 | 88 |
  | Kotlin | 53 | 88 |
  | Exemplos | 35 | — |
  | **Total** | | **20 821** |

  As 20 001 + 10 000 linhas parametrizadas dos CSVs de equivalência entram na contagem do núcleo.
- **T051 · Gates.**
  - JaCoCo: núcleo com 98,8% das linhas (1291/1307) e 94,2% dos branches (588/624); Kotlin com 99,3% (145/146) e 100% (6/6).
  - DocLint passou nos dois JDKs, e Dokka no JDK 21.
  - `scripts/check-bytecode.py` confirmou bytecode Java 8 nos dois JARs.
  - O log do JDK 21 tem 7 avisos de deprecação, todos de testes e exemplos que exercitam as fachadas de propósito.
- **T020 e T047 · Compatibilidade.** japicmp 0.26.1 (SHA-256 `4b4f0301…f858`, baixado com autorização do usuário) contra baselines empacotadas em JDK 21 a partir de `git archive`. Código de saída 0 nas três:

  | Baseline | `MODIFIED` compatíveis | Incompatíveis | `NEW` |
  | --- | --- | --- | --- |
  | `v0.2.0` | 14 | 0 | 39 |
  | `5fd9dae` | 17 | 0 | 34 |
  | `18bfe9c` | 19 | 0 | 31 |

  O "field removed" dos enums de modo é o campo privado `strategy`. A primeira rodada da etapa 5 acusou `METHOD_NOW_FINAL` e foi corrigida (`055da9b`).
- **T050 · Proveniência.** Todas as linhas acrescentadas pela 010 que mencionam "oficial" negam a oficialidade: `recorte.md`, `revisao.md`, F-15 em `fontes.md`, `NisRules.java` e `NisValidationMode.java`. Nenhuma regra nova foi rotulada como oficial. F-15 a F-17 usam CONVENCAO_TECNICA com fonte e alcance, e F-18 usa ESTRUTURAL como escolha de projeto.
- **T052 · Revisão independente.**
  - O agente `quality-gatekeeper` deu FAIL só por rastreabilidade: tarefas desmarcadas e evidência final ausente neste registro. Os dois pontos foram corrigidos aqui. Ele não encontrou placeholders, segredos, testes vazios, JavaDoc faltando nem divergência entre `contracts/api.md` e o código conferido.
  - A primeira chamada ao `agy` estourou o limite de 9m30s sem conteúdo. A segunda, de escopo menor, deu **APROVA COM AJUSTES** e confirmou três pontos: `parse` nunca lança para entrada inválida, os diagnósticos não contêm a entrada e nenhuma regra é rotulada como oficial sem fonte. O tratamento de cada achado:
    - Decorar um interpretador do consumidor descartava o `parseOrThrow` dele. **Corrigido:** a rejeição nativa agora delega ao `parseOrThrow` de origem.
    - No `LEGACY_MOD11`, só `00000000000` recebia o código `REPEATED_DIGITS`. **Corrigido:** a guarda virou regra canônica, aplicada antes do cálculo, como nos legados.
    - O contrato divergia do código em dois pontos: NPE e não IAE para nulo no `Modulus11`, e o formato do `toString` de `Invalid`. **`api.md` corrigido.**
    - O JavaDoc de `withPolicy` não mencionava "só espaços". **Corrigido.**
    - **Sugestões aplicadas:** os presets do `Modulus11` ficaram em cache como constantes imutáveis, e a conversão de DV em rejeição foi centralizada na nova regra pública `ValidationRule.checkDigits`, usada pelo builder e por `NisRules`.
    - **Sugestões não aplicadas:** estratégia interna de pesos no `Modulus11` (refatoração interna, sem ganho de contrato) e variância de `canonicalRule` (`String` é final).
    - Depois dos ajustes, `clean verify` passou em JDK 21 e JDK 8, e o japicmp segue sem incompatibilidade contra as três baselines.
- **Validação antes do merge (2026-11-28).** A branch foi rebaseada sobre a `main` em `5405591`, onde está a 009 reescrita depois do primeiro rebase. Houve conflito só de texto no README, resolvido mantendo as linhas da 009 e da 010, e o backup ficou em `backup/010-antes-rebase-main`. O `clean verify` passou em JDK 21 e JDK 8, inclusive os exemplos novos da 009, que não foram alterados. Pareceres pedidos em modo somente leitura:
  - **Antigravity: APROVA COM AJUSTES.** Confirmou a preservação integral das Specs 008 e 009 nos documentos compartilhados e apontou o mesmo bloqueante do Codex (item 1 abaixo).
  - **Codex (`llm-call.ps1`, modo read): REPROVA.** Achados e tratamento:
    1. **Bloqueante:** com duas ou mais decorações, a exceção do interpretador do consumidor se perdia. RED em `DocumentParsersTest`. **Corrigido** com Decorator achatado (`DecoratedParser`): a origem é interpretada uma vez e `parseOrThrow` delega ao da origem. Um teste confere uma única interpretação em três camadas.
    2. **Importante:** overflow em `Modulus11` com pesos grandes. RED em `Modulus11Test.pesosGrandesNaoTransbordam`, que esperava 9 e recebia 0. **Corrigido** reduzindo cada parcela módulo 11, com equivalência congelada intacta.
    3. **Importante:** faltavam os nove vetores C1 no modo estrutural. O teste `estruturalAceitaOsNoveVetoresC1` foi acrescentado e passou de imediato, como regressão.
    4. **Sugestão:** o contrato de `verify` descrevia exceção para sufixo divergente. **JavaDoc e `api.md` corrigidos.**
    5. **Sugestão:** rastreabilidade defasada. **Atualizada nesta seção.**
- **Segunda rodada, em `c1aa4e6`.** O `clean verify` passou em JDK 8 e em JDK 21. Os números finais:

  | Módulo | Surefire | Failsafe |
  | --- | --- | --- |
  | Núcleo | 20 573 | 88 |
  | Kotlin | 53 | 88 |
  | Exemplos | 37 | — |
  | **Total** | | **20 839** |

  - Zero falhas. JaCoCo do núcleo com 98,8% das linhas e 94,1% dos branches, e do Kotlin com 99,3% e 100%.
  - Bytecode 52 nos dois JARs. japicmp sem incompatibilidade contra `v0.2.0`, `5fd9dae` e `18bfe9c`.
  - **Antigravity: APROVA PARA MERGE.** Confirmou o Decorator achatado, a redução módulo 11 e a rastreabilidade.
  - **Codex: APROVA PARA MERGE.** Confirmou os cinco achados resolvidos, o merge fast-forward e o `git diff --check` limpo. Também conferiu nos XMLs locais os 20 839 testes, a cobertura e o bytecode 52.
- **T053 · Coordenação.** Concluída: a 010 está rebaseada sobre a `main` em `5405591`, com a 008 e a 009 integradas, sem divergência e com merge fast-forward possível.
- **Pendência de ambiente.** Um `install` acidental na etapa 10 gravou `~/.m2/repository/io/github/brasil4j/brasil4j-core/0.2.0` com o JAR candidato. A remoção foi bloqueada pela política de ações destrutivas e aguarda decisão do usuário. Nenhum build da 010 depende desse artefato, porque todos usam o reator.

## Expansão: governança, automação e CI (2026-11-28)

O usuário aprovou o [plano v2](../../docs/descoberta/010-contratos-documentos/plano-governanca-qualidade.md) para que planejamentos e revisões futuras cobrem explicitamente qualidade de desenho, padrões, arquitetura, regras de biblioteca e qualidade de testes. Todas as entregas abaixo foram verificadas em `8105a4e`.

| Trilha | Commit | Entrega e evidência |
| --- | --- | --- |
| A · Governança | `ed1b9ba` | Constituição v0.6.0 em inglês, com regras numeradas e os princípios VI (qualidade de desenho e testes) e VII (entrega). `.context` em inglês, com nove arquivos de cerca de 300 a 700 tokens e um `review-checklist` de 12 lentes. `AGENTS.md`, `CLAUDE.md` e `GEMINI.md` versionados, ligando todo o `.context`. Templates do Spec Kit com Design Quality Gate e Design Decisions. Template de PR. |
| B1 · Kotlin explicit API | `00736d3` | RED: a compilação exigiu visibilidade explícita em 119 declarações. GREEN: `public` explícito, sem mudar a visibilidade efetiva. |
| B2 · Nome de módulo | `ab83a89` | RED: `ModuleNameJarIT` falhou (atributo ausente). GREEN: `io.github.brasil4j.core` e `io.github.brasil4j.kotlin` no manifest dos JARs. |
| B3 · ArchUnit | `1598ca2` | `archunit` 1.5.1 em escopo de teste, com testes JUnit comuns, porque o engine JUnit 5 do ArchUnit descobria 0 testes na Platform 5.12. RED: uma violação semeada em `mascara` fez AR-4 e AR-7 falharem. GREEN em JDK 8 e 21. |
| B4/B5 · PIT e versão | `ef24730` | Perfil `mutation` com pitest 1.30.2. Baseline de 89,5% (826 de 923 mutantes) e gate de 89%. Versão `0.3.0-SNAPSHOT`. |
| C · CI e GitHub | `8105a4e` | `verify.yml` com `verify (8)`, `verify (21)`, `api-compatibility` (contra a última tag), `mutation` e `branch-policy`. `release.yml` por tag na `main`, sem Maven Central, porque o namespace aguarda aprovação. Dependabot. Rulesets sem bypass e `scripts/github/apply-repo-settings.ps1`, testado em dry-run. |
| D · Global | `~/.agents@804436f` | A skill `multi-llm-cli-delegation` ganhou o comportamento verificado do `agy` e o inventário dos 13 CLIs da máquina. Commit local, sem push. |

**Verificação final em `8105a4e`:**
- `clean verify` passou em JDK 8 e 21, com 20 848 testes e zero falhas.
- JaCoCo do núcleo com 98,8% e 94,1%, e do Kotlin com 99,3% e 100%.
- Bytecode 52 nos dois JARs.
- japicmp sem incompatibilidades contra `v0.2.0`, `5fd9dae` e `18bfe9c`.

**Ocorrências de ambiente:**
- O `~/.gitconfig` do usuário desapareceu durante a sessão, por ação externa: nenhum comando desta sessão tocou nele. Os commits seguintes usaram identidade por comando (`git -c`), sem recriar o arquivo.
- A validação do `~/.agents` confirmou que o Codex desktop continua importando skills para a biblioteca, porque "Keep import in sync" está ativo.


## Pós-merge: correções do pr-review

Correções dos achados do pr-review após o merge da PR #1, na branch `feature/010-pr-review-fixes`, com TDD (III.1). A compatibilidade segue aditiva: `InvalidBaseException` é um tipo novo e estende `IllegalArgumentException`, e o pacote `verificacao` não existia na `v0.2.0`.

| Achado | Teste (RED) | Mensagem da falha | GREEN |
| --- | --- | --- | --- |
| `ValidationRule.checkDigits` convertia toda `IllegalArgumentException` em `INVALID_FORMAT` | `ValidationRuleTest.algoritmoMalConfiguradoNaoViraFormatoInvalido` | `Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` (a regra devolvia `INVALID_FORMAT`) | Só `InvalidBaseException` vira `INVALID_FORMAT`; as demais exceções se propagam. `baseForaDoDominioContinuaSendoFormatoInvalido` e `algoritmoPodeSinalizarBaseForaDoDominioPelaExcecaoDedicada` documentam o caso de domínio. |
| `Modulus11.Builder.build()` aceitava mais DVs que pesos | `Modulus11Test.construcaoRecusaConfiguracaoIncoerente` | `Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` | `build()` recusa `weightsFromLeft(1, 2).digits(3)`; o limite `digits(2)` com dois pesos continua válido. |
| Valor de caractere negativo só falhava no cálculo | `Modulus11Test.recusaValoresDeCaractereOuDvForaDoIntervalo` (ajustado para exigir a falha no `build()`) | `Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` | `build()` confere o valor de cada caractere do alfabeto. |
| Alfabeto sem dígitos com mais de um DV (o DV entra na base do seguinte) | `Modulus11Test.construcaoRecusaAlfabetoSemOsDigitosQuandoHaMaisDeUmDv` | `Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` | `build()` exige os dígitos de 0 a 9 no alfabeto quando `digits > 1`. |

Os presets não mudaram: `CpfModulus11EquivalenceTest` e `CnpjModulus11EquivalenceTest` seguem verdes.

**Testes que já passavam (sem defeito encontrado):** `DocumentFormatterTest.quantidadesVisiveisMaximasOcultamTudoSemTransbordo` (`redacted(Integer.MAX_VALUE, Integer.MAX_VALUE, ...)` oculta tudo), `DocumentParsersTest.alterarOConstrutorDepoisDeBuildNaoAfetaOInterpretadorJaCriado`, `DocumentParsersTest.decoracoesDaMesmaOrigemSaoIndependentes`, `DocumentCheckOfTest.regraDoInterpretadorComMotivoQueNaoEPoliticaViraRejeicaoDeValidacao` (motivo `UNSUPPORTED_BASE` resulta em `REJECTED_BY_VALIDATION`) e `AbstractDocumentTest.canonicoNuloLancaNpeComMensagemEQualquerTextoNaoNuloEAceito`, que caracteriza o NPE do canônico nulo. A `AbstractDocument` não ganhou validação nova: o JavaDoc diz que a subclasse valida o canônico.

**Sem comportamento:** `scripts/github/apply-repo-settings.ps1` agora lança erro quando a listagem de rulesets falha, em vez de seguir para o POST (sem harness de teste). Comentários corrigidos: bloqueio de 14 zeros atribuído a F-05 e Anexo XV só para o cálculo (`Modulus11`, `Cnpj`, `CnpjValidationMode`), máscara `999.999.999-99` no `CpfFormatter`, mensagem de `parseOrThrow` restrita aos interpretadores de `DocumentParsers.builder` e uso `withRule(NisRules.checkDigits(...))` no `Nis`.

**Verificação:** `clean verify` passou em JDK 8 (Corretto 1.8.0_512) e JDK 21 (Corretto 21.0.13), com 20 590 testes no núcleo, 89 no Kotlin e zero falhas; JaCoCo do núcleo com 98,9% (instruções) e 94,3% (ramos), e do Kotlin com 99,0% e 100%; bytecode 52 nos dois JARs. PIT (perfil `mutation`): 937 mutantes, 840 mortos, 90%, acima do gate de 89%.

## Revisão independente na worktree do Codex · 2026-11-28

> Rodada feita sobre um checkpoint intermediário da 010 (`93ca2e6`), anterior às correções dos pareceres e ao merge do PR #1 (`b4ad5ec`). Os números abaixo valem para esse checkpoint; os finais estão nas seções anteriores.

O checkpoint `6db94c3` do Claude foi reaplicado em uma worktree própria sobre a versão integrada dos Exemplos (`5405591`). O candidato `93ca2e6` mantém os exemplos da Spec 007 e acrescenta os da 010. O ajuste de integração preservou os links das duas specs no README. As árvores de código dos módulos `brasil4j-core` e `brasil4j-kotlin` são idênticas no checkpoint e no candidato.

| Verificação | Corretto 8u512 | Corretto 21.0.13 |
| --- | --- | --- |
| `clean verify` | 20.823 testes | 20.823 testes |
| Testes por módulo | 20.645 núcleo, 141 Kotlin, 37 exemplos | 20.645 núcleo, 141 Kotlin, 37 exemplos |
| JaCoCo do núcleo · linhas / branches | 98,77% / 94,23% | 98,78% / 94,23% |
| JaCoCo Kotlin · linhas / branches | 99,32% / 100% | 99,32% / 100% |
| JARs Java e Kotlin | Bytecode 52 e suítes `ContratosJarIT` | Bytecode 52 e suítes `ContratosJarIT` |
| Documentação | Javadoc com DocLint | Javadoc com DocLint e Dokka |

Em ambos os JDKs, foram zero falhas, erros ou pulos. A comparação por japicmp dos dois JARs reconstruídos com os da `v0.2.0`, da Spec 006 (`5fd9dae`) e da Spec 007 (`18bfe9c`) passou com os critérios binário e de fonte.

Os ensaios de consumo executaram interpretador e formatador injetáveis, documento sintético do consumidor em uma lista heterogênea, regras com rejeição por motivo e preservação do NIS estrutural. A revisão também alinhou o contrato de `Modulus11` ao comportamento e ao JavaDoc existentes para argumentos nulos. Os relatórios locais desta rodada ficam em `.tmp/review010/`.

## Fechamento dos contratos para a v0.3.0 · 2026-11-28

Esta rodada substitui a exigência de todos os dígitos no alfabeto e a conversão exclusiva de `InvalidBaseException` registradas na revisão pós-merge. O TDD começou na base `d0164a3` e a worktree foi atualizada para `73af5d0`. A entrada de changelog da Spec 011 veio dessa base.

| Caso | RED em JDK 8 | GREEN em JDK 8 |
| --- | --- | --- |
| Alfabeto reduzido | O builder recusava três configurações antes de calcular. | O alfabeto `01` permite calcular `01` e `09` para a base sintética `1`, conforme o mapeamento. O último DV pode ficar fora do alfabeto. Um DV intermediário incompatível lança `IllegalStateException`. A base fora do domínio continua lançando `InvalidBaseException`. |
| Algoritmo do consumidor anterior ao PR #12 | A regra propagava a `IllegalArgumentException` que sinalizava a base recusada. | A regra retorna `INVALID_FORMAT`. DV divergente continua retornando `INVALID_CHECK_DIGITS`, e uma falha de configuração sinalizada com `IllegalStateException` se propaga. |

Os quatro cenários novos falharam pelas razões acima. Após a correção, os 27 testes focados passaram. As assinaturas públicas permanecem iguais, e `InvalidBaseException` fica disponível para novos algoritmos.

O Claude Sonnet aprovou com ajustes. Foram acrescentados o registro de RED/GREEN, as tarefas da rodada, a distinção entre erro de configuração e rejeição de entrada no modelo e o custo da compatibilidade no plano. Uma `IllegalArgumentException` que sinalize um defeito do algoritmo também vira `INVALID_FORMAT`. O contrato recomenda `IllegalStateException` para distinguir esse caso.

Na base `73af5d0`, o `clean verify` passou em Corretto 8u512 e 21.0.13. Em cada JDK foram 20.992 testes, com 20.776 no core, 165 no Kotlin e 51 nos exemplos, sem falhas, erros ou pulos. Os dois JARs de cada JDK passaram na checagem de bytecode Java 8 e no japicmp contra a `v0.2.0` e a base `d0164a3`, com critérios de fonte e binário. O PIT matou 1.143 de 1.243 mutantes (92%). Os relatórios locais ficam em `.tmp/spec010-release/`.
