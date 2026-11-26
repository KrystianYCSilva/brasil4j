package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.cpf.Cpf as JavaCpf
import io.github.brasil4j.identidade.cpf.CpfValidationMode

/** CPF baseado no valor imutável do núcleo Java. */
@JvmInline
value class Cpf private constructor(private val delegate: JavaCpf) {
    /**
     * Interpreta [input] no modo padrão, que verifica somente o formato.
     *
     * @throws IllegalArgumentException se o formato for inválido
     */
    constructor(input: String) : this(JavaCpf.parse(input))

    /** Os 11 dígitos do CPF, sem pontuação. */
    val canonicalValue: String get() = delegate.canonicalValue

    /** Os nove dígitos da base. */
    val base: String get() = delegate.base

    /** Os dois dígitos verificadores informados. */
    val checkDigits: String get() = delegate.checkDigits

    /** Devolve o CPF com máscara completa, exibindo todos os dígitos. */
    fun applyMask(): String = delegate.applyMask()

    /** Confere se os DVs informados coincidem com o módulo 11. */
    fun hasValidCheckDigits(): Boolean = delegate.hasValidCheckDigits()

    /** Devolve o mesmo objeto imutável do núcleo Java. */
    fun toJava(): JavaCpf = delegate

    /** Devolve uma representação que oculta todos os dígitos do CPF. */
    override fun toString(): String = delegate.toString()

    internal companion object {
        internal fun from(input: String, mode: CpfValidationMode): Cpf = Cpf(JavaCpf.parse(input, mode))
    }
}

/**
 * Interpreta o texto como CPF no modo padrão, que verifica o formato.
 *
 * @throws IllegalArgumentException se o formato for inválido
 */
fun String.toCpf(): Cpf = Cpf(this)

/**
 * Interpreta o texto como CPF com o [mode] informado.
 *
 * @throws IllegalArgumentException se a entrada for inválida no modo escolhido
 */
fun String.toCpf(mode: CpfValidationMode): Cpf = Cpf.from(this, mode)
