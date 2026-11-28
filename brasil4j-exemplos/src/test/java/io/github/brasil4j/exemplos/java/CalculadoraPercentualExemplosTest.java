package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.financas.ApuracaoProgressiva;
import io.github.brasil4j.financas.CalculadoraFinanceira;
import io.github.brasil4j.financas.Dinheiro;
import io.github.brasil4j.financas.Dolar;
import io.github.brasil4j.financas.Euro;
import io.github.brasil4j.financas.FaixaApurada;
import io.github.brasil4j.financas.MetodoArredondamento;
import io.github.brasil4j.financas.ModoApuracaoProgressiva;
import io.github.brasil4j.financas.Moeda;
import io.github.brasil4j.financas.Percentual;
import io.github.brasil4j.financas.Real;
import io.github.brasil4j.financas.TabelaProgressiva;
import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exemplos práticos e casos de uso reais em Java 8 para {@link CalculadoraFinanceira},
 * {@link Percentual} (incluindo cálculo "por dentro" / <i>gross-up</i>) e {@link TabelaProgressiva}.
 *
 * <p>Nota: todas as alíquotas e faixas utilizadas abaixo são parâmetros ilustrativos passados pelo
 * consumidor; o {@code brasil4j} não acopla tabelas normativas ou leis tributárias vigentes.
 */
@DisplayName("Exemplos Java · Calculadora Financeira, Percentuais (Gross-Up) e Tabela Progressiva")
class CalculadoraPercentualExemplosTest {

    @Test
    @DisplayName("CalculadoraFinanceira: cálculos pró-rata e índices encadeados com arredondamento monetário único na saída")
    void exemploCalculadoraFinanceiraProRataEMultimoeda() {
        // 1. Mensalidade pró-rata (17 de 30 dias sobre R$ 1.250,00) avaliada em DECIMAL128 (34 dígitos)
        //    e arredondada uma única vez na entrega como Real:
        Real proRata = CalculadoraFinanceira.emReais(MetodoArredondamento.FINANCEIRO, mc ->
                new BigDecimal("1250.00")
                        .multiply(new BigDecimal("17"), mc)
                        .divide(new BigDecimal("30"), mc));
        assertEquals(Real.de("708.33"), proRata);

        // 2. Instância reutilizável/injetável configurada com política e MathContext explícitos:
        CalculadoraFinanceira calcTruncada =
                CalculadoraFinanceira.de(MetodoArredondamento.TRUNCAMENTO, MathContext.DECIMAL128);

        Dolar servicoUsd = calcTruncada.calcularDolares(mc ->
                new BigDecimal("499.00").divide(new BigDecimal("12"), mc).multiply(new BigDecimal("5"), mc));
        Euro servicoEur = calcTruncada.calcularEuros(mc ->
                new BigDecimal("310.00").multiply(new BigDecimal("0.3333333333"), mc));
        Dinheiro servicoKwd = calcTruncada.calcularMoeda(Moeda.iso("KWD", 3), mc ->
                new BigDecimal("10.000").divide(new BigDecimal("3"), mc));

        assertEquals(Dolar.de("207.91"), servicoUsd);
        assertEquals(Euro.de("103.33"), servicoEur);
        assertEquals("KWD 3.333", servicoKwd.toString());
    }

    @Test
    @DisplayName("Percentual: eliminação da confusão 17.5 vs. 0.175 vs. 1750 bps e reconciliação contábil de descontos")
    void exemploPercentualSemAmbiguidadeEDescontoConciliavel() {
        // As três representações produzem exatamente o mesmo objeto de valor:
        Percentual porPontos = Percentual.de("17.5");
        Percentual porFator = Percentual.deFator("0.175");
        Percentual porBps = Percentual.dePontosBase(1750L);

        assertEquals(porPontos, porFator);
        assertEquals(porPontos, porBps);
        assertEquals("17.5%", porPontos.toString());

        // Operações monetárias preservam o subtipo concreto e garantem fechamento contábil:
        Real valorProduto = Real.de("349.90");
        Percentual cupom = Percentual.de("12.5");

        Real valorDesconto = cupom.aplicarSobre(valorProduto, MetodoArredondamento.FINANCEIRO); // R$ 43,74
        Real liquidoAPagar = cupom.descontarDe(valorProduto, MetodoArredondamento.FINANCEIRO);  // R$ 306,16
        assertEquals(valorProduto, liquidoAPagar.somar(valorDesconto));

        // Combinação de encargos percentuais:
        Percentual encargoTotal = Percentual.de("9.25").somar(Percentual.dePontosBase(200L)); // 11.25%
        assertEquals(Real.de("1112.50"), encargoTotal.acrescentarEm(Real.de("1000.00")));
    }

