package io.github.brasil4j.exemplos.kotlin

import io.github.brasil4j.financas.MetodoArredondamento
import io.github.brasil4j.financas.PoliticaResiduo
import io.github.brasil4j.financas.Real
import io.github.brasil4j.financas.TaxaCambio
import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.kotlin.financas.alocarPorPesos
import io.github.brasil4j.kotlin.financas.dolares
import io.github.brasil4j.kotlin.financas.euros
import io.github.brasil4j.kotlin.financas.minus
import io.github.brasil4j.kotlin.financas.plus
import io.github.brasil4j.kotlin.financas.reais
import io.github.brasil4j.kotlin.geografia.CodigoMunicipio
import io.github.brasil4j.kotlin.geografia.toCodigoMunicipio
import io.github.brasil4j.kotlin.identidade.Cnpj
import io.github.brasil4j.kotlin.identidade.Cpf
import io.github.brasil4j.kotlin.identidade.Nis
import io.github.brasil4j.kotlin.identidade.toCnpj
import io.github.brasil4j.kotlin.identidade.toCpf
import io.github.brasil4j.kotlin.identidade.toNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Cenários integrados ponta a ponta em Kotlin demonstrando o uso conjunto de *value classes*
 * (`Cnpj`, `Cpf`, `Nis`, `CodigoMunicipio`) e operadores monetários em modelos de domínio imutáveis.
 */
@DisplayName("Exemplos Kotlin · Cenários Integrados de Domínio")
class CenariosIntegradosKotlinExemplosTest {

    private data class PedidoInterestadual(
        val emitente: Cnpj,
        val municipioOrigem: CodigoMunicipio,
        val comprador: Cpf,
        val municipioDestino: CodigoMunicipio,
        val itens: List<Real>,
        val frete: Real
    ) {
        val interestadual: Boolean
            get() {
                val uo = municipioOrigem.uf
                val ud = municipioDestino.uf
                return uo != null && ud != null && uo != ud
            }

        val total: Real
            get() = itens.fold(Real.ZERO) { acc, item -> acc + item } + frete
    }

    @Test
    @DisplayName("Data class de domínio combinando CNPJ, CPF, Município/UF, rateio de frete e ocultação segura em toString()")
    fun cenarioDataClassPedidoInterestadualSeguroPorPadrao() {
        val pedido = PedidoInterestadual(
            emitente = "12.ABC.345/01DE-35".toCnpj(),
            municipioOrigem = "3550308".toCodigoMunicipio(), // São Paulo/SP
            comprador = "280.012.389-38".toCpf(CpfValidationMode.NUMERIC),
            municipioDestino = "5300108".toCodigoMunicipio(), // Brasília/DF
            itens = listOf("300.00".reais(), "200.00".reais()),
            frete = "50.00".reais()
        )

        assertTrue(pedido.interestadual)
        assertEquals(Uf.SP, pedido.municipioOrigem.uf)
        assertEquals(Uf.DF, pedido.municipioDestino.uf)
        assertEquals("550.00".reais(), pedido.total)

        // Rateio proporcional do frete entre os itens usando pesos em centavos:
        val fretePorItem = pedido.frete.alocarPorPesos(*pedido.itens.map { it.centavos }.toLongArray())
        assertEquals(listOf("30.00".reais(), "20.00".reais()), fretePorItem)

        // Parcelamento em 3x com centavo residual na última parcela:
        assertEquals(
            listOf("183.33".reais(), "183.33".reais(), "183.34".reais()),
            pedido.total.ratear(3, PoliticaResiduo.ULTIMAS_PARCELAS)
        )

        // Mesmo no toString() automático de uma data class Kotlin, o CPF jamais vaza em texto claro (nem com máscara):
        val dumpDaDataClass = pedido.toString()
        assertTrue("***.***.***-**" in dumpDaDataClass)
        assertTrue("28001238938" !in dumpDaDataClass)
        assertTrue("280.012.389-38" !in dumpDaDataClass)
    }

    @Test
    @DisplayName("Reembolso corporativo multimoeda com NIS oculto em data class de colaborador")
    fun cenarioReembolsoCorporativoComNisOculto() {
        data class Colaborador(val cpf: Cpf, val nis: Nis, val unidade: Cnpj)

        val colab = Colaborador(
            cpf = "000.123.456-01".toCpf(CpfValidationMode.NUMERIC),
            nis = "00000000280".toNis(),
            unidade = "11.222.333/0002-62".toCnpj()
        )

        // Nem CPF nem NIS vazam em logs estruturados da data class:
        assertTrue("***.***.***-**" in "$colab")
        assertTrue("[NIS oculto]" in "$colab")
        assertTrue("00012345601" !in "$colab")
        assertTrue("000.123.456-01" !in "$colab")
        assertTrue("00000000280" !in "$colab")

        val ptaxUsd = TaxaCambio.umDolarEmReais("5.4832")
        val ptaxEur = TaxaCambio.umEuroEmReais("6.1000")

        val liquido = "5000.00".reais() - "800.00".reais() +
            "100.00".dolares().paraReal(ptaxUsd, MetodoArredondamento.FINANCEIRO) +
            "50.00".euros().paraReal(ptaxEur, MetodoArredondamento.FINANCEIRO)

        assertEquals("5053.32".reais(), liquido)
    }
}
