package io.github.brasil4j.kotlin.geografia

import io.github.brasil4j.geografia.CodigoMunicipio as JavaCodigoMunicipio
import io.github.brasil4j.geografia.Uf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class CodigoMunicipioJarIT {
    @ParameterizedTest
    @CsvSource("3550308,SP", "4115200,PR", "4113700,PR", "3509502,SP")
    fun preservaOsVetoresDosConsumidores(entrada: String, uf: Uf) {
        assertTrue(CodigoMunicipio::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        assertTrue(JavaCodigoMunicipio::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        val codigo = entrada.toCodigoMunicipio()

        assertEquals(entrada, codigo.canonicalValue)
        assertEquals(uf, codigo.uf)
        assertTrue(codigo.matchesUf(uf))
        assertEquals(JavaCodigoMunicipio.parse(entrada), codigo.toJava())
        assertEquals(codigo, java.lang.Long.parseLong(codigo.canonicalValue).toString().toCodigoMunicipio())
    }

    @ParameterizedTest
    @ValueSource(strings = ["+4115200", "04115200", "４１１５２００"])
    fun separaApresentacaoDaConversaoNumerica(entrada: String) {
        assertThrows(IllegalArgumentException::class.java) { entrada.toCodigoMunicipio() }
        assertEquals("4115200", java.lang.Long.parseLong(entrada).toString().toCodigoMunicipio().canonicalValue)
    }

    @Test
    fun preservaZerosEConfereSomenteOPrefixo() {
        val desconhecido = "0012345".toCodigoMunicipio()
        val representacao: Any = desconhecido
        assertEquals("0012345", representacao.toString())
        assertEquals("12345", java.lang.Long.parseLong(desconhecido.canonicalValue).toString())
        assertNull(desconhecido.uf)
        assertFalse(desconhecido.matchesUf(null))
        assertThrows(IllegalArgumentException::class.java) { "12345".toCodigoMunicipio() }

        val sintetico = "3599999".toCodigoMunicipio()
        assertEquals(Uf.SP, sintetico.uf)
        assertTrue(sintetico.matchesUf(Uf.SP))
        assertFalse(sintetico.matchesUf(Uf.RJ))
    }

    @Test
    fun expoeMetadadosDaUf() {
        val uf = Uf.fromName(" goias ").get()

        assertEquals(Uf.GO, uf)
        assertEquals("Goiás", uf.nome)
        assertEquals("GO", uf.sigla)
        assertEquals("Goiânia", uf.capital)
        assertEquals("52", uf.ibgeCode)
        assertEquals(listOf("61", "62", "64"), uf.ddds)
    }
}
