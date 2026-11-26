package io.github.brasil4j.identidade.cpf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfJarIT {
    @Test
    void consomeCpfDoJarPublicadoPeloReator() {
        assertTrue(Cpf.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
        assertEquals("***.***.***-**", Cpf.parse("280.012.389-39").toString());
        assertFalse(CpfValidator.isValid("28001238939", CpfValidationMode.NUMERIC));
    }
}
