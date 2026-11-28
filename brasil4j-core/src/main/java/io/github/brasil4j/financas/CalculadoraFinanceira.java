package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Executor imutável de expressões {@link CalculoFinanceiro} que opera sobre precisão intermediária
 * finita (padrão {@link MathContext#DECIMAL128}) e realiza um único arredondamento monetário final
 * na escala da moeda alvo, devolvendo {@link Real}, {@link Dolar}, {@link Euro} ou {@link Dinheiro}
 * canônico com a política de arredondamento retida.
 *
 * <p>Nota sobre precisão intermediária: o {@link MathContext} fornecido ao callback executa seus
 * próprios arredondamentos nas operações intermediárias da expressão. Modos finais direcionais
 * ({@link MetodoArredondamento#TRUNCAMENTO}, {@link MetodoArredondamento#PISO},
 * {@link MetodoArredondamento#TETO} e {@link MetodoArredondamento#PARA_CIMA}) são sensíveis a
 * resíduos de dízimas periódicas (ex.: {@code 100 * (1/3) * 3} em {@code DECIMAL128} produz
 * {@code 99.999...9}, resultando em {@code 99.99} sob {@code TRUNCAMENTO}).
 */
public final class CalculadoraFinanceira {
    private static final int PRECISAO_MAXIMA_CONTEXTO = 90;

    /**
     * Instância padrão configurada com {@link MetodoArredondamento#ABNT_NBR_5891} e
     * {@link MathContext#DECIMAL128} ({@code 34} dígitos significativos).
     */
    public static final CalculadoraFinanceira PADRAO =
            new CalculadoraFinanceira(MetodoArredondamento.ABNT_NBR_5891, MathContext.DECIMAL128);

    private final MetodoArredondamento arredondamento;
    private final MathContext precisaoIntermediaria;

    private CalculadoraFinanceira(MetodoArredondamento arredondamento, MathContext precisaoIntermediaria) {
        this.arredondamento = arredondamento;
        this.precisaoIntermediaria = precisaoIntermediaria;
    }

    /**
     * Obtém uma calculadora imutável configurada com a política informada e {@link MathContext#DECIMAL128}.
     *
     * @param arredondamento política final não nula
     * @return calculadora configurada
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static CalculadoraFinanceira de(MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        if (arredondamento == MetodoArredondamento.ABNT_NBR_5891) {
            return PADRAO;
        }
        return new CalculadoraFinanceira(arredondamento, MathContext.DECIMAL128);
    }

    /**
     * Obtém uma calculadora imutável configurada com política final e precisão intermediária explícitas.
     *
     * @param arredondamento política final não nula
     * @param precisaoIntermediaria contexto intermediário com precisão em {@code 1..90} e modo diferente de {@link RoundingMode#UNNECESSARY}
     * @return calculadora configurada
     * @throws IllegalArgumentException se qualquer argumento for nulo ou violar os limites
     */
    public static CalculadoraFinanceira de(
            MetodoArredondamento arredondamento, MathContext precisaoIntermediaria) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        if (precisaoIntermediaria == null) {
            throw new IllegalArgumentException("O MathContext intermediário não pode ser nulo.");
        }
        int p = precisaoIntermediaria.getPrecision();
        if (p < 1 || p > PRECISAO_MAXIMA_CONTEXTO) {
            throw new IllegalArgumentException(
                    "A precisão do MathContext intermediário deve estar entre 1 e 90 dígitos.");
        }
        if (precisaoIntermediaria.getRoundingMode() == RoundingMode.UNNECESSARY) {
            throw new IllegalArgumentException(
                    "RoundingMode.UNNECESSARY não é permitido no MathContext intermediário.");
        }
        if (arredondamento == MetodoArredondamento.ABNT_NBR_5891
                && MathContext.DECIMAL128.equals(precisaoIntermediaria)) {
            return PADRAO;
        }
        return new CalculadoraFinanceira(arredondamento, precisaoIntermediaria);
    }

    /**
     * Retorna a política de arredondamento final configurada nesta instância.
     *
     * @return política de arredondamento
     */
    public MetodoArredondamento getArredondamento() {
        return arredondamento;
    }

    /**
     * Retorna o {@link MathContext} intermediário configurado nesta instância.
     *
     * @return contexto matemático intermediário
     */
    public MathContext getPrecisaoIntermediaria() {
        return precisaoIntermediaria;
    }

    /**
     * Avalia {@code calculo} e entrega o resultado arredondado em {@link Real} retendo {@link #getArredondamento()}.
     *
     * @param calculo expressão financeira não nula
     * @return quantia em Reais
     * @throws IllegalArgumentException se {@code calculo} ou seu retorno forem nulos, subclasses não confiáveis ou fora dos limites
     */
    public Real calcularReais(CalculoFinanceiro calculo) {
        BigDecimal escalado = avaliarEEscalar(calculo, 2);
        return new Real(escalado, arredondamento);
    }

    /**
     * Avalia {@code calculo} e entrega o resultado arredondado em {@link Dolar} retendo {@link #getArredondamento()}.
     *
     * @param calculo expressão financeira não nula
     * @return quantia em Dólar
     * @throws IllegalArgumentException se {@code calculo} ou seu retorno forem inválidos
     */
    public Dolar calcularDolares(CalculoFinanceiro calculo) {
        BigDecimal escalado = avaliarEEscalar(calculo, 2);
        return new Dolar(escalado, arredondamento);
    }

    /**
     * Avalia {@code calculo} e entrega o resultado arredondado em {@link Euro} retendo {@link #getArredondamento()}.
     *
     * @param calculo expressão financeira não nula
     * @return quantia em Euro
     * @throws IllegalArgumentException se {@code calculo} ou seu retorno forem inválidos
     */
    public Euro calcularEuros(CalculoFinanceiro calculo) {
        BigDecimal escalado = avaliarEEscalar(calculo, 2);
        return new Euro(escalado, arredondamento);
    }

    /**
     * Avalia {@code calculo} e entrega o resultado no subtipo canônico de {@link Dinheiro} correspondente a {@code moeda}.
     *
     * @param moeda moeda alvo
     * @param calculo expressão financeira não nula
     * @return instância canônica ({@link Real}, {@link Dolar}, {@link Euro} ou {@link QuantiaMonetaria})
     * @throws IllegalArgumentException se a moeda, o cálculo ou o retorno forem inválidos
     */
    public Dinheiro calcularMoeda(Moeda moeda, CalculoFinanceiro calculo) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        BigDecimal escalado = avaliarEEscalar(calculo, m.getCasasDecimais());
        return Dinheiro.instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.PADRAO.calcularReais(calculo)}.
     *
     * @param calculo expressão financeira não nula
     * @return quantia em Reais com {@link MetodoArredondamento#ABNT_NBR_5891}
     */
    public static Real emReais(CalculoFinanceiro calculo) {
        return PADRAO.calcularReais(calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.de(arredondamento).calcularReais(calculo)}.
     *
     * @param arredondamento política de arredondamento final a aplicar e reter
     * @param calculo expressão financeira não nula
     * @return quantia em Reais
     */
    public static Real emReais(MetodoArredondamento arredondamento, CalculoFinanceiro calculo) {
        return de(arredondamento).calcularReais(calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.PADRAO.calcularDolares(calculo)}.
     *
     * @param calculo expressão financeira não nula
     * @return quantia em Dólar com {@link MetodoArredondamento#ABNT_NBR_5891}
     */
    public static Dolar emDolares(CalculoFinanceiro calculo) {
        return PADRAO.calcularDolares(calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.de(arredondamento).calcularDolares(calculo)}.
     *
     * @param arredondamento política de arredondamento final a aplicar e reter
     * @param calculo expressão financeira não nula
     * @return quantia em Dólar
     */
    public static Dolar emDolares(MetodoArredondamento arredondamento, CalculoFinanceiro calculo) {
        return de(arredondamento).calcularDolares(calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.PADRAO.calcularEuros(calculo)}.
     *
     * @param calculo expressão financeira não nula
     * @return quantia em Euro com {@link MetodoArredondamento#ABNT_NBR_5891}
     */
    public static Euro emEuros(CalculoFinanceiro calculo) {
        return PADRAO.calcularEuros(calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.de(arredondamento).calcularEuros(calculo)}.
     *
     * @param arredondamento política de arredondamento final a aplicar e reter
     * @param calculo expressão financeira não nula
     * @return quantia em Euro
     */
    public static Euro emEuros(MetodoArredondamento arredondamento, CalculoFinanceiro calculo) {
        return de(arredondamento).calcularEuros(calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.PADRAO.calcularMoeda(moeda, calculo)}.
     *
     * @param moeda moeda alvo
     * @param calculo expressão financeira não nula
     * @return instância canônica de {@link Dinheiro}
     */
    public static Dinheiro emMoeda(Moeda moeda, CalculoFinanceiro calculo) {
        return PADRAO.calcularMoeda(moeda, calculo);
    }

    /**
     * Atalho estático equivalente a {@code CalculadoraFinanceira.de(arredondamento).calcularMoeda(moeda, calculo)}.
     *
     * @param moeda moeda alvo
     * @param arredondamento política de arredondamento final a aplicar e reter
     * @param calculo expressão financeira não nula
     * @return instância canônica de {@link Dinheiro}
     */
    public static Dinheiro emMoeda(
            Moeda moeda, MetodoArredondamento arredondamento, CalculoFinanceiro calculo) {
        return de(arredondamento).calcularMoeda(moeda, calculo);
    }

    private BigDecimal avaliarEEscalar(CalculoFinanceiro calculo, int casasDecimais) {
        if (calculo == null) {
            throw new IllegalArgumentException("A função de cálculo financeiro não pode ser nula.");
        }
        BigDecimal resultadoIntermediario = calculo.calcular(precisaoIntermediaria);
        return AritmeticaMonetaria.aplicarArredondamentoIntermediario(
                resultadoIntermediario, casasDecimais, arredondamento);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof CalculadoraFinanceira)) {
            return false;
        }
        CalculadoraFinanceira c = (CalculadoraFinanceira) outro;
        return Objects.equals(arredondamento, c.arredondamento)
                && Objects.equals(precisaoIntermediaria, c.precisaoIntermediaria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arredondamento, precisaoIntermediaria);
    }

    @Override
    public String toString() {
        return "CalculadoraFinanceira[arredondamento="
                + arredondamento
                + ", precisao="
                + precisaoIntermediaria
                + "]";
    }
}
