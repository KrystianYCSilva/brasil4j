package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.nis.Nis as JavaNis
import io.github.brasil4j.identidade.nis.NisValidationMode
import java.util.Objects
import java.util.function.Function
import java.util.function.Predicate

/** Inscrição estrutural baseada no valor imutável do núcleo Java. */
@JvmInline
public value class Nis private constructor(private val delegate: JavaNis) {
    /**
     * Confere a apresentação literal de [input], com exatamente 11 dígitos ASCII.
     *
     * @throws IllegalArgumentException se a entrada for nula ou tiver formato inválido
     */
    public constructor(input: String?) : this(JavaNis.parse(input))

    /** Obtém explicitamente os 11 dígitos na ordem recebida, inclusive zeros iniciais. */
    public val canonicalValue: String get() = delegate.canonicalValue

    /** Devolve o valor imutável do núcleo para comparação explícita com Java. */
    public fun toJava(): JavaNis = delegate

    /**
     * Aplica a apresentação escolhida em [formatter] ao valor estrutural Java.
     * O resultado pode expor a inscrição e as exceções da função propagam-se ao chamador.
     *
     * @throws NullPointerException se a função produzir nulo
     */
    public fun format(formatter: Function<in JavaNis, String>): String =
        Objects.requireNonNull(formatter.apply(delegate), "Resultado de formatação do NIS obrigatório.")

    /**
     * Devolve a máscara de segurança sobre os 11 dígitos, sem pontuação, ocultando os três primeiros
     * e o último com [maskChar].
     *
     * @throws IllegalArgumentException se [maskChar] for letra ou dígito
     */
    public fun applySecurityMask(maskChar: Char = '*'): String = delegate.applySecurityMask(maskChar)

    /** Devolve o marcador fixo de ocultação, sem dígitos da inscrição. */
    override fun toString(): String = delegate.toString()

    internal companion object {
        internal fun of(delegate: JavaNis): Nis = Nis(delegate)
    }
}

/**
 * Interpreta o texto literal como uma inscrição estrutural.
 *
 * @throws IllegalArgumentException se a entrada tiver formato inválido
 */
public fun String.toNis(): Nis = Nis(this)

/**
 * Interpreta o texto como inscrição na configuração [mode], que pode acrescentar um validador de DV
 * nomeado pela fonte.
 *
 * @throws IllegalArgumentException se a entrada for rejeitada
 */
public fun String.toNis(mode: NisValidationMode): Nis = Nis.of(JavaNis.parser(mode).parseOrThrow(this))

/** Interpreta o texto como inscrição na configuração [mode], ou devolve `null` para entrada ausente ou rejeitada. */
public fun String?.toNisOrNull(mode: NisValidationMode = NisValidationMode.FORMAT_ONLY): Nis? =
    JavaNis.parser(mode).parseOrNull(this)?.let(Nis::of)

/** Confere se o texto tem exatamente 11 dígitos ASCII. Devolve `false` para nulo. */
public fun String?.isValidNis(): Boolean = JavaNis.parser().isValid(this)

/**
 * Confere a forma e aplica [policy] uma vez ao valor estrutural Java.
 * A política é executada somente para entrada canônica e suas exceções são propagadas.
 */
public fun String?.isValidNis(policy: Predicate<in JavaNis>): Boolean = JavaNis.parser().withPolicy(policy).isValid(this)
