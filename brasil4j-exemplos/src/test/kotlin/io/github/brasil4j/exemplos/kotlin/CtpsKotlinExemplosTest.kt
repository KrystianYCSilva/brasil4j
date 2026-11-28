package io.github.brasil4j.exemplos.kotlin

import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult
import io.github.brasil4j.trabalhista.ctps.CtpsGroup
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin
import io.github.brasil4j.trabalhista.ctps.CtpsProfile
import io.github.brasil4j.kotlin.identidade.checkCtps
import io.github.brasil4j.kotlin.identidade.issuingUfOrNull
import io.github.brasil4j.kotlin.identidade.originOrNull
import io.github.brasil4j.kotlin.identidade.recordOrNull
import io.github.brasil4j.kotlin.identidade.toCpfOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CtpsKotlinExemplosTest {
    @Test
    fun importaGrupoFisicoPreservandoZeros() {
        val grupo = CtpsGroup.of("0000123", "00004", "PR", CtpsOrigin.PHYSICAL, false)
        val resultado = grupo.checkCtps(CtpsProfile.ESOCIAL_S2200_V2_5)

        assertEquals(CtpsCheckResult.Status.CONFORMING, resultado.status)
        val registro = requireNotNull(resultado.recordOrNull)
        assertEquals("0000123", registro.number)
        assertEquals("00004", registro.series)
        assertEquals(Uf.PR, resultado.issuingUfOrNull)
    }

    @Test
    fun importaProjecaoLegadaComPerdaDeclarada() {
        // O receptor anulável permite passar diretamente o grupo extraído.
        val grupo: CtpsGroup? = CtpsGroup.of("123", "4", "PR", CtpsOrigin.CPF_PROJECTION, true)
        val resultado = grupo.checkCtps(CtpsProfile.ESOCIAL_S2200_V2_5)

        assertEquals(CtpsOrigin.CPF_PROJECTION, resultado.originOrNull)
        assertTrue(resultado.hasSourceLoss())
        val registro = requireNotNull(resultado.recordOrNull)
        assertEquals("123", registro.number)
        assertEquals("4", registro.series)
        assertEquals(Uf.PR, registro.uf)
        assertNull(resultado.issuingUfOrNull)
    }

    @Test
    fun confereIdentificadorDigitalComOsModosDoCpf() {
        assertEquals("28001238938", "28001238938".toCpfOrNull(CpfValidationMode.FORMAT_ONLY)?.canonicalValue)
        assertEquals("28001238938", "28001238938".toCpfOrNull(CpfValidationMode.NUMERIC)?.canonicalValue)
        assertEquals("28001238939", "28001238939".toCpfOrNull(CpfValidationMode.FORMAT_ONLY)?.canonicalValue)
        assertNull("28001238939".toCpfOrNull(CpfValidationMode.NUMERIC))
    }
}
