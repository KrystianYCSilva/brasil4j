package io.github.brasil4j.trabalhista.ctps;

/** Proveniência do grupo histórico declarada pelo consumidor. */
public enum CtpsOrigin {
    /** Campos provenientes da carteira física. */
    PHYSICAL,
    /** CPF projetado nos campos do leiaute histórico. */
    CPF_PROJECTION,
    /** Origem sem evidência suficiente para classificação. */
    UNDETERMINED
}
