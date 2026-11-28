---
description: "Tarefas de implementação da conferência histórica de CTPS"
---

# Tarefas · Spec 014 · CTPS histórica

**Branch** `feature/014-ctps-historica` · **Base** `b4ad5ec` (`develop`), com a Spec 010 integrada.

**Entradas** [spec](spec.md), [plano](plan.md), [pesquisa](research.md), [modelo](data-model.md), [contrato](contracts/api.md) e [guia de verificação](quickstart.md).

**TDD** Em cada ciclo RED → GREEN, registrar em `specs/014-ctps-historica/analysis.md` o comando, a falha esperada, o resultado após a menor mudança e a refatoração quando houver. Suítes que caracterizam uma API existente ou testam seu empacotamento registram o resultado observado. `[P]` indica tarefas com arquivos distintos e pré-requisitos já satisfeitos. Serializar as execuções Maven que compartilhem o mesmo diretório `target`.

## Fase 1 · Preparação

- [X] T001 Confirmar worktree, `JAVA_HOME` dos JDKs 8 e 21 e disponibilidade do Wrapper em `mvnw.cmd`; registrar o estado da base e a suíte de regressão de partida e o caminho escolhido para o ZIP local da NT 18 (`CTPS_XSD_ZIP`) em `specs/014-ctps-historica/analysis.md`.

## Fase 2 · Base de testes

