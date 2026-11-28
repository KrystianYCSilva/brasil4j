package io.github.brasil4j.financas;

import java.math.BigDecimal;

/**
 * Taxa relativa exata e imutável com conversão sem ambiguidade entre pontos percentuais
 * ({@code 17.5%}), fator unitário ({@code 0.175}) e pontos-base ({@code 1750 bps}), além de
 * operações monetárias com reconciliação contábil e cálculo "por dentro" (<i>gross-up</i>).
 *
 * <p>O fator canônico interno respeita {@code precision <= 38} e {@code scale} em {@code 0..40}.
 */
public final class Percentual implements Comparable<Percentual> {
    private static final int ESCALA_MAXIMA_FATOR = 40;

    /** Zero por cento ({@code 0%}, fator {@code 0}). */
    public static final Percentual ZERO = new Percentual(BigDecimal.ZERO);

    /** Cem por cento ({@code 100%}, fator {@code 1}). */
    public static final Percentual CEM = new Percentual(BigDecimal.ONE);

    private final BigDecimal fator;

    private Percentual(BigDecimal fatorCanonico) {
        this.fator = fatorCanonico;
    }

    private static Percentual deFatorInterno(BigDecimal fatorBruto) {
        AritmeticaMonetaria.exigirBigDecimalPadrao(fatorBruto);
        if (fatorBruto.scale() < -(AritmeticaMonetaria.PRECISAO_MAXIMA - 1)
                || fatorBruto.scale() > ESCALA_MAXIMA_FATOR + AritmeticaMonetaria.PRECISAO_MAXIMA
                || fatorBruto.unscaledValue().bitLength() > 260) {
            throw new IllegalArgumentException(
                    "O fator percentual excede os limites suportados (precisão <= 38 e escala 0..40).");
        }
        if (fatorBruto.signum() == 0) {
            return ZERO;
        }
        BigDecimal stripped = fatorBruto.stripTrailingZeros();
        BigDecimal canon = stripped.scale() < 0 ? stripped.setScale(0) : stripped;
        if (canon.scale() > ESCALA_MAXIMA_FATOR
                || canon.unscaledValue().bitLength() > 130
                || canon.precision() > AritmeticaMonetaria.PRECISAO_MAXIMA) {
            throw new IllegalArgumentException(
                    "O fator percentual excede os limites suportados (precisão <= 38 e escala 0..40).");
        }
        if (canon.compareTo(BigDecimal.ONE) == 0) {
            return CEM;
        }
        return new Percentual(canon);
    }

    /**
     * Cria um percentual a partir de texto decimal em pontos percentuais (ex.: {@code "17.5"} = {@code 17.5%}).
     * Exige ponto decimal quando houver fração; vírgula não é aceita.
     *
     * @param pontosPercentuais texto decimal canônico
     * @return instância imutável de {@link Percentual}
     * @throws IllegalArgumentException se a entrada for nula, inválida ou exceder os limites
     */
    public static Percentual de(String pontosPercentuais) {
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(pontosPercentuais);
        return de(parsed);
    }

    /**
     * Cria um percentual a partir de {@link BigDecimal} em pontos percentuais (ex.: {@code 17.5} = {@code 17.5%}).
     *
     * @param pontosPercentuais valor padrão não nulo em pontos percentuais
     * @return instância imutável de {@link Percentual}
     * @throws IllegalArgumentException se o valor for nulo, subclasse não confiável ou exceder os limites
     */
    public static Percentual de(BigDecimal pontosPercentuais) {
        AritmeticaMonetaria.validarLimitesDecimal(pontosPercentuais);
        return deFatorInterno(pontosPercentuais.movePointLeft(2));
    }

    /**
     * Cria um percentual a partir de um número inteiro de pontos percentuais (ex.: {@code 18L} = {@code 18%}).
     *
     * @param pontosPercentuais quantidade inteira de pontos percentuais
     * @return instância imutável de {@link Percentual}
     */
    public static Percentual de(long pontosPercentuais) {
        return deFatorInterno(BigDecimal.valueOf(pontosPercentuais, 2));
    }

