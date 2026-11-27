package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaxaCambioTest {

    @Test
    void conversaoDiretaInversaExataEInvolutivaSemPerdaEmTruncamentoEPiso() {
        TaxaCambio ptax = TaxaCambio.umDolarEmReais("5.4832");
        assertEquals(Moeda.USD, ptax.getMoedaOrigem());
        assertEquals(Moeda.BRL, ptax.getMoedaDestino());
        assertEquals(new BigDecimal("5.4832"), ptax.getCotacaoBase());
        assertFalse(ptax.isInvertida());
        assertEquals("1 USD = 5.4832 BRL", ptax.toString());

        Dolar cemDolares = Dolar.de("100.00", MetodoArredondamento.TRUNCAMENTO);
        Real convertido = cemDolares.paraReal(ptax);
        assertEquals(Real.de("548.32"), convertido);
        assertSame(MetodoArredondamento.TRUNCAMENTO, convertido.getArredondamento());

        TaxaCambio invertida = ptax.inverter();
        assertTrue(invertida.isInvertida());
        assertEquals(Moeda.BRL, invertida.getMoedaOrigem());
        assertEquals(Moeda.USD, invertida.getMoedaDestino());
        assertEquals("5.4832 BRL = 1 USD", invertida.toString());

        // Involução exata: inverter duas vezes restaura exatamente a taxa original
        assertEquals(ptax, invertida.inverter());
        assertEquals(ptax.hashCode(), invertida.inverter().hashCode());
        assertNotEquals(ptax, invertida);

        // Volta exata mesmo com TRUNCAMENTO e PISO (sem erro de 99.99 por arredondamento intermediário)
        Dinheiro voltaTrunc = Real.de("548.32", MetodoArredondamento.TRUNCAMENTO).converter(invertida);
        assertInstanceOf(Dolar.class, voltaTrunc);
        assertEquals(Dolar.de("100.00"), voltaTrunc);

        Dolar voltaPiso = Real.de("548.32").paraDolar(ptax, MetodoArredondamento.PISO);
        assertEquals(Dolar.de("100.00"), voltaPiso);
        assertSame(MetodoArredondamento.PISO, voltaPiso.getArredondamento());
    }

    @Test
    void atalhosBidirecionaisEntreRealDolarEEuroENormalizacaoSemExpoente() {
        TaxaCambio usdBrl = TaxaCambio.umDolarEmReais(new BigDecimal("5.5000"));
        TaxaCambio eurBrl = TaxaCambio.umEuroEmReais("6.00");
        TaxaCambio eurUsd = TaxaCambio.de(Moeda.EUR, Moeda.USD, "1.10");

        assertEquals(Dolar.de("20.00"), Real.de("110.00").paraDolar(usdBrl));
        assertEquals(Dolar.de("20.00"), Real.de("110.00").paraDolar(usdBrl.inverter()));
        assertEquals(Euro.de("15.00"), Real.de("90.00").paraEuro(eurBrl));
        assertEquals(Real.de("90.00"), Euro.de("15.00").paraReal(eurBrl));
        assertEquals(Dolar.de("11.00"), Euro.de("10.00").paraDolar(eurUsd));
        assertEquals(Euro.de("10.00"), Dolar.de("11.00").paraEuro(eurUsd));

        // Cotação inteira com zeros decimais não gera notação científica 1E+2
        TaxaCambio ienePorDolar = TaxaCambio.de(Moeda.USD, Moeda.iso("JPY", 0), "100.00");
        assertEquals("100", ienePorDolar.getCotacaoBase().toString());
        assertEquals("1 USD = 100 JPY", ienePorDolar.toString());
        assertEquals(TaxaCambio.de(Moeda.USD, Moeda.iso("JPY", 0), "100"), ienePorDolar);

        // Paridade 1:1 invertida equivale à direta na direção oposta
        TaxaCambio paridadeDireta = TaxaCambio.de(Moeda.USD, Moeda.EUR, "1.000");
        TaxaCambio paridadeOposta = TaxaCambio.de(Moeda.EUR, Moeda.USD, "1");
        assertEquals(paridadeOposta, paridadeDireta.inverter());
        assertEquals(paridadeOposta.hashCode(), paridadeDireta.inverter().hashCode());

        assertEquals(new BigDecimal("0.2"), TaxaCambio.umDolarEmReais("5").inverter().getFatorAproximado(MathContext.DECIMAL32));
        assertEquals(new BigDecimal("5"), TaxaCambio.umDolarEmReais("5").getFatorAproximado(MathContext.DECIMAL32));
        assertEquals(TaxaCambio.umEuroEmReais("6"), TaxaCambio.umEuroEmReais(new BigDecimal("6.0")));
        assertEquals(Euro.de("15.00"), Real.de("90.00").paraEuro(eurBrl, MetodoArredondamento.FINANCEIRO));
        assertEquals(Real.de("90.00"), Euro.de("15.00").paraReal(eurBrl, MetodoArredondamento.FINANCEIRO));
        assertEquals(Dolar.de("11.00"), Euro.de("10.00").paraDolar(eurUsd, MetodoArredondamento.FINANCEIRO));
        assertEquals(Euro.de("10.00"), Dolar.de("11.00").paraEuro(eurUsd, MetodoArredondamento.FINANCEIRO));
        assertEquals(Real.de("55.00"), Dolar.de("10.00").paraReal(usdBrl, MetodoArredondamento.FINANCEIRO));
        assertEquals(Dolar.de("10.00"), Real.de("55.00").converter(usdBrl.inverter(), MetodoArredondamento.FINANCEIRO));
        assertTrue(usdBrl.equals(usdBrl));
        assertFalse(usdBrl.equals("1 USD = 5.5 BRL"));
    }

    @Test
    void rejeicoesEmTaxaCambioEConversoesIncompativeis() {
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.BRL, Moeda.BRL, "1.0"));
        assertThrows(
                IllegalArgumentException.class,
                () -> TaxaCambio.de(Moeda.BRL, Moeda.of("BRL", 2, "Reais"), "1.0"));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.USD, Moeda.BRL, "0"));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.USD, Moeda.BRL, "-5.0"));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.USD, Moeda.BRL, (String) null));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.USD, Moeda.BRL, (BigDecimal) null));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(null, Moeda.BRL, "5.0"));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.USD, null, "5.0"));

        TaxaCambio usdBrl = TaxaCambio.umDolarEmReais("5.00");
        assertThrows(IllegalArgumentException.class, () -> Real.de("50.00").converter(usdBrl));
        assertThrows(IllegalArgumentException.class, () -> Euro.de("50.00").converter(usdBrl));
        assertThrows(IllegalArgumentException.class, () -> Real.de("50.00").paraEuro(usdBrl));
        assertThrows(IllegalArgumentException.class, () -> Dolar.de("50.00").paraEuro(usdBrl));
        assertThrows(IllegalArgumentException.class, () -> Euro.de("50.00").paraReal(usdBrl));
        assertThrows(IllegalArgumentException.class, () -> usdBrl.getFatorAproximado(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> TaxaCambio.umDolarEmReais("3").inverter().getFatorAproximado(MathContext.UNLIMITED));
        assertThrows(IllegalArgumentException.class, () -> usdBrl.converter(null));
        assertThrows(IllegalArgumentException.class, () -> Real.de("10.00").converter(null));

        // Conversão invertida com política customizada entre escalas distintas (KWD/3 <-> JPY/0)
        Moeda kwd = Moeda.iso("KWD", 3);
        Moeda jpy = Moeda.iso("JPY", 0);
        TaxaCambio kwdJpy = TaxaCambio.de(kwd, jpy, "485.5");
        MetodoArredondamento customHalfUp = (v, c) -> v.setScale(c, java.math.RoundingMode.HALF_UP);
        Dinheiro emJpy = Dinheiro.de("1.000", kwd).converter(kwdJpy, customHalfUp);
        assertEquals(QuantiaMonetaria.de("486", jpy), emJpy);
        Dinheiro voltaKwd = QuantiaMonetaria.de("485", jpy).converter(kwdJpy.inverter(), customHalfUp);
        assertEquals(QuantiaMonetaria.de("0.999", kwd), voltaKwd);
    }
}