- [X] T002 [P] Criar os XMLs UTF-8 `01.xml` a `11.xml` e `cases.tsv` em `brasil4j-core/src/test/resources/ctps/esocial-2.5/` a partir dos [11 vetores](quickstart.md#fragmentos-compartilhados-do-s-2200-v25); guardar expectativas independentes de desfecho, campos afetados e textos dos registros conformes, e manter origem/perda como dados fornecidos no teste. Representar os espaços do vetor 05 por referências XML `&#x20;` e escape `\u0020` no catálogo, com comprimentos esperados 2 e 1. Ler o TSV preservando colunas vazias, inclusive as finais. Acrescentar as bordas de extração do guia em `extra-*.xml`, com entradas no catálogo fora da contagem SC-001.
- [X] T003 [P] Incluir `ctps/esocial-2.5/*.xml` e `ctps/esocial-2.5/cases.tsv` no `testResources` de `brasil4j-kotlin/pom.xml`, preservando os recursos existentes e mantendo os novos somente no classpath de teste.
- [X] T004 Conferir o SHA-256 do ZIP local não versionado indicado por `CTPS_XSD_ZIP` antes de isolar o tipo `TCtps` original e comparar offline os 11 vetores BMP de T002, conforme [D1](research.md#d1--autoridade-e-limites-do-perfil-histórico); registrar o caminho local do ZIP, hash conferido, comando, JDK, 5 conformes ao XSD (incluindo o grupo omitido) e 6 não conformes em `specs/014-ctps-historica/analysis.md`. Para Unicode suplementar, registrar em separado a divergência observada no JAXP e usar a expectativa normativa de [D4](research.md#d4--comprimento-unicode-e-verificação).

## Fase 3 · US1 · Conferir o grupo legado (P1)

**Objetivo** Distinguir estados de preenchimento e recuperar os textos aceitos. **Teste independente** Ambos os JARs classificam os 11 fragmentos em 1 ausente, 1 incompleto, 5 fora do perfil e 4 conformes, com os campos afetados e os textos previstos para cada ID.

- [X] T005 [US1] RED em `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/CtpsProfileTest.java`: ausência, grupo presente com três nulos, campos nulos em combinações distintas, campo nulo com outro inválido, ordem e imutabilidade dos campos afetados em `INCOMPLETE`, perfil explícito e origem nula na fábrica, com mensagem fixa e sem entrada.
- [X] T006 [US1] GREEN para ausência e precedência de incompletude em `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsProfile.java`, `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsGroup.java`, `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsOrigin.java` e `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsCheckResult.java`; aplicar as definições de `CtpsProfile.ESOCIAL_S2200_V2_5`, `CtpsGroup.number`, `CtpsGroup.series`, `CtpsGroup.uf`, `CtpsGroup.origin`, `CtpsGroup.sourceLoss`, constantes de `CtpsOrigin` e `CtpsCheckResult.profile`, `CtpsCheckResult.status` e `CtpsCheckResult.affectedFields` no [modelo de dados](data-model.md) e sua [precedência](data-model.md#decisão-e-precedência). Os campos de `CtpsGroup` são estado interno. A API pública desse tipo segue as assinaturas do [contrato](contracts/api.md).
- [X] T007 [US1] RED em `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/CtpsFacetsTest.java`: limites de número e série, textos vazios ou só com espaços, as 27 UFs exatas e tolerâncias rejeitadas, ordem e imutabilidade dos campos afetados em `OUT_OF_PROFILE`, número só com `U+1F600` abaixo do mínimo, suplementares nos limites inclusivos e acima do máximo dos dois campos, sequência com marca combinante e recuperação literal do registro.
- [X] T008 [US1] GREEN para campos fornecidos em `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsProfile.java`, `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsRecord.java` e `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsCheckResult.java`; aplicar as definições de `CtpsRecord.number`, `CtpsRecord.series`, `CtpsRecord.uf` e `CtpsCheckResult.status`, `CtpsCheckResult.affectedFields`, `CtpsCheckResult.record` no [modelo de dados](data-model.md) e a [precedência](data-model.md#decisão-e-precedência). Reusar `Uf` e contar pontos de código.
- [X] T009 [US1] Implementar o consumidor Failsafe dos 11 XMLs e do catálogo em `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/CtpsJarIT.java`; extrair filhos diretos pelo namespace sem trim, desabilitar DTD e acesso externo, distinguir elemento omitido de vazio, testar também as bordas de extração compartilhadas e confirmar pelo `codeSource` o JAR do core gerado pelo build. Comparar preservação às expectativas do catálogo, inclusive nos quatro conformes.
- [X] T010 [US1] RED em `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/CtpsTest.kt` para `CtpsGroup?`, perfil explícito, os quatro desfechos e acesso por `recordOrNull`, cobrindo a fronteira da adaptação.
- [X] T011 [US1] GREEN em `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Ctps.kt` para `checkCtps` e `recordOrNull`, delegando a conferência ao perfil Java e convertendo apenas `CtpsCheckResult.record` conforme [modelo](data-model.md) e [contrato Kotlin](contracts/api.md#kotlin--iogithubbrasil4jkotlinidentidade).
- [X] T012 [US1] Exercitar os mesmos 11 XMLs, recursos e expectativas em `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/CtpsJarIT.kt`; ler também as mesmas bordas de extração de T009, desabilitar DTD e acesso externo no parser de teste, verificar textos conformes, estados e campos afetados e confirmar pelo `codeSource` os JARs Java e Kotlin gerados pelo build.

**Checkpoint interno da US1** Fechar T012 com os dois consumidores dos JARs verdes antes de escrever ou executar os testes RED da US2.

## Fase 4 · US2 · Preservar proveniência e distinguir o fluxo digital (P2)

**Objetivo** Conservar origem e perda, expor UF de expedição no caso previsto e manter o CPF como o identificador do fluxo digital. **Teste independente** A mesma tripla percorre as três origens com os desfechos de expedição distintos. Um campo proveniente de armazenamento numérico preserva o texto disponível e a indicação de perda. Ensaios do CPF exercitam ambos os modos explícitos.

- [X] T013 [US2] RED em `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/CtpsProvenanceTest.java`: os três valores de origem sobre a mesma tripla, perda declarada sem preenchimento, origem/perda em incompleto e fora do perfil, ausência de metadados em `ABSENT` e UF de expedição somente em registro físico conforme.
- [X] T014 [US2] GREEN de proveniência em `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsGroup.java`, `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsProfile.java` e `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsCheckResult.java`; completar `CtpsGroup.origin`, `CtpsGroup.sourceLoss`, `CtpsCheckResult.origin`, `CtpsCheckResult.sourceLoss` conforme [modelo](data-model.md) e `CtpsCheckResult.issuingUf` conforme a [precedência do modelo](data-model.md#decisão-e-precedência). Manter os campos do grupo internos. Registrar T013 verde e verificar a regressão da US1 antes de T015.
- [X] T015 [US2] RED em `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/CtpsPrivacyTest.java`: fixar os marcadores de grupo e registro e verificar positivamente o diagnóstico estrutural do resultado nos quatro estados. Afirmar ausência de sentinelas distintas de número, série e UF sem colisão com os rótulos do diagnóstico. O acesso explícito ao registro ocorre pelos getters de `CtpsCheckResult` e `CtpsRecord`.
- [X] T016 [US2] GREEN de privacidade em `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsGroup.java`, `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsRecord.java` e `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/CtpsCheckResult.java`; aplicar os [limites de representação](data-model.md#limites-de-representação) aos marcadores e diagnósticos dos quatro desfechos. Registrar T015 verde e a regressão de proveniência antes de T017.
- [X] T017 [US2] Ampliar `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/CtpsJarIT.java` com as três origens, migração com perda, ocultação e casos de CPF digital `28001238938` e `28001238939` pelos modos `FORMAT_ONLY` e `NUMERIC` de `Cpf.parser`.
- [X] T018 [US2] RED em `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/CtpsProvenanceTest.kt` para `originOrNull`, `issuingUfOrNull`, propagação da perda e interpolação segura dos quatro estados.
- [X] T019 [US2] GREEN em `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Ctps.kt` para `originOrNull` e `issuingUfOrNull`, adaptando `CtpsCheckResult.origin` e `CtpsCheckResult.issuingUf` do [modelo](data-model.md) e do [contrato](contracts/api.md), com paridade dos diagnósticos e nenhuma transformação dos dados.
- [X] T020 [US2] Ampliar `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/CtpsJarIT.kt` com as três origens, perda, interpolação segura e `toCpfOrNull(mode)` para os mesmos vetores digitais de T017. Verificar UF, metadados e as classes de CPF e extensões pelos JARs.

## Fase 5 · Acabamento e gates

- [X] T021 Conferir JavaDoc, KDoc, [contrato](contracts/api.md) e [quickstart](quickstart.md) contra a implementação antes dos builds finais. Atualizar `docs/fontes.md` com autoridade, leiaute, revisão, alcance, SHA-256 e ligação aos casos sintéticos. Registrar separadamente o perfil histórico e o identificador digital referenciado no [recorte](../../docs/descoberta/ctps/recorte.md).
- [X] T022 Executar `mvnw.cmd -B -ntp clean verify` em JDK 8 e 21 e `scripts/check-bytecode.py` após cada build; registrar em `specs/014-ctps-historica/analysis.md` a execução das duas `CtpsJarIT` com contagens dos relatórios Failsafe e os 11 IDs em cada linguagem, o SHA-256 dos dois JARs por JDK, regressões 006/007/010, JaCoCo ≥90% linhas e ≥85% branches por módulo, execução dos pacotes da 014 nos relatórios, bytecode 52 e acionamento de DocLint nos dois JDKs e Dokka no JDK 21.
- [X] T023 Reconstruir `v0.2.0` e `a95efda` em JDK 21 e em árvores separadas, comparar os JARs Java e Kotlin com os JARs do candidato verificados no JDK 21 em T022 usando `scripts/check-api-compatibility.py` incluindo membros sintéticos e registrar os quatro pares (dois artefatos × duas bases) e o resultado binário e de fonte em `specs/014-ctps-historica/analysis.md`.
- [X] T024 Consolidar a evidência dos gates na matriz FR-001 a FR-006 e SC-001 a SC-004 em `specs/014-ctps-historica/analysis.md` e fechar a verificação local.

## Dependências e ordem

- T001 → T002/T003 em paralelo. T004 depende de T002. A US1 começa com os três itens de base concluídos.
- T005 → T006 → T007 → T008 → T009 → T010 → T011 → T012. T012 também depende dos recursos de T002/T003 e encerra a US1 com os 11 casos verdes pelos dois JARs.
- T012 → T013 → T014 para proveniência. Só abrir o RED de privacidade em T015 depois de T014 verde. Concluir T016 antes do consumo Java de T017. T017 → T018 → T019 → T020, com o RED Kotlin depois do consumo Java.
- T020 → T021 → T022 → T023 → T024. A revisão documental precede os builds finais. A comparação usa o candidato Java 21 verificado em T022. T024 fecha a matriz de entrega.
- **Checkpoint interno** T012 comprova a US1 e libera o início da US2. A entrega é elegível após proveniência, privacidade, ensaios de CPF digital e aprovação de T021 a T024, incluindo os gates em Java 8 e 21 e as duas bases de compatibilidade.
