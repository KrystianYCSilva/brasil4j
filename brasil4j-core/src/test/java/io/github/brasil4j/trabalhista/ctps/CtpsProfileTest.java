package io.github.brasil4j.trabalhista.ctps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Field.*;
import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Status.*;
import static io.github.brasil4j.trabalhista.ctps.CtpsOrigin.UNDETERMINED;
import static io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5;
import static org.junit.jupiter.api.Assertions.*;

class CtpsProfileTest {
    @Test
    void distingueGrupoAusenteDeGrupoPresenteVazio() {
        CtpsCheckResult absent = ESOCIAL_S2200_V2_5.check(null);
        assertSame(ESOCIAL_S2200_V2_5, absent.getProfile());
        assertEquals(ABSENT, absent.getStatus());
        assertTrue(absent.getAffectedFields().isEmpty());

        CtpsCheckResult empty = ESOCIAL_S2200_V2_5.check(CtpsGroup.of(null, null, null, UNDETERMINED, false));
        assertEquals(INCOMPLETE, empty.getStatus());
        assertEquals(Arrays.asList(NUMBER, SERIES, UF), new ArrayList<>(empty.getAffectedFields()));
        assertThrows(UnsupportedOperationException.class, () -> empty.getAffectedFields().clear());
    }

    @Test
    void identificaCamposNulosAntesDasFacetasDosCamposFornecidos() {
        CtpsCheckResult invalidAndMissing = ESOCIAL_S2200_V2_5.check(
                CtpsGroup.of("1", null, "xx", UNDETERMINED, false));
        assertEquals(INCOMPLETE, invalidAndMissing.getStatus());
        assertEquals(Collections.singleton(SERIES), invalidAndMissing.getAffectedFields());

        CtpsCheckResult twoMissing = ESOCIAL_S2200_V2_5.check(CtpsGroup.of(null, "", null, UNDETERMINED, false));
        assertEquals(INCOMPLETE, twoMissing.getStatus());
        assertEquals(Arrays.asList(NUMBER, UF), new ArrayList<>(twoMissing.getAffectedFields()));
    }

    @Test
    void exigeOrigemExplicitaComMensagemSegura() {
        NullPointerException failure = assertThrows(NullPointerException.class,
                () -> CtpsGroup.of("NUMERO", "ABC", "PR", null, false));
        assertEquals("Origem da CTPS obrigatória.", failure.getMessage());
    }
}
