package io.github.brasil4j.identidade;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.nis.Nis;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractDocumentTest {
    private static final class Protocolo extends AbstractDocument {
        Protocolo(String canonical) {
            super(canonical);
        }
    }

    private static final class OutroProtocolo extends AbstractDocument {
        OutroProtocolo(String canonical) {
            super(canonical);
        }
    }

    @Test
    void igualdadePelaClasseEPeloCanonico() {
        Protocolo protocolo = new Protocolo("123");
        assertEquals("123", protocolo.getCanonicalValue());
        assertEquals(protocolo, new Protocolo("123"));
        assertEquals(protocolo, protocolo);
        assertEquals("123".hashCode(), protocolo.hashCode());
        assertNotEquals(protocolo, new Protocolo("124"));
        assertNotEquals(protocolo, new OutroProtocolo("123"));
        assertNotEquals(protocolo, "123");
        assertFalse(protocolo.equals(null));
    }

    @Test
    void representacaoTextualOcultaPorPadrao() {
        assertEquals("[documento oculto]", new Protocolo("123").toString());
    }

    @Test
    void recusaCanonicoNulo() {
        assertThrows(NullPointerException.class, () -> new Protocolo(null));
    }

    @Test
    void documentosNativosImplementamSomenteAsCapacidadesQueTem() {
        Object cpf = Cpf.parse("01234567890");
        Object cnpj = Cnpj.parse("12ABC34501DE35");
        Object nis = Nis.parse("01234567898");
        for (Object documento : new Object[] {cpf, cnpj}) {
            assertTrue(documento instanceof AbstractDocument);
            assertTrue(documento instanceof MaskedDocument);
            assertTrue(documento instanceof SecurityMaskedDocument);
            assertTrue(documento instanceof CheckDigitDocument);
        }
        assertTrue(nis instanceof AbstractDocument);
        assertTrue(nis instanceof SecurityMaskedDocument);
        assertFalse(nis instanceof MaskedDocument);
        assertFalse(nis instanceof CheckDigitDocument);
    }

    @Test
    void cnpjConfereDvComBloqueioDeQuatorzeZeros() {
        assertTrue(Cnpj.parse("12ABC34501DE35").hasValidCheckDigits());
        assertFalse(Cnpj.parse("12ABC34501DE36", CnpjValidationMode.FORMAT_ONLY).hasValidCheckDigits());
        assertFalse(Cnpj.parse("00000000000000", CnpjValidationMode.FORMAT_ONLY).hasValidCheckDigits());
    }

    @Test
    void mascaraDeSegurancaComLayoutPadrao() {
        Cpf cpf = Cpf.parse("01234567890");
        Cnpj cnpj = Cnpj.parse("12ABC34501DE35");
        Nis nis = Nis.parse("01234567898");
        assertEquals("***.345.678-**", cpf.applySecurityMask());
        assertEquals("###.345.678-##", cpf.applySecurityMask('#'));
        assertEquals("**.ABC.345/01DE-**", cnpj.applySecurityMask());
        assertEquals("##.ABC.345/01DE-##", cnpj.applySecurityMask('#'));
        assertEquals("***3456789*", nis.applySecurityMask());
        assertEquals("###3456789#", nis.applySecurityMask('#'));
        assertThrows(IllegalArgumentException.class, () -> cpf.applySecurityMask('0'));
        assertThrows(IllegalArgumentException.class, () -> nis.applySecurityMask('A'));
    }

    @Test
    void apresentacaoSemOcultacaoERepresentacaoPadraoInalteradas() {
        Cpf cpf = Cpf.parse("01234567890");
        Cnpj cnpj = Cnpj.parse("12ABC34501DE35");
        Nis nis = Nis.parse("01234567898");
        assertEquals("012.345.678-90", cpf.applyMask());
        assertEquals("12.ABC.345/01DE-35", cnpj.applyMask());
        assertEquals("01234567898", nis.getCanonicalValue());
        assertEquals("***.***.***-**", cpf.toString());
        assertEquals("12ABC34501DE35", cnpj.toString());
        assertEquals("[NIS oculto]", nis.toString());
    }

    /** Documenta o comportamento atual: a classe só recusa nulo e não valida o conteúdo do canônico. */
    @Test
    void canonicoNuloLancaNpeComMensagemEQualquerTextoNaoNuloEAceito() {
        NullPointerException erro = assertThrows(NullPointerException.class, () -> new Protocolo(null));
        assertEquals("Valor canônico obrigatório.", erro.getMessage());
        assertEquals("", new Protocolo("").getCanonicalValue());
        assertEquals("qualquer coisa", new Protocolo("qualquer coisa").getCanonicalValue());
    }
}
