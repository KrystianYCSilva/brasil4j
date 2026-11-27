package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.financas.Dinheiro;
import io.github.brasil4j.financas.Dolar;
import io.github.brasil4j.financas.Euro;
import io.github.brasil4j.financas.MetodoArredondamento;
import io.github.brasil4j.financas.Moeda;
import io.github.brasil4j.financas.PoliticaResiduo;
import io.github.brasil4j.financas.QuantiaMonetaria;
import io.github.brasil4j.financas.Real;
import io.github.brasil4j.financas.TaxaCambio;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemplos completos e casos de uso reais do contexto de Finanças em Java 8
 * ({@link Moeda}, {@link MetodoArredondamento}, {@link Real}, {@link Dolar}, {@link Euro},
 * {@link QuantiaMonetaria}, {@link Dinheiro}, {@link PoliticaResiduo} e {@link TaxaCambio}).
 */
@DisplayName("Exemplos Java · Finanças (Moedas, Arredondamentos, Operações, Rateio e Câmbio)")
class FinancasExemplosTest {

    /** Exemplo de unidade interna do consumidor implementando diretamente a interface aberta {@link Moeda}. */
    private enum UnidadeInterna implements Moeda {
        PONTOS_FIDELIDADE("PTS", 0, "pts"),
        CREDITO_ENERGIA_KWH("KWH", 4, "kWh");

        private final String codigo;
        private final int casas;
        private final String simbolo;

        UnidadeInterna(String codigo, int casas, String simbolo) {
            this.codigo = codigo;
            this.casas = casas;
            this.simbolo = simbolo;
        }

        @Override
        public String getCodigo() {
            return codigo;
        }

        @Override
        public int getCasasDecimais() {
            return casas;
        }

        @Override
        public String getSimbolo() {
            return simbolo;
        }
    }

    @Test
    @DisplayName("Catálogo completo de políticas de arredondamento (positivos, negativos, RoundingMode e política customizada)")
    void exemploCatalogoCompletoDePoliticasDeArredondamento() {
        // 1. Empate exato (.xx5): ABNT NBR 5891 vai para o par; FINANCEIRO afasta de zero; HALF_DOWN aproxima de zero
        MetodoArredondamento meioParaBaixo = MetodoArredondamento.fromRoundingMode(RoundingMode.HALF_DOWN);
        assertEquals(Real.de("10.24"), Real.de("10.245", MetodoArredondamento.ABNT_NBR_5891));
        assertEquals(Real.de("10.26"), Real.de("10.255", MetodoArredondamento.ABNT_NBR_5891));
        assertEquals(Real.de("10.25"), Real.de("10.2450001", MetodoArredondamento.ABNT_NBR_5891));
        assertEquals(Real.de("10.25"), Real.de("10.245", MetodoArredondamento.FINANCEIRO));
        assertEquals(Real.de("10.24"), Real.de("10.245", meioParaBaixo));
        assertThrows(
                IllegalArgumentException.class,
                () -> MetodoArredondamento.fromRoundingMode(RoundingMode.UNNECESSARY));

        // 2. Diferença entre TRUNCAMENTO/PARA_CIMA (simétricos em módulo) e PISO/TETO (direcionados na reta real) em negativos:
        assertEquals(Real.de("-10.24"), Real.de("-10.241", MetodoArredondamento.TRUNCAMENTO)); // aproxima de zero
        assertEquals(Real.de("-10.25"), Real.de("-10.241", MetodoArredondamento.PISO));        // vai para -infinito
        assertEquals(Real.de("-10.25"), Real.de("-10.241", MetodoArredondamento.PARA_CIMA));   // afasta de zero
        assertEquals(Real.de("-10.24"), Real.de("-10.241", MetodoArredondamento.TETO));        // vai para +infinito

        // 3. Troca explícita da política retida via comArredondamento e sobrescrita pontual em dividir(divisor, politica):
        Real basePadrao = Real.de("100.00");
        Real baseComTeto = basePadrao.comArredondamento(MetodoArredondamento.TETO);
        assertSame(MetodoArredondamento.ABNT_NBR_5891, basePadrao.getArredondamento());
        assertSame(MetodoArredondamento.TETO, baseComTeto.getArredondamento());
        assertEquals(basePadrao, baseComTeto); // igualdade contábil ignora a política retida!
        assertEquals(Real.de("33.33"), basePadrao.dividir(3L));
        assertEquals(Real.de("33.34"), basePadrao.dividir(3L, MetodoArredondamento.TETO));
        assertEquals(Real.de("33.34"), baseComTeto.dividir(3L));

        // 4. Política ilustrativa (não normativa) via lambda — toda implementação deve devolver FLOOR ou CEILING na escala da moeda:
        MetodoArredondamento sempreParaVizinhoPar = (valor, casas) -> {
            BigDecimal floor = valor.setScale(casas, RoundingMode.FLOOR);
            BigDecimal ceil = valor.setScale(casas, RoundingMode.CEILING);
            return floor.unscaledValue().testBit(0) ? ceil : floor;
        };
        assertEquals(Real.de("10.24"), Real.de("10.231", sempreParaVizinhoPar));
    }

