# Análise de consistência · Spec 006

**Data** 2026-11-27. **Base Git** `v0.2.0` em `8e94704689ac4a48b174a29ab264f0f5eba0164d`.

O Claude e o Antigravity receberam o mesmo pacote de spec, plano, modelo, contrato, pesquisa, quickstart, tarefas e instruções do projeto. O SHA-256 do pacote comum é `B8D2627A2A139A2F67455C33CD31E328D7959E2D00129F158C9DAFB0DC9F0E37`. O `tasks.md` submetido tinha SHA-256 `193C1EF5DBDF3BF1BE7F7FF8FA89FCC5A9BF64D401229297F5F33AAB2C202C40`.

## Rastreabilidade conferida

| Requisito e aceite | Tarefas | Evidência planejada |
| --- | --- | --- |
| FR-001, FR-002, FR-003 · SC-001 e SC-002 | T002–T007, T009–T011 | Catálogo C1 e caso de 12 dígitos, leitura do campo S-2200 e validação pelo JAR nas duas linguagens. |
| FR-004 · SC-003 | T008, T009 | Igualdade por texto, hash e comparação entre linguagens por conversão explícita. |
| FR-005 · SC-004 | T005, T009, T012–T014 | Falha de formato constante desde o primeiro ciclo, marcador em diagnósticos e acesso explícito ao texto. |
| Constituição e compatibilidade | T001, T015–T017 | Proveniência, evidência red–green–refactor, validação em JDK 8 e 21, bytecode 52, cobertura, documentação e comparação binária e de fonte dos dois módulos. |

## Claude

**Pronto com ajustes menores.** O parecer confirmou a cobertura dos requisitos e pediu separar o TDD de paridade, tratar a mensagem de erro já existente como regressão e alinhar o teste de `toJava()` à igualdade do contrato. Também pediu explicitar comparação entre tipos, rastreabilidade dos recursos e registro de evidência em data ISO 8601. As tarefas foram ajustadas nesses pontos. A comparação com v0.2.0 permanece na verificação planejada e a etapa que já compara com v0.1.0 segue no CI, conforme a decisão do plano.

O pacote de revisão referenciava a matriz e o POM Kotlin sem incorporar seus conteúdos. Na checagem local, confirmei que os casos 07 e 17 são, respectivamente, `00000000000` e `00000000060`. A classificação na spec confere com a gramática para todos os 22 vetores. O POM Kotlin já declara Failsafe e o reator tem precedentes de testes Kotlin `*JarIT` com verificação de origem em JAR. As referências dos recursos são registradas em T002/T003 e a publicação documental da proveniência em `docs/fontes.md` pertence a T015.

## Antigravity

**Pronto com ressalvas.** O parecer pediu delimitar a exposição JVM do tipo Kotlin, separar escrita paralela de execução do Failsafe, explicitar JavaDoc/KDoc antes do build e ampliar os ensaios de borda e privacidade. Também pediu fixar o JDK da reconstrução de v0.2.0 e o destino temporário da baseline.

- O contrato Kotlin explicita o construtor primário privado e a classe JVM da extensão de topo. A checagem de membros sintéticos continua em T017.
- T010/T011 podem ser escritos em paralelo. T016 roda o Failsafe na ordem do reator, após o empacotamento de cada módulo. As tarefas registram a documentação da API antes do full build.
- T002 cobre os caracteres adjacentes à faixa ASCII. T003 e o quickstart complementam as formas vazias e com whitespace. T014 cobre também a privacidade nas rejeições de SC-001, e usa uma sonda que torna observável a exposição do texto canônico. Isso exercita FR-005 sem alterar a gramática aprovada.
- T017 fixa JDK 21 para reconstrução e comparação e o uso de diretório temporário dedicado. Japicmp 0.26.1 é fixado pelo workflow existente. O script seleciona as versões pelo POM e inclui as regras de compatibilidade binária e de fonte para os JARs dos dois módulos.

O pacote não incorporava o workflow, o script, os leiautes originais nem os testes existentes do CPF. Conferi localmente a tag da baseline, as opções do script e o precedente de Failsafe dos dois módulos. A sugestão opcional de ampliar o CI para v0.2.0 fica fora desta rodada, conforme o plano.

## Decisão

Os ajustes documentais foram incorporados. Spec, plano e tarefas foram considerados consistentes para iniciar a implementação após a autorização de Krystian em 2026-11-27.

## Evidência de TDD · 2026-11-27

Os ciclos de desenvolvimento usaram Corretto 21.0.13 e Maven Wrapper 3.9.14. Os 22 registros C1 do catálogo de teste foram comparados ao texto da matriz, incluindo nulo, vazio, espaços e Unicode. Os três registros adicionais cobrem comprimento 12 e as duas fronteiras do intervalo ASCII.

