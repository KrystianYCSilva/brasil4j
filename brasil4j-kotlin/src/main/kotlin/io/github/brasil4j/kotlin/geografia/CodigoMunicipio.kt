package io.github.brasil4j.kotlin.geografia

import io.github.brasil4j.geografia.CodigoMunicipio as JavaCodigoMunicipio
import io.github.brasil4j.geografia.Uf

/**
 * Código estrutural de município do IBGE na API Kotlin.
 *
 * A projeção de UF considera o prefixo. Existência, vigência e dígito verificador
 * pertencem ao catálogo ou ao consumidor.
 */
@JvmInline
public value class CodigoMunicipio private constructor(private val delegate: JavaCodigoMunicipio) {
    /**
     * Interpreta [entrada] com exatamente sete dígitos ASCII.
     *
     * @throws IllegalArgumentException quando a apresentação é inválida.
     */
    public constructor(entrada: String) : this(JavaCodigoMunicipio.parse(entrada))

    /** Os sete dígitos do código, preservados como texto. */
    public val canonicalValue: String get() = delegate.canonicalValue

    /** Os dois primeiros dígitos, inclusive para um prefixo desconhecido. */
    public val ufCode: String get() = delegate.ufCode

    /** A unidade federativa do prefixo ou nulo para uma correspondência desconhecida. */
    public val uf: Uf? get() = delegate.uf.orElse(null)

    /** Confere se [uf] corresponde ao prefixo. UF nula retorna falso. */
    public fun matchesUf(uf: Uf?): Boolean = delegate.matchesUf(uf)

    /** Retorna o valor Java para interoperabilidade e comparação entre APIs. */
    public fun toJava(): JavaCodigoMunicipio = delegate

    /** Retorna os sete dígitos canônicos do código público. */
    override fun toString(): String = delegate.toString()
}

/**
 * Interpreta este texto como [CodigoMunicipio], preservando seus sete dígitos ASCII.
 *
 * @throws IllegalArgumentException quando a apresentação é inválida.
 */
public fun String.toCodigoMunicipio(): CodigoMunicipio = CodigoMunicipio(this)
