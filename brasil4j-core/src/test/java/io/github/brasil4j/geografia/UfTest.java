package io.github.brasil4j.geografia;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UfTest {
    @ParameterizedTest
    @CsvSource({
            "RO,11", "AC,12", "AM,13", "RR,14", "PA,15", "AP,16", "TO,17",
            "MA,21", "PI,22", "CE,23", "RN,24", "PB,25", "PE,26", "AL,27", "SE,28", "BA,29",
            "MG,31", "ES,32", "RJ,33", "SP,35", "PR,41", "SC,42", "RS,43",
            "MS,50", "MT,51", "GO,52", "DF,53"
    })
    void resolveOsParesDaTabelaIbge(Uf uf, String codigo) {
        assertEquals(codigo, uf.getIbgeCode());
        assertEquals(Optional.of(uf), Uf.fromIbgeCode(codigo));
    }

    @Test
    void expoeAsVinteESeteUfsComCodigosUnicos() {
        assertEquals(27, Uf.values().length);
        assertEquals(27, Arrays.stream(Uf.values()).map(Uf::getIbgeCode).collect(Collectors.toSet()).size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"00", "34", "99", "SP", " 35", "+35", "035", "３５"})
    void rejeitaCodigosDesconhecidosEApresentacoesAlternativas(String codigo) {
        assertEquals(Optional.empty(), Uf.fromIbgeCode(codigo));
    }

    @ParameterizedTest
    @CsvSource({
            "RO,Rondônia,Porto Velho",
            "AC,Acre,Rio Branco",
            "AM,Amazonas,Manaus",
            "RR,Roraima,Boa Vista",
            "PA,Pará,Belém",
            "AP,Amapá,Macapá",
            "TO,Tocantins,Palmas",
            "MA,Maranhão,São Luís",
            "PI,Piauí,Teresina",
            "CE,Ceará,Fortaleza",
            "RN,Rio Grande do Norte,Natal",
            "PB,Paraíba,João Pessoa",
            "PE,Pernambuco,Recife",
            "AL,Alagoas,Maceió",
            "SE,Sergipe,Aracaju",
            "BA,Bahia,Salvador",
            "MG,Minas Gerais,Belo Horizonte",
            "ES,Espírito Santo,Vitória",
            "RJ,Rio de Janeiro,Rio de Janeiro",
            "SP,São Paulo,São Paulo",
            "PR,Paraná,Curitiba",
            "SC,Santa Catarina,Florianópolis",
            "RS,Rio Grande do Sul,Porto Alegre",
            "MS,Mato Grosso do Sul,Campo Grande",
            "MT,Mato Grosso,Cuiabá",
            "GO,Goiás,Goiânia",
            "DF,Distrito Federal,Brasília"
    })
    void expoeNomeOficialSiglaECapital(Uf uf, String nome, String capital) {
        assertEquals(nome, uf.getNome());
        assertEquals(uf.name(), uf.getSigla());
        assertEquals(capital, uf.getCapital());
        assertEquals(Optional.of(uf), Uf.fromName(nome));
    }

    @Test
    void encontraPeloNomeCompletoComToleranciaACaixaEAcentos() {
        assertEquals(Optional.of(Uf.SP), Uf.fromName("  sAo PAULO  "));
        assertEquals(Optional.of(Uf.SP), Uf.fromName("Sa\u0303o Paulo"));
        assertEquals(Optional.of(Uf.PR), Uf.fromName("P\u0308ara\u0301na\u0301"));
        assertEquals(Optional.of(Uf.DF), Uf.fromName("distrito federal"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"SP", "São", "São  Paulo", "35", " ", "\u00a0São Paulo", "Ｓão Paulo"})
    void buscaPorNomeRetornaVazioParaOutrasApresentacoes(String entrada) {
        assertEquals(Optional.empty(), Uf.fromName(entrada));
    }

    @ParameterizedTest
    @CsvSource({
            "RO,69",
            "AC,68",
            "AM,92 97",
            "RR,95",
            "PA,91 93 94",
            "AP,96",
            "TO,63",
            "MA,98 99",
            "PI,86 89",
            "CE,85 88",
            "RN,84",
            "PB,83",
            "PE,81 87",
            "AL,82",
            "SE,79",
            "BA,71 73 74 75 77",
            "MG,31 32 33 34 35 37 38",
            "ES,27 28",
            "RJ,21 22 24",
            "SP,11 12 13 14 15 16 17 18 19",
            "PR,41 42 43 44 45 46",
            "SC,47 48 49",
            "RS,51 53 54 55",
            "MS,67",
            "MT,65 66",
            "GO,61 62 64",
            "DF,61"
    })
    void expoeDddsOficiaisOrdenados(Uf uf, String ddds) {
        assertEquals(Arrays.asList(ddds.split(" ")), uf.getDdds());
    }

    @Test
    void admiteDddCompartilhadoEProtegeAListaContraAlteracao() {
        assertEquals(Collections.singletonList("61"), Uf.DF.getDdds());
        assertEquals(Arrays.asList("61", "62", "64"), Uf.GO.getDdds());
        assertThrows(UnsupportedOperationException.class, () -> Uf.GO.getDdds().set(0, "00"));
    }
}
