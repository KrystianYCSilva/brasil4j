package io.github.brasil4j.identidade;

import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Documento do consumidor montado só com blocos públicos da biblioteca, sem alterá-la (US1).
 * A matrícula é sintética: nove dígitos, máscara {@code 999.999-999} e um DV por módulo 11.
 */
class CustomDocumentExampleTest {
    static final class Matricula extends AbstractDocument implements MaskedDocument, SecurityMaskedDocument {
        private static final DocumentMask MASK = DocumentMask.of("999.999-999");
        static final DocumentParser<Matricula> PARSER = DocumentParsers.builder(Matricula::new)
                .mask(MASK)
                .checkDigits(Modulus11.builder().weightsFromRight(2, 9).digits(1).build())
                .errorMessage("Matrícula inválida.")
                .build();

        private Matricula(String canonical) {
            super(canonical);
        }

        @Override
        public String applyMask() {
            return MASK.apply(getCanonicalValue());
        }

        @Override
        public String applySecurityMask(char maskChar) {
            return MASK.applySecure(getCanonicalValue(), 3, 1, maskChar);
        }
    }

    @Nested
    class Contrato extends DocumentParserContractTest<Matricula> {
        @Override
        protected DocumentParser<Matricula> parser() {
            return Matricula.PARSER;
        }

        @Override
        protected String validInput() {
            return "123.456-789";
        }

        @Override
        protected String errorMessage() {
            return "Matrícula inválida.";
        }
    }

    @Test
    void aceitaCanonicoEMascaradoComOMesmoValor() {
        Matricula canonica = Matricula.PARSER.parseOrThrow("123456789");
        Matricula mascarada = Matricula.PARSER.parseOrThrow("123.456-789");
        assertEquals(canonica, mascarada);
        assertEquals("123456789", mascarada.getCanonicalValue());
        assertEquals("123.456-789", canonica.applyMask());
    }

    @Test
    void distingueFormatoDeDigitoVerificadorSemExcecao() {
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                Matricula.PARSER.parse("123.456789").getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                Matricula.PARSER.parse("123.456-780").getRejection());
    }

    @Test
    void herdaIgualdadeEOcultacaoDaBibliotecaEOferecePresentacoes() {
        Matricula matricula = Matricula.PARSER.parseOrThrow("123456789");
        assertEquals("[documento oculto]", matricula.toString());
        assertEquals("***.456-78*", matricula.applySecurityMask());
        assertEquals("###.456-78#", matricula.applySecurityMask('#'));
        assertEquals("***.456-78*", DocumentFormatter.<Matricula>secure().apply(matricula));
        assertEquals("123.456-789", DocumentFormatter.<Matricula>masked().apply(matricula));
    }
}
