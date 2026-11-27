package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.nis.Nis as JavaNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrowsExactly
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class NisTest {
    @Test
    fun `aplica politica adicional com um valor Java canonico`() {
        val consultados = mutableListOf<String>()
        val ausente: String? = null
        val politica = java.util.function.Predicate<JavaNis> {
            consultados += it.canonicalValue
            it.canonicalValue.endsWith("9")
        }
        assertFalse(ausente.isValidNis(politica))
        assertFalse("012.34567.89-7".isValidNis(politica))
        assertTrue("00000000019".isValidNis(politica))
        assertFalse("01234567897".isValidNis(politica))
        assertEquals(listOf("00000000019", "01234567897"), consultados)
    }

    @Test
    fun `formata explicitamente com uma funcao do consumidor`() {
        val nis = "00000000019".toNis()
        assertEquals("inscrição:00000000019", nis.format { "inscrição:" + it.canonicalValue })
        assertEquals("[NIS oculto]", "$nis")
    }

    @Test
    fun `propaga a mesma excecao dos callbacks`() {
        val falha = IllegalStateException("callback externo")
        assertSame(falha, assertThrowsExactly(IllegalStateException::class.java) {
            "00000000019".isValidNis { throw falha }
        })
        assertSame(falha, assertThrowsExactly(IllegalStateException::class.java) {
            Nis("00000000019").format { throw falha }
        })
    }

    @Test
    fun `oculta qualquer inscrição na apresentação Kotlin`() {
        for (nis in listOf(Nis("00000000019"), Nis("01234567897"))) {
            val boxed: Any = nis
            assertEquals("[NIS oculto]", nis.toString())
            assertEquals("[NIS oculto]", "$nis")
            assertEquals("[NIS oculto]", "$boxed")
        }
    }

    @Test
    fun `compara valores Kotlin e converte para a comparação com o Java`() {
        val kotlin = Nis("00000000019")
        val primeiro: Any = kotlin
        val segundo: Any = "00000000019".toNis()
        val distinto: Any = Nis("01234567897")
        val java: Any = JavaNis.parse("00000000019")

        assertEquals(primeiro, segundo)
        assertEquals(primeiro.hashCode(), segundo.hashCode())
        assertNotEquals(primeiro, distinto)
        assertNotEquals(primeiro, java)
        assertNotEquals(java, primeiro)
        assertEquals(java, kotlin.toJava())
    }

    @ParameterizedTest(name = "caso {0}")
    @CsvFileSource(resources = ["/nis/nis-c1.csv"], nullValues = ["<null>"],
        ignoreLeadingAndTrailingWhitespace = false)
    fun `confere a apresentação literal`(caso: String, entrada: String?, aceita: Boolean) {
        assertEquals(aceita, entrada.isValidNis())
        if (aceita) {
            assertEquals(entrada, Nis(entrada).canonicalValue)
        } else {
            assertFormatoInvalido { Nis(entrada) }
        }
        if (entrada != null) {
            if (aceita) {
                assertEquals(entrada, entrada.toNis().canonicalValue)
            } else {
                assertFormatoInvalido { entrada.toNis() }
            }
        }
    }

    private fun assertFormatoInvalido(construcao: () -> Unit) {
        val erro = assertThrowsExactly(IllegalArgumentException::class.java) { construcao() }
        assertEquals("NIS inválido.", erro.message)
        assertNull(erro.cause)
    }
}
