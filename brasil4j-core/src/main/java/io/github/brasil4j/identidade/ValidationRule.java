package io.github.brasil4j.identidade;

import io.github.brasil4j.verificacao.CheckDigitAlgorithm;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Regra componível que aceita um valor ou devolve uma rejeição.
 * Regras encadeadas por {@link #and(ValidationRule)} executam em ordem e param na primeira rejeição.
 *
 * @param <T> tipo do valor conferido
 */
@FunctionalInterface
public interface ValidationRule<T> {
    /**
     * Confere um valor.
     *
     * @param value valor a conferir
     * @return rejeição, ou vazio quando o valor é aceito
     */
    Optional<Rejection> check(T value);

    /**
     * Cria uma regra que rejeita quando a condição é falsa.
     *
     * @param condition condição de aceitação
     * @param rejection rejeição produzida quando a condição falha
     * @param <T> tipo do valor
     * @return regra
     * @throws NullPointerException se algum argumento for nulo
     */
    static <T> ValidationRule<T> of(Predicate<? super T> condition, Rejection rejection) {
        Objects.requireNonNull(condition, "Condição obrigatória.");
        Objects.requireNonNull(rejection, "Rejeição obrigatória.");
        return value -> condition.test(value) ? Optional.empty() : Optional.of(rejection);
    }

    /**
     * Cria a regra de dígitos verificadores sobre o valor canônico. Uma divergência gera
     * {@link RejectionReason#INVALID_CHECK_DIGITS}; um valor fora do domínio do algoritmo gera
     * {@link RejectionReason#INVALID_FORMAT}, sem exceção.
     *
     * @param algorithm algoritmo de dígitos verificadores
     * @return regra sobre o valor canônico
     * @throws NullPointerException se o algoritmo for nulo
     */
    static ValidationRule<String> checkDigits(CheckDigitAlgorithm algorithm) {
        Objects.requireNonNull(algorithm, "Algoritmo obrigatório.");
        return canonical -> {
            try {
                return algorithm.verify(canonical)
                        ? Optional.empty() : Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS));
            } catch (IllegalArgumentException outOfDomain) {
                return Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT));
            }
        };
    }

    /**
     * Regra neutra, que aceita qualquer valor. Em Java 8, informe o tipo quando não houver alvo,
     * como em {@code ValidationRule.<String>accept()}.
     *
     * @param <T> tipo do valor
     * @return regra que sempre aceita
     */
    static <T> ValidationRule<T> accept() {
        return value -> Optional.empty();
    }

    /**
     * Encadeia outra regra, executada somente se esta aceitar.
     *
     * @param next regra seguinte
     * @return regra composta
     * @throws NullPointerException se a regra seguinte for nula
     */
    default ValidationRule<T> and(ValidationRule<? super T> next) {
        Objects.requireNonNull(next, "Regra obrigatória.");
        return value -> {
            Optional<Rejection> rejection = check(value);
            return rejection.isPresent() ? rejection : next.check(value);
        };
    }
}
