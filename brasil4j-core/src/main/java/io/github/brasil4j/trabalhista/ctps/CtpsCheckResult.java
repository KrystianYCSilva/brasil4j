package io.github.brasil4j.trabalhista.ctps;

import io.github.brasil4j.geografia.Uf;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/** Desfecho estrutural imutável da conferência de um grupo histórico. */
public final class CtpsCheckResult {
    /** Estado do grupo no perfil selecionado. */
    public enum Status {
        /** Grupo omitido. */
        ABSENT,
        /** Grupo presente com pelo menos um campo omitido. */
        INCOMPLETE,
        /** Campos fornecidos com violação das facetas do perfil. */
        OUT_OF_PROFILE,
        /** Grupo presente com os três campos conformes. */
        CONFORMING
    }

    /** Campo do grupo, na ordem do leiaute. */
    public enum Field {
        /** Número. */
        NUMBER,
        /** Série. */
        SERIES,
        /** UF registrada. */
        UF
    }

    private final CtpsProfile profile;
    private final Status status;
    private final Set<Field> affectedFields;
    private final CtpsRecord record;
    private final CtpsOrigin origin;
    private final boolean sourceLoss;

    CtpsCheckResult(CtpsProfile profile, Status status, EnumSet<Field> affectedFields,
                    CtpsRecord record, CtpsGroup group) {
        this.profile = profile;
        this.status = status;
        this.record = record;
        this.origin = group == null ? null : group.origin;
        this.sourceLoss = group != null && group.sourceLoss;
        this.affectedFields = Collections.unmodifiableSet(EnumSet.copyOf(affectedFields));
    }

    /**
     * Obtém o perfil selecionado.
     *
     * @return perfil aplicado à conferência
     */
    public CtpsProfile getProfile() {
        return profile;
    }

    /**
     * Obtém o desfecho da conferência.
     *
     * @return estado estrutural do grupo
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Identifica os campos omitidos ou fora das facetas.
     *
     * @return conjunto imutável dos campos afetados na ordem do leiaute
     */
    public Set<Field> getAffectedFields() {
        return affectedFields;
    }

    /**
     * Obtém explicitamente o registro conforme.
     *
     * @return registro com os textos originais somente se o grupo está conforme
     */
    public Optional<CtpsRecord> getRecord() {
        return Optional.ofNullable(record);
    }

    /**
     * Obtém a origem informada pelo consumidor.
     *
     * @return origem declarada para grupo presente, mesmo incompleto ou fora do perfil
     */
    public Optional<CtpsOrigin> getOrigin() {
        return Optional.ofNullable(origin);
    }

    /**
     * Indica se o consumidor declarou possível perda de informação.
     *
     * @return falso para grupo ausente e a indicação declarada nos demais casos
     */
    public boolean hasSourceLoss() {
        return sourceLoss;
    }

    /**
     * Obtém a UF de expedição quando a proveniência física permite essa interpretação.
     *
     * @return UF somente para registro conforme de origem física
     */
    public Optional<Uf> getIssuingUf() {
        return origin == CtpsOrigin.PHYSICAL ? getRecord().map(CtpsRecord::getUf) : Optional.empty();
    }

    /**
     * Resume a conferência sem exibir os valores dos campos ou da proveniência.
     *
     * @return perfil, desfecho e nomes dos campos afetados
     */
    @Override
    public String toString() {
        return "CtpsCheckResult[profile=" + profile + ", status=" + status
                + ", affectedFields=" + affectedFields + "]";
    }
}
