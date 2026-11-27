package io.github.brasil4j.exemplos.kotlin

import io.github.brasil4j.financas.Dinheiro
import io.github.brasil4j.financas.Dolar
import io.github.brasil4j.financas.Euro
import io.github.brasil4j.financas.MetodoArredondamento
import io.github.brasil4j.financas.Moeda
import io.github.brasil4j.financas.PoliticaResiduo
import io.github.brasil4j.financas.QuantiaMonetaria
import io.github.brasil4j.financas.Real
import io.github.brasil4j.financas.TaxaCambio
import io.github.brasil4j.kotlin.financas.alocarPorPesos
import io.github.brasil4j.kotlin.financas.centavosDolares
import io.github.brasil4j.kotlin.financas.centavosEuros
import io.github.brasil4j.kotlin.financas.centavosReais
import io.github.brasil4j.kotlin.financas.div
import io.github.brasil4j.kotlin.financas.dolares
import io.github.brasil4j.kotlin.financas.dolaresInteiros
import io.github.brasil4j.kotlin.financas.emMoeda
import io.github.brasil4j.kotlin.financas.euros
import io.github.brasil4j.kotlin.financas.eurosInteiros
import io.github.brasil4j.kotlin.financas.minus
import io.github.brasil4j.kotlin.financas.plus
import io.github.brasil4j.kotlin.financas.reais
import io.github.brasil4j.kotlin.financas.reaisInteiros
import io.github.brasil4j.kotlin.financas.times
import io.github.brasil4j.kotlin.financas.unaryMinus
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Exemplos idiomáticos completos em Kotlin para o contexto de Finanças
 * utilizando todas as extensões construtoras, operadores e rateios de `brasil4j-kotlin`.
 */
@DisplayName("Exemplos Kotlin · Finanças (Construtores, Operadores, Rateio e Carteira Multimoeda)")
class FinancasKotlinExemplosTest {

    @Test
    @DisplayName("Todas as extensões construtoras fluentes em Real, Dólar, Euro e moedas abertas")
    fun exemploTodasAsExtensoesConstrutorasFluentes() {
        // 1. Real (String, BigDecimal, Int, Long e centavos):
        assertEquals("10.50".reais(), BigDecimal("10.50").reais())
        assertEquals(10.reaisInteiros(), 10L.reaisInteiros())
        assertEquals("10.50".reais(), 1050L.centavosReais())

        // 2. Dólar (String, BigDecimal, Int, Long e centavos):
        assertEquals("25.75".dolares(), BigDecimal("25.75").dolares())
        assertEquals(25.dolaresInteiros(), 25L.dolaresInteiros())
        assertEquals("25.75".dolares(), 2575L.centavosDolares())

        // 3. Euro (String, BigDecimal, Int, Long e centavos):
        assertEquals("40.20".euros(), BigDecimal("40.20").euros())
        assertEquals(40.eurosInteiros(), 40L.eurosInteiros())
        assertEquals("40.20".euros(), 4020L.centavosEuros())

        // 4. Extensão polimórfica .emMoeda(...) devolve automaticamente o subtipo canônico de Dinheiro:
        val comoBrl: Dinheiro = "99.90".emMoeda(Moeda.BRL)
        assertTrue(comoBrl is Real)

        val kwd = Moeda.iso("KWD", 3)
        val comoKwd = BigDecimal("1.250").emMoeda(kwd, MetodoArredondamento.FINANCEIRO)
        assertTrue(comoKwd is QuantiaMonetaria)
        assertEquals("KWD 1.250", "$comoKwd")
    }

    @Test
    @DisplayName("Operadores aritméticos (+, -, * comutativo, /, - unário) e relacionais em todas as moedas")
    fun exemploOperadoresAritmeticosERelacionaisEmTodasAsMoedas() {
        val subtotal = "250.00".reais()
        val frete = 35.reaisInteiros()
        val desconto = 1550L.centavosReais()

        val totalPedido = subtotal + frete - desconto
        assertEquals("269.50".reais(), totalPedido)

        // Multiplicação comutativa (Int/Long * Money e Money * Int/Long) e por fator BigDecimal:
        assertEquals("539.00".reais(), 2 * totalPedido)
        assertEquals("539.00".reais(), 2L * totalPedido)
        assertEquals("539.00".reais(), totalPedido * 2)
        assertEquals("539.00".reais(), totalPedido * 2L)
        assertEquals("296.45".reais(), totalPedido * BigDecimal("1.10"))

        // Mesmos operadores disponíveis em Dolar, Euro, QuantiaMonetaria e no supertipo Dinheiro:
        val usd = 3 * "15.00".dolares() - "5.00".dolares()
        assertEquals("20.00".dolares(), usd / 2)
        assertEquals("-20.00".dolares(), -(usd / 2))

        val eur = 2 * "30.00".euros() + 15.eurosInteiros()
        assertEquals("25.00".euros(), eur / 3L)

        val jpy = Moeda.iso("JPY", 0)
        val ienes = QuantiaMonetaria.de("1000", jpy)
        assertEquals(QuantiaMonetaria.de("2500", jpy), 3 * ienes - QuantiaMonetaria.de("500", jpy))

        // No supertipo polimórfico Dinheiro também funcionam +, -, * comutativo, * BigDecimal, / e - unário:
        val polimorfico: Dinheiro = "40.00".reais()
        assertEquals("60.00".reais(), (2 * polimorfico + (polimorfico * 2L)) / 2 - ("10.00".reais() as Dinheiro) * BigDecimal("2.0"))
        assertEquals("-40.00".reais(), -polimorfico)

        // Divisão respeitando a política retida, comparação relacional (<, >) e ordenação Kotlin:
        val tercoAbnt = "100.00".reais(MetodoArredondamento.ABNT_NBR_5891) / 3
        val tercoTeto = "100.00".reais(MetodoArredondamento.TETO) / 3
        assertTrue(tercoTeto > tercoAbnt)
        assertEquals("33.34".reais(), tercoTeto.coerceIn("0.00".reais(), "50.00".reais()))
    }

