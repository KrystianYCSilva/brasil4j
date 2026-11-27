package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DinheiroNucleoTest {

    @Test
    void roteamentoCanonicoSimetriaDeIgualdadeENormalizacaoDeSimbolo() {
        Dinheiro viaFabricaBrl = Dinheiro.de("10.00", Moeda.of("BRL", 2, "Reais"), MetodoArredondamento.FINANCEIRO);
        Real viaReal = Real.de("10", MetodoArredondamento.ABNT_NBR_5891);

        assertInstanceOf(Real.class, viaFabricaBrl);
        assertSame(Moeda.BRL, viaFabricaBrl.getMoeda());
        assertEquals("R$", viaFabricaBrl.getMoeda().getSimbolo());
        assertEquals(viaReal, viaFabricaBrl);
        assertEquals(viaFabricaBrl, viaReal);
        assertEquals(viaReal.hashCode(), viaFabricaBrl.hashCode());
        assertEquals("BRL 10.00", viaReal.toString());

        assertInstanceOf(Dolar.class, Dinheiro.de("5", Moeda.USD));
        assertInstanceOf(Euro.class, Dinheiro.de(new BigDecimal("5.5"), Moeda.EUR));
        assertInstanceOf(Real.class, Dinheiro.zero(Moeda.BRL));
        assertInstanceOf(Dolar.class, Dinheiro.zero(Moeda.USD));
        assertInstanceOf(Euro.class, Dinheiro.zero(Moeda.EUR));

        Moeda jpy = Moeda.iso("JPY", 0);
        Dinheiro ienes = Dinheiro.de("500", jpy);
        assertInstanceOf(QuantiaMonetaria.class, ienes);
        assertEquals("JPY 500", ienes.toString());

        assertThrows(IllegalArgumentException.class, () -> QuantiaMonetaria.de("10.00", Moeda.BRL));
        assertThrows(IllegalArgumentException.class, () -> QuantiaMonetaria.de("10.00", Moeda.USD));
        assertThrows(IllegalArgumentException.class, () -> QuantiaMonetaria.de("10.00", Moeda.EUR));

        Moeda brl4 = Moeda.of("BRL", 4, "R$");
        Dinheiro brlQuatroCasas = Dinheiro.de("10.0000", brl4);
        assertInstanceOf(QuantiaMonetaria.class, brlQuatroCasas);
        assertNotEquals(viaReal, brlQuatroCasas);
        assertNotEquals(brlQuatroCasas, viaReal);
    }

    @Test
    void construcaoArredondadaEstritaInteiraECentavos() {
        assertEquals(new BigDecimal("10.24"), Real.de("10.245", MetodoArredondamento.ABNT_NBR_5891).getValor());
        assertEquals(new BigDecimal("10.26"), Real.de("10.255", MetodoArredondamento.ABNT_NBR_5891).getValor());
        assertEquals(new BigDecimal("10.25"), Real.de("10.2450001", MetodoArredondamento.ABNT_NBR_5891).getValor());
        assertEquals(new BigDecimal("10.25"), Real.de("10.245", MetodoArredondamento.FINANCEIRO).getValor());
        assertEquals(new BigDecimal("10.24"), Real.de("10.249", MetodoArredondamento.TRUNCAMENTO).getValor());

        assertEquals(Real.ZERO, Real.de("-0.004", MetodoArredondamento.ABNT_NBR_5891));
        assertEquals(Real.de("-0.01"), Real.de("-0.004", MetodoArredondamento.PISO));
        assertTrue(Real.ZERO.isZero());
        assertFalse(Real.ZERO.isPositivo());
        assertFalse(Real.ZERO.isNegativo());

        assertEquals(new BigDecimal("10.50"), Real.exato("10.5").getValor());
        assertEquals(new BigDecimal("10.00"), Real.exato(new BigDecimal("10")).getValor());
        assertThrows(IllegalArgumentException.class, () -> Real.exato("10.500"));
        assertThrows(IllegalArgumentException.class, () -> Real.exato("10.555"));
        assertThrows(IllegalArgumentException.class, () -> Real.exato(new BigDecimal("10.555")));

        Moeda jpy = Moeda.iso("JPY", 0);
        assertEquals(new BigDecimal("500"), Dinheiro.exato("500", jpy).getValor());
        assertThrows(IllegalArgumentException.class, () -> Dinheiro.exato("500.0", jpy));
        assertThrows(IllegalArgumentException.class, () -> Dinheiro.exato("500.5", jpy));

        assertEquals(Real.de("1050.00"), Real.deInteiro(1050L));
        assertEquals(Real.de("10.50"), Real.deCentavos(1050L));
        assertEquals(1050L, Real.de("10.50").getCentavos());
        assertEquals(1050L, Real.de("10.50").emUnidadesMinimas());

        assertEquals(Dolar.de("25.00"), Dolar.deInteiro(25L));
        assertEquals(2500L, Dolar.deCentavos(2500L).getCentavos());
        assertEquals(Euro.de("30.00"), Euro.deInteiro(30L));
        assertEquals(3000L, Euro.deCentavos(3000L).getCentavos());

        Moeda kwd = Moeda.iso("KWD", 3);
        QuantiaMonetaria dinar = QuantiaMonetaria.deUnidadesMinimas(12345L, kwd);
        assertEquals(new BigDecimal("12.345"), dinar.getValor());
        assertEquals(12345L, dinar.emUnidadesMinimas());
    }

    @Test
    void operacoesAritmeticasExatasPropagacaoDePoliticaEComparacao() {
        Real base = Real.de("100.00", MetodoArredondamento.TRUNCAMENTO);
        assertEquals(Real.de("125.50"), base.somar(Real.de("25.50")));
        assertEquals(Real.de("89.75"), base.subtrair(Real.de("10.25")));
        assertEquals(Real.de("200.00"), base.multiplicar(2L));
        assertEquals(Real.de("33.33"), base.dividir(3L));
        assertSame(MetodoArredondamento.TRUNCAMENTO, base.somar(Real.de("1.00")).getArredondamento());

        Real reconfigurado = base.comArredondamento(MetodoArredondamento.PARA_CIMA);
        assertEquals(Real.de("33.34"), reconfigurado.dividir(3L));
        assertEquals(Real.de("33.33"), reconfigurado.dividir(3L, MetodoArredondamento.TRUNCAMENTO));
        assertSame(
                MetodoArredondamento.TRUNCAMENTO,
                reconfigurado.dividir(3L, MetodoArredondamento.TRUNCAMENTO).getArredondamento());

        Real neg = base.negar();
        assertTrue(neg.isNegativo());
        assertEquals(Real.de("-100.00"), neg);
        assertEquals(base, neg.abs());
        assertTrue(base.isPositivo());
        assertTrue(base.compareTo(Real.de("99.99")) > 0);
        assertEquals(0, base.compareTo(Real.de("100.00", MetodoArredondamento.FINANCEIRO)));

        assertEquals(Real.de("102.45"), Real.de("100.00").multiplicar(new BigDecimal("1.0245")));
        assertEquals(
                Real.de("102.46"),
                Real.de("100.00").multiplicar(new BigDecimal("1.02455"), MetodoArredondamento.FINANCEIRO));

        Dolar d1 = Dolar.de("40.00");
        Dolar d2 = Dolar.de("15.25");
        assertEquals(Dolar.de("55.25"), d1.somar(d2));
        assertEquals(Dolar.de("24.75"), d1.subtrair(d2));
        assertEquals(Dolar.de("-40.00"), d1.negar());
        assertEquals(d1, d1.negar().abs());
        assertTrue(d1.compareTo(d2) > 0);

        Euro e1 = Euro.de("40.00");
        Euro e2 = Euro.de("15.25");
        assertEquals(Euro.de("55.25"), e1.somar(e2));
        assertEquals(Euro.de("24.75"), e1.subtrair(e2));
        assertEquals(Euro.de("-40.00"), e1.negar());
        assertEquals(e1, e1.negar().abs());
        assertTrue(e1.compareTo(e2) > 0);

        Moeda gbp = Moeda.iso("GBP", 2);
        QuantiaMonetaria q1 = QuantiaMonetaria.de("20.00", gbp);
        QuantiaMonetaria q2 = QuantiaMonetaria.de("5.50", gbp);
        assertEquals(QuantiaMonetaria.de("25.50", gbp), q1.somar(q2));
        assertEquals(QuantiaMonetaria.de("14.50", gbp), q1.subtrair(q2));
        assertTrue(q1.compareTo(q2) > 0);
    }

    @Test
    void divisaoSemArredondamentoDuploEmGrandeMagnitudeEPoliticaCustomizadaSticky() {
        // 10^33.00 / 3 = 333...333.333333...
        // Se usasse DECIMAL128 (34 dígitos), arredondaria na casa inteira antes da escala 2!
        BigDecimal grande = new BigDecimal("1000000000000000000000000000000001.00"); // 36 dígitos de precisão <= 38
        Real rGrande = Real.de(grande, MetodoArredondamento.TETO);
        Real quocienteTeto = rGrande.dividir(3L);
        assertEquals(new BigDecimal("333333333333333333333333333333333.67"), quocienteTeto.getValor());

        Real quocienteTrunc = rGrande.dividir(3L, MetodoArredondamento.TRUNCAMENTO);
        assertEquals(new BigDecimal("333333333333333333333333333333333.66"), quocienteTrunc.getValor());

        // Política customizada tie-to-odd exercitando sticky exato (< half, == half, > half)
        MetodoArredondamento empateParaImpar = (v, casas) -> {
            BigDecimal floor = v.setScale(casas, RoundingMode.FLOOR);
            BigDecimal ceil = v.setScale(casas, RoundingMode.CEILING);
            if (floor.equals(ceil)) {
                return floor;
            }
            BigDecimal dobro = v.subtract(floor).movePointRight(casas).multiply(new BigDecimal("2"));
            int cmp = dobro.compareTo(BigDecimal.ONE);
            if (cmp < 0) {
                return floor;
            }
            if (cmp > 0) {
                return ceil;
            }
            return floor.unscaledValue().testBit(0) ? floor : ceil;
        };

        // 0.01 / 2 = 0.005 (empate exato -> ímpar = 0.01)
        assertEquals(Real.de("0.01"), Real.de("0.01", empateParaImpar).dividir(2L));
        // 0.03 / 2 = 0.015 (empate exato -> ímpar = 0.01)
        assertEquals(Real.de("0.01"), Real.de("0.03", empateParaImpar).dividir(2L));
        // 1.00 / 3 = 0.3333... (< half -> 0.33)
        assertEquals(Real.de("0.33"), Real.de("1.00", empateParaImpar).dividir(3L));
        // 2.00 / 3 = 0.6666... (> half -> 0.67)
        assertEquals(Real.de("0.67"), Real.de("2.00", empateParaImpar).dividir(3L));
    }

    @Test
    void divisaoTerminanteComPoliticaCustomizadaRecebeQuocienteDecimalExato() {
        long divisor = 1099511627776L; // 2^40
        BigDecimal limitePositivo = new BigDecimal("0.0000000000009094947017729282379150390625");
        AtomicReference<BigDecimal> recebidoPositivo = new AtomicReference<>();
        MetodoArredondamento politica = (valor, casas) -> {
            recebidoPositivo.set(valor);
            RoundingMode modo = valor.compareTo(limitePositivo) > 0 ? RoundingMode.CEILING : RoundingMode.FLOOR;
            return valor.setScale(casas, modo);
        };

        Real resultadoPositivo = Real.de("1.00").dividir(divisor, politica);
        assertEquals(Real.de("0.00"), resultadoPositivo);
        assertNotNull(recebidoPositivo.get());
        assertEquals(
                0,
                limitePositivo.compareTo(recebidoPositivo.get()),
                () -> "valor recebido: " + recebidoPositivo.get());

        BigDecimal limiteNegativo = limitePositivo.negate();
        AtomicReference<BigDecimal> recebidoNegativo = new AtomicReference<>();
        MetodoArredondamento politicaNegativa = (valor, casas) -> {
            recebidoNegativo.set(valor);
            RoundingMode modo = valor.compareTo(limiteNegativo) < 0 ? RoundingMode.FLOOR : RoundingMode.CEILING;
            return valor.setScale(casas, modo);
        };

        Real resultadoNegativo = Real.de("-1.00").dividir(divisor, politicaNegativa);
        assertEquals(Real.de("0.00"), resultadoNegativo);
        assertNotNull(recebidoNegativo.get());
        assertEquals(
                0,
                limiteNegativo.compareTo(recebidoNegativo.get()),
                () -> "valor recebido: " + recebidoNegativo.get());

        // Cotação terminante de alta potência de 2 (2^126 -> escala exata 126) delegando para política padrão:
        String doisElevado126 = java.math.BigInteger.ONE.shiftLeft(126).toString();
        TaxaCambio taxaExtrema = TaxaCambio.umDolarEmReais(doisElevado126).inverter();
        MetodoArredondamento delegandoFinanceiro =
                (valor, casas) -> MetodoArredondamento.FINANCEIRO.arredondar(valor, casas);
        assertEquals(Dolar.ZERO, Real.de("1.00").converter(taxaExtrema, delegandoFinanceiro));
    }

    @Test
    void rejeicoesDeMoedasDistintasDivisaoPorZeroEOverflowLong() {
        Dinheiro real = Real.de("10.00");
        Dinheiro dolar = Dolar.de("10.00");

        assertThrows(IllegalArgumentException.class, () -> real.somarMesmaMoeda(dolar));
        assertThrows(IllegalArgumentException.class, () -> real.subtrairMesmaMoeda(dolar));
        assertThrows(IllegalArgumentException.class, () -> real.compararMesmaMoeda(dolar));
        assertThrows(IllegalArgumentException.class, () -> real.somarMesmaMoeda(null));
        assertThrows(IllegalArgumentException.class, () -> real.dividir(0L));

        QuantiaMonetaria jpy = QuantiaMonetaria.de("100", Moeda.iso("JPY", 0));
        QuantiaMonetaria kwd = QuantiaMonetaria.de("10.000", Moeda.iso("KWD", 3));
        assertThrows(IllegalArgumentException.class, () -> jpy.somar(kwd));
        assertThrows(IllegalArgumentException.class, () -> jpy.compareTo(kwd));

        Real enorme = Real.de("99999999999999999999.99");
        assertThrows(ArithmeticException.class, enorme::emUnidadesMinimas);
        assertThrows(ArithmeticException.class, enorme::getCentavos);
    }

    @Test
    void paridadeCompletaDeSobrecargasEBordasEmRealDolarEuroQuantiaEDinheiro() {
        MetodoArredondamento customFloor = (v, c) -> v.setScale(c, RoundingMode.FLOOR);
        Moeda gbp = Moeda.iso("GBP", 2);

        // Dinheiro estáticos (String, BigDecimal, exato, deInteiro, deUnidadesMinimas, zero)
        assertEquals(Real.de("12.35"), Dinheiro.de(new BigDecimal("12.345"), Moeda.BRL, MetodoArredondamento.FINANCEIRO));
        assertEquals(Dolar.de("12.34"), Dinheiro.exato("12.34", Moeda.USD, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Euro.de("12.30"), Dinheiro.exato(new BigDecimal("12.3"), Moeda.EUR));
        assertEquals(Euro.de("12.30"), Dinheiro.exato(new BigDecimal("12.3"), Moeda.EUR, MetodoArredondamento.PISO));
        assertEquals(Real.de("7.00"), Dinheiro.deInteiro(7L, Moeda.BRL));
        assertEquals(Dolar.de("7.00"), Dinheiro.deInteiro(7L, Moeda.USD, MetodoArredondamento.TETO));
        assertEquals(Euro.de("7.50"), Dinheiro.deUnidadesMinimas(750L, Moeda.EUR));
        assertEquals(Real.de("7.50"), Dinheiro.deUnidadesMinimas(750L, Moeda.BRL, MetodoArredondamento.PARA_CIMA));
        assertEquals(QuantiaMonetaria.de("0.00", gbp), Dinheiro.zero(gbp));

        Dinheiro polBrl = Real.de("20.00");
        assertEquals(Real.de("15.00"), polBrl.subtrairMesmaMoeda(Real.de("5.00")));
        assertEquals(0, polBrl.compararMesmaMoeda(Real.de("20.00")));
        assertTrue(polBrl.equals(polBrl));
        assertFalse(polBrl.equals("BRL 20.00"));
        assertFalse(polBrl.equals(null));

        // Real sobrecargas adicionais e guardas de nulo/estouro
        assertEquals(Real.de("5.00"), Real.de(new BigDecimal("5")));
        assertEquals(Real.de("5.20"), Real.exato("5.2", MetodoArredondamento.FINANCEIRO));
        assertEquals(Real.de("5.20"), Real.exato(new BigDecimal("5.2"), MetodoArredondamento.FINANCEIRO));
        assertEquals(Real.de("8.00"), Real.deInteiro(8L, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Real.de("8.25"), Real.deCentavos(825L, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Real.de("10.00"), Real.de("10.00").abs());
        assertEquals(Real.de("25.00"), Real.de("10.00", customFloor).multiplicar(new BigDecimal("2.5")));
        assertThrows(IllegalArgumentException.class, () -> Real.ZERO.somar(null));
        assertThrows(IllegalArgumentException.class, () -> Real.ZERO.subtrair(null));
        assertThrows(IllegalArgumentException.class, () -> Real.ZERO.compareTo(null));
        assertThrows(IllegalArgumentException.class, () -> MetodoArredondamento.ABNT_NBR_5891.arredondar(null, 2));

        // Dolar sobrecargas completas
        assertEquals(Dolar.de("5.00"), Dolar.de(new BigDecimal("5")));
        assertEquals(Dolar.de("5.25"), Dolar.de(new BigDecimal("5.245"), MetodoArredondamento.FINANCEIRO));
        assertEquals(Dolar.de("5.20"), Dolar.exato("5.2"));
        assertEquals(Dolar.de("5.20"), Dolar.exato("5.2", MetodoArredondamento.FINANCEIRO));
        assertEquals(Dolar.de("5.20"), Dolar.exato(new BigDecimal("5.2")));
        assertEquals(Dolar.de("5.20"), Dolar.exato(new BigDecimal("5.2"), MetodoArredondamento.FINANCEIRO));
        assertEquals(Dolar.de("8.00"), Dolar.deInteiro(8L, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Dolar.de("8.25"), Dolar.deCentavos(825L, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Dolar.de("10.00"), Dolar.de("10.00").abs());
        assertEquals(Dolar.de("20.00"), Dolar.de("10.00").comArredondamento(MetodoArredondamento.TETO).multiplicar(2L));
        assertEquals(Dolar.de("15.00"), Dolar.de("10.00").multiplicar(new BigDecimal("1.5")));
        assertEquals(Dolar.de("15.00"), Dolar.de("10.00").multiplicar(new BigDecimal("1.5"), MetodoArredondamento.PISO));
        assertEquals(Dolar.de("3.33"), Dolar.de("10.00").dividir(3L));
        assertEquals(Dolar.de("3.34"), Dolar.de("10.00").dividir(3L, MetodoArredondamento.TETO));
        assertEquals(2, Dolar.de("10.00").alocar(java.util.Arrays.asList(BigDecimal.ONE, BigDecimal.ONE)).size());
        assertThrows(IllegalArgumentException.class, () -> Dolar.ZERO.somar(null));
        assertThrows(IllegalArgumentException.class, () -> Dolar.ZERO.subtrair(null));
        assertThrows(IllegalArgumentException.class, () -> Dolar.ZERO.compareTo(null));

        // Euro sobrecargas completas
        assertEquals(Euro.de("5.00"), Euro.de(new BigDecimal("5")));
        assertEquals(Euro.de("5.25"), Euro.de(new BigDecimal("5.245"), MetodoArredondamento.FINANCEIRO));
        assertEquals(Euro.de("5.20"), Euro.exato("5.2"));
        assertEquals(Euro.de("5.20"), Euro.exato("5.2", MetodoArredondamento.FINANCEIRO));
        assertEquals(Euro.de("5.20"), Euro.exato(new BigDecimal("5.2")));
        assertEquals(Euro.de("5.20"), Euro.exato(new BigDecimal("5.2"), MetodoArredondamento.FINANCEIRO));
        assertEquals(Euro.de("8.00"), Euro.deInteiro(8L, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Euro.de("8.25"), Euro.deCentavos(825L, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(Euro.de("10.00"), Euro.de("10.00").abs());
        assertEquals(Euro.de("20.00"), Euro.de("10.00").comArredondamento(MetodoArredondamento.TETO).multiplicar(2L));
        assertEquals(Euro.de("15.00"), Euro.de("10.00").multiplicar(new BigDecimal("1.5")));
        assertEquals(Euro.de("15.00"), Euro.de("10.00").multiplicar(new BigDecimal("1.5"), MetodoArredondamento.PISO));
        assertEquals(Euro.de("3.33"), Euro.de("10.00").dividir(3L));
        assertEquals(Euro.de("3.34"), Euro.de("10.00").dividir(3L, MetodoArredondamento.TETO));
        assertEquals(2, Euro.de("10.00").alocar(java.util.Arrays.asList(BigDecimal.ONE, BigDecimal.ONE)).size());
        assertThrows(IllegalArgumentException.class, () -> Euro.ZERO.somar(null));
        assertThrows(IllegalArgumentException.class, () -> Euro.ZERO.subtrair(null));
        assertThrows(IllegalArgumentException.class, () -> Euro.ZERO.compareTo(null));

        // QuantiaMonetaria sobrecargas completas
        assertEquals(QuantiaMonetaria.de("5.00", gbp), QuantiaMonetaria.de(new BigDecimal("5"), gbp));
        assertEquals(
                QuantiaMonetaria.de("5.25", gbp),
                QuantiaMonetaria.de(new BigDecimal("5.245"), gbp, MetodoArredondamento.FINANCEIRO));
        assertEquals(QuantiaMonetaria.de("5.20", gbp), QuantiaMonetaria.exato("5.2", gbp));
        assertEquals(
                QuantiaMonetaria.de("5.20", gbp),
                QuantiaMonetaria.exato("5.2", gbp, MetodoArredondamento.FINANCEIRO));
        assertEquals(QuantiaMonetaria.de("5.20", gbp), QuantiaMonetaria.exato(new BigDecimal("5.2"), gbp));
        assertEquals(
                QuantiaMonetaria.de("5.20", gbp),
                QuantiaMonetaria.exato(new BigDecimal("5.2"), gbp, MetodoArredondamento.FINANCEIRO));
        assertEquals(QuantiaMonetaria.de("8.00", gbp), QuantiaMonetaria.deInteiro(8L, gbp));
        assertEquals(
                QuantiaMonetaria.de("8.00", gbp),
                QuantiaMonetaria.deInteiro(8L, gbp, MetodoArredondamento.TRUNCAMENTO));
        assertEquals(
                QuantiaMonetaria.de("8.25", gbp),
                QuantiaMonetaria.deUnidadesMinimas(825L, gbp, MetodoArredondamento.TRUNCAMENTO));
        QuantiaMonetaria qNeg = QuantiaMonetaria.de("-10.00", gbp);
        assertEquals(QuantiaMonetaria.de("10.00", gbp), qNeg.abs());
        assertEquals(QuantiaMonetaria.de("10.00", gbp), qNeg.abs().abs());
        assertEquals(
                QuantiaMonetaria.de("20.00", gbp),
                QuantiaMonetaria.de("10.00", gbp).comArredondamento(MetodoArredondamento.TETO).multiplicar(2L));
        assertEquals(
                QuantiaMonetaria.de("15.00", gbp),
                QuantiaMonetaria.de("10.00", gbp).multiplicar(new BigDecimal("1.5")));
        assertEquals(
                QuantiaMonetaria.de("15.00", gbp),
                QuantiaMonetaria.de("10.00", gbp, customFloor)
                        .multiplicar(new BigDecimal("1.5"), customFloor));
        assertEquals(QuantiaMonetaria.de("3.33", gbp), QuantiaMonetaria.de("10.00", gbp).dividir(3L));
        assertEquals(
                QuantiaMonetaria.de("3.34", gbp),
                QuantiaMonetaria.de("10.00", gbp).dividir(3L, MetodoArredondamento.TETO));
        assertEquals(
                QuantiaMonetaria.de("5.00", gbp),
                QuantiaMonetaria.de("10.00", gbp, customFloor).dividir(2L));
        assertEquals(
                2,
                QuantiaMonetaria.de("10.00", gbp)
                        .alocar(java.util.Arrays.asList(BigDecimal.ONE, BigDecimal.ONE))
                        .size());

        // Achado 5 e 12: política customizada com limiar 0.3 em divisão não terminante, divisor negativo e preservação de política
        MetodoArredondamento tetoAcimaDeZeroTres = (v, c) -> {
            BigDecimal floor = v.setScale(c, RoundingMode.FLOOR);
            BigDecimal ceil = v.setScale(c, RoundingMode.CEILING);
            BigDecimal fracao = v.subtract(floor).movePointRight(c);
            return fracao.compareTo(new BigDecimal("0.3")) >= 0 ? ceil : floor;
        };
        assertEquals(Real.de("0.34"), Real.de("1.00", tetoAcimaDeZeroTres).dividir(3L));
        assertEquals(Real.de("-0.33"), Real.de("1.00", tetoAcimaDeZeroTres).dividir(-3L));
        assertEquals(Real.ZERO, Real.de("1.00", tetoAcimaDeZeroTres).dividir(Long.MIN_VALUE));

        // Preservação explícita de getArredondamento() em abs, negar e multiplicar(long)
        Real rTrunc = Real.de("-10.00", MetodoArredondamento.TRUNCAMENTO);
        assertSame(MetodoArredondamento.TRUNCAMENTO, rTrunc.abs().getArredondamento());
        assertSame(MetodoArredondamento.TRUNCAMENTO, rTrunc.negar().getArredondamento());
        assertSame(MetodoArredondamento.TRUNCAMENTO, rTrunc.multiplicar(2L).getArredondamento());

        // Anti-TOCTOU pela API pública Dinheiro.de
        java.util.concurrent.atomic.AtomicInteger leituras = new java.util.concurrent.atomic.AtomicInteger();
        Moeda mutante = new Moeda() {
            @Override
            public String getCodigo() {
                return leituras.incrementAndGet() == 1 ? "CLP" : "ERR";
            }

            @Override
            public int getCasasDecimais() {
                return 0;
            }
        };
        Dinheiro clp = Dinheiro.de("500", mutante);
        assertEquals("CLP 500", clp.toString());
    }
}
