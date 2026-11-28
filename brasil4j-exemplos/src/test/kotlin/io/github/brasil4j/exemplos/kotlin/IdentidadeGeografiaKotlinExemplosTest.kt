package io.github.brasil4j.exemplos.kotlin

import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.identidade.cnpj.CnpjType
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode
import io.github.brasil4j.identidade.composicao.DocumentCheck
import io.github.brasil4j.identidade.composicao.DocumentCheckResult
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.kotlin.geografia.CodigoMunicipio
import io.github.brasil4j.kotlin.geografia.toCodigoMunicipio
import io.github.brasil4j.kotlin.identidade.Cnpj
import io.github.brasil4j.kotlin.identidade.Cpf
import io.github.brasil4j.kotlin.identidade.Nis
import io.github.brasil4j.kotlin.identidade.isValidNis
import io.github.brasil4j.kotlin.identidade.toCnpj
import io.github.brasil4j.kotlin.identidade.toCpf
import io.github.brasil4j.kotlin.identidade.toNis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Exemplos idiomáticos completos em Kotlin para Identidade Brasileira e Geografia
 * utilizando as *value classes* e extensões de `brasil4j-kotlin`.
 */
@DisplayName("Exemplos Kotlin · Identidade e Geografia")
class IdentidadeGeografiaKotlinExemplosTest {

    @Test
    @DisplayName("CNPJ em Kotlin: value class, agrupamento de estabelecimentos por raiz e interoperabilidade Java")
    fun exemploCnpjValueClassAgrupamentoEInteropJava() {
        // 1. Construção por extensão .toCnpj() ou construtor direto Cnpj(...)
        val matrizAlfa = "12ABC34501DE35".toCnpj()
        val filialAlfa = Cnpj("12.ABC.345/02DE-06")
        val matrizNum = "11.222.333/0001-81".toCnpj(CnpjValidationMode.NUMERIC)

        assertEquals("12.ABC.345/01DE-35", matrizAlfa.applyMask())
        assertEquals("12ABC34501DE35", matrizAlfa.canonicalValue)
        assertEquals("12ABC34501DE", matrizAlfa.base)
        assertEquals("12ABC345", matrizAlfa.root)
        assertEquals("01DE", matrizAlfa.branch)
        assertEquals("35", matrizAlfa.checkDigits)
        assertEquals(CnpjType.ALPHANUMERIC, matrizAlfa.type)
        assertEquals(CnpjType.NUMERIC, matrizNum.type)

        // Modo FORMAT_ONLY para triagem sem conferência de DV:
        val rascunho = "12ABC34501DE99".toCnpj(CnpjValidationMode.FORMAT_ONLY)
        assertEquals("99", rascunho.checkDigits)
        assertThrows<IllegalArgumentException> { "12.ABC.345/01DE-35".toCnpj(CnpjValidationMode.NUMERIC) }

        // 2. Agrupamento idiomático por raiz empresarial usando coleções Kotlin:
        val porEmpresa = listOf(matrizAlfa, filialAlfa, matrizNum).groupBy { it.root }
        assertEquals(listOf("01DE", "02DE"), porEmpresa.getValue("12ABC345").map { it.branch })

        // 3. Ponte sem cópia para APIs Java via .toJava():
        assertEquals("12ABC34501DE35", matrizAlfa.toJava().canonicalValue)
    }

    @Test
    @DisplayName("CPF em Kotlin: partição de lote legado, modo estrito NUMERIC e ocultação em templates")
    fun exemploCpfParticaoDeLoteModoEstritoEOcultacao() {
        // 1. Particionamento de carga legada: .toCpf() aceita formato válido e hasValidCheckDigits() separa DVs íntegros
        val lote = listOf("280.012.389-38", "00012345601", "280.012.389-39").map { it.toCpf() }
        val (integros, divergentes) = lote.partition { it.hasValidCheckDigits() }

        assertEquals(2, integros.size)
        assertEquals(1, divergentes.size)

        // 2. Exigência estrita de módulo 11 em endpoints de cadastro novo:
        val cpf = "280.012.389-38".toCpf(CpfValidationMode.NUMERIC)
        assertEquals("280012389", cpf.base)
        assertEquals("38", cpf.checkDigits)
        assertEquals("280.012.389-38", cpf.applyMask())
        assertEquals(Cpf("28001238938").toJava(), cpf.toJava())
        assertThrows<IllegalArgumentException> { "280.012.389-39".toCpf(CpfValidationMode.NUMERIC) }

        // 3. Interpolação de string oculta os dígitos automaticamente:
        assertEquals("Auditoria titular=***.***.***-**", "Auditoria titular=$cpf")
    }

