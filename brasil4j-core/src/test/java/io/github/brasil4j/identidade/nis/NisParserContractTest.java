package io.github.brasil4j.identidade.nis;

import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParserContractTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NisParserContractTest {
    @Nested
    class Estrutural extends DocumentParserContractTest<Nis> {
        @Override
        protected DocumentParser<Nis> parser() {
            return Nis.parser();
        }

        @Override
        protected String validInput() {
            return "01234567898";
        }

        @Override
        protected String errorMessage() {
            return "NIS inválido.";
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"01234567898", "00000000063", "00000000050", "00000000281"})
    void padraoContinuaSoEstruturalComDvDivergente(String vetor) {
        assertTrue(Nis.parser().isValid(vetor));
    }

    @Test
    void mascaraContinuaRejeitadaNaEntrada() {
        assertTrue(!Nis.parser().isValid("012.34567.89-8"));
    }
}
