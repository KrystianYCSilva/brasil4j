package io.github.brasil4j.mascara;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMaskTest {
    private static final DocumentMask CPF = DocumentMask.of("999.999.999-99");
    private static final DocumentMask CNPJ = DocumentMask.of("XX.XXX.XXX/XXXX-99");
    private static final DocumentMask PLACA = DocumentMask.of("AAA-9X99");

    @Test
    void removeAApresentacaoCanonicaOuMascaradaCompleta() {
        assertEquals(11, CPF.canonicalLength());
        assertEquals(Optional.of("01234567890"), CPF.unmask("01234567890"));
        assertEquals(Optional.of("01234567890"), CPF.unmask("012.345.678-90"));
        assertEquals(Optional.of("12ABC34501DE35"), CNPJ.unmask("12.ABC.345/01DE-35"));
        assertEquals(Optional.of("12ABC34501DE35"), CNPJ.unmask("12ABC34501DE35"));
        assertEquals(Optional.of("ABC1D23"), PLACA.unmask("ABC-1D23"));
        assertEquals(Optional.of("ABC1234"), PLACA.unmask("ABC1234"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "012.345.67890", "012.345.678-9", "012.345.678-900", " 01234567890", "0123456789A",
        "012,345,678-90", "０１２３４５６７８９０"
    })
    void recusaApresentacaoParcialOuForaDosSlots(String entrada) {
        assertEquals(Optional.empty(), CPF.unmask(entrada));
    }

    @Test
    void respeitaAsClassesDeSlot() {
        assertEquals(Optional.empty(), CNPJ.unmask("12.abc.345/01de-35"));
        assertEquals(Optional.empty(), CNPJ.unmask("12.ABC.345/01DE-A5"));
        assertEquals(Optional.empty(), PLACA.unmask("1BC-1D23"));
        assertEquals(Optional.empty(), PLACA.unmask("ABC-1D2Z"));
    }

    @Test
    void aplicaAMascaraCompleta() {
        assertEquals("012.345.678-90", CPF.apply("01234567890"));
        assertEquals("12.ABC.345/01DE-35", CNPJ.apply("12ABC34501DE35"));
        assertThrows(IllegalArgumentException.class, () -> CPF.apply("0123456789"));
        assertThrows(IllegalArgumentException.class, () -> CPF.apply("012.345.678-90"));
        assertThrows(NullPointerException.class, () -> CPF.apply(null));
    }

    @Test
    void aplicaAMascaraDeSegurancaPreservandoLiterais() {
        assertEquals("***.345.678-**", CPF.applySecure("01234567890", 3, 2, '*'));
        assertEquals("###.345.678-##", CPF.applySecure("01234567890", 3, 2, '#'));
        assertEquals("**.ABC.345/01DE-**", CNPJ.applySecure("12ABC34501DE35", 2, 2, '*'));
        assertEquals("012.345.678-90", CPF.applySecure("01234567890", 0, 0, '*'));
        assertEquals("***.***.***-**", CPF.applySecure("01234567890", 6, 5, '*'));
        assertEquals("***.***.***-**", CPF.applySecure("01234567890", 20, 0, '*'));
    }

    @Test
    void recusaParametrosDeSegurancaInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> CPF.applySecure("01234567890", -1, 2, '*'));
        assertThrows(IllegalArgumentException.class, () -> CPF.applySecure("01234567890", 3, -1, '*'));
        assertThrows(IllegalArgumentException.class, () -> CPF.applySecure("01234567890", 3, 2, '0'));
        assertThrows(IllegalArgumentException.class, () -> CPF.applySecure("01234567890", 3, 2, 'X'));
        assertThrows(IllegalArgumentException.class, () -> CPF.applySecure("0123456789", 3, 2, '*'));
    }

    @Test
    void validaOPadrao() {
        assertThrows(NullPointerException.class, () -> DocumentMask.of(null));
        assertThrows(IllegalArgumentException.class, () -> DocumentMask.of(""));
        assertThrows(IllegalArgumentException.class, () -> DocumentMask.of("..-/"));
        assertEquals("999.999.999-99", CPF.toString());
        assertEquals(CPF, DocumentMask.of("999.999.999-99"));
        assertEquals(CPF.hashCode(), DocumentMask.of("999.999.999-99").hashCode());
        assertNotEquals(CPF, CNPJ);
        assertNotEquals(CPF, "999.999.999-99");
    }

    @Test
    void mascaraSemLiteraisAceitaSomenteOCanonico() {
        DocumentMask nis = DocumentMask.of("99999999999");
        assertEquals(Optional.of("01234567898"), nis.unmask("01234567898"));
        assertEquals(Optional.empty(), nis.unmask("012.34567.89-8"));
        assertEquals("***3456789*", nis.applySecure("01234567898", 3, 1, '*'));
    }
}
