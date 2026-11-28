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
        String number = group.number;
        String series = group.series;
        String ufCode = group.uf;
        if (number == null || series == null || ufCode == null) {
            if (number == null) {
                affected.add(Field.NUMBER);
            }
            if (series == null) {
                affected.add(Field.SERIES);
            }
            if (ufCode == null) {
                affected.add(Field.UF);
            }
            return new CtpsCheckResult(this, Status.INCOMPLETE, affected, null, group);
        }
        if (lengthOutOfRange(number, 2, 11)) {
            affected.add(Field.NUMBER);
        }
        if (lengthOutOfRange(series, 1, 5)) {
            affected.add(Field.SERIES);
        }
        Uf uf = UFS_BY_SIGLA.get(ufCode);
        if (uf == null) {
            affected.add(Field.UF);
        }
        if (!affected.isEmpty()) {
            return new CtpsCheckResult(this, Status.OUT_OF_PROFILE, affected, null, group);
        }
        CtpsRecord record = new CtpsRecord(number, series, uf);
        return new CtpsCheckResult(this, Status.CONFORMING, affected, record, group);
    }

    private static boolean lengthOutOfRange(String value, int minimum, int maximum) {
        int length = value.codePointCount(0, value.length());
        return length < minimum || length > maximum;
    }
}
