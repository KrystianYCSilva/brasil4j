package io.github.brasil4j.kotlin.financas

import io.github.brasil4j.financas.Dinheiro
import io.github.brasil4j.financas.MetodoArredondamento
import io.github.brasil4j.financas.Moeda
import io.github.brasil4j.financas.QuantiaMonetaria
import io.github.brasil4j.financas.TaxaCambio
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FinancasTest {

    @Test
    fun `opera Real com construtores expressivos e operadores comutativos`() {
        val base = "100.00".reais()
        val acrescimo = BigDecimal("25.50").reais(MetodoArredondamento.FINANCEIRO)

        assertEquals("125.50".reais(), base + acrescimo)
        assertEquals("74.50".reais(), base - acrescimo)
        assertEquals("-100.00".reais(), -base)
        assertEquals("200.00".reais(), base * 2)
        assertEquals("200.00".reais(), 2 * base)
        assertEquals("300.00".reais(), base * 3L)
        assertEquals("300.00".reais(), 3L * base)
        assertEquals("105.00".reais(), base * BigDecimal("1.05"))
        assertEquals("33.33".reais(), base / 3)
        assertEquals("33.33".reais(), base / 3L)
        assertTrue(base > acrescimo)

        assertEquals("1050.00".reais(), 1050.reaisInteiros())
        assertEquals("1050.00".reais(), 1050L.reaisInteiros())
        assertEquals("10.50".reais(), 1050L.centavosReais())
        assertEquals(
            listOf("0.00".reais(), "33.34".reais(), "33.33".reais(), "33.33".reais()),
            base.alocarPorPesos(0, 1, 1, 1)
        )
    }

    @Test
    fun `opera Dolar, Euro, QuantiaMonetaria e Dinheiro polimorfico`() {
        val usd = "50.00".dolares()
        assertEquals("75.00".dolares(), usd + 25.dolaresInteiros())
        assertEquals("25.00".dolares(), usd - 25L.dolaresInteiros())
        assertEquals("-50.00".dolares(), -usd)
        assertEquals("100.00".dolares(), 2 * usd)
        assertEquals("100.00".dolares(), 2L * usd)
        assertEquals("100.00".dolares(), usd * 2L)
        assertEquals("100.00".dolares(), usd * 2)
        assertEquals("55.00".dolares(), usd * BigDecimal("1.10"))
        assertEquals("25.00".dolares(), usd / 2)
        assertEquals("25.00".dolares(), usd / 2L)
        assertEquals("10.50".dolares(), 1050L.centavosDolares())
        assertEquals("50.00".dolares(), BigDecimal("50").dolares())
        assertEquals(listOf("25.00".dolares(), "25.00".dolares()), usd.alocarPorPesos(1, 1))

        val eur = "60.00".euros()
        assertEquals("80.00".euros(), eur + 20.eurosInteiros())
        assertEquals("40.00".euros(), eur - 20L.eurosInteiros())
        assertEquals("-60.00".euros(), -eur)
        assertEquals("120.00".euros(), 2 * eur)
        assertEquals("120.00".euros(), 2L * eur)
        assertEquals("120.00".euros(), eur * 2L)
        assertEquals("120.00".euros(), eur * 2)
        assertEquals("66.00".euros(), eur * BigDecimal("1.10"))
        assertEquals("20.00".euros(), eur / 3)
        assertEquals("20.00".euros(), eur / 3L)
        assertEquals("10.50".euros(), 1050L.centavosEuros())
        assertEquals("60.00".euros(), BigDecimal("60").euros())
        assertEquals(listOf("30.00".euros(), "30.00".euros()), eur.alocarPorPesos(1, 1))

        val jpy = Moeda.iso("JPY", 0)
        val q1 = QuantiaMonetaria.de("100", jpy)
        val q2 = QuantiaMonetaria.de("50", jpy)
        assertEquals(QuantiaMonetaria.de("150", jpy), q1 + q2)
        assertEquals(QuantiaMonetaria.de("50", jpy), q1 - q2)
        assertEquals(QuantiaMonetaria.de("-100", jpy), -q1)
        assertEquals(QuantiaMonetaria.de("200", jpy), 2 * q1)
        assertEquals(QuantiaMonetaria.de("200", jpy), 2L * q1)
        assertEquals(QuantiaMonetaria.de("200", jpy), q1 * 2)
        assertEquals(QuantiaMonetaria.de("200", jpy), q1 * 2L)
        assertEquals(QuantiaMonetaria.de("150", jpy), q1 * BigDecimal("1.5"))
        assertEquals(QuantiaMonetaria.de("33", jpy), q1 / 3)
        assertEquals(QuantiaMonetaria.de("33", jpy), q1 / 3L)
        assertEquals(
            listOf(QuantiaMonetaria.de("34", jpy), QuantiaMonetaria.de("33", jpy), QuantiaMonetaria.de("33", jpy)),
            q1.alocarPorPesos(1, 1, 1)
        )

        val d1: Dinheiro = "40.00".emMoeda(Moeda.BRL)
        val d2: Dinheiro = BigDecimal("10.00").emMoeda(Moeda.BRL)
        assertEquals("50.00".reais(), d1 + d2)
        assertEquals("30.00".reais(), d1 - d2)
        assertEquals("-40.00".reais(), -d1)
        assertEquals("80.00".reais(), 2 * d1)
        assertEquals("80.00".reais(), 2L * d1)
        assertEquals("80.00".reais(), d1 * 2)
        assertEquals("80.00".reais(), d1 * 2L)
        assertEquals("44.00".reais(), d1 * BigDecimal("1.10"))
        assertEquals("20.00".reais(), d1 / 2)
        assertEquals("20.00".reais(), d1 / 2L)
        assertEquals(listOf("20.00".reais(), "20.00".reais()), d1.alocarPorPesos(1, 1))

        assertThrows(IllegalArgumentException::class.java) { d1 + usd }
        assertThrows(IllegalArgumentException::class.java) { usd.alocarPorPesos() }
        assertThrows(IllegalArgumentException::class.java) { d1.alocarPorPesos() }
    }

    @Test
    fun `converte moedas com TaxaCambio em Kotlin`() {
        val ptax = TaxaCambio.umDolarEmReais("5.4832")
        assertEquals("548.32".reais(), "100.00".dolares().paraReal(ptax))
        assertEquals("100.00".dolares(), "548.32".reais().paraDolar(ptax))
    }
}
