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
    void recusaValorNegativoDeCaractereNoBuild() {
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().charValue(c -> -1, "0").build());
    }

    @Test
    void recusaNoBuildMapeamentoQueProduzDigitoForaDeZeroANove() {
        Modulus11.Builder dez = Modulus11.builder().remainder(resto -> OptionalInt.of(10));
        assertThrows(IllegalArgumentException.class, dez::build);
        // Só o resto 10 sai do intervalo: o build confere todos os restos de 0 a 10, não só os usados.
        Modulus11.Builder soNoUltimoResto = Modulus11.builder()
                .remainder(resto -> OptionalInt.of(resto == 10 ? 10 : 0));
        assertThrows(IllegalArgumentException.class, soNoUltimoResto::build);
        Modulus11.Builder negativo = Modulus11.builder().remainder(resto -> OptionalInt.of(resto == 0 ? -1 : 0));
        assertThrows(IllegalArgumentException.class, negativo::build);
    }

    @Test
    void recusaNoBuildMapeamentoQueDevolveNulo() {
        Modulus11.Builder nulo = Modulus11.builder().remainder(resto -> null);
        assertThrows(IllegalArgumentException.class, nulo::build);
        Modulus11.Builder nuloSoNoUltimoResto = Modulus11.builder().remainder(resto -> resto == 10 ? null : OptionalInt.empty());
        assertThrows(IllegalArgumentException.class, nuloSoNoUltimoResto::build);
    }

    @Test
    void pesosGrandesNaoTransbordam() {
        // 2147483647 mod 11 = 1; para a base "2": soma 2 (mod 11), resto 2, DV 11 - 2 = 9.
        assertEquals(Optional.of("9"), Modulus11.builder().weightsFromLeft(Integer.MAX_VALUE).build().compute("2"));
        // Soma que transbordaria em int: 9 parcelas de 9 * (2^31 - 1); cada peso vale 1 mod 11, soma 81, resto 4, DV 7.
        int[] pesos = new int[9];
        java.util.Arrays.fill(pesos, Integer.MAX_VALUE);
        assertEquals(Optional.of("7"), Modulus11.builder().weightsFromLeft(pesos).build().compute("999999999"));
        assertEquals(Optional.of("9"),
                Modulus11.builder().weightsFromRight(Integer.MAX_VALUE, Integer.MAX_VALUE).build().compute("2"));
    }

    @Test
    void presetsComDocumentacaoDeFonte() {
        assertEquals(2, Modulus11.cpf().digitCount());
        assertEquals(2, Modulus11.cnpj().digitCount());
        assertTrue(Modulus11.cpf().verify("28001238938"));
        assertTrue(Modulus11.cnpj().verify("12ABC34501DE35"));
        assertFalse(Modulus11.cnpj().verify("12ABC34501DE36"));
    }

    @Test
    void construcaoRecusaConfiguracaoIncoerente() {
        // Mais DVs que pesos: nenhuma base de ao menos um caractere seria aceita.
        assertThrows(IllegalArgumentException.class, () -> Modulus11.builder().weightsFromLeft(1, 2).digits(3).build());
        // Alfabeto com caractere de valor negativo.
        assertThrows(IllegalArgumentException.class,
                () -> Modulus11.builder().charValue(c -> c == 'B' ? -1 : c - 'A', "AB").build());
        // Configuração coerente continua válida, inclusive no limite.
        assertEquals(2, Modulus11.builder().weightsFromLeft(1, 2).digits(2).build().digitCount());
    }

    @Test
    void aceitaDoisDvsComAlfabetoReduzidoQuandoOMapeamentoECompativel() {
        Modulus11 algoritmo = Modulus11.builder()
                .charValue(c -> c - '0', "01")
                .remainder(resto -> OptionalInt.of(resto % 2))
                .digits(2)
                .build();

        assertEquals(Optional.of("01"), algoritmo.compute("1"));
        assertTrue(algoritmo.verify("101"));
    }

    @Test
    void umUnicoDvPodeEstarForaDoAlfabetoDaBase() {
        Modulus11 algoritmo = Modulus11.builder()
                .charValue(c -> c - '0', "01")
                .remainder(resto -> OptionalInt.of(9))
                .build();

        assertEquals(Optional.of("9"), algoritmo.compute("1"));
        assertTrue(algoritmo.verify("19"));
        assertThrows(InvalidBaseException.class, () -> algoritmo.compute("A"));
    }

    @Test
    void recusaNoBuildVariosDvsQuandoOMapeamentoProduzDigitoForaDoAlfabeto() {
        // O mesmo mapeamento gera todos os DVs e todos, menos o último, voltam à base: o alfabeto precisa conter
        // todo dígito que o mapeamento pode produzir, mesmo que o último DV não volte à base.
        Modulus11.Builder semNove = Modulus11.builder().charValue(c -> c - '0', "012345678").digits(2);
        assertThrows(IllegalArgumentException.class, semNove::build);
        Modulus11.Builder soNoUltimoDv = Modulus11.builder()
                .charValue(c -> c - '0', "01")
                .remainder(resto -> OptionalInt.of(resto == 2 ? 0 : 9))
                .digits(2);
        assertThrows(IllegalArgumentException.class, soNoUltimoDv::build);
    }

    @Test
    void mapeamentoVazioNaoExigeDigitoNoAlfabeto() {
        Modulus11 algoritmo = Modulus11.builder()
                .charValue(c -> c - '0', "01")
                .weightsFromRight(1, 1)
                .remainder(resto -> resto == 1 ? OptionalInt.empty() : OptionalInt.of(0))
                .digits(2)
                .build();

        // Base "1" com peso 1: resto 1, sem DV. Vazio não precisa pertencer ao alfabeto.
        assertEquals(Optional.empty(), algoritmo.compute("1"));
    }
}
