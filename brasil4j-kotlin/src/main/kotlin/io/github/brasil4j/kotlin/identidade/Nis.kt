package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.nis.Nis as JavaNis
import io.github.brasil4j.identidade.nis.NisFormatter
import io.github.brasil4j.identidade.nis.NisValidator
import java.util.function.Function
import java.util.function.Predicate

/** Inscrição estrutural baseada no valor imutável do núcleo Java. */
@JvmInline
value class Nis private constructor(private val delegate: JavaNis) {
    /**
     * Confere a apresentação literal de [input], com exatamente 11 dígitos ASCII.
     *
     * @throws IllegalArgumentException se a entrada for nula ou tiver formato inválido
     */
    constructor(input: String?) : this(JavaNis.parse(input))

    /** Obtém explicitamente os 11 dígitos na ordem recebida, inclusive zeros iniciais. */
    val canonicalValue: String get() = delegate.canonicalValue

    /** Devolve o valor imutável do núcleo para comparação explícita com Java. */
    fun toJava(): JavaNis = delegate

    /**
     * Aplica a apresentação escolhida em [formatter] ao valor estrutural Java.
     * O resultado pode expor a inscrição e as exceções da função propagam-se ao chamador.
     *
     * @throws NullPointerException se a função produzir nulo
     */
    fun format(formatter: Function<in JavaNis, String>): String = NisFormatter.format(delegate, formatter)

    /** Devolve o marcador fixo de ocultação, sem dígitos da inscrição. */
    override fun toString(): String = delegate.toString()
}

/**
 * Interpreta o texto literal como uma inscrição estrutural.
 *
 * @throws IllegalArgumentException se a entrada tiver formato inválido
 */
fun String.toNis(): Nis = Nis(this)

/** Confere se o texto tem exatamente 11 dígitos ASCII. Devolve `false` para nulo. */
fun String?.isValidNis(): Boolean = NisValidator.isValid(this)

/**
 * Confere a forma e aplica [policy] uma vez ao valor estrutural Java.
 * A política é executada somente para entrada canônica e suas exceções são propagadas.
 */
fun String?.isValidNis(policy: Predicate<in JavaNis>): Boolean = NisValidator.isValid(this, policy)
