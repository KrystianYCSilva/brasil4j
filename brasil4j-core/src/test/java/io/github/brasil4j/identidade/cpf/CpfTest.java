package io.github.brasil4j.identidade.cpf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static io.github.brasil4j.identidade.cpf.CpfValidationMode.FORMAT_ONLY;
import static io.github.brasil4j.identidade.cpf.CpfValidationMode.NUMERIC;
import static org.junit.jupiter.api.Assertions.*;

class CpfTest {
    @Test
    void confereDvSomenteQuandoSolicitado() {
        assertTrue(CpfValidator.isValid("280.012.389-38", NUMERIC));
        assertTrue(CpfValidator.isValid("28001238939"));
        assertTrue(CpfValidator.isValid("280.012.389-39", FORMAT_ONLY));
        assertFalse(CpfValidator.isValid("28001238939", NUMERIC));
        assertFalse(CpfValidator.isValid("28001238948", NUMERIC));
        assertFalse(CpfValidator.isValid(null, NUMERIC));
        assertTrue(Cpf.parse("280.012.389-38", NUMERIC).hasValidCheckDigits());
        assertFalse(Cpf.parse("280.012.389-39").hasValidCheckDigits());
        assertEquals("39", Cpf.parse("28001238939").getCheckDigits());
        assertEquals("CPF inválido.", assertThrows(IllegalArgumentException.class,
                () -> Cpf.parse("280.012.389-39", NUMERIC)).getMessage());
        assertThrows(IllegalArgumentException.class, () -> Cpf.parse("28001238938", null));
        assertThrows(IllegalArgumentException.class, () -> CpfValidator.isValid(null, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "2800123893", "280012389380", " 28001238938", "28001238938 ",
        "280.012389-38", "280.012.38938", "2.8001238938",
        "28A01238938", "２８００１２３８９３８"
    })
    void recusaFormatoInvalidoEmAmbosOsModos(String entrada) {
        assertFalse(CpfValidator.isValid(entrada));
        assertFalse(CpfValidator.isValid(entrada, NUMERIC));
        assertEquals("CPF inválido.", assertThrows(IllegalArgumentException.class,
                () -> Cpf.parse(entrada)).getMessage());
        assertThrows(IllegalArgumentException.class, () -> CpfFormatter.format(entrada));
        assertThrows(IllegalArgumentException.class, () -> CpfFormatter.unformat(entrada));
    }

    @Test
    void preservaZerosMascaraEIdentidadeCanonica() {
        Cpf valor = Cpf.parse("012.345.678-90", NUMERIC);
        assertEquals("01234567890", valor.getCanonicalValue());
        assertEquals("012345678", valor.getBase());
        assertEquals("90", valor.getCheckDigits());
        assertEquals("012.345.678-90", valor.applyMask());
        assertEquals("***.***.***-**", valor.toString());
        Cpf canonico = Cpf.parse("01234567890");
        assertEquals(canonico, valor);
        assertEquals(canonico.hashCode(), valor.hashCode());
        assertNotEquals(valor, Cpf.parse("01234567891"));
        assertNotEquals(valor, "01234567890");
        assertFalse(valor.equals(null));
        assertEquals("280.012.389-39", CpfFormatter.format("28001238939"));
        assertEquals("28001238939", CpfFormatter.unformat("280.012.389-39"));
        assertEquals("01234567890", CpfFormatter.unformat(CpfFormatter.format("01234567890")));
    }

    @Test
    void aplicaSomenteARegraMatematicaParaSequenciasRepetidas() {
        assertTrue(CpfValidator.isValid("00000000000"));
        assertTrue(CpfValidator.isValid("00000000000", NUMERIC));
        assertTrue(CpfValidator.isValid("111.111.111-11", NUMERIC));
        assertTrue(CpfValidator.isValid("00000000604", NUMERIC));
        assertFalse(CpfValidator.isValid("00000000001", NUMERIC));
    }
    @Test
    void modoInternoTambemConfereApresentacao() {
        assertNull(FORMAT_ONLY.validatedCanonical("qualquer valor"));
        assertNull(FORMAT_ONLY.validatedCanonical(null));
        assertNull(NUMERIC.validatedCanonical("qualquer valor"));
        assertNull(FORMAT_ONLY.validatedCanonical("280.012.38939"));
        assertEquals("28001238939", FORMAT_ONLY.validatedCanonical("280.012.389-39"));
    }

}
