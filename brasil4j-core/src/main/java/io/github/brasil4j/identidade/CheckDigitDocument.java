package io.github.brasil4j.identidade;

/** Documento composto de uma base e de dígitos verificadores. */
public interface CheckDigitDocument extends Document {
    /**
     * Obtém a base, sem os dígitos verificadores.
     *
     * @return base
     */
    String getBase();

    /**
     * Obtém os dígitos verificadores informados.
     *
     * @return dígitos verificadores, mesmo quando divergem do cálculo
     */
    String getCheckDigits();

    /**
     * Confere os dígitos informados pelo cálculo do documento, independentemente do modo de construção.
     *
     * @return verdadeiro quando os dígitos informados coincidem com o cálculo
     */
    boolean hasValidCheckDigits();
}
