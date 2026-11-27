package io.github.brasil4j.identidade.nis;

import io.github.brasil4j.identidade.AbstractDocument;
import io.github.brasil4j.identidade.SecurityMaskedDocument;
import io.github.brasil4j.mascara.DocumentMask;

/**
 * Inscrição estrutural de NIS, PIS, Pasep ou NIT, com igualdade pelo valor canônico.
 * A conferência abrange somente a apresentação do campo.
 */
public final class Nis extends AbstractDocument implements SecurityMaskedDocument {
    private static final String INVALID_FORMAT = "NIS inválido.";
    private static final DocumentMask CANONICAL = DocumentMask.of("99999999999");

    private Nis(String canonical) {
        super(canonical);
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
        return CANONICAL.unmask(input).isPresent();
    }

    /**
     * Obtém explicitamente o texto integral da inscrição.
     *
     * @return os 11 dígitos na ordem recebida, inclusive zeros iniciais
     */
    @Override
    public String getCanonicalValue() {
        return super.getCanonicalValue();
    }

    /**
     * Aplica a máscara de segurança sobre os 11 dígitos, sem pontuação, ocultando os três primeiros e
     * o último, como em {@code ***3456789*}. O NIS não tem máscara de apresentação pronta.
     *
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @return a inscrição parcialmente ocultada
     * @throws IllegalArgumentException se o caractere for letra ou dígito
     */
    @Override
    public String applySecurityMask(char maskChar) {
        return CANONICAL.applySecure(getCanonicalValue(), 3, 1, maskChar);
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
