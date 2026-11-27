package io.github.brasil4j.identidade;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/** Fixa, antes da Spec 010, o comportamento observável que a refatoração precisa preservar. */
class CharacterizationTest {
    @Test
    void representacoesTextuaisPadrao() {
        assertEquals("***.***.***-**", Cpf.parse("01234567890").toString());
        assertEquals("[NIS oculto]", Nis.parse("01234567898").toString());
        assertEquals("12ABC34501DE35", Cnpj.parse("12ABC34501DE35").toString());
    }

    @Test
    void mensagensETiposExatosDeErro() {
        assertEquals("CPF inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> Cpf.parse("123")).getMessage());
        assertEquals("CNPJ inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> Cnpj.parse("123")).getMessage());
        assertEquals("NIS inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> Nis.parse("123")).getMessage());
        assertThrowsExactly(IllegalArgumentException.class, () -> Cpf.parse("01234567890", null));
        assertThrowsExactly(IllegalArgumentException.class, () -> Cnpj.parse("12ABC34501DE35", null));
    }

    @ParameterizedTest
    @EnumSource(value = CnpjValidationMode.class, names = {"ALPHANUMERIC", "NUMERIC"})
    void bloqueioDeQuatorzeZerosNosModosComDv(CnpjValidationMode mode) {
        assertThrowsExactly(IllegalArgumentException.class, () -> Cnpj.parse("00000000000000", mode));
        assertEquals("00000000000000",
                Cnpj.parse("00000000000000", CnpjValidationMode.FORMAT_ONLY).getCanonicalValue());
    }

    @Test
    void mascaraParcialEMinusculasSaoRejeitadas() {
        assertThrowsExactly(IllegalArgumentException.class,
                () -> Cpf.parse("280.012.38939", CpfValidationMode.FORMAT_ONLY));
        assertThrowsExactly(IllegalArgumentException.class,
                () -> Cnpj.parse("12.abc.345/01de-35", CnpjValidationMode.FORMAT_ONLY));
        assertThrowsExactly(IllegalArgumentException.class, () -> Nis.parse("012.34567.89-8"));
    }
}
