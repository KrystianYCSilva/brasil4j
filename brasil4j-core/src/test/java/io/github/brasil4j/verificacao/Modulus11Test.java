package io.github.brasil4j.verificacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class Modulus11Test {
    @Test
    void calculaComPesosPadraoDaDireita() {
        Modulus11 algoritmo = Modulus11.builder().build();
        assertEquals(1, algoritmo.digitCount());
        // 12345678 com pesos 2..9 da direita: soma 156, resto 2, DV 9.
        assertEquals(Optional.of("9"), algoritmo.compute("12345678"));
        assertTrue(algoritmo.verify("123456789"));
        assertFalse(algoritmo.verify("123456780"));
    }

    @Test
    void pesosCiclicosRecomecamNoPrimeiro() {
        Modulus11 algoritmo = Modulus11.builder().weightsFromRight(2, 3).build();
        // 1111 com pesos 2,3,2,3 da direita: soma 10, resto 10, DV 1.
        assertEquals(Optional.of("1"), algoritmo.compute("1111"));
    }

    @Test
    void pesosFixosDaEsquerdaEMapeamentoProprio() {
        Modulus11 algoritmo = Modulus11.builder()
                .weightsFromLeft(1, 2, 3)
                .remainder(resto -> resto == 1 ? OptionalInt.empty() : OptionalInt.of(resto % 10))
                .build();
        assertEquals(Optional.of("6"), algoritmo.compute("111"));
        assertEquals(Optional.empty(), algoritmo.compute("100"));
        assertFalse(algoritmo.verify("1000"));
        assertThrows(IllegalArgumentException.class, () -> algoritmo.compute("1111"));
    }

    @Test
    void variosDigitosEntramNaBaseDoSeguinte() {
        Modulus11 algoritmo = Modulus11.builder().digits(2).build();
        assertEquals(2, algoritmo.digitCount());
        String primeiro = Modulus11.builder().build().compute("12345678").get();
        String segundo = Modulus11.builder().build().compute("12345678" + primeiro).get();
        assertEquals(Optional.of(primeiro + segundo), algoritmo.compute("12345678"));
        assertTrue(algoritmo.verify("12345678" + primeiro + segundo));
    }

    @Test
    void alfabetoEValorDoCaractereConfiguraveis() {
        Modulus11 algoritmo = Modulus11.builder().charValue(c -> c - '0', "0123456789AB").build();
        assertTrue(algoritmo.compute("A1").isPresent());
        assertThrows(IllegalArgumentException.class, () -> algoritmo.compute("C1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12 4", "12-4", "１２", "a1"})
    void recusaBaseForaDoDominio(String base) {
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().build().compute(base));
    }

    @Test
    void recusaEntradasInvalidas() {
        Modulus11 algoritmo = Modulus11.builder().digits(2).build();
        assertThrows(NullPointerException.class, () -> algoritmo.compute(null));
        assertThrows(NullPointerException.class, () -> algoritmo.verify(null));
        assertThrows(IllegalArgumentException.class, () -> algoritmo.verify("12"));
        assertThrows(IllegalArgumentException.class, () -> algoritmo.verify("1 34"));
        assertFalse(algoritmo.verify("12345678AB"));
    }

    @Test
    void recusaConfiguracaoInvalida() {
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().digits(0));
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().weightsFromRight(0, 9));
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().weightsFromRight(9, 2));
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().weightsFromLeft());
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().weightsFromLeft(1, -1));
        assertThrows(NullPointerException.class, () -> Modulus11.builder().weightsFromLeft((int[]) null));
        assertThrows(NullPointerException.class, () -> Modulus11.builder().remainder(null));
        assertThrows(NullPointerException.class, () -> Modulus11.builder().charValue(null, "0"));
        assertThrows(NullPointerException.class, () -> Modulus11.builder().charValue(c -> c, null));
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().charValue(c -> c, ""));
    }

    @Test
    void recusaValoresDeCaractereOuDvForaDoIntervalo() {
        Modulus11 negativo = Modulus11.builder().charValue(c -> -1, "0").build();
        assertThrows(IllegalArgumentException.class, () -> negativo.compute("0"));
        Modulus11 dvLongo = Modulus11.builder().remainder(resto -> OptionalInt.of(10)).build();
        assertThrows(IllegalStateException.class, () -> dvLongo.compute("1"));
        Modulus11 mapeamentoNulo = Modulus11.builder().remainder(resto -> null).build();
        assertThrows(NullPointerException.class, () -> mapeamentoNulo.compute("1"));
    }

    @Test
    void presetsComDocumentacaoDeFonte() {
        assertEquals(2, Modulus11.cpf().digitCount());
        assertEquals(2, Modulus11.cnpj().digitCount());
        assertTrue(Modulus11.cpf().verify("28001238938"));
        assertTrue(Modulus11.cnpj().verify("12ABC34501DE35"));
        assertFalse(Modulus11.cnpj().verify("12ABC34501DE36"));
    }
}
