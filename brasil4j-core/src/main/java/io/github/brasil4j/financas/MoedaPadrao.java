package io.github.brasil4j.financas;

import java.util.Objects;

/**
 * Portador imutável interno de {@link Moeda}, instanciado apenas após inicialização de {@link Moeda}
 * via {@link ValidacaoMoeda}.
 */
final class MoedaPadrao implements Moeda {
    private final String codigo;
    private final int casasDecimais;
    private final String simbolo;

    MoedaPadrao(String codigo, int casasDecimais, String simbolo) {
        this.codigo = codigo;
        this.casasDecimais = casasDecimais;
        this.simbolo = simbolo;
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public int getCasasDecimais() {
        return casasDecimais;
    }

    @Override
    public String getSimbolo() {
        return simbolo;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof MoedaPadrao)) {
            return false;
        }
        MoedaPadrao outra = (MoedaPadrao) outro;
        return casasDecimais == outra.casasDecimais && codigo.equals(outra.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, casasDecimais);
    }

    @Override
    public String toString() {
        return codigo;
    }
}
