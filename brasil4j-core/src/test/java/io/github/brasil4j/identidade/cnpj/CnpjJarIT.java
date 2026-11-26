package io.github.brasil4j.identidade.cnpj;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CnpjJarIT {
    @ParameterizedTest
    @CsvFileSource(resources = {"/cnpj-c1.csv", "/cnpj-rfb-ascii.csv"}, nullValues = "<null>", ignoreLeadingAndTrailingWhitespace = false)
    void executaVetoresDeCnpjPeloJarDoNucleo(String entrada, String canonico) {
        assertTrue(Cnpj.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
        assertEquals(canonico != null, CnpjValidator.isValid(entrada));
        if (canonico == null) {
            assertThrows(IllegalArgumentException.class, () -> Cnpj.parse(entrada));
        } else {
            assertEquals(canonico, Cnpj.parse(entrada).getCanonicalValue());
        }
    }
}
