# Tasks: Composição de validação e formatação de CPF, CNPJ e NIS

**Base** `5fd9dae6f4059b5e25528c5717d10c7c2f0aa922` · **Data** 2026-11-27

**Entradas** [Spec](spec.md), [plano](plan.md), [pesquisa](research.md), [modelo](data-model.md), [contrato](contracts/api.md) e [guia](quickstart.md).

## Formato e convenções

Cada tarefa usa `- [ ] Tnnn [P?] [USn?]`. `[P]` permite autoria paralela em arquivos distintos após as dependências indicadas. Caminhos de código e de relatórios são relativos à raiz do reator. Cada par RED/GREEN agrupa cenários da mesma etapa. Quando houver mais de um comportamento novo, fazer um ciclo por comportamento e parametrizar as variantes da mesma regra. Registrar comando, falha esperada, correção mínima e decisão de refatoração em `specs/007-composicao-identidade/analysis.md` durante a execução futura. Depois de cada GREEN, rever a mudança com a suíte relevante verde antes de iniciar outro ciclo. Casos satisfeitos por um ciclo anterior são registrados como regressões verdes.

## Fase 1 · Preparação

- [X] T001 Confirmar a spec ativa, a base Git, as referências de `v0.2.0` e da Spec 006, as instalações de JDK 8 e 21 e o Wrapper conforme `specs/007-composicao-identidade/quickstart.md`. Registrar o ponto de partida em `specs/007-composicao-identidade/analysis.md`.

## Fase 2 · Pré-requisitos comuns

- [X] T002 Executar `clean verify` na base em JDK 8 e 21 em série, registrar versões e contagens de Surefire e Failsafe dos dois módulos em `specs/007-composicao-identidade/analysis.md` e confirmar o acesso ao catálogo `brasil4j-core/src/test/resources/nis/nis-c1.csv` nos dois módulos pelo padrão de recursos em `brasil4j-kotlin/pom.xml`.

## Fase 3 · US1 · Conferir uma lista de campos de identidade (P1, MVP)

**Teste independente** Percorrer CPF, NIS e CNPJ nessa ordem, com o mesmo texto para CPF e NIS. Verificar a associação tipada, a ordem e a apresentação opcional. Em outra lista, usar um NIS mascarado como rejeição nativa entre dois itens aceitos. O aceite completo da lista com rejeição pela regra adicional é verificado na US2.

### TDD · Lista tipada e validação nativa

