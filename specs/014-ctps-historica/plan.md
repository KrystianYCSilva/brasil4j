# Plano de implementação · Conferência histórica de CTPS

- **Branch** `feature/014-ctps-historica`
- **Data** 2026-11-28
- **Entrada** [Spec 014](spec.md)
- **Base Git** `b4ad5ec` (`develop`), com a Spec 010 integrada. A base original de revisão usava a 010 em `a95efda`.

## Summary

Conferir os campos já extraídos do grupo CTPS do S-2200 v2.5, devolvendo um desfecho estruturado com proveniência. O Java concentra as facetas do perfil. O Kotlin usa o mesmo contrato. A carteira física é uma origem declarada do registro legado. A CTPS Digital usa o CPF pelos modos existentes.

## Technical Context

| Aspecto | Decisão |
| --- | --- |
| Linguagem e plataforma | Java 8 como mínimo, Kotlin 2.3.10 com JVM target 1.8 e bytecode 52. |
| Dependências e armazenamento | Reator Maven Wrapper existente. Core usa apenas JDK. Kotlin usa core e stdlib. Operação em memória sobre três campos textuais. |
| Escopo e desempenho | Uma conferência por chamada. Comprimento verificado em tempo linear por pontos de código. |
| Testes | JUnit 5.12.2, Surefire para unidade e Failsafe para JARs. Fragmentos sintéticos e JAXP restritos aos testes. |
| Compatibilidade | API aditiva. Comparar core e Kotlin com os JARs de `a95efda` e da tag `v0.2.0` via japicmp, incluindo assinaturas sintéticas Kotlin. Manter verdes os consumos das Specs 006, 007 e 010. |

## Constitution Check

| Princípio | Antes da pesquisa | Após o desenho |
| --- | --- | --- |
| I · SDD | Requisitos e aceite aprovados na 014. | Contrato público, impacto aditivo e rastreabilidade definidos. Tarefas e análise de consistência precedem o código. |
| II · DDD | Conferência de leiaute, origem e evidência trabalhista têm fronteiras distintas. | O perfil Java detém as invariantes. O consumidor extrai campos, declara origem e trata a identificação digital via CPF. |
| III · TDD | Cenários observáveis de forma, proveniência e privacidade. | Ciclos previstos por estado, facetas, proveniência, ocultação e paridade pelos JARs. |
| IV · Plataforma | Dois artefatos JVM com runtime mínimo Java 8. | Prevê `clean verify` em JDK 8 e 21, bytecode 52, JaCoCo ≥90% linhas e ≥85% branches, DocLint nos dois JDKs, Dokka no JDK 21 e japicmp. |
| V · Proveniência | Fontes, versão, alcance e pareceres documentados. | Vincular facetas e vetores ao `TCtps`, registrar as fontes em `docs/fontes.md` na entrega e ocultar número, série e UF de origem em diagnósticos. |
| Engenharia JVM | Constituição v0.5 e contratos da 010 disponíveis. | Perfil explícito aplica Strategy para selecionar as regras de conferência. Grupo, resultado e `Uf` são compostos sem criar um identificador canônico artificial. O ArchUnit verifica o isolamento de `trabalhista.ctps` e a dependência explícita de `Uf` (AR-9). |

## Project Structure

### Documentation

- [Pesquisa](research.md), [modelo](data-model.md), [contrato público](contracts/api.md) e [guia de verificação](quickstart.md).
- `tasks.md` pertence à próxima etapa do Spec Kit.

### Source Code · destinos previstos

| Destino | Conteúdo |
| --- | --- |
| `brasil4j-core/src/main/java/io/github/brasil4j/trabalhista/ctps/` | Perfil, grupo bruto, origem, desfecho e registro conforme. |
| `brasil4j-kotlin/src/main/kotlin/io/github/brasil4j/kotlin/identidade/Ctps.kt` | Extensão de conferência e acesso anulável, delegados ao núcleo. |
| `brasil4j-core/src/test/java/io/github/brasil4j/trabalhista/ctps/` | Unidade Java e `CtpsJarIT`. |
| `brasil4j-kotlin/src/test/kotlin/io/github/brasil4j/kotlin/identidade/` | Unidade Kotlin e `CtpsJarIT`. |
| `brasil4j-core/src/test/resources/ctps/esocial-2.5/` | Fragmentos e expectativas compartilhados pelo `testResources` do módulo Kotlin. |

## Phase 0 · Outline & Research

As [decisões técnicas](research.md) fixam as fronteiras do perfil histórico e da identificação digital, a equivalência com as facetas XSD, a composição com APIs existentes e os estados de rejeição.

## Phase 1 · Design & Contracts

O [contrato público](contracts/api.md) usa `CtpsProfile.ESOCIAL_S2200_V2_5.check(group)` no Java e `group.checkCtps(profile)` no Kotlin. O [modelo](data-model.md) documenta precedência, conteúdo recuperável e origem, inclusive em resultados incompletos ou fora do perfil.

| Ciclo previsto | Evidência de aceite | Rastreabilidade |
| --- | --- | --- |
| Estados e precedência | Grupo ausente, grupo presente com campos nulos, texto vazio e campos restantes fora do perfil. | FR-001, SC-001 |
| Facetas e preservação | 11 fragmentos, bordas inclusivas, pontos de código suplementares, 27 UFs e preservação integral dos textos. | FR-002/003, SC-001/002 |
| Proveniência e privacidade | Mesma tripla com três origens, migração com perda, UF de expedição somente em origem física conforme e diagnósticos sem conteúdo. | FR-004/005, SC-003/004 |
| Consumo Java/Kotlin e CPF digital | JARs em JDK 8 e 21, paridade dos 11 fragmentos e CPF nos modos `FORMAT_ONLY` e `NUMERIC`. | FR-001 a FR-006, SC-001 a SC-004 |

Registrar RED, GREEN e refatoração por ciclo. Os comandos, o catálogo e as verificações pelos JARs estão no [quickstart](quickstart.md). A extração dos fragmentos acontece só em código de teste.
