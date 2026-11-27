package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * Taxa de câmbio imutável, exata e involutivamente invertível entre duas moedas distintas.
 *
 * <p>Quando direta ({@link #isInvertida()} == {@code false}), expressa que {@code 1} unidade
 * de {@link #getMoedaOrigem()} equivale a {@link #getCotacaoBase()} unidades de
 * {@link #getMoedaDestino()}. Ao chamar {@link #inverter()}, a cotação original é preservada
 * sem arredondamento intermediário, executando divisão exata no momento da conversão.
 */
public final class TaxaCambio {
    private final MoedaPadrao moedaBase;
    private final MoedaPadrao moedaCotada;
    private final BigDecimal cotacaoBase;
    private final boolean invertida;

    private TaxaCambio(
            MoedaPadrao moedaBase, MoedaPadrao moedaCotada, BigDecimal cotacaoBase, boolean invertida) {
        this.moedaBase = moedaBase;
        this.moedaCotada = moedaCotada;
        this.cotacaoBase = cotacaoBase;
        this.invertida = invertida;
    }

    private static BigDecimal normalizarCotacaoPositiva(BigDecimal cotacao) {
        AritmeticaMonetaria.validarLimitesDecimal(cotacao);
        if (cotacao.signum() <= 0) {
            throw new IllegalArgumentException("A cotação da taxa de câmbio deve ser estritamente positiva.");
        }
        BigDecimal stripped = cotacao.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    /**
     * Cria uma taxa de câmbio a partir de cotação textual decimal ({@code 1 origem = cotacao destino}).
     *
     * @param origem moeda de origem
     * @param destino moeda de destino (distinta da origem)
     * @param cotacao cotação positiva textual
     * @return taxa de câmbio imutável
     * @throws IllegalArgumentException se qualquer parâmetro for inválido ou se as moedas forem equivalentes
     */
    public static TaxaCambio de(Moeda origem, Moeda destino, String cotacao) {
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(cotacao);
        return de(origem, destino, parsed);
    }

    /**
     * Cria uma taxa de câmbio a partir de {@link BigDecimal} ({@code 1 origem = cotacao destino}).
     *
     * @param origem moeda de origem
     * @param destino moeda de destino (distinta da origem)
     * @param cotacao cotação estritamente positiva
     * @return taxa de câmbio imutável
     * @throws IllegalArgumentException se qualquer parâmetro for inválido ou se as moedas forem equivalentes
     */
    public static TaxaCambio de(Moeda origem, Moeda destino, BigDecimal cotacao) {
        MoedaPadrao orig = ValidacaoMoeda.normalizarMoedaCanonica(origem);
        MoedaPadrao dest = ValidacaoMoeda.normalizarMoedaCanonica(destino);
        if (orig.equals(dest)) {
            throw new IllegalArgumentException("A moeda de origem e a moeda de destino devem ser distintas.");
        }
        BigDecimal norm = normalizarCotacaoPositiva(cotacao);
        return new TaxaCambio(orig, dest, norm, false);
    }

    /**
     * Cria uma taxa expressando quantos Reais equivalem a {@code 1} Dólar americano ({@code USD -> BRL}).
     *
     * @param cotacao cotação positiva textual (ex.: {@code "5.4832"})
     * @return taxa {@code USD -> BRL}
     * @throws IllegalArgumentException se a cotação for inválida
     */
    public static TaxaCambio umDolarEmReais(String cotacao) {
        return de(Moeda.USD, Moeda.BRL, cotacao);
    }

    /**
     * Cria uma taxa expressando quantos Reais equivalem a {@code 1} Dólar americano ({@code USD -> BRL}).
     *
     * @param cotacao cotação estritamente positiva
     * @return taxa {@code USD -> BRL}
     * @throws IllegalArgumentException se a cotação for inválida
     */
    public static TaxaCambio umDolarEmReais(BigDecimal cotacao) {
        return de(Moeda.USD, Moeda.BRL, cotacao);
    }

    /**
     * Cria uma taxa expressando quantos Reais equivalem a {@code 1} Euro ({@code EUR -> BRL}).
     *
     * @param cotacao cotação positiva textual
     * @return taxa {@code EUR -> BRL}
     * @throws IllegalArgumentException se a cotação for inválida
     */
    public static TaxaCambio umEuroEmReais(String cotacao) {
        return de(Moeda.EUR, Moeda.BRL, cotacao);
    }

    /**
     * Cria uma taxa expressando quantos Reais equivalem a {@code 1} Euro ({@code EUR -> BRL}).
     *
     * @param cotacao cotação estritamente positiva
     * @return taxa {@code EUR -> BRL}
     * @throws IllegalArgumentException se a cotação for inválida
     */
    public static TaxaCambio umEuroEmReais(BigDecimal cotacao) {
        return de(Moeda.EUR, Moeda.BRL, cotacao);
    }

    /**
     * Retorna a moeda de origem na direção atual desta taxa.
     *
     * @return moeda de origem
     */
    public Moeda getMoedaOrigem() {
        return invertida ? moedaCotada : moedaBase;
    }

    /**
     * Retorna a moeda de destino na direção atual desta taxa.
     *
     * @return moeda de destino
     */
    public Moeda getMoedaDestino() {
        return invertida ? moedaBase : moedaCotada;
    }

    /**
     * Retorna a cotação base normalizada sem notação científica ({@code scale() >= 0}).
     *
     * @return cotação base positiva
     */
    public BigDecimal getCotacaoBase() {
        return cotacaoBase;
    }

    /**
     * Indica se esta taxa opera por divisão exata da cotação base.
     *
     * @return {@code true} quando invertida
     */
    public boolean isInvertida() {
        return invertida;
    }

    /**
     * Calcula o fator multiplicativo aproximado no {@link MathContext} informado.
     *
     * @param mc contexto matemático não nulo
     * @return {@code cotacaoBase.round(mc)} quando direta, ou {@code 1 / cotacaoBase} no contexto quando invertida
     * @throws IllegalArgumentException se {@code mc} for nulo
     */
    public BigDecimal getFatorAproximado(MathContext mc) {
        if (mc == null) {
            throw new IllegalArgumentException("O MathContext não pode ser nulo.");
        }
        if (invertida && mc.getPrecision() == 0) {
            throw new IllegalArgumentException(
                    "Informe um MathContext com precisão finita (> 0) para fator de taxa invertida.");
        }
        return invertida ? BigDecimal.ONE.divide(cotacaoBase, mc) : cotacaoBase.round(mc);
    }

    /**
     * Inverte a direção desta taxa de forma exata e involutiva ({@code t.inverter().inverter().equals(t)}).
     *
     * @return taxa invertida sem perda de precisão
     */
    public TaxaCambio inverter() {
        if (cotacaoBase.equals(BigDecimal.ONE)) {
            return new TaxaCambio(moedaCotada, moedaBase, BigDecimal.ONE, false);
        }
        return new TaxaCambio(moedaBase, moedaCotada, cotacaoBase, !invertida);
    }

    /**
     * Converte {@code valor} da moeda de origem para a moeda de destino preservando a política do valor.
     *
     * @param valor valor monetário na moeda de origem
     * @return valor convertido canônico na moeda de destino
     * @throws IllegalArgumentException se {@code valor} for nulo ou de moeda divergente da origem
     */
    public Dinheiro converter(Dinheiro valor) {
        if (valor == null) {
            throw new IllegalArgumentException("O valor a converter não pode ser nulo.");
        }
        return converter(valor, valor.getArredondamento());
    }

    /**
     * Converte {@code valor} da moeda de origem para a moeda de destino aplicando e retendo {@code arredondamento}.
     *
     * @param valor valor monetário na moeda de origem
     * @param arredondamento política a aplicar e reter no resultado
     * @return valor convertido canônico na moeda de destino
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda divergente
     */
    public Dinheiro converter(Dinheiro valor, MetodoArredondamento arredondamento) {
        if (valor == null) {
            throw new IllegalArgumentException("O valor a converter não pode ser nulo.");
        }
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        MoedaPadrao origemEsperada = (MoedaPadrao) getMoedaOrigem();
        if (!valor.moeda.equals(origemEsperada)) {
            throw new IllegalArgumentException(
                    "A moeda do valor (" + valor.moeda + ") diverge da moeda de origem da taxa (" + origemEsperada + ").");
        }
        MoedaPadrao destino = (MoedaPadrao) getMoedaDestino();
        int casasDestino = destino.getCasasDecimais();
        BigDecimal convertido = invertida
                ? AritmeticaMonetaria.dividirExato(valor.valor, cotacaoBase, casasDestino, arredondamento)
                : AritmeticaMonetaria.multiplicarPorDecimal(valor.valor, cotacaoBase, casasDestino, arredondamento);
        return Dinheiro.instanciarCanonico(destino, convertido, arredondamento);
    }

    static Dinheiro converterParBidirecional(
            Dinheiro valor, TaxaCambio taxa, MoedaPadrao destinoEsperado, MetodoArredondamento arredondamento) {
        if (taxa == null) {
            throw new IllegalArgumentException("A taxa de câmbio não pode ser nula.");
        }
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        MoedaPadrao origTaxa = (MoedaPadrao) taxa.getMoedaOrigem();
        MoedaPadrao destTaxa = (MoedaPadrao) taxa.getMoedaDestino();
        if (valor.moeda.equals(origTaxa) && destinoEsperado.equals(destTaxa)) {
            return taxa.converter(valor, arredondamento);
        }
        if (valor.moeda.equals(destTaxa) && destinoEsperado.equals(origTaxa)) {
            return taxa.inverter().converter(valor, arredondamento);
        }
        throw new IllegalArgumentException(
                "A taxa de câmbio (" + origTaxa + "->" + destTaxa + ") não corresponde ao par ("
                        + valor.moeda + "->" + destinoEsperado + ").");
    }

    /**
     * Compara igualdade representacional exata sobre {@code (moedaBase, moedaCotada, cotacaoBase, invertida)}
     * (com paridade {@code 1:1} normalizada na direção direta).
     */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof TaxaCambio)) {
            return false;
        }
        TaxaCambio t = (TaxaCambio) outro;
        return invertida == t.invertida
                && moedaBase.equals(t.moedaBase)
                && moedaCotada.equals(t.moedaCotada)
                && cotacaoBase.equals(t.cotacaoBase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moedaBase, moedaCotada, cotacaoBase, invertida);
    }

    /**
     * Retorna {@code "1 <ORIGEM> = <COTACAO> <DESTINO>"} quando direta, ou
     * {@code "<COTACAO> <ORIGEM> = 1 <DESTINO>"} quando invertida.
     *
     * @return representação textual canônica sem notação científica
     */
    @Override
    public String toString() {
        if (!invertida) {
            return "1 " + moedaBase.getCodigo() + " = " + cotacaoBase.toPlainString() + " " + moedaCotada.getCodigo();
        }
        return cotacaoBase.toPlainString() + " " + moedaCotada.getCodigo() + " = 1 " + moedaBase.getCodigo();
    }
}