    @Test
    @DisplayName("Caso de uso E-commerce: fechamento de carrinho com rateio de cupom (vararg) e parcelamento")
    fun exemploCarrinhoDeComprasComRateioDeCupomEParcelamento() {
        val itens = listOf("120.00".reais(), "80.00".reais(), "50.00".reais())
        val bruto = itens.fold(Real.ZERO) { acc, item -> acc + item } // BRL 250.00

        // Rateia um cupom de R$ 30,00 proporcionalmente ao valor em centavos de cada item (e peso 0 para brinde):
        val cupom = "30.00".reais()
        val descontosPorItem = cupom.alocarPorPesos(
            0L,
            itens[0].centavos,
            itens[1].centavos,
            itens[2].centavos
        )
        assertEquals(
            listOf(Real.ZERO, "14.40".reais(), "9.60".reais(), "6.00".reais()),
            descontosPorItem
        )

        val liquido = bruto - cupom // BRL 220.00
        val parcelas = liquido.ratear(3, PoliticaResiduo.ULTIMAS_PARCELAS)
        assertEquals(listOf("73.33".reais(), "73.33".reais(), "73.34".reais()), parcelas)
        assertEquals(liquido, parcelas.fold(Real.ZERO) { acc, p -> acc + p })

        // alocarPorPesos(vararg) também está disponível em Dolar, Euro, QuantiaMonetaria e Dinheiro:
        assertEquals(listOf("60.00".dolares(), "40.00".dolares()), "100.00".dolares().alocarPorPesos(3, 2))
        assertEquals(listOf("70.00".euros(), "30.00".euros()), "100.00".euros().alocarPorPesos(7, 3))
        val iene = Moeda.iso("JPY", 0)
        assertEquals(
            listOf(QuantiaMonetaria.de("500", iene), QuantiaMonetaria.de("500", iene)),
            QuantiaMonetaria.de("1000", iene).alocarPorPesos(1, 1)
        )
        val generico: Dinheiro = "10.00".reais()
        assertEquals(listOf("5.00".reais(), "5.00".reais()), generico.alocarPorPesos(1, 1))
    }

    @Test
    @DisplayName("Caso de uso Tesouraria em Kotlin: consolidação de carteira multimoeda usando when polimórfico")
    fun exemploConsolidacaoDeCarteiraMultimoedaComWhenPolimorfico() {
        val ptaxUsd = TaxaCambio.umDolarEmReais("5.4832")
        val ptaxEur = TaxaCambio.umEuroEmReais("6.1000")
        val iene = Moeda.iso("JPY", 0)
        val jpyBrl = TaxaCambio.de(iene, Moeda.BRL, "0.0365")

        val posicoes: List<Dinheiro> = listOf(
            "1000.00".reais(),
            "100.00".dolares(),
            "50.00".euros(),
            "10000".emMoeda(iene)
        )

        val consolidadoEmBrl = posicoes.fold(Real.ZERO) { soma, posicao ->
            val convertido = when (posicao) {
                is Real -> posicao
                is Dolar -> posicao.paraReal(ptaxUsd)
                is Euro -> posicao.paraReal(ptaxEur)
                is QuantiaMonetaria -> posicao.converter(jpyBrl) as Real
                else -> error("Subtipo inesperado")
            }
            soma + convertido
        }

        // 1000.00 + 548.32 + 305.00 + 365.00 = BRL 2218.32
        assertEquals("2218.32".reais(), consolidadoEmBrl)
    }

    @Test
    @DisplayName("Fronteiras de segurança: rejeição explícita de casas extras, divisão por zero e moedas distintas")
    fun exemploFronteirasDeSegurancaEmKotlin() {
        assertThrows<IllegalArgumentException> { Real.exato("150.555") }
        assertThrows<IllegalArgumentException> { "100.00".reais() / 0 }

        val emBrl: Dinheiro = "10.00".reais()
        val emUsd: Dinheiro = "10.00".dolares()
        assertThrows<IllegalArgumentException> { emBrl + emUsd }
        assertThrows<IllegalArgumentException> { emBrl - emUsd }

        val jpy = QuantiaMonetaria.de("100", Moeda.iso("JPY", 0))
        val kwd = QuantiaMonetaria.de("1.000", Moeda.iso("KWD", 3))
        assertThrows<IllegalArgumentException> { jpy.compareTo(kwd) }
    }
}
