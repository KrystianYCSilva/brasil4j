package io.github.brasil4j.identidade.cnpj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static io.github.brasil4j.identidade.cnpj.CnpjValidationMode.ALPHANUMERIC;
import static io.github.brasil4j.identidade.cnpj.CnpjValidationMode.FORMAT_ONLY;
import static io.github.brasil4j.identidade.cnpj.CnpjValidationMode.NUMERIC;
import static org.junit.jupiter.api.Assertions.*;

class CnpjTest {
    @ParameterizedTest
    @CsvFileSource(resources = {"/cnpj-c1.csv", "/cnpj-rfb-ascii.csv"}, nullValues = "<null>", ignoreLeadingAndTrailingWhitespace = false)
    void contratoPadrao(String entrada, String canonico) {
        assertEquals(canonico != null, CnpjValidator.isValid(entrada));
        if (canonico == null) {
            assertEquals("CNPJ inválido.", assertThrows(IllegalArgumentException.class,
                    () -> Cnpj.parse(entrada)).getMessage());
            assertThrows(IllegalArgumentException.class, () -> CnpjFormatter.format(entrada));
            assertThrows(IllegalArgumentException.class, () -> CnpjFormatter.unformat(entrada));
        } else {
            assertEquals(canonico, Cnpj.parse(entrada).getCanonicalValue());
            assertEquals(canonico, CnpjFormatter.unformat(entrada));
            assertEquals(canonico, CnpjFormatter.unformat(CnpjFormatter.format(entrada)));
        }
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "90.024.778/000123", "1.2ABC34501DE35", "12ABC34501DEA5", "12!ABC34501DE35",
        " 12.ABC.345/01DE-35 ", "12.abc.345/01de-35", "１２ABC34501DE35",
        "90.025.255/0001", "90.024.420/0001A2", "1345c3A5000106"
    })
    void rejeitaApresentacaoInvalidaEmTodosOsModos(String entrada) {
        for (CnpjValidationMode mode : CnpjValidationMode.values()) {
            assertFalse(CnpjValidator.isValid(entrada, mode), mode.name());
            assertThrows(IllegalArgumentException.class, () -> Cnpj.parse(entrada, mode), mode.name());
        }
    }

    @Test
    void validaNuloPelaApiJava() {
        contratoPadrao(null, null);
    }

    @Test
    void ofereceTresModosSemConfundirTipoEPolitica() {
        assertTrue(CnpjValidator.isValid("12ABC34501DE35", ALPHANUMERIC));
        assertTrue(CnpjValidator.isValid("00000000000191", ALPHANUMERIC));
        assertTrue(CnpjValidator.isValid("00.000.000/0001-91", NUMERIC));
        assertFalse(CnpjValidator.isValid("12ABC34501DE35", NUMERIC));
        assertFalse(CnpjValidator.isValid("00000000000192", NUMERIC));
        assertFalse(CnpjValidator.isValid("00000000000000", NUMERIC));
        assertFalse(CnpjValidator.isValid(null, NUMERIC));
        assertFalse(CnpjValidator.isValid("12.ABC.345/01DE-A5", FORMAT_ONLY));
        assertFalse(CnpjValidator.isValid("1.2ABC34501DE35", FORMAT_ONLY));
        assertTrue(CnpjValidator.isValid("00000000000000", FORMAT_ONLY));
        assertTrue(CnpjValidator.isValid("12ABC34501DE36", FORMAT_ONLY));
        assertEquals("12.ABC.345/01DE-36", Cnpj.parse("12ABC34501DE36", FORMAT_ONLY).applyMask());
        assertThrows(IllegalArgumentException.class, () -> Cnpj.parse("12ABC34501DE35", NUMERIC));
        assertThrows(IllegalArgumentException.class, () -> Cnpj.parse("12ABC34501DE35", null));
        assertThrows(IllegalArgumentException.class, () -> CnpjValidator.isValid(null, null));
    }

    @Test
    void decompoeEComparaPeloCanonico() {
        Cnpj valor = Cnpj.parse("12.ABC.345/01DE-35");
        assertEquals("12ABC345", valor.getRoot());
        assertEquals("01DE", valor.getBranch());
        assertEquals("12ABC34501DE", valor.getBase());
        assertEquals("35", valor.getCheckDigits());
        assertEquals(CnpjType.ALPHANUMERIC, valor.getType());
        assertEquals("12ABC34501DE35", valor.toString());
        assertEquals("12.ABC.345/01DE-35", valor.applyMask());
        Cnpj canonico = Cnpj.parse("12ABC34501DE35", FORMAT_ONLY);
        assertEquals(valor, canonico);
        assertEquals(valor.hashCode(), canonico.hashCode());
        assertNotEquals(valor, Cnpj.parse("1345C3A5000106"));
        assertNotEquals(valor, "12ABC34501DE35");
        assertFalse(valor.equals(null));
        Cnpj numerico = Cnpj.parse("00000000000191", NUMERIC);
        assertEquals(CnpjType.NUMERIC, numerico.getType());
        assertEquals("00000000", numerico.getRoot());
        assertEquals("0001", numerico.getBranch());
        assertEquals("000000000001", numerico.getBase());
    }
    @Test
    void modoInternoTambemConfereApresentacao() {
        assertNull(FORMAT_ONLY.validatedCanonical("qualquer valor"));
        assertNull(FORMAT_ONLY.validatedCanonical(null));
        assertNull(NUMERIC.validatedCanonical("qualquer valor"));
        assertNull(ALPHANUMERIC.validatedCanonical("1.2ABC34501DE35"));
        assertNull(FORMAT_ONLY.validatedCanonical("90.024.778/000123"));
        assertEquals("12ABC34501DE36", FORMAT_ONLY.validatedCanonical("12.ABC.345/01DE-36"));
    }

}