    @Test
    @DisplayName("Fábricas seguras, inspeção de sinais, igualdade contábil e ordenação natural")
    void exemploFabricasInspecaoDeSinalComparacaoEOrdenacao() {
        // 1. Modo exato rejeita casas excedentes (protege importação de XML fiscal/SPED contra truncamento silencioso):
        Real itemNota = Real.exato("150.5");
        assertEquals(new BigDecimal("150.50"), itemNota.getValor());
        assertEquals(Real.de("150.50"), Real.exato(new BigDecimal("150.5")));
        assertThrows(IllegalArgumentException.class, () -> Real.exato("150.555"));
        assertThrows(IllegalArgumentException.class, () -> Dolar.exato("10.999"));
        assertThrows(IllegalArgumentException.class, () -> Euro.exato("10.999"));

        // 2. Fábricas por inteiros e por unidades mínimas/centavos (integração com CNAB/arquivos posicionais):
        Real boleto = Real.deCentavos(15099L);
        assertEquals(Moeda.BRL, boleto.getMoeda());
        assertEquals(Real.de("150.99"), boleto);
        assertEquals(15099L, boleto.getCentavos());
        assertEquals(15099L, boleto.emUnidadesMinimas());
        assertEquals(Real.de("150.00"), Real.deInteiro(150L));
        assertEquals(Dolar.de("25.50"), Dolar.deCentavos(2550L));
        assertEquals(Euro.de("40.00"), Euro.deInteiro(40L));
        assertEquals(Real.de("150.99"), Dinheiro.deUnidadesMinimas(15099L, Moeda.BRL));
        assertEquals(Real.de("150.50"), Dinheiro.exato("150.5", Moeda.BRL));
        assertSame(Real.ZERO, Dinheiro.zero(Moeda.BRL));

        // 3. Inspeção de sinal (isZero, isPositivo, isNegativo) e inversão/módulo (negar, abs):
        Real estorno = boleto.negar();
        assertTrue(estorno.isNegativo());
        assertFalse(estorno.isPositivo());
        assertFalse(estorno.isZero());
        assertEquals(boleto, estorno.abs());
        assertTrue(Real.ZERO.negar().isZero());
        assertEquals("BRL 0.00", Real.ZERO.negar().toString()); // zero normalizado sem sinal negativo!

        // 4. Ordenação natural em coleções tipadas e comparação polimórfica segura por moeda:
        List<Real> lancamentos = new ArrayList<>(Arrays.asList(
                Real.de("250.00"), Real.de("-40.00"), Real.ZERO, Real.de("19.90")));
        Collections.sort(lancamentos);
        assertEquals(
                Arrays.asList(Real.de("-40.00"), Real.ZERO, Real.de("19.90"), Real.de("250.00")),
                lancamentos);

        Dinheiro d1 = Real.de("10.00");
        Dinheiro d2 = Real.de("20.00");
        assertTrue(d1.compararMesmaMoeda(d2) < 0);
        assertThrows(IllegalArgumentException.class, () -> d1.compararMesmaMoeda(Dolar.de("10.00")));
    }

