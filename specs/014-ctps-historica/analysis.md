# Verificação da implementação · Spec 014

**Base** `7294c2b` · **Branch** `codex/014-ctps-historica` · **Data** 2026-11-28

## Preparação

- O seletor do Spec Kit resolveu a Spec 014 na worktree própria. O checklist de requisitos tem 16 itens atendidos.
- A base tem as mesmas fontes e POMs do candidato `93ca2e6`, que passou com 20.823 testes em Corretto 8u512 e 21.0.13. A conferência usou `git diff --exit-code 93ca2e6 HEAD -- 'brasil4j-*' pom.xml`.
- JDKs Corretto 8u512 e 21.0.13, Wrapper e cache local verificados. O ZIP para `CTPS_XSD_ZIP` fica no cache de auditoria `.tmp/audit008/esocial-xsd-v25-nt18.zip` da árvore principal, com SHA-256 `64329D891A2D072BA4731724D03089B7D74809A9007AD52650628F1A175DBB19`.

## Oráculo XSD

`CTPS_XSD_ZIP` apontou para o ZIP descrito na preparação e o hash foi conferido antes do uso. A execução local em Corretto 21.0.13 usou `java .tmp/ctps014/XsdCheck.java <zip> brasil4j-core/src/test/resources/ctps/esocial-2.5`. O verificador extraiu o `TCtps` original e validou o elemento `documentos` com grupo opcional. Confirmou 01 a 05 conformes ao XSD e 06 a 11 fora. Para os pontos suplementares, aplica-se a expectativa normativa da pesquisa D4, devido à divergência UTF-16 já observada no JAXP de ambos os JDKs.

## TDD e consumo da US1

| Ciclo | RED observado | GREEN e revisão |
| --- | --- | --- |
| T005–T006 · presença e incompletude | `-pl brasil4j-core -Dtest=CtpsProfileTest test` falhou por ausência dos tipos de CTPS. | Três testes verdes. Os campos ausentes passaram a ser agregados na ordem do leiaute. |
| T007–T008 · facetas e preservação | `-pl brasil4j-core -Dtest=CtpsFacetsTest test` falhou por ausência de `CtpsRecord` e `getRecord()`. | 54 testes verdes, incluindo a regressão da presença, 27 UFs e contagem Unicode com pontos suplementares. |
| T009 · JAR Java | A primeira rodada do consumidor passou, e o DocLint identificou getters sem descrição principal. | `-pl brasil4j-core -Dit.test=CtpsJarIT verify` passou com 17 casos Failsafe, incluindo os 11 da matriz, cinco extras e a origem do JAR. As descrições foram completadas. |
| T010–T011 · adaptação Kotlin | `-pl brasil4j-kotlin -am -Dtest=CtpsTest -Dsurefire.failIfNoSpecifiedTests=false test` falhou em `checkCtps` ausente. | Teste verde nos quatro desfechos, com conversão de `Optional` para nulável. |
| T012 · JAR Kotlin | Ensaio de consumo da US1. | 17 testes Failsafe por JDK, com os mesmos 11 fragmentos, cinco bordas e a origem dos JARs. |

### T012 · checkpoint interno fechado antes da US2

As duas cópias isoladas tinham o mesmo manifesto de fontes, com hash `0b77be39d6ee6a3636254d2f92c9ac8412a2ba652bc5c3a33e74f42d9cd6217f`. O Maven Wrapper executou `clean verify` em Corretto 8u512 e 21.0.13. Cada JDK passou com 20.912 testes, sem falhas, erros ou pulos. Cada `CtpsJarIT` executou 17 casos, incluindo os 11 IDs obrigatórios, cinco extras e a confirmação do JAR pelo `codeSource`. Os dois artefatos apresentaram bytecode 52 em cada JDK. Os relatórios `checkpoint-*` ficam em `.tmp/ctps014-builds/` da árvore principal. A US2 começou após essa conferência.

