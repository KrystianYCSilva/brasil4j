package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Status.*
import io.github.brasil4j.trabalhista.ctps.CtpsGroup
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin.PHYSICAL
import io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CtpsProvenanceTest {
    @Test
    fun `expõe opções anuláveis sem alterar a origem informada`() {
        for (origin in CtpsOrigin.entries) {
            val result = CtpsGroup.of("0123456", "7890", "PR", origin, false).checkCtps(ESOCIAL_S2200_V2_5)
            assertEquals(origin, result.originOrNull)
            assertEquals(if (origin == PHYSICAL) Uf.PR else null, result.issuingUfOrNull)
            assertFalse(result.hasSourceLoss())
        }
        val absent: CtpsGroup? = null
        val result = absent.checkCtps(ESOCIAL_S2200_V2_5)
        assertNull(result.originOrNull)
        assertNull(result.issuingUfOrNull)
        assertFalse(result.hasSourceLoss())
    }

    @Test
    fun `preserva perda e oculta os dados em todos os desfechos`() {
        val group = CtpsGroup.of("NUM-S3", "@@S@@", "SC", PHYSICAL, true)
        val cases = listOf(
            null to ABSENT,
            CtpsGroup.of("NUM-S3", null, "SC", PHYSICAL, true) to INCOMPLETE,
            CtpsGroup.of("NUM-S3", "@@S@@", "INVALID-UF", PHYSICAL, true) to OUT_OF_PROFILE,
            group to CONFORMING
        )
        for ((input, status) in cases) {
            val result = input.checkCtps(ESOCIAL_S2200_V2_5)
            assertEquals(status, result.status)
            assertEquals(input != null, result.hasSourceLoss())
            assertEquals(if (input == null) null else PHYSICAL, result.originOrNull)
            assertEquals(if (status == CONFORMING) Uf.SC else null, result.issuingUfOrNull)
            for (sentinel in listOf("NUM-S3", "@@S@@", "SC", "INVALID-UF")) {
                assertFalse("$result".contains(sentinel))
            }
        }
        val record = requireNotNull(group.checkCtps(ESOCIAL_S2200_V2_5).recordOrNull)
        assertEquals("[Grupo CTPS oculto]", "$group")
        assertEquals("[Registro CTPS oculto]", "$record")
        assertEquals("NUM-S3", record.number)
    }
}
