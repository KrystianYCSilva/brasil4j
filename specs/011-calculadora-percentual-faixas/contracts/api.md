# Contrato Público de API · Spec 011 (`io.github.brasil4j.financas` e `io.github.brasil4j.kotlin.financas`)

Entradas nulas, fora de domínio, com gramática inválida ou subclasses não confiáveis de `BigDecimal` lançam `IllegalArgumentException`. Reutilização de `TabelaProgressiva.Builder` já encerrado lança `IllegalStateException`. Exceções lançadas pelo callback `CalculoFinanceiro` propagam-se sem embrulho.

## 1. Java · `brasil4j-core` (`io.github.brasil4j.financas`)

### 1.1. `CalculoFinanceiro` (`@FunctionalInterface public interface`)
- `BigDecimal calcular(MathContext precisaoIntermediaria)`

### 1.2. `CalculadoraFinanceira` (`public final class`)
- Constante: `public static final CalculadoraFinanceira PADRAO` (`ABNT_NBR_5891`, `MathContext.DECIMAL128`).
- Fábricas de instância configurável:
  - `static CalculadoraFinanceira de(MetodoArredondamento arredondamento)` (`MathContext.DECIMAL128`)
  - `static CalculadoraFinanceira de(MetodoArredondamento arredondamento, MathContext precisaoIntermediaria)` (`precision` em `1..90`, `roundingMode != UNNECESSARY`)
- Consultas: `getArredondamento()`, `getPrecisaoIntermediaria()`, `equals`, `hashCode`, `toString`.
- Execução em instância (aplica um arredondamento monetário final e retém `this.getArredondamento()`):
  - `Real calcularReais(CalculoFinanceiro calculo)`
  - `Dolar calcularDolares(CalculoFinanceiro calculo)`
  - `Euro calcularEuros(CalculoFinanceiro calculo)`
  - `Dinheiro calcularMoeda(Moeda moeda, CalculoFinanceiro calculo)`
- Atalhos estáticos diretos (`DECIMAL128`; equivalentes a `CalculadoraFinanceira.de(arred).calcularX(calculo)`):
  - `static Real emReais(CalculoFinanceiro calculo)` / `emReais(MetodoArredondamento arred, CalculoFinanceiro calculo)`
  - `static Dolar emDolares(CalculoFinanceiro calculo)` / `emDolares(MetodoArredondamento arred, CalculoFinanceiro calculo)`
  - `static Euro emEuros(CalculoFinanceiro calculo)` / `emEuros(MetodoArredondamento arred, CalculoFinanceiro calculo)`
  - `static Dinheiro emMoeda(Moeda moeda, CalculoFinanceiro calculo)` / `emMoeda(Moeda moeda, MetodoArredondamento arred, CalculoFinanceiro calculo)`

### 1.3. `Percentual` (`public final class implements Comparable<Percentual>`)
- Constantes: `Percentual.ZERO` (`0%`), `Percentual.CEM` (`100%`).
- Fábricas (ponto decimal obrigatório em texto; vírgula ou `float`/`double` não aceitos; fator resultante deve ter `precision <= 38` e `scale` em `0..40`):
  - `static Percentual de(String pontosPercentuais)` / `de(BigDecimal pontosPercentuais)` / `de(long pontosPercentuais)`
  - `static Percentual deFator(String fatorUnitario)` / `deFator(BigDecimal fatorUnitario)`
  - `static Percentual dePontosBase(long pontosBase)`
- Consultas e álgebra pura:
  - `BigDecimal getPontosPercentuais()`, `BigDecimal getFator()`, `BigDecimal getFatorAcrescimo()`, `BigDecimal getFatorDesconto()`
  - `boolean isZero()`, `boolean isPositivo()`, `boolean isNegativo()`
  - `Percentual somar(Percentual outro)`, `Percentual subtrair(Percentual outro)`, `Percentual negar()`, `Percentual abs()`
  - `int compareTo(Percentual outro)`, `equals`, `hashCode`, `toString()` (`getPontosPercentuais().toPlainString() + "%"`)
