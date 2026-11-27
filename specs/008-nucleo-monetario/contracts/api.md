# Contrato Público · Spec 008 — Núcleo monetário, rateio e câmbio

## 1. Java · `brasil4j-core` (`io.github.brasil4j.financas`)

Nenhum tipo deste pacote implementa `java.io.Serializable`. Todos os parâmetros nulos ou fora de faixa lançam `IllegalArgumentException` (exceto `emUnidadesMinimas()` / `getCentavos()`, que lançam `ArithmeticException` se o valor exceder `Long.MAX_VALUE` / `Long.MIN_VALUE`).

### 1.1. `Moeda` (`public interface`)

| Assinatura | Contrato |
| --- | --- |
| `Moeda BRL`, `Moeda USD`, `Moeda EUR` | Constantes públicas imutáveis: `("BRL", 2, "R$")`, `("USD", 2, "US$")`, `("EUR", 2, "€")`. |
| `String getCodigo()` | Código identificador ASCII visível (`1..16` caracteres, sem espaços). |
| `int getCasasDecimais()` | Quantidade de casas decimais (`0..18`). |
| `default String getSimbolo()` | Símbolo textual não vazio (`1..16` caracteres sem controle); padrão retorna `getCodigo()`. |
| `static Moeda iso(String codigo, int casasDecimais)` | Valida `codigo` `[A-Z]{3}` e `casasDecimais` em `0..18`; símbolo igual a `codigo`. |
| `static Moeda of(String codigo, int casasDecimais, String simbolo)` | Cria instância imutável validando `Moeda.codigo`, `Moeda.casasDecimais` e `Moeda.simbolo`. |
| `static Moeda fromJdkCurrency(java.util.Currency currency, int casasDecimais)` | Adapta `Currency` não nula do JDK com `casasDecimais` explícitas (`0..18`) e símbolo obtido em `Locale.ROOT`. |
| `static boolean mesmaMoeda(Moeda a, Moeda b)` | Retorna `true` se ambas forem não nulas, tiverem `codigo` ASCII válido e `casasDecimais` (`0..18`) idênticos (sem validar nem comparar `simbolo`); retorna `false` para `null`, código/casas inválidos ou moedas distintas. |

### 1.2. `MetodoArredondamento` (`@FunctionalInterface public interface`) e `PoliticaResiduo` (`public enum`)

| Tipo / Membro | Contrato |
| --- | --- |
| `MetodoArredondamento.ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`, `PARA_CIMA`, `TETO`, `PISO` | Constantes imutáveis implementadas por enum interno (`HALF_EVEN`, `HALF_UP`, `DOWN`, `UP`, `CEILING`, `FLOOR`). |
| `BigDecimal arredondar(BigDecimal valor, int casasDecimais)` | Escalona `valor` não nulo para `casasDecimais` (`0..18`). Deve devolver `FLOOR` ou `CEILING` de `valor` na escala alvo (`scale() == casasDecimais`), com zero sem sinal negativo. |
| `static MetodoArredondamento fromRoundingMode(RoundingMode modo)` | Adapta qualquer `RoundingMode` exceto `null` e `UNNECESSARY` (ambos lançam `IllegalArgumentException`). |
| `PoliticaResiduo.PRIMEIRAS_PARCELAS`, `ULTIMAS_PARCELAS` | Define onde distribuir as unidades mínimas residuais no parcelamento igualitário. |

### 1.3. `Dinheiro` (`public abstract class`, construtor `package-private`)

Selada no pacote para `Real`, `Dolar`, `Euro` e `QuantiaMonetaria`. Limites de entrada: texto `<= 64` chars na gramática `[+-]?[0-9]+(\.[0-9]+)?`; `BigDecimal` com `precision() <= 38` e `scale()` em `[-18, 38]`.

