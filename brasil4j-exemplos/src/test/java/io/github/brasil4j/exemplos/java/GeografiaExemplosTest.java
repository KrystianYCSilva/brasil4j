package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.geografia.CodigoMunicipio;
import io.github.brasil4j.geografia.Uf;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemplos completos e casos de uso reais do contexto de Geografia em Java 8
 * ({@link CodigoMunicipio} de 7 dígitos do IBGE e metadados das 27 {@link Uf}s).
 */
@DisplayName("Exemplos Java · Geografia (Código IBGE de Município e UF)")
class GeografiaExemplosTest {

    @Test
    @DisplayName("Caso de uso NF-e/Cadastro: validação cruzada entre código IBGE municipal (cMun) e UF declarada")
    void exemploCodigoMunicipioValidacaoCruzadaComUfEmNotaFiscal() {
        // 1. Em leiautes fiscais (NF-e / CT-e / NFS-e), o prefixo de 2 dígitos de cMun deve coincidir com a UF do endereço:
        CodigoMunicipio saoPaulo = CodigoMunicipio.parse("3550308");
        CodigoMunicipio portoVelho = CodigoMunicipio.parse("1100205");

        assertEquals("3550308", saoPaulo.getCanonicalValue());
        assertEquals("3550308", saoPaulo.toString());
        assertEquals("35", saoPaulo.getUfCode());
        assertEquals(Optional.of(Uf.SP), saoPaulo.getUf());
        assertTrue(saoPaulo.matchesUf(Uf.SP));
        assertFalse(saoPaulo.matchesUf(Uf.RJ)); // detecta divergência entre cMun de SP e UF=RJ!
        assertFalse(saoPaulo.matchesUf(null));

        assertEquals(Optional.of(Uf.RO), portoVelho.getUf());
        assertEquals("Porto Velho", portoVelho.getUf().map(Uf::getCapital).orElse(""));

        // 2. Uso como chave imutável de cache/parametrização municipal (alíquota meramente ilustrativa):
        Map<CodigoMunicipio, BigDecimal> aliquotaIlustrativaPorMunicipio = new LinkedHashMap<>();
        aliquotaIlustrativaPorMunicipio.put(CodigoMunicipio.parse("4115200"), new BigDecimal("0.02"));
        assertEquals(new BigDecimal("0.02"), aliquotaIlustrativaPorMunicipio.get(CodigoMunicipio.parse("4115200")));

        // 3. Código estruturalmente válido (7 dígitos ASCII) com prefixo sem UF correspondente e entradas inválidas:
        CodigoMunicipio prefixoDesconhecido = CodigoMunicipio.parse("0012345");
        assertEquals("0012345", prefixoDesconhecido.getCanonicalValue());
        assertEquals("00", prefixoDesconhecido.getUfCode());
        assertFalse(prefixoDesconhecido.getUf().isPresent());

        assertThrows(IllegalArgumentException.class, () -> CodigoMunicipio.parse("355030")); // 6 dígitos
        assertThrows(IllegalArgumentException.class, () -> CodigoMunicipio.parse("3550308-0")); // com pontuação
    }

    @Test
    @DisplayName("UF: normalização de nomes vindos de planilhas/formulários e consulta de metadados oficiais")
    void exemploNormalizacaoDeUfEmImportacaoDePlanilhas() {
        // 1. Busca por nome completo tolera caixa mista, ausência de acentos e espaços nas extremidades:
        assertEquals(Optional.of(Uf.SP), Uf.fromName("  sao paulo "));
        assertEquals(Optional.of(Uf.ES), Uf.fromName("ESPIRITO SANTO"));
        assertEquals(Optional.of(Uf.MS), Uf.fromName("mato grosso do sul"));
        assertEquals(Optional.empty(), Uf.fromName("Estado Inexistente"));
        assertEquals(Optional.empty(), Uf.fromName(null));

        // 2. Busca exata pelo código IBGE de 2 dígitos e inspeção completa de propriedades:
        Uf parana = Uf.fromIbgeCode("41").orElseThrow(AssertionError::new);
        assertEquals("PR", parana.getSigla());
        assertEquals("41", parana.getIbgeCode());
        assertEquals("Paraná", parana.getNome());
        assertEquals("Curitiba", parana.getCapital());
        assertEquals(Arrays.asList("41", "42", "43", "44", "45", "46"), parana.getDdds());
        assertEquals(Optional.empty(), Uf.fromIbgeCode("99"));
    }

    @Test
    @DisplayName("Caso de uso Telecom/Cadastro: índice reverso de DDD Anatel para UFs atendidas (incluindo DDD compartilhado)")
    void exemploIndiceReversoDeDddParaUfsAtendidas() {
        assertEquals(27, Uf.values().length);

        Map<String, List<Uf>> ufsPorDdd = new LinkedHashMap<>();
        for (Uf uf : Uf.values()) {
            for (String ddd : uf.getDdds()) {
                ufsPorDdd.computeIfAbsent(ddd, d -> new ArrayList<>()).add(uf);
            }
        }

        // DDD 11 atende apenas SP; já o DDD 61 abrange tanto o Distrito Federal quanto municípios goianos do Entorno!
        assertEquals(Arrays.asList(Uf.SP), ufsPorDdd.get("11"));
        assertEquals(Arrays.asList(Uf.GO, Uf.DF), ufsPorDdd.get("61"));
    }
}