| JDK | Conclusão do checkpoint (BRT) | SHA-256 core | SHA-256 Kotlin |
| --- | --- | --- | --- |
| corretto-1.8.0_512 | 2026-11-28T10:52:24-03:00 | 3734BF6D89A6F433BB00543A80B66C488491B4B7A9F265E3D7C8348AFFAD2654 | 3D42844DB4361D85AB157DAA1F4CE1EE91029B91891A9A3A28603902EDB2C987 |
| corretto-21.0.13 | 2026-11-28T10:53:03-03:00 | 4BC852775D5457726FE3696EC14E85DAF6DE6209C48F1BEC7DF9B8EF3A44888B | DBB9609F03AEBEFC5B4369FC1E538582F07B62E522E86E5FC2040316E6DF5D80 |

## TDD e consumo da US2

| Ciclo | RED observado | GREEN e revisão |
| --- | --- | --- |
| T013–T014 · proveniência | `-pl brasil4j-core -Dtest=CtpsProvenanceTest test` falhou pela ausência de `getOrigin()`, `hasSourceLoss()` e `getIssuingUf()`. | Os seis casos e a regressão da US1 passaram, com 60 testes ao todo. O resultado retém só os metadados da origem, independentemente do desfecho. |
| T015–T016 · privacidade | Aberto depois de T014 verde. `-pl brasil4j-core -Dtest=CtpsPrivacyTest test` recebeu o texto padrão de `Object` em vez do marcador exigido. | Após os marcadores e o diagnóstico estrutural, passaram 61 testes, incluindo a regressão de proveniência. |
| T017 · JAR Java | O ensaio de consumo usa o contrato já implementado. | `-pl brasil4j-core -Dit.test=CtpsJarIT verify` passou com 19 casos Failsafe, incluindo proveniência, perda e CPF digital nos dois modos. |
| T018–T019 · adaptação Kotlin | `-pl brasil4j-kotlin -am -Dtest=CtpsProvenanceTest -Dsurefire.failIfNoSpecifiedTests=false test` falhou por ausência de `originOrNull` e `issuingUfOrNull`. | Os três testes Kotlin de estado, proveniência e privacidade passaram. As extensões apenas adaptam a nulabilidade do núcleo. |
| T020 · JAR Kotlin | Ensaio de consumo da US2. | 19 testes Failsafe por JDK, incluindo proveniência, perda, diagnósticos e CPF digital pelos JARs. |

## Gate final em Java 8 e 21

As duas execuções do Maven Wrapper usaram clean verify em cópias isoladas com o mesmo manifesto de fontes, de SHA-256 b20596fbd2d0e98889c6fc016fcfd201ff27385655bca744c82b45da8dc64f81. Os relatórios final-* ficam na área local ignorada .tmp/ctps014-builds/ da árvore principal.

- **corretto-1.8.0_512** 20.925 testes, sem falhas, erros ou pulos. Bytecode 52 confirmado. Cada CtpsJarIT executou 19 testes, com os 11 fragmentos obrigatórios, cinco extras e os ensaios de origem do artefato, proveniência e identificação digital.
  - brasil4j-core · 20725 testes · JaCoCo 98,84% linhas e 94,50% branches · CTPS BRANCH 30/30, LINE 75/75 · SHA-256 413A70F1D77F9D1E3955A638F14AF6A5D111C3AC55447BD9C2CCBF89E449111F.
  - brasil4j-kotlin · 163 testes · JaCoCo 99,33% linhas e 100,00% branches · CTPS LINE 4/4 · SHA-256 2901BF258068D740182CD13C36DC310D1AC139DFD0F44138563838136243919E.
  - brasil4j-exemplos · 37 testes nesta rodada, antes dos exemplos da Spec 014.
