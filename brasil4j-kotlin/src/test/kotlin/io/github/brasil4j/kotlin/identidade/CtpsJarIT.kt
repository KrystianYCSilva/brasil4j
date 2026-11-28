package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.cpf.CpfValidationMode.FORMAT_ONLY
import io.github.brasil4j.identidade.cpf.CpfValidationMode.NUMERIC
import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult
import io.github.brasil4j.trabalhista.ctps.CtpsGroup
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin.PHYSICAL
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin.UNDETERMINED
import io.github.brasil4j.trabalhista.ctps.CtpsProfile
import io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.w3c.dom.Element
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties
import java.util.jar.JarFile
import java.util.stream.Stream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class CtpsJarIT {
    @Test
    fun `consome o core e as extensões dos JARs do reator`() {
        val kotlinFacade = Class.forName("io.github.brasil4j.kotlin.identidade.CtpsKt")
        assertJar(kotlinFacade, "target", "brasil4j-kotlin")
        assertJar(CtpsProfile::class.java, "../brasil4j-core/target", "brasil4j-core")
    }

    @ParameterizedTest(name = "fragmento {0}")
    @MethodSource("cases")
    fun `confere o fragmento histórico`(id: String, expected: List<String>) {
        val group = readGroup(id)
        val result = group.checkCtps(ESOCIAL_S2200_V2_5)
        assertEquals(expected[1], result.status.name)
        val fields = if (expected[2].isEmpty()) emptyList() else expected[2].split(',')
        assertEquals(fields, result.affectedFields.map { it.name })
        assertDiagnostico(result, expected[1], fields)
        if (group != null) {
            assertEquals("[Grupo CTPS oculto]", group.toString())
        }
        if (expected[1] == "CONFORMING") {
            val record = requireNotNull(result.recordOrNull)
            assertEquals(expected[3].replace("\\u0020", " "), record.number)
            assertEquals(expected[4].replace("\\u0020", " "), record.series)
            assertEquals(expected[5], record.uf.sigla)
            assertEquals("[Registro CTPS oculto]", record.toString())
        } else {
            assertNull(result.recordOrNull)
        }
    }

    @Test
    fun `preserva proveniência e diagnóstico pelos JARs`() {
        for (origin in CtpsOrigin.entries) {
            val group = CtpsGroup.of("0123456", "7890", "PR", origin, false)
            val result = group.checkCtps(ESOCIAL_S2200_V2_5)
            assertEquals(origin, result.originOrNull)
            assertEquals(Uf.PR, result.recordOrNull?.uf)
            assertEquals(if (origin == PHYSICAL) Uf.PR else null, result.issuingUfOrNull)
            assertEquals("[Grupo CTPS oculto]", group.toString())
            assertDiagnostico(result, "CONFORMING", emptyList())
        }
        val migrated = CtpsGroup.of("123", "4", "PR", UNDETERMINED, true).checkCtps(ESOCIAL_S2200_V2_5)
        val record = requireNotNull(migrated.recordOrNull)
        assertTrue(migrated.hasSourceLoss())
        assertEquals("123", record.number)
        assertEquals("4", record.series)
        assertDiagnostico(migrated, "CONFORMING", emptyList())
    }

    @Test
    fun `reusa CPF do núcleo e as extensões para identificação digital`() {
        assertJar(JavaCpf::class.java, "../brasil4j-core/target", "brasil4j-core")
        assertJar(Class.forName("io.github.brasil4j.kotlin.identidade.CpfKt"), "target", "brasil4j-kotlin")
        assertNotNull("28001238938".toCpfOrNull(FORMAT_ONLY))
        assertNotNull("28001238938".toCpfOrNull(NUMERIC))
        assertNotNull("28001238939".toCpfOrNull(FORMAT_ONLY))
        assertNull("28001238939".toCpfOrNull(NUMERIC))
    }

    private fun readGroup(id: String): CtpsGroup? {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        val root = resource("$id.xml").use { factory.newDocumentBuilder().parse(it).documentElement }
        assertEquals(NS, root.namespaceURI)
        assertEquals("eSocial", root.localName)
        val event = requireNotNull(child(root, "evtAdmissao"))
        val worker = requireNotNull(child(event, "trabalhador"))
        val group = child(child(worker, "documentos"), "CTPS") ?: return null
        return CtpsGroup.of(
            child(group, "nrCtps")?.textContent,
            child(group, "serieCtps")?.textContent,
            child(group, "ufCtps")?.textContent,
            UNDETERMINED,
            false
        )
    }

    private fun child(parent: Element?, localName: String): Element? =
        generateSequence(parent?.firstChild) { it.nextSibling }
            .filterIsInstance<Element>()
            .firstOrNull { it.namespaceURI == NS && it.localName == localName }

    private fun assertJar(type: Class<*>, directory: String, artifact: String) {
        val jar = Paths.get(type.protectionDomain.codeSource.location.toURI())
        assertEquals(Paths.get(directory).toRealPath(), jar.parent.toRealPath())
        assertTrue(Files.isRegularFile(jar))
        JarFile(jar.toFile()).use { archive ->
            val descriptor = requireNotNull(archive.getJarEntry("META-INF/maven/io.github.brasil4j/$artifact/pom.properties"))
            val coordinates = Properties()
            archive.getInputStream(descriptor).use { coordinates.load(it) }
            val version = requireNotNull(System.getProperty("brasil4j.reactor.version"))
            assertTrue(version.isNotEmpty())
            assertEquals(version, coordinates.getProperty("version"))
            assertEquals("$artifact-$version.jar", jar.fileName.toString())
        }
    }

    private fun assertDiagnostico(result: CtpsCheckResult, status: String, fields: List<String>) {
        assertEquals(
            "CtpsCheckResult[profile=ESOCIAL_S2200_V2_5, status=$status, affectedFields=$fields]",
            result.toString()
        )
    }

    companion object {
        private const val ROOT = "/ctps/esocial-2.5/"
        private const val NS = "http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00"

        private fun resource(name: String) = requireNotNull(CtpsJarIT::class.java.getResourceAsStream(ROOT + name))

        @JvmStatic
        fun cases(): Stream<Arguments> {
            val lines = resource("cases.tsv").bufferedReader(Charsets.UTF_8).use { it.readLines() }
            assertEquals("id\tstatus\tfields\tnumber\tseries\tuf\tsuite", lines.first())
            val rows = lines.drop(1).map { it.split('\t') }
            rows.forEach { row ->
                assertEquals(7, row.size)
                assertTrue(row[6] == "MAIN" || row[6] == "EXTRA", row[6])
            }
            val main = rows.filter { it[6] == "MAIN" }
            assertEquals((1..11).map { it.toString().padStart(2, '0') }, main.map { it[0] })
            val extraIds = rows.filter { it[6] == "EXTRA" }.map { it[0] }.sorted()
            assertEquals(
                listOf(
                    "extra-ctps-vazia", "extra-documentos-vazio", "extra-numero-vazio",
                    "extra-omitido-e-invalido", "extra-sem-documentos"
                ),
                extraIds
            )
            assertEquals(
                mapOf("ABSENT" to 1, "INCOMPLETE" to 1, "OUT_OF_PROFILE" to 5, "CONFORMING" to 4),
                main.groupingBy { it[1] }.eachCount()
            )
            return rows.stream().map { Arguments.of(it[0], it) }
        }
    }
}
