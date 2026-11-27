# Implementation Plan: Conferência estrutural de NIS, PIS, Pasep e NIT

- **Identificador Spec Kit** `006-nis-estrutural`
- **Data** 2026-11-26
- **Entrada** [Spec 006](spec.md)
- **Base Git** `main` em `8e94704689ac4a48b174a29ab264f0f5eba0164d` (`v0.2.0`)

## Summary

Adicionar um valor imutável de inscrição estrutural em Java e uma adaptação Kotlin. A API recebe o campo textual extraído pelo consumidor, preserva os 11 dígitos ASCII e oferece representação padrão oculta. O primeiro cenário de integração usa fragmentos sintéticos do S-2200 v2.5 para provar, pelos JARs, a fronteira entre extração e conferência de forma.

## Technical Context

| Aspecto | Decisão |
| --- | --- |
| Linguagem e plataforma | Java 8 como runtime mínimo. Kotlin 2.3.10 com JVM target 1.8. Bytecode 52. |
| Módulos e dependências | Reator Maven existente. Core depende apenas do JDK. Kotlin depende do core e da stdlib. |
| Dados e volume | Uma string de 11 caracteres por valor. Conferência local, com inspeção limitada a 11 caracteres depois da checagem de comprimento. |
| Testes | JUnit 5.12.2 por Surefire, Failsafe para consumo pelos JARs, fixtures sintéticas compartilhadas como recursos de teste. |
| Fronteira | O consumidor extrai, escolhe o leiaute e decide o tratamento de ausência, DV e cadastro. O adaptador XML deste plano é exclusivamente de teste. |

## Constitution Check

| Princípio | Antes da pesquisa | Após o desenho |
| --- | --- | --- |
| I · SDD | Recorte aprovado em `spec.md`. | API aditiva, impacto para consumidores e baseline descritos. A derivação de tarefas e a análise de consistência são etapas anteriores ao código. |
| II · DDD | Regra estrutural em Identidade Brasileira. | Invariante no valor Java, com delegação Kotlin e dados de cadastro no consumidor. |
| III · TDD | Aceite observável em FR-001 a FR-005. | Ciclos de gramática, preservação, igualdade, privacidade e consumo pelos JARs definidos. Evidência red–green–refactor será produzida na implementação. |
| IV · Plataforma | Reutilizar as verificações do reator. | Planejados `clean verify` em JDK 8 e 21, bytecode 52, JaCoCo ≥90% linhas e ≥85% branches, DocLint nos dois JDKs, Dokka no JDK 21 e japicmp nos dois artefatos. |
| V · Proveniência | Matriz C1, leiautes e pareceres identificados. | Vetores sintéticos rastreados. Registrar autoridade, versão e evidência em `docs/fontes.md` junto à entrega. Forma avaliada antes de qualquer transformação e saída padrão sem dígitos. |

O desenho segue os padrões de [JVM](../../.context/standards/jvm.md) e [qualidade](../../.context/standards/qualidade.md). Nenhuma exceção à constituição foi identificada.

## Project Structure

### Documentation

- [Spec](spec.md) e [checklist de requisitos](checklists/requirements.md)
- [Pesquisa](research.md), [modelo](data-model.md), [contrato público](contracts/api.md) e [guia de verificação](quickstart.md)

### Source Code · Destinos previstos

| Artefato | Caminho |
| --- | --- |
| Java | `brasil4j-core/src/main/java/io/github/brasil4j/identidade/nis/Nis.java` |
| Kotlin | `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Nis.kt` |
| Testes Java | `brasil4j-core/src/test/java/io/github/brasil4j/identidade/nis/`, classes `NisTest` e `NisJarIT`. |
| Testes Kotlin | `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/`, classes `NisTest` e `NisJarIT`. |
| Recursos compartilhados | `brasil4j-core/src/test/resources/nis/`, com catálogo dos vetores C1 e fragmentos XML. A futura configuração inclui apenas esses recursos de teste no POM Kotlin, pelo padrão já usado para CNPJ. |

## Phase 0 · Outline & Research

As decisões D1–D4 estão em [research.md](research.md). O leiaute histórico e a sondagem do XSD estão registrados na [descoberta](../../docs/descoberta/006-nis/leiautes-oficiais.md). As alternativas de origem, máscara e DV foram avaliadas para preservar o recorte estrutural.

