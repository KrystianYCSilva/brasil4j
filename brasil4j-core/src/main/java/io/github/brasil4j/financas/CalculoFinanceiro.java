package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Expressão matemática financeira avaliada sobre um {@link MathContext} de precisão intermediária
 * controlada antes do arredondamento monetário final realizado por {@link CalculadoraFinanceira}.
 *
 * <p>Exceções lançadas durante a avaliação da expressão (como {@link ArithmeticException})
 * propagam-se diretamente ao chamador sem embrulho.
 */
@FunctionalInterface
public interface CalculoFinanceiro {

    /**
     * Avalia a fórmula financeira utilizando a precisão intermediária informada.
     *
     * @param precisaoIntermediaria contexto matemático intermediário não nulo ({@code 1..90} dígitos)
     * @return valor decimal intermediário padrão não nulo (precisão {@code <= 180}, escala {@code -60..180})
     */
    BigDecimal calcular(MathContext precisaoIntermediaria);
}
