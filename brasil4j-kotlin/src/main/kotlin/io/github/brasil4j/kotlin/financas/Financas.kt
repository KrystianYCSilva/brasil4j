package io.github.brasil4j.kotlin.financas

import io.github.brasil4j.financas.Dinheiro
import io.github.brasil4j.financas.Dolar
import io.github.brasil4j.financas.Euro
import io.github.brasil4j.financas.MetodoArredondamento
import io.github.brasil4j.financas.Moeda
import io.github.brasil4j.financas.QuantiaMonetaria
import io.github.brasil4j.financas.Real
import java.math.BigDecimal

/**
 * Interpreta este texto decimal canônico como quantia em [Real], aplicando e retendo [arredondamento].
 */
public fun String.reais(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Real =
    Real.de(this, arredondamento)

/**
 * Converte este [BigDecimal] em quantia de [Real], aplicando e retendo [arredondamento].
 */
public fun BigDecimal.reais(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Real =
    Real.de(this, arredondamento)

/**
 * Constrói quantia em [Real] a partir de unidades inteiras de Reais (ex.: `1050L.reaisInteiros()` = `BRL 1050.00`).
 */
public fun Long.reaisInteiros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Real =
    Real.deInteiro(this, arredondamento)

/**
 * Constrói quantia em [Real] a partir de unidades inteiras de Reais (ex.: `1050.reaisInteiros()` = `BRL 1050.00`).
 */
public fun Int.reaisInteiros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Real =
    Real.deInteiro(this.toLong(), arredondamento)

/**
 * Constrói quantia em [Real] a partir do total inteiro de centavos (ex.: `1050L.centavosReais()` = `BRL 10.50`).
 */
public fun Long.centavosReais(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Real =
    Real.deCentavos(this, arredondamento)

/**
 * Interpreta este texto decimal canônico como quantia em [Dolar], aplicando e retendo [arredondamento].
 */
public fun String.dolares(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Dolar =
    Dolar.de(this, arredondamento)

/**
 * Converte este [BigDecimal] em quantia de [Dolar], aplicando e retendo [arredondamento].
 */
public fun BigDecimal.dolares(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Dolar =
    Dolar.de(this, arredondamento)

/**
 * Constrói quantia em [Dolar] a partir de unidades inteiras de Dólar.
 */
public fun Long.dolaresInteiros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Dolar =
    Dolar.deInteiro(this, arredondamento)

/**
 * Constrói quantia em [Dolar] a partir de unidades inteiras de Dólar.
 */
public fun Int.dolaresInteiros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Dolar =
    Dolar.deInteiro(this.toLong(), arredondamento)

/**
 * Constrói quantia em [Dolar] a partir do total inteiro de cents.
 */
public fun Long.centavosDolares(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Dolar =
    Dolar.deCentavos(this, arredondamento)

/**
 * Interpreta este texto decimal canônico como quantia em [Euro], aplicando e retendo [arredondamento].
 */
public fun String.euros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Euro =
    Euro.de(this, arredondamento)

/**
 * Converte este [BigDecimal] em quantia de [Euro], aplicando e retendo [arredondamento].
 */
public fun BigDecimal.euros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Euro =
    Euro.de(this, arredondamento)

/**
 * Constrói quantia em [Euro] a partir de unidades inteiras de Euro.
 */
public fun Long.eurosInteiros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Euro =
    Euro.deInteiro(this, arredondamento)

/**
 * Constrói quantia em [Euro] a partir de unidades inteiras de Euro.
 */
public fun Int.eurosInteiros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Euro =
    Euro.deInteiro(this.toLong(), arredondamento)

/**
 * Constrói quantia em [Euro] a partir do total inteiro de centavos de Euro.
 */
public fun Long.centavosEuros(arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891): Euro =
    Euro.deCentavos(this, arredondamento)

/**
 * Interpreta este texto decimal na [moeda] informada, devolvendo o subtipo canônico de [Dinheiro].
 */
public fun String.emMoeda(
    moeda: Moeda,
    arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891
): Dinheiro = Dinheiro.de(this, moeda, arredondamento)

/**
 * Converte este [BigDecimal] na [moeda] informada, devolvendo o subtipo canônico de [Dinheiro].
 */
public fun BigDecimal.emMoeda(
    moeda: Moeda,
    arredondamento: MetodoArredondamento = MetodoArredondamento.ABNT_NBR_5891
): Dinheiro = Dinheiro.de(this, moeda, arredondamento)

/** Soma dois valores em [Real] preservando a política de arredondamento do receptor. */
public operator fun Real.plus(outro: Real): Real = this.somar(outro)

/** Subtrai [outro] deste valor em [Real] preservando a política de arredondamento do receptor. */
public operator fun Real.minus(outro: Real): Real = this.subtrair(outro)

/** Inverte o sinal deste valor em [Real]. */
public operator fun Real.unaryMinus(): Real = this.negar()

/** Multiplica de forma exata este valor em [Real] por [fator] inteiro. */
public operator fun Real.times(fator: Long): Real = this.multiplicar(fator)

/** Multiplica de forma exata este valor em [Real] por [fator] inteiro. */
public operator fun Real.times(fator: Int): Real = this.multiplicar(fator.toLong())

/** Multiplica de forma exata [valor] em [Real] por este inteiro. */
public operator fun Long.times(valor: Real): Real = valor.multiplicar(this)

/** Multiplica de forma exata [valor] em [Real] por este inteiro. */
public operator fun Int.times(valor: Real): Real = valor.multiplicar(this.toLong())

/** Multiplica este valor em [Real] por [fator] decimal usando a política retida na instância. */
public operator fun Real.times(fator: BigDecimal): Real = this.multiplicar(fator)

/** Divide este valor em [Real] por [divisor] inteiro usando a política retida na instância. */
public operator fun Real.div(divisor: Long): Real = this.dividir(divisor)

/** Divide este valor em [Real] por [divisor] inteiro usando a política retida na instância. */
public operator fun Real.div(divisor: Int): Real = this.dividir(divisor.toLong())

/** Aloca proporcionalmente este valor em [Real] pelos [pesos] inteiros informados (Hamilton). */
public fun Real.alocarPorPesos(vararg pesos: Long): List<Real> =
    this.alocar(pesos.map { BigDecimal.valueOf(it) })

/** Soma dois valores em [Dolar] preservando a política de arredondamento do receptor. */
public operator fun Dolar.plus(outro: Dolar): Dolar = this.somar(outro)

/** Subtrai [outro] deste valor em [Dolar] preservando a política de arredondamento do receptor. */
public operator fun Dolar.minus(outro: Dolar): Dolar = this.subtrair(outro)

/** Inverte o sinal deste valor em [Dolar]. */
public operator fun Dolar.unaryMinus(): Dolar = this.negar()

/** Multiplica de forma exata este valor em [Dolar] por [fator] inteiro. */
public operator fun Dolar.times(fator: Long): Dolar = this.multiplicar(fator)

/** Multiplica de forma exata este valor em [Dolar] por [fator] inteiro. */
public operator fun Dolar.times(fator: Int): Dolar = this.multiplicar(fator.toLong())

/** Multiplica de forma exata [valor] em [Dolar] por este inteiro. */
public operator fun Long.times(valor: Dolar): Dolar = valor.multiplicar(this)

/** Multiplica de forma exata [valor] em [Dolar] por este inteiro. */
public operator fun Int.times(valor: Dolar): Dolar = valor.multiplicar(this.toLong())

/** Multiplica este valor em [Dolar] por [fator] decimal usando a política retida na instância. */
public operator fun Dolar.times(fator: BigDecimal): Dolar = this.multiplicar(fator)

/** Divide este valor em [Dolar] por [divisor] inteiro usando a política retida na instância. */
public operator fun Dolar.div(divisor: Long): Dolar = this.dividir(divisor)

/** Divide este valor em [Dolar] por [divisor] inteiro usando a política retida na instância. */
public operator fun Dolar.div(divisor: Int): Dolar = this.dividir(divisor.toLong())

/** Aloca proporcionalmente este valor em [Dolar] pelos [pesos] inteiros informados (Hamilton). */
public fun Dolar.alocarPorPesos(vararg pesos: Long): List<Dolar> =
    this.alocar(pesos.map { BigDecimal.valueOf(it) })

/** Soma dois valores em [Euro] preservando a política de arredondamento do receptor. */
public operator fun Euro.plus(outro: Euro): Euro = this.somar(outro)

/** Subtrai [outro] deste valor em [Euro] preservando a política de arredondamento do receptor. */
public operator fun Euro.minus(outro: Euro): Euro = this.subtrair(outro)

/** Inverte o sinal deste valor em [Euro]. */
public operator fun Euro.unaryMinus(): Euro = this.negar()

/** Multiplica de forma exata este valor em [Euro] por [fator] inteiro. */
public operator fun Euro.times(fator: Long): Euro = this.multiplicar(fator)

/** Multiplica de forma exata este valor em [Euro] por [fator] inteiro. */
public operator fun Euro.times(fator: Int): Euro = this.multiplicar(fator.toLong())

/** Multiplica de forma exata [valor] em [Euro] por este inteiro. */
public operator fun Long.times(valor: Euro): Euro = valor.multiplicar(this)

/** Multiplica de forma exata [valor] em [Euro] por este inteiro. */
public operator fun Int.times(valor: Euro): Euro = valor.multiplicar(this.toLong())

/** Multiplica este valor em [Euro] por [fator] decimal usando a política retida na instância. */
public operator fun Euro.times(fator: BigDecimal): Euro = this.multiplicar(fator)

/** Divide este valor em [Euro] por [divisor] inteiro usando a política retida na instância. */
public operator fun Euro.div(divisor: Long): Euro = this.dividir(divisor)

/** Divide este valor em [Euro] por [divisor] inteiro usando a política retida na instância. */
public operator fun Euro.div(divisor: Int): Euro = this.dividir(divisor.toLong())

/** Aloca proporcionalmente este valor em [Euro] pelos [pesos] inteiros informados (Hamilton). */
public fun Euro.alocarPorPesos(vararg pesos: Long): List<Euro> =
    this.alocar(pesos.map { BigDecimal.valueOf(it) })

/** Soma duas instâncias de [QuantiaMonetaria] exigindo a mesma moeda. */
public operator fun QuantiaMonetaria.plus(outro: QuantiaMonetaria): QuantiaMonetaria = this.somar(outro)

/** Subtrai [outro] desta [QuantiaMonetaria] exigindo a mesma moeda. */
public operator fun QuantiaMonetaria.minus(outro: QuantiaMonetaria): QuantiaMonetaria = this.subtrair(outro)

/** Inverte o sinal desta [QuantiaMonetaria]. */
public operator fun QuantiaMonetaria.unaryMinus(): QuantiaMonetaria = this.negar()

/** Multiplica de forma exata esta [QuantiaMonetaria] por [fator] inteiro. */
public operator fun QuantiaMonetaria.times(fator: Long): QuantiaMonetaria = this.multiplicar(fator)

/** Multiplica de forma exata esta [QuantiaMonetaria] por [fator] inteiro. */
public operator fun QuantiaMonetaria.times(fator: Int): QuantiaMonetaria = this.multiplicar(fator.toLong())

/** Multiplica de forma exata [valor] por este inteiro. */
public operator fun Long.times(valor: QuantiaMonetaria): QuantiaMonetaria = valor.multiplicar(this)

/** Multiplica de forma exata [valor] por este inteiro. */
public operator fun Int.times(valor: QuantiaMonetaria): QuantiaMonetaria = valor.multiplicar(this.toLong())

/** Multiplica esta [QuantiaMonetaria] por [fator] decimal usando a política retida na instância. */
public operator fun QuantiaMonetaria.times(fator: BigDecimal): QuantiaMonetaria = this.multiplicar(fator)

/** Divide esta [QuantiaMonetaria] por [divisor] inteiro usando a política retida na instância. */
public operator fun QuantiaMonetaria.div(divisor: Long): QuantiaMonetaria = this.dividir(divisor)

/** Divide esta [QuantiaMonetaria] por [divisor] inteiro usando a política retida na instância. */
public operator fun QuantiaMonetaria.div(divisor: Int): QuantiaMonetaria = this.dividir(divisor.toLong())

/** Aloca proporcionalmente esta [QuantiaMonetaria] pelos [pesos] inteiros informados (Hamilton). */
public fun QuantiaMonetaria.alocarPorPesos(vararg pesos: Long): List<QuantiaMonetaria> =
    this.alocar(pesos.map { BigDecimal.valueOf(it) })

/** Soma dois valores polimórficos de [Dinheiro] exigindo a mesma moeda contábil. */
public operator fun Dinheiro.plus(outro: Dinheiro): Dinheiro = this.somarMesmaMoeda(outro)

/** Subtrai [outro] deste [Dinheiro] exigindo a mesma moeda contábil. */
public operator fun Dinheiro.minus(outro: Dinheiro): Dinheiro = this.subtrairMesmaMoeda(outro)

/** Inverte o sinal deste [Dinheiro]. */
public operator fun Dinheiro.unaryMinus(): Dinheiro = this.negar()

/** Multiplica de forma exata este [Dinheiro] por [fator] inteiro. */
public operator fun Dinheiro.times(fator: Long): Dinheiro = this.multiplicar(fator)

/** Multiplica de forma exata este [Dinheiro] por [fator] inteiro. */
public operator fun Dinheiro.times(fator: Int): Dinheiro = this.multiplicar(fator.toLong())

/** Multiplica de forma exata [valor] de [Dinheiro] por este inteiro. */
public operator fun Long.times(valor: Dinheiro): Dinheiro = valor.multiplicar(this)

/** Multiplica de forma exata [valor] de [Dinheiro] por este inteiro. */
public operator fun Int.times(valor: Dinheiro): Dinheiro = valor.multiplicar(this.toLong())

/** Multiplica este [Dinheiro] por [fator] decimal usando a política retida na instância. */
public operator fun Dinheiro.times(fator: BigDecimal): Dinheiro = this.multiplicar(fator)

/** Divide este [Dinheiro] por [divisor] inteiro usando a política retida na instância. */
public operator fun Dinheiro.div(divisor: Long): Dinheiro = this.dividir(divisor)

/** Divide este [Dinheiro] por [divisor] inteiro usando a política retida na instância. */
public operator fun Dinheiro.div(divisor: Int): Dinheiro = this.dividir(divisor.toLong())

/** Aloca proporcionalmente este [Dinheiro] pelos [pesos] inteiros informados (Hamilton). */
public fun Dinheiro.alocarPorPesos(vararg pesos: Long): List<Dinheiro> =
    this.alocar(pesos.map { BigDecimal.valueOf(it) })