    @Test
    @DisplayName("Caso de uso Tributário/Comercial: formação de preço com tributo 'por dentro' (gross-up reversível)")
    void exemploCalculoPorDentroGrossUp() {
        // Meta líquida do prestador: R$ 8.250,00 livres após incidência embutida de 17,5% na nota:
        Real metaLiquida = Real.de("8250.00");
        Percentual cargaEmbutida = Percentual.de("17.5");

        Real valorBrutoNota = cargaEmbutida.calcularBasePorDentro(metaLiquida, MetodoArredondamento.FINANCEIRO);
        Real tributoDestacado = cargaEmbutida.aplicarSobre(valorBrutoNota, MetodoArredondamento.FINANCEIRO);

        assertEquals(Real.de("10000.00"), valorBrutoNota);
        assertEquals(Real.de("1750.00"), tributoDestacado);
        assertEquals(metaLiquida, valorBrutoNota.subtrair(tributoDestacado));

        // Alíquotas >= 100% ou negativas são rejeitadas no cálculo por dentro:
        assertThrows(
                IllegalArgumentException.class,
                () -> Percentual.CEM.calcularBasePorDentro(metaLiquida));
    }

    @Test
    @DisplayName("TabelaProgressiva sem teto: apuração marginal por faixas, parcela a deduzir exata e alíquota efetiva")
    void exemploTabelaProgressivaAbertaParaFaixasMarginais() {
        // Faixas ilustrativas parametrizadas pelo sistema consumidor:
        TabelaProgressiva<Real> tabelaMarginal = TabelaProgressiva.emReais()
                .faixaAte("2250.00", Percentual.ZERO)
                .faixaAte("3750.00", Percentual.de("7.5"))
                .faixaAte("5000.00", Percentual.de("15"))
                .faixaAcima(Percentual.de("27.5"));

        ApuracaoProgressiva<Real> apuracao =
                tabelaMarginal.apurar(Real.de("6000.00"), ModoApuracaoProgressiva.NO_TOTAL, MetodoArredondamento.FINANCEIRO);

        // Faixa 1: 2250.00 * 0%    =   0.00
        // Faixa 2: 1500.00 * 7.5%  = 112.50
        // Faixa 3: 1250.00 * 15%   = 187.50
        // Faixa 4: 1000.00 * 27.5% = 275.00  -> Total = R$ 575,00
        assertEquals(Real.de("575.00"), apuracao.getValorTotal());
        assertEquals(
                Percentual.de("9.58"),
                apuracao.getAliquotaEfetiva(2, MetodoArredondamento.FINANCEIRO));

        // A soma dos valores apurados de cada faixa fecha 100% com getValorTotal():
        Real conferencia = Real.ZERO;
        for (FaixaApurada<Real> item : apuracao.getFaixas()) {
            conferencia = conferencia.somar(item.getValorApurado());
        }
        assertEquals(apuracao.getValorTotal(), conferencia);
    }

    @Test
    @DisplayName("TabelaProgressiva com teto contributivo (0% acima do limite) e truncamento POR_FAIXA")
    void exemploTabelaProgressivaComTetoContributivoPorFaixa() {
        // Ao encerrar com Percentual.ZERO em faixaAcima, o excedente acima do teto não gera acréscimo:
        TabelaProgressiva<Real> tabelaComTeto = TabelaProgressiva.emReais()
                .faixaAte("1500.00", Percentual.de("7.5"))
                .faixaAte("3000.00", Percentual.de("9"))
                .faixaAte("4500.00", Percentual.de("12"))
                .faixaAte("8000.00", Percentual.de("14"))
                .faixaAcima(Percentual.ZERO);

        ApuracaoProgressiva<Real> acimaDoTeto = tabelaComTeto.apurar(
                Real.de("12000.00"), ModoApuracaoProgressiva.POR_FAIXA, MetodoArredondamento.TRUNCAMENTO);

        // 112.50 + 135.00 + 180.00 + 490.00 + 0.00 = R$ 917,50 (mesmo para salários acima de R$ 8.000,00!)
        assertEquals(Real.de("917.50"), acimaDoTeto.getValorTotal());
        assertEquals(Real.de("4000.00"), acimaDoTeto.getFaixas().get(4).getBaseNaFaixa());
        assertEquals(Real.ZERO, acimaDoTeto.getFaixas().get(4).getValorApurado());
    }
}
