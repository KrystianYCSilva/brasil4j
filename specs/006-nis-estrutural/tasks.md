---
description: "Tarefas da Spec 006 de conferência estrutural de NIS, PIS, Pasep e NIT"
---

# Tasks · Conferência estrutural de NIS, PIS, Pasep e NIT

- **Base** `v0.2.0` em `8e94704689ac4a48b174a29ab264f0f5eba0164d`.
- **Entradas** [Spec](spec.md), [plano](plan.md), [modelo](data-model.md), [contrato](contracts/api.md), [pesquisa](research.md) e [quickstart](quickstart.md).

Cada ciclo começa pelo teste e pelo registro da falha esperada. Depois da menor mudança que o faça passar, rode a suíte pertinente e registre o estado após a refatoração. Os marcadores `[P]` indicam tarefas com arquivos próprios e pré-requisitos satisfeitos. Use IDs de vetores nas evidências.

## Fase 1 · Preparação

- [x] T001 Concluir a análise de consistência de `specs/006-nis-estrutural/spec.md`, `specs/006-nis-estrutural/plan.md` e `specs/006-nis-estrutural/tasks.md`. Registrar pareceres e decisões em `specs/006-nis-estrutural/analysis.md` e conferir a base Git e a spec ativa em `.specify/feature.json` antes da implementação.

## Fase 2 · Recursos de teste

- [X] T002 [P] Criar `brasil4j-core/src/test/resources/nis/nis-c1.csv`, copiando fielmente os 22 vetores sintéticos da matriz C1 e classificando-os por SC-002, com um vetor adicional de 12 dígitos. Acrescentar vetores sintéticos de 11 caracteres com `/` e `:` em posição de dígito para testar as fronteiras do intervalo ASCII. Preservar os IDs de origem, distinguir nulo de vazio e preservar espaços, máscara e caracteres Unicode.
- [X] T003 [P] Criar os fragmentos UTF-8 em `brasil4j-core/src/test/resources/nis/esocial-2.5/`, com namespace e caminho de `nisTrab` descritos no plano. Incluir os quatro vetores de SC-001, DV divergente, máscara completa, espaços externos, quebra de linha e tabulação dentro do campo, conteúdo só de espaços, elemento omitido e as duas formas de elemento vazio. Registrar nos recursos a origem no leiaute v2.5 consolidado até NT 19/2020 e a procedência sintética dos valores. Citar [`Campo de origem.texto extraído`](data-model.md) para a distinção entre ausência, vazio e texto literal.
- [X] T004 [P] Declarar em `brasil4j-kotlin/pom.xml` a inclusão de `nis/nis-c1.csv` e `nis/esocial-2.5/*.xml` do núcleo como recursos de teste, seguindo as inclusões explícitas de CNPJ.

## Fase 3 · US1 · Conferir o campo de um arquivo histórico (P1)

**Teste independente** Extrair os quatro exemplos de SC-001 do S-2200 sintético. Nos dois JARs, preservar os canônicos e rejeitar texto curto e com letra. Conferir também o catálogo C1 e igualdade pelo texto.

