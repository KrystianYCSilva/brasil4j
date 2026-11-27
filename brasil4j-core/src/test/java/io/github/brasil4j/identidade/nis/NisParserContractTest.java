package io.github.brasil4j.identidade.nis;

import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParserContractTest;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NisParserContractTest {
    private static final Rejection DV = Rejection.of(RejectionReason.INVALID_CHECK_DIGITS);
    private static final Rejection REPETIDOS = Rejection.of(RejectionReason.INVALID_CHECK_DIGITS, "REPEATED_DIGITS");

    abstract static class NisContrato extends DocumentParserContractTest<Nis> {
        @Override
        protected String validInput() {
            return "00000000019";
        }

        @Override
        protected String errorMessage() {
            return "NIS inválido.";
        }
    }

    @Nested
    class Estrutural extends NisContrato {
        @Override
        protected DocumentParser<Nis> parser() {
            return Nis.parser();
        }
    }

    @Nested
    class CaixaFgts extends NisContrato {
        @Override
        protected DocumentParser<Nis> parser() {
            return Nis.parser(NisValidationMode.CAIXA_FGTS_V016);
        }
    }

    @Nested
    class Legado extends NisContrato {
        @Override
        protected DocumentParser<Nis> parser() {
            return Nis.parser(NisValidationMode.LEGACY_MOD11);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"01234567898", "00000000063", "00000000050", "00000000281"})
    void padraoContinuaSoEstruturalComDvDivergente(String vetor) {
        assertTrue(Nis.parser().isValid(vetor));
        assertTrue(Nis.parser(NisValidationMode.FORMAT_ONLY).isValid(vetor));
        assertEquals(Nis.parse(vetor), Nis.parser().parseOrThrow(vetor));
    }

    @Test
    void mascaraContinuaRejeitadaNaEntrada() {
        for (NisValidationMode mode : NisValidationMode.values()) {
            assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                    Nis.parser(mode).parse("012.34567.89-7").getRejection(), mode.name());
        }
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "07, 00000000000, ACEITA,    REPETIDOS",
        "09, 01234567898, DV,        DV",
        "15, 00000000019, ACEITA,    ACEITA",
        "16, 00000000063, DV,        DV",
        "17, 00000000060, DV,        ACEITA",
        "18, 00000000050, DV,        DV",
        "19, 00000000280, ACEITA,    ACEITA",
        "20, 00000000281, DV,        DV",
        "21, 11111111116, ACEITA,    ACEITA",
    })
    void validadoresOpcionaisNosVetoresC1(String caso, String nis, String caixa, String legado) {
        assertEquals(esperado(caixa), Nis.parser(NisValidationMode.CAIXA_FGTS_V016).parse(nis).getRejection());
        assertEquals(esperado(legado), Nis.parser(NisValidationMode.LEGACY_MOD11).parse(nis).getRejection());
    }

    @Test
    void guardaSipreVOpcionalEncadeadaAoValidadorDaCaixa() {
        DocumentParser<Nis> caixaComGuarda = Nis.parser(NisValidationMode.CAIXA_FGTS_V016)
                .withRule(NisRules.repeatedDigits());
        assertEquals(Optional.of(REPETIDOS), caixaComGuarda.parse("00000000000").getRejection());
        assertTrue(caixaComGuarda.isValid("11111111116"));
        assertTrue(caixaComGuarda.isValid("00000000019"));
        assertEquals(Optional.of(REPETIDOS), Nis.parser().withRule(NisRules.repeatedDigits())
                .parse("22222222222").getRejection());
    }

    @Test
    void algoritmoDoConsumidorPorRegra() {
        DocumentParser<Nis> composto = Nis.parser().withRule(NisRules.checkDigits(Modulus11.pisLegacyMod11()));
        assertTrue(composto.isValid("00000000060"));
        assertEquals(Optional.of(DV), composto.parse("00000000063").getRejection());
        DocumentParser<Nis> foraDoDominio = Nis.parser().withRule(
                NisRules.checkDigits(Modulus11.builder().weightsFromLeft(1, 2, 3).build()));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                foraDoDominio.parse("00000000019").getRejection());
        assertThrows(NullPointerException.class, () -> NisRules.checkDigits(null));
    }

    @Test
    void regraDoConsumidorComCodigoNaoRodaAposRejeicaoNativa() {
        DocumentParser<Nis> importacao = Nis.parser()
                .withPolicy(nis -> nis.getCanonicalValue().startsWith("00"), "PREFIXO_IMPORTACAO");
        assertTrue(importacao.isValid("00000000019"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.POLICY, "PREFIXO_IMPORTACAO")),
                importacao.parse("01234567898").getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                importacao.parse("012.34567.89-7").getRejection());
    }

    @Test
    void modoNuloEhErroDeArgumento() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Nis.parser(null));
    }

    private static Optional<Rejection> esperado(String desfecho) {
        switch (desfecho) {
            case "ACEITA":
                return Optional.empty();
            case "DV":
                return Optional.of(DV);
            default:
                return Optional.of(REPETIDOS);
        }
    }
}
