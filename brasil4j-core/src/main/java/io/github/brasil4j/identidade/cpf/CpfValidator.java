package io.github.brasil4j.identidade.cpf;

import static io.github.brasil4j.identidade.cpf.CpfValidationMode.FORMAT_ONLY;

/** Verifica o formato de um CPF e confere seus dígitos verificadores quando solicitado. */
public final class CpfValidator {
    private CpfValidator() {
    }

    /**
     * Verifica se a entrada possui tamanho e apresentação válidos.
     *
     * @param input CPF canônico ou com máscara completa
     * @return se o formato é válido, ou false para entrada nula
     */
    public static boolean isValid(String input) {
        return isValid(input, FORMAT_ONLY);
    }

    /**
     * Aplica o modo informado ao CPF.
     *
     * @param input CPF canônico ou com máscara completa
     * @param mode nível de verificação
     * @return se a entrada atende ao modo escolhido, ou false para entrada nula
     * @throws IllegalArgumentException se o modo for nulo
     */
    public static boolean isValid(String input, CpfValidationMode mode) {
        return validatedCanonical(input, mode) != null;
    }

    static String validatedCanonical(String input, CpfValidationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Modo de validação do CPF obrigatório.");
        }
        return mode.validatedCanonical(input);
    }
}
