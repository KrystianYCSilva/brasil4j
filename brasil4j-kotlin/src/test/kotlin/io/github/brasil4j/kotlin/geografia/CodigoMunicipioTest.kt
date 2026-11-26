package io.github.brasil4j.kotlin.geografia

import io.github.brasil4j.geografia.CodigoMunicipio as JavaCodigoMunicipio
import io.github.brasil4j.geografia.Uf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource

class CodigoMunicipioTest {
    @Test
    fun interpretaComOContratoJava() {
        val codigo = CodigoMunicipio("4115200")

        assertEquals("4115200", codigo.canonicalValue)
        assertEquals("41", codigo.ufCode)
        assertEquals(Uf.PR, codigo.uf)
        assertEquals(codigo, "4115200".toCodigoMunicipio())
        assertTrue(codigo.matchesUf(Uf.PR))
        assertFalse(codigo.matchesUf(Uf.SP))
        assertFalse(codigo.matchesUf(null))
        assertEquals(JavaCodigoMunicipio.parse("4115200"), codigo.toJava())
    }

    @Test
    fun prefixoDesconhecidoNaoCoincideComUfNula() {
        val codigo = "0012345".toCodigoMunicipio()

        assertEquals("0012345", codigo.canonicalValue)
        assertEquals("00", codigo.ufCode)
        assertNull(codigo.uf)
        assertFalse(codigo.matchesUf(null))
        assertFalse(codigo.matchesUf(Uf.SP))
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["355030", "03550308", "+3550308", "-3550308", " 3550308 ", "35.503-08", "３５５０３０８"])
    fun rejeitaApresentacaoInvalidaSemExporAEntrada(entrada: String) {
        val peloConstrutor = assertThrows(IllegalArgumentException::class.java) { CodigoMunicipio(entrada) }
        val pelaExtensao = assertThrows(IllegalArgumentException::class.java) { entrada.toCodigoMunicipio() }
        if (entrada.isNotEmpty()) {
            assertFalse(peloConstrutor.message.orEmpty().contains(entrada))
            assertFalse(pelaExtensao.message.orEmpty().contains(entrada))
        }
    }

    @Test
    fun representaEComparaPeloCodigoCanonico() {
        val codigo = "3550308".toCodigoMunicipio()
        val equivalente = CodigoMunicipio("3550308")
        val encaixotado: Any = codigo
        val outroEncaixotado: Any = equivalente

        assertEquals(equivalente, codigo)
        assertEquals(outroEncaixotado, encaixotado)
        assertEquals(outroEncaixotado.hashCode(), encaixotado.hashCode())
        assertNotEquals("4115200".toCodigoMunicipio(), codigo)
        assertEquals(JavaCodigoMunicipio.parse("3550308"), codigo.toJava())
        assertEquals(JavaCodigoMunicipio.parse("3550308").hashCode(), codigo.toJava().hashCode())
        assertEquals("3550308", codigo.toString())
        assertEquals("3550308", "${codigo}")
        assertEquals("3550308", encaixotado.toString())
    }
}
