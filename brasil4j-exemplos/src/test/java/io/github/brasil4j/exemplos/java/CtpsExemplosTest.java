package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.geografia.Uf;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult;
import io.github.brasil4j.trabalhista.ctps.CtpsGroup;
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin;
import io.github.brasil4j.trabalhista.ctps.CtpsProfile;
import io.github.brasil4j.trabalhista.ctps.CtpsRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CtpsExemplosTest {
    @Test
    void importaGrupoFisicoPreservandoZeros() {
        // Os campos já foram extraídos do leiaute histórico como texto.
        CtpsGroup grupo = CtpsGroup.of("0000123", "00004", "PR", CtpsOrigin.PHYSICAL, false);
        CtpsCheckResult resultado = CtpsProfile.ESOCIAL_S2200_V2_5.check(grupo);

        assertEquals(CtpsCheckResult.Status.CONFORMING, resultado.getStatus());
        CtpsRecord registro = resultado.getRecord().get();
        assertEquals("0000123", registro.getNumber());
        assertEquals("00004", registro.getSeries());
        assertEquals(Uf.PR, resultado.getIssuingUf().get());
    }

    @Test
    void importaProjecaoLegadaComPerdaDeclarada() {
        // A origem da importação declarou perda. Os campos recebidos são preservados.
        CtpsGroup grupo = CtpsGroup.of("123", "4", "PR", CtpsOrigin.CPF_PROJECTION, true);
        CtpsCheckResult resultado = CtpsProfile.ESOCIAL_S2200_V2_5.check(grupo);

        assertEquals(CtpsOrigin.CPF_PROJECTION, resultado.getOrigin().get());
        assertTrue(resultado.hasSourceLoss());
        CtpsRecord registro = resultado.getRecord().get();
        assertEquals("123", registro.getNumber());
        assertEquals("4", registro.getSeries());
        assertEquals(Uf.PR, registro.getUf());
        assertFalse(resultado.getIssuingUf().isPresent());
    }

    @Test
    void confereIdentificadorDigitalComOsModosDoCpf() {
        DocumentParser<Cpf> formato = Cpf.parser(CpfValidationMode.FORMAT_ONLY);
        DocumentParser<Cpf> comDv = Cpf.parser(CpfValidationMode.NUMERIC);

        assertTrue(formato.parse("28001238938").isValid());
        assertTrue(comDv.parse("28001238938").isValid());
        assertTrue(formato.parse("28001238939").isValid());
        assertFalse(comDv.parse("28001238939").isValid());
    }
}
