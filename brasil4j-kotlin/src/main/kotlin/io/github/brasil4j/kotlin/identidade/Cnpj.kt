package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cnpj.Cnpj as JavaCnpj
import io.github.brasil4j.identidade.cnpj.CnpjType
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode

/** CNPJ completo baseado no valor imutável do núcleo Java. */
@JvmInline
value class Cnpj private constructor(private val delegate: JavaCnpj) {
    /**
     * Interpreta [input] no modo alfanumérico padrão, válido também para CNPJ numérico.
     *
     * @throws IllegalArgumentException se a entrada for inválida
     */
    constructor(input: String) : this(JavaCnpj.parse(input))

    /** Os 14 caracteres do CNPJ, sem pontuação. */
    val canonicalValue: String get() = delegate.canonicalValue

    /** A base de 12 caracteres. */
    val base: String get() = delegate.base

    /** Os oito caracteres da raiz. */
    val root: String get() = delegate.root

    /** Os quatro caracteres da ordem do estabelecimento. */
    val branch: String get() = delegate.branch

    /** Os dois dígitos verificadores informados. */
    val checkDigits: String get() = delegate.checkDigits

    /** Tipo da base, numérica ou alfanumérica. */
    val type: CnpjType get() = delegate.type

    /** Devolve a forma com máscara completa. */
    fun applyMask(): String = delegate.applyMask()

    /** Devolve o mesmo objeto imutável do núcleo Java. */
    fun toJava(): JavaCnpj = delegate

    /** Devolve a forma canônica do CNPJ. */
    override fun toString(): String = canonicalValue

    internal companion object {
        internal fun from(input: String, mode: CnpjValidationMode): Cnpj = Cnpj(JavaCnpj.parse(input, mode))
    }
}

/**
 * Interpreta o texto como CNPJ no modo padrão.
 *
 * @throws IllegalArgumentException se a entrada for inválida
 */
fun String.toCnpj(): Cnpj = Cnpj(this)

/**
 * Interpreta o texto como CNPJ com o [mode] informado.
 *
 * @throws IllegalArgumentException se a entrada for inválida
 */
fun String.toCnpj(mode: CnpjValidationMode): Cnpj = Cnpj.from(this, mode)
