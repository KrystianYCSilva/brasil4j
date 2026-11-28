package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabelaProgressivaTest {

    @Test
    void builderDeUsoUnicoOrdemEstritaMoedaConsistenteEValidacoes() {
        TabelaProgressiva.Builder<Real> builder = TabelaProgressiva.emReais()
                .faixaAte("2000.00", Percentual.ZERO)
                .faixaAte(Real.de("5000.00"), Percentual.de(10L));

        TabelaProgressiva<Real> tabela = builder.faixaAcima(Percentual.de(20L));
        assertEquals(Moeda.BRL, tabela.getMoeda());
        assertEquals(3, tabela.getFaixas().size());

        // Builder selado após faixaAcima lança IllegalStateException em todas as sobrecargas
        assertThrows(
                IllegalStateException.class,
                () -> builder.faixaAte("8000.00", Percentual.de(25L)));
        assertThrows(
                IllegalStateException.class,
                () -> builder.faixaAte(Real.de("8000.00"), Percentual.de(25L)));
        assertThrows(IllegalStateException.class, () -> builder.faixaAcima(Percentual.de(25L)));
        assertThrows(IllegalArgumentException.class, () -> tabela.apurar(null));
        assertThrows(IllegalArgumentException.class, () -> tabela.apurar(null, ModoApuracaoProgressiva.NO_TOTAL));
        assertThrows(
                IllegalArgumentException.class,
                () -> tabela.apurar(null, ModoApuracaoProgressiva.NO_TOTAL, MetodoArredondamento.FINANCEIRO));

        // Limite zero, negativo, repetido ou decrescente e alíquota negativa lançam IllegalArgumentException
        assertThrows(
                IllegalArgumentException.class,
                () -> TabelaProgressiva.emReais().faixaAte("0.00", Percentual.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> TabelaProgressiva.emReais()
                        .faixaAte("2000.00", Percentual.ZERO)
                        .faixaAte("2000.00", Percentual.de(10L)));
        assertThrows(
                IllegalArgumentException.class,
                () -> TabelaProgressiva.emReais()
                        .faixaAte("2000.00", Percentual.ZERO)
                        .faixaAte("1999.99", Percentual.de(10L)));
        assertThrows(
                IllegalArgumentException.class,
                () -> TabelaProgressiva.emReais().faixaAte("1000.00", Percentual.de("-1")));
        assertThrows(
                IllegalArgumentException.class,
                () -> TabelaProgressiva.emReais().faixaAcima(Percentual.de("-1")));

        // paraMoeda confere moeda dos limites e da base em tempo de execução
        TabelaProgressiva<Dinheiro> tabelaUsd = TabelaProgressiva.paraMoeda(Moeda.USD)
                .faixaAte("1000.00", Percentual.ZERO)
                .faixaAcima(Percentual.de(15L));
        assertThrows(IllegalArgumentException.class, () -> tabelaUsd.apurar(Real.de("500.00")));
        assertThrows(
                IllegalArgumentException.class,
                () -> TabelaProgressiva.paraMoeda(Moeda.USD).faixaAte(Real.de("500.00"), Percentual.ZERO));
        assertThrows(IllegalArgumentException.class, () -> tabela.apurar(Real.de("-0.01")));
    }

    @Test
    void apuracaoEmFronteirasBaseZeroTabelaAbertaETabelaComTetoNosDoisModos() {
        // 1. Tabela aberta sintética: [0..2000] 0%, (2000..4000] 10%, >4000 20%
        TabelaProgressiva<Real> tabelaAberta = TabelaProgressiva.emReais()
                .faixaAte("2000.00", Percentual.ZERO)
                .faixaAte("4000.00", Percentual.de(10L))
                .faixaAcima(Percentual.de(20L));

        // Parcelas a deduzir exatas normalizadas (scale >= 0, sem notação científica): faixa 1 = 0, faixa 2 = 200, faixa 3 = 600
        assertEquals(new BigDecimal("0"), tabelaAberta.getFaixas().get(0).getParcelaADeduzirExata());
        assertEquals(new BigDecimal("200"), tabelaAberta.getFaixas().get(1).getParcelaADeduzirExata());
        assertEquals(new BigDecimal("600"), tabelaAberta.getFaixas().get(2).getParcelaADeduzirExata());

        // Base zero -> total 0, alíquota efetiva 0%
        ApuracaoProgressiva<Real> apZero = tabelaAberta.apurar(Real.ZERO);
        assertEquals(Real.ZERO, apZero.getValorTotal());
        assertEquals(Percentual.ZERO, apZero.getAliquotaEfetiva(2, MetodoArredondamento.FINANCEIRO));
        assertEquals(3, apZero.getFaixas().size());

        // Base exatamente na fronteira L_1 (2000.00) -> fica 100% na faixa 1!
        ApuracaoProgressiva<Real> apFronteira = tabelaAberta.apurar(Real.de("2000.00"));
        assertEquals(Real.de("2000.00"), apFronteira.getFaixas().get(0).getBaseNaFaixa());
        assertEquals(Real.ZERO, apFronteira.getFaixas().get(1).getBaseNaFaixa());
        assertEquals(Real.ZERO, apFronteira.getValorTotal());

        // Base 5000.00 -> 2000*0% + 2000*10% (200) + 1000*20% (200) = 400.00 (efetiva = 8%)
        ApuracaoProgressiva<Real> ap5000 = tabelaAberta.apurar(Real.de("5000.00"));
        assertEquals(Real.de("400.00"), ap5000.getValorTotal());
        assertEquals(Percentual.de("8"), ap5000.getAliquotaEfetiva(2, MetodoArredondamento.FINANCEIRO));

        // 2. Divergência controlada entre NO_TOTAL e POR_FAIXA + reconciliação Hamilton da memória (Achado #7):
        // Duas faixas gerando R$ 0,004 cada (0.40 * 1% = 0.004 e 0.40 * 1% = 0.004 -> soma exata = 0.008)
        TabelaProgressiva<Real> microTabela = TabelaProgressiva.emReais()
                .faixaAte("0.40", Percentual.de(1L))
                .faixaAcima(Percentual.de(1L));

        ApuracaoProgressiva<Real> noTotal = microTabela.apurar(
                Real.de("0.80"), ModoApuracaoProgressiva.NO_TOTAL, MetodoArredondamento.FINANCEIRO);
        ApuracaoProgressiva<Real> porFaixa = microTabela.apurar(
                Real.de("0.80"), ModoApuracaoProgressiva.POR_FAIXA, MetodoArredondamento.FINANCEIRO);

        // Soma exata é 0.008 em ambos!
        assertEquals(new BigDecimal("0.008"), noTotal.getValorExatoTotal());
        assertEquals(new BigDecimal("0.008"), porFaixa.getValorExatoTotal());

        // Em NO_TOTAL: round(0.008, HALF_UP) = 0.01, e a soma dos valorApurado reconciliados fecha 0.01!
        assertEquals(Real.de("0.01"), noTotal.getValorTotal());
        assertEquals(new BigDecimal("0.002"), noTotal.getDiferencaArredondamento());
        Real somaApuradaNoTotal = Real.ZERO;
        for (FaixaApurada<Real> f : noTotal.getFaixas()) {
            assertEquals(Real.ZERO, f.getValorArredondadoIndividual());
            somaApuradaNoTotal = somaApuradaNoTotal.somar(f.getValorApurado());
        }
        assertEquals(noTotal.getValorTotal(), somaApuradaNoTotal);

        // Em POR_FAIXA: 0.00 + 0.00 = 0.00!
        assertEquals(Real.ZERO, porFaixa.getValorTotal());
        assertEquals(new BigDecimal("-0.008"), porFaixa.getDiferencaArredondamento());

        // 3. Tabela com teto (faixaAcima = 0%) e truncamento por faixa:
        TabelaProgressiva<Real> tabelaComTeto = TabelaProgressiva.emReais()
                .faixaAte("1000.00", Percentual.de("7.5"))
                .faixaAte("3000.00", Percentual.de("9"))
                .faixaAcima(Percentual.ZERO);

        assertTrue(tabelaComTeto.getFaixas().get(2).getParcelaADeduzirExata().signum() < 0);
        ApuracaoProgressiva<Real> acimaDoTeto = tabelaComTeto.apurar(
                Real.de("10000.00"), ModoApuracaoProgressiva.POR_FAIXA, MetodoArredondamento.TRUNCAMENTO);
        // 1000 * 7.5% = 75.00; 2000 * 9% = 180.00; 7000 * 0% = 0.00 -> total = 255.00
        assertEquals(Real.de("255.00"), acimaDoTeto.getValorTotal());
        assertEquals(Real.de("7000.00"), acimaDoTeto.getFaixas().get(2).getBaseNaFaixa());
        assertEquals(Real.ZERO, acimaDoTeto.getFaixas().get(2).getValorApurado());

        // Também funciona em Dolar e Euro
        TabelaProgressiva<Dolar> tabUsd = TabelaProgressiva.emDolares()
                .faixaAte("100.00", Percentual.ZERO)
                .faixaAcima(Percentual.de(10L));
        assertEquals(Dolar.de("5.00"), tabUsd.apurar(Dolar.de("150.00")).getValorTotal());

        TabelaProgressiva<Euro> tabEur = TabelaProgressiva.emEuros()
                .faixaAte("100.00", Percentual.ZERO)
                .faixaAcima(Percentual.de(10L));
        assertEquals(Euro.de("5.00"), tabEur.apurar(Euro.de("150.00")).getValorTotal());
    }

    @Test
    void reconciliacaoMaioresRestosEmModosDirecionaisBordasDeMilFaixasMoedasIsoEIgualdadePorValor() {
        // Contraexemplo A2 (Claude): faixa 1 tem R$ 75,00 exatos (resto 0) e faixa 2 tem R$ 0,009 (resto 0,9 centavo).
        // Em NO_TOTAL + TRUNCAMENTO/PISO: total = 75,00 -> faixa 1 DEVE permanecer 75,00 (nunca 74,99!) e faixa 2 = 0,00.
        // Em NO_TOTAL + TETO/PARA_CIMA: total = 75,01 -> faixa 1 permanece 75,00 e faixa 2 recebe o centavo (0,01).
        TabelaProgressiva<Real> tabA2 = TabelaProgressiva.emReais()
                .faixaAte("1000.00", Percentual.de("7.5"))
                .faixaAcima(Percentual.de("9"));
        Real baseA2 = Real.de("1000.10");

        for (MetodoArredondamento mBaixo :
                new MetodoArredondamento[] {MetodoArredondamento.TRUNCAMENTO, MetodoArredondamento.PISO}) {
            ApuracaoProgressiva<Real> ap = tabA2.apurar(baseA2, ModoApuracaoProgressiva.NO_TOTAL, mBaixo);
            assertEquals(Real.de("75.00"), ap.getValorTotal());
            assertEquals(Real.de("75.00"), ap.getFaixas().get(0).getValorApurado());
            assertEquals(Real.de("0.00"), ap.getFaixas().get(1).getValorApurado());
        }
        for (MetodoArredondamento mAlto : new MetodoArredondamento[] {
            MetodoArredondamento.TETO,
            MetodoArredondamento.PARA_CIMA,
            MetodoArredondamento.FINANCEIRO,
            MetodoArredondamento.ABNT_NBR_5891,
            MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_DOWN)
        }) {
            ApuracaoProgressiva<Real> ap = tabA2.apurar(baseA2, ModoApuracaoProgressiva.NO_TOTAL, mAlto);
            assertEquals(Real.de("75.01"), ap.getValorTotal());
            assertEquals(Real.de("75.00"), ap.getFaixas().get(0).getValorApurado());
            assertEquals(Real.de("0.01"), ap.getFaixas().get(1).getValorApurado());
        }

        // Em TETO quando w1 = 0.001 e w2 = 0.009 -> W = 0.010, T = 0.01 -> faixa 2 (maior resto 0.9) recebe 0.01:
        TabelaProgressiva<Real> tabTeto = TabelaProgressiva.emReais()
                .faixaAte("0.10", Percentual.de(1L))
                .faixaAcima(Percentual.de("9"));
        ApuracaoProgressiva<Real> apTeto =
                tabTeto.apurar(Real.de("0.20"), ModoApuracaoProgressiva.NO_TOTAL, MetodoArredondamento.TETO);
        assertEquals(Real.de("0.01"), apTeto.getValorTotal());
        assertEquals(Real.de("0.00"), apTeto.getFaixas().get(0).getValorApurado());
        assertEquals(Real.de("0.01"), apTeto.getFaixas().get(1).getValorApurado());

        // Política customizada (M1): escolhe sempre o vizinho FLOOR
        MetodoArredondamento customFloor = (valor, casas) -> valor.setScale(casas, RoundingMode.FLOOR);
        ApuracaoProgressiva<Real> apCustom =
                tabA2.apurar(baseA2, ModoApuracaoProgressiva.NO_TOTAL, customFloor);
        assertEquals(Real.de("75.00", customFloor), apCustom.getValorTotal());
        assertEquals(Real.de("75.00", customFloor), apCustom.getFaixas().get(0).getValorApurado());

        // faixaAcima(null) lança IAE sem selar o builder (M1)
        TabelaProgressiva.Builder<Real> bNulo = TabelaProgressiva.emReais().faixaAte("100.00", Percentual.ZERO);
        assertThrows(IllegalArgumentException.class, () -> bNulo.faixaAcima(null));
        TabelaProgressiva<Real> recuperada = bNulo.faixaAcima(Percentual.de(5L));
        assertEquals(2, recuperada.getFaixas().size());

        // Limite de 999 faixas limitadas + 1 aberta = 1000 faixas; 1000ª faixaAte falha com IAE (M1)
        TabelaProgressiva.Builder<Real> bMil = TabelaProgressiva.emReais();
        for (int i = 1; i <= 999; i++) {
            bMil.faixaAte(Real.deCentavos(i * 100L), Percentual.de(1L));
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> bMil.faixaAte(Real.deCentavos(1000L * 100L), Percentual.de(1L)));
        TabelaProgressiva<Real> tabMil = bMil.faixaAcima(Percentual.de(2L));
        assertEquals(1000, tabMil.getFaixas().size());

        // Moedas ISO de 0 casas (JPY) e 3 casas (KWD) (M1)
        Moeda jpy = Moeda.iso("JPY", 0);
        TabelaProgressiva<Dinheiro> tabJpy = TabelaProgressiva.paraMoeda(jpy)
                .faixaAte("1000", Percentual.ZERO)
                .faixaAcima(Percentual.de(10L));
        assertEquals(QuantiaMonetaria.de("50", jpy), tabJpy.apurar(QuantiaMonetaria.de("1500", jpy)).getValorTotal());

        Moeda kwd = Moeda.iso("KWD", 3);
        TabelaProgressiva<Dinheiro> tabKwd = TabelaProgressiva.paraMoeda(kwd)
                .faixaAte("10.000", Percentual.ZERO)
                .faixaAcima(Percentual.de(10L));
        assertEquals(
                QuantiaMonetaria.de("0.500", kwd),
                tabKwd.apurar(QuantiaMonetaria.de("15.000", kwd)).getValorTotal());

        // getAliquotaEfetiva rejeita casasPercentuais < 0 ou > 18 com mensagem clara sobre casas percentuais (M1 + B4)
        IllegalArgumentException exNeg = assertThrows(
                IllegalArgumentException.class,
                () -> apCustom.getAliquotaEfetiva(-1, MetodoArredondamento.FINANCEIRO));
        assertTrue(exNeg.getMessage().contains("casas percentuais"));
        assertThrows(
                IllegalArgumentException.class,
                () -> apCustom.getAliquotaEfetiva(19, MetodoArredondamento.FINANCEIRO));
        assertEquals(Percentual.de(7L), apCustom.getAliquotaEfetiva(0, MetodoArredondamento.TRUNCAMENTO));
        assertNotEquals(Percentual.ZERO, apCustom.getAliquotaEfetiva(18, MetodoArredondamento.FINANCEIRO));

        // Igualdade por valor e toString auditável em TabelaProgressiva, FaixaProgressiva, FaixaApurada e ApuracaoProgressiva (B2)
        TabelaProgressiva<Real> tabA2Copia = TabelaProgressiva.emReais()
                .faixaAte("1000.00", Percentual.de("7.5"))
                .faixaAcima(Percentual.de("9"));
        assertEquals(tabA2, tabA2);
        assertEquals(tabA2, tabA2Copia);
        assertNotEquals(tabA2, "tabela");
        assertEquals(tabA2.hashCode(), tabA2Copia.hashCode());
        assertNotEquals(0, tabA2.hashCode());
        assertNotEquals(tabA2, tabTeto);
        assertTrue(tabA2.toString().contains("BRL"));
        assertEquals(1, tabA2.getFaixas().get(0).getIndice());
        assertEquals(2, tabA2.getFaixas().get(1).getIndice());
        assertEquals(tabA2.getFaixas().get(0), tabA2.getFaixas().get(0));
        assertNotEquals(tabA2.getFaixas().get(0), "faixa");
        assertNotEquals(tabA2.getFaixas().get(0), tabA2.getFaixas().get(1));
        assertNotEquals(0, tabA2.getFaixas().get(0).hashCode());
        assertTrue(tabA2.getFaixas().get(1).toString().contains("+infinito"));

        ApuracaoProgressiva<Real> ap1 =
                tabA2.apurar(baseA2, ModoApuracaoProgressiva.NO_TOTAL, MetodoArredondamento.FINANCEIRO);
        ApuracaoProgressiva<Real> ap2 =
                tabA2Copia.apurar(baseA2, ModoApuracaoProgressiva.NO_TOTAL, MetodoArredondamento.FINANCEIRO);
        ApuracaoProgressiva<Real> apPorFaixa =
                tabA2.apurar(baseA2, ModoApuracaoProgressiva.POR_FAIXA);
        assertEquals(baseA2, ap1.getBaseCalculo());
        assertEquals(ModoApuracaoProgressiva.NO_TOTAL, ap1.getModo());
        assertEquals(MetodoArredondamento.FINANCEIRO, ap1.getArredondamento());
        assertEquals(ModoApuracaoProgressiva.POR_FAIXA, apPorFaixa.getModo());
        assertEquals(new BigDecimal("75"), ap1.getFaixas().get(0).getValorExato());
        assertEquals(ap1, ap1);
        assertEquals(ap1, ap2);
        assertNotEquals(ap1, "ap");
        assertNotEquals(ap1, apPorFaixa);
        assertEquals(ap1.hashCode(), ap2.hashCode());
        assertNotEquals(0, ap1.hashCode());
        assertEquals(ap1.getFaixas().get(0), ap1.getFaixas().get(0));
        assertEquals(ap1.getFaixas().get(0), ap2.getFaixas().get(0));
        assertNotEquals(ap1.getFaixas().get(0), "fa");
        assertNotEquals(ap1.getFaixas().get(0), ap1.getFaixas().get(1));
        assertNotEquals(0, ap1.getFaixas().get(0).hashCode());
        assertTrue(ap1.toString().contains("75.01"));
        assertTrue(ap1.getFaixas().get(0).toString().contains("75.00"));
        assertTrue(ap1.getFaixas().get(0).getFaixa().toString().contains("7.5%"));
    }
}
