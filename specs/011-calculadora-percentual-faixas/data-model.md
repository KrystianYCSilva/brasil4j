# Data Model · Spec 011 — Calculadora financeira, percentuais e tabela progressiva

## 1. `CalculoFinanceiro` (`@FunctionalInterface`)
- Função `(MathContext precisaoIntermediaria) -> BigDecimal` (retorno padrão `java.math.BigDecimal` não nulo, `scale` em `[-60, 180]`, `bitLength <= 600`, `precision <= 180`; exceções próprias propagam-se sem embrulho).

## 2. `CalculadoraFinanceira` (imutável, `final`)
- `arredondamento`: `MetodoArredondamento` não nulo (padrão `ABNT_NBR_5891`).
- `precisaoIntermediaria`: `MathContext` não nulo com `precision` em `1..90` e `roundingMode != RoundingMode.UNNECESSARY` (padrão `MathContext.DECIMAL128`).

## 3. `Percentual` (objeto de valor imutável, `final`, `Comparable<Percentual>`)
- `fator`: `BigDecimal` padrão canônico (`precision <= 38`, `scale` em `0..40`, sem zeros finais supérfluos e `scale >= 0`).
- `getPontosPercentuais()`: `fator.movePointRight(2)` normalizado com `scale >= 0` (texto plano sem expoente em `toString()`).
- Restrição de *gross-up* (`calcularBasePorDentro`): `0 <= fator < 1` (`[0%, 100%)`).

## 4. `TabelaProgressiva<T extends Dinheiro>` e entidades de apuração (imutáveis, `final`)
- **`FaixaProgressiva<T>`**:
  - `indice`: `int >= 1`; `limiteInferior`: `T` (`>= 0`); `limiteSuperior`: `Optional<T>` (intervalo `(limiteInferior, limiteSuperior]`, ou `[0, L_1]` na 1ª faixa); `aliquota`: `Percentual` (`>= 0%`); `parcelaADeduzirExata`: `BigDecimal` (`scale >= 0`, aplicável ao cálculo direto em `NO_TOTAL`; pode ser negativa quando uma faixa superior tiver alíquota menor, como `0%` acima de um teto).
- **`TabelaProgressiva<T>`**:
  - `moeda`: `Moeda` canônica; `faixas`: lista imutável (`1..1_000` faixas, última aberta). `Builder<T>` não *thread-safe* e de uso único (lança `IllegalStateException` se chamado após `faixaAcima`).
- **`ModoApuracaoProgressiva`** (`enum`): `NO_TOTAL`, `POR_FAIXA`.
- **`FaixaApurada<T>`**:
  - `faixa`: `FaixaProgressiva<T>`; `baseNaFaixa`: `T` (`>= 0`, retendo `arredondamento` da apuração); `valorExato`: `BigDecimal` (`scale >= 0`); `valorArredondadoIndividual`: `T` (arredondamento isolado da faixa); `valorApurado`: `T` (igual a `valorArredondadoIndividual` em `POR_FAIXA`, ou reconciliado por Hamilton para somar `valorTotal` em `NO_TOTAL`).
- **`ApuracaoProgressiva<T>`**:
  - `baseCalculo`: `T` (`>= 0`); `modo`: `ModoApuracaoProgressiva`; `arredondamento`: `MetodoArredondamento`; `faixas`: `List<FaixaApurada<T>>` (todas as faixas da tabela); `valorExatoTotal`: `BigDecimal` (sempre a soma exata $\sum \text{valorExato}$); `valorTotal`: `T` (sempre igual a $\sum \text{valorApurado}$); `diferencaArredondamento`: `BigDecimal` (`valorTotal.getValor().subtract(valorExatoTotal)`).
