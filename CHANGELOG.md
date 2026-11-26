# Changelog

## 0.2.0 — em desenvolvimento

- Código estrutural de município do IBGE, com preservação do texto, resolução de UF por prefixo e comparação explícita.
- As 27 UFs incluem nome oficial, sigla, código IBGE, capital, DDDs imutáveis e busca por nome.
- A API Kotlin delega ao núcleo Java, com representação textual canônica e compatibilidade Java 8.

## 0.1.0 — 2026-11-26

- Núcleo Java para CNPJ numérico e alfanumérico, com validação de apresentação, dígitos verificadores e formatação. Modos `ALPHANUMERIC` (padrão), `NUMERIC` e `FORMAT_ONLY`.
- CPF com validação de formato por padrão e conferência opcional de dígitos verificadores no modo `NUMERIC`. A representação padrão em texto oculta todos os dígitos.
- Objetos de valor imutáveis em Java, com forma canônica, máscara e igualdade pelo documento. Value classes e extensões Kotlin delegam às mesmas regras do núcleo.
- Compatibilidade com Java 8. O módulo `brasil4j-core` depende apenas do JDK, e `brasil4j-kotlin` acrescenta a API idiomática para Kotlin.

[Exemplos em Java e Kotlin](README.md#uso-dos-documentos) · [Fontes das regras](docs/fontes.md)