    @Test
    @DisplayName("Moedas ISO de 0 e 3 casas, ponte java.util.Currency e unidades customizadas implementando Moeda")
    void exemploMoedasAbertasPonteJdkEUnidadesCustomizadas() {
        // 1. Constantes prontas e equivalência contábil por (código, casasDecimais):
        assertEquals("R$", Moeda.BRL.getSimbolo());
        assertEquals("US$", Moeda.USD.getSimbolo());
        assertEquals("€", Moeda.EUR.getSimbolo());
        assertTrue(Moeda.mesmaMoeda(Moeda.BRL, Moeda.of("BRL", 2, "R$")));
        assertFalse(Moeda.mesmaMoeda(Moeda.BRL, Moeda.USD));

        // 2. Moedas ISO com 0 casas (JPY) e 3 casas decimais (KWD — Dinar Kuwaitiano em milésimos/fils):
        Moeda iene = Moeda.iso("JPY", 0);
        QuantiaMonetaria reservaJpy = QuantiaMonetaria.deInteiro(2500L, iene);
        assertEquals("JPY 2500", reservaJpy.toString());
        assertEquals(2500L, reservaJpy.emUnidadesMinimas());

        Moeda dinarKuwaitiano = Moeda.iso("KWD", 3);
        QuantiaMonetaria remessaKwd = QuantiaMonetaria.deUnidadesMinimas(1250L, dinarKuwaitiano); // 1.250 KWD
        assertEquals("KWD 1.250", remessaKwd.toString());

        // 3. Ponte determinística com java.util.Currency (casas explícitas independentes da versão da JDK):
        Moeda libra = Moeda.fromJdkCurrency(Currency.getInstance("GBP"), 2);
        QuantiaMonetaria servicoUk = QuantiaMonetaria.de("85.40", libra);
        assertEquals("GBP 85.40", servicoUk.toString());

        // 4. Enum próprio do consumidor implementando Moeda (ex.: pontos de fidelidade e créditos em kWh):
        Dinheiro saldoPontos = Dinheiro.deInteiro(1200L, UnidadeInterna.PONTOS_FIDELIDADE);
        Dinheiro creditoEnergia = Dinheiro.de("18.7525", UnidadeInterna.CREDITO_ENERGIA_KWH);
        assertEquals("PTS 1200", saldoPontos.toString());
        assertEquals("KWH 18.7525", creditoEnergia.toString());

        // Segurança contra mistura de moedas no supertipo polimórfico:
        assertThrows(IllegalArgumentException.class, () -> saldoPontos.somarMesmaMoeda(creditoEnergia));
    }

    @Test
    @DisplayName("Caso de uso Faturamento: fechamento de nota de serviço com desconto comercial e retenções")
    void exemploFechamentoDeNotaDeServicoComDescontoERetencao() {
        // 12 horas técnicas a R$ 185,50/h
        Real valorHora = Real.de("185.50");
        Real valorBruto = valorHora.multiplicar(12L); // R$ 2.226,00
        assertEquals(Real.de("2226.00"), valorBruto);

        // Desconto comercial contratual de 7,5% com arredondamento FINANCEIRO (HALF_UP):
        Real desconto = valorBruto.multiplicar(new BigDecimal("0.075"), MetodoArredondamento.FINANCEIRO);
        assertEquals(Real.de("166.95"), desconto);

        Real baseTributavel = valorBruto.subtrair(desconto); // R$ 2.059,05
        assertEquals(Real.de("2059.05"), baseTributavel);

        // O consumidor aplica sua alíquota própria (ex.: retenção contratual de 4,65% truncada conforme regra do leiaute):
        Real retencaoFonte = baseTributavel.multiplicar(new BigDecimal("0.0465"), MetodoArredondamento.TRUNCAMENTO);
        assertEquals(Real.de("95.74"), retencaoFonte); // 2059.05 * 0.0465 = 95.745825 -> truncado = 95.74

        Real liquidoAReceber = baseTributavel.subtrair(retencaoFonte);
        assertEquals(Real.de("1963.31"), liquidoAReceber);
    }

