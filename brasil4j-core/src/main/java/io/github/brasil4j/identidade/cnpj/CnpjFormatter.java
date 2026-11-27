package io.github.brasil4j.identidade.cnpj;

/** Converte entre as duas apresentações de um CNPJ validado no modo padrão. */
public final class CnpjFormatter {
    private CnpjFormatter() {
    }

    /**
     * Aplica a máscara completa.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @return CNPJ no formato XX.XXX.XXX/XXXX-DD
     * @throws IllegalArgumentException se a entrada for inválida no modo padrão
     */
    public static String format(String input) {
        return Cnpj.parse(input).applyMask();
    }

    /**
     * Devolve os caracteres canônicos, com os zeros preservados.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @return CNPJ sem pontuação
     * @throws IllegalArgumentException se a entrada for inválida no modo padrão
     */
    public static String unformat(String input) {
        return Cnpj.parse(input).getCanonicalValue();
    }
}
