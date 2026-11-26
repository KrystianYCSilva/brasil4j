package io.github.brasil4j.identidade.cpf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converte entre as apresentações do CPF com verificação de formato. */
public final class CpfFormatter {
    private static final Pattern CANONICAL = Pattern.compile("[0-9]{11}");
    private static final Pattern MASKED = Pattern.compile("([0-9]{3})\\.([0-9]{3})\\.([0-9]{3})-([0-9]{2})");

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
        if (input == null || CANONICAL.matcher(input).matches()) {
            return input;
        }
        Matcher match = MASKED.matcher(input);
        return match.matches() ? match.group(1) + match.group(2) + match.group(3) + match.group(4) : null;
    }
}