    @Test
    @DisplayName("NIS em Kotlin: receptor anulável em isValidNis, políticas do consumidor e formatação explícita")
    fun exemploNisReceptorAnulavelPoliticasEFormatacao() {
        // 1. isValidNis aceita String? diretamente sem exigir operador ?. do chamador, enquanto Nis(null) falha rápido:
        val entradaNula: String? = null
        assertFalse(entradaNula.isValidNis())
        assertThrows<IllegalArgumentException> { Nis(entradaNula) }
        assertFalse("000.00000.28-0".isValidNis())
        assertTrue("00000000280".isValidNis { it.canonicalValue.toSet().size > 1 })
        assertFalse("00000000000".isValidNis { it.canonicalValue.toSet().size > 1 })

        // 2. Value class Nis preserva zeros à esquerda, oculta toString() e delega formatação ao consumidor:
        val nis = "00000000280".toNis()
        assertEquals(Nis("00000000280"), nis)
        assertEquals("00000000280", nis.canonicalValue)
        assertEquals("00000000280", nis.toJava().canonicalValue)
        assertEquals("[NIS oculto]", "$nis")

        val formatado = nis.format { v ->
            val d = v.canonicalValue
            "${d.substring(0, 3)}.${d.substring(3, 8)}.${d.substring(8, 10)}-${d.substring(10)}"
        }
        assertEquals("000.00000.28-0", formatado)
    }

    @Test
    @DisplayName("Geografia em Kotlin: Código IBGE com UF anulável, operador Elvis e consultas sobre as 27 UFs")
    fun exemploGeografiaCodigoMunicipioConsultasDeUfEAgregacao() {
        val maringa = "4115200".toCodigoMunicipio()
        assertEquals(CodigoMunicipio("4115200"), maringa)
        assertEquals("4115200", maringa.canonicalValue)
        assertEquals("41", maringa.ufCode)
        assertEquals(Uf.PR, maringa.uf)
        assertTrue(maringa.matchesUf(Uf.PR))
        assertFalse(maringa.matchesUf(null))
        assertEquals("Curitiba", maringa.uf?.capital)
        assertEquals("4115200", maringa.toJava().canonicalValue)

        // Prefixo desconhecido devolve uf nula em Kotlin (permitindo fallback limpo com Elvis ?:):
        val desconhecido = "0012345".toCodigoMunicipio()
        assertNull(desconhecido.uf)
        val nomeUfOuPadrao = desconhecido.uf?.nome ?: "UF não mapeada"
        assertEquals("UF não mapeada", nomeUfOuPadrao)

        // Consultas diretas de UF e filtragem funcional por DDD compartilhado:
        val parana = Uf.fromName("  parana  ").orElse(null)
        assertEquals(Uf.PR, parana)
        assertEquals(listOf("41", "42", "43", "44", "45", "46"), parana?.ddds)

        val distritoFederal = Uf.fromIbgeCode("53").orElse(null)
        assertEquals("Brasília", distritoFederal?.capital)

        val ufsComDdd61 = Uf.entries.filter { "61" in it.ddds }
        assertEquals(listOf(Uf.GO, Uf.DF), ufsComDdd61)
    }

    @Test
    @DisplayName("DocumentCheck em Kotlin: fila heterogênea de documentos com conversão SAM e saída opcional")
    fun exemploDocumentCheckFilaHeterogeneaEmKotlin() {
        val fila: List<DocumentCheck<*>> = listOf(
            DocumentCheck.cpf("28001238938", CpfValidationMode.NUMERIC).withFormatter { it.applyMask() },
            DocumentCheck.nis("00000000280").withPolicy { it.canonicalValue.startsWith("000") },
            DocumentCheck.cnpj("12ABC34501DE99", CnpjValidationMode.ALPHANUMERIC)
        )

        val decisoes = fila.map { it.evaluate() }
        assertEquals(
            listOf(
                DocumentCheckResult.Status.ACCEPTED,
                DocumentCheckResult.Status.ACCEPTED,
                DocumentCheckResult.Status.REJECTED_BY_VALIDATION
            ),
            decisoes.map { it.status }
        )
        assertEquals("280.012.389-38", decisoes[0].formattedValue.orElse(null))
    }
}
