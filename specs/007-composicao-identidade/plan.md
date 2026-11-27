# Implementation Plan: Composição de validação e formatação de CPF, CNPJ e NIS

- **Identificador Spec Kit** `007-composicao-identidade`
- **Data** 2026-11-27
- **Entrada** [Spec 007](spec.md)
- **Base Git** `main` em `5fd9dae6f4059b5e25528c5717d10c7c2f0aa922`.

## Summary

Adicionar ao núcleo um item de conferência tipado `DocumentCheck<T>` que associa entrada, regra nativa, política adicional e apresentação. Itens de CPF, CNPJ e NIS podem conviver na mesma lista e produzir uma decisão comum com saída opcional. CPF e CNPJ exigem modo explícito. NIS aplica a forma da Spec 006 e aceita a mesma interface de política que o seu validador direto já oferece aos consumidores. O DV da UEM exemplifica uma regra fornecida pelo consumidor.

## Technical Context

| Aspecto | Decisão |
| --- | --- |
| Plataforma | Java 8 como runtime mínimo, APIs Java 8 e bytecode 52. Kotlin 2.3.10 com target 1.8 e `-Xjdk-release=8`. |
| Dependências | Reator Maven Wrapper 3.9.14. Núcleo usa `Predicate`, `Function` e `Optional` do JDK. O artefato Kotlin consome o núcleo por SAM. |
| Operação e dados | Conferência local e síncrona. Uma construção por execução e no máximo uma chamada de cada função configurada. O consumidor mantém a lista, a ordem e os rótulos. |
| Testes | JUnit 5.12.2 por Surefire e Failsafe nos dois módulos. Vetores sintéticos, sem rede ou checkout externo. |
| Compatibilidade | API aditiva no núcleo. Consumidores Kotlin acessam as mesmas classes Java. Preservação verificada nos dois JARs contra `v0.2.0` e o commit da Spec 006. |

## Constitution Check

| Princípio | Antes da pesquisa | Após o desenho |
| --- | --- | --- |
| I · SDD | Spec com checklist completo. | Contrato, impacto e baselines definidos. `speckit-tasks` e análise de consistência antecedem a implementação. |
| II · DDD | Regras nativas pertencem aos tipos existentes. | Uma chamada à fábrica do tipo por execução. A composição define a sequência e recebe políticas de aceitação do consumidor. |
| III · TDD | Aceites observáveis em três histórias. | Ciclos separados para modos e lista, política, formatação, erros, privacidade e consumo pelos JARs. A execução futura registra falha esperada, correção mínima e refatoração. |
| IV · Plataforma | Gates do reator já existem. | JDK 8 e 21, bytecode 52, JaCoCo ≥90% linhas e ≥85% branches, DocLint nos dois JDKs, Dokka no JDK 21, japicmp nos dois artefatos. |
| V · Proveniência | Vetores CPF/CNPJ existentes e matriz C1 identificados. | Política da UEM representada por resultados sintéticos nos testes. A entrega registrará em `docs/fontes.md` a procedência dos vetores e da política externa. As novas representações omitem conteúdo dos documentos. |

O desenho segue os padrões de [JVM](../../.context/standards/jvm.md) e de [qualidade](../../.context/standards/qualidade.md). Nenhuma exceção à constituição foi identificada.

## Project Structure

### Documentation

- [Spec](spec.md) e [checklist de requisitos](checklists/requirements.md)
- [Pesquisa](research.md), [modelo](data-model.md), [contrato](contracts/api.md) e [guia de verificação](quickstart.md)

### Source Code · Destinos previstos

| Artefato | Caminho |
| --- | --- |
| API Java | `brasil4j-core/src/main/java/io/github/brasil4j/identidade/composicao/`, com `DocumentCheck` e `DocumentCheckResult`. |
| Testes Java | `brasil4j-core/src/test/java/io/github/brasil4j/identidade/composicao/`, com testes unitários do contrato e `DocumentCheckJarIT`. |
| Testes Kotlin | `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/`, com `DocumentCheckJarIT` consumindo a API Java. |
| Recursos | Reusar `brasil4j-core/src/test/resources/nis/nis-c1.csv`, já disponível nos dois módulos, e os vetores sintéticos de CPF/CNPJ dos testes atuais. |

## Phase 0 · Outline & Research

As [decisões D1–D4](research.md) registram o contrato funcional, o reaproveitamento da fábrica, a tipagem da lista e o método de verificação. A distinção entre uso direto do validador de NIS e uso da mesma política em uma lista fica explícita no contrato. A pesquisa de topologia e compatibilidade foi conferida contra os POMs, scripts e testes da base.

