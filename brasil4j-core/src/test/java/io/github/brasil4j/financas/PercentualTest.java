package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PercentualTest {

    @Test
    void fabricasBordasDeEscala40AlgebraPuraEToStringSemExpoente() {
        Percentual p1 = Percentual.de("17.5");
        Percentual p2 = Percentual.deFator("0.175");
        Percentual p3 = Percentual.dePontosBase(1750L);
        Percentual p4 = Percentual.de(new BigDecimal("17.500"));
        Percentual p5 = Percentual.deFator(new BigDecimal("0.1750"));

        assertEquals(p1, p1);
        assertEquals(p1, p2);
        assertEquals(p1, p3);
        assertEquals(p1, p4);
        assertEquals(p1, p5);
        org.junit.jupiter.api.Assertions.assertNotEquals(p1, "17.5%");
        org.junit.jupiter.api.Assertions.assertNotEquals(p1, Percentual.CEM);
        org.junit.jupiter.api.Assertions.assertNotEquals(0, p1.hashCode());
        assertFalse(Percentual.ZERO.isPositivo());
        assertFalse(Percentual.ZERO.isNegativo());
        assertTrue(p1.isPositivo());
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals(new BigDecimal("17.5"), p1.getPontosPercentuais());
        assertEquals(new BigDecimal("0.175"), p1.getFator());
        assertEquals(new BigDecimal("1.175"), p1.getFatorAcrescimo());
        assertEquals(new BigDecimal("0.825"), p1.getFatorDesconto());
        assertEquals("17.5%", p1.toString());

        // Inteiro grande sem notação científica (1000% -> "1000%", nunca "1E+3%")
        Percentual milPorCento = Percentual.de(1000L);
        assertEquals(new BigDecimal("1000"), milPorCento.getPontosPercentuais());
        assertEquals("1000%", milPorCento.toString());
        assertSame(Percentual.ZERO, Percentual.de("0.00"));
        assertSame(Percentual.CEM, Percentual.de(100L));

        // Borda de escala 38 e 40 no fator canônico (Achado #2)
        String trintaEOitoCasas = "0.00000000000000000000000000000000000001"; // 38 casas -> fator escala 40!
        Percentual escala40 = Percentual.de(trintaEOitoCasas);
        assertEquals(40, escala40.getFator().scale());
        assertEquals(Real.ZERO, escala40.aplicarSobre(Real.de("100.00")));
        assertEquals(Real.de("0.01"), escala40.aplicarSobre(Real.de("100.00"), MetodoArredondamento.TETO));

        // Fator acima de escala 40 rejeitado
        BigDecimal fatorEscala41 = new BigDecimal("1E-41");
        assertThrows(IllegalArgumentException.class, () -> Percentual.deFator(fatorEscala41));
        assertThrows(IllegalArgumentException.class, () -> Percentual.de("17,5")); // vírgula rejeitada
        assertThrows(IllegalArgumentException.class, () -> Percentual.de((String) null));
        assertThrows(IllegalArgumentException.class, () -> Percentual.de((BigDecimal) null));

        // Álgebra pura e sinais
        Percentual neg = p1.negar();
        assertTrue(neg.isNegativo());
        assertFalse(neg.isPositivo());
        assertEquals(p1, neg.abs());
        assertTrue(Percentual.ZERO.isZero());
        assertEquals(Percentual.de("20"), p1.somar(Percentual.de("2.5")));
        assertEquals(Percentual.de("15"), p1.subtrair(Percentual.de("2.5")));
        assertTrue(p1.compareTo(Percentual.de("18")) < 0);

        // Estouro de precisão > 38 na soma de dois percentuais de escalas distantes
        Percentual muitoPequeno = Percentual.de(trintaEOitoCasas);
        assertThrows(IllegalArgumentException.class, () -> Percentual.CEM.somar(muitoPequeno));

        // Long.MIN_VALUE em de(long) e dePontosBase(long) + negar() sem overflow de 64 bits (M1)
        Percentual minPontos = Percentual.de(Long.MIN_VALUE);
        assertTrue(minPontos.isNegativo());
        assertEquals(new BigDecimal("9223372036854775808"), minPontos.negar().getPontosPercentuais());
        Percentual minBps = Percentual.dePontosBase(Long.MIN_VALUE);
        assertEquals(new BigDecimal("92233720368547758.08"), minBps.negar().getPontosPercentuais());
        assertEquals(new BigDecimal("922337203685477.5808"), minBps.negar().getFator());

        // Proteção anti-DoS em deFator(BigDecimal) para escalas extremas e bitLength gigante (A1)
        assertTimeoutPreemptively(
                Duration.ofMillis(200),
                () -> {
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> Percentual.deFator(new BigDecimal("1E+999999999")));
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> Percentual.deFator(new BigDecimal("1E-999999999")));
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> Percentual.deFator(new BigDecimal("0E+999999999")));
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> Percentual.deFator(new BigDecimal("1E+38")));
                    BigDecimal unscaledGigante = new BigDecimal(BigInteger.ONE.shiftLeft(5_000_000), 10);
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> Percentual.deFator(unscaledGigante));
                });

        // Bordas exatas aceitas/rejeitadas em deFator e nulos nas operações monetárias
        assertEquals(new BigDecimal("10000000000000000000000000000000000000"), Percentual.deFator(new BigDecimal("1E+37")).getFator());
        assertThrows(
                IllegalArgumentException.class,
                () -> Percentual.deFator(new BigDecimal("0.5") {
                    @Override
                    public int scale() {
                        return 1;
                    }
                }));
        assertThrows(IllegalArgumentException.class, () -> p1.aplicarSobre(null));
        assertThrows(IllegalArgumentException.class, () -> p1.aplicarSobre(null, MetodoArredondamento.FINANCEIRO));
        assertThrows(IllegalArgumentException.class, () -> p1.acrescentarEm(null));
        assertThrows(IllegalArgumentException.class, () -> p1.descontarDe(null));
        assertThrows(IllegalArgumentException.class, () -> p1.calcularBasePorDentro(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> p1.calcularBasePorDentro(null, MetodoArredondamento.FINANCEIRO));
    }

    @Test
    void operacoesMonetariasGenericasPreservamSubtipoEReconciliacaoContabil() {
        List<MetodoArredondamento> modos = Arrays.asList(
                MetodoArredondamento.ABNT_NBR_5891,
                MetodoArredondamento.FINANCEIRO,
                MetodoArredondamento.TRUNCAMENTO,
                MetodoArredondamento.PARA_CIMA,
                MetodoArredondamento.TETO,
                MetodoArredondamento.PISO,
                MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_DOWN));

        // Caso crítico de paridade ABNT: R$ 0,01 com 50% (0.01 * 0.5 = 0.005 -> ABNT = 0.00; 0.01 * 1.5 = 0.015 -> 0.02!)
        Percentual cinquentaPorCento = Percentual.de(50L);
        for (MetodoArredondamento modo : modos) {
            for (long centavos : new long[] {-101L, -3L, -1L, 0L, 1L, 3L, 101L, 9999L}) {
                Real base = Real.deCentavos(centavos, modo);
                Real parcela = cinquentaPorCento.aplicarSobre(base);
                Real acrescido = cinquentaPorCento.acrescentarEm(base);
                Real descontado = cinquentaPorCento.descontarDe(base);

                assertSame(modo, parcela.getArredondamento());
                assertSame(modo, acrescido.getArredondamento());
                assertSame(modo, descontado.getArredondamento());
                assertEquals(parcela, acrescido.subtrair(base));
                assertEquals(parcela, base.subtrair(descontado));
            }
        }

        // Preservação estática do subtipo em Dolar, Euro, QuantiaMonetaria e Dinheiro
        Percentual dezPorCento = Percentual.de(10L);
        Dolar d = dezPorCento.acrescentarEm(Dolar.de("50.00"), MetodoArredondamento.FINANCEIRO);
        assertEquals(Dolar.de("55.00"), d);
        assertSame(MetodoArredondamento.FINANCEIRO, d.getArredondamento());

        Euro e = dezPorCento.descontarDe(Euro.de("80.00"));
        assertEquals(Euro.de("72.00"), e);

        QuantiaMonetaria kwd = dezPorCento.aplicarSobre(QuantiaMonetaria.de("1.250", Moeda.iso("KWD", 3)));
        assertEquals(QuantiaMonetaria.de("0.125", Moeda.iso("KWD", 3)), kwd);

        Dinheiro polimorfico = dezPorCento.aplicarSobre((Dinheiro) Real.de("30.00"));
        assertEquals(Real.de("3.00"), polimorfico);
    }

    @Test
    void grossUpPorDentroReversibilidadeIncondicionalNosSeisModosERejeicoesDeDominio() {
        // Exemplo clássico: líquido R$ 825,00 com 17,5% por dentro -> base R$ 1.000,00
        Percentual carga = Percentual.de("17.5");
        Real base = carga.calcularBasePorDentro(Real.de("825.00"));
        assertEquals(Real.de("1000.00"), base);
        assertEquals(Real.de("825.00"), carga.descontarDe(base));

        // 0% por dentro é identidade:
        assertEquals(Real.de("123.45"), Percentual.ZERO.calcularBasePorDentro(Real.de("123.45")));

        // Fora de [0%, 100%) deve falhar:
        assertThrows(
                IllegalArgumentException.class,
                () -> Percentual.de("-0.01").calcularBasePorDentro(Real.de("100.00")));
        assertThrows(
                IllegalArgumentException.class,
                () -> Percentual.CEM.calcularBasePorDentro(Real.de("100.00")));
        assertThrows(
                IllegalArgumentException.class,
                () -> Percentual.de("120").calcularBasePorDentro(Real.de("100.00")));

        // Propriedade (Achado #6 + M2): descontarDe(calcularBasePorDentro(L, m), m) == L nos 7 modos padrão!
        List<MetodoArredondamento> seteModos = Arrays.asList(
                MetodoArredondamento.ABNT_NBR_5891,
                MetodoArredondamento.FINANCEIRO,
                MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_DOWN),
                MetodoArredondamento.TRUNCAMENTO,
                MetodoArredondamento.PARA_CIMA,
                MetodoArredondamento.TETO,
                MetodoArredondamento.PISO);
        String[] aliquotasTeste = {"0.01", "1", "9.25", "17.5", "27.5", "30", "33.33", "60", "99.99"};
        long[] valoresCentavos = {-10001L, -1L, 0L, 1L, 2L, 3L, 99L, 82500L, 1234567L};

        for (String aliq : aliquotasTeste) {
            Percentual p = Percentual.de(aliq);
            for (MetodoArredondamento m : seteModos) {
                for (long c : valoresCentavos) {
                    Real liquido = Real.deCentavos(c, m);
                    Real brutoPorDentro = p.calcularBasePorDentro(liquido, m);
                    Real recomposto = p.descontarDe(brutoPorDentro, m);
                    assertEquals(
                            liquido,
                            recomposto,
                            () -> "Falha reversibilidade p=" + p + " m=" + m + " L=" + liquido);
                }
            }
        }
    }
}
