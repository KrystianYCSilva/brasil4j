---
description: "Tarefas da spec Código IBGE de município e UF"
---

# Tasks: Código IBGE de município e UF

**Input**: [Spec](spec.md), [plano](plan.md) e [contrato](contracts/api.md).

## Phase 1: Setup

- [x] T001 Aceite explícito de Krystian em 2026-11-26 para a v0.2.0, com adoção separada por consumidor. Base Git 1a0d045f8bca85656f173103177d49abdb509faf, com alterações locais. Manifesto SHA-256 da spec aceita 645A9DBB06699063A57F1C4E3FF682F8CCCD188D1767D182E129E0275D7F1C24. Resolvedor Spec Kit confirmou a 005.
- [x] T002 Registrar fonte IBGE, data de consulta, snapshot das 27 UFs e vetores públicos/sintéticos. [F-08](../../docs/fontes.md) e [pesquisa](research.md) (FR-001 a FR-004).

## Phase 2: User Story 1

- [x] T003 Red–green–refactor em `CodigoMunicipio.java` e `CodigoMunicipioTest.java`. Separar ciclos de apresentação ASCII e erro, preservação de texto e prefixo, e identidade canônica com igualdade, hash e `toString()` (FR-001, FR-003).

## Phase 3: User Story 2

- [x] T004 Red–green–refactor em `Uf.java`, `UfTest.java` e `CodigoMunicipioTest.java`. Verificar enum e busca exata, com nulo, prefixo desconhecido, sigla, espaço, sinal, tamanho alternativo e Unicode. Cobrir UF divergente, `0012345` com UF nula e o vetor sintético `3599999` que resolve SP sem DV nem consulta ao catálogo. Explicar a fronteira estrutural em JavaDoc (FR-002, FR-003).

- [x] T009 [US3] Red–green–refactor para nome, sigla, capital e busca por nome. Em ciclo próprio, adicionar DDDs, ordem, imutabilidade e compartilhamento GO/DF. Conferir metadados das 27 UFs (FR-005).

## Phase 4: Integração e verificação

- [x] T005 Adicionar a value class e extensões em `CodigoMunicipio.kt`. Documentar a fronteira estrutural em KDoc e delegar `matchesUf` ao Java. Testar `0012345` com UF nula, apresentação inválida pelo construtor e pela extensão, mensagem sem a entrada inválida não vazia, igualdade/hash sob `Any` e após `toJava()`, e `toString()` canônico direto, por interpolação e sob `Any` (FR-004).
- [x] T006 Exercitar os JARs em `CodigoMunicipioJarIT.java` e `CodigoMunicipioJarIT.kt`. No adaptador de teste, conferir ida e volta de `3550308`, `4115200`, `4113700` e `3509502`, a perda dos zeros de `0012345` no caminho numérico e a rejeição textual de `+4115200`, `04115200` e dígitos Unicode, mesmo quando a conversão numérica produz um código canônico. Confirmar `3599999` como caso exclusivamente estrutural. Usar UF divergente para representar a checagem do Core. Exercitar também a busca por nome e metadados de UF pelos JARs. O ensaio ocorre em isolamento e preserva os serviços (FR-004, FR-005).
- [x] T007 Verificar JDK 8/21, bytecode, cobertura, DocLint e Dokka. Na v0.2.0, executar japicmp nos dois JARs com baseline verificada da v0.1.0 (FR-004).
- [x] T008 Documentar requisitos e arquitetura de Geografia, atualizar exemplos no README e registrar a entrega no CHANGELOG da v0.2.0 (FR-001 a FR-005).

Os arquivos Java ficam sob `brasil4j-core/src/{main,test}/java/io/github/brasil4j/geografia`. Os arquivos Kotlin ficam sob `brasil4j-kotlin/src/{main,test}/kotlin/io/github/brasil4j/kotlin/geografia`.

## Dependencies & Execution Order

A evidência da T002 subsidia a T001. Depois do aceite, executar T003 → T004 → T005 → T009 → T006 → T007 → T008.
