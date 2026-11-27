package io.github.brasil4j.identidade.cnpj;

import io.github.brasil4j.mascara.DocumentMask;

import static io.github.brasil4j.identidade.cnpj.CnpjValidationMode.ALPHANUMERIC;

/**
 * Valor imutável de um CNPJ completo, com igualdade pelo valor canônico.
 * A construção padrão verifica a estrutura e os DVs. A existência da inscrição é cadastral.
 */
public final class Cnpj {
    static final DocumentMask MASK = DocumentMask.of("XX.XXX.XXX/XXXX-99");

    private final String canonical;

    private Cnpj(String canonical) {
        this.canonical = canonical;
    }

    /**
     * Interpreta um CNPJ no modo alfanumérico, que aceita também CNPJs numéricos.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @return valor do CNPJ
     * @throws IllegalArgumentException se a entrada for inválida
     */
    public static Cnpj parse(String input) {
        return parse(input, ALPHANUMERIC);
    }

    /**
     * Interpreta um CNPJ com o nível de verificação informado.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @param mode nível de verificação
     * @return valor do CNPJ
     * @throws IllegalArgumentException se a entrada for inválida ou se o modo for nulo
     */
    public static Cnpj parse(String input, CnpjValidationMode mode) {
        String canonical = CnpjValidator.validatedCanonical(input, mode);
        if (canonical == null) {
            throw new IllegalArgumentException("CNPJ inválido.");
        }
        return new Cnpj(canonical);
    }

    /**
     * Obtém a representação canônica.
     *
     * @return os 14 caracteres do CNPJ, sem pontuação e com zeros preservados
     */
    public String getCanonicalValue() {
        return canonical;
    }

    /**
     * Obtém a base do documento.
     *
     * @return a base de 12 caracteres, composta de raiz e ordem
     */
    public String getBase() {
        return canonical.substring(0, 12);
    }

    /**
     * Obtém a raiz do documento.
     *
     * @return os oito caracteres da raiz
     */
    public String getRoot() {
        return canonical.substring(0, 8);
    }

    /**
     * Obtém a ordem do estabelecimento.
     *
     * @return os quatro caracteres da ordem do estabelecimento
     */
    public String getBranch() {
        return canonical.substring(8, 12);
    }

    /**
     * Obtém os dígitos verificadores.
     *
     * @return os dois dígitos verificadores informados
     */
    public String getCheckDigits() {
        return canonical.substring(12);
    }

    /**
     * Classifica a base de acordo com sua composição.
     *
     * @return o tipo numérico ou alfanumérico, conforme a base
     */
    public CnpjType getType() {
        return CnpjType.of(canonical);
    }

    /**
     * Formata o valor com a máscara completa.
     *
     * @return o CNPJ com máscara completa, preservando a base e os DVs do valor
     */
    public String applyMask() {
        return MASK.apply(canonical);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return other instanceof Cnpj && canonical.equals(((Cnpj) other).canonical);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    /**
     * Representa o documento pelo seu valor canônico.
     *
     * @return o texto canônico do CNPJ
     */
    @Override
    public String toString() {
        return canonical;
    }
}
