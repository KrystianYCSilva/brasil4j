package io.github.brasil4j.exemplos.kotlin

import io.github.brasil4j.financas.MetodoArredondamento
import io.github.brasil4j.financas.ModoApuracaoProgressiva
import io.github.brasil4j.financas.Percentual
import io.github.brasil4j.kotlin.financas.acrescentar
import io.github.brasil4j.kotlin.financas.aplicar
import io.github.brasil4j.kotlin.financas.basePorDentro
import io.github.brasil4j.kotlin.financas.calcularDolares
import io.github.brasil4j.kotlin.financas.calcularReais
import io.github.brasil4j.kotlin.financas.descontar
import io.github.brasil4j.kotlin.financas.dolares
import io.github.brasil4j.kotlin.financas.plus
import io.github.brasil4j.kotlin.financas.pontosBase
import io.github.brasil4j.kotlin.financas.porCento
import io.github.brasil4j.kotlin.financas.reais
import io.github.brasil4j.kotlin.financas.tabelaProgressivaReais
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Exemplos idiomáticos em Kotlin para `CalculadoraFinanceira`, `Percentual` e `TabelaProgressiva`
 * usando as extensões e DSLs de `brasil4j-kotlin`.
 */
@DisplayName("Exemplos Kotlin · Calculadora Financeira, Percentuais e Tabela Progressiva (DSL)")
class CalculadoraPercentualKotlinExemplosTest {

    @Test
    @DisplayName("DSL calcularReais e calcularDolares para fórmulas compostas em precisão DECIMAL128")
    fun exemploDslCalculadoraFinanceiraEmKotlin() {
        val proRata = calcularReais(MetodoArredondamento.FINANCEIRO) { mc ->
            BigDecimal("1250.00")
                .multiply(BigDecimal("17"), mc)
                .divide(BigDecimal("30"), mc)
        }
        assertEquals("708.33".reais(), proRata)

        val licencaAnualProporcional = calcularDolares { mc ->
            BigDecimal("299.00")
                .multiply(BigDecimal("7"), mc)
                .divide(BigDecimal("12"), mc)
        }
        assertEquals("174.42".dolares(), licencaAnualProporcional)
    }

    @Test
    @DisplayName("Extensões .porCento(), .pontosBase(), descontar, acrescentar e basePorDentro em Kotlin")
    fun exemploPercentualEGrossUpEmKotlin() {
        val aliquotaTotal = "15.5".porCento() + 200.pontosBase() // 17.5%
        assertEquals("17.5%", "$aliquotaTotal")

        val liquidoDesejado = "825.00".reais()
        val brutoComGrossUp = liquidoDesejado.basePorDentro(aliquotaTotal, MetodoArredondamento.FINANCEIRO)

        assertEquals("1000.00".reais(), brutoComGrossUp)
        assertEquals("175.00".reais(), brutoComGrossUp.aplicar(aliquotaTotal, MetodoArredondamento.FINANCEIRO))
        assertEquals(liquidoDesejado, brutoComGrossUp.descontar(aliquotaTotal, MetodoArredondamento.FINANCEIRO))
        assertEquals("1100.00".reais(), "1000.00".reais().acrescentar(10.porCento()))
    }

    @Test
    @DisplayName("DSL declarativo tabelaProgressivaReais para comissões escalonadas e faixas com teto")
    fun exemploDslTabelaProgressivaEmKotlin() {
        val comissaoEscalonada = tabelaProgressivaReais {
            faixaAte("5000.00", Percentual.ZERO)
            faixaAte("15000.00", 5.porCento())
            faixaAcima(8.porCento())
        }

        val apuracao = comissaoEscalonada.apurar("20000.00".reais(), ModoApuracaoProgressiva.NO_TOTAL)
        assertEquals("900.00".reais(), apuracao.valorTotal)
        assertEquals("4.5".porCento(), apuracao.getAliquotaEfetiva(2, MetodoArredondamento.FINANCEIRO))
    }
}
