package io.github.brasil4j.identidade;

import java.util.Objects;
import java.util.Optional;

/**
 * Motivo imutável de uma rejeição, com código simbólico opcional.
 * A biblioteca nunca inclui a entrada, o valor canônico ou a saída formatada em uma rejeição.
 * O conteúdo do código é responsabilidade de quem o define.
 */
public final class Rejection {
    private final RejectionReason reason;
    private final String code;

    private Rejection(RejectionReason reason, String code) {
        this.reason = reason;
        this.code = code;
    }

    /**
     * Cria uma rejeição sem código.
     *
     * @param reason motivo obrigatório
     * @return rejeição com o motivo informado
     * @throws NullPointerException se o motivo for nulo
     */
    public static Rejection of(RejectionReason reason) {
        return new Rejection(Objects.requireNonNull(reason, "Motivo obrigatório."), null);
    }

    /**
     * Cria uma rejeição com código simbólico, por exemplo o identificador de uma regra do consumidor.
     *
     * @param reason motivo obrigatório
     * @param code código simbólico, não vazio
     * @return rejeição com motivo e código
     * @throws NullPointerException se o motivo ou o código forem nulos
     * @throws IllegalArgumentException se o código for vazio ou só tiver espaços
     */
    public static Rejection of(RejectionReason reason, String code) {
        Objects.requireNonNull(reason, "Motivo obrigatório.");
        Objects.requireNonNull(code, "Código obrigatório.");
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código de rejeição vazio.");
        }
        return new Rejection(reason, code);
    }

    /**
     * Obtém o motivo da rejeição.
     *
     * @return motivo
     */
    public RejectionReason getReason() {
        return reason;
    }

    /**
     * Obtém o código simbólico, quando definido.
     *
     * @return código ou vazio
     */
    public Optional<String> getCode() {
        return Optional.ofNullable(code);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Rejection)) {
            return false;
        }
        Rejection rejection = (Rejection) other;
        return reason == rejection.reason && Objects.equals(code, rejection.code);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(reason, code);
    }

    /**
     * Representa a rejeição pelo motivo e pelo código.
     *
     * @return texto diagnóstico sem conteúdo de documento produzido pela biblioteca
     */
    @Override
    public String toString() {
        return code == null ? "Rejection[" + reason + "]" : "Rejection[" + reason + ":" + code + "]";
    }
}
