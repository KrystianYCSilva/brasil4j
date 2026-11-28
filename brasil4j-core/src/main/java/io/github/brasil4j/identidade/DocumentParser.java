package io.github.brasil4j.identidade;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Estratégia que interpreta um texto como documento, sem lançar exceção para entrada inválida.
 * Interpretadores são sem estado e reutilizáveis. Regras e políticas acrescentadas por
 * {@link #withRule(ValidationRule)} e {@link #withPolicy(Predicate)} decoram o interpretador e só
 * executam depois da aceitação nativa, na ordem em que foram acrescentadas.
 *
 * @param <D> documento produzido
 */
@FunctionalInterface
public interface DocumentParser<D extends Document> {
    /**
     * Interpreta o texto.
     *
     * @param input texto, possivelmente nulo
     * @return documento aceito ou rejeição com motivo
     */
    ValidationResult<D> parse(String input);

    /**
     * Interpreta o texto e lança {@link IllegalArgumentException} em caso de rejeição.
     * A mensagem é a configurada no interpretador e não contém a entrada.
     *
     * @param input texto, possivelmente nulo
     * @return documento aceito
     * @throws IllegalArgumentException se a entrada for rejeitada
     */
    default D parseOrThrow(String input) {
        return parse(input).orElseThrow();
    }

    /**
     * Indica se o texto é aceito.
     *
     * @param input texto, possivelmente nulo
     * @return verdadeiro quando a interpretação aceita a entrada
     */
    default boolean isValid(String input) {
        return parse(input).isValid();
    }

    /**
     * Acrescenta uma regra aplicada ao documento aceito.
     *
     * @param rule regra adicional
     * @return novo interpretador decorado; o original permanece inalterado
     * @throws NullPointerException se a regra for nula
     */
    default DocumentParser<D> withRule(ValidationRule<? super D> rule) {
        return DocumentParsers.decorate(this, Objects.requireNonNull(rule, "Regra obrigatória."));
    }

    /**
     * Acrescenta uma política do consumidor. A recusa gera {@link RejectionReason#POLICY}.
     *
     * @param policy condição de aceitação
     * @return novo interpretador decorado
     * @throws NullPointerException se a política for nula
     */
    default DocumentParser<D> withPolicy(Predicate<? super D> policy) {
        return withRule(ValidationRule.of(policy, Rejection.of(RejectionReason.POLICY)));
    }

    /**
     * Acrescenta uma política do consumidor identificada por um código simbólico.
     *
     * @param policy condição de aceitação
     * @param code código incluído na rejeição
     * @return novo interpretador decorado
     * @throws NullPointerException se a política ou o código forem nulos
     * @throws IllegalArgumentException se o código for vazio ou só tiver espaços
     */
    default DocumentParser<D> withPolicy(Predicate<? super D> policy, String code) {
        Objects.requireNonNull(policy, "Política obrigatória.");
        return withRule(ValidationRule.of(policy, Rejection.of(RejectionReason.POLICY, code)));
    }
}
