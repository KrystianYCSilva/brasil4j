package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Resultado consolidado e auditável da apuração de uma {@link TabelaProgressiva}.
 *
 * @param <T> subtipo monetário da tabela
 */
public final class ApuracaoProgressiva<T extends Dinheiro> {
    private final T baseCalculo;
    private final ModoApuracaoProgressiva modo;
    private final MetodoArredondamento arredondamento;
    private final List<FaixaApurada<T>> faixas;
    private final BigDecimal valorExatoTotal;
    private final T valorTotal;
    private final BigDecimal diferencaArredondamento;

    ApuracaoProgressiva(
            T baseCalculo,
            ModoApuracaoProgressiva modo,
            MetodoArredondamento arredondamento,
            List<FaixaApurada<T>> faixas,
            BigDecimal valorExatoTotal,
            T valorTotal,
            BigDecimal diferencaArredondamento) {
        this.baseCalculo = baseCalculo;
        this.modo = modo;
        this.arredondamento = arredondamento;
        this.faixas = faixas;
        this.valorExatoTotal = valorExatoTotal;
        this.valorTotal = valorTotal;
        this.diferencaArredondamento = diferencaArredondamento;
    }

    /**
     * Retorna a base de cálculo submetida à apuração.
     *
     * @return base de cálculo ({@code >= 0})
     */
    public T getBaseCalculo() {
        return baseCalculo;
    }

    /**
     * Retorna o modo de arredondamento progressivo utilizado ({@link ModoApuracaoProgressiva#NO_TOTAL} ou {@link ModoApuracaoProgressiva#POR_FAIXA}).
     *
     * @return modo de apuração
     */
    public ModoApuracaoProgressiva getModo() {
        return modo;
    }

    /**
     * Retorna a política de arredondamento utilizada e retida nos montantes resultantes.
     *
     * @return política de arredondamento
     */
    public MetodoArredondamento getArredondamento() {
        return arredondamento;
    }

    /**
     * Retorna a memória imutável de todas as faixas da tabela na ordem configurada.
     *
     * @return lista imutável de faixas apuradas
     */
    public List<FaixaApurada<T>> getFaixas() {
        return faixas;
    }

    /**
     * Retorna a soma matemática exata ({@code sum(faixa.getValorExato())}) sem arredondamento em ambos os modos.
     *
     * @return soma exata ({@code scale() >= 0})
     */
    public BigDecimal getValorExatoTotal() {
        return valorExatoTotal;
    }

    /**
     * Retorna o montante total apurado na moeda da tabela (sempre idêntico à soma de {@link FaixaApurada#getValorApurado()}).
     *
     * @return valor monetário total apurado
     */
    public T getValorTotal() {
        return valorTotal;
    }

    /**
     * Retorna a diferença algébrica de arredondamento {@code getValorTotal().getValor().subtract(getValorExatoTotal())}.
     *
     * @return diferença de arredondamento
     */
    public BigDecimal getDiferencaArredondamento() {
        return diferencaArredondamento;
    }

    /**
     * Calcula a alíquota efetiva ({@code (valorTotal / baseCalculo) * 100%}) com a quantidade de
     * casas decimais percentuais e a política informadas. Para {@code baseCalculo == 0}, devolve {@link Percentual#ZERO}.
     *
     * @param casasPercentuais casas decimais nos pontos percentuais ({@code 0..18})
     * @param arredondamentoAliquota política de arredondamento para a divisão
     * @return alíquota efetiva resultante
     * @throws IllegalArgumentException se {@code casasPercentuais} estiver fora de {@code 0..18} ou política nula/inválida
     */
    public Percentual getAliquotaEfetiva(
            int casasPercentuais, MetodoArredondamento arredondamentoAliquota) {
        if (casasPercentuais < 0 || casasPercentuais > 18) {
            throw new IllegalArgumentException(
                    "A quantidade de casas percentuais deve estar entre 0 e 18: " + casasPercentuais);
        }
        AritmeticaMonetaria.exigirArredondamento(arredondamentoAliquota);
        if (baseCalculo.isZero() || valorTotal.isZero()) {
            return Percentual.ZERO;
        }
        BigDecimal pontos = AritmeticaMonetaria.dividirExato(
                valorTotal.valor.movePointRight(2),
                baseCalculo.valor,
                casasPercentuais,
                arredondamentoAliquota);
        return Percentual.de(pontos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ApuracaoProgressiva)) {
            return false;
        }
        ApuracaoProgressiva<?> outra = (ApuracaoProgressiva<?>) obj;
        return Objects.equals(baseCalculo, outra.baseCalculo)
                && modo == outra.modo
                && Objects.equals(arredondamento, outra.arredondamento)
                && Objects.equals(faixas, outra.faixas)
                && Objects.equals(valorExatoTotal, outra.valorExatoTotal)
                && Objects.equals(valorTotal, outra.valorTotal)
                && Objects.equals(diferencaArredondamento, outra.diferencaArredondamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                baseCalculo,
                modo,
                arredondamento,
                faixas,
                valorExatoTotal,
                valorTotal,
                diferencaArredondamento);
    }

    @Override
    public String toString() {
        return "ApuracaoProgressiva[base="
                + baseCalculo
                + ", modo="
                + modo
                + ", total="
                + valorTotal
                + "]";
    }
}
