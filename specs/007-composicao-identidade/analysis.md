# Análise e evidências · Spec 007

- **Data** 2026-11-27
- **Base de implementação** `5fd9dae6f4059b5e25528c5717d10c7c2f0aa922` (Spec 006)
- **Base anterior** `v0.2.0^{commit}` em `8e94704689ac4a48b174a29ab264f0f5eba0164d`

## Consistência antes da implementação

A análise cruzou FR-001 a FR-010 e SC-001 a SC-006 com as 18 tarefas. Os 16 itens do checklist de requisitos estavam aprovados. A única correção exigida foi antecipar o teste de modo nulo de CPF e CNPJ para o RED de T003, antes da implementação em T004. T010 preserva os testes de funções nulas na montagem, texto inválido na avaliação e resultado nulo do formatador.

O núcleo reutiliza as fábricas nativas. A política opcional usa o mesmo contrato `Predicate` do validador direto de NIS e é exercitada com regras do consumidor e exemplos sintéticos do C1. O DV da UEM é uma referência de integração. A validação de JARs e as duas comparações de API permanecem nas tarefas T017 e T018.

## Linha de base · T001–T002

O `clean verify` da Spec 006 foi executado em uma cópia isolada de `5fd9dae`, no Corretto 8u512 e depois no Corretto 21.0.13. As duas execuções tiveram 435 testes, sem falhas, erros ou testes pulados. O núcleo executou 238 no Surefire e 76 no Failsafe. O Kotlin executou 45 no Surefire e 76 no Failsafe. O recurso `nis/nis-c1.csv` já é compartilhado pelo reator nos testes Kotlin. Foram usados o Wrapper 3.9.14 e o cache Maven local em modo offline.

## Ciclos de TDD

Os ciclos do núcleo foram executados com o Wrapper, `-pl brasil4j-core` e seleção das classes `DocumentCheck*Test`. Os logs por tarefa ficaram em `.tmp/spec007/`.

| Ciclo | RED | GREEN e revisão |
| --- | --- | --- |
| T003–T004 · Lista, modos e montagem | O teste, já com modo nulo de CPF e CNPJ, não compilou na ausência de `DocumentCheck` e `DocumentCheckResult`. | Cinco testes passaram. As fábricas usam o modo já validado e a captura de rejeição cobre apenas uma construção nativa. |
| T005–T006 · Formatação | A compilação indicou ausência de `withFormatter`. | Sete testes passaram. O formatador usa a instância criada no modo escolhido. `Optional` distingue saída ausente de saída vazia. |
| T007–T008 · Política do consumidor | A compilação indicou ausência de `withPolicy` e do estado `REJECTED_BY_POLICY`. | Onze testes passaram. O mesmo `Predicate<Nis>` funciona na conferência direta e na lista. A política e o formatador ficam fora da captura da fábrica e usam a mesma instância. |
| T009 · Vetores C1 | Regressão sobre a política implementada. | Os quatro vetores entraram na suíte. Quinze testes passaram e os callbacks receberam o texto canônico somente após a aceitação da forma. |
| T010–T011 · Configuração e reexecução | Duas falhas observadas. Funções nulas foram aceitas na montagem e um formatador configurado retornou nulo sem erro. | Vinte e quatro testes passaram após introduzir as guardas de nulidade fora da captura da fábrica. Cópias de configuração e reexecução passaram como regressões do desenho imutável. |
| T012–T013 · Propagação e privacidade | Os casos novos já passaram na primeira execução. | Vinte e oito testes passaram. A captura ao redor da fábrica preserva a exceção original do callback. Os dois tipos mantêm `Object.toString()` e identidade padrão, sem derivar texto de seus campos. As mensagens de nulidade são constantes. |

Em uma cópia isolada, remover separadamente cada guarda de modo nulo fez falhar o `assertThrows` correspondente de CPF e de CNPJ. Expor deliberadamente o texto no item ou a saída no resultado fez falhar os três casos de privacidade. As mutações foram revertidas depois de cada prova. Isso confirmou a capacidade dos testes de detectar as regressões sem deixar mudanças no candidato.

## Verificação dos artefatos · T014–T017

| Execução | Surefire Java | Failsafe Java | Surefire Kotlin | Failsafe Kotlin | Total |
| --- | ---: | ---: | ---: | ---: | ---: |
| Base Spec 006, em cada JDK | 238 | 76 | 45 | 76 | 435 |
| Candidato, Corretto 8u512 | 266 | 83 | 45 | 83 | 477 |
| Candidato, Corretto 21.0.13 | 266 | 83 | 45 | 83 | 477 |

