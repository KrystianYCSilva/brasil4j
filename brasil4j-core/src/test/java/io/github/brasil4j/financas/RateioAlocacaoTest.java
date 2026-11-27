package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateioAlocacaoTest {

    @Test
    void rateioEmReaisPrimeirasEUltimasParcelasPositivoENegativo() {
        Real cem = Real.de("100.00", MetodoArredondamento.TRUNCAMENTO);
        List<Real> primeiras = cem.ratear(3);
        assertEquals(
                Arrays.asList(Real.de("33.34"), Real.de("33.33"), Real.de("33.33")),
                primeiras);
        assertSame(MetodoArredondamento.TRUNCAMENTO, primeiras.get(0).getArredondamento());
        assertThrows(UnsupportedOperationException.class, () -> primeiras.add(Real.ZERO));

        List<Real> ultimas = cem.ratear(3, PoliticaResiduo.ULTIMAS_PARCELAS);
        assertEquals(
                Arrays.asList(Real.de("33.33"), Real.de("33.33"), Real.de("33.34")),
                ultimas);

        Real menosCem = Real.de("-100.00");
        assertEquals(
                Arrays.asList(Real.de("-33.34"), Real.de("-33.33"), Real.de("-33.33")),
                menosCem.ratear(3, PoliticaResiduo.PRIMEIRAS_PARCELAS));
        assertEquals(
                Arrays.asList(Real.de("-33.33"), Real.de("-33.33"), Real.de("-33.34")),
                menosCem.ratear(3, PoliticaResiduo.ULTIMAS_PARCELAS));
    }

    @Test
    void rateioExaustivoDe1A120ParcelasEm0_2_E3CasasDecimaisConservaSomaExata() {
        Moeda jpy = Moeda.iso("JPY", 0);
        Moeda kwd = Moeda.iso("KWD", 3);

        Dinheiro[] amostras = new Dinheiro[] {
            Real.de("100.00"),
            Real.de("-100.07"),
            Real.ZERO,
            Dolar.de("999.99"),
            Euro.de("-0.05"),
            QuantiaMonetaria.de("100", jpy),
            QuantiaMonetaria.de("-1000", jpy),
            QuantiaMonetaria.de("50.005", kwd)
        };

        for (Dinheiro amostra : amostras) {
            for (int n = 1; n <= 120; n++) {
                for (PoliticaResiduo pol : PoliticaResiduo.values()) {
                    List<? extends Dinheiro> cotas = amostra.ratear(n, pol);
                    assertEquals(n, cotas.size());
                    Dinheiro soma = Dinheiro.zero(amostra.getMoeda());
                    for (Dinheiro cota : cotas) {
                        soma = soma.somarMesmaMoeda(cota);
                    }
                    assertEquals(amostra, soma);
                }
            }
        }
    }

    @Test
    void alocacaoHamiltonExataComPesoZeroENegativos() {
        Real cem = Real.de("100.00", MetodoArredondamento.FINANCEIRO);
        List<Real> iguais = cem.alocar(Arrays.asList(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
        assertEquals(Arrays.asList(Real.de("33.34"), Real.de("33.33"), Real.de("33.33")), iguais);
        assertSame(MetodoArredondamento.FINANCEIRO, iguais.get(1).getArredondamento());

        List<Real> comPesoZero = cem.alocar(
                Arrays.asList(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
        assertEquals(
                Arrays.asList(Real.ZERO, Real.de("33.34"), Real.de("33.33"), Real.de("33.33")),
                comPesoZero);

        List<Real> proporcionais = cem.alocar(
                Arrays.asList(new BigDecimal("70"), new BigDecimal("20"), new BigDecimal("10")));
        assertEquals(
                Arrays.asList(Real.de("70.00"), Real.de("20.00"), Real.de("10.00")),
                proporcionais);

        Real menosCincoCentavos = Real.de("-0.05");
        List<Real> negAloc = menosCincoCentavos.alocar(
                Arrays.asList(new BigDecimal("1"), new BigDecimal("2")));
        assertEquals(Arrays.asList(Real.de("-0.02"), Real.de("-0.03")), negAloc);
    }

    @Test
    void rejeicoesEmRatearEAlocar() {
        Real base = Real.de("10.00");
        assertThrows(IllegalArgumentException.class, () -> base.ratear(0));
        assertThrows(IllegalArgumentException.class, () -> base.ratear(-2));
        assertThrows(IllegalArgumentException.class, () -> base.ratear(2, null));

        assertThrows(IllegalArgumentException.class, () -> base.alocar(null));
        assertThrows(IllegalArgumentException.class, () -> base.alocar(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> base.alocar(Arrays.asList(BigDecimal.ONE, null)));
        assertThrows(
                IllegalArgumentException.class,
                () -> base.alocar(Arrays.asList(BigDecimal.ONE, new BigDecimal("-1"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> base.alocar(Arrays.asList(BigDecimal.ZERO, BigDecimal.ZERO)));
        assertThrows(
                IllegalArgumentException.class,
                () -> base.alocar(Arrays.asList(new BigDecimal("1E+100000"), new BigDecimal("1E-100000"))));

        // Achado 3: teto anti-DoS em ratear (máx 10.000 parcelas)
        assertThrows(IllegalArgumentException.class, () -> Real.ZERO.ratear(10_001));
        assertThrows(IllegalArgumentException.class, () -> Real.ZERO.ratear(Integer.MAX_VALUE));

        // Achado 4: lista mutável / TOCTOU lida apenas uma vez via toArray()
        java.util.concurrent.atomic.AtomicInteger chamadasToArray = new java.util.concurrent.atomic.AtomicInteger();
        List<BigDecimal> listaToctou = new java.util.AbstractList<BigDecimal>() {
            @Override
            public BigDecimal get(int index) {
                return chamadasToArray.get() == 0 ? BigDecimal.ONE : new BigDecimal("-99");
            }

            @Override
            public int size() {
                return 2;
            }

            @Override
            public Object[] toArray() {
                chamadasToArray.incrementAndGet();
                return new Object[] {BigDecimal.ONE, BigDecimal.ONE};
            }
        };
        assertEquals(Arrays.asList(Real.de("5.00"), Real.de("5.00")), base.alocar(listaToctou));
        assertEquals(1, chamadasToArray.get());
    }
}
