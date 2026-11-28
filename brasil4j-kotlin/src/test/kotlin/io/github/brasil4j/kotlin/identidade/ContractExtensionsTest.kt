package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.DocumentParser
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.identidade.nis.NisValidationMode
import io.github.brasil4j.identidade.cnpj.Cnpj as JavaCnpj
import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.nis.Nis as JavaNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContractExtensionsTest {
    @Test
    fun `conversoes tolerantes a nulo devolvem as value classes`() {
        val ausente: String? = null
        assertEquals("28001238939", "28001238939".toCpfOrNull()?.canonicalValue)
        assertNull("28001238939".toCpfOrNull(CpfValidationMode.NUMERIC))
        assertNull(ausente.toCpfOrNull())
        assertEquals("12ABC34501DE35", "12.ABC.345/01DE-35".toCnpjOrNull()?.canonicalValue)
        assertNull("12ABC34501DE35".toCnpjOrNull(CnpjValidationMode.NUMERIC))
        assertEquals("01234567898", "01234567898".toNisOrNull()?.canonicalValue)
        assertNull("00000000060".toNisOrNull(NisValidationMode.CAIXA_FGTS_V016))
        assertEquals("00000000060", "00000000060".toNisOrNull(NisValidationMode.LEGACY_MOD11)?.canonicalValue)
        assertNull(ausente.toNisOrNull())
    }

    @Test
    fun `interpretador generico devolve o tipo Java ou nulo`() {
        val parser: DocumentParser<JavaCpf> = JavaCpf.parser(CpfValidationMode.NUMERIC)
        assertEquals(JavaCpf.parse("28001238938"), parser.parseOrNull("280.012.389-38"))
        assertNull(parser.parseOrNull("28001238939"))
        assertNull(parser.parseOrNull(null))
        assertEquals(JavaCnpj.parse("12ABC34501DE35"), JavaCnpj.parser().parseOrNull("12ABC34501DE35"))
    }

    @Test
    fun `nis com modo explicito`() {
        assertEquals(JavaNis.parse("00000000019"), "00000000019".toNis(NisValidationMode.CAIXA_FGTS_V016).toJava())
        assertThrows(IllegalArgumentException::class.java) { "00000000060".toNis(NisValidationMode.CAIXA_FGTS_V016) }
    }

    @Test
    fun `cnpj confere o dv`() {
        assertTrue(Cnpj("12ABC34501DE35").hasValidCheckDigits())
        assertFalse("12ABC34501DE36".toCnpj(CnpjValidationMode.FORMAT_ONLY).hasValidCheckDigits())
    }

    @Test
    fun `mascara de seguranca e mascara completa`() {
        val cpf = Cpf("01234567890")
        assertEquals("***.345.678-**", cpf.applySecurityMask())
        assertEquals("###.345.678-##", cpf.applySecurityMask('#'))
        assertEquals("012.345.678-90", cpf.applyMask())
        assertEquals("**.ABC.345/01DE-**", Cnpj("12ABC34501DE35").applySecurityMask())
        assertEquals("***3456789*", Nis("01234567898").applySecurityMask())
        assertEquals("***.***.***-**", cpf.toString())
        assertThrows(IllegalArgumentException::class.java) { cpf.applySecurityMask('0') }
    }
}
