package io.github.brasil4j.verificacao;

import java.util.Objects;
import java.util.Optional;

/**
 * Estratégia de cálculo de dígitos verificadores, independente do documento.
 * Implementações recusam explicitamente entradas fora do domínio, sem produzir DV inválido, com
 * {@link InvalidBaseException}. Qualquer outra {@link IllegalArgumentException} é tratada como defeito
 * do algoritmo e não é convertida em rejeição de formato pelas regras da biblioteca.
 */
public interface CheckDigitAlgorithm {
    /**
     * Quantidade de dígitos verificadores ao final do valor.
     *
     * @return quantidade, maior que zero
     */
    int digitCount();

    /**
     * Calcula os dígitos verificadores de uma base.
     *
     * @param base caracteres sem os dígitos verificadores
     * @return dígitos calculados, ou vazio quando a base não admite DV válido
     * @throws NullPointerException se a base for nula
     * @throws InvalidBaseException se a base estiver fora do domínio do algoritmo
     */
    Optional<String> compute(String base);

    /**
     * Confere se os últimos {@link #digitCount()} caracteres coincidem com o cálculo sobre a base. Um
     * sufixo divergente, inclusive com caracteres não numéricos, devolve {@code false}; só a base é
     * conferida contra o domínio do algoritmo.
     *
     * @param canonical valor completo, com os dígitos verificadores ao final
     * @return verdadeiro quando os dígitos informados coincidem
     * @throws NullPointerException se o valor for nulo
     * @throws InvalidBaseException se o valor não tiver base ou se a base estiver fora do domínio
     */
    default boolean verify(String canonical) {
        Objects.requireNonNull(canonical, "Valor obrigatório.");
        int baseLength = canonical.length() - digitCount();
        if (baseLength < 1) {
            throw new InvalidBaseException("Valor sem base para o cálculo do DV.");
        }
        Optional<String> expected = compute(canonical.substring(0, baseLength));
        return expected.isPresent() && canonical.substring(baseLength).equals(expected.get());
    }
}
