package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.nis.Nis as JavaNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrowsExactly
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.w3c.dom.Element
import java.util.stream.Stream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class NisJarIT {
    @Test
    fun `consome NIS dos dois JARs do reator`() {
        assertTrue(Nis::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        assertTrue(JavaNis::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
    }

    @Test
    fun `compoe politica e apresentacao com conversao Java pelos JARs`() {
        val campo = extraiNisTrab("zeros")
        val nis = Nis(campo)
        val java = JavaNis.parse("00000000019")
        val boxed: Any = nis

        assertEquals(java, nis.toJava())
        assertEquals(java.hashCode(), nis.hashCode())
        assertEquals("[NIS oculto]", "$boxed")
        assertTrue(campo.isValidNis { it == java })
        assertEquals("campo:00000000019", nis.format { "campo:" + it.canonicalValue })
    }

    @ParameterizedTest(name = "fragmento {0}")
    @MethodSource("fragmentos")
    fun `confere campo histórico`(caso: String, esperado: String?, aceita: Boolean) {
        val entrada = extraiNisTrab(caso)
        assertEquals(esperado, entrada)

        if (aceita) {
            val nis = Nis(entrada)
            assertEquals(esperado, nis.canonicalValue)
            assertEquals("[NIS oculto]", nis.toString())
            assertEquals(nis, requireNotNull(entrada).toNis())
        } else {
            assertFormatoInvalido { Nis(entrada) }
            if (entrada != null) {
                assertFormatoInvalido { entrada.toNis() }
            }
        }
    }

    private fun assertFormatoInvalido(construcao: () -> Unit) {
        val erro = assertThrowsExactly(IllegalArgumentException::class.java) { construcao() }
        assertEquals("NIS inválido.", erro.message)
        assertNull(erro.cause)
    }

    private fun extraiNisTrab(caso: String): String? {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")

        val recurso = requireNotNull(javaClass.getResourceAsStream("/nis/esocial-2.5/$caso.xml")) { caso }
        return recurso.use { xml ->
            val raiz = factory.newDocumentBuilder().parse(xml).documentElement
            val trabalhadores = raiz.getElementsByTagNameNS(NAMESPACE_S2200, "trabalhador")
            assertEquals(1, trabalhadores.length)
            val trabalhador = trabalhadores.item(0) as Element
            trabalhador.getElementsByTagNameNS(NAMESPACE_S2200, "nisTrab").item(0)?.textContent
        }
    }

    companion object {
        private const val NAMESPACE_S2200 = "http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00"

        @JvmStatic
        fun fragmentos(): Stream<Arguments> = Stream.of(
            Arguments.of("zeros", "00000000019", true),
            Arguments.of("canonico", "01234567897", true),
            Arguments.of("curto", "1234567897", false),
            Arguments.of("letra", "A1234567897", false),
            Arguments.of("dv-divergente", "01234567898", true),
            Arguments.of("mascara", "012.34567.89-7", false),
            Arguments.of("espacos", " 01234567897 ", false),
            Arguments.of("quebra-tab", "\n\t01234567897", false),
            Arguments.of("somente-espacos", "   ", false),
            Arguments.of("omitido", null, false),
            Arguments.of("vazio-curto", "", false),
            Arguments.of("vazio-longo", "", false)
        )
    }
}
