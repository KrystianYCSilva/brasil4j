package io.github.brasil4j.identidade;

/** Conferência compartilhada do caractere de ocultação, fora da API pública. */
final class MaskChars {
    private MaskChars() {
    }

    static void require(char maskChar) {
        if (Character.isLetterOrDigit(maskChar)) {
            throw new IllegalArgumentException("Caractere de ocultação não pode ser letra nem dígito.");
        }
    }
}
