package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Definição imutável de uma faixa contígua em uma {@link TabelaProgressiva}.
 *
 * <p>A primeira faixa cobre {@code [0, limiteSuperior]} e cada faixa subsequente cobre
 * {@code (limiteInferior, limiteSuperior]} (ou {@code (limiteInferior, +infinito)} na última faixa).
 *
 * @param <T> subtipo monetário da tabela
 */
public final class FaixaProgressiva<T extends Dinheiro> {
    private final int indice;
    private final T limiteInferior;
    private final T limiteSuperior;
    private final Percentual aliquota;
    private final BigDecimal parcelaADeduzirExata;

    FaixaProgressiva(
            int indice,
            T limiteInferior,
            T limiteSuperior,
            Percentual aliquota,
            BigDecimal parcelaADeduzirExata) {
        this.indice = indice;
        this.limiteInferior = limiteInferior;
        this.limiteSuperior = limiteSuperior;
        this.aliquota = aliquota;
        this.parcelaADeduzirExata = parcelaADeduzirExata;
    }

    /**
     * Retorna o número sequencial desta faixa iniciado em {@code 1}.
     *
     * @return índice da faixa ({@code >= 1})
     */
    public int getIndice() {
        return indice;
    }

    /**
     * Retorna o limite inferior desta faixa ({@code zero} na 1ª faixa).
     *
     * @return limite inferior
     */
    public T getLimiteInferior() {
        return limiteInferior;
    }

    /**
     * Retorna o limite superior desta faixa, ou {@link Optional#empty()} na faixa final aberta.
     *
     * @return limite superior opcional
     */
    public Optional<T> getLimiteSuperior() {
        return Optional.ofNullable(limiteSuperior);
    }

    /**
     * Retorna a alíquota marginal incidente sobre a parcela da base contida nesta faixa.
     *
     * @return alíquota não negativa
     */
    public Percentual getAliquota() {
        return aliquota;
    }

    /**
     * Retorna a parcela matemática exata a deduzir correspondente ao cálculo direto em
     * {@link ModoApuracaoProgressiva#NO_TOTAL} ({@code base * aliquota - parcelaADeduzirExata}).
     *
     * <p>Nota: pode assumir valor negativo caso uma faixa superior possua alíquota menor que faixas
     * anteriores (como {@code 0%} acima de um teto contributivo).
     *
     * @return parcela exata a deduzir ({@code scale() >= 0})
     */
    public BigDecimal getParcelaADeduzirExata() {
        return parcelaADeduzirExata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FaixaProgressiva)) {
            return false;
        }
        FaixaProgressiva<?> outra = (FaixaProgressiva<?>) obj;
        return indice == outra.indice
                && Objects.equals(limiteInferior, outra.limiteInferior)
                && Objects.equals(limiteSuperior, outra.limiteSuperior)
                && Objects.equals(aliquota, outra.aliquota)
                && Objects.equals(parcelaADeduzirExata, outra.parcelaADeduzirExata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indice, limiteInferior, limiteSuperior, aliquota, parcelaADeduzirExata);
    }

    @Override
    public String toString() {
        return "FaixaProgressiva["
                + indice
                + ": "
                + limiteInferior
                + ".."
                + (limiteSuperior != null ? limiteSuperior : "+infinito")
                + " @ "
                + aliquota
                + "]";
    }
}