- **corretto-21.0.13** 20.925 testes, sem falhas, erros ou pulos. Bytecode 52 confirmado. Cada CtpsJarIT executou 19 testes, com os 11 fragmentos obrigatórios, cinco extras e os ensaios de origem do artefato, proveniência e identificação digital.
  - brasil4j-core · 20725 testes · JaCoCo 98,85% linhas e 94,50% branches · CTPS BRANCH 30/30, LINE 75/75 · SHA-256 31C7962EE8F1453E824A48FC1489630424EA17E6D2924056C4ABBDBA165EA283.
  - brasil4j-kotlin · 163 testes · JaCoCo 99,33% linhas e 100,00% branches · CTPS LINE 4/4 · SHA-256 3BF63C272AD60FB1C026C45318696E5F944BC80CE953A1BCF50B21694F27C41F.
  - brasil4j-exemplos · 37 testes nesta rodada, antes dos exemplos da Spec 014.

O gate também executou DocLint nos dois JDKs, Dokka no JDK 21 e as regressões das Specs 006, 007 e 010 no reator.

## Compatibilidade

- Baseline v0.2.0 reconstruída em Corretto 21.0.13. JARs Java e Kotlin aprovados contra o candidato do mesmo JDK, com verificação de compatibilidade binária e de fonte incluindo membros sintéticos.
- Baseline a95efda reconstruída em Corretto 21.0.13. JARs Java e Kotlin aprovados contra o candidato do mesmo JDK, com verificação de compatibilidade binária e de fonte incluindo membros sintéticos.

Comparações feitas por scripts/check-api-compatibility.py com japicmp 0.26.1 e hash conferido.


## Rastreabilidade do aceite

| Requisito ou critério | Evidência |
| --- | --- |
| FR-001 e SC-001 | CtpsProfileTest e as duas CtpsJarIT. Em cada JDK, 1 ausente, 1 incompleto, 5 fora do perfil e 4 conformes. |
| FR-002 | CtpsFacetsTest, com 27 UFs e limites inclusivos e exclusivos, inclusive pontos suplementares. Oráculo TCtps para os 11 casos BMP. |
| FR-003 e SC-002 | Os dois JARs recuperaram os valores literais nos quatro conformes. A matriz inclui zeros, letras, pontuação e espaços. |
| FR-004 e SC-003 | CtpsProvenanceTest em Java e Kotlin e os dois JARs. Três origens para a mesma tripla, perda descrita pelo consumidor e UF de expedição só para origem física conforme. |
| FR-005 e SC-004 | CtpsPrivacyTest e teste de interpolação Kotlin. Marcadores constantes e ausência de sentinelas de número, série e UF nos quatro diagnósticos. |
| FR-006 | Os JARs Java e Kotlin usaram a API existente de CPF nos modos FORMAT_ONLY e NUMERIC, com vetores sintéticos de DV válido e divergente. |

## Revisão pré-commit · Claude e Antigravity

O Claude e o Agy receberam, em leitura, o mesmo pacote de 40 arquivos, SHA-256 `9F5DA0E88F395033B34B173974734E7AEFF426AB63F26A7D513F27D13420B043`. Ambos deram parecer **aprovado com ajustes**. Os revisores confirmaram a aderência do núcleo ao contrato, incluindo precedência, contagem Unicode, preservação literal, proveniência e privacidade.

| Achado consolidado | Tratamento |
| --- | --- |
| Evolução de `Uf` poderia ampliar o perfil histórico em silêncio | As 27 siglas do XSD foram conferidas contra `Uf`. O conjunto exato passa a ter uma asserção independente, preservando o catálogo geográfico como fonte da API. |
| O catálogo comum podia perder casos complementares sem derrubar os dois JAR ITs | Os dois consumidores conferem o domínio das suítes e os IDs esperados. O caso extra de `documentos` sem filhos de elemento usa comentário e espaços para distinguir a extração do caso 01. |
| Prova de origem do JAR permissiva e privacidade parcial no consumo | Os testes confirmam arquivo regular, diretório de build e nome exato do artefato. O Failsafe injeta a versão do reator, comparada ao nome e ao metadado Maven do JAR carregado. Os diagnósticos dos quatro desfechos e a UF física esperada são verificados também pelos JARs. |
| Literais sensíveis à normalização do editor | Escapes explicitam os pontos de código. A série de um caractere suplementar ganhou um caso próprio. |
| Contagens e checkpoint pouco autossuficientes | T012 e T020 aparecem nos ciclos de consumo. O checkpoint original registra horário e hashes dos JARs, e os 37 testes do módulo vitrine reconciliam a rodada anterior. |

