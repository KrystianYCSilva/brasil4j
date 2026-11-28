package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Exercita o contrato {@link CalculoFinanceiroContractTest} com uma implementação concreta
 * estilo consumidor (classe imutável dedicada parametrizada).
 */
class CalculoFinanceiroConsumidorContractTest extends CalculoFinanceiroContractTest {

    private static final class RateioProRataDias implements CalculoFinanceiro {
        private final BigDecimal valorMensal;
        private final int diasUtilizados;
        private final int diasMes;

        RateioProRataDias(BigDecimal valorMensal, int diasUtilizados, int diasMes) {
            this.valorMensal = valorMensal;
            this.diasUtilizados = diasUtilizados;
            this.diasMes = diasMes;
        }

        @Override
        public BigDecimal calcular(MathContext precisaoIntermediaria) {
            return valorMensal
                    .multiply(BigDecimal.valueOf(diasUtilizados), precisaoIntermediaria)
                    .divide(BigDecimal.valueOf(diasMes), precisaoIntermediaria);
        }
    }

    @Override
    protected CalculoFinanceiro calculo() {
        return new RateioProRataDias(new BigDecimal("1000.00"), 17, 30);
    }

    @Override
    protected Real esperadoEmReaisPadrao() {
        return Real.de("566.67");
    }
}
