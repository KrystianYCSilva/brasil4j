package io.github.brasil4j.identidade.nis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NisJarIT {
    private static final String NAMESPACE_S2200 = "http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00";

    @Test
    void consomeNisDoJarDoReator() {
        assertTrue(Nis.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
        assertTrue(NisValidator.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
        assertTrue(NisFormatter.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar"));
    }

    @Test
    void compoeIgualdadePoliticaEApresentacaoPeloJar() throws Exception {
        String campo = extraiNisTrab("zeros");
        Nis nis = Nis.parse(campo);
        Nis equivalente = Nis.parse("00000000019");

        assertEquals(equivalente, nis);
        assertEquals(equivalente.hashCode(), nis.hashCode());
        assertTrue(NisValidator.isValid(campo, equivalente::equals));
        assertEquals("campo:00000000019", NisFormatter.format(nis, valor -> "campo:" + valor.getCanonicalValue()));
        assertEquals("[NIS oculto]", nis.toString());
    }

    @ParameterizedTest(name = "fragmento {0}")
    @MethodSource("fragmentos")
    void confereCampoHistorico(String caso, String esperado, boolean aceita) throws Exception {
        String entrada = extraiNisTrab(caso);
        assertEquals(esperado, entrada);

        if (aceita) {
            Nis nis = Nis.parse(entrada);
            assertEquals(esperado, nis.getCanonicalValue());
            assertEquals("[NIS oculto]", nis.toString());
        } else {
            IllegalArgumentException erro = assertThrowsExactly(IllegalArgumentException.class,
                    () -> Nis.parse(entrada));
            assertEquals("NIS inválido.", erro.getMessage());
            assertNull(erro.getCause());
        }
    }

    static Stream<Arguments> fragmentos() {
        return Stream.of(
                Arguments.of("zeros", "00000000019", true),
                Arguments.of("canonico", "01234567897", true),
                Arguments.of("curto", "1234567897", false),
                Arguments.of("letra", "A1234567897", false),
                Arguments.of("dv-divergente", "01234567898", true),
                Arguments.of("mascara", "012.34567.89-7", false),
                Arguments.of("espacos", " 01234567897 ", false),
                Arguments.of("quebra-tab", "\n\t01234567897", false),
                Arguments.of("somente-espacos", "   ", false),
                Arguments.of("omitido", null, false),
                Arguments.of("vazio-curto", "", false),
                Arguments.of("vazio-longo", "", false));
    }

    private static String extraiNisTrab(String caso) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try (InputStream xml = NisJarIT.class.getResourceAsStream("/nis/esocial-2.5/" + caso + ".xml")) {
            assertNotNull(xml, caso);
            Element raiz = factory.newDocumentBuilder().parse(xml).getDocumentElement();
            NodeList trabalhadores = raiz.getElementsByTagNameNS(NAMESPACE_S2200, "trabalhador");
            assertEquals(1, trabalhadores.getLength());
            NodeList inscricoes = ((Element) trabalhadores.item(0)).getElementsByTagNameNS(NAMESPACE_S2200, "nisTrab");
            return inscricoes.getLength() == 0 ? null : inscricoes.item(0).getTextContent();
        }
    }
}
