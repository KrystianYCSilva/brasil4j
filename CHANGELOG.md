# Changelog

## Não lançado

- Perfil de CTPS histórica do S-2200 v2.5 em Java e Kotlin, com validação estrutural, proveniência declarada e diagnósticos que ocultam número, série e UF. A vitrine inclui exemplos de registros físicos, projeções legadas e identificação digital via CPF.

- Contratos públicos de documento (Spec 010):
  - `Document`, `MaskedDocument`, `SecurityMaskedDocument`, `CheckDigitDocument` e `AbstractDocument`, adotados por CPF, CNPJ e NIS;
  - interpretadores injetáveis `Cpf.parser(mode)`, `Cnpj.parser(mode)` e `Nis.parser(mode)`, cujo `ValidationResult` informa o motivo da rejeição sem lançar exceção;
  - regras componíveis (`ValidationRule`) e políticas com código (`withPolicy`);
  - `DocumentParsers.builder` para o consumidor criar os próprios documentos no mesmo padrão.
- Blocos reutilizáveis `verificacao.Modulus11` (equivalência com os cálculos anteriores conferida em 10 000 bases por documento) e `mascara.DocumentMask`.
- Máscara de segurança em CPF (`***.345.678-**`), CNPJ (`**.ABC.345/01DE-**`) e NIS (`***3456789*`), com `*` padrão e caractere configurável, e apresentações reutilizáveis em `DocumentFormatter`. A representação textual padrão não muda.
- NIS com validadores de DV opcionais nomeados pela fonte: `CAIXA_FGTS_V016` (fórmula publicada pela Caixa para PIS/Pasep no FGTS), `LEGACY_MOD11` (convenção de Declarações/UEM) e a guarda `NisRules.repeatedDigits()` (SIPREV RV_03). O padrão continua estrutural.
- `DocumentCheck.of(input, parser)` aceita qualquer interpretador, e `DocumentCheckResult.getRejection()` expõe o motivo.
- Kotlin: `toCpfOrNull`, `toCnpjOrNull`, `toNisOrNull`, `DocumentParser.parseOrNull`, `toNis(mode)`, `Cnpj.hasValidCheckDigits()` e `applySecurityMask()`.
- Deprecados, sem mudança de comportamento: `CpfValidator`, `CpfFormatter`, `CnpjValidator`, `CnpjFormatter`, `NisValidator` e `NisFormatter`. Remoção prevista para a 1.0.

- Inscrição estrutural de NIS, PIS, Pasep e NIT em Java e Kotlin. A API confere exatamente 11 dígitos ASCII, preserva zeros iniciais e oculta o valor na saída padrão. A verificação de arquivos históricos cobre o campo `nisTrab` do S-2200 v2.5.
- `NisValidator` oferece verificação estrutural booleana e política opcional do consumidor. `NisFormatter` usa uma função de apresentação explícita. Kotlin delega aos mesmos contratos.

## 0.2.0 — 2026-11-26

- Código estrutural de município do IBGE, com preservação do texto, resolução de UF por prefixo e comparação explícita.
- As 27 UFs incluem nome oficial, sigla, código IBGE, capital, DDDs imutáveis e busca por nome.
- A API Kotlin delega ao núcleo Java, com representação textual canônica e compatibilidade Java 8.

## 0.1.0 — 2026-11-26

- Núcleo Java para CNPJ numérico e alfanumérico, com validação de apresentação, dígitos verificadores e formatação. Modos `ALPHANUMERIC` (padrão), `NUMERIC` e `FORMAT_ONLY`.
- CPF com validação de formato por padrão e conferência opcional de dígitos verificadores no modo `NUMERIC`. A representação padrão em texto oculta todos os dígitos.
- Objetos de valor imutáveis em Java, com forma canônica, máscara e igualdade pelo documento. Value classes e extensões Kotlin delegam às mesmas regras do núcleo.
- Compatibilidade com Java 8. O módulo `brasil4j-core` depende apenas do JDK, e `brasil4j-kotlin` acrescenta a API idiomática para Kotlin.

[Exemplos em Java e Kotlin](README.md#uso-dos-documentos) · [Fontes das regras](docs/fontes.md)