| Assinatura | Contrato |
| --- | --- |
| `static Dinheiro de(String valor, Moeda moeda)` / `(String, Moeda, MetodoArredondamento)` | Interpreta texto decimal, arredonda na escala da moeda (padrão `ABNT_NBR_5891`) e devolve `Real` para `("BRL", 2)`, `Dolar` para `("USD", 2)`, `Euro` para `("EUR", 2)` ou `QuantiaMonetaria` para as demais. |
| `static Dinheiro de(BigDecimal valor, Moeda moeda)` / `(BigDecimal, Moeda, MetodoArredondamento)` | Constrói a partir de `BigDecimal` validando limites de precisão/escala. |
| `static Dinheiro exato(String valor, Moeda moeda)` / `(String, Moeda, MetodoArredondamento)` / `(BigDecimal, Moeda)` / `(BigDecimal, Moeda, MetodoArredondamento)` | Rejeita entradas cuja escala fornecida exceda `moeda.getCasasDecimais()`. |
| `static Dinheiro deInteiro(long unidadesPrincipais, Moeda moeda)` / `(long, Moeda, MetodoArredondamento)` | Constrói `BigDecimal.valueOf(unidadesPrincipais).setScale(casasDecimais)`. |
| `static Dinheiro deUnidadesMinimas(long unidadesMinimas, Moeda moeda)` / `(long, Moeda, MetodoArredondamento)` | Constrói `BigDecimal.valueOf(unidadesMinimas, casasDecimais)`. |
| `static Dinheiro zero(Moeda moeda)` | Retorna zero canônico na escala da moeda com `ABNT_NBR_5891`. |
| `final Moeda getMoeda()`, `final BigDecimal getValor()`, `final MetodoArredondamento getArredondamento()` | Acessores finais imutáveis (`Real.getMoeda()` é sempre `Moeda.BRL`, etc.). |
| `final long emUnidadesMinimas()` | Converte `valor.movePointRight(casasDecimais).longValueExact()`. Lança `ArithmeticException` em *overflow*. |
| `final boolean isZero()`, `final boolean isPositivo()`, `final boolean isNegativo()` | Predicados de sinal sobre `valor`. |
| `abstract Dinheiro abs()`, `abstract Dinheiro negar()`, `abstract Dinheiro comArredondamento(MetodoArredondamento arred)` | Transformações unárias preservando o tipo concreto. `abs` e `negar` preservam `getArredondamento()`. |
| `Dinheiro somarMesmaMoeda(Dinheiro outro)`, `Dinheiro subtrairMesmaMoeda(Dinheiro outro)` | Exigem mesma moeda `(codigo, casasDecimais)`; preservam `getArredondamento()` do receptor (`this`). |
| `abstract Dinheiro multiplicar(long fator)` | Multiplicação exata por inteiro preservando `getArredondamento()`. |
| `abstract Dinheiro multiplicar(BigDecimal fator)` / `multiplicar(BigDecimal fator, MetodoArredondamento arred)` | Multiplicação exata seguida de arredondamento único; a sobrecarga com `arred` retém `arred` no resultado. |
| `abstract Dinheiro dividir(long divisor)` / `dividir(long divisor, MetodoArredondamento arred)` | Divisão com arredondamento único nos modos padrão. A política customizada recebe o quociente exato quando terminante ou a aproximação descrita abaixo. Divisor `0` lança `IllegalArgumentException`; a sobrecarga com `arred` retém `arred` no resultado. |
| `final int compararMesmaMoeda(Dinheiro outro)` | Compara montantes de mesma moeda; lança `IllegalArgumentException` se moedas divergirem ou `outro == null`. |
| `abstract List<? extends Dinheiro> ratear(int parcelas)` / `ratear(int parcelas, PoliticaResiduo politica)` | Parcela em `1..10_000` partes imutáveis preservando `getArredondamento()` e soma exata (padrão `PRIMEIRAS_PARCELAS`). |
| `abstract List<? extends Dinheiro> alocar(List<BigDecimal> pesos)` | Aloca proporcionalmente pelo método exato dos maiores restos (Hamilton, até `10_000` pesos `>= 0` com soma `> 0`), preservando `getArredondamento()` e soma exata. |
| `Dinheiro converter(TaxaCambio taxa)` / `converter(TaxaCambio taxa, MetodoArredondamento arred)` | Exige `Moeda.mesmaMoeda(this.getMoeda(), taxa.getMoedaOrigem())` e converte para `taxa.getMoedaDestino()` preservando `this.getArredondamento()` (ou retendo `arred`). |
| `final boolean equals(Object o)`, `final int hashCode()`, `final String toString()` | Igualdade por `(moeda.codigo, moeda.casasDecimais, valor)` (`arredondamento` não compõe `equals`, documentado em JavaDoc); `toString()` no formato `"<CODIGO> <VALOR>"`. |

