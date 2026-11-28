package io.github.brasil4j.trabalhista.ctps;

import io.github.brasil4j.geografia.Uf;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Field;
import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Status;

/** Perfil explícito de conferência dos campos legados da CTPS. */
public enum CtpsProfile {
    /** Grupo CTPS do evento de admissão S-2200 v2.5. */
    ESOCIAL_S2200_V2_5;

    private static final Map<String, Uf> UFS_BY_SIGLA = Arrays.stream(Uf.values())
            .collect(Collectors.toMap(Uf::getSigla, Function.identity()));

    /**
     * Confere os campos já extraídos do grupo histórico.
     *
     * @param group grupo presente, ou nulo quando omitido
     * @return desfecho estrutural relativo ao perfil
     */
    public CtpsCheckResult check(CtpsGroup group) {
        EnumSet<Field> affected = EnumSet.noneOf(Field.class);
        if (group == null) {
            return new CtpsCheckResult(this, Status.ABSENT, affected, null, null);
        }
        if (group.number == null) {
            affected.add(Field.NUMBER);
        }
        if (group.series == null) {
            affected.add(Field.SERIES);
        }
        if (group.uf == null) {
            affected.add(Field.UF);
        }
        if (!affected.isEmpty()) {
            return new CtpsCheckResult(this, Status.INCOMPLETE, affected, null, group);
        }
        if (!lengthInRange(group.number, 2, 11)) {
            affected.add(Field.NUMBER);
        }
        if (!lengthInRange(group.series, 1, 5)) {
            affected.add(Field.SERIES);
        }
        Uf uf = UFS_BY_SIGLA.get(group.uf);
        if (uf == null) {
            affected.add(Field.UF);
        }
        if (!affected.isEmpty()) {
            return new CtpsCheckResult(this, Status.OUT_OF_PROFILE, affected, null, group);
        }
        CtpsRecord record = new CtpsRecord(group.number, group.series, uf);
        return new CtpsCheckResult(this, Status.CONFORMING, affected, record, group);
    }

    private static boolean lengthInRange(String value, int minimum, int maximum) {
        int length = value.codePointCount(0, value.length());
        return length >= minimum && length <= maximum;
    }
}
