package io.github.brasil4j.identidade.cnpj;

import java.util.regex.Pattern;

/** Tipo do CNPJ, determinado pelos caracteres da base. */
public enum CnpjType {
    /** Base formada por dígitos ASCII. */
    NUMERIC,

    /** Base com pelo menos uma letra ASCII maiúscula. */
    ALPHANUMERIC;

    private static final Pattern DIGITS = Pattern.compile("[0-9]{14}");

    static CnpjType of(String canonical) {
        return DIGITS.matcher(canonical).matches() ? NUMERIC : ALPHANUMERIC;
    }
}