Na divisão por inteiro e na divisão cambial inversa com política customizada, o callback recebe o quociente decimal exato quando a expansão termina. Para expansão infinita, o quociente é truncado em direção a zero na escala monetária de destino mais 34 casas. Havendo resto, acrescenta-se um dígito `1` com o sinal do quociente na próxima casa. Políticas que decidem sobre limiares além da precisão dessa representação podem divergir da decisão sobre o quociente matemático.

### 1.4. `Real`, `Dolar`, `Euro` e `QuantiaMonetaria` (`public final class extends Dinheiro`)

- `Real extends Dinheiro implements Comparable<Real>`
- `Dolar extends Dinheiro implements Comparable<Dolar>`
- `Euro extends Dinheiro implements Comparable<Euro>`
- `QuantiaMonetaria extends Dinheiro implements Comparable<QuantiaMonetaria>` (`compareTo` exige mesma moeda, lançando `IllegalArgumentException` se misturadas, documentado para coleções ordenadas).

Para cada tipo dedicado `T` em `{Real, Dolar, Euro}`:
- Constante e fábricas: `T.ZERO`, `T.de(String)`, `T.de(String, MetodoArredondamento)`, `T.de(BigDecimal)`, `T.de(BigDecimal, MetodoArredondamento)`, `T.deInteiro(long)` / `T.deInteiro(long, MetodoArredondamento)`, `T.exato(String)` / `T.exato(String, MetodoArredondamento)` / `T.exato(BigDecimal)` / `T.exato(BigDecimal, MetodoArredondamento)`, `T.deCentavos(long)` / `T.deCentavos(long, MetodoArredondamento)`.
- Operações tipadas e covariantes: `T somar(T)`, `T subtrair(T)`, `T abs()`, `T negar()`, `T comArredondamento(MetodoArredondamento)`, `T multiplicar(long)`, `T multiplicar(BigDecimal)`, `T multiplicar(BigDecimal, MetodoArredondamento)`, `T dividir(long)`, `T dividir(long, MetodoArredondamento)`, `long getCentavos()`, `List<T> ratear(int)`, `List<T> ratear(int, PoliticaResiduo)`, `List<T> alocar(List<BigDecimal>)`.
- Atalhos cambiais bidirecionais por par:
  - Em `Real`: `Dolar paraDolar(TaxaCambio)` / `(TaxaCambio, MetodoArredondamento)` (aceita taxa `BRL -> USD` ou `USD -> BRL`!) e `Euro paraEuro(TaxaCambio)` / `(TaxaCambio, MetodoArredondamento)` (aceita `BRL -> EUR` ou `EUR -> BRL`!).
  - Em `Dolar`: `Real paraReal(...)` (`USD <-> BRL`) e `Euro paraEuro(...)` (`USD <-> EUR`).
  - Em `Euro`: `Real paraReal(...)` (`EUR <-> BRL`) e `Dolar paraDolar(...)` (`EUR <-> USD`).

Para `QuantiaMonetaria`:
- Fábricas `de(String/BigDecimal, Moeda)` e `(..., MetodoArredondamento)`, `exato(...)`, `deInteiro(...)`, `deUnidadesMinimas(...)` (rejeitando os pares reservados `BRL/2`, `USD/2`, `EUR/2`).
- Operações tipadas e covariantes completas: `QuantiaMonetaria somar(QuantiaMonetaria)`, `subtrair(QuantiaMonetaria)`, `abs()`, `negar()`, `comArredondamento(...)`, `multiplicar(...)`, `dividir(...)`, `List<QuantiaMonetaria> ratear(...)`, `List<QuantiaMonetaria> alocar(...)`.

