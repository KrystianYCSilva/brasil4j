package io.github.brasil4j.identidade.cnpj;

import static io.github.brasil4j.identidade.cnpj.CnpjValidationMode.ALPHANUMERIC;

/**
 * Verifica apresentação e, conforme o modo, os dígitos verificadores de um CNPJ.
 *
 * <p>Nos modos com DV, usa o módulo 11 com a conversão ASCII menos 48 para
 * a base. O modo alfanumérico aceita também a base composta só por dígitos,
 * pois essa conversão preserva seus valores numéricos.
 *
 * @see <a href="https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj">Manual de cálculo do DV da Receita Federal</a>
 */
public final class CnpjValidator {
    private CnpjValidator() {
    }

    /**
     * Aplica o modo alfanumérico, que aceita CNPJs numéricos e alfanuméricos.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @return se a entrada atende ao modo padrão, ou false para entrada nula
     */
    public static boolean isValid(String input) {
        return isValid(input, ALPHANUMERIC);
    }

    /**
     * Aplica o modo informado ao CNPJ.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @param mode nível de verificação
     * @return se a entrada atende ao modo escolhido, ou false para entrada nula
     * @throws IllegalArgumentException se o modo for nulo
     */
    public static boolean isValid(String input, CnpjValidationMode mode) {
        return validatedCanonical(input, mode) != null;
    }

    static String validatedCanonical(String input, CnpjValidationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Modo de validação do CNPJ obrigatório.");
        }
        return mode.validatedCanonical(input);
    }
}
