package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Política determinística de arredondamento decimal para valores monetários.
 *
 * <p>Políticas customizadas fornecidas pelo consumidor devem devolver uma instância padrão de
 * {@link BigDecimal} não nula com {@code scale() == casasDecimais} igual a um dos dois vizinhos
 * imediatos ({@link RoundingMode#FLOOR} ou {@link RoundingMode#CEILING}) do valor recebido.
 * Em divisões terminantes, o núcleo entrega o quociente decimal exato; em divisões não
 * terminantes, entrega um valor intermediário com 34 casas adicionais além de {@code casasDecimais}
 * mais um dígito <i>sticky</i> assinado.
 */
@FunctionalInterface
public interface MetodoArredondamento {
    /**
     * Arredondamento conforme a norma ABNT NBR 5891: empate exato em {@code 5} arredonda para
     * o algarismo par mais próximo ({@link RoundingMode#HALF_EVEN} sobre decimais exatos).
     */
    MetodoArredondamento ABNT_NBR_5891 = ModoArredondamentoPadrao.ABNT_NBR_5891;

    /** Arredondamento comercial/financeiro: empate em {@code 5} afasta de zero ({@link RoundingMode#HALF_UP}). */
    MetodoArredondamento FINANCEIRO = ModoArredondamentoPadrao.FINANCEIRO;

    /** Truncamento das casas excedentes em direção a zero ({@link RoundingMode#DOWN}). */
    MetodoArredondamento TRUNCAMENTO = ModoArredondamentoPadrao.TRUNCAMENTO;

    /** Arredondamento afastando de zero para qualquer fração não nula ({@link RoundingMode#UP}). */
    MetodoArredondamento PARA_CIMA = ModoArredondamentoPadrao.PARA_CIMA;

    /** Arredondamento em direção ao infinito positivo ({@link RoundingMode#CEILING}). */
    MetodoArredondamento TETO = ModoArredondamentoPadrao.TETO;

    /** Arredondamento em direção ao infinito negativo ({@link RoundingMode#FLOOR}). */
    MetodoArredondamento PISO = ModoArredondamentoPadrao.PISO;

    /**
     * Escalona {@code valor} para {@code casasDecimais}, devolvendo um dos vizinhos imediatos na escala alvo.
     *
     * @param valor instância padrão não nula de {@link BigDecimal}
     * @param casasDecimais quantidade de casas decimais alvo ({@code 0..18})
     * @return valor arredondado com {@code scale() == casasDecimais}
     * @throws IllegalArgumentException se {@code valor} for nulo, subclasse não confiável, fora dos limites ou {@code casasDecimais} fora de {@code 0..18}
     */
    BigDecimal arredondar(BigDecimal valor, int casasDecimais);

    /**
     * Obtém a política equivalente a um {@link RoundingMode} do JDK, exceto {@link RoundingMode#UNNECESSARY}.
     *
     * @param modo modo do JDK não nulo e diferente de {@code UNNECESSARY}
     * @return política correspondente de arredondamento
     * @throws IllegalArgumentException se {@code modo} for nulo ou {@code UNNECESSARY}
     */
    static MetodoArredondamento fromRoundingMode(RoundingMode modo) {
        if (modo == null) {
            throw new IllegalArgumentException("O modo de arredondamento não pode ser nulo.");
        }
        switch (modo) {
            case HALF_EVEN:
                return ABNT_NBR_5891;
            case HALF_UP:
                return FINANCEIRO;
            case DOWN:
                return TRUNCAMENTO;
            case UP:
                return PARA_CIMA;
            case CEILING:
                return TETO;
            case FLOOR:
                return PISO;
            case HALF_DOWN:
                return ModoArredondamentoPadrao.MEIO_PARA_BAIXO;
            case UNNECESSARY:
            default:
                throw new IllegalArgumentException("RoundingMode.UNNECESSARY não define política de arredondamento.");
        }
    }
}
