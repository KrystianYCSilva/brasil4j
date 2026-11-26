package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cnpj.Cnpj as JavaCnpj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class CnpjJarIT {
    @ParameterizedTest
    @CsvFileSource(resources = ["/cnpj-c1.csv", "/cnpj-rfb-ascii.csv"], nullValues = ["<null>"], ignoreLeadingAndTrailingWhitespace = false)
    fun `executa os vetores de CNPJ com os dois JARs`(input: String, canonical: String?) {
        assertTrue(Cnpj::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        assertTrue(JavaCnpj::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        when {
            canonical == null -> assertThrows(IllegalArgumentException::class.java) { input.toCnpj() }
            else -> assertEquals(canonical, input.toCnpj().canonicalValue)
        }
    }
}
