package io.github.brasil4j.financas;

/**
 * Convenção matemática de arredondamento em apurações de {@link TabelaProgressiva}.
 */
public enum ModoApuracaoProgressiva {
    /**
     * Soma os produtos exatos de todas as faixas e aplica um único arredondamento no total,
     * reconciliando os valores apurados por faixa pelo método dos maiores restos (Hamilton).
     */
    NO_TOTAL,

    /**
     * Arredonda individualmente o valor de cada faixa e obtém o total pela soma das parcelas arredondadas.
     */
    POR_FAIXA
}
