# Implementation Plan: CPF

**Branch**: `003-cpf` (identificador lógico) | **Date**: 2026-11-25 | **Spec**: [spec.md](spec.md)

**Input**: [Feature specification](spec.md)

## Summary

Separar interpretação estrutural de CPF e verificação de DV, protegendo sua representação textual padrão.

## Technical Context

| Aspecto | Decisão |
| --- | --- |
| Language/Version | Java 8, Kotlin 2.3.10 com JVM target 1.8. |
| Primary Dependencies | Núcleo apenas JDK. Módulo Kotlin depende do núcleo e da biblioteca padrão. |
| Storage | Valores em memória e JARs do reator. |
| Testing | JUnit 5, Surefire, Failsafe, JaCoCo e verificação de JavaDoc. |
| Target Platform | JVM 8 e superiores, com verificação em JDK 8 e 21. |
| Project Type | Biblioteca JVM. |
| Performance Goals | Operações sobre entradas de comprimento fixo, com trabalho proporcional ao texto. |
| Constraints | Java 8, política offline em runtime, núcleos de regra em Java, fontes versionadas. |
| Scale/Scope | CPF textual, dois modos, objeto Java e value class Kotlin. |

## Constitution Check

| Princípio | Como será verificado |
| --- | --- |
| SDD | Artefatos da feature e verificação cruzada de requisitos e tarefas. |
| TDD | Contrato em falha antes do código de domínio, seguido do verde e da revisão. |
| DDD | Objetos imutáveis e regras no contexto Identidade Brasileira. |
| Compatibilidade | JARs em Java 8/21, APIs Java 8 e bytecode 52. |
| Proveniência | Regras e casos ligados a fontes ou políticas de apresentação. |
| Engenharia JVM | Revisar responsabilidades, composição e APIs idiomáticas pelo guia de padrões. |
| Documentação | DocLint nos dois JDKs e Dokka no verify do JDK 21. |

## Project Structure

### Documentation (this feature)

- [Pesquisa](research.md)
- [Modelo](data-model.md)
- [Contrato](contracts/api.md)
- [Quickstart](quickstart.md)
- [Tarefas](tasks.md)

### Source Code (repository root)

```text
pom.xml
brasil4j-core/src/{main,test}/java/
brasil4j-kotlin/src/{main,test}/kotlin/
.github/workflows/verify.yml
```

**Structure Decision**: O reator preserva um núcleo Java e um adaptador Kotlin. A implementação de documentos segue seus pacotes de domínio.
