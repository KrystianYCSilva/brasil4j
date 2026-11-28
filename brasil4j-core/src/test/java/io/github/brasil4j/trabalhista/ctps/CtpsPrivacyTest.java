package io.github.brasil4j.trabalhista.ctps;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static io.github.brasil4j.trabalhista.ctps.CtpsOrigin.PHYSICAL;
import static io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5;
import static org.junit.jupiter.api.Assertions.*;

class CtpsPrivacyTest {
    @Test
    void ocultaGrupoRegistroEDiagnosticosDosQuatroDesfechos() {
        CtpsGroup group = CtpsGroup.of("NUM-S3", "@@S@@", "SC", PHYSICAL, true);
        CtpsCheckResult present = ESOCIAL_S2200_V2_5.check(group);
        CtpsRecord record = present.getRecord().get();

        assertEquals("[Grupo CTPS oculto]", group.toString());
        assertEquals("[Registro CTPS oculto]", record.toString());
        assertEquals("NUM-S3", record.getNumber());
        assertEquals("@@S@@", record.getSeries());
        assertEquals("SC", record.getUf().getSigla());

        CtpsCheckResult absent = ESOCIAL_S2200_V2_5.check(null);
        CtpsCheckResult missing = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("NUM-S3", null, "SC", PHYSICAL, true));
        CtpsCheckResult invalid = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("NUM-S3", "@@S@@", "INVALID-UF", PHYSICAL, true));
        for (CtpsCheckResult result : Arrays.asList(present, absent, missing, invalid)) {
            String diagnostic = result.toString();
            assertEquals("CtpsCheckResult[profile=ESOCIAL_S2200_V2_5, status=" + result.getStatus()
                    + ", affectedFields=" + result.getAffectedFields() + "]", diagnostic);
            for (String sentinel : Arrays.asList("NUM-S3", "@@S@@", "SC", "INVALID-UF")) {
                assertFalse(diagnostic.contains(sentinel));
            }
        }
    }
}
