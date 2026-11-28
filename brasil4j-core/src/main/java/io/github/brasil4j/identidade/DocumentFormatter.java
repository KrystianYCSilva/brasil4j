package io.github.brasil4j.identidade;

import java.util.Objects;
import java.util.function.Function;

/**
 * Estratégia de apresentação de um documento, compatível com {@link Function}. Os presets cobrem o
 * valor canônico, a máscara completa, a máscara de segurança e a ocultação parcial por quantidade.
 * A saída pode revelar o documento conforme a apresentação escolhida.
 *
 * @param <D> documento apresentado
 */
@FunctionalInterface
public interface DocumentFormatter<D extends Document> extends Function<D, String> {
    /**
     * Apresenta o valor canônico, sem ocultação.
     *
     * @param <D> documento apresentado
     * @return apresentação canônica
     */
    static <D extends Document> DocumentFormatter<D> canonical() {
        return document -> Objects.requireNonNull(document, "Documento obrigatório.").getCanonicalValue();
    }

    /**
     * Apresenta a máscara completa, sem ocultação.
     *
     * @param <D> documento mascarável
     * @return apresentação mascarada
     */
    static <D extends MaskedDocument> DocumentFormatter<D> masked() {
        return document -> Objects.requireNonNull(document, "Documento obrigatório.").applyMask();
    }

    /**
     * Apresenta a máscara de segurança do documento com o caractere {@code *}.
     *
     * @param <D> documento com máscara de segurança
     * @return apresentação parcialmente ocultada
     */
    static <D extends SecurityMaskedDocument> DocumentFormatter<D> secure() {
        return secure('*');
    }

    /**
     * Apresenta a máscara de segurança do documento com o caractere informado.
     *
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @param <D> documento com máscara de segurança
     * @return apresentação parcialmente ocultada
     * @throws IllegalArgumentException se o caractere for letra ou dígito
     */
    static <D extends SecurityMaskedDocument> DocumentFormatter<D> secure(char maskChar) {
        MaskChars.require(maskChar);
        return document -> Objects.requireNonNull(document, "Documento obrigatório.").applySecurityMask(maskChar);
    }

    /**
     * Mostra só os primeiros e os últimos caracteres do valor canônico e oculta o restante. Quando a soma
     * das quantidades visíveis alcança o comprimento, oculta o valor inteiro.
     *
     * @param visiblePrefix caracteres visíveis no início, não negativo
     * @param visibleSuffix caracteres visíveis no fim, não negativo
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @param <D> documento apresentado
     * @return apresentação parcialmente ocultada
     * @throws IllegalArgumentException se uma quantidade for negativa ou o caractere for letra ou dígito
     */
    static <D extends Document> DocumentFormatter<D> redacted(int visiblePrefix, int visibleSuffix, char maskChar) {
        if (visiblePrefix < 0 || visibleSuffix < 0) {
            throw new IllegalArgumentException("Quantidade visível negativa.");
        }
        MaskChars.require(maskChar);
        return document -> {
            String canonical = Objects.requireNonNull(document, "Documento obrigatório.").getCanonicalValue();
            int length = canonical.length();
            boolean hideAll = (long) visiblePrefix + visibleSuffix >= length;
            StringBuilder output = new StringBuilder(length);
            for (int index = 0; index < length; index++) {
                boolean visible = !hideAll && (index < visiblePrefix || index >= length - visibleSuffix);
                output.append(visible ? canonical.charAt(index) : maskChar);
            }
            return output.toString();
        };
    }
}
