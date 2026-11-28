package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cnpj.Cnpj as JavaCnpj
import io.github.brasil4j.identidade.cnpj.CnpjType
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode

/** CNPJ completo baseado no valor imutável do núcleo Java. */
@JvmInline
public value class Cnpj private constructor(private val delegate: JavaCnpj) {
    /**
     * Interpreta [input] no modo alfanumérico padrão, válido também para CNPJ numérico.
     *
     * @throws IllegalArgumentException se a entrada for inválida
     */
    public constructor(input: String) : this(JavaCnpj.parse(input))

    /** Os 14 caracteres do CNPJ, sem pontuação. */
    public val canonicalValue: String get() = delegate.canonicalValue

    /** A base de 12 caracteres. */
    public val base: String get() = delegate.base

    /** Os oito caracteres da raiz. */
    public val root: String get() = delegate.root

    /** Os quatro caracteres da ordem do estabelecimento. */
    public val branch: String get() = delegate.branch

    /** Os dois dígitos verificadores informados. */
    public val checkDigits: String get() = delegate.checkDigits

    /** Tipo da base, numérica ou alfanumérica. */
    public val type: CnpjType get() = delegate.type

    /** Devolve a forma com máscara completa. */
    public fun applyMask(): String = delegate.applyMask()

    /**
     * Devolve a máscara de segurança, que oculta os dois primeiros caracteres e os dois DVs com [maskChar].
     *
     * @throws IllegalArgumentException se [maskChar] for letra ou dígito
     */
    public fun applySecurityMask(maskChar: Char = '*'): String = delegate.applySecurityMask(maskChar)

    /** Confere os DVs pelo módulo 11 do CNPJ, com o bloqueio de 14 zeros. */
    public fun hasValidCheckDigits(): Boolean = delegate.hasValidCheckDigits()

    /** Devolve o mesmo objeto imutável do núcleo Java. */
    public fun toJava(): JavaCnpj = delegate

    /** Devolve a forma canônica do CNPJ. */
    override fun toString(): String = canonicalValue

    internal companion object {
        internal fun from(input: String, mode: CnpjValidationMode): Cnpj = Cnpj(JavaCnpj.parse(input, mode))

        internal fun of(delegate: JavaCnpj): Cnpj = Cnpj(delegate)
    }
}

/**
 * Interpreta o texto como CNPJ no modo padrão.
 *
 * @throws IllegalArgumentException se a entrada for inválida
 */
public fun String.toCnpj(): Cnpj = Cnpj(this)

/**
 * Interpreta o texto como CNPJ com o [mode] informado.
 *
 * @throws IllegalArgumentException se a entrada for inválida
 */
public fun String.toCnpj(mode: CnpjValidationMode): Cnpj = Cnpj.from(this, mode)

/** Interpreta o texto como CNPJ no [mode] informado, ou devolve `null` para entrada ausente ou rejeitada. */
public fun String?.toCnpjOrNull(mode: CnpjValidationMode = CnpjValidationMode.ALPHANUMERIC): Cnpj? =
    JavaCnpj.parser(mode).parseOrNull(this)?.let(Cnpj::of)