    @Test
    @DisplayName("Caso de uso Crediário: cronograma com entrada + parcelas e estorno parcelado sem perda de centavo")
    void exemploCronogramaDeParcelasComEntradaERateioDeCentavos() {
        Real valorContrato = Real.de("1250.00");
        Real entrada = valorContrato.multiplicar(new BigDecimal("0.20")); // R$ 250,00
        Real saldoFinanciado = valorContrato.subtrair(entrada);          // R$ 1.000,00 em 6x

        // Ajuste de centavos nas primeiras parcelas (padrão) vs. na última parcela:
        List<Real> seisParcelasInicio = saldoFinanciado.ratear(6, PoliticaResiduo.PRIMEIRAS_PARCELAS);
        List<Real> seisParcelasFim = saldoFinanciado.ratear(6, PoliticaResiduo.ULTIMAS_PARCELAS);

        assertEquals(
                Arrays.asList(
                        Real.de("166.67"), Real.de("166.67"), Real.de("166.67"),
                        Real.de("166.67"), Real.de("166.66"), Real.de("166.66")),
                seisParcelasInicio);
        assertEquals(
                Arrays.asList(
                        Real.de("166.66"), Real.de("166.66"), Real.de("166.67"),
                        Real.de("166.67"), Real.de("166.67"), Real.de("166.67")),
                seisParcelasFim);

        // Conferência contábil: entrada + soma das parcelas == contrato integral
        Real somaPlano = entrada;
        for (Real p : seisParcelasFim) {
            somaPlano = somaPlano.somar(p);
        }
        assertEquals(valorContrato, somaPlano);

        // Também funciona simetricamente para notas de crédito/estornos negativos:
        List<Real> estornoEmTres = Real.de("-100.00").ratear(3);
        assertEquals(Arrays.asList(Real.de("-33.34"), Real.de("-33.33"), Real.de("-33.33")), estornoEmTres);
    }

    @Test
    @DisplayName("Caso de uso NF-e: rateio proporcional de frete e desconto entre itens pelo método de Hamilton")
    void exemploRateioProporcionalDeFreteEDescontoEmNotaFiscal() {
        // 4 itens na nota: brinde promocional (peso 0) e três mercadorias com valores distintos
        List<BigDecimal> pesosMercadorias = Arrays.asList(
                BigDecimal.ZERO,
                new BigDecimal("199.90"),
                new BigDecimal("89.90"),
                new BigDecimal("49.90"));

        Real freteTotal = Real.de("45.00");
        Real descontoGlobal = Real.de("20.00");

        List<Real> fretePorItem = freteTotal.alocar(pesosMercadorias);
        List<Real> descontoPorItem = descontoGlobal.alocar(pesosMercadorias);

        // O item de peso zero (brinde) jamais recebe centavo residual:
        assertEquals(Real.ZERO, fretePorItem.get(0));
        assertEquals(Real.ZERO, descontoPorItem.get(0));

        // A soma dos valores alocados fecha exatamente com o cabeçalho da NF-e (zero erro de arredondamento):
        assertEquals(freteTotal, fretePorItem.stream().reduce(Real.ZERO, Real::somar));
        assertEquals(descontoGlobal, descontoPorItem.stream().reduce(Real.ZERO, Real::somar));

        // Rateio e alocação também estão disponíveis em Dolar, Euro e QuantiaMonetaria (ex.: KWD com 3 casas):
        assertEquals(
                Arrays.asList(Dolar.de("33.34"), Dolar.de("33.33"), Dolar.de("33.33")),
                Dolar.de("100.00").ratear(3));
        assertEquals(
                Arrays.asList(Euro.de("60.00"), Euro.de("40.00")),
                Euro.de("100.00").alocar(Arrays.asList(new BigDecimal("3"), new BigDecimal("2"))));
        Moeda kwd = Moeda.iso("KWD", 3);
        assertEquals(
                Arrays.asList(
                        QuantiaMonetaria.de("0.334", kwd),
                        QuantiaMonetaria.de("0.333", kwd),
                        QuantiaMonetaria.de("0.333", kwd)),
                QuantiaMonetaria.de("1.000", kwd).ratear(3));
    }

