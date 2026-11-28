package io.github.brasil4j.trabalhista.ctps;

import io.github.brasil4j.geografia.Uf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Field.*;
import static io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Status.*;
import static io.github.brasil4j.trabalhista.ctps.CtpsOrigin.UNDETERMINED;
import static io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5;
import static org.junit.jupiter.api.Assertions.*;

class CtpsFacetsTest {
    // Enumeração da faceta ufCtps no XSD do S-2200 v2.5.
    private static final Set<String> UF_SIGLAS_XSD = new HashSet<>(Arrays.asList(
            "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS", "MT", "PA",
            "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"));

    @ParameterizedTest
    @CsvSource({"00,0", "0000123,00004", "ABCDEFGHIJK,12345", "a-0,X", "'  ',' '",
            "A\uD83D\uDE00,X", "12,\uD83D\uDE00", "ABCDEFGHIJ\uD83D\uDE00,ABCD\uD83D\uDE00", "A\u0301,1"})
    void recuperaTextosSemNormalizacao(String number, String series) {
        CtpsCheckResult result = check(number, series, "PR");
        assertEquals(CONFORMING, result.getStatus());
        assertTrue(result.getAffectedFields().isEmpty());
        CtpsRecord record = result.getRecord().get();
        assertEquals(number, record.getNumber());
        assertEquals(series, record.getSeries());
        assertSame(Uf.PR, record.getUf());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1", "123456789012", "\uD83D\uDE00", "ABCDEFGHIJK\uD83D\uDE00"})
    void rejeitaNumeroForaDosLimites(String number) {
        assertInvalid(check(number, "1", "PR"), NUMBER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123456", "ABCDE\uD83D\uDE00"})
    void rejeitaSerieForaDosLimites(String series) {
        assertInvalid(check("12", series, "PR"), SERIES);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "XX", "pr", " PR", "PR ", "Paran\u00E1"})
    void rejeitaUfInexata(String uf) {
        assertInvalid(check("12", "1", uf), UF);
    }

    @ParameterizedTest
    @MethodSource("siglasDoXsd")
    void aceitaSiglaExataDeCadaUf(String sigla) {
        CtpsCheckResult result = check("12", "1", sigla);
        assertEquals(CONFORMING, result.getStatus());
        assertEquals(sigla, result.getRecord().get().getUf().getSigla());
    }

    @Test
    void dominioDeUfCorrespondeAoXsd() {
        Set<String> siglas = Arrays.stream(Uf.values()).map(Uf::getSigla).collect(Collectors.toSet());
        assertEquals(UF_SIGLAS_XSD, siglas);
    }

    static Stream<String> siglasDoXsd() {
        return UF_SIGLAS_XSD.stream().sorted();
    }

    @Test
    void agregaCamposInvalidosNaOrdemDoLeiaute() {
        CtpsCheckResult result = check("1", "", "pr");
        assertEquals(OUT_OF_PROFILE, result.getStatus());
        assertEquals(Arrays.asList(NUMBER, SERIES, UF), new ArrayList<>(result.getAffectedFields()));
        assertThrows(UnsupportedOperationException.class, () -> result.getAffectedFields().clear());
        assertFalse(result.getRecord().isPresent());
    }

    @Test
    void registroSoExisteQuandoOGrupoEstaConforme() {
        assertFalse(ESOCIAL_S2200_V2_5.check(null).getRecord().isPresent());
        assertFalse(check("12", null, "PR").getRecord().isPresent());
    }

    private static CtpsCheckResult check(String number, String series, String uf) {
        return ESOCIAL_S2200_V2_5.check(CtpsGroup.of(number, series, uf, UNDETERMINED, false));
    }

    private static void assertInvalid(CtpsCheckResult result, CtpsCheckResult.Field field) {
        assertEquals(OUT_OF_PROFILE, result.getStatus());
        assertEquals(Collections.singleton(field), result.getAffectedFields());
        assertFalse(result.getRecord().isPresent());
    }
}