    /**
     * Cria um percentual a partir de texto decimal representando o fator unitário (ex.: {@code "0.175"} = {@code 17.5%}).
     *
     * @param fatorUnitario texto decimal canônico do fator
     * @return instância imutável de {@link Percentual}
     * @throws IllegalArgumentException se o texto for inválido ou exceder os limites
     */
    public static Percentual deFator(String fatorUnitario) {
        return deFatorInterno(AritmeticaMonetaria.parseDecimalSemLimitesEscala(fatorUnitario));
    }

    /**
     * Cria um percentual a partir de {@link BigDecimal} representando o fator unitário (ex.: {@code 0.175} = {@code 17.5%}).
     *
     * @param fatorUnitario fator padrão não nulo
     * @return instância imutável de {@link Percentual}
     * @throws IllegalArgumentException se o fator for inválido ou exceder os limites
     */
    public static Percentual deFator(BigDecimal fatorUnitario) {
        return deFatorInterno(fatorUnitario);
    }

    /**
     * Cria um percentual a partir de pontos-base inteiros ({@code 1 bp = 0.01% = 0.0001}, ex.: {@code 1750L} = {@code 17.5%}).
     *
     * @param pontosBase quantidade inteira de pontos-base
     * @return instância imutável de {@link Percentual}
     */
    public static Percentual dePontosBase(long pontosBase) {
        return deFatorInterno(BigDecimal.valueOf(pontosBase, 4));
    }

    /**
     * Retorna o valor em pontos percentuais normalizado sem zeros supérfluos e com {@code scale() >= 0}.
     *
     * @return pontos percentuais (ex.: {@code 17.5} para {@code 17.5%})
     */
    public BigDecimal getPontosPercentuais() {
        return AritmeticaMonetaria.normalizarDecimalPlano(fator.movePointRight(2));
    }

    /**
     * Retorna o fator multiplicativo unitário canônico ({@code scale() >= 0}).
     *
     * @return fator unitário (ex.: {@code 0.175} para {@code 17.5%})
     */
    public BigDecimal getFator() {
        return fator;
    }

    /**
     * Retorna o fator algébrico de acréscimo ({@code 1 + getFator()}).
     *
     * @return fator de acréscimo exato
     */
    public BigDecimal getFatorAcrescimo() {
        return AritmeticaMonetaria.normalizarDecimalPlano(BigDecimal.ONE.add(fator));
    }

    /**
     * Retorna o fator algébrico de desconto ({@code 1 - getFator()}).
     *
     * @return fator de desconto exato
     */
    public BigDecimal getFatorDesconto() {
        return AritmeticaMonetaria.normalizarDecimalPlano(BigDecimal.ONE.subtract(fator));
    }

    /**
     * Indica se este percentual é igual a zero ({@code 0%}).
     *
     * @return {@code true} quando zero
     */
    public boolean isZero() {
        return fator.signum() == 0;
    }

    /**
     * Indica se este percentual é estritamente positivo ({@code > 0%}).
     *
     * @return {@code true} quando positivo
     */
    public boolean isPositivo() {
        return fator.signum() > 0;
    }

    /**
     * Indica se este percentual é estritamente negativo ({@code < 0%}).
     *
     * @return {@code true} quando negativo
     */
    public boolean isNegativo() {
        return fator.signum() < 0;
    }

