package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.DocumentParser
import io.github.brasil4j.identidade.RejectionReason
import io.github.brasil4j.identidade.ValidationResult
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.identidade.nis.NisValidationMode
import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

/** Consome os contratos da Spec 010 a partir dos JARs do reator, em Kotlin. */
class ContratosJarIT {
    @Test
    fun `carrega contratos do nucleo e extensoes do JAR Kotlin`() {
        assertOrigemNoJar(DocumentParser::class.java, "brasil4j-core")
        assertOrigemNoJar(ValidationResult::class.java, "brasil4j-core")
        assertOrigemNoJar(Class.forName("io.github.brasil4j.kotlin.identidade.DocumentParsersKt"), "brasil4j-kotlin")
    }

    @Test
    fun `usa motivo da rejeicao, conversoes tolerantes a nulo e mascara de seguranca`() {
        val motivo = JavaCpf.parser(CpfValidationMode.NUMERIC).parse("28001238939").rejection.map { it.reason }
        assertEquals(RejectionReason.INVALID_CHECK_DIGITS, motivo.orElse(null))
        assertNull("28001238939".toCpfOrNull(CpfValidationMode.NUMERIC))
        assertEquals("***.012.389-**", "28001238939".toCpfOrNull()?.applySecurityMask())
        assertNull("00000000060".toNisOrNull(NisValidationMode.CAIXA_FGTS_V016))
        assertEquals("00000000060", JavaCpf.parser().parseOrNull("00000000060")?.canonicalValue)
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
}
