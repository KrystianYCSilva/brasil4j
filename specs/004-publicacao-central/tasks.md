---
description: "Tarefas do bundle para Maven Central"
---

# Tasks: Bundle para Maven Central

**Input**: [Spec](spec.md) e [plano](plan.md)

## Phase 1: Setup

- [x] T001 [US1] Conferir POMs, metadados e artefatos atuais (FR-001 a FR-004).

## Phase 2: User Story 1

- [x] T002 [US1] Acrescentar metadados, fontes e documentação assinável nos três POMs (FR-001, FR-002).
- [x] T003 [US1] Isolar assinatura por ambiente e geração local no perfil central (FR-003, FR-004).
- [x] T004 [US1] Inspecionar ZIP e verificar assinaturas, checksums, bytecode e a tag. Repetir o build comum em Java 8 (FR-001 a FR-004).

## Phase 3: User Story 2

- [x] T005 [US2] Referenciar o guia do namespace e registrar o ponto de validação no Portal (FR-005).

## Dependencies & Execution Order

A checagem inicial precede a configuração. Os anexos precedem assinatura e ZIP.

## Evidências

- Corretto 21.0.13, perfil `central`. 193 testes aprovados. O log confirmou `skipUpload`.
- ZIP com três POMs e seis JARs, incluindo fontes Java/Kotlin e HTML de JavaDoc/Dokka. GnuPG verificou as nove assinaturas com chave efêmera. Os 36 checksums coincidiram. Os JARs principais têm bytecode 52.
- Corretto 8u512, build regular. 193 testes aprovados. O perfil falhou em JDK 8 e falhou sem a chave em JDK 21.
- A tag `v0.1.0` manteve o commit `8a6780d61a8d0ee036fe5ec134da906b596a2fb8`.
- O guia do namespace está concluído. O estado `Verified` depende da conta do mantenedor no Portal.

## Revalidação após a Spec 005

- Build comum de `0.2.0-SNAPSHOT` com 343 testes aprovados em Corretto 8u512 e 21.0.13. O perfil `central` também passou em `verify` no JDK 21 com chave descartável. Em `validate`, o JDK 8 e a ausência de chave no JDK 21 impediram a execução.
- Uma cópia de teste com versões `0.2.0` gerou o ZIP com o endpoint de publicação local. O log confirmou `skipUpload`. Foram conferidos três POMs, seis JARs, nove assinaturas, 36 checksums, fontes, documentação e bytecode 52.
- A validação preservou a tag e o ZIP da v0.1.0. O checkout segue em `0.2.0-SNAPSHOT`.