- [X] T003 [US1] RED em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckTest.java`. Cobrir `List<DocumentCheck<?>>` sem casts com CPF, NIS e CNPJ, listas vazias, CPF/NIS com `01234567898`, NIS mascarado entre dois aceitos e aceitação nativa de NIS repetido. Testar `NullPointerException` na montagem para modo nulo de CPF e de CNPJ. Demonstrar CPF nos dois modos e CNPJ nos três modos, incluindo CNPJ numérico e alfanumérico, máscaras completas aceitas e regressões de máscara parcial e minúsculas.
- [X] T004 [US1] GREEN em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheck.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheckResult.java`. Vincular as três fábricas existentes. Conferir por inspeção o único ponto de chamada e a ausência de pré-validação ou reparse. Com modo explícito e não nulo, as fábricas nativas já lançam `IllegalArgumentException` quando rejeitam entrada, inclusive texto nulo. Converter essa falha em `REJECTED_BY_VALIDATION` apenas no trecho da chamada nativa. Validar modo nulo na montagem, conforme o [contrato](contracts/api.md#sequência-e-falhas). Aplicar [Item de conferência.texto literal e Item de conferência.modo](data-model.md#item-de-conferência) e [Resultado.estado e Resultado.saída opcional](data-model.md#transições-por-avaliação).

### TDD · Formatação do valor no modo escolhido

- [X] T005 [US1] RED em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckTest.java`. Para `12ABC34501DE36`, exigir rejeição em `ALPHANUMERIC` e saída `12.ABC.345/01DE-36` em `FORMAT_ONLY` com `Cnpj::applyMask`. Conferir `Cpf::applyMask`, chamada única de apresentação e distinção entre ausência, saída vazia e saída com espaços.
- [X] T006 [US1] GREEN em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheck.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheckResult.java`. Aplicar [Item de conferência.função de apresentação opcional](data-model.md#item-de-conferência) sobre o valor produzido pela fábrica e devolver [Resultado.saída opcional](data-model.md#transições-por-avaliação) sem reprocessar o texto.

## Fase 4 · US2 · Compor a validação do NIS com uma regra do consumidor (P2)

**Teste independente** Passar o mesmo `Predicate<Nis>` ao `NisValidator` e a um item de NIS. A regra de importação com prefixo `00` aceita `00000000019` e rejeita `01234567898` nos dois usos. Os vetores da UEM são um exercício adicional de integração por regra sintética.

### TDD · Política reutilizável e curto-circuito

- [X] T007 [US2] RED em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckPolicyTest.java`. Reusar a regra de prefixo no validador direto e na lista. Cobrir duas políticas sobre a mesma inscrição, omissão da política e predicados também para CPF e CNPJ. Na lista da US1 com o mesmo `01234567898` como CPF e NIS, verificar `ACCEPTED`, `REJECTED_BY_POLICY`, `ACCEPTED`. Exigir zero chamadas ao formatador e saída ausente se a política recusar. Com rejeição nativa, exigir zero chamadas aos dois callbacks e saída ausente. Verificar ordem e identidade da instância em política e formatação com `assertSame`.
- [X] T008 [US2] GREEN em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheck.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheckResult.java`. Aplicar [Item de conferência.política opcional](data-model.md#item-de-conferência) tipada sobre o valor construído, antes da apresentação. Usar [Resultado.estado](data-model.md#transições-por-avaliação) de rejeição por política e manter os callbacks fora da captura da fábrica. Preservar o validador direto e suas assinaturas.
- [X] T009 [US2] Acrescentar a regressão parametrizada em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckPolicyTest.java` usando `brasil4j-core/src/test/resources/nis/nis-c1.csv`. Com predicado sintético, conferir 15 e 17 aceitos com zeros intactos, 09 rejeitado pela política com zero chamadas ao formatador e 02 rejeitado pela forma com zero chamadas a ambos. Manter o predicado de teste independente do código e do checkout da UEM.

## Fase 5 · US3 · Distinguir rejeições, falhas e diagnósticos (P3)

**Teste independente** Reavaliar o mesmo item, injetar falhas nas duas funções e conferir que os diagnósticos omitem os valores para os três documentos. Verificar que resultados anteriores continuam acessíveis ao consumidor que os coletou.

### TDD · Configuração e reexecução

- [X] T010 [US3] RED em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckFailureTest.java`. Conferir `NullPointerException` na montagem apenas ao fornecer política ou formatador nulos. Conferir modo e assinaturas das fábricas na API pública, com modo obrigatório para CPF/CNPJ e somente texto para NIS. Testar texto nulo, vazio e só espaços com zero callbacks e `REJECTED_BY_VALIDATION`, e saída nula com `NullPointerException` na avaliação. Conferir substituição independente de callback, item de origem intacto e quatro chamadas em duas avaliações que aceitam, na ordem política/apresentação e na thread de quem chama. Reavaliar o mesmo item com predicado alternado para obter aceitação e depois rejeição, comprovando recálculo da decisão.
- [X] T011 [US3] GREEN em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheck.java`. Aplicar os limites de [Item de conferência.texto literal, Item de conferência.modo, Item de conferência.política opcional e Item de conferência.função de apresentação opcional](data-model.md#item-de-conferência). Conferir `NullPointerException` nos pontos do contrato e preservar a imutabilidade e a configuração da etapa não substituída.

### TDD · Propagação e privacidade

- [X] T012 [US3] RED de privacidade em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckFailureTest.java`. Conferir `toString()` do item e do resultado e mensagens da biblioteca para CPF, CNPJ e NIS, com texto bruto, forma canônica e saída sintética identificável. Injetar `IllegalArgumentException` em cada callback e comparar a exceção original por identidade como regressão da captura restrita de T008. Testar a coleta anterior pelo consumidor e preservar representação e igualdade legadas de CPF, CNPJ e NIS.
- [X] T013 [US3] GREEN em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheck.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheckResult.java`. Manter propagação e proteção diagnóstica de [Resultado.estado e Resultado.saída opcional](data-model.md#transições-por-avaliação) e do item. Ajustar somente comportamentos ainda não cobertos pelo GREEN anterior.

## Fase 6 · Consumo pelos JARs, documentação e verificação de entrega

- [X] T014 [P] Escrever `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/DocumentCheckJarIT.java`. Verificar `codeSource` no JAR do núcleo para `DocumentCheck` e `DocumentCheckResult`. Consumir a lista da US1/US2 sem casts, `FORMAT_ONLY` com DV divergente, saída opcional e C1 02/09/15/17 sem dependência da UEM. Usar o mesmo `Predicate<Nis>` em `NisValidator.isValid(input, policy)` e na lista. Conferir que item e resultado aceito com texto formatado não o expõem em `toString()`.
- [X] T015 [P] Escrever `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/DocumentCheckJarIT.kt`. Repetir os cenários Java com `List<DocumentCheck<*>>`, `String?`, SAM tipado e saída convertida em `String?`. Reusar a regra com `isValidNis(policy)` e um item da lista. Confirmar `codeSource` dos tipos novos no JAR do núcleo e da value class `io.github.brasil4j.kotlin.identidade.Nis` no JAR Kotlin, exercitando a adaptação existente.
- [X] T016 [P] Conferir Javadoc dos símbolos novos em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheck.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/DocumentCheckResult.java`. Registrar procedência dos vetores e política externa em `docs/fontes.md` e conferir os exemplos Java/Kotlin em `specs/007-composicao-identidade/quickstart.md`.
- [X] T017 Rodar `clean verify` e `scripts/check-bytecode.py` primeiro no JDK 8 e depois no JDK 21, com os JARs do JDK 21 em `target/` para T018. Guardar relatórios antes do próximo `clean` e confrontar suítes e contagens de Surefire e Failsafe por módulo e JDK com T002. Exigir zero falhas, erros e skips nos testes previstos, bytecode 52, DocLint nos dois JDKs, Dokka no JDK 21 pelo perfil existente e JaCoCo ≥90% linhas e ≥85% branches nos módulos com branches. Confirmar ambos os novos `DocumentCheckJarIT`, as regressões da 006 com fragmentos S-2200 e registrar a evidência em `specs/007-composicao-identidade/analysis.md`.
- [X] T018 Após T017, empacotar em JDK 21 as bases `v0.2.0^{commit}` (`8e94704689ac4a48b174a29ab264f0f5eba0164d`) e Spec 006 (`5fd9dae6f4059b5e25528c5717d10c7c2f0aa922`), cada uma em raiz temporária própria. Comparar os dois JARs de cada base com o candidato de JDK 21 por `scripts/check-api-compatibility.py` e japicmp 0.26.1 com SHA-256 do workflow. Exigir zero incompatibilidades binárias ou de fonte e revisar as adições da 007, com inclusão de símbolos sintéticos na comparação. Registrar os quatro resultados em `specs/007-composicao-identidade/analysis.md`, guardar os relatórios e remover as raízes temporárias criadas pela verificação.

## Dependências e ordem de execução

T001 → T002 → T003/T004 → T005/T006 → T007/T008 → T009 → T010/T011 → T012/T013 → T014/T015/T016 → T017 → T018. Cada par RED/GREEN é sequencial. O MVP termina em T006. A US2 estende o item da US1. A US3 aplica as garantias de configuração, reexecução e diagnóstico a esse mesmo fluxo. Os testes independentes permitem conferir cada incremento mesmo com essa dependência técnica.

### Oportunidades de trabalho paralelo

- US1 · Revisão em leitura dos vetores e dos modos pode acompanhar T003. Teste e implementação do ciclo compartilham a evidência RED e seguem em série.
- US2 · A conferência em leitura dos desfechos C1 pode acompanhar T007, mantendo a escrita dos testes e a execução de T007–T009 com um responsável.
- US3 · Um revisor pode conferir os contratos de exceção e privacidade enquanto o responsável escreve T010 ou T012. Os ciclos continuam sequenciais no mesmo arquivo.
- Depois de T013, T014 e T015 podem ser escritos em paralelo em módulos distintos, junto da documentação de T016. Antes de escrever em paralelo, registrar responsáveis e base. Executar o reator e os `clean verify` em série no mesmo checkout.

## Estratégia de implementação

Validar primeiro o MVP com lista mista e formatação no modo escolhido. Acrescentar a política reutilizável do consumidor em um ciclo próprio. Completar cenários de falha e diagnósticos antes dos testes pelos JARs e das verificações de plataforma e compatibilidade. A entrega preserva as APIs existentes e mantém CTPS e publicação no Central fora do escopo.

## Rastreabilidade

| Recorte | Tarefas |
| --- | --- |
| US1 · FR-001/002/004/005 · SC-001/002 | T003–T006, T014–T015 |
| US2 · FR-003/004/005/009 · SC-001/003/004 | T007–T009, T014–T015 |
| US3 · FR-006/007/008 · SC-004/005 | T010–T015 |
| FR-010 · SC-006 | T002, T014–T018 |

## Revisão das tarefas · 2026-11-27

Claude e Agy revisaram a mesma versão do documento em leitura. SHA-256 do pacote enviado `D49254542688374C7417A5A2B7B7A198CE666B4FA954CA55A4AF711AC6268845`.

| Parecer | Consolidação |
| --- | --- |
| Claude · Aprovável com ressalvas | T004 e T010 distinguem texto nulo de configuração nula. A inspeção do fluxo em T004 cobre a chamada única da fábrica, e a identidade do valor entre callbacks fica em T007. T014/T015 verificam a mesma política direta e composta pelos JARs. T017 fixa JDK 8 antes de JDK 21 e compara os relatórios com a base. |
| Agy · Aprovado com ressalvas | T007 antecipa o curto-circuito e a ausência de saída para o RED. T009 mantém os vetores C1 como regressão. T010 testa reavaliação com mudança de decisão. T012 distingue regressão de propagação da verificação de privacidade dos dois tipos. T018 fixa o aceite das quatro comparações. |

As fábricas atuais de CPF, CNPJ e NIS confirmam que entrada nula é rejeição de texto quando o modo obrigatório é válido. Os resultados dos ciclos e das verificações estão em [analysis.md](analysis.md).
