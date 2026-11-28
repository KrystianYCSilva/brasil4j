# Quickstart · Spec 011 — Calculadora financeira, percentuais e tabela progressiva

## Pré-requisitos
- JDK 8 ou JDK 21 e Maven Wrapper na raiz.

## Verificação em Java 8
```java
// 1. CalculadoraFinanceira com precisão intermediária DECIMAL128 e arredondamento único
Real proRata = CalculadoraFinanceira.emReais(MetodoArredondamento.FINANCEIRO, mc ->
        new BigDecimal("1000.00")
                .multiply(new BigDecimal("17"), mc)
                .divide(new BigDecimal("30"), mc));

// 2. Percentual explícito e gross-up ("por dentro")
Percentual carga = Percentual.de("17.5");
Real precoComCargaPorDentro = carga.calcularBasePorDentro(Real.de("825.00")); // BRL 1000.00

// 3. TabelaProgressiva parametrizada pelo consumidor (sem lei embutida)
TabelaProgressiva<Real> tabelaComissao = TabelaProgressiva.emReais()
        .faixaAte("5000.00", Percentual.ZERO)
        .faixaAte("15000.00", Percentual.de("5"))
        .faixaAcima(Percentual.de("8"));
ApuracaoProgressiva<Real> apuracao = tabelaComissao.apurar(Real.de("20000.00")); // BRL 900.00
```

## Comandos de validação
```powershell
.\mvnw.cmd clean verify
```