- Operações monetárias genéricas (preservam o subtipo concreto/dinâmico de `Dinheiro` — `Real`, `Dolar`, `Euro` ou `QuantiaMonetaria` — e retêm `valor.getArredondamento()` ou `novoArredondamento`):
  - `<T extends Dinheiro> T aplicarSobre(T valor)` / `aplicarSobre(T valor, MetodoArredondamento novoArredondamento)`
  - `<T extends Dinheiro> T acrescentarEm(T valor)` / `acrescentarEm(T valor, MetodoArredondamento novoArredondamento)`
  - `<T extends Dinheiro> T descontarDe(T valor)` / `descontarDe(T valor, MetodoArredondamento novoArredondamento)`
  - `<T extends Dinheiro> T calcularBasePorDentro(T valorLiquido)` / `calcularBasePorDentro(T valorLiquido, MetodoArredondamento novoArredondamento)` (`0% <= this < 100%`)

### 1.4. `TabelaProgressiva<T extends Dinheiro>`, `ModoApuracaoProgressiva`, `FaixaProgressiva<T>`, `FaixaApurada<T>` e `ApuracaoProgressiva<T>`
- `public enum ModoApuracaoProgressiva { NO_TOTAL, POR_FAIXA }`
- Fábricas do builder (`TabelaProgressiva.Builder<T>`, uso único):
  - `static Builder<Real> emReais()`
  - `static Builder<Dolar> emDolares()`
  - `static Builder<Euro> emEuros()`
  - `static Builder<Dinheiro> paraMoeda(Moeda moeda)` (produz instâncias canônicas `Real`/`Dolar`/`Euro`/`QuantiaMonetaria` sob o supertipo `Dinheiro` e confere a moeda em tempo de execução)
- Métodos de `Builder<T>`:
  - `Builder<T> faixaAte(T limiteSuperior, Percentual aliquota)` e `Builder<T> faixaAte(String limiteSuperior, Percentual aliquota)`
  - `TabelaProgressiva<T> faixaAcima(Percentual aliquotaFinal)`
- Métodos de `TabelaProgressiva<T>`:
  - `Moeda getMoeda()`, `List<FaixaProgressiva<T>> getFaixas()`
  - `ApuracaoProgressiva<T> apurar(T baseCalculo)` (`NO_TOTAL`, `baseCalculo.getArredondamento()`)
  - `ApuracaoProgressiva<T> apurar(T baseCalculo, ModoApuracaoProgressiva modo)`
  - `ApuracaoProgressiva<T> apurar(T baseCalculo, ModoApuracaoProgressiva modo, MetodoArredondamento arredondamento)`
- Métodos de `FaixaProgressiva<T>`: `getIndice()`, `getLimiteInferior()`, `getLimiteSuperior()` (`Optional<T>`), `getAliquota()`, `getParcelaADeduzirExata()`.
- Métodos de `FaixaApurada<T>`: `getFaixa()`, `getBaseNaFaixa()`, `getValorExato()`, `getValorArredondadoIndividual()`, `getValorApurado()`.
- Métodos de `ApuracaoProgressiva<T>`: `getBaseCalculo()`, `getModo()`, `getArredondamento()`, `getFaixas()`, `getValorExatoTotal()`, `getValorTotal()`, `getDiferencaArredondamento()`, `Percentual getAliquotaEfetiva(int casasPercentuais, MetodoArredondamento arredondamento)` (`casasPercentuais` em `0..18`; calculada sobre `valorTotal / baseCalculo`; devolve `Percentual.ZERO` quando `baseCalculo.isZero()`).

---

## 2. Kotlin · `brasil4j-kotlin` (`io.github.brasil4j.kotlin.financas`)

- `Percentual`:
  - `String.porCento()`, `BigDecimal.porCento()`, `Int.porCento()`, `Long.porCento()`, `Long.pontosBase()`, `Int.pontosBase()`
  - Operadores `+`, `-`, `-` unário em `Percentual`
  - Extensões genéricas `<T : Dinheiro> T.aplicar(Percentual, MetodoArredondamento = this.arredondamento): T`, `acrescentar(...)`, `descontar(...)`, `basePorDentro(...)`
- `CalculadoraFinanceira`:
  - `calcularReais(arredondamento = ABNT_NBR_5891, precisao = MathContext.DECIMAL128, bloco: (MathContext) -> BigDecimal): Real`, `calcularDolares(...)`, `calcularEuros(...)`, `calcularEmMoeda(moeda, arredondamento = ABNT_NBR_5891, precisao = MathContext.DECIMAL128, bloco)`
- `TabelaProgressiva`:
  - DSL `tabelaProgressivaReais(bloco: EscopoTabelaProgressiva<Real>.() -> Unit): TabelaProgressiva<Real>` (e equivalentes `Dolares`, `Euros`, `EmMoeda`), lançando `IllegalArgumentException` caso `faixaAcima(...)` não seja chamada exatamente uma vez no bloco.
