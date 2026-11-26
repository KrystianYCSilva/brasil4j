# Implementation Plan: Bundle para Maven Central

**Branch**: `004-publicacao-central` (identificador lógico) | **Date**: 2026-11-26 | **Spec**: [spec.md](spec.md)

## Summary

Adicionar um perfil Maven local para empacotar fontes, documentação e assinaturas da v0.1.0, mantendo a validação comum de Java 8.

## Technical Context

| Aspecto | Decisão |
| --- | --- |
| Linguagem e plataforma | Runtime Java 8, Kotlin JVM 1.8, Maven Wrapper e geração do bundle em JDK 21. |
| Dependências de build | Maven Source, Maven GPG com Bouncy Castle, JavaDoc, Dokka e plugin oficial do Central. |
| Testes | CI existente em JDK 8 e 21, inspeção do ZIP e verificação de assinaturas. |
| Escopo | Um ZIP com três POMs e seis JARs. A execução é local. |

## Constitution Check

- SDD por spec, plano, tarefas rastreáveis e checagem cruzada.
- DDD preservado pelas mesmas regras de documento e a mesma API.
- TDD de empacotamento com inspeção do estado inicial e do bundle final.
- Java 8 verificado no build comum e no bytecode do bundle. KDoc continua sujeito a Dokka.
- Requisitos de publicação referenciados na [pesquisa](research.md).

## Project Structure

### Documentation (this feature)

[Pesquisa](research.md) · [Modelo](data-model.md) · [Contrato](contracts/bundle.md) · [Quickstart](quickstart.md) · [Tarefas](tasks.md).

### Source Code (repository root)

- `pom.xml` define metadados, fontes e o perfil central.
- `brasil4j-core/pom.xml` anexa JavaDoc antes da assinatura no perfil.
- `brasil4j-kotlin/pom.xml` anexa a documentação gerada pelo Dokka.
