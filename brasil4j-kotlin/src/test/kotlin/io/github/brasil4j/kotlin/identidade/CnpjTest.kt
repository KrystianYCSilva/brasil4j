package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cnpj.Cnpj as JavaCnpj
import io.github.brasil4j.identidade.cnpj.CnpjType
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CnpjTest {
    @Test
    fun `expõe o mesmo valor e recortes do Java`() {
        val value = Cnpj("12.ABC.345/01DE-35")
        assertEquals("12ABC34501DE35", value.canonicalValue)
        assertEquals("12ABC34501DE", value.base)
        assertEquals("12ABC345", value.root)
        assertEquals("01DE", value.branch)
        assertEquals("35", value.checkDigits)
        assertEquals(CnpjType.ALPHANUMERIC, value.type)
        assertEquals("12.ABC.345/01DE-35", value.applyMask())
        assertEquals("12ABC34501DE35", value.toString())
        assertEquals(JavaCnpj.parse("12ABC34501DE35"), value.toJava())
        assertEquals(value, "12ABC34501DE35".toCnpj())
        assertEquals(value.hashCode(), "12ABC34501DE35".toCnpj().hashCode())
    }

    @Test
    fun `permite escolher o modo e conserva o padrão do construtor`() {
        assertEquals("00000000000191", Cnpj("00.000.000/0001-91").canonicalValue)
        assertEquals(CnpjType.NUMERIC, "00000000000191".toCnpj(CnpjValidationMode.NUMERIC).type)
        assertEquals("36", "12ABC34501DE36".toCnpj(CnpjValidationMode.FORMAT_ONLY).checkDigits)
        assertThrows(IllegalArgumentException::class.java) { Cnpj("12ABC34501DE36") }
        assertThrows(IllegalArgumentException::class.java) {
            "12ABC34501DE35".toCnpj(CnpjValidationMode.NUMERIC)
        }
    }
}
