package io.github.brasil4j.identidade.cpf;

/** Converte entre as apresentações do CPF com verificação de formato. */
public final class CpfFormatter {
    private CpfFormatter() {
    }

    /**
     * Aplica a máscara completa, exibindo todos os dígitos.
     *
     * @param input CPF canônico ou com máscara completa
     * @return CPF no formato XXX.XXX.XXX-XX
     * @throws IllegalArgumentException se o formato da entrada for inválido
     */
    public static String format(String input) {
        return Cpf.parse(input).applyMask();
    }

    /**
     * Devolve os 11 dígitos, com os zeros preservados.
     *
     * @param input CPF canônico ou com máscara completa
     * @return CPF sem pontuação
     * @throws IllegalArgumentException se o formato da entrada for inválido
     */
    public static String unformat(String input) {
        return Cpf.parse(input).getCanonicalValue();
    }

    static String canonicalize(String input) {
        return Cpf.MASK.unmask(input).orElse(null);
    }
}
