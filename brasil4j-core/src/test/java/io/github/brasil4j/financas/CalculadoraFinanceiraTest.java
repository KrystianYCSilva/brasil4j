package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraFinanceiraTest {

    @Test
    void limitesDeMathContextPropagacaoDeExcecoesERejeicoesAntiDos() {
        // MathContext inválido: nulo, precision == 0, precision > 90 ou UNNECESSARY
        assertThrows(IllegalArgumentException.class, () -> CalculadoraFinanceira.de(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891, MathContext.UNLIMITED));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.de(
                        MetodoArredondamento.ABNT_NBR_5891, new MathContext(91, RoundingMode.HALF_EVEN)));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.de(
                        MetodoArredondamento.ABNT_NBR_5891, new MathContext(16, RoundingMode.UNNECESSARY)));

        // Callback nulo, retorno nulo ou subclasse maliciosa de BigDecimal
        assertThrows(IllegalArgumentException.class, () -> CalculadoraFinanceira.emReais(null));
        assertThrows(IllegalArgumentException.class, () -> CalculadoraFinanceira.emReais(mc -> null));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.emReais(mc -> new BigDecimal("10.00") {
                    @Override
                    public int scale() {
                        return 2;
                    }
                }));

        // Intermediário fora de escala [-60, 180] ou precisão > 180 ou resultado final > 38 dígitos
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.emReais(mc -> new BigDecimal("1E-181")));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.emReais(mc -> new BigDecimal("1E+61")));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.emReais(mc -> new BigDecimal(BigInteger.TEN.pow(200), 100)));
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculadoraFinanceira.emReais(mc -> new BigDecimal(BigInteger.TEN.pow(40), 0)));

        // Exceções aritméticas lançadas pela expressão propagam-se sem embrulho
        assertThrows(
                ArithmeticException.class,
                () -> CalculadoraFinanceira.emReais(mc -> BigDecimal.ONE.divide(new BigDecimal("3"))));
    }

    @Test
    void avaliacaoEmTodasAsMoedasZeroSemSinalECaracterizacaoDeDizimasEmDecimal128() {
        // Pró-rata em 34 dígitos com arredondamento monetário único no final: 1000 * 17 / 30 = 566.666...
        Real proRata = CalculadoraFinanceira.emReais(MetodoArredondamento.FINANCEIRO, mc ->
                new BigDecimal("1000.00").multiply(new BigDecimal("17"), mc).divide(new BigDecimal("30"), mc));
        assertEquals(Real.de("566.67"), proRata);
        assertSame(MetodoArredondamento.FINANCEIRO, proRata.getArredondamento());

        // Caracterização T004 (Achado #1): 100 * (1/3 em DECIMAL128) * 3 = 99.99999999999999999999999999999999
        CalculoFinanceiro tercoVezesTres = mc ->
                BigDecimal.ONE.divide(new BigDecimal("3"), mc)
                        .multiply(new BigDecimal("100.00"))
                        .multiply(new BigDecimal("3"));
        assertEquals(Real.de("100.00"), CalculadoraFinanceira.emReais(tercoVezesTres));
        assertEquals(
                Real.de("99.99"),
                CalculadoraFinanceira.emReais(MetodoArredondamento.TRUNCAMENTO, tercoVezesTres));

        // Produto intermediário de dois operandos de 45 dígitos (90 dígitos > limite público 38) arredondado no fim:
        MathContext mc45 = new MathContext(45, RoundingMode.HALF_EVEN);
        CalculadoraFinanceira calc45 = CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891, mc45);
        Real produtoAltaPrecisao = calc45.calcularReais(mc -> {
            BigDecimal a = BigDecimal.ONE.divide(new BigDecimal("7"), mc);
            return a.multiply(a).multiply(new BigDecimal("4900")); // sem mc -> ~90 dígitos intermediários!
        });
        assertEquals(Real.de("100.00"), produtoAltaPrecisao);

        // Atalhos tipados para Dolar, Euro e roteamento canônico em Moeda aberta + zero sem sinal negativo:
        Dolar emUsd = CalculadoraFinanceira.emDolares(mc -> new BigDecimal("-0.00001"));
        assertEquals("USD 0.00", emUsd.toString());
        assertEquals(Dolar.de("25.50"), CalculadoraFinanceira.emDolares(MetodoArredondamento.FINANCEIRO, mc -> new BigDecimal("25.504")));
        assertEquals(Euro.de("10.25"), CalculadoraFinanceira.emEuros(mc -> new BigDecimal("10.25")));
        assertEquals(
                Euro.de("10.26"),
                CalculadoraFinanceira.emEuros(MetodoArredondamento.TETO, mc -> new BigDecimal("10.251")));

        Moeda kwd = Moeda.iso("KWD", 3);
        Dinheiro emKwd = CalculadoraFinanceira.emMoeda(kwd, MetodoArredondamento.FINANCEIRO, mc -> new BigDecimal("1.2345"));
        assertInstanceOf(QuantiaMonetaria.class, emKwd);
        assertEquals("KWD 1.235", emKwd.toString());

        Dinheiro brlCanonico = CalculadoraFinanceira.emMoeda(Moeda.BRL, mc -> new BigDecimal("50"));
        assertInstanceOf(Real.class, brlCanonico);
        assertEquals(Real.de("50.00"), brlCanonico);

        // Igualdade, hashCode, bordas de MathContext (1 e 90) e toString de CalculadoraFinanceira
        CalculadoraFinanceira c1 = CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891);
        CalculadoraFinanceira cFin = CalculadoraFinanceira.de(MetodoArredondamento.FINANCEIRO);
        CalculadoraFinanceira mc1 =
                CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891, new MathContext(1, RoundingMode.HALF_EVEN));
        CalculadoraFinanceira mc90 =
                CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891, new MathContext(90, RoundingMode.HALF_EVEN));
        assertSame(CalculadoraFinanceira.PADRAO, CalculadoraFinanceira.de(MetodoArredondamento.ABNT_NBR_5891, MathContext.DECIMAL128));
        assertEquals(CalculadoraFinanceira.PADRAO, c1);
        assertEquals(c1, c1);
        assertNotEquals(c1, "outro");
        assertNotEquals(c1, cFin);
        assertNotEquals(c1, mc1);
        assertNotEquals(0, c1.hashCode());
        assertSame(MetodoArredondamento.FINANCEIRO, cFin.getArredondamento());
        assertEquals(Real.de("3.00"), mc1.calcularReais(mc -> new BigDecimal("3.4").round(mc)));
        assertEquals(Real.de("3.40"), mc90.calcularReais(mc -> new BigDecimal("3.40").round(mc)));
        assertEquals(Real.ZERO, CalculadoraFinanceira.emReais(mc -> new BigDecimal("1E-180")));
        assertEquals(Real.ZERO, CalculadoraFinanceira.emReais(mc -> new BigDecimal("0E+60")));
        org.junit.jupiter.api.Assertions.assertTrue(c1.toString().contains("precision=34"));
        assertEquals(CalculadoraFinanceira.PADRAO.hashCode(), c1.hashCode());
        assertNotEquals(CalculadoraFinanceira.PADRAO, calc45);
        assertEquals(mc45, calc45.getPrecisaoIntermediaria());
    }
}
