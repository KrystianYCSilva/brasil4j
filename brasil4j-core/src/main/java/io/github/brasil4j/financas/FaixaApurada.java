package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Memória de cálculo imutável de uma faixa individual dentro de uma {@link ApuracaoProgressiva}.
 *
 * @param <T> subtipo monetário da tabela
 */
public final class FaixaApurada<T extends Dinheiro> {
    private final FaixaProgressiva<T> faixa;
    private final T baseNaFaixa;
    private final BigDecimal valorExato;
    private final T valorArredondadoIndividual;
    private final T valorApurado;

    FaixaApurada(
            FaixaProgressiva<T> faixa,
            T baseNaFaixa,
            BigDecimal valorExato,
            T valorArredondadoIndividual,
            T valorApurado) {
        this.faixa = faixa;
        this.baseNaFaixa = baseNaFaixa;
        this.valorExato = valorExato;
        this.valorArredondadoIndividual = valorArredondadoIndividual;
        this.valorApurado = valorApurado;
    }

    /**
     * Retorna a definição da faixa avaliada.
     *
     * @return definição da faixa
     */
    public FaixaProgressiva<T> getFaixa() {
        return faixa;
    }

    /**
     * Retorna a fatia da base de cálculo enquadrada nesta faixa ({@code zero} quando não atingida).
     *
     * @return fatia monetária da base
     */
    public T getBaseNaFaixa() {
        return baseNaFaixa;
    }

    /**
     * Retorna o produto decimal exato {@code baseNaFaixa * aliquota} antes de qualquer arredondamento.
     *
     * @return valor exato ({@code scale() >= 0})
     */
    public BigDecimal getValorExato() {
        return valorExato;
    }

    /**
     * Retorna o valor desta faixa caso arredondada isoladamente.
     *
     * @return valor arredondado individual
     */
    public T getValorArredondadoIndividual() {
        return valorArredondadoIndividual;
    }

    /**
     * Retorna o valor efetivamente atribuído a esta faixa na memória de cálculo, garantindo que a soma
     * das faixas coincida integralmente com {@link ApuracaoProgressiva#getValorTotal()}.
     *
     * @return valor apurado reconciliado na faixa
     */
    public T getValorApurado() {
        return valorApurado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FaixaApurada)) {
            return false;
        }
        FaixaApurada<?> outra = (FaixaApurada<?>) obj;
        return Objects.equals(faixa, outra.faixa)
                && Objects.equals(baseNaFaixa, outra.baseNaFaixa)
                && Objects.equals(valorExato, outra.valorExato)
                && Objects.equals(valorArredondadoIndividual, outra.valorArredondadoIndividual)
                && Objects.equals(valorApurado, outra.valorApurado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faixa, baseNaFaixa, valorExato, valorArredondadoIndividual, valorApurado);
    }

    @Override
    public String toString() {
        return "FaixaApurada[faixa="
                + faixa.getIndice()
                + ", base="
                + baseNaFaixa
                + ", exato="
                + valorExato.toPlainString()
                + ", apurado="
                + valorApurado
                + "]";
    }
}
