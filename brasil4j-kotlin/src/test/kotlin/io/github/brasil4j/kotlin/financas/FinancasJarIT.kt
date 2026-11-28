package io.github.brasil4j.kotlin.financas

import io.github.brasil4j.financas.Moeda
import io.github.brasil4j.financas.Real
import io.github.brasil4j.financas.TaxaCambio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FinancasJarIT {

    @Test
    fun consomeExtensoesKotlinENucleoPelosJarsEmpacotados() {
        val ktFacade = Class.forName("io.github.brasil4j.kotlin.financas.FinancasKt")
        assertTrue(ktFacade.protectionDomain.codeSource.location.path.endsWith(".jar"))
        assertTrue(Real::class.java.protectionDomain.codeSource.location.path.endsWith(".jar"))

        val total = "100.00".reais()
        assertEquals(
            listOf("33.34".reais(), "33.33".reais(), "33.33".reais()),
            total.ratear(3)
        )
        assertEquals("200.00".reais(), 2 * total)

        val ptax = TaxaCambio.umDolarEmReais("5.4832")
        assertEquals("100.00".dolares(), "548.32".reais().paraDolar(ptax))

        val jpy = Moeda.iso("JPY", 0)
        val ienes = "100".emMoeda(jpy)
        assertEquals("300".emMoeda(jpy), 3 * ienes)

        // Spec 011 pelos JARs empacotados:
        val p = "17.5".porCento()
        assertEquals("1000.00".reais(), "825.00".reais().basePorDentro(p))
        val calc = calcularReais { mc -> java.math.BigDecimal("100").divide(java.math.BigDecimal("3"), mc) }
        assertEquals("33.33".reais(), calc)
        val tab = tabelaProgressivaReais {
            faixaAte("1000.00", io.github.brasil4j.financas.Percentual.ZERO)
            faixaAcima(10.porCento())
        }
        assertEquals("50.00".reais(), tab.apurar("1500.00".reais()).valorTotal)
    }
}
