package io.github.brasil4j.identidade.nis;

import io.github.brasil4j.identidade.AbstractDocument;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.identidade.SecurityMaskedDocument;
import io.github.brasil4j.mascara.DocumentMask;

import java.util.EnumMap;
import java.util.Map;

/**
 * Inscrição estrutural de NIS, PIS, Pasep ou NIT, com igualdade pelo valor canônico.
 * A conferência padrão abrange somente a apresentação do campo. Validadores de DV são opcionais:
 * {@link #parser(NisValidationMode)} escolhe um modo e {@code parser.withRule(NisRules.checkDigits(...))}
 * acrescenta a conferência a um interpretador existente.
 */
public final class Nis extends AbstractDocument implements SecurityMaskedDocument {
    private static final String INVALID_FORMAT = "NIS inválido.";
    private static final DocumentMask CANONICAL = DocumentMask.of("99999999999");
    private static final Map<NisValidationMode, DocumentParser<Nis>> PARSERS = new EnumMap<>(NisValidationMode.class);

    static {
        for (NisValidationMode mode : NisValidationMode.values()) {
            PARSERS.put(mode, mode.configure(DocumentParsers.builder(Nis::new).mask(CANONICAL))
                    .errorMessage(INVALID_FORMAT).build());
        }
    }

    private Nis(String canonical) {
        super(canonical);
    }

    /**
     * Obtém o interpretador estrutural, que confere exatamente 11 dígitos ASCII, sem máscara e sem DV.
     *
     * @return interpretador reutilizável
     */
    public static DocumentParser<Nis> parser() {
        return parser(NisValidationMode.FORMAT_ONLY);
    }

    /**
     * Obtém o interpretador da configuração informada.
     *
     * @param mode configuração de conferência
     * @return interpretador reutilizável
     * @throws IllegalArgumentException se a configuração for nula
     */
    public static DocumentParser<Nis> parser(NisValidationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Modo de validação do NIS obrigatório.");
        }
        return PARSERS.get(mode);
    }

    /**
     * Interpreta o texto literal de uma inscrição.
     *
     * @param input exatamente 11 dígitos ASCII, com zeros iniciais preservados
     * @return inscrição estrutural imutável
     * @throws IllegalArgumentException se a entrada for nula ou tiver formato inválido
     */
    public static Nis parse(String input) {
        return parser().parseOrThrow(input);
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
