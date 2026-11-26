package io.github.brasil4j.geografia;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodigoMunicipioTest {
    @ParameterizedTest
    @ValueSource(strings = {"3550308", "4115200", "0012345"})
    void interpretaSeteDigitosAscii(String codigo) {
        assertNotNull(CodigoMunicipio.parse(codigo));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"355030", "03550308", "+3550308", "-3550308", " 3550308 ",
            "35.503-08", "３５５０３０８"})
    void rejeitaApresentacaoInvalidaSemExporAEntrada(String entrada) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> CodigoMunicipio.parse(entrada));
        if (entrada != null && !entrada.isEmpty()) {
            assertFalse(erro.getMessage().contains(entrada));
        }
    }

    @ParameterizedTest
    @CsvSource({"3550308,35", "4115200,41", "4113700,41", "3509502,35", "0012345,00"})
    void preservaTextoEExtraiPrefixo(String valor, String prefixo) {
        CodigoMunicipio codigo = CodigoMunicipio.parse(valor);
        assertEquals(valor, codigo.getCanonicalValue());
        assertEquals(prefixo, codigo.getUfCode());
    }

    @Test
    void defineIdentidadeETextoPelosSeteDigitos() {
        CodigoMunicipio codigo = CodigoMunicipio.parse("0012345");
        CodigoMunicipio equivalente = CodigoMunicipio.parse("0012345");

        assertEquals(codigo, codigo);
        assertEquals(codigo, equivalente);
        assertEquals(codigo.hashCode(), equivalente.hashCode());
        assertNotEquals(codigo, CodigoMunicipio.parse("3550308"));
        assertNotEquals(codigo, "0012345");
        assertNotEquals(codigo, null);
        assertEquals("0012345", codigo.toString());
    }

    @Test
    void resolveSomenteAUfSemConsultarCadastroOuCalcularDv() {
        CodigoMunicipio codigo = CodigoMunicipio.parse("3599999");

        assertEquals("3599999", codigo.getCanonicalValue());
        assertEquals(Optional.of(Uf.SP), codigo.getUf());
        assertTrue(codigo.matchesUf(Uf.SP));
        assertFalse(codigo.matchesUf(Uf.RJ));
        assertFalse(codigo.matchesUf(null));
    }

    @Test
    void prefixoDesconhecidoNaoCoincideNemComUfNula() {
        CodigoMunicipio codigo = CodigoMunicipio.parse("0012345");

        assertEquals(Optional.empty(), codigo.getUf());
        assertFalse(codigo.matchesUf(Uf.SP));
        assertFalse(codigo.matchesUf(null));
    }
}
