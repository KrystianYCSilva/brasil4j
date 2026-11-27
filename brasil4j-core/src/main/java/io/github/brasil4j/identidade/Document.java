package io.github.brasil4j.identidade;

/**
 * Contrato de um documento imutável identificado pela sua forma canônica.
 * Os tipos da biblioteca e os documentos do consumidor implementam este contrato.
 */
public interface Document {
    /**
     * Obtém explicitamente o valor canônico, que pode expor o documento.
     *
     * @return valor canônico, nunca nulo
     */
    String getCanonicalValue();
}
