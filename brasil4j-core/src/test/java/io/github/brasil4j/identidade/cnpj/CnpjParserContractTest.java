package io.github.brasil4j.identidade.cnpj;

import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParserContractTest;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CnpjParserContractTest {
    @Nested
    class Padrao extends DocumentParserContractTest<Cnpj> {
        @Override
        protected DocumentParser<Cnpj> parser() {
            return Cnpj.parser();
        }

        @Override
        protected String validInput() {
            return "12.ABC.345/01DE-35";
        }

        @Override
        protected String errorMessage() {
            return "CNPJ inválido.";
        }
    }

    @Nested
    class Numerico extends DocumentParserContractTest<Cnpj> {
        @Override
        protected DocumentParser<Cnpj> parser() {
            return Cnpj.parser(CnpjValidationMode.NUMERIC);
        }

        @Override
        protected String validInput() {
            return "00.000.000/0001-91";
        }

        @Override
        protected String errorMessage() {
            return "CNPJ inválido.";
        }
    }

    @Nested
    class Formato extends DocumentParserContractTest<Cnpj> {
        @Override
        protected DocumentParser<Cnpj> parser() {
            return Cnpj.parser(CnpjValidationMode.FORMAT_ONLY);
        }

        @Override
        protected String validInput() {
            return "12ABC34501DE36";
        }

        @Override
        protected String errorMessage() {
            return "CNPJ inválido.";
        }
    }

    @Test
    void classificaCadaModo() {
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                Cnpj.parser(CnpjValidationMode.ALPHANUMERIC).parse("12ABC34501DE36").getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.UNSUPPORTED_BASE)),
                Cnpj.parser(CnpjValidationMode.NUMERIC).parse("12ABC34501DE35").getRejection());
        assertEquals("12.ABC.345/01DE-36",
                Cnpj.parser(CnpjValidationMode.FORMAT_ONLY).parse("12ABC34501DE36").map(Cnpj::applyMask).orElseThrow());
    }

    @ParameterizedTest
    @EnumSource(value = CnpjValidationMode.class, names = {"ALPHANUMERIC", "NUMERIC"})
    void preservaOBloqueioDeQuatorzeZeros(CnpjValidationMode mode) {
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                Cnpj.parser(mode).parse("00000000000000").getRejection());
        assertTrue(Cnpj.parser(CnpjValidationMode.FORMAT_ONLY).isValid("00000000000000"));
    }

    @Test
    void modoNuloContinuaSendoErroDeArgumento() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Cnpj.parser(null));
    }
}
