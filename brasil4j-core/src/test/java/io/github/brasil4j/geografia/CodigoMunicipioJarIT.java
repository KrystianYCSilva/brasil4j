package io.github.brasil4j.geografia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodigoMunicipioJarIT {
    @ParameterizedTest
    @CsvSource({"3550308,SP", "4115200,PR", "4113700,PR", "3509502,SP"})
    void preservaOsVetoresDosConsumidores(String entrada, Uf uf) {
        assertTrue(CodigoMunicipio.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
        assertTrue(Uf.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
        CodigoMunicipio codigo = CodigoMunicipio.parse(entrada);

        assertEquals(entrada, codigo.getCanonicalValue());
        assertEquals(uf, codigo.getUf().get());
        assertTrue(codigo.matchesUf(uf));
        assertEquals(codigo, CodigoMunicipio.parse(String.valueOf(Long.parseLong(codigo.getCanonicalValue()))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+4115200", "04115200", "４１１５２００"})
    void separaApresentacaoDaConversaoNumerica(String entrada) {
        assertThrows(IllegalArgumentException.class, () -> CodigoMunicipio.parse(entrada));
        assertEquals("4115200", CodigoMunicipio.parse(String.valueOf(Long.parseLong(entrada))).getCanonicalValue());
    }

    @Test
    void preservaZerosEConfereSomenteOPrefixo() {
        CodigoMunicipio desconhecido = CodigoMunicipio.parse("0012345");
        assertEquals("0012345", desconhecido.toString());
        assertEquals("12345", String.valueOf(Long.parseLong(desconhecido.getCanonicalValue())));
        assertFalse(desconhecido.getUf().isPresent());
        assertFalse(desconhecido.matchesUf(null));
        assertThrows(IllegalArgumentException.class, () -> CodigoMunicipio.parse("12345"));

        CodigoMunicipio sintetico = CodigoMunicipio.parse("3599999");
        assertEquals(Uf.SP, sintetico.getUf().get());
        assertTrue(sintetico.matchesUf(Uf.SP));
        assertFalse(sintetico.matchesUf(Uf.RJ));
    }

    @Test
    void expoeMetadadosDaUf() {
        Uf uf = Uf.fromName(" goias ").get();

        assertEquals(Uf.GO, uf);
        assertEquals("Goiás", uf.getNome());
        assertEquals("GO", uf.getSigla());
        assertEquals("Goiânia", uf.getCapital());
        assertEquals("52", uf.getIbgeCode());
        assertEquals(java.util.Arrays.asList("61", "62", "64"), uf.getDdds());
    }
}
