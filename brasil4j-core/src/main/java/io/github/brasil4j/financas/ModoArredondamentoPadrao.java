package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementação interna das políticas padrão de {@link MetodoArredondamento}.
 */
enum ModoArredondamentoPadrao implements MetodoArredondamento {
    ABNT_NBR_5891(RoundingMode.HALF_EVEN),
    FINANCEIRO(RoundingMode.HALF_UP),
    TRUNCAMENTO(RoundingMode.DOWN),
    PARA_CIMA(RoundingMode.UP),
    TETO(RoundingMode.CEILING),
    PISO(RoundingMode.FLOOR),
    MEIO_PARA_BAIXO(RoundingMode.HALF_DOWN);

    private final RoundingMode roundingMode;

    ModoArredondamentoPadrao(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    RoundingMode roundingMode() {
        return roundingMode;
    }

    @Override
    public BigDecimal arredondar(BigDecimal valor, int casasDecimais) {
        AritmeticaMonetaria.validarLimitesIntermediariosArredondamento(valor);
        ValidacaoMoeda.validarCasasDecimais(casasDecimais);
        BigDecimal escalado = valor.setScale(casasDecimais, roundingMode);
        return AritmeticaMonetaria.normalizarZero(escalado, casasDecimais);
    }
}
