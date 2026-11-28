package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Contrato reutilizável para implementações da interface pública {@link CalculoFinanceiro} (TQ-3):
 * determinismo frente ao mesmo {@link MathContext} e integração limpa com {@link CalculadoraFinanceira}.
 */
public abstract class CalculoFinanceiroContractTest {

    /** @return instância sob teste */
    protected abstract CalculoFinanceiro calculo();

    /** @return valor esperado quando avaliado em {@link Real} com {@link MetodoArredondamento#ABNT_NBR_5891} */
    protected abstract Real esperadoEmReaisPadrao();

    @Test
    void produzBigDecimalPadraoDeterministicoParaMesmoMathContext() {
        BigDecimal primeiro = calculo().calcular(MathContext.DECIMAL128);
        BigDecimal segundo = calculo().calcular(MathContext.DECIMAL128);
        assertNotNull(primeiro);
        assertEquals(BigDecimal.class, primeiro.getClass());
        assertEquals(primeiro, segundo);
    }

    @Test
    void integraComCalculadoraFinanceiraPadrao() {
        assertEquals(esperadoEmReaisPadrao(), CalculadoraFinanceira.PADRAO.calcularReais(calculo()));
    }
}
