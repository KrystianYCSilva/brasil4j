package io.github.brasil4j.identidade.cpf;

import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Confere o preset do CPF contra 10 000 bases geradas com semente fixa e congeladas com os DVs calculados
 * pelo algoritmo anterior à Spec 010.
 */
class CpfModulus11EquivalenceTest {
    @ParameterizedTest
    @CsvFileSource(resources = "/verificacao/cpf-equivalencia.csv", numLinesToSkip = 1)
    void reproduzOsDvsCongelados(String base, String dv) {
        assertEquals(Optional.of(dv), Modulus11.cpf().compute(base));
    }
}