A vitrine ganhou exemplos executáveis em Java e Kotlin para importação física, proveniência e identificação da CTPS Digital via CPF.

### Revalidação da revisão em Java 8 e 21

As duas execuções do Maven Wrapper usaram clean verify em cópias isoladas com o mesmo manifesto de fontes, de SHA-256 e6cab6ec72580dc764d042128f874cda74c6f4995d8dacb318b0e39f1078adb7. Os relatórios review-gated-* ficam na área local ignorada .tmp/ctps014-builds/ da árvore principal.

- **corretto-1.8.0_512** 20933 testes, sem falhas, erros ou pulos. Bytecode 52 confirmado. Cada CtpsJarIT executou 19 testes, com os 11 fragmentos obrigatórios, cinco extras e os ensaios de origem do artefato, proveniência e identificação digital.
  - brasil4j-core · 20727 testes · JaCoCo 98,84% linhas e 94,50% branches · CTPS BRANCH 30/30, LINE 75/75 · SHA-256 47E4B006C656110EDDFF93AB80D3C5C7E375A138DB2B8C89356A6C28F81E337B.
  - brasil4j-kotlin · 163 testes · JaCoCo 99,33% linhas e 100,00% branches · CTPS LINE 4/4 · SHA-256 E923B9E184E6FAA9D3A752D72FCEBDDB25EC3CF8664F5F0F75ECABB23476C4B8.
  - brasil4j-exemplos · 43 testes, incluindo os novos exemplos de CTPS.
- **corretto-21.0.13** 20933 testes, sem falhas, erros ou pulos. Bytecode 52 confirmado. Cada CtpsJarIT executou 19 testes, com os 11 fragmentos obrigatórios, cinco extras e os ensaios de origem do artefato, proveniência e identificação digital.
  - brasil4j-core · 20727 testes · JaCoCo 98,85% linhas e 94,50% branches · CTPS BRANCH 30/30, LINE 75/75 · SHA-256 0E76395D78B39BD3019EC9ECD9B07D18E8D760270DAF3E27FEFB08CA4C6C3478.
  - brasil4j-kotlin · 163 testes · JaCoCo 99,33% linhas e 100,00% branches · CTPS LINE 4/4 · SHA-256 C27330D8C0130883ECF142D03E250B5A27E4DF1800A8C613FDA8F8EC37301C71.
  - brasil4j-exemplos · 43 testes, incluindo os novos exemplos de CTPS.

O gate também executou DocLint nos dois JDKs, Dokka no JDK 21 e as regressões das Specs 006, 007 e 010 no reator.

## Compatibilidade

- Baseline v0.2.0 reconstruída em Corretto 21.0.13. JARs Java e Kotlin aprovados contra o candidato do mesmo JDK, com verificação de compatibilidade binária e de fonte incluindo membros sintéticos.
- Baseline a95efda reconstruída em Corretto 21.0.13. JARs Java e Kotlin aprovados contra o candidato do mesmo JDK, com verificação de compatibilidade binária e de fonte incluindo membros sintéticos.

Comparações feitas por scripts/check-api-compatibility.py com japicmp 0.26.1 e hash conferido.


A checagem estática independente aprovou a comparação com a versão do reator. Os relatórios Surefire identificaram três exemplos Java e três Kotlin executados por JDK. O código da API é idêntico ao da rodada anterior.

## Integração com develop

A branch `feature/014-ctps-historica` parte de `b4ad5ec`, com a Spec 010 integrada. As evidências acima registram a rodada de revisão na base original. Para atender às fronteiras do núcleo na develop, o perfil histórico foi movido para `io.github.brasil4j.trabalhista.ctps`. O ArchUnit confere que o pacote usa apenas o JDK, seus próprios tipos e `Uf`. As extensões Kotlin preservam o pacote e declaram a visibilidade pública. A conferência dos fragmentos, a proveniência e o uso de CPF para identificação digital seguem o contrato revisado.
