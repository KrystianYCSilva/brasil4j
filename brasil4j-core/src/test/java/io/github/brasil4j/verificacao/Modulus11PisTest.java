package io.github.brasil4j.verificacao;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Vetores sintéticos da matriz C1 (docs/descoberta/c1/nis-matriz.md) com somas e restos conferidos na D6. */
class Modulus11PisTest {
    @ParameterizedTest(name = "{0} · {1}")
    @CsvSource({
        "07, 00000000000, true,  true",
        "01, 01234567897, true,  true",
        "09, 01234567898, false, false",
        "15, 00000000019, true,  true",
        "16, 00000000063, false, false",
        "17, 00000000060, false, true",
        "18, 00000000050, false, false",
        "19, 00000000280, true,  true",
        "20, 00000000281, false, false",
        "21, 11111111116, true,  true",
    })
    void conferePelasDuasFormulas(String caso, String nis, boolean caixa, boolean legado) {
        assertEquals(caixa, Modulus11.pisCaixaFgtsV016().verify(nis), "Caixa FGTS v016");
        assertEquals(legado, Modulus11.pisLegacyMod11().verify(nis), "legado");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"0000000006, , 0", "0000000028, 0, 0", "0000000001, 9, 9"})
    void restoUmNaoTemDvNaFormulaDaCaixa(String base, String caixa, String legado) {
        assertEquals(Optional.ofNullable(caixa), Modulus11.pisCaixaFgtsV016().compute(base));
        assertEquals(Optional.of(legado), Modulus11.pisLegacyMod11().compute(base));
    }
}
