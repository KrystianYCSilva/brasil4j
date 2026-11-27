package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cnpj.Cnpj as JavaCnpj
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode
import io.github.brasil4j.identidade.composicao.DocumentCheck
import io.github.brasil4j.identidade.composicao.DocumentCheckResult
import io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.ACCEPTED
import io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_POLICY
import io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_VALIDATION
import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.identidade.nis.Nis as JavaNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Predicate

class DocumentCheckJarIT {
    @Test
    fun `carrega os tipos de composicao e a classe de valor dos JARs do reator`() {
        assertOrigemNoJar(DocumentCheck::class.java, "brasil4j-core")
        assertOrigemNoJar(DocumentCheckResult::class.java, "brasil4j-core")
        assertOrigemNoJar(Nis::class.java, "brasil4j-kotlin")
        assertOrigemNoJar(Class.forName("io.github.brasil4j.kotlin.identidade.NisKt"), "brasil4j-kotlin")
    }

    @Test
    fun `percorre lista mista com SAM e conserva resultados do consumidor`() {
        val campo: String? = "01234567898"
        val politicaDoImportador = Predicate<JavaNis> { it.canonicalValue.startsWith("00") }
        assertFalse(campo.isValidNis(politicaDoImportador))

        val itens: List<DocumentCheck<*>> = listOf(
            DocumentCheck.cpf(campo, CpfValidationMode.FORMAT_ONLY).withFormatter(JavaCpf::applyMask),
            DocumentCheck.nis(campo).withPolicy(politicaDoImportador),
            DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC)
                .withFormatter(JavaCnpj::applyMask)
        )
        val resultados = itens.map { it.evaluate() }
        val cpfFormatado: String? = resultados[0].formattedValue.orElse(null)
        val nisFormatado: String? = resultados[1].formattedValue.orElse(null)
        val cnpjFormatado: String? = resultados[2].formattedValue.orElse(null)

        assertEquals(listOf(ACCEPTED, REJECTED_BY_POLICY, ACCEPTED), resultados.map { it.status })
        assertEquals("012.345.678-98", cpfFormatado)
        assertNull(nisFormatado)
        assertEquals("12.ABC.345/01DE-35", cnpjFormatado)
        val nisSemFormato = DocumentCheck.nis("00000000019").evaluate()
        assertEquals(ACCEPTED, nisSemFormato.status)
        assertNull(nisSemFormato.formattedValue.orElse(null))
        val campoAusente: String? = null
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.nis(campoAusente).evaluate().status)
    }

    @Test
    fun `formata CNPJ conferido pela forma e protege diagnosticos`() {
        val entrada = "12ABC34501DE36"
        val formatado = "12.ABC.345/01DE-36"
        val item = DocumentCheck.cnpj(entrada, CnpjValidationMode.FORMAT_ONLY)
            .withFormatter(JavaCnpj::applyMask)
        val resultado = item.evaluate()

        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cnpj(entrada, CnpjValidationMode.ALPHANUMERIC)
            .evaluate().status)
        assertEquals(ACCEPTED, resultado.status)
        assertEquals(formatado, resultado.formattedValue.orElse(null))
        for (diagnostico in listOf(item.toString(), resultado.toString())) {
            assertFalse(diagnostico.contains(entrada))
            assertFalse(diagnostico.contains(formatado))
        }
    }

    @Test
    fun `rejeita a saida nula de um formatador Kotlin`() {
        val item = DocumentCheck.nis("00000000019").withFormatter { null }

        assertThrows(NullPointerException::class.java) { item.evaluate() }
    }

    @Test
    fun `conserva a saida vazia com politica Kotlin por SAM`() {
        val resultado = DocumentCheck.nis("00000000019")
            .withPolicy { it.canonicalValue.startsWith("00") }
            .withFormatter { "" }
            .evaluate()

        assertEquals(ACCEPTED, resultado.status)
        assertEquals("", resultado.formattedValue.orElse(null))
    }

    @ParameterizedTest(name = "caso C1 {0}")
    @CsvSource(
        "02, REJECTED_BY_VALIDATION",
        "09, REJECTED_BY_POLICY",
        "15, ACCEPTED",
        "17, ACCEPTED"
    )
    fun `usa o mesmo predicado na extensao Kotlin e na composicao`(
        caso: String,
        esperado: DocumentCheckResult.Status
    ) {
        val entrada = entradaC1(caso)
        val conferidos = mutableListOf<JavaNis>()
        val politica = Predicate<JavaNis> {
            conferidos += it
            it.canonicalValue.startsWith("00")
        }
        val valoresEsperados = if (esperado == REJECTED_BY_VALIDATION) emptyList() else listOf(entrada)
        assertEquals(esperado == ACCEPTED, entrada.isValidNis(politica))
        assertEquals(valoresEsperados, conferidos.map { it.canonicalValue })
        conferidos.clear()

        val apresentados = mutableListOf<String>()
        val resultado = DocumentCheck.nis(entrada).withPolicy(politica).withFormatter {
            assertSame(conferidos.single(), it)
            apresentados += it.canonicalValue
            "campo:${it.canonicalValue}"
        }.evaluate()

        assertEquals(esperado, resultado.status)
        assertEquals(valoresEsperados, conferidos.map { it.canonicalValue })
        assertEquals(if (esperado == ACCEPTED) listOf(entrada) else emptyList(), apresentados)
        val saida: String? = resultado.formattedValue.orElse(null)
        assertEquals(if (esperado == ACCEPTED) "campo:$entrada" else null, saida)
    }

    private fun assertOrigemNoJar(tipo: Class<*>, modulo: String) {
        val moduloAtual = Paths.get(System.getProperty("basedir"))
        val target = moduloAtual.resolveSibling(modulo).resolve("target")
        val origem = Paths.get(tipo.protectionDomain.codeSource.location.toURI()).toRealPath()
        val artefatos = Files.newDirectoryStream(target, "$modulo-*.jar").use { jars ->
            jars.filterNot { it.fileName.toString().matches(Regex(".*-(sources|javadoc|tests)\\.jar")) }
                .map { it.toRealPath() }
        }
        assertEquals(artefatos, listOf(origem), tipo.name)
    }

    private fun entradaC1(caso: String): String {
        val prefixo = "$caso,\""
        val recurso = requireNotNull(javaClass.getResourceAsStream("/nis/nis-c1.csv"))
        return recurso.bufferedReader().useLines { linhas ->
            val linha = linhas.first { it.startsWith(prefixo) }
            linha.substring(prefixo.length, linha.indexOf('"', prefixo.length))
        }
    }
}