| Tarefa | Red | Green e refactor |
| --- | --- | --- |
| T005–T006 · Gramática Java | A compilação do teste apontou ausência de `Nis`. | 25 casos passaram após introduzir `parse` e o texto imutável. |
| T007 · Texto canônico Java | A compilação apontou ausência de `getCanonicalValue()`. | Os 25 casos passaram, com comparação integral nos aceitos. |
| T008 · Igualdade e hash Java | O teste acusou desigualdade de duas instâncias com o mesmo texto. | Os 26 casos passaram com igualdade e hash pelo texto canônico. |
| T009 · Construtor Kotlin | A compilação apontou referência não resolvida a `Nis`. | Os 25 vetores passaram com validação delegada, incluindo nulo e vazio. |
| T009 · Texto canônico Kotlin | A compilação apontou referência não resolvida a `canonicalValue`. | Os 25 casos passaram com recuperação explícita nos aceitos. |
| T009 · Extensão Kotlin | A compilação apontou referência não resolvida a `toNis()`. | Os 25 casos passaram, com os mesmos retornos e falhas aplicáveis à extensão. |
| T009 · Conversão e igualdade Kotlin | A compilação apontou referência não resolvida a `toJava()`. | Os 26 casos passaram. A igualdade e o hash sob `Any` usaram o comportamento gerado da value class. A conversão explícita permitiu comparar com Java. |
| T012–T013 · Ocultação | Um teste em cada módulo falhou ao obter a representação padrão anterior à ocultação. | 27 testes em cada módulo passaram com o marcador fixo, incluindo interpolação e valor Kotlin sob `Any`. A mensagem de formato permaneceu coberta. O texto do erro passou a ter uma constante privada no núcleo. |

A resolução offline reaproveitou uma cópia local do cache de dependências usado na Spec 005. Após o bloqueio da gravação no diretório do daemon Kotlin, a reexecução isolou o primeiro red Kotlin. Os ciclos seguintes usaram `-Dkotlin.compiler.daemon=false`.

## Verificação da implementação · 2026-11-27

Em T010, T011 e T014, sondas temporárias exclusivamente nos testes substituíram a extração literal por trim e a representação padrão pelo acesso canônico. O Failsafe detectou seis falhas por módulo, três de texto alterado e três de exposição da inscrição. As sondas foram removidas antes das duas execuções completas.

| Execução | Java Surefire / Failsafe | Kotlin Surefire / Failsafe | Resultado |
| --- | --- | --- | --- |
| Corretto 8u512 | 233 / 75 | 42 / 75 | 425 testes, sem falhas, erros ou skips. |
| Corretto 21.0.13 | 233 / 75 | 42 / 75 | 425 testes, sem falhas, erros ou skips. |

Cada módulo rodou 27 testes unitários e 13 testes de integração da 006 em cada JDK. A integração usa 12 fragmentos sintéticos, verifica a origem das classes nos JARs e distingue elemento ausente, vazio e conteúdo com whitespace sem alterar o texto extraído. O resultado cobre o campo isolado do S-2200 v2.5.

O Wrapper executou `clean verify` offline com Enforcer, JaCoCo e DocLint nos dois JDKs, além de Dokka no JDK 21. Os dois artefatos têm bytecode 52 e contêm seus tipos `Nis`. Os recursos de teste ficaram fora dos JARs de runtime. JaCoCo registrou 100% de linhas em ambos os módulos e 98/98 branches no núcleo em ambos os JDKs. O tipo Java `Nis` cobriu 14/14 linhas e 14/14 branches. O módulo Kotlin não registrou branches no relatório.

## Compatibilidade com v0.2.0 · 2026-11-27

Os JARs de referência foram reconstruídos localmente com Corretto 21.0.13 a partir do commit `8e94704689ac4a48b174a29ab264f0f5eba0164d` apontado pela tag `v0.2.0`. O SHA-256 do japicmp 0.26.1 coincidiu com o valor fixado no workflow Verify.

O script de compatibilidade retornou sucesso para os dois módulos, com as verificações binária e de código-fonte e a inclusão de membros sintéticos. O relatório mostrou somente as novas classes `io.github.brasil4j.identidade.nis.Nis`, `io.github.brasil4j.kotlin.identidade.Nis` e `io.github.brasil4j.kotlin.identidade.NisKt`, com seus membros. A verificação do CI com v0.1.0 permanece configurada.

## Revisão da implementação pré-commit · 2026-11-27

Claude e Antigravity aprovaram a implementação estrutural que receberam, sem achados bloqueantes. Usaram o mesmo pacote, SHA-256 `7F8302E3D12A0F1B37678988CC4D47004F3C4CBFB4207C6C979E5C0A10147B5F`. Os pareceres foram documentais e se apoiaram nas evidências de execução acima.

