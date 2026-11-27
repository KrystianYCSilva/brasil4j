package io.github.brasil4j.identidade;

/** Documento com apresentação mascarada completa, sem ocultação de caracteres. */
public interface MaskedDocument extends Document {
    /**
     * Aplica a máscara completa, exibindo todos os caracteres.
     *
     * @return valor com a máscara
     */
    String applyMask();
}
