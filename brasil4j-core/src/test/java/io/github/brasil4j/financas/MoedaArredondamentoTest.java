package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoedaArredondamentoTest {

    @Test
    void constantesPadraoPossuemMetadadosEsperados() {
        assertEquals("BRL", Moeda.BRL.getCodigo());
        assertEquals(2, Moeda.BRL.getCasasDecimais());
        assertEquals("R$", Moeda.BRL.getSimbolo());

        assertEquals("USD", Moeda.USD.getCodigo());
        assertEquals(2, Moeda.USD.getCasasDecimais());
        assertEquals("US$", Moeda.USD.getSimbolo());

        assertEquals("EUR", Moeda.EUR.getCodigo());
        assertEquals(2, Moeda.EUR.getCasasDecimais());
        assertEquals("€", Moeda.EUR.getSimbolo());
    }

    @Test
    void fabricaIsoValidaTresLetrasMaiusculasAsciiECasas() {
        Moeda jpy = Moeda.iso("JPY", 0);
        assertEquals("JPY", jpy.getCodigo());
        assertEquals(0, jpy.getCasasDecimais());
        assertEquals("JPY", jpy.getSimbolo());

        Moeda kwd = Moeda.iso("KWD", 3);
        assertEquals(3, kwd.getCasasDecimais());

        assertSame(Moeda.BRL, Moeda.iso("BRL", 2));
        assertSame(Moeda.USD, Moeda.iso("USD", 2));
        assertSame(Moeda.EUR, Moeda.iso("EUR", 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "BR", "BRLX", "brl", "BR1", "B L", "BR\n", "BRÇ"})
    void fabricaIsoRejeitaCodigosInvalidos(String codigoInvalido) {
        assertThrows(IllegalArgumentException.class, () -> Moeda.iso(codigoInvalido, 2));
    }

    @Test
    void fabricaIsoEOfRejeitamNulosECasasForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class, () -> Moeda.iso(null, 2));
        assertThrows(IllegalArgumentException.class, () -> Moeda.iso("JPY", -1));
        assertThrows(IllegalArgumentException.class, () -> Moeda.iso("JPY", 19));

        Moeda ufir = Moeda.of("UFIR-RJ", 4, "UFIR");
        assertEquals("UFIR-RJ", ufir.getCodigo());
        assertEquals(4, ufir.getCasasDecimais());
        assertEquals("UFIR", ufir.getSimbolo());

        assertThrows(IllegalArgumentException.class, () -> Moeda.of(null, 2, "X"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("", 2, "X"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("ABCDEFGHIJKLMNOPQ", 2, "X"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("COM ESPACO", 2, "X"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("NAO_ASCII_Ç", 2, "X"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("BTC", 8, null));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("BTC", 8, ""));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("BTC", 8, "12345678901234567"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("BTC", 8, "S\u0007"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("BTC", 8, "S\u007F"));
    }

    @Test
    void ponteJdkCurrencyECapturaAntiToctou() {
        Moeda chf = Moeda.fromJdkCurrency(Currency.getInstance("CHF"), 2);
        assertEquals("CHF", chf.getCodigo());
        assertEquals(2, chf.getCasasDecimais());
        assertThrows(IllegalArgumentException.class, () -> Moeda.fromJdkCurrency(null, 2));
        assertThrows(IllegalArgumentException.class, () -> Moeda.fromJdkCurrency(Currency.getInstance("CHF"), 19));

        AtomicInteger leiturasCodigo = new AtomicInteger();
        AtomicInteger leiturasCasas = new AtomicInteger();
        AtomicInteger leiturasSimbolo = new AtomicInteger();
        Moeda moedaToctou = new Moeda() {
            @Override
            public String getCodigo() {
                return leiturasCodigo.incrementAndGet() == 1 ? "PTS" : "HACKED";
            }

            @Override
            public int getCasasDecimais() {
                return leiturasCasas.incrementAndGet() == 1 ? 0 : 99;
            }

            @Override
            public String getSimbolo() {
                return leiturasSimbolo.incrementAndGet() == 1 ? "pt" : null;
            }
        };

        Moeda snapshot = AritmeticaMonetaria.capturarSnapshot(moedaToctou);
        assertEquals(1, leiturasCodigo.get());
        assertEquals(1, leiturasCasas.get());
        assertEquals(1, leiturasSimbolo.get());
        assertEquals("PTS", snapshot.getCodigo());
        assertEquals(0, snapshot.getCasasDecimais());
        assertEquals("pt", snapshot.getSimbolo());
    }

    @Test
    void equivalenciaDeMoedaSimetricaPorCodigoECasas() {
        Moeda brlCustomSimbolo = Moeda.of("BRL", 2, "Reais");
        assertTrue(Moeda.mesmaMoeda(Moeda.BRL, brlCustomSimbolo));
        assertEquals(Moeda.BRL, brlCustomSimbolo);
        assertEquals(Moeda.BRL.hashCode(), brlCustomSimbolo.hashCode());

        Moeda brlQuatroCasas = Moeda.of("BRL", 4, "R$");
        assertFalse(Moeda.mesmaMoeda(Moeda.BRL, brlQuatroCasas));
        assertNotEquals(Moeda.BRL, brlQuatroCasas);
        assertFalse(Moeda.mesmaMoeda(Moeda.BRL, null));
        assertFalse(Moeda.mesmaMoeda(null, Moeda.BRL));

        Moeda externaAnonima = new Moeda() {
            @Override
            public String getCodigo() {
                return "BRL";
            }

            @Override
            public int getCasasDecimais() {
                return 2;
            }
        };
        assertEquals("BRL", externaAnonima.getSimbolo());
        assertTrue(Moeda.mesmaMoeda(Moeda.BRL, externaAnonima));
        assertFalse(Moeda.BRL.equals(externaAnonima));
    }

    @ParameterizedTest
    @CsvSource({
        "10.245,    10.24, 10.25, 10.24, 10.25, 10.25, 10.24",
        "10.255,    10.26, 10.26, 10.25, 10.26, 10.26, 10.25",
        "10.2450001,10.25, 10.25, 10.24, 10.25, 10.25, 10.24",
        "-10.245,  -10.24,-10.25,-10.24,-10.25,-10.24,-10.25",
        "-0.004,     0.00,  0.00,  0.00, -0.01,  0.00, -0.01"
    })
    void seisModosPadraoRespeitamNormaESinais(
            String entrada,
            String espAbnt,
            String espFin,
            String espTrunc,
            String espParaCima,
            String espTeto,
            String espPiso) {
        BigDecimal v = new BigDecimal(entrada);
        assertEquals(new BigDecimal(espAbnt), MetodoArredondamento.ABNT_NBR_5891.arredondar(v, 2));
        assertEquals(new BigDecimal(espFin), MetodoArredondamento.FINANCEIRO.arredondar(v, 2));
        assertEquals(new BigDecimal(espTrunc), MetodoArredondamento.TRUNCAMENTO.arredondar(v, 2));
        assertEquals(new BigDecimal(espParaCima), MetodoArredondamento.PARA_CIMA.arredondar(v, 2));
        assertEquals(new BigDecimal(espTeto), MetodoArredondamento.TETO.arredondar(v, 2));
        assertEquals(new BigDecimal(espPiso), MetodoArredondamento.PISO.arredondar(v, 2));
    }

    @Test
    void mapeamentoRoundingModeEValidacaoDeVizinhosEmPoliticaCustomizada() {
        assertSame(MetodoArredondamento.ABNT_NBR_5891, MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_EVEN));
        assertSame(MetodoArredondamento.FINANCEIRO, MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_UP));
        assertSame(MetodoArredondamento.TRUNCAMENTO, MetodoArredondamento.fromRoundingMode(RoundingMode.DOWN));
        assertSame(MetodoArredondamento.PARA_CIMA, MetodoArredondamento.fromRoundingMode(RoundingMode.UP));
        assertSame(MetodoArredondamento.TETO, MetodoArredondamento.fromRoundingMode(RoundingMode.CEILING));
        assertSame(MetodoArredondamento.PISO, MetodoArredondamento.fromRoundingMode(RoundingMode.FLOOR));

        MetodoArredondamento halfDown = MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_DOWN);
        assertEquals(new BigDecimal("10.24"), halfDown.arredondar(new BigDecimal("10.245"), 2));

        assertThrows(IllegalArgumentException.class, () -> MetodoArredondamento.fromRoundingMode(null));
        assertThrows(IllegalArgumentException.class, () -> MetodoArredondamento.fromRoundingMode(RoundingMode.UNNECESSARY));

        MetodoArredondamento vizinhoImparValido = (val, casas) -> {
            BigDecimal floor = val.setScale(casas, RoundingMode.FLOOR);
            BigDecimal ceil = val.setScale(casas, RoundingMode.CEILING);
            return floor.unscaledValue().testBit(0) ? floor : ceil;
        };
        assertEquals(
                new BigDecimal("10.25"),
                AritmeticaMonetaria.aplicarArredondamentoSeguro(new BigDecimal("10.245"), 2, vizinhoImparValido));

        MetodoArredondamento invalidoArbitrario = (val, casas) -> new BigDecimal("999.00");
        assertThrows(
                IllegalArgumentException.class,
                () -> AritmeticaMonetaria.aplicarArredondamentoSeguro(new BigDecimal("1.234"), 2, invalidoArbitrario));

        MetodoArredondamento invalidoEscala = (val, casas) -> new BigDecimal("1.230");
        assertThrows(
                IllegalArgumentException.class,
                () -> AritmeticaMonetaria.aplicarArredondamentoSeguro(new BigDecimal("1.23"), 2, invalidoEscala));

        MetodoArredondamento invalidoNulo = (val, casas) -> null;
        assertThrows(
                IllegalArgumentException.class,
                () -> AritmeticaMonetaria.aplicarArredondamentoSeguro(new BigDecimal("1.23"), 2, invalidoNulo));
    }

    @Test
    void limitesAntiDosEGramaticaDecimalCanonica() {
        assertEquals(new BigDecimal("0.00"), AritmeticaMonetaria.parseDecimalCanonico("-0.00"));
        assertEquals(new BigDecimal("123.45"), AritmeticaMonetaria.parseDecimalCanonico("+123.45"));

        for (String invalido : new String[] {
            null, "", " ", " 10.00", "10.00 ", "10,00", "1.000.00", "1e2", "10.", ".50", "+-10", "١٢٣",
            "12345678901234567890123456789012345678901234567890123456789012345"
        }) {
            assertThrows(IllegalArgumentException.class, () -> AritmeticaMonetaria.parseDecimalCanonico(invalido));
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> AritmeticaMonetaria.validarLimitesDecimal(new BigDecimal("1E+999999999")));
        assertThrows(
                IllegalArgumentException.class,
                () -> AritmeticaMonetaria.validarLimitesDecimal(new BigDecimal("1E-999999999")));
        assertThrows(
                IllegalArgumentException.class,
                () -> AritmeticaMonetaria.validarLimitesDecimal(
                        new BigDecimal("123456789012345678901234567890123456789")));

        assertEquals(2, PoliticaResiduo.values().length);
    }

    @Test
    void blindagemContraSubclasseMaliciosaDeBigDecimalSimboloUnicodeEMesmaMoedaInvalida() {
        // Achado 2a: subclasse de BigDecimal tentando burlar validação de vizinhos via equals
        MetodoArredondamento politicaMaliciosaEquals = (v, c) -> new BigDecimal("999.00") {
            @Override
            public boolean equals(Object x) {
                return true;
            }
        };
        assertThrows(
                IllegalArgumentException.class,
                () -> Real.de(new BigDecimal("1.234"), politicaMaliciosaEquals));

        // Achado 2b: subclasse de BigDecimal tentando burlar limites anti-DoS mentindo em scale/precision
        BigDecimal decimalMentiroso = new BigDecimal("1E-1000") {
            @Override
            public int scale() {
                return 2;
            }

            @Override
            public int precision() {
                return 1;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> Real.de(decimalMentiroso));

        // Achado 6: Moeda.mesmaMoeda devolve false para moeda inválida sem lançar exceção e ignora símbolo inválido
        Moeda moedaCasasInvalidas = new Moeda() {
            @Override
            public String getCodigo() {
                return "BRL";
            }

            @Override
            public int getCasasDecimais() {
                return 99;
            }
        };
        assertFalse(Moeda.mesmaMoeda(Moeda.BRL, moedaCasasInvalidas));

        Moeda brlSimboloLongo = new Moeda() {
            @Override
            public String getCodigo() {
                return "BRL";
            }

            @Override
            public int getCasasDecimais() {
                return 2;
            }

            @Override
            public String getSimbolo() {
                return "SIMBOLO_MUITO_LONGO_MAIS_DE_16_CHARS";
            }
        };
        assertTrue(Moeda.mesmaMoeda(Moeda.BRL, brlSimboloLongo));

        // Achado 7: delegar a modo padrão dentro de política customizada com produto de escala 40
        MetodoArredondamento delegandoPadrao = (v, c) -> MetodoArredondamento.FINANCEIRO.arredondar(v, c);
        BigDecimal fatorEscala38 = new BigDecimal("1.0000000000000000000000000000000000001");
        assertEquals(Real.de("10.00"), Real.de("10.00", delegandoPadrao).multiplicar(fatorEscala38));

        // Achado 8: fromJdkCurrency independente do Locale default da JVM
        java.util.Locale anterior = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.US);
            String simbUs = Moeda.fromJdkCurrency(Currency.getInstance("USD"), 2).getSimbolo();
            java.util.Locale.setDefault(new java.util.Locale("pt", "BR"));
            String simbBr = Moeda.fromJdkCurrency(Currency.getInstance("USD"), 2).getSimbolo();
            assertEquals(simbUs, simbBr);
        } finally {
            java.util.Locale.setDefault(anterior);
        }

        // Achado 11: rejeitar controles C1 (NEL), formatação bidirecional (RTL override) e surrogates isolados
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("X", 2, "\u0085"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("X", 2, "\u202E"));
        assertThrows(IllegalArgumentException.class, () -> Moeda.of("X", 2, "\uD800"));
    }
}
