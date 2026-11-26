# brasil4j

Biblioteca de domínio brasileiro para Java e Kotlin, com Java 8 como versão mínima.

| Artefato | Coordenadas | API |
| --- | --- | --- |
| Núcleo | `io.github.brasil4j:brasil4j-core` | Objetos de valor e regras de CNPJ, CPF e geografia brasileira |
| Kotlin | `io.github.brasil4j:brasil4j-kotlin` | Value classes e extensões sobre o núcleo |

Execute `sh ./mvnw clean verify` ou `.\mvnw.cmd clean verify` no Windows. O build verifica testes, cobertura e JavaDoc. Em JDK 21, o verify verifica KDoc com Dokka e gera a referência Kotlin em target/dokka. A publicação no Maven Central depende da verificação do namespace. [Guia do bundle local e do namespace](specs/004-publicacao-central/quickstart.md).

## Especificações

1. [Fundação JVM](specs/001-fundacao/spec.md)
2. [CNPJ](specs/002-cnpj/spec.md)
3. [CPF](specs/003-cpf/spec.md)
4. [Publicação no Central](specs/004-publicacao-central/spec.md)
5. [Código IBGE de município e UF](specs/005-municipio-ibge-uf/spec.md)

[Contexto de engenharia](.context/README.md) · [Requisitos](docs/requisitos.md) · [Arquitetura](docs/arquitetura.md) · [Constituição](.specify/memory/constitution.md) · [Fontes](docs/fontes.md) · [Descoberta do C1](docs/descoberta/c1/README.md)

## Uso dos documentos

- [CNPJ em Java e Kotlin](specs/002-cnpj/quickstart.md). O modo padrão valida o DV de CNPJs numéricos e alfanuméricos.
- [CPF em Java e Kotlin](specs/003-cpf/quickstart.md). O modo padrão verifica o formato. Escolha NUMERIC para conferir o DV.
- [Código IBGE de município e UF em Java e Kotlin](specs/005-municipio-ibge-uf/quickstart.md). Consulte o prefixo e compare a UF. Use `Uf.fromName("sao paulo")` para obter a UF e consultar nome, sigla, capital, código IBGE e DDDs.
