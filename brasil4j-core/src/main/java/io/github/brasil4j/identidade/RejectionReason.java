package io.github.brasil4j.identidade;

/** Motivo pelo qual uma entrada não foi aceita como documento. */
public enum RejectionReason {
    /** A entrada é nula ou vazia. */
    MISSING,
    /** A apresentação não corresponde a nenhuma forma aceita. */
    INVALID_FORMAT,
    /** Os dígitos verificadores informados divergem do cálculo configurado. */
    INVALID_CHECK_DIGITS,
    /** A base tem composição não suportada pela configuração escolhida. */
    UNSUPPORTED_BASE,
    /** Uma regra adicional do consumidor rejeitou o documento. */
    POLICY
}
