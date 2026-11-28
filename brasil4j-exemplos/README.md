# brasil4j-exemplos · Guia Prático e Casos de Uso

Este módulo reúne **exemplos executáveis, comentados e orientados a cenários reais** das classes, validadores, modos, formatadores, políticas monetárias e operadores do `brasil4j` em **Java 8+** e **Kotlin**.

> **Nota sobre distribuição:** `brasil4j-exemplos` contém código exclusivamente em pacotes de teste (`src/test/java` e `src/test/kotlin`) e **não é publicado no Maven Central**. Ele é compilado e executado automaticamente em `mvnw clean verify` para garantir que todos os exemplos permaneçam 100% sincronizados com a API real.

## Índice de Exemplos e Casos de Uso

### Java 8+ (`src/test/java/io/github/brasil4j/exemplos/java/`)

| Suíte | Cenários e recursos demonstrados |
| --- | --- |
| [IdentidadeExemplosTest.java](src/test/java/io/github/brasil4j/exemplos/java/IdentidadeExemplosTest.java) | **CNPJ:** modos `ALPHANUMERIC`, `NUMERIC` e `FORMAT_ONLY`, `CnpjValidator`, `CnpjFormatter`, deduplicação em `Set`/`Map` e **agrupamento corporativo de matriz/filiais por `getRoot()`**.<br>**CPF:** `FORMAT_ONLY` vs. `NUMERIC`, preservação de zeros à esquerda, `CpfFormatter`, **triagem de base legada em quarentena** e **ocultação automática em logs (`***.***.***-**`, LGPD)**.<br>**NIS/PIS/Pasep/NIT:** conferência estritamente estrutural (11 dígitos ASCII com zeros iniciais, sem DV por padrão), composição de política módulo 11 do consumidor com `Predicate.and(...)`, múltiplos leiautes via `NisFormatter` (holerite vs. portal mascarado) e `[NIS oculto]`.<br>**Composição (`DocumentCheck`):** avaliação uniforme de lote heterogêneo (`ACCEPTED`, `REJECTED_BY_POLICY`, `REJECTED_BY_VALIDATION`). |
| [GeografiaExemplosTest.java](src/test/java/io/github/brasil4j/exemplos/java/GeografiaExemplosTest.java) | **Validação cruzada fiscal (NF-e/CT-e/ISS):** conferência entre `CodigoMunicipio` (`cMun` IBGE de 7 dígitos) e `Uf` declarada (`matchesUf`), uso como chave de `Map` e prefixos sem UF.<br>**Saneamento de planilhas:** `Uf.fromName` tolerante a caixa/acentos/espaços e `Uf.fromIbgeCode`.<br>**Telecom/Cadastro:** construção de **índice reverso de DDD Anatel para UFs**, evidenciando DDD compartilhado (`61` em `DF` e `GO`). |
| [FinancasExemplosTest.java](src/test/java/io/github/brasil4j/exemplos/java/FinancasExemplosTest.java) | **Políticas de arredondamento:** `ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`, `PARA_CIMA`, `TETO`, `PISO` (em positivos e negativos), `fromRoundingMode`, `comArredondamento` e lambda customizada.<br>**Construção e sinais:** `de`, `exato` (XML fiscal/SPED), `deCentavos` (CNAB), `deInteiro`, `Dinheiro.zero`, `abs`, `negar`, `isZero`/`isPositivo`/`isNegativo` e ordenação `Comparable`.<br>**Moedas abertas:** `BRL`/`USD`/`EUR`, `Moeda.iso` (`JPY/0`, `KWD/3`), `Moeda.fromJdkCurrency` e `enum` próprio do consumidor implementando `Moeda` (pontos e kWh).<br>**Casos de uso financeiros:** fechamento de nota de serviço com desconto e retenção na fonte; crediário com entrada + parcelas (`ratear` início/fim e estorno negativo); rateio de frete e desconto de NF-e por pesos (`alocar` Hamilton com item isento peso `0`); e **consolidação de carteira multimoeda em BRL** com `TaxaCambio` direta, cruzada (`EUR/USD`) e invertida (`inverter()`). |
| [CenariosIntegradosExemplosTest.java](src/test/java/io/github/brasil4j/exemplos/java/CenariosIntegradosExemplosTest.java) | **Fluxos ponta a ponta multidomínio:** emissão de venda interestadual (`Cnpj` alfanumérico + `Cpf` + conferência `CodigoMunicipio`/`Uf` + rateio de frete e duplicatas + log seguro LGPD) e liquidação de folha/reembolso internacional de filial corporativa (`Cnpj.getRoot()` + `Nis` no holerite + câmbio PTAX para centavos CNAB). |
| [ContratosExemplosTest.java](src/test/java/io/github/brasil4j/exemplos/java/ContratosExemplosTest.java) | **Contratos (Spec 010):** `DocumentParser` injetável com motivo da rejeição (`ValidationResult`/`Rejection`), políticas com código (`withPolicy`), dublê de teste, **documento próprio do consumidor** montado com `AbstractDocument` + `DocumentMask` + `Modulus11` + `DocumentParsers` em lista com `DocumentCheck.of`, **máscara de segurança** (`***.345.678-**`, `*` ou `#`) e máscara completa, e **validadores opcionais do NIS** (`CAIXA_FGTS_V016`, `LEGACY_MOD11`, guarda `NisRules.repeatedDigits()`). |
| [CtpsExemplosTest.java](src/test/java/io/github/brasil4j/exemplos/java/CtpsExemplosTest.java) | Importação de CTPS física com zeros iniciais, projeção legada com perda declarada e distinção entre UF informada e UF de expedição. Conferência do identificador da CTPS Digital pelos modos do CPF. |