As duas execuções do candidato passaram com zero falhas, erros ou testes pulados. Cada suíte existente manteve sua contagem. Foram acrescentados 28 testes unitários Java e sete testes de consumo por JAR em cada linguagem. As suítes de NIS e os fragmentos S-2200 da Spec 006 continuaram verdes. Os testes confrontaram `codeSource` e os relatórios do Failsafe mostraram os JARs do reator em `target/` no classpath. Os dois artefatos passaram em bytecode 52. DocLint passou nos dois JDKs e Dokka no JDK 21.

JaCoCo registrou 100% de linhas nos dois módulos e 112 de 112 branches no núcleo, em ambos os JDKs. As classes novas tiveram 33 de 33 linhas e seis de seis branches cobertos. A evidência da verificação está em `.tmp/spec007/verify-8.log`, `verify-21.log`, `evidence-8.txt` e `evidence-21.txt`.

## Compatibilidade de API · T018

As duas bases foram empacotadas em diretórios separados no Corretto 21.0.13 usado no candidato. O script `scripts/check-api-compatibility.py` executou japicmp 0.26.1 com SHA-256 conferido, inclusão de membros sintéticos e falha em incompatibilidade binária ou de fonte.

| Base | Núcleo | Kotlin |
| --- | --- | --- |
| `v0.2.0` (`8e94704`) | Compatível. Adições da 006 e da 007. | Compatível. Adições da 006. |
| Spec 006 (`5fd9dae`) | Compatível. Adições limitadas a `DocumentCheck`, `DocumentCheckResult` e `DocumentCheckResult.Status`. | Compatível. Comparação sem diferenças. |

Os relatórios estão em `.tmp/spec007/baseline-v020-api.log` e `baseline006-api.log`. As cópias temporárias das baselines foram removidas após a verificação.

## Revisão da implementação pré-commit · 2026-11-27

Claude e Agy revisaram em leitura os mesmos sete arquivos novos, o contrato público e o resumo da verificação inicial. SHA-256 do pacote enviado `F66C0B7707B7D419B096EDCDA5433E65EE16A5498582BF36320E9DFB89E286C9`. Ambos aprovaram a implementação sem bloqueios. A revisão documental confirmou uma chamada à fábrica nativa por avaliação, a mesma instância na política e no formatador, o curto-circuito e a propagação intacta das falhas do consumidor.

| Parecer | Consolidação |
| --- | --- |
| Claude | `CodeSource` agora é comparado com o caminho real do único JAR principal do módulo em `target/`, desconsiderando JARs de fontes, documentação e testes. Foi incluída a origem das classes da validação direta, incluindo a extensão compilada `NisKt`. Os testes também conferem o tipo genérico de cada fábrica, a preservação do modo ao adicionar uma política, saída nula no Kotlin e identidade do valor nos dois callbacks. |
| Agy | A cobertura passou a incluir CPF aceito em `NUMERIC`, política Kotlin via conversão SAM com uma saída vazia presente, zero chamadas de formatação após erro de política e mensagens sem identificação após modo nulo. Os leitores de teste dos dois JARs delimitam o campo pela primeira aspa de fechamento após o prefixo. |

O `catch` de `IllegalArgumentException` permanece restrito à chamada das fábricas privadas selecionadas pelo tipo. As fábricas existentes usam essa exceção para rejeição de entrada. A representação padrão dos tipos de valor existentes permanece compatível com a Spec 006. Os vetores C1 02, 09, 15 e 17 foram conferidos no recurso de testes do reator. Os ajustes desta rodada ficaram nos testes e nas evidências, com a implementação de produção preservada.

### Verificação após os pareceres

`clean verify` passou com 480 testes em cada JDK, sem falhas, erros ou testes pulados. O núcleo executou 267 no Surefire e 83 no Failsafe. O módulo Kotlin executou 45 no Surefire e 85 no Failsafe. Dentro dessas contagens, os `DocumentCheckJarIT` executaram sete casos Java e nove Kotlin. Bytecode 52 passou nos dois artefatos. JaCoCo registrou 100% de linhas nos dois módulos e 112/112 branches no núcleo. DocLint passou nos dois JDKs e Dokka no JDK 21.

O caso já existente de saída ausente, vazia e com espaços foi movido para `DocumentCheckResultTest`, junto da API que testa. Os 29 testes unitários da composição passaram novamente nos dois JDKs após essa movimentação. A revisão estática não encontrou placeholders ou testes desativados. As quatro comparações japicmp foram repetidas com `v0.2.0` e `5fd9dae` e passaram em compatibilidade binária e de fonte. Os logs estão em `.tmp/spec007/review-verify-*.log`, `review-result-*.log` e `review-base-*-api.log`.