    /**
     * Soma este percentual a outro percentual.
     *
     * @param outro parcela não nula
     * @return soma exata
     * @throws IllegalArgumentException se {@code outro} for nulo ou o resultado estourar os limites
     */
    public Percentual somar(Percentual outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O percentual a somar não pode ser nulo.");
        }
        return deFatorInterno(fator.add(outro.fator));
    }

    /**
     * Subtrai {@code outro} deste percentual.
     *
     * @param outro subtraendo não nulo
     * @return diferença exata
     * @throws IllegalArgumentException se {@code outro} for nulo ou o resultado estourar os limites
     */
    public Percentual subtrair(Percentual outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O percentual a subtrair não pode ser nulo.");
        }
        return deFatorInterno(fator.subtract(outro.fator));
    }

    /**
     * Inverte o sinal deste percentual.
     *
     * @return percentual negado (ou {@link #ZERO} se zero)
     */
    public Percentual negar() {
        return isZero() ? ZERO : new Percentual(fator.negate());
    }

    /**
     * Retorna o valor absoluto deste percentual.
     *
     * @return percentual não negativo
     */
    public Percentual abs() {
        return isNegativo() ? negar() : this;
    }

    /**
     * Aplica este percentual sobre {@code valor} usando e retendo {@code valor.getArredondamento()}.
     *
     * @param <T> subtipo monetário
     * @param valor montante base não nulo
     * @return parcela correspondente a {@code valor * fator} arredondada na escala da moeda
     * @throws IllegalArgumentException se {@code valor} for nulo ou o resultado exceder a precisão máxima
     */
    public <T extends Dinheiro> T aplicarSobre(T valor) {
        exigirDinheiro(valor);
        return aplicarSobre(valor, valor.getArredondamento());
    }

    /**
     * Aplica este percentual sobre {@code valor} aplicando e retendo {@code novoArredondamento}.
     *
     * @param <T> subtipo monetário
     * @param valor montante base não nulo
     * @param novoArredondamento política a aplicar e reter
     * @return parcela correspondente a {@code valor * fator} arredondada na escala da moeda
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    @SuppressWarnings("unchecked")
    public <T extends Dinheiro> T aplicarSobre(T valor, MetodoArredondamento novoArredondamento) {
        exigirDinheiro(valor);
        BigDecimal escalado = AritmeticaMonetaria.multiplicarPorFatorValidado(
                valor.valor, fator, valor.moeda.getCasasDecimais(), novoArredondamento);
        return (T) valor.criarMesmoSubtipo(escalado, novoArredondamento);
    }

    /**
     * Acrescenta este percentual a {@code valor} ({@code valor + aplicarSobre(valor)}), garantindo
     * reconciliação contábil exata ({@code acrescentarEm(v) - v == aplicarSobre(v)}).
     *
     * @param <T> subtipo monetário
     * @param valor montante base não nulo
     * @return montante acrescido
     */
    public <T extends Dinheiro> T acrescentarEm(T valor) {
        exigirDinheiro(valor);
        return acrescentarEm(valor, valor.getArredondamento());
    }

    /**
     * Acrescenta este percentual a {@code valor} aplicando e retendo {@code novoArredondamento}.
     *
     * @param <T> subtipo monetário
     * @param valor montante base não nulo
     * @param novoArredondamento política a aplicar e reter
     * @return montante acrescido
     */
    @SuppressWarnings("unchecked")
    public <T extends Dinheiro> T acrescentarEm(T valor, MetodoArredondamento novoArredondamento) {
        T parcela = aplicarSobre(valor, novoArredondamento);
        BigDecimal soma = AritmeticaMonetaria.somarEscalado(
                valor.valor, parcela.valor, valor.moeda.getCasasDecimais());
        return (T) valor.criarMesmoSubtipo(soma, novoArredondamento);
    }

    /**
     * Desconta este percentual de {@code valor} ({@code valor - aplicarSobre(valor)}), garantindo
     * reconciliação contábil exata ({@code v - descontarDe(v) == aplicarSobre(v)}).
     *
     * @param <T> subtipo monetário
     * @param valor montante base não nulo
     * @return montante líquido após desconto
     */
    public <T extends Dinheiro> T descontarDe(T valor) {
        exigirDinheiro(valor);
        return descontarDe(valor, valor.getArredondamento());
    }

    /**
     * Desconta este percentual de {@code valor} aplicando e retendo {@code novoArredondamento}.
     *
     * @param <T> subtipo monetário
     * @param valor montante base não nulo
     * @param novoArredondamento política a aplicar e reter
     * @return montante líquido após desconto
     */
    @SuppressWarnings("unchecked")
    public <T extends Dinheiro> T descontarDe(T valor, MetodoArredondamento novoArredondamento) {
        T parcela = aplicarSobre(valor, novoArredondamento);
        BigDecimal dif = AritmeticaMonetaria.subtrairEscalado(
                valor.valor, parcela.valor, valor.moeda.getCasasDecimais());
        return (T) valor.criarMesmoSubtipo(dif, novoArredondamento);
    }

    /**
     * Calcula a base "por dentro" (<i>gross-up</i>: {@code valorLiquido / (1 - fator)}) usando a
     * política retida em {@code valorLiquido}. Nos sete modos padrão, {@code descontarDe(base, m)}
     * recompõe exatamente {@code valorLiquido}.
     *
     * @param <T> subtipo monetário
     * @param valorLiquido valor líquido não nulo
     * @return base bruta com este percentual embutido por dentro
     * @throws IllegalArgumentException se este percentual estiver fora de {@code [0%, 100%)} ou {@code valorLiquido} for nulo
     */
    public <T extends Dinheiro> T calcularBasePorDentro(T valorLiquido) {
        exigirDinheiro(valorLiquido);
        return calcularBasePorDentro(valorLiquido, valorLiquido.getArredondamento());
    }

    /**
     * Calcula a base "por dentro" (<i>gross-up</i>: {@code valorLiquido / (1 - fator)}) aplicando
     * e retendo {@code novoArredondamento}.
     *
     * @param <T> subtipo monetário
     * @param valorLiquido valor líquido não nulo
     * @param novoArredondamento política a aplicar e reter (políticas customizadas arbitrárias podem divergir em 1 unidade mínima na recomposição)
     * @return base bruta com este percentual embutido por dentro
     * @throws IllegalArgumentException se este percentual estiver fora de {@code [0%, 100%)} ou qualquer argumento for inválido
     */
    @SuppressWarnings("unchecked")
    public <T extends Dinheiro> T calcularBasePorDentro(T valorLiquido, MetodoArredondamento novoArredondamento) {
        exigirDinheiro(valorLiquido);
        AritmeticaMonetaria.exigirArredondamento(novoArredondamento);
        if (fator.signum() < 0 || fator.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException(
                    "O cálculo por dentro exige percentual no intervalo [0%, 100%).");
        }
        if (isZero()) {
            return (T) valorLiquido.comArredondamento(novoArredondamento);
        }
        BigDecimal divisor = BigDecimal.ONE.subtract(fator);
        BigDecimal bruto = AritmeticaMonetaria.dividirExato(
                valorLiquido.valor, divisor, valorLiquido.moeda.getCasasDecimais(), novoArredondamento);
        return (T) valorLiquido.criarMesmoSubtipo(bruto, novoArredondamento);
    }

    private static void exigirDinheiro(Dinheiro valor) {
        if (valor == null) {
            throw new IllegalArgumentException("O valor monetário não pode ser nulo.");
        }
    }

    @Override
    public int compareTo(Percentual outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O percentual a comparar não pode ser nulo.");
        }
        return fator.compareTo(outro.fator);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Percentual)) {
            return false;
        }
        return fator.equals(((Percentual) outro).fator);
    }

    @Override
    public int hashCode() {
        return fator.hashCode();
    }

    /**
     * Retorna a representação textual canônica sem notação científica (ex.: {@code "17.5%"}, {@code "1000%"}).
     *
     * @return texto canônico terminado em {@code '%'}
     */
    @Override
    public String toString() {
        return getPontosPercentuais().toPlainString() + "%";
    }
}