    @Test
    @DisplayName("Caso de uso Tesouraria: câmbio bidirecional, par cruzado EUR/USD e consolidação multimoeda em BRL")
    void exemploTesourariaMultimoedaConversaoCruzadaEConsolidacaoEmBrl() {
        TaxaCambio ptaxUsd = TaxaCambio.umDolarEmReais("5.4832");
        TaxaCambio ptaxEur = TaxaCambio.umEuroEmReais(new BigDecimal("6.1000"));
        TaxaCambio eurUsd = TaxaCambio.de(Moeda.EUR, Moeda.USD, "1.1125");

        // 1. Inspeção de metadados, involução exata de inverter() e fronteiras de validação de TaxaCambio:
        assertEquals(Moeda.USD, ptaxUsd.getMoedaOrigem());
        assertEquals(Moeda.BRL, ptaxUsd.getMoedaDestino());
        assertEquals(new BigDecimal("5.4832"), ptaxUsd.getCotacaoBase());
        assertFalse(ptaxUsd.isInvertida());
        assertTrue(ptaxUsd.inverter().isInvertida());
        assertEquals(ptaxUsd, ptaxUsd.inverter().inverter()); // involução exata sem perda de precisão!
        assertEquals("1 USD = 5.4832 BRL", ptaxUsd.toString());
        assertEquals("5.4832 BRL = 1 USD", ptaxUsd.inverter().toString());
        assertEquals(
                new BigDecimal("0.1823752553253575"),
                ptaxUsd.inverter().getFatorAproximado(MathContext.DECIMAL64));
        assertThrows(IllegalArgumentException.class, () -> TaxaCambio.de(Moeda.BRL, Moeda.BRL, "1.00"));
        assertThrows(IllegalArgumentException.class, () -> ptaxUsd.converter(Real.de("100.00"))); // origem espera USD!

        // 2. Conversões tipadas bidirecionais (BRL <-> USD, BRL <-> EUR e cruzada EUR <-> USD) e TaxaCambio.converter direto:
        Dolar nuvemUsd = Dolar.de("200.00");
        assertEquals(Real.de("1096.64"), nuvemUsd.paraReal(ptaxUsd));
        assertEquals(Real.de("1096.64"), ptaxUsd.converter(nuvemUsd));
        assertEquals(nuvemUsd, nuvemUsd.paraReal(ptaxUsd).paraDolar(ptaxUsd));
        assertEquals(Euro.de("100.00"), Real.de("610.00").paraEuro(ptaxEur));

        Euro licencaEur = Euro.de("100.00");
        Dolar licencaEmUsd = licencaEur.paraDolar(eurUsd);
        assertEquals(Dolar.de("111.25"), licencaEmUsd);
        assertEquals(licencaEur, licencaEmUsd.paraEuro(eurUsd));

        // 3. Consolidação contábil de carteira polimórfica (BRL + USD + EUR + GBP) em Real:
        Moeda gbp = Moeda.iso("GBP", 2);
        TaxaCambio gbpBrl = TaxaCambio.de(gbp, Moeda.BRL, "7.1200");

        List<Dinheiro> carteira = Arrays.asList(
                Real.de("1500.00"),
                nuvemUsd,                          // USD 200.00 -> BRL 1096.64
                licencaEur,                        // EUR 100.00 -> BRL 610.00
                QuantiaMonetaria.de("50.00", gbp)  // GBP 50.00  -> BRL 356.00
        );

        Real totalConsolidadoBrl = Real.ZERO;
        for (Dinheiro posicao : carteira) {
            Real emReais;
            if (posicao instanceof Real) {
                emReais = (Real) posicao;
            } else if (posicao instanceof Dolar) {
                emReais = ((Dolar) posicao).paraReal(ptaxUsd, MetodoArredondamento.FINANCEIRO);
            } else if (posicao instanceof Euro) {
                emReais = ((Euro) posicao).paraReal(ptaxEur, MetodoArredondamento.FINANCEIRO);
            } else {
                emReais = (Real) posicao.converter(gbpBrl, MetodoArredondamento.FINANCEIRO);
            }
            totalConsolidadoBrl = totalConsolidadoBrl.somar(emReais);
        }

        assertEquals(Real.de("3562.64"), totalConsolidadoBrl);
    }
}
