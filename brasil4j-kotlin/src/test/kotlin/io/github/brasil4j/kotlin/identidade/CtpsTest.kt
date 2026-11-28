package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult.Status.*
import io.github.brasil4j.trabalhista.ctps.CtpsGroup
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin.UNDETERMINED
import io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CtpsTest {
    @Test
    fun `distingue os quatro estados com perfil explícito`() {
        val absent: CtpsGroup? = null
        val incomplete = CtpsGroup.of("0000123", null, "PR", UNDETERMINED, false)
        val invalid = CtpsGroup.of("0000123", "00004", "pr", UNDETERMINED, false)
        val valid = CtpsGroup.of("0000123", "  ", "PR", UNDETERMINED, false)

        for ((group, expected) in listOf(absent to ABSENT, incomplete to INCOMPLETE, invalid to OUT_OF_PROFILE)) {
            val result = group.checkCtps(ESOCIAL_S2200_V2_5)
            assertEquals(expected, result.status)
            assertSame(ESOCIAL_S2200_V2_5, result.profile)
            assertNull(result.recordOrNull)
        }
        val result = valid.checkCtps(ESOCIAL_S2200_V2_5)
        assertEquals(CONFORMING, result.status)
        val record = requireNotNull(result.recordOrNull)
        assertEquals("0000123", record.number)
        assertEquals("  ", record.series)
    }
}