- [X] T005 [US1] Em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisTest.java`, iniciar o ciclo red de gramática com o catálogo C1 e os vetores adicionais de T002. Conferir rejeição com `IllegalArgumentException` e mensagem constante e aceitação por forma de repetidos e casos divergentes de DV (FR-001, FR-003, SC-002).
- [X] T006 [US1] Fechar o ciclo de T005 em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/Nis.java` com `parse(String)` e o armazenamento mínimo da [`Nis Java.string canônica`](data-model.md). Aplicar a falha constante do contrato, escrever JavaDoc da classe e da fábrica e registrar green e refactor (FR-001, FR-003, SC-002).
- [X] T007 [US1] Executar o ciclo de preservação explícita em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisTest.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/Nis.java`. Depois de observar red, expor `getCanonicalValue()` para a [`Nis Java.string canônica`](data-model.md) e conferir todos os aceitos, inclusive zeros iniciais (FR-002, SC-001, SC-002).
- [X] T008 [US1] Executar o ciclo de igualdade e hash em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisTest.java` e `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/Nis.java`. Cobrir mesmo texto extraído de campos com rótulos distintos, textos diferentes, nulo, string e outro tipo. Após red, implementar igualdade e hash consistentes (FR-004, SC-003).
- [X] T009 [US1] Executar ciclos separados de paridade em `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/NisTest.kt` e `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Nis.kt`. Começar pelo construtor com todos os vetores de T002 e a mesma falha Java para ausentes, antes de introduzir o [`Nis Kotlin.delegado Java imutável`](data-model.md). Em ciclos próprios, testar primeiro o acesso canônico, a extensão para entradas não nulas e a conversão `toJava()` com igualdade de valor Java. Cobrir igualdade/hash Kotlin sob `Any` e distinguir os tipos Java e Kotlin antes da conversão. Registrar igualdade gerada pela value class como comportamento herdado se já passar e escrever KDoc ao introduzir as assinaturas (FR-001 a FR-004, SC-002, SC-003).
- [X] T010 [P] [US1] Em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisJarIT.java`, escrever a sondagem Java para os fragmentos de T003 com checagem de origem em JAR. Extrair o [`Campo de origem.texto extraído`](data-model.md) em UTF-8 com leitor sensível a namespace, DTD e entidades externas desabilitadas, preservando o texto. Verificar a tabela do quickstart no ciclo do reator de T016 (FR-001 a FR-003, SC-001).
- [X] T011 [P] [US1] Em `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/NisJarIT.kt`, escrever a sondagem Kotlin para os mesmos fragmentos de T003 e as restrições de leitura do plano. Conferir o [`Campo de origem.texto extraído`](data-model.md) pelo construtor Kotlin, também pela extensão quando o campo estiver presente, e verificar origem em JAR da value class e do núcleo no ciclo do reator de T016 (FR-001 a FR-003, SC-001).

## Fase 4 · US2 · Proteger a inscrição em diagnósticos (P2)

**Teste independente** Comparar a representação padrão de dois valores canônicos distintos e verificar que a falha de apresentação não ecoa o campo. A recuperação explícita continua integral.

- [X] T012 [US2] Iniciar o ciclo red de ocultação em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisTest.java` e `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/NisTest.kt`. Conferir o marcador exato `[NIS oculto]` para dois canônicos distintos em Java e, em Kotlin, na representação direta, na interpolação e sob `Any`. O erro constante permanece coberto como regressão pelos testes T005 e T009 (FR-005, SC-004).
- [X] T013 [US2] Fechar o ciclo de T012 em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/Nis.java` e `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Nis.kt`. Retornar o marcador fixo pelo núcleo e delegar na value class. Registrar green e refactor, mantendo a recuperação explícita e as verificações de erro (FR-005, SC-004).
- [X] T014 [US2] Estender `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisJarIT.java` e `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/NisJarIT.kt` para conferir marcador fixo e erro sem eco nem causa em todos os fragmentos inválidos, inclusive os casos históricos de SC-001. Comparar com a recuperação explícita dos aceitos. Verificar a sensibilidade da asserção de privacidade com uma sonda temporária limitada às fixtures sintéticas, como substituir o texto padrão pela recuperação canônica (FR-002, FR-005, SC-004).

## Fase 5 · Documentação e verificação

- [X] T015 [P] Completar JavaDoc em `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/Nis.java` e KDoc em `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Nis.kt` antes do build de verificação. Atualizar `docs/fontes.md`, `docs/arquitetura.md`, `README.md` e `CHANGELOG.md` com proveniência, exemplos, limites de adoção e adição de API. Sustentar o formato pelos leiautes do plano e explicitar o alcance da fórmula Caixa.
- [X] T016 Rodar `mvnw.cmd -B -ntp clean verify` na raiz do reator, sem paralelismo Maven, primeiro em JDK 8 e depois em JDK 21. Manter a ordem core → Kotlin, com Failsafe após o empacotamento de cada módulo. Conferir bytecode com `scripts/check-bytecode.py` e registrar data ISO 8601, contagens Surefire/Failsafe, JaCoCo ≥90% linhas e ≥85% branches, bytecode 52, Enforcer, DocLint em ambos e Dokka em JDK 21 em `specs/006-nis-estrutural/analysis.md`.
- [X] T017 Reconstruir em JDK 21 os JARs de `v0.2.0` pelo commit da base, em diretório temporário separado do checkout. Comparar core e Kotlin candidatos no mesmo JDK com `scripts/check-api-compatibility.py`. Verificar o SHA-256 de japicmp 0.26.1 definido em `.github/workflows/verify.yml`. Registrar data ISO 8601 e resultado binário e de fonte dos dois módulos, incluindo membros sintéticos, em `specs/006-nis-estrutural/analysis.md`. Manter a etapa existente do CI com referência a v0.1.0 e descartar a reconstrução temporária depois de registrar a evidência.

## Dependências e ordem de execução

- T001 → T002/T003/T004 → T005 → T006 → T007 → T008 → T009.
- Após T009, a escrita de T010 e T011 pode ocorrer em paralelo. Os ensaios Failsafe executam na ordem do reator após o empacotamento. T011 aplica a mesma regra de extração do plano sem depender de código do teste Java.
- T010/T011 → T012 → T013 → T014. T015 pode ocorrer junto de T014 após T013, em arquivos próprios. T014/T015 → T016 → T017.
- Os ciclos T005–T009 e T012–T013 precisam observar a falha antes da mudança de produto correspondente. Para verificações pelos JARs que já passem na primeira execução, registre esse resultado e confirme a sensibilidade com uma sonda temporária nos testes. Use remoção de espaços ou perda de zeros para T010/T011 e exposição canônica para T014. Retire a sonda e execute novamente.

## Exemplos de trabalho em paralelo

- Na preparação, uma frente prepara os vetores, outra os fragmentos e outra as inclusões de recursos, cada qual em seus arquivos.
- Na US1, após a API e os recursos estarem disponíveis, compor a sondagem Java e a Kotlin em paralelo.
- Na US2, os ciclos principais são sequenciais porque usam o mesmo contrato e arquivos da US1. A atualização documental de T015 pode acompanhar T014.

## Estratégia de implementação

Validar primeiro o percurso da US1, da extração do campo sintético do S-2200 até a conferência pelos JARs. Completar a ocultação da US2 na mesma entrega. A conclusão exige o contrato integral e as verificações de plataforma e compatibilidade. Extração XML de produção, conversão numérica, máscara de entrada, DV, cadastro e CTPS ficam fora do incremento.

## Aditamento pré-commit · US3

- [X] T018 [US3] Cobrir a consulta estrutural e os retornos e limites do callback com TDD em `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/NisTest.java`. Implementar `NisValidator` no mesmo pacote de `Nis.java` e manter uma única conferência de gramática (FR-006, SC-005).
- [X] T019 [US3] Cobrir formatação explícita, entradas e retorno nulos, exceções e ocultação padrão com TDD em `NisTest.java`. Implementar `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/NisFormatter.java` como ponto de uso de uma função do consumidor (FR-007, SC-005).
- [X] T020 [US3] Cobrir primeiro a ergonomia pública em `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/NisTest.kt`. Expô-la por delegação em `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Nis.kt` (FR-006, FR-007).
- [X] T021 Exercitar os contratos adicionais pelos JARs em `NisJarIT.java` e `NisJarIT.kt`. Fortalecer os casos de nulo literal, leitura do Unicode, igualdade, conversão Kotlin e representação sob `Any`. Atualizar `docs/requisitos.md` e os exemplos de composição em `specs/006-nis-estrutural/quickstart.md`.
- [X] T022 Revalidar T016 e T017 e registrar as evidências do código pós-revisão em `specs/006-nis-estrutural/analysis.md`.

Executar T018/T019 antes de T020, T021 e T022. O aditamento usa as interfaces funcionais existentes do JDK. O algoritmo da UEM continua com o consumidor e as APIs de CPF e CNPJ preservam seus contratos.
