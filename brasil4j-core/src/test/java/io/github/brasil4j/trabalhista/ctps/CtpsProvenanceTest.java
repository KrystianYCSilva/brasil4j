package io.github.brasil4j.trabalhista.ctps;

import io.github.brasil4j.geografia.Uf;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Status.*;
import static io.github.brasil4j.trabalhista.ctps.CtpsOrigin.*;
import static io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5;
import static org.junit.jupiter.api.Assertions.*;

class CtpsProvenanceTest {
    @ParameterizedTest
    @EnumSource(CtpsOrigin.class)
    void preservaCadaOrigemDaMesmaTripla(CtpsOrigin origin) {
        CtpsCheckResult result = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("0123456", "7890", "PR", origin, false));

        assertEquals(CONFORMING, result.getStatus());
        assertEquals(Optional.of(origin), result.getOrigin());
        assertFalse(result.hasSourceLoss());
        assertSame(Uf.PR, result.getRecord().get().getUf());
        assertEquals(origin == PHYSICAL ? Optional.of(Uf.PR) : Optional.empty(), result.getIssuingUf());
    }

    @Test
    void preservaPerdaSemPreencherOsTextos() {
        CtpsCheckResult result = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("123", "4", "PR", PHYSICAL, true));

        assertEquals(Optional.of(PHYSICAL), result.getOrigin());
        assertTrue(result.hasSourceLoss());
        assertEquals("123", result.getRecord().get().getNumber());
        assertEquals("4", result.getRecord().get().getSeries());
        assertEquals(Optional.of(Uf.PR), result.getIssuingUf());
    }

    @Test
    void preservaMetadadosDeGrupoPresenteMesmoSemRegistroConforme() {
        CtpsCheckResult incomplete = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("123", null, "PR", PHYSICAL, true));
        assertEquals(INCOMPLETE, incomplete.getStatus());
        assertEquals(Optional.of(PHYSICAL), incomplete.getOrigin());
        assertTrue(incomplete.hasSourceLoss());
        assertFalse(incomplete.getIssuingUf().isPresent());

        CtpsCheckResult invalid = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("1", "4", "PR", PHYSICAL, true));
        assertEquals(OUT_OF_PROFILE, invalid.getStatus());
        assertEquals(Optional.of(PHYSICAL), invalid.getOrigin());
        assertTrue(invalid.hasSourceLoss());
        assertFalse(invalid.getIssuingUf().isPresent());
    }

    @Test
    void ausenteNaoCarregaProveniencia() {
        CtpsCheckResult result = ESOCIAL_S2200_V2_5.check(null);
        assertFalse(result.getOrigin().isPresent());
        assertFalse(result.hasSourceLoss());
        assertFalse(result.getIssuingUf().isPresent());
    }
}