## Phase 1 · Design & Contracts

### API e fluxo

As fábricas Java `DocumentCheck.cpf`, `cnpj` e `nis` vinculam as regras existentes. A montagem com `withPolicy(Predicate<? super T>)` e `withFormatter(Function<? super T, String>)` devolve itens imutáveis. `evaluate()` devolve `DocumentCheckResult` com três estados e texto opcional. Um `Predicate<Nis>` fornecido por um consumidor também pode ser usado diretamente no `NisValidator`. O consumo Kotlin usa a mesma API Java com `List<DocumentCheck<*>>` e lambdas SAM. [Assinaturas e semântica](contracts/api.md) · [Transições](data-model.md)

A fronteira de captura de `IllegalArgumentException` cobre exclusivamente a chamada à fábrica nativa, depois de validar o modo obrigatório na montagem. A avaliação não usa o validador booleano antes da fábrica. A política e o formatador executam fora dessa captura. `Cnpj::applyMask` trabalha com o valor que passou no modo selecionado, inclusive quando esse modo é `FORMAT_ONLY` e o DV informado diverge.

### Testes e rastreabilidade

| Camada | Estratégia | Rastreabilidade |
| --- | --- | --- |
| Surefire Java · lista e modos | Cobrir os dois modos CPF, os três modos CNPJ, as formas mascaradas aceitas pelo contrato original e vetor alfanumérico de DV divergente em `FORMAT_ONLY`. Conferir lista vazia, tipo declarado para entradas idênticas e decisões em ordem. | US1 · FR-001/002 · SC-001/002 |
| Surefire Java · políticas e saída | Parametrizar vetores C1 02, 09, 15 e 17 com política sintética baseada nesses vetores. Conferir também uma regra do importador reutilizada no validador direto de NIS e na lista, política opcional em CPF/CNPJ, duas políticas sobre o mesmo NIS, instância idêntica entre callbacks, zeros, ordem, contagens e ausência de callback após rejeição. Diferenciar saída ausente, vazia e com espaços. | US1/US2 · FR-003/004/005/009 · SC-002/003/004 |
| Surefire Java · falhas e privacidade | Cobrir reexecução na mesma thread, imutabilidade das configurações, configuração nula, ausência de entrada, saída nula e exceções originais nos dois callbacks, incluindo `IllegalArgumentException`. Conferir que entrada, canônico e saída não aparecem no `toString()` dos dois tipos nem nas mensagens da biblioteca. | US3 · FR-006/007/008 · SC-004/005 |
| Failsafe Java e Kotlin · consumo | Os dois `DocumentCheckJarIT` constroem listas mistas sem casts, exercitam tipos inferidos e modos explícitos, o CNPJ formatado por `FORMAT_ONLY`, os quatro casos do C1 e a saída opcional. Em cada teste, confirmar `codeSource` dos novos tipos no JAR do núcleo. O teste Kotlin inclui consumo de um tipo ou extensão da Spec 006 a partir do JAR Kotlin além da nova API. | US1/US2 · SC-001/002/003/006 |
| Regressões e gates | Rodar as suítes existentes, inclusive as da 006 com fragmentos S-2200. Conferir documentação, bytecode, cobertura e comparações dos artefatos. | FR-010 · SC-006 |

Os testes distinguem a contagem por avaliação da reexecução de um mesmo item. A revisão do fluxo verifica que a fábrica é chamada em um único ponto. A integração Kotlin demonstra `String?` na entrada, SAM com tipos Java concretos e conversão explícita da saída opcional. Os testes negativos de nulidade percorrem a fronteira Java. [Guia com exemplos e comandos](quickstart.md)

### Compatibilidade

| Base | Commit | Cobertura |
| --- | --- | --- |
| `v0.2.0` | `8e94704689ac4a48b174a29ab264f0f5eba0164d` | Contratos de CPF, CNPJ e demais APIs anteriores à Spec 006. |
| Spec 006 | `5fd9dae6f4059b5e25528c5717d10c7c2f0aa922` | Inclui NIS, seus suportes Java e as assinaturas Kotlin, inclusive sintéticas. |

Reconstruir as duas baselines em diretórios separados e comparar com o candidato empacotado no mesmo JDK 21. O script existente usa japicmp 0.26.1 e falha em incompatibilidades binárias ou de fonte. Conferir também os relatórios para validar as adições no núcleo e a preservação da API Kotlin. Os testes Java/Kotlin do reator complementam a comparação de símbolos com comportamento. A matriz atual do Verify em JDK 8 e 21 e o gate histórico contra v0.1.0 continuam como evidência de CI. As duas comparações acima fazem parte da verificação de entrega da 007.
