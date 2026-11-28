package io.github.brasil4j.identidade;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.nis.Nis;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class DocumentFormatterTest {
    private static final Cpf CPF = Cpf.parse("01234567890");
    private static final Cnpj CNPJ = Cnpj.parse("12ABC34501DE35");
    private static final Nis NIS = Nis.parse("01234567898");

    @Test
    void canonicoEMascaraCompletaSemOcultacao() {
        assertEquals("01234567898", DocumentFormatter.<Nis>canonical().apply(NIS));
        assertEquals("012.345.678-90", DocumentFormatter.<Cpf>masked().apply(CPF));
        assertEquals("12.ABC.345/01DE-35", DocumentFormatter.<Cnpj>masked().apply(CNPJ));
    }

    @Test
    void mascaraDeSegurancaPadraoEConfiguravel() {
        assertEquals("***.345.678-**", DocumentFormatter.<Cpf>secure().apply(CPF));
        assertEquals("##.ABC.345/01DE-##", DocumentFormatter.<Cnpj>secure('#').apply(CNPJ));
        assertEquals("***3456789*", DocumentFormatter.<Nis>secure().apply(NIS));
        assertThrows(IllegalArgumentException.class, () -> DocumentFormatter.secure('7'));
    }

    @Test
    void ocultacaoParcialPorQuantidadeVisivel() {
        assertEquals("**********DE35", DocumentFormatter.<Cnpj>redacted(0, 4, '*').apply(CNPJ));
        assertEquals("01*********", DocumentFormatter.<Cpf>redacted(2, 0, '*').apply(CPF));
        assertEquals("012#####898", DocumentFormatter.<Nis>redacted(3, 3, '#').apply(NIS));
        assertEquals("***********", DocumentFormatter.<Cpf>redacted(6, 5, '*').apply(CPF));
        assertEquals("***********", DocumentFormatter.<Cpf>redacted(20, 0, '*').apply(CPF));
    }

    @Test
    void validaArgumentosEDocumento() {
        assertThrows(IllegalArgumentException.class, () -> DocumentFormatter.redacted(-1, 0, '*'));
        assertThrows(IllegalArgumentException.class, () -> DocumentFormatter.redacted(0, -1, '*'));
        assertThrows(IllegalArgumentException.class, () -> DocumentFormatter.redacted(1, 1, 'a'));
        assertThrows(NullPointerException.class, () -> DocumentFormatter.canonical().apply(null));
        assertThrows(NullPointerException.class, () -> DocumentFormatter.<Cpf>masked().apply(null));
        assertThrows(NullPointerException.class, () -> DocumentFormatter.<Cpf>secure().apply(null));
        assertThrows(NullPointerException.class, () -> DocumentFormatter.redacted(1, 1, '*').apply(null));
    }

    @Test
    void interoperaComFunctionDoJdk() {
        Function<Cpf, Integer> funcao = DocumentFormatter.<Cpf>masked().andThen(String::length);
        assertEquals(14, funcao.apply(CPF));
        DocumentFormatter<Cpf> lambda = cpf -> "x";
        assertEquals("x", lambda.apply(CPF));
    }
}
