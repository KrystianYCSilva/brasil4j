package io.github.brasil4j.identidade.cpf;

import static io.github.brasil4j.identidade.cpf.CpfValidationMode.FORMAT_ONLY;

/**
 * Valor imutável de um CPF, com igualdade pelo valor canônico.
 * A construção padrão confere a apresentação. A conferência do DV é opcional.
 */
public final class Cpf {
    private final String canonical;

    private Cpf(String canonical) {
        this.canonical = canonical;
    }

    /**
     * Interpreta um CPF com verificação de formato.
     *
     * @param input CPF canônico ou com máscara completa
     * @return valor do CPF
     * @throws IllegalArgumentException se o formato da entrada for inválido
     */
    public static Cpf parse(String input) {
        return parse(input, FORMAT_ONLY);
    }

    /**
     * Interpreta um CPF com o nível de verificação informado.
     *
     * @param input CPF canônico ou com máscara completa
     * @param mode nível de verificação
     * @return valor do CPF
     * @throws IllegalArgumentException se a entrada for inválida ou se o modo for nulo
     */
    public static Cpf parse(String input, CpfValidationMode mode) {
        String canonical = CpfValidator.validatedCanonical(input, mode);
        if (canonical == null) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        return new Cpf(canonical);
    }

    /**
     * Obtém o valor canônico.
     *
     * @return os 11 dígitos informados, sem pontuação e com zeros preservados
     */
    public String getCanonicalValue() {
        return canonical;
    }

    /**
     * Obtém a base do CPF.
     *
     * @return os nove dígitos da base
     */
    public String getBase() {
        return canonical.substring(0, 9);
    }

    /**
     * Obtém os dígitos verificadores.
     *
     * @return os dois dígitos verificadores informados, inclusive no modo de formato
     */
    public String getCheckDigits() {
        return canonical.substring(9);
    }

    /**
     * Compara os dígitos informados ao resultado do módulo 11.
     *
     * @return se os dígitos informados coincidem com o cálculo pelo módulo 11
     */
    public boolean hasValidCheckDigits() {
        return CpfCheckDigits.matches(canonical);
    }

    /**
     * Aplica a máscara completa.
     *
     * @return o CPF com máscara completa, exibindo todos os dígitos
     */
    public String applyMask() {
        return canonical.substring(0, 3) + "." + canonical.substring(3, 6) + "."
                + canonical.substring(6, 9) + "-" + getCheckDigits();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return other instanceof Cpf && canonical.equals(((Cpf) other).canonical);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    /**
     * Representa o CPF em texto com ocultação dos dígitos.
     *
     * @return uma representação que oculta todos os dígitos do CPF
     */
    @Override
    public String toString() {
        return "***.***.***-**";
    }
}
