package io.github.brasil4j.identidade.nis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NisTest {
    @Test
    void aplicaApresentacaoDoConsumidorSemMudarTextoPadrao() {
        Nis nis = Nis.parse("00000000019");
        assertEquals("inscrição:00000000019", NisFormatter.format(nis,
                valor -> "inscrição:" + valor.getCanonicalValue()));
        assertEquals("[NIS oculto]", nis.toString());
    }

    @Test
    void distingueNulosEExcecaoDaFormatacao() {
        Nis nis = Nis.parse("00000000019");
        assertThrowsExactly(NullPointerException.class, () -> NisFormatter.format(null, Nis::toString));
        assertThrowsExactly(NullPointerException.class, () -> NisFormatter.format(nis, null));
        assertThrowsExactly(NullPointerException.class, () -> NisFormatter.format(nis, valor -> null));
        IllegalStateException falha = new IllegalStateException("formatação externa");
        assertSame(falha, assertThrowsExactly(IllegalStateException.class,
                () -> NisFormatter.format(nis, valor -> { throw falha; })));
    }

    @Test
    void rejeitaEntradaNulaExplicitamente() {
        assertFalse(NisValidator.isValid(null));
        assertThrowsExactly(IllegalArgumentException.class, () -> Nis.parse(null));
    }

    @Test
    void aplicaPoliticaSomenteAInscricoesEstruturais() {
        List<Nis> conferidos = new ArrayList<>();
        Predicate<Nis> politica = nis -> {
            conferidos.add(nis);
            return nis.getCanonicalValue().endsWith("9");
        };

        assertFalse(NisValidator.isValid(null, politica));
        assertFalse(NisValidator.isValid("012.34567.89-7", politica));
        assertTrue(NisValidator.isValid("00000000019", politica));
        assertFalse(NisValidator.isValid("01234567897", politica));
        assertEquals(Arrays.asList(Nis.parse("00000000019"), Nis.parse("01234567897")), conferidos);
    }

    @Test
    void distingueFalhaDeConfiguracaoEExcecaoDaPolitica() {
        assertThrowsExactly(NullPointerException.class, () -> NisValidator.isValid(null, null));
        IllegalStateException falha = new IllegalStateException("regra externa");
        assertSame(falha, assertThrowsExactly(IllegalStateException.class,
                () -> NisValidator.isValid("00000000019", nis -> { throw falha; })));
    }

    @Test
    void ocultaQualquerInscricaoNaRepresentacaoPadrao() {
        assertEquals("[NIS oculto]", Nis.parse("00000000019").toString());
        assertEquals("[NIS oculto]", Nis.parse("01234567897").toString());
    }

    @Test
    void comparaInscricoesPeloTextoCanônico() {
        Nis deNisTrab = Nis.parse("00000000019");
        Nis dePisPasep = Nis.parse("00000000019");

        assertEquals(deNisTrab, dePisPasep);
        assertEquals(deNisTrab.hashCode(), dePisPasep.hashCode());
        assertNotEquals(deNisTrab, Nis.parse("01234567897"));
        assertNotEquals(deNisTrab, "00000000019");
        assertNotEquals(deNisTrab, new Object());
        assertFalse(deNisTrab.equals(null));
    }

    @ParameterizedTest(name = "caso {0}")
    @CsvFileSource(resources = "/nis/nis-c1.csv", nullValues = "<null>",
            ignoreLeadingAndTrailingWhitespace = false)
    void confereApresentacaoLiteral(String caso, String entrada, boolean aceita) {
        if ("12".equals(caso)) {
            assertEquals("\uFF10\uFF11\uFF12\uFF13\uFF14\uFF15\uFF16\uFF17\uFF18\uFF19\uFF17", entrada);
        }
        assertEquals(aceita, NisValidator.isValid(entrada));
        if (aceita) {
            assertEquals(entrada, Nis.parse(entrada).getCanonicalValue());
        } else {
            IllegalArgumentException erro = assertThrowsExactly(IllegalArgumentException.class,
                    () -> Nis.parse(entrada));
            assertEquals("NIS inválido.", erro.getMessage());
            assertNull(erro.getCause());
        }
    }
}