| Parecer | Achado e decisão |
| --- | --- |
| Claude | O caso nulo do CSV podia passar mesmo se o literal fosse lido como texto. O dado Unicode também podia ser corrompido e continuar rejeitado. Acrescentar o teste de nulo literal e conferir o valor Unicode recebido. Exercitar `toJava()` e `Any` pelos JARs. |
| Antigravity | Cobrir `equals` e `hashCode` pelo JAR Java, e a conversão e o hash pelo JAR Kotlin. Sustentar a estrutura de entrada e a ocultação no contrato padrão. |
| Arquitetura | Ambos sugeriram avaliar as extensões em seguida e usar contratos Java 8 sobre um valor já conferido. Claude identificou valor imediato no booleano estrutural. O caso da UEM trazido pelo usuário justificou um aditamento da 006 com `NisValidator` e `NisFormatter` como pontos explícitos de composição. |

O aditamento preserva os requisitos estruturais. A política do consumidor recebe `Nis`, e o formatador trabalha somente sobre esse valor já construído. O resultado de apresentação é explícito e pode revelar a inscrição. O marcador padrão continua oculto. A UEM mantém a autoridade sobre a regra de DV que fornece. CPF e CNPJ continuam com suas APIs existentes. Um protocolo comum de validação em lote, com tratamento de ausência e diagnóstico por item, requer contrato próprio.

### TDD do aditamento

| Tarefa | Red observado | Green e refactor |
| --- | --- | --- |
| T018 | Teste Java falhou na compilação pela ausência de `NisValidator`. | 30 testes passaram. O objeto de valor manteve a gramática única para fábrica e consulta. |
| T019 | Teste Java falhou na compilação pela ausência de `NisFormatter`. | 32 testes passaram com apresentação explícita, contratos de nulo e propagação da falha externa. |
| T020 | Teste Kotlin falhou na compilação pelas referências não resolvidas a `isValidNis` e `format`. | 29 testes Kotlin passaram com delegação. A mesma execução passou os 32 testes Java. |
| T021 | Ensaio da representação, igualdade e composição pelos JARs acrescentado aos casos já existentes. | Cada módulo executou 14 testes de integração da spec. `toJava()`, igualdade/hash e a representação sob `Any` entraram no ensaio Kotlin. |

### Contraprova isolada com a UEM

Compilei `PisStrategy` e `ValidatorStrategy` de `uem-npd-zk-utils` em `9dd29d1` contra o JAR do núcleo, em diretório temporário. Sete verificações passaram em Corretto 8u512 e 21.0.13 com quatro vetores sintéticos do C1. O caso 17 passou pela política da UEM. O caso 09 passou por forma e foi rejeitado pela política. A máscara do caso 02 passou no validador legado e foi barrada pela gramática da biblioteca antes da composição. O formatador da UEM preservou os zeros iniciais do caso 15 e a saída padrão de `Nis` continuou oculta. A sondagem exercitou a composição sem incluir o código da UEM nos artefatos da biblioteca.

### Revisão curta do aditamento

Claude e Antigravity analisaram o código adicional em leitura a partir do pacote SHA-256 `52DE52C4CE3119DAF8B43496721152B5D81514C486DE96FD127B448DD637164F`. Ambos deram parecer sem bloqueadores. Acrescentei a verificação de que as duas funções Kotlin propagam a mesma exceção original, explicitei o resultado para entrada nula no KDoc e atualizei o texto que lista as extensões Kotlin. O caso de retorno nulo da formatação é exercitado diretamente em Java, onde a função pode devolvê-lo, e a adaptação Kotlin delega ao mesmo método. A observação do Claude sobre as datas da constituição é preexistente na v0.2.0 e pertence a uma revisão de governança.

### Verificação final do aditamento · 2026-11-27

`clean verify` passou com 435 testes em cada JDK, sem falhas, erros ou skips. Por execução foram 238 unitários e 76 pelos JARs no núcleo, e 45 unitários e 76 pelos JARs no Kotlin. Os 14 testes de integração de NIS em cada módulo exercitam os 12 fragmentos, a origem em JAR e a composição. Os dois JARs continuam com bytecode 52. JaCoCo registrou 100% de linhas em ambos os módulos e 106/106 branches no núcleo. DocLint passou nos dois JDKs e Dokka no JDK 21.

A comparação final com a baseline v0.2.0 reconstruída em JDK 21 passou nos dois módulos, com japicmp 0.26.1 e opções de compatibilidade de fonte, binária e de membros sintéticos. O relatório mostrou cinco adições de classes de API, incluindo `NisValidator`, `NisFormatter` e os tipos de NIS Java/Kotlin.

### Sequência na Spec 007

A Spec 006 entrega os pontos de extensão locais de NIS revisados acima. A Spec 007 será a etapa para definir os contratos e interfaces compartilhados de validação e formatação e aplicar composição a CPF, CNPJ e NIS. O desenho deverá avaliar operações em listas heterogêneas e políticas fornecidas pelo consumidor, como a regra de DV da UEM, preservando a compatibilidade das APIs Java e Kotlin. A CTPS terá especificação própria.
