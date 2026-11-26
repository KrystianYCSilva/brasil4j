package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CpfJarIT {
    @Test
    fun `consome CPF dos dois JARs do reator`() {
        assertTrue(Cpf::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        assertTrue(JavaCpf::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))
        assertEquals("28001238938", "280.012.389-38".toCpf(CpfValidationMode.NUMERIC).canonicalValue)
    }
}
