package io.github.brasil4j.trabalhista.ctps;

import java.util.Objects;

/** Campos brutos de um grupo histórico presente. */
public final class CtpsGroup {
    final String number;
    final String series;
    final String uf;
    final CtpsOrigin origin;
    final boolean sourceLoss;

    private CtpsGroup(String number, String series, String uf, CtpsOrigin origin, boolean sourceLoss) {
        this.number = number;
        this.series = series;
        this.uf = uf;
        this.origin = Objects.requireNonNull(origin, "Origem da CTPS obrigatória.");
        this.sourceLoss = sourceLoss;
    }

    /**
     * Recebe os textos já extraídos, preservando campos vazios, espaços e zeros.
     *
     * @param number número bruto, ou nulo se omitido
     * @param series série bruta, ou nulo se omitida
     * @param uf sigla bruta, ou nulo se omitida
     * @param origin origem declarada, inclusive {@link CtpsOrigin#UNDETERMINED}
     * @param sourceLoss se a origem pode ter perdido informação, como zeros à esquerda
     * @return grupo presente, mesmo que todos os campos estejam omitidos
     * @throws NullPointerException se a origem for nula
     */
    public static CtpsGroup of(String number, String series, String uf, CtpsOrigin origin, boolean sourceLoss) {
        return new CtpsGroup(number, series, uf, origin, sourceLoss);
    }

    /**
     * Oculta os campos brutos do grupo.
     *
     * @return marcador fixo de ocultação
     */
    @Override
    public String toString() {
        return "[Grupo CTPS oculto]";
    }
}
