package io.github.brasil4j.exemplos.kotlin

import io.github.brasil4j.identidade.DocumentParser
import io.github.brasil4j.identidade.RejectionReason
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.identidade.nis.NisRules
import io.github.brasil4j.identidade.nis.NisValidationMode
import io.github.brasil4j.kotlin.identidade.Cpf
import io.github.brasil4j.kotlin.identidade.parseOrNull
import io.github.brasil4j.kotlin.identidade.toCnpjOrNull
import io.github.brasil4j.kotlin.identidade.toCpfOrNull
import io.github.brasil4j.kotlin.identidade.toNis
import io.github.brasil4j.kotlin.identidade.toNisOrNull
import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.nis.Nis as JavaNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** Exemplos Kotlin dos contratos da Spec 010, delegados ao núcleo Java. */
@DisplayName("Exemplos Kotlin · Contratos, composição e polimorfismo")
class ContratosKotlinExemplosTest {
    @Test
    @DisplayName("Conversões tolerantes a nulo com o modo escolhido e fallback Elvis")
    fun `conversoes tolerantes a nulo`() {
        val campos: List<String?> = listOf("280.012.389-38", "280.012.389-39", null)
        val validos = campos.mapNotNull { it.toCpfOrNull(CpfValidationMode.NUMERIC) }
        assertEquals(listOf("28001238938"), validos.map(Cpf::canonicalValue))
        assertEquals("sem CNPJ", "12ABC34501DE35".toCnpjOrNull(CnpjValidationMode.NUMERIC)?.canonicalValue ?: "sem CNPJ")
        assertNull("00000000060".toNisOrNull(NisValidationMode.CAIXA_FGTS_V016))
        assertEquals("00000000060", "00000000060".toNis(NisValidationMode.LEGACY_MOD11).canonicalValue)
    }

    @Test
    @DisplayName("Motivo da rejeição com when, sem capturar exceções")
    fun `motivo da rejeicao`() {
        val parser: DocumentParser<JavaCpf> = JavaCpf.parser(CpfValidationMode.NUMERIC)
        val mensagens = listOf("28001238938", "28001238939", "abc", null).map { campo ->
            when (parser.parse(campo).rejection.map { it.reason }.orElse(null)) {
                null -> "ok"
                RejectionReason.MISSING -> "campo vazio"
                RejectionReason.INVALID_FORMAT -> "formato inválido"
                RejectionReason.INVALID_CHECK_DIGITS -> "DV divergente"
                else -> "outro"
            }
        }
        assertEquals(listOf("ok", "DV divergente", "formato inválido", "campo vazio"), mensagens)
        assertEquals("28001238938", parser.parseOrNull("280.012.389-38")?.canonicalValue)
    }

    @Test
    @DisplayName("Máscara de segurança e guarda opcional do NIS com lambdas Kotlin")
    fun `mascara de seguranca e guarda opcional`() {
        val cpf = Cpf("01234567890")
        assertEquals("***.345.678-**", cpf.applySecurityMask())
        assertEquals("###.345.678-##", cpf.applySecurityMask('#'))
        assertEquals("012.345.678-90", cpf.applyMask())

        val folha = JavaNis.parser(NisValidationMode.CAIXA_FGTS_V016)
            .withRule(NisRules.repeatedDigits())
            .withPolicy({ it.canonicalValue.startsWith("0") }, "PREFIXO_FOLHA")
        assertTrue(folha.isValid("00000000019"))
        assertEquals("PREFIXO_FOLHA", folha.parse("11111111116").rejection.flatMap { it.code }.orElse(null))
    }
}
