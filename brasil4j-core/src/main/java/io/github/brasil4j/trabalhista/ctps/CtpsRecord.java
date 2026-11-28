package io.github.brasil4j.trabalhista.ctps;

import io.github.brasil4j.geografia.Uf;

/** Registro dos três campos reconhecidos como conformes ao perfil histórico. */
public final class CtpsRecord {
    private final String number;
    private final String series;
    private final Uf uf;

    CtpsRecord(String number, String series, Uf uf) {
        this.number = number;
        this.series = series;
        this.uf = uf;
    }

    /**
     * Obtém o texto original do número.
     *
     * @return número original, com zeros, espaços e caixa preservados
     */
    public String getNumber() {
        return number;
    }

    /**
     * Obtém o texto original da série.
     *
     * @return série original, com zeros, espaços e caixa preservados
     */
    public String getSeries() {
        return series;
    }

    /**
     * Obtém a UF informada.
     *
     * @return UF registrada no leiaute
     */
    public Uf getUf() {
        return uf;
    }

    /**
     * Oculta os valores do registro.
     *
     * @return marcador fixo de ocultação
     */
    @Override
    public String toString() {
        return "[Registro CTPS oculto]";
    }
}
