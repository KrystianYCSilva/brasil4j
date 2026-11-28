package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult
import io.github.brasil4j.trabalhista.ctps.CtpsGroup
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin
import io.github.brasil4j.trabalhista.ctps.CtpsProfile
import io.github.brasil4j.trabalhista.ctps.CtpsRecord

/**
 * Confere os campos já extraídos no [profile]. Um receptor nulo representa grupo omitido.
 */
public fun CtpsGroup?.checkCtps(profile: CtpsProfile): CtpsCheckResult = profile.check(this)

/** Obtém explicitamente o registro conforme, ou nulo nos demais desfechos. */
public val CtpsCheckResult.recordOrNull: CtpsRecord?
    get() = record.orElse(null)

/** Obtém a origem declarada, ou nulo quando o grupo está ausente. */
public val CtpsCheckResult.originOrNull: CtpsOrigin?
    get() = origin.orElse(null)

/** Obtém a UF de expedição apenas para um registro físico conforme. */
public val CtpsCheckResult.issuingUfOrNull: Uf?
    get() = issuingUf.orElse(null)