### 1.5. `TaxaCambio` (`public final class`)

| Assinatura | Contrato |
| --- | --- |
| `static TaxaCambio de(Moeda origem, Moeda destino, String cotacao)` / `(Moeda, Moeda, BigDecimal)` | Exige moedas distintas (`!Moeda.mesmaMoeda(origem, destino)`) e `cotacao > 0` dentro dos limites de precisão/escala. Representa `1 origem = cotacao destino`. |
| `static TaxaCambio umDolarEmReais(String cotacao)` / `(BigDecimal)` | Atalho para `TaxaCambio.de(Moeda.USD, Moeda.BRL, cotacao)`. |
| `static TaxaCambio umEuroEmReais(String cotacao)` / `(BigDecimal)` | Atalho para `TaxaCambio.de(Moeda.EUR, Moeda.BRL, cotacao)`. |
| `Moeda getMoedaOrigem()`, `Moeda getMoedaDestino()` | Moedas efetivas da direção atual. |
| `BigDecimal getCotacaoBase()`, `boolean isInvertida()` | Cotação exata informada na criação (escala `>= 0`, sem notação científica) e indicador se a taxa opera por divisão exata (`1 destino = cotacaoBase origem`). |
| `BigDecimal getFatorAproximado(MathContext mc)` | Retorna `cotacaoBase.round(mc)` quando direta ou `BigDecimal.ONE.divide(cotacaoBase, mc)` quando invertida (`mc != null` e, quando invertida, `mc.getPrecision() > 0`). |
| `TaxaCambio inverter()` | Inversão exata e involutiva (`t.inverter().inverter().equals(t)` é sempre `true`). |
| `Dinheiro converter(Dinheiro valor)` / `(Dinheiro, MetodoArredondamento)` | Exige `Moeda.mesmaMoeda(valor.getMoeda(), getMoedaOrigem())`; multiplica por `cotacaoBase` quando direta ou divide de forma exata por `cotacaoBase` quando invertida. |
| `equals`, `hashCode`, `toString` | Igualdade canônica exata; `toString()` retorna `"1 <ORIGEM> = <COTACAO_BASE> <DESTINO>"` (direta) ou `"<COTACAO_BASE> <ORIGEM> = 1 <DESTINO>"` (invertida). |

---
## 2. Kotlin · `brasil4j-kotlin` (`io.github.brasil4j.kotlin.financas`)

Extensões idiomáticas em `Financas.kt` delegando integralmente ao núcleo Java:

- **Construção sem ambiguidade de centavos:**
  - `String.reais(arredondamento: MetodoArredondamento = ABNT_NBR_5891): Real`, `BigDecimal.reais(...): Real`, `Long.reaisInteiros(...): Real`, `Int.reaisInteiros(...): Real`, `Long.centavosReais(...): Real`.
  - Equivalentes `.dolares(...)`, `.dolaresInteiros(...)`, `.centavosDolares(...)`, `.euros(...)`, `.eurosInteiros(...)`, `.centavosEuros(...)` e `String.emMoeda(moeda: Moeda, arredondamento: MetodoArredondamento = ABNT_NBR_5891): Dinheiro`, `BigDecimal.emMoeda(...): Dinheiro`.
- **Operadores em `Real`, `Dolar`, `Euro`, `QuantiaMonetaria` e `Dinheiro`:**
  - `+` e `-` tipados para `Real`, `Dolar`, `Euro`, `QuantiaMonetaria` e `Dinheiro` (`somarMesmaMoeda`/`subtrairMesmaMoeda`).
  - `-valor` (`unaryMinus`), `valor * n`, `n * valor` (comutativo para `Int` e `Long`!), `valor * fatorBigDecimal`, `valor / divisor` (`Int` e `Long`, usando a política retida da instância, documentado no KDoc).
  - Atalho Kotlin `Dinheiro.alocarPorPesos(vararg pesos: Long)` / `Real.alocarPorPesos(vararg pesos: Long)` (e equivalentes em `Dolar`, `Euro`, `QuantiaMonetaria`).
