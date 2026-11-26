# brasil4j 0.1.0

Biblioteca de domínio brasileiro para Java e Kotlin, com Java 8 como versão mínima.

| Artefato | Coordenadas | API |
| --- | --- | --- |
| Núcleo | `io.github.brasil4j:brasil4j-core` | Objetos de valor e regras de CNPJ e CPF |
| Kotlin | `io.github.brasil4j:brasil4j-kotlin` | Value classes e extensões sobre o núcleo |

Execute `sh ./mvnw clean verify` ou `.\mvnw.cmd clean verify` no Windows. O build verifica testes, cobertura e JavaDoc. Em JDK 21, o verify verifica KDoc com Dokka e gera a referência Kotlin em target/dokka. As coordenadas são locais ao projeto até a verificação do namespace e a publicação.

## Especificações

1. [Fundação JVM](specs/001-fundacao/spec.md)
2. [CNPJ](specs/002-cnpj/spec.md)
3. [CPF](specs/003-cpf/spec.md)

[Contexto de engenharia](.context/README.md) · [Requisitos](docs/requisitos.md) · [Arquitetura](docs/arquitetura.md) · [Constituição](.specify/memory/constitution.md) · [Fontes](docs/fontes.md) · [Descoberta do C1](docs/descoberta/c1/README.md)

## Uso dos documentos

- [CNPJ em Java e Kotlin](specs/002-cnpj/quickstart.md). O modo padrão valida o DV de CNPJs numéricos e alfanuméricos.
- [CPF em Java e Kotlin](specs/003-cpf/quickstart.md). O modo padrão verifica o formato. Escolha NUMERIC para conferir o DV.
