# Pesquisa técnica · Spec 007

**Data** 2026-11-27 · **Base** `5fd9dae6f4059b5e25528c5717d10c7c2f0aa922`.

> **Superada em parte (2026-11-27).** As alternativas rejeitadas em D1 (contratos próprios) e D3 (superinterface nos valores) passam a ser o desenho adotado pela Spec 010 (`specs/010-contratos-documentos`). Essa mudança decorre da [constituição v0.5.0](../../.specify/memory/constitution.md#engenharia-jvm). D2 e D4 seguem válidas como contexto histórico.

## D1 · Contratos funcionais para consumidores

- **Decisão** Usar `Predicate<? super T>` para compor a validação nativa com uma regra do consumidor e `Function<? super T, String>` para escolher a apresentação. Para NIS, `Predicate<? super Nis>` é a mesma interface na validação direta da Spec 006 e na operação em lista. Regras sobre o valor canônico podem ser compartilhadas pelo integrador e combinadas com `Predicate.and`, respeitando sua ordem de avaliação.
- **Motivo** A mesma função tipada pode ser usada isoladamente e em itens que exigem validação e saída associadas. O conteúdo da regra é do consumidor. A política de DV da UEM é um exemplo já verificado em isolamento. [Descoberta](../../docs/descoberta/007-composicao-identidade/recorte.md) · [Contrato de NIS na Spec 006](../006-nis-estrutural/contracts/api.md#validação-e-formatação-explícitas)
- **Alternativas** Interfaces `Validator<T>` e `Formatter<T>` próprias repetiriam as assinaturas do JDK. Listas paralelas de funções perderiam a associação entre decisão e apresentação. Uma função vinculada por item resolve a associação, mas transfere a cada integrador a sequência, a privacidade e a classificação do resultado.

## D2 · Usar a fábrica do tipo uma vez

- **Decisão** As três fábricas de item vinculam internamente `Cpf.parse(input, mode)`, `Cnpj.parse(input, mode)` ou `Nis.parse(input)`. Modo nulo é recusado na montagem. Somente a chamada à fábrica nativa fica no limite que converte `IllegalArgumentException` de entrada inválida em rejeição. Os callbacks executam fora desse limite.
- **Motivo** As fábricas atuais conferem o modo e constroem o valor na mesma operação. O teste existente de CNPJ demonstra formatação do valor em `FORMAT_ONLY` com DV divergente. No CPF, `parse(String)` já delega a `FORMAT_ONLY` e aceita o vetor sintético de DV divergente. A nova camada reaproveita essas regras sem promover auxiliares internos a API pública. [Fábrica CPF](../../brasil4j-core/src/main/java/io/github/brasil4j/identidade/cpf/Cpf.java#L35) · [Fábrica CNPJ](../../brasil4j-core/src/main/java/io/github/brasil4j/identidade/cnpj/Cnpj.java#L35) · [Teste CNPJ](../../brasil4j-core/src/test/java/io/github/brasil4j/identidade/cnpj/CnpjTest.java#L60)
- **Alternativas** Executar o validador booleano e depois a fábrica conferiria o texto duas vezes. Usar o formatador estático que recebe texto reaplicaria o modo padrão. Oferecer uma fábrica genérica com parser do consumidor ampliaria a superfície e impediria distinguir a rejeição nativa de uma falha arbitrária de parser.

## D3 · Item tipado e resultado comum para Java e Kotlin

- **Decisão** `DocumentCheck<T>` imutável, com fábricas tipadas e um `DocumentCheckResult` comum. Três estados de decisão, com `Optional<String>` para a saída. Cada método de configuração devolve um novo item, substitui a configuração anterior da mesma etapa e permite reutilizar o item de origem. A lista fica com o consumidor.
- **Motivo** A referência heterogênea pode usar `<?>` em Java e `<*>` em Kotlin. A chamada de avaliação segue utilizável porque o resultado independe de `T`. Kotlin consome a API Java com SAM e recebe os tipos Java concretos. `Optional` distingue saída ausente de string vazia e mantém o acesso explícito.
- **Alternativas** Superinterface nos valores, retorno do valor como `Object`, rótulo interno e método de lote não atendem a uma necessidade adicional do recorte. Fábricas Kotlin duplicadas acrescentariam uma segunda superfície e exigiriam adaptar os callbacks para value classes em posição genérica. [Pareceres](../../docs/descoberta/007-composicao-identidade/revisao.md#conferências-e-decisões)

## D4 · Verificação sobre os artefatos empacotados

- **Decisão** Reutilizar Surefire, Failsafe, Maven Wrapper e japicmp. Rodar os testes em JDK 8 e 21. Para compatibilidade, reconstruir as duas baselines e o candidato em JDK 21 e comparar cada par de JARs em diretórios isolados, mesmo se os POMs usam a mesma versão.
- **Motivo** A tag `v0.2.0` cobre a API publicada no Git. O commit `5fd9dae` cobre adicionalmente os tipos e extensões de NIS da Spec 006. O script atual compara core e Kotlin, inclui símbolos sintéticos e usa core e stdlib separados conforme a versão de cada lado. Testes de consumo continuam necessários para observar comportamento e uso em Kotlin. [Script](../../scripts/check-api-compatibility.py) · [Verify atual](../../.github/workflows/verify.yml)
- **Alternativas** Comparar só com `v0.2.0` não detectaria regressões da API de NIS. Usar apenas testes sobre diretórios de classes não exercitaria o empacotamento. Trocar a infraestrutura de verificação acrescentaria trabalho sem mudar a evidência exigida.
