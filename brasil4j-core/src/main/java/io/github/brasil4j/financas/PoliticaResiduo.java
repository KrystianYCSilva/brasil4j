package io.github.brasil4j.financas;

/**
 * Regra de distribuição das unidades mínimas residuais no parcelamento igualitário.
 */
public enum PoliticaResiduo {
    /** Aloca uma unidade mínima residual em cada uma das primeiras parcelas. */
    PRIMEIRAS_PARCELAS,
    /** Aloca uma unidade mínima residual em cada uma das últimas parcelas. */
    ULTIMAS_PARCELAS
}
