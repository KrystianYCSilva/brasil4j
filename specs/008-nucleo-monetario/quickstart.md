# Quickstart · Spec 008 — Núcleo monetário, rateio e câmbio

## Pré-requisitos

- JDK 8 (`1.8.0_512`) e JDK 21 (`21.0.13`)
- Maven Wrapper (`./mvnw` ou `mvnw.cmd`) na worktree `008-nucleo-monetario`

## Cenários de Verificação Rápida

### 1. Java 8 (`io.github.brasil4j.financas`)

1. **Arredondamento ABNT NBR 5891 vs. Financeiro vs. Truncamento e `"-0.004"`:**
   - `Real.de("10.245", MetodoArredondamento.ABNT_NBR_5891)` → `"BRL 10.24"`
   - `Real.de("10.255", MetodoArredondamento.ABNT_NBR_5891)` → `"BRL 10.26"`
   - `Real.de("10.2450001", MetodoArredondamento.ABNT_NBR_5891)` → `"BRL 10.25"`
   - `Real.de("10.245", MetodoArredondamento.FINANCEIRO)` → `"BRL 10.25"`
   - `Real.de("-0.004", MetodoArredondamento.ABNT_NBR_5891)` → `"BRL 0.00"`
   - `Real.de("-0.004", MetodoArredondamento.PISO)` → `"BRL -0.01"`
2. **Parcelamento e alocação proporcional sem perda de centavos:**
   - `Real.de("100.00").ratear(3)` → `[BRL 33.34, BRL 33.33, BRL 33.33]` (soma exata `BRL 100.00`).
   - `Real.de("100.00").ratear(3, PoliticaResiduo.ULTIMAS_PARCELAS)` → `[BRL 33.33, BRL 33.33, BRL 33.34]`.
   - `Real.de("100.00").alocar(Arrays.asList(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))` → `[BRL 0.00, BRL 33.34, BRL 33.33, BRL 33.33]`.
3. **Conversão cambial exata e involutiva:**
   - `TaxaCambio ptax = TaxaCambio.umDolarEmReais("5.4832");`
   - `Dolar.de("100.00").paraReal(ptax)` → `Real` `"BRL 548.32"`.
   - `Real.de("548.32", MetodoArredondamento.TRUNCAMENTO).paraDolar(ptax)` → `Dolar` `"USD 100.00"` (aceita `USD -> BRL` diretamente ou `ptax.inverter()`, sem perda na divisão exata).

### 2. Kotlin (`io.github.brasil4j.kotlin.financas`)

- `"100.00".reais() + "25.50".reais()` → `BRL 125.50`
- `2 * "50.25".reais()` → `BRL 100.50`
- `"100.00".reais().alocarPorPesos(0, 1, 1, 1)` → `[BRL 0.00, BRL 33.34, BRL 33.33, BRL 33.33]`
- `"548.32".reais().paraDolar(TaxaCambio.umDolarEmReais("5.4832"))` → `USD 100.00`

## Comandos de Validação Completa

```powershell
# 1. Verificação completa em JDK 8 (testes unitários, JAR ITs, JaCoCo e JavaDoc DocLint)
.\mvnw.cmd clean verify

# 2. Verificação completa em JDK 21 (inclui Dokka 2.2.0 para KDoc)
.\mvnw.cmd clean verify

# 3. Verificação de bytecode Java 8 (major version 52) nos dois JARs empacotados
python scripts/check-bytecode.py
```

Consulte assinaturas completas em [contracts/api.md](contracts/api.md) e restrições em [data-model.md](data-model.md).