## Phase 1 · Design & Contracts

### API

O [contrato público](contracts/api.md) fixa a fábrica Java `Nis.parse(String)`, a value class Kotlin com `Nis(String?)` e a extensão `String.toNis()`. A recuperação canônica é explícita. O erro constante é `NIS inválido.` e o texto padrão é `[NIS oculto]`. O [modelo](data-model.md) delimita a inscrição e o campo de origem.

### Testes e rastreabilidade

| Camada | Estratégia | Requisitos e aceite |
| --- | --- | --- |
| Unidade Java | Parametrizar os 22 casos do C1 e o de 12 dígitos. Conferir o texto integral de todo aceito e tipo e mensagem constante de todo rejeitado. Isolar a igualdade, o hash, outros tipos e o marcador fixo. | FR-001 a FR-005, SC-002 a SC-004 |
| Unidade Kotlin | Reaproveitar o catálogo C1 para aferir a paridade do construtor. Usar também a extensão nos casos não nulos, igualdade dentro de Kotlin e conversão para Java, além de texto direto, interpolação e valor tratado como `Any`. | FR-001 a FR-005, SC-002 a SC-004 |
| Failsafe Java e Kotlin | Ler os mesmos fragmentos XML em UTF-8, extrair o campo como texto e conferir pelos JARs. Afirmar a origem em JAR do tipo Java e, no teste Kotlin, também da value class. | FR-001 a FR-005, SC-001 e regressões de preservação e privacidade |
| Verificações de entrega | Rodar o reator nos dois JDKs, verificar relatórios de cobertura, documentação, bytecode e compatibilidade pública dos dois módulos. | Constituição e compatibilidade com `v0.2.0` |

Cada fragmento terá namespace `http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00` e o caminho `eSocial/evtAdmissao/trabalhador/nisTrab`. O leitor de teste será sensível a namespace, com DTD e acesso a entidades externas desabilitados. Ele passa o conteúdo extraído sem trim ou conversão numérica. Elemento omitido vira nulo e elemento presente sem texto vira string vazia. As fixtures representam a extração de campo e podem omitir elementos exigidos para um evento completo.

Os quatro vetores de SC-001 cobrem zeros preservados, entrada curta e letra. Acrescentar fragmentos com vazio, ausência, máscara, espaços externos e DV divergente com forma correta. Os resultados esperados e as verificações de execução estão no [quickstart](quickstart.md). Os testes do catálogo mantêm a distinção entre sequência aceita pela forma e pelo DV de um legado.

Na implementação, conduzir um ciclo red–green–refactor por comportamento, na ordem gramática, preservação, igualdade, ocultação e paridade. Exercitar a extração e o consumo pelos JARs em ciclo próprio. Registrar o primeiro resultado e a evidência nos dois JDKs.

### Compatibilidade

O desenho adiciona símbolos sem alterar as assinaturas existentes. A checagem deve reconstruir a tag `v0.2.0` em diretório separado e comparar os dois JARs com o script existente, incluindo os membros sintéticos de Kotlin. O CI já verifica v0.1.0. A evidência da comparação com v0.2.0 faz parte da futura validação da 006. Não há dependência de publicação para obter a baseline.

## Aditamento pré-commit · 2026-11-27

FR-006, FR-007 e SC-005 formalizam recursos que o usuário levantou após a implementação. A validação booleana e a fábrica compartilham a conferência no núcleo. A variante de validação adicional usa `Predicate<? super Nis>` após criar o valor estrutural. A formatação explícita usa `Function<? super Nis, String>` sobre um valor pronto. Kotlin oferece pontos de uso sobre o mesmo núcleo, aceitando as interfaces Java 8 como SAMs.

Os contratos padrão conservam as assinaturas e o comportamento. As funções do JDK permitem compor regras e apresentações do mesmo tipo. Operações vinculadas a instâncias podem ser reunidas em listas heterogêneas pelo consumidor. Uma hierarquia comum ou mudança nos tipos CPF/CNPJ exigirá uma spec própria com requisitos de semântica e privacidade. Os testes adicionais cobrem os contratos Java/Kotlin, a ausência de chamada da política para forma inválida, o tratamento dos callbacks e a execução pelos JARs. As verificações do reator e da baseline são repetidas após o aditamento.
