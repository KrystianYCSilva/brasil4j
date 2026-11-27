package io.github.brasil4j.identidade.composicao;

import java.util.Optional;

/**
 * Decisão de uma conferência com acesso explícito à saída opcional.
 * A representação textual padrão omite o conteúdo do documento e a saída formatada.
 */
public final class DocumentCheckResult {
    /** Etapa que determinou a decisão da conferência. */
    public enum Status {
        /** A regra nativa rejeitou a entrada. */
        REJECTED_BY_VALIDATION,
        /** A regra adicional rejeitou o valor que passou pela conferência nativa. */
        REJECTED_BY_POLICY,
        /** A entrada atendeu às regras configuradas. */
        ACCEPTED
    }

    private final Status status;
    private final String formattedValue;

    DocumentCheckResult(Status status, String formattedValue) {
        this.status = status;
        this.formattedValue = formattedValue;
    }

    /**
     * Retorna a decisão da conferência.
     *
     * @return estado da decisão
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Retorna a apresentação produzida, inclusive texto vazio se produzido.
     * A saída pode conter o documento conforme a função escolhida pelo consumidor.
     *
     * @return saída, presente somente após aceitação com formatador
     */
    public Optional<String> getFormattedValue() {
        return Optional.ofNullable(formattedValue);
    }
}
