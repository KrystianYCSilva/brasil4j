package io.github.brasil4j.identidade;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Desfecho de uma interpretação: um valor aceito ou uma rejeição com motivo.
 * Rejeição é um resultado ordinário, sem exceção. As únicas implementações são internas.
 * A representação textual mostra apenas o estado e a rejeição, nunca o valor.
 *
 * @param <D> tipo do valor aceito
 */
public abstract class ValidationResult<D> {
    static final String DEFAULT_ERROR_MESSAGE = "Documento inválido.";

    ValidationResult() {
    }

    /**
     * Cria um resultado aceito.
     *
     * @param document valor aceito
     * @param <D> tipo do valor
     * @return resultado aceito
     * @throws NullPointerException se o valor for nulo
     */
    public static <D> ValidationResult<D> valid(D document) {
        return new Valid<>(Objects.requireNonNull(document, "Documento obrigatório."));
    }

    /**
     * Cria um resultado rejeitado.
     *
     * @param rejection motivo da rejeição
     * @param <D> tipo do valor que seria aceito
     * @return resultado rejeitado
     * @throws NullPointerException se a rejeição for nula
     */
    public static <D> ValidationResult<D> invalid(Rejection rejection) {
        return new Invalid<>(Objects.requireNonNull(rejection, "Rejeição obrigatória."));
    }

    /**
     * Indica se o valor foi aceito.
     *
     * @return verdadeiro quando há valor aceito
     */
    public abstract boolean isValid();

    /**
     * Obtém o valor aceito.
     *
     * @return valor, ou vazio quando rejeitado
     */
    public abstract Optional<D> getDocument();

    /**
     * Obtém a rejeição.
     *
     * @return rejeição, ou vazio quando aceito
     */
    public abstract Optional<Rejection> getRejection();

    /**
     * Transforma o valor aceito, preservando a rejeição.
     *
     * @param mapper função aplicada ao valor aceito, que não pode devolver nulo
     * @param <U> tipo produzido
     * @return novo resultado
     * @throws NullPointerException se a função for nula ou devolver nulo
     */
    public abstract <U> ValidationResult<U> map(Function<? super D, ? extends U> mapper);

    /**
     * Reduz o resultado a um valor, tratando os dois desfechos.
     *
     * @param onValid função aplicada ao valor aceito
     * @param onInvalid função aplicada à rejeição
     * @param <R> tipo produzido
     * @return valor produzido pela função do desfecho
     * @throws NullPointerException se alguma função for nula
     */
    public abstract <R> R fold(Function<? super D, ? extends R> onValid,
                               Function<? super Rejection, ? extends R> onInvalid);

    /**
     * Obtém o valor aceito ou lança {@link IllegalArgumentException} com mensagem genérica.
     *
     * @return valor aceito
     * @throws IllegalArgumentException se o resultado for uma rejeição
     */
    public D orElseThrow() {
        return orElseThrow(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Obtém o valor aceito ou lança {@link IllegalArgumentException} com a mensagem informada.
     *
     * @param message mensagem do erro, que não deve conter o documento
     * @return valor aceito
     * @throws NullPointerException se a mensagem for nula
     * @throws IllegalArgumentException se o resultado for uma rejeição
     */
    public D orElseThrow(String message) {
        Objects.requireNonNull(message, "Mensagem obrigatória.");
        return orElseThrow(rejection -> new IllegalArgumentException(message));
    }

    /**
     * Obtém o valor aceito ou lança a exceção produzida a partir da rejeição.
     *
     * @param exception função que cria a exceção
     * @param <X> tipo da exceção
     * @return valor aceito
     * @throws X se o resultado for uma rejeição
     * @throws NullPointerException se a função for nula ou devolver nulo
     */
    public abstract <X extends Throwable> D orElseThrow(Function<? super Rejection, ? extends X> exception)
            throws X;

    private static final class Valid<D> extends ValidationResult<D> {
        private final D document;

        Valid(D document) {
            this.document = document;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Optional<D> getDocument() {
            return Optional.of(document);
        }

        @Override
        public Optional<Rejection> getRejection() {
            return Optional.empty();
        }

        @Override
        public <U> ValidationResult<U> map(Function<? super D, ? extends U> mapper) {
            return valid(Objects.requireNonNull(mapper, "Função obrigatória.").apply(document));
        }

        @Override
        public <R> R fold(Function<? super D, ? extends R> onValid,
                          Function<? super Rejection, ? extends R> onInvalid) {
            Objects.requireNonNull(onInvalid, "Função de rejeição obrigatória.");
            return Objects.requireNonNull(onValid, "Função de aceitação obrigatória.").apply(document);
        }

        @Override
        public <X extends Throwable> D orElseThrow(Function<? super Rejection, ? extends X> exception) {
            Objects.requireNonNull(exception, "Função de exceção obrigatória.");
            return document;
        }

        @Override
        public String toString() {
            return "ValidationResult[VALID]";
        }
    }

    private static final class Invalid<D> extends ValidationResult<D> {
        private final Rejection rejection;

        Invalid(Rejection rejection) {
            this.rejection = rejection;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public Optional<D> getDocument() {
            return Optional.empty();
        }

        @Override
        public Optional<Rejection> getRejection() {
            return Optional.of(rejection);
        }

        @Override
        public <U> ValidationResult<U> map(Function<? super D, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "Função obrigatória.");
            return invalid(rejection);
        }

        @Override
        public <R> R fold(Function<? super D, ? extends R> onValid,
                          Function<? super Rejection, ? extends R> onInvalid) {
            Objects.requireNonNull(onValid, "Função de aceitação obrigatória.");
            return Objects.requireNonNull(onInvalid, "Função de rejeição obrigatória.").apply(rejection);
        }

        @Override
        public <X extends Throwable> D orElseThrow(Function<? super Rejection, ? extends X> exception) throws X {
            throw Objects.requireNonNull(Objects.requireNonNull(exception, "Função de exceção obrigatória.")
                    .apply(rejection), "Exceção obrigatória.");
        }

        @Override
        public String toString() {
            return "ValidationResult[INVALID:" + rejection + "]";
        }
    }
}
