package io.github.brasil4j.identidade.nis;

/**
 * Inscrição estrutural de NIS, PIS, Pasep ou NIT, com igualdade pelo valor canônico.
 * A conferência abrange somente a apresentação do campo.
 */
public final class Nis {
    private static final String INVALID_FORMAT = "NIS inválido.";

    private final String canonical;

    private Nis(String canonical) {
        this.canonical = canonical;
    }

    /**
     * Interpreta o texto literal de uma inscrição.
     *
     * @param input exatamente 11 dígitos ASCII, com zeros iniciais preservados
     * @return inscrição estrutural imutável
     * @throws IllegalArgumentException se a entrada for nula ou tiver formato inválido
     */
    public static Nis parse(String input) {
        Nis nis = parseOrNull(input);
        if (nis == null) {
            throw new IllegalArgumentException(INVALID_FORMAT);
        }
        return nis;
    }

    static Nis parseOrNull(String input) {
        return hasCanonicalFormat(input) ? new Nis(input) : null;
    }

    static boolean hasCanonicalFormat(String input) {
        if (input == null || input.length() != 11) {
            return false;
        }
        for (int index = 0; index < input.length(); index++) {
            char digit = input.charAt(index);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtém explicitamente o texto integral da inscrição.
     *
     * @return os 11 dígitos na ordem recebida, inclusive zeros iniciais
     */
    public String getCanonicalValue() {
        return canonical;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return other instanceof Nis && canonical.equals(((Nis) other).canonical);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    /**
     * Devolve a representação textual padrão.
     *
     * @return o marcador fixo de ocultação, sem dígitos da inscrição
     */
    @Override
    public String toString() {
        return "[NIS oculto]";
    }
}
