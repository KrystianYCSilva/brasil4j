package io.github.brasil4j.identidade;

/**
 * Documento com máscara de segurança, que oculta os primeiros e os últimos caracteres.
 * A representação textual padrão não muda. O layout de cada documento é uma escolha de projeto.
 */
public interface SecurityMaskedDocument extends Document {
    /**
     * Aplica a máscara de segurança com o caractere {@code *}.
     *
     * @return valor parcialmente ocultado
     */
    default String applySecurityMask() {
        return applySecurityMask('*');
    }

    /**
     * Aplica a máscara de segurança com o caractere informado.
     *
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @return valor parcialmente ocultado
     * @throws IllegalArgumentException se o caractere for letra ou dígito
     */
    String applySecurityMask(char maskChar);
}
