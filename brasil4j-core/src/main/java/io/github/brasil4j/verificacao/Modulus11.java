package io.github.brasil4j.verificacao;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

/**
 * Módulo 11 configurável pelo valor de cada caractere, pela sequência de pesos e pelo mapeamento do resto.
 * Cada dígito intermediário entra na base do dígito seguinte. Por isso, com vários DVs, o alfabeto precisa
 * conter todo dígito que o mapeamento do resto pode produzir, o que o {@link Builder#build()} confere.
 * As instâncias são imutáveis.
 *
 * <p>Os presets citam a fonte e o alcance de cada regra. Um documento do consumidor pode montar o
 * próprio cálculo com {@link #builder()}.
 */
public final class Modulus11 implements CheckDigitAlgorithm {
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = DIGITS + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int[] PIS_WEIGHTS = {3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final Modulus11 CPF = builder().weightsFromRight(2, 11).digits(2).build();
    private static final Modulus11 CNPJ = builder().charValue(c -> c - '0', ALPHANUMERIC).digits(2).build();
    private static final Modulus11 PIS_CAIXA_FGTS_V016 = builder().weightsFromLeft(PIS_WEIGHTS)
            .remainder(rest -> rest == 1 ? OptionalInt.empty() : OptionalInt.of(rest == 0 ? 0 : 11 - rest))
            .build();
    private static final Modulus11 PIS_LEGACY_MOD11 = builder().weightsFromLeft(PIS_WEIGHTS).build();

    private final IntUnaryOperator charValue;
    private final String alphabet;
    private final int firstWeight;
    private final int lastWeight;
    private final int[] leftWeights;
    private final IntFunction<OptionalInt> remainder;
    private final int digits;

    private Modulus11(Builder builder) {
        this.charValue = builder.charValue;
        this.alphabet = builder.alphabet;
        this.firstWeight = builder.firstWeight;
        this.lastWeight = builder.lastWeight;
        this.leftWeights = builder.leftWeights;
        this.remainder = builder.remainder;
        this.digits = builder.digits;
    }

    /**
     * Inicia a configuração de um módulo 11. Padrões: dígitos ASCII com valor {@code c - '0'}, pesos de 2
     * a 9 da direita para a esquerda, resto menor que 2 vira 0 e os demais viram {@code 11 - resto}, e um DV.
     *
     * @return construtor com os padrões
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Cálculo dos dois DVs do CPF: pesos de 2 a 11 da direita para a esquerda, sem recomeço, e
     * mapeamento padrão do resto. Equivale à regra REGRA_VALIDA_CPF do manual da e-Financeira, já
     * registrada em {@code docs/fontes.md}.
     *
     * @return algoritmo do CPF
     */
    public static Modulus11 cpf() {
        return CPF;
    }

    /**
     * Cálculo dos dois DVs do CNPJ numérico e alfanumérico, conforme o Anexo XV da IN RFB 2.119/2022,
     * que cobre somente o cálculo: valor ASCII menos 48, pesos de 2 a 9 da direita para a esquerda, com
     * recomeço, e mapeamento padrão do resto. O bloqueio de quatorze zeros não vem do Anexo XV: é a
     * política de entrada F-05 de {@code docs/fontes.md}, aplicada pelo documento e não por este algoritmo.
     *
     * @return algoritmo do CNPJ
     */
    public static Modulus11 cnpj() {
        return CNPJ;
    }

    /**
     * Fórmula publicada no Caderno do Agente Financeiro v016 da Caixa (p. 25), com alcance documentado
     * para PIS/Pasep no contexto operacional do FGTS: pesos 3, 2, 9, 8, 7, 6, 5, 4, 3 e 2 da esquerda
     * para a direita; resto 0 vira 0, resto 1 deixa a base sem DV válido e os demais viram
     * {@code 11 - resto}. A extensão ao Cadastro NIS, ao NIT do INSS ou ao Pasep em geral não está
     * confirmada. Não inclui a guarda de algarismos repetidos.
     *
     * @return algoritmo da fórmula publicada pela Caixa para o FGTS
     */
    public static Modulus11 pisCaixaFgtsV016() {
        return PIS_CAIXA_FGTS_V016;
    }

    /**
     * Convenção técnica observada nos validadores legados de Declarações e da UEM (matriz C1 em
     * {@code docs/descoberta/c1/nis-matriz.md}): os mesmos pesos da fórmula da Caixa, com restos 0 e 1
     * convertidos em DV 0. Diverge da fórmula da Caixa quando o resto é 1. A guarda de algarismos
     * repetidos observada nesses legados é uma regra separada.
     *
     * @return algoritmo da convenção legada
     */
    public static Modulus11 pisLegacyMod11() {
        return PIS_LEGACY_MOD11;
    }

    /** {@inheritDoc} */
    @Override
    public int digitCount() {
        return digits;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> compute(String base) {
        Objects.requireNonNull(base, "Base obrigatória.");
        if (base.isEmpty()) {
            throw new InvalidBaseException("Base vazia.");
        }
        StringBuilder value = new StringBuilder(base);
        StringBuilder computed = new StringBuilder(digits);
        for (int index = 0; index < digits; index++) {
            OptionalInt digit = next(value);
            if (!digit.isPresent()) {
                return Optional.empty();
            }
            char character = (char) ('0' + digit.getAsInt());
            if (index < digits - 1) {
                value.append(character);
            }
            computed.append(character);
        }
        return Optional.of(computed.toString());
    }

    private OptionalInt next(CharSequence value) {
        if (leftWeights != null && value.length() > leftWeights.length) {
            throw new InvalidBaseException("Base maior que a sequência de pesos.");
        }
        // Cada parcela é reduzida módulo 11 antes da soma, o que evita transbordamento para qualquer peso aceito.
        int sum = 0;
        long range = (long) lastWeight - firstWeight + 1;
        for (int offset = 0; offset < value.length(); offset++) {
            char character = value.charAt(value.length() - 1 - offset);
            long weight = leftWeights == null
                    ? firstWeight + offset % range
                    : leftWeights[value.length() - 1 - offset];
            sum = (int) ((sum + (valueOf(character) % 11) * (weight % 11)) % 11);
        }
        // O build já conferiu o mapeamento para os restos de 0 a 10, os únicos possíveis aqui.
        return remainder.apply(sum);
    }

    private int valueOf(char character) {
        if (alphabet.indexOf(character) < 0) {
            throw new InvalidBaseException("Caractere fora do domínio do algoritmo.");
        }
        return charValue.applyAsInt(character);
    }

    /** Construtor mutável de {@link Modulus11}. Cada chamada substitui a configuração anterior da mesma etapa. */
    public static final class Builder {
        private IntUnaryOperator charValue = c -> c - '0';
        private String alphabet = DIGITS;
        private int firstWeight = 2;
        private int lastWeight = 9;
        private int[] leftWeights;
        private IntFunction<OptionalInt> remainder = rest -> OptionalInt.of(rest < 2 ? 0 : 11 - rest);
        private int digits = 1;

        private Builder() {
        }

        /**
         * Define os caracteres aceitos na base e o valor numérico de cada um.
         *
         * @param charToValue valor não negativo de cada caractere
         * @param alphabet caracteres aceitos
         * @return este construtor
         * @throws NullPointerException se algum argumento for nulo
         * @throws IllegalArgumentException se o alfabeto for vazio
         */
        public Builder charValue(IntUnaryOperator charToValue, String alphabet) {
            Objects.requireNonNull(charToValue, "Função de valor obrigatória.");
            Objects.requireNonNull(alphabet, "Alfabeto obrigatório.");
            if (alphabet.isEmpty()) {
                throw new IllegalArgumentException("Alfabeto vazio.");
            }
            this.charValue = charToValue;
            this.alphabet = alphabet;
            return this;
        }

        /**
         * Usa pesos cíclicos da direita para a esquerda, de {@code first} a {@code last}, recomeçando em
         * {@code first}.
         *
         * @param first primeiro peso, maior que zero
         * @param last último peso, maior ou igual ao primeiro
         * @return este construtor
         * @throws IllegalArgumentException se o intervalo for inválido
         */
        public Builder weightsFromRight(int first, int last) {
            if (first < 1 || last < first) {
                throw new IllegalArgumentException("Intervalo de pesos inválido.");
            }
            this.firstWeight = first;
            this.lastWeight = last;
            this.leftWeights = null;
            return this;
        }

        /**
         * Usa pesos fixos da esquerda para a direita. Bases maiores que a sequência são recusadas no cálculo.
         *
         * @param weights pesos maiores que zero
         * @return este construtor
         * @throws NullPointerException se a sequência for nula
         * @throws IllegalArgumentException se a sequência for vazia ou tiver peso menor que 1
         */
        public Builder weightsFromLeft(int... weights) {
            Objects.requireNonNull(weights, "Pesos obrigatórios.");
            if (weights.length == 0) {
                throw new IllegalArgumentException("Sequência de pesos vazia.");
            }
            for (int weight : weights) {
                if (weight < 1) {
                    throw new IllegalArgumentException("Peso menor que 1.");
                }
            }
            this.leftWeights = weights.clone();
            return this;
        }

        /**
         * Define como o resto da divisão por 11 vira dígito. A função precisa ser pura e determinística: o
         * {@link #build()} a avalia uma vez para cada resto de 0 a 10 e o cálculo confia nesse resultado.
         * Com vários DVs, todo dígito que ela pode produzir precisa pertencer ao alfabeto, porque os DVs
         * anteriores ao último voltam à base e o mesmo mapeamento gera todos eles.
         *
         * @param mapping função do resto para um dígito de 0 a 9, ou vazio quando a base não admite DV
         * @return este construtor
         * @throws NullPointerException se a função for nula
         */
        public Builder remainder(IntFunction<OptionalInt> mapping) {
            this.remainder = Objects.requireNonNull(mapping, "Mapeamento obrigatório.");
            return this;
        }

        /**
         * Define a quantidade de dígitos verificadores.
         *
         * @param count quantidade, maior que zero
         * @return este construtor
         * @throws IllegalArgumentException se a quantidade for menor que 1
         */
        public Builder digits(int count) {
            if (count < 1) {
                throw new IllegalArgumentException("Quantidade de DVs menor que 1.");
            }
            this.digits = count;
            return this;
        }

        /**
         * Cria o algoritmo imutável com a configuração atual, conferindo antes a coerência dela.
         *
         * @return algoritmo
         * @throws IllegalArgumentException se houver menos pesos fixos que dígitos verificadores,
         *         se algum caractere do alfabeto tiver valor negativo, se o mapeamento do resto devolver nulo
         *         ou dígito fora de 0 a 9 para algum resto de 0 a 10, ou, com vários DVs, se devolver
         *         dígito fora do alfabeto
         */
        public Modulus11 build() {
            if (leftWeights != null && digits > leftWeights.length) {
                throw new IllegalArgumentException("Menos pesos que dígitos verificadores.");
            }
            for (int index = 0; index < alphabet.length(); index++) {
                if (charValue.applyAsInt(alphabet.charAt(index)) < 0) {
                    throw new IllegalArgumentException("Valor de caractere negativo.");
                }
            }
            checkRemainderMapping();
            return new Modulus11(this);
        }

        private void checkRemainderMapping() {
            for (int rest = 0; rest <= 10; rest++) {
                OptionalInt digit = remainder.apply(rest);
                if (digit == null) {
                    throw new IllegalArgumentException("Mapeamento do resto devolveu nulo para o resto " + rest + ".");
                }
                if (!digit.isPresent()) {
                    continue;
                }
                if (digit.getAsInt() < 0 || digit.getAsInt() > 9) {
                    throw new IllegalArgumentException("Mapeamento do resto devolveu DV fora de 0 a 9 para o resto "
                            + rest + ".");
                }
                if (digits > 1 && alphabet.indexOf('0' + digit.getAsInt()) < 0) {
                    throw new IllegalArgumentException("Mapeamento do resto devolveu DV fora do alfabeto para o resto "
                            + rest + ".");
                }
            }
        }
    }
}
