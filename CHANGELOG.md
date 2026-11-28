# Changelog

## Não lançado

### NIS e composição de documentos (Specs 006 e 007)

- Inscrição estrutural de NIS, PIS, Pasep e NIT em Java e Kotlin. A API confere exatamente 11 dígitos ASCII, preserva zeros iniciais e oculta o valor na saída padrão. A verificação de arquivos históricos cobre o campo `nisTrab` do S-2200 v2.5.
- `NisValidator` oferece verificação estrutural booleana e política opcional do consumidor. `NisFormatter` usa uma função de apresentação explícita. Kotlin delega aos mesmos contratos.
- `DocumentCheck` compõe conferência nativa, política do consumidor e apresentação explícita para CPF, CNPJ e NIS em listas heterogêneas.

### Núcleo monetário (Spec 008)

- Pacote `financas` com moedas abertas (`Moeda`, `BRL`, `USD`, `EUR`, `Moeda.iso`) e `Dinheiro` imutável em hierarquia selada (`Real`, `Dolar`, `Euro`, `QuantiaMonetaria`).
- Políticas de arredondamento explícitas (`MetodoArredondamento`: `ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`, `PARA_CIMA`, `TETO`, `PISO`), sem arredondamento implícito.
- Parcelamento sem perda de centavos (`ratear`, com `PoliticaResiduo`) e alocação proporcional por pesos (`alocar`, maiores restos).
- `TaxaCambio` exata e involutiva (`inverter()`).
- Kotlin: construtores fluentes (`.reais()`, `.dolares()`, `.euros()`, `.emMoeda(...)`) e operadores aritméticos sobre os tipos monetários.

### Módulo de exemplos (Spec 009)

- `brasil4j-exemplos`: suítes de teste comentadas em Java e Kotlin para identidade, geografia, finanças, contratos, CTPS e cenários integrados. O módulo roda em `verify`, não é publicado e mantém os exemplos sincronizados com a API.

### Contratos de documento (Spec 010)

- `Document`, `MaskedDocument`, `SecurityMaskedDocument`, `CheckDigitDocument` e `AbstractDocument`, adotados por CPF, CNPJ e NIS.
- Interpretadores injetáveis `Cpf.parser(mode)`, `Cnpj.parser(mode)` e `Nis.parser(mode)`, cujo `ValidationResult` informa o motivo da rejeição sem lançar exceção.
- Regras componíveis (`ValidationRule`) e políticas com código (`withPolicy`).
- `DocumentParsers.builder` para o consumidor criar os próprios documentos no mesmo padrão.
- Blocos reutilizáveis `verificacao.Modulus11` (equivalência com os cálculos anteriores conferida em 10 000 bases por documento) e `mascara.DocumentMask`.
- `Modulus11.builder().build()` recusa configuração incoerente (menos pesos fixos que DVs, valor de caractere negativo e vários DVs sem os dígitos no alfabeto). `InvalidBaseException` marca a base fora do domínio, e `ValidationRule.checkDigits` só converte essa exceção em `INVALID_FORMAT`; qualquer outra `IllegalArgumentException` do algoritmo se propaga.
- Máscara de segurança em CPF (`***.345.678-**`), CNPJ (`**.ABC.345/01DE-**`) e NIS (`***3456789*`), com `*` padrão e caractere configurável, e apresentações reutilizáveis em `DocumentFormatter`. A representação textual padrão não muda.
- NIS com validadores de DV opcionais nomeados pela fonte: `CAIXA_FGTS_V016` (fórmula publicada pela Caixa para PIS/Pasep no FGTS), `LEGACY_MOD11` (convenção de Declarações/UEM) e a guarda `NisRules.repeatedDigits()` (SIPREV RV_03). O padrão continua estrutural.
- `DocumentCheck.of(input, parser)` aceita qualquer interpretador, e `DocumentCheckResult.getRejection()` expõe o motivo.
- Kotlin: `toCpfOrNull`, `toCnpjOrNull`, `toNisOrNull`, `DocumentParser.parseOrNull`, `toNis(mode)`, `hasValidCheckDigits()` em `Cpf` e `Cnpj` e `applySecurityMask()`.

### Calculadora financeira, percentuais e tabela progressiva (Spec 011)

- `CalculoFinanceiro` e `CalculadoraFinanceira` para expressões compostas em `MathContext.DECIMAL128` (ou precisão `1..90`) com arredondamento monetário final único para `Real`, `Dolar`, `Euro` ou `Moeda`.
- `Percentual` imutável (`de`, `deFator`, `dePontosBase`), incidência genérica preservando o subtipo monetário (`aplicarSobre`, `acrescentarEm`, `descontarDe`) e cálculo por dentro (`calcularBasePorDentro`) com reversibilidade exata nos sete modos padrão.
- `TabelaProgressiva<T>` parametrizada pelo consumidor com modos `NO_TOTAL` (reconciliado por maiores restos sobre partes fracionárias) e `POR_FAIXA`, memória auditável (`FaixaProgressiva`, `FaixaApurada`, `ApuracaoProgressiva`), parcela a deduzir exata e alíquota efetiva.
- Kotlin: `.porCento()`, `.pontosBase()`, operadores `+`/`-` entre percentuais, `aplicar`, `acrescentar`, `descontar`, `basePorDentro`, `calcularReais` e DSL `tabelaProgressivaReais`.

### CTPS histórica (Spec 014)

- Perfil de CTPS histórica do S-2200 v2.5 (`trabalhista.ctps`, `CtpsProfile.ESOCIAL_S2200_V2_5`) em Java e Kotlin (`checkCtps`), com validação estrutural, proveniência declarada e diagnósticos que ocultam número, série e UF.
- A vitrine inclui exemplos de registros físicos, projeções legadas e identificação digital via CPF.

### Build e CI

- Gitflow aplicado como código: rulesets de `main`, `develop` e tags `v*` em `.github/rulesets/`, aplicados por `scripts/github/apply-repo-settings.ps1`, com checks obrigatórios `verify (8)`, `verify (21)`, `api-compatibility`, `mutation` e `branch-policy` (PRs para `main`).
- Pipeline `verify.yml` com JDK 8 e 21, ArchUnit, JaCoCo, DocLint, Dokka, conferência de bytecode 52, japicmp contra a última tag e mutação com PIT.
- `release.yml` publica a GitHub Release com os JARs e a seção do CHANGELOG ao receber a tag `vX.Y.Z` em `main`. A publicação no Maven Central segue pendente da aprovação do namespace.
- Dependências atualizadas: Maven Wrapper 3.10.2, JaCoCo 0.8.16, maven-resources-plugin 3.5.0, Dokka 2.3.0 e as ações `actions/checkout` v7, `actions/setup-java` v6 e `actions/upload-artifact` v7. O Kotlin permanece em 2.3.10.
- Kotlin com *explicit API mode* ativado no adaptador.

### Deprecados

- Sem mudança de comportamento: `CpfValidator`, `CpfFormatter`, `CnpjValidator`, `CnpjFormatter`, `NisValidator` e `NisFormatter`. Use os interpretadores `parser(mode)` e as apresentações de `DocumentFormatter`. Remoção prevista para a 1.0.

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
