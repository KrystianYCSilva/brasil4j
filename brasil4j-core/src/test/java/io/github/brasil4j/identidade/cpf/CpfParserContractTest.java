package io.github.brasil4j.identidade.cpf;

import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParserContractTest;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CpfParserContractTest {
    @Nested
    class Padrao extends DocumentParserContractTest<Cpf> {
        @Override
        protected DocumentParser<Cpf> parser() {
            return Cpf.parser();
        }

        @Override
        protected String validInput() {
            return "280.012.389-39";
        }

        @Override
        protected String errorMessage() {
            return "CPF inválido.";
        }
    }

    @Nested
    class Numerico extends DocumentParserContractTest<Cpf> {
        @Override
        protected DocumentParser<Cpf> parser() {
            return Cpf.parser(CpfValidationMode.NUMERIC);
        }

        @Override
        protected String validInput() {
            return "28001238938";
        }

        @Override
        protected String errorMessage() {
            return "CPF inválido.";
        }
    }

    @Test
    void distingueFormatoDeDigitoVerificador() {
        assertTrue(Cpf.parser(CpfValidationMode.FORMAT_ONLY).isValid("28001238939"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                Cpf.parser(CpfValidationMode.NUMERIC).parse("28001238939").getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                Cpf.parser(CpfValidationMode.NUMERIC).parse("280.012.38939").getRejection());
        assertEquals(Cpf.parse("01234567890"), Cpf.parser().parseOrThrow("012.345.678-90"));
    }

    @Test
    void modoNuloContinuaSendoErroDeArgumento() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Cpf.parser(null));
    }
}
