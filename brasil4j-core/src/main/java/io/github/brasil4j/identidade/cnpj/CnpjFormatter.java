package io.github.brasil4j.identidade.cnpj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converte entre as duas apresentações de um CNPJ validado no modo padrão. */
public final class CnpjFormatter {
    private static final Pattern CANONICAL = Pattern.compile("[A-Z0-9]{12}[0-9]{2}");
    private static final Pattern MASKED =
            Pattern.compile("([A-Z0-9]{2})\\.([A-Z0-9]{3})\\.([A-Z0-9]{3})/([A-Z0-9]{4})-([0-9]{2})");

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

    static String canonicalize(String input) {
        if (input == null || CANONICAL.matcher(input).matches()) {
            return input;
        }
        Matcher match = MASKED.matcher(input);
        return match.matches()
                ? match.group(1) + match.group(2) + match.group(3) + match.group(4) + match.group(5)
                : null;
    }
}