### Kotlin (`src/test/kotlin/io/github/brasil4j/exemplos/kotlin/`)

| Suíte | Cenários e recursos demonstrados |
| --- | --- |
| [IdentidadeGeografiaKotlinExemplosTest.kt](src/test/kotlin/io/github/brasil4j/exemplos/kotlin/IdentidadeGeografiaKotlinExemplosTest.kt) | *Value classes* e extensões `.toCnpj()`, `.toCpf()`, `.toNis()`, `.isValidNis()` em receptor anulável (`String?`), `.toCodigoMunicipio()`, agrupamento com `groupBy { it.root }`, triagem com `partition { it.hasValidCheckDigits() }`, fallback nulo-seguro com Elvis (`?:`), consulta funcional sobre `Uf.entries` e ponte `.toJava()`. |
| [FinancasKotlinExemplosTest.kt](src/test/kotlin/io/github/brasil4j/exemplos/kotlin/FinancasKotlinExemplosTest.kt) | Todas as extensões construtoras (`.reais()`, `.reaisInteiros()`, `.centavosReais()`, `.dolares()`, `.dolaresInteiros()`, `.centavosDolares()`, `.euros()`, `.eurosInteiros()`, `.centavosEuros()`, `.emMoeda(...)`), operadores `+`, `-`, `*` comutativo com `Int`/`Long` e `* BigDecimal` à direita, `/`, `-` unário (inclusive no supertipo `Dinheiro`), `coerceIn`, fechamento de carrinho de e-commerce com `alocarPorPesos(vararg)` + `ratear`, consolidação de carteira com `when (posicao)` e fronteiras de exceção (`assertThrows`). |
| [CenariosIntegradosKotlinExemplosTest.kt](src/test/kotlin/io/github/brasil4j/exemplos/kotlin/CenariosIntegradosKotlinExemplosTest.kt) | **Modelagem de domínio com `data class`:** pedido interestadual e colaborador com *value classes* (`Cnpj`, `Cpf`, `Nis`, `CodigoMunicipio`), provando que até o `toString()` gerado pelo compilador Kotlin em `data class` preserva a ocultação automática de `Cpf` e `Nis`. |
| [ContratosKotlinExemplosTest.kt](src/test/kotlin/io/github/brasil4j/exemplos/kotlin/ContratosKotlinExemplosTest.kt) | Conversões tolerantes a nulo (`toCpfOrNull`, `toCnpjOrNull`, `toNisOrNull`) com `mapNotNull` e Elvis, `when` sobre `RejectionReason`, `parseOrNull` genérico, `applySecurityMask('#')` e composição do NIS com guarda SIPREV e política por lambda. |
| [CtpsKotlinExemplosTest.kt](src/test/kotlin/io/github/brasil4j/exemplos/kotlin/CtpsKotlinExemplosTest.kt) | Conferência do grupo histórico com `checkCtps`, preservação dos zeros e leitura da origem, do registro e da UF de expedição pelas extensões anuláveis. CTPS Digital com `toCpfOrNull`. |

## Como executar apenas os exemplos

Na raiz do repositório:

```sh
sh ./mvnw test -pl brasil4j-exemplos -am
```

No PowerShell (Windows):

```powershell
.\mvnw.cmd test -pl brasil4j-exemplos -am
```
