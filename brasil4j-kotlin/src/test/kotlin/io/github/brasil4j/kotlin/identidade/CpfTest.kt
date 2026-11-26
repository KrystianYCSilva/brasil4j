package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CpfTest {
    @Test
    fun `preserva valor e oferece representação protegida`() {
        val value = Cpf("012.345.678-90")
        assertEquals("01234567890", value.canonicalValue)
        assertEquals("012345678", value.base)
        assertEquals("90", value.checkDigits)
        assertEquals("012.345.678-90", value.applyMask())
        assertEquals("***.***.***-**", value.toString())
        assertEquals("***.***.***-**", "$value")
        assertEquals(JavaCpf.parse("01234567890"), value.toJava())
        val same = "01234567890".toCpf()
        assertEquals(same, value)
        assertEquals(same.hashCode(), value.hashCode())
    }

    @Test
    fun `aplica formato por padrão e módulo 11 por opção`() {
        assertFalse(Cpf("280.012.389-39").hasValidCheckDigits())
        assertEquals("39", "28001238939".toCpf(CpfValidationMode.FORMAT_ONLY).checkDigits)
        assertTrue("280.012.389-38".toCpf(CpfValidationMode.NUMERIC).hasValidCheckDigits())
        assertThrows(IllegalArgumentException::class.java) {
            "28001238939".toCpf(CpfValidationMode.NUMERIC)
        }
        assertThrows(IllegalArgumentException::class.java) { Cpf("280.012.38939") }
    }
}
