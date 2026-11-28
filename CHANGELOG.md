# Changelog

## Não lançado

<!-- Notas preparadas para a v0.3.0. A data será registrada na branch de release. -->

### Documentos

- Inscrição estrutural de NIS, PIS, Pasep e NIT em Java e Kotlin (Spec 006). A API confere 11 dígitos ASCII, preserva zeros iniciais e oculta o valor na saída padrão. Os testes de consumo incluem o campo histórico `nisTrab` do S-2200 v2.5.
- Composição de listas mistas de CPF, CNPJ e NIS com `DocumentCheck` (Spec 007). Cada item pode escolher o modo de validação, uma regra do consumidor e a apresentação do documento.
- Perfil de CTPS histórica do S-2200 v2.5 em Java e Kotlin, com validação estrutural, proveniência declarada e diagnósticos que ocultam número, série e UF. A vitrine inclui exemplos de registros físicos, projeções legadas e identificação digital via CPF.

### Contratos públicos de documento (Spec 010)

- `Document`, `MaskedDocument`, `SecurityMaskedDocument`, `CheckDigitDocument` e `AbstractDocument`, adotados por CPF, CNPJ e NIS.
- Interpretadores injetáveis `Cpf.parser(mode)`, `Cnpj.parser(mode)` e `Nis.parser(mode)` com `ValidationResult` para rejeições de entrada. `ValidationRule` e `withPolicy` compõem regras, e `DocumentParsers.builder` permite criar interpretadores de documentos do consumidor.
- `Modulus11` aceita alfabetos reduzidos. O `build()` avalia o mapeamento do resto para os restos de 0 a 10 e lança `IllegalArgumentException` para resultado nulo ou fora de 0 a 9 e, com vários DVs, para dígito fora do alfabeto. O builder também recusa menos pesos fixos que DVs e valores negativos para caracteres do alfabeto.
- `ValidationRule.checkDigits` converte base fora do domínio em `INVALID_FORMAT` tanto com `InvalidBaseException` quanto com qualquer `IllegalArgumentException`, pela convenção de argumento inadequado. Defeitos de configuração sinalizados com `IllegalStateException` se propagam.
- Blocos reutilizáveis `verificacao.Modulus11` (equivalência com os cálculos anteriores conferida em 10 000 bases por documento) e `mascara.DocumentMask`.
- Máscara de segurança em CPF (`***.345.678-**`), CNPJ (`**.ABC.345/01DE-**`) e NIS (`***3456789*`), com `*` padrão e caractere configurável, e apresentações reutilizáveis em `DocumentFormatter`. A representação textual padrão não muda.
- Validação opcional de DV no NIS com `CAIXA_FGTS_V016` (fórmula da Caixa para PIS/Pasep no FGTS) e `LEGACY_MOD11` (convenção de Declarações/UEM). A guarda `NisRules.repeatedDigits()` segue a RV_03 do SIPREV. O padrão é estrutural.
- `DocumentCheck.of(input, parser)` aceita qualquer interpretador, e `DocumentCheckResult.getRejection()` expõe o motivo.
- Kotlin: `toCpfOrNull`, `toCnpjOrNull`, `toNisOrNull`, `DocumentParser.parseOrNull`, `toNis(mode)`, `Cnpj.hasValidCheckDigits()` e `applySecurityMask()`.
- Deprecados, sem mudança de comportamento: `CpfValidator`, `CpfFormatter`, `CnpjValidator`, `CnpjFormatter`, `NisValidator` e `NisFormatter`. Remoção prevista para a 1.0.

### Finanças

- Núcleo monetário em Java e Kotlin (Spec 008) com políticas explícitas de arredondamento, rateio e alocação sem perda de centavos e conversão cambial. Divisões terminantes entregam o quociente decimal exato à política customizada.

- Calculadora financeira funcional, percentuais com *gross-up* e tabela progressiva (Spec 011):
  - `CalculoFinanceiro` e `CalculadoraFinanceira` para expressões compostas em `MathContext.DECIMAL128` (ou precisão `1..90`) com arredondamento monetário final único para `Real`, `Dolar`, `Euro` ou `Moeda`;
  - `Percentual` imutável (`de`, `deFator`, `dePontosBase`), incidência genérica preservando o subtipo monetário (`aplicarSobre`, `acrescentarEm`, `descontarDe`) e cálculo por dentro (`calcularBasePorDentro`) com reversibilidade exata nos sete modos padrão;
  - `TabelaProgressiva<T>` parametrizada pelo consumidor com modos `NO_TOTAL` (reconciliado por maiores restos sobre partes fracionárias) e `POR_FAIXA`, memória auditável (`FaixaProgressiva`, `FaixaApurada`, `ApuracaoProgressiva`), parcela a deduzir exata e alíquota efetiva;
  - Kotlin: `.porCento()`, `.pontosBase()`, operadores `+`/`-`, `aplicar`, `acrescentar`, `descontar`, `basePorDentro`, `calcularReais` e DSL `tabelaProgressivaReais`.

### Exemplos

- O módulo `brasil4j-exemplos` (Spec 009) reúne exemplos executáveis em Java e Kotlin para identidade, geografia e finanças. A vitrine é exercitada no build do repositório.

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
