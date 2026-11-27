package io.github.brasil4j.identidade.cnpj;

import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere o preset do CNPJ contra 10 000 bases numéricas e alfanuméricas geradas com semente fixa e
 * congeladas com os DVs calculados pelo algoritmo anterior à Spec 010.
 */
class CnpjModulus11EquivalenceTest {
    @ParameterizedTest
    @CsvFileSource(resources = "/verificacao/cnpj-equivalencia.csv", numLinesToSkip = 1)
    void reproduzOsDvsCongelados(String base, String dv) {
        assertEquals(Optional.of(dv), Modulus11.cnpj().compute(base));
    }

    @Test
    void bloqueioDeQuatorzeZerosFicaNosModosDoDocumento() {
        assertTrue(Modulus11.cnpj().verify("00000000000000"));
        assertFalse(CnpjValidator.isValid("00000000000000", CnpjValidationMode.ALPHANUMERIC));
    }
}
