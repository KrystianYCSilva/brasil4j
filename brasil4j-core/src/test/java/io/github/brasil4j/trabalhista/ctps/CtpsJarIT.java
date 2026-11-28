package io.github.brasil4j.trabalhista.ctps;

import io.github.brasil4j.geografia.Uf;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static io.github.brasil4j.trabalhista.ctps.CtpsOrigin.UNDETERMINED;
import static io.github.brasil4j.trabalhista.ctps.CtpsProfile.ESOCIAL_S2200_V2_5;
import static org.junit.jupiter.api.Assertions.*;

class CtpsJarIT {
    private static final String ROOT = "/ctps/esocial-2.5/";
    private static final String NS = "http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00";

    @Test
    void usaJarCoreDoReator() throws Exception {
        Path jar = Paths.get(CtpsProfile.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertEquals(Paths.get("target").toRealPath(), jar.getParent().toRealPath());
        assertTrue(Files.isRegularFile(jar));
        try (JarFile archive = new JarFile(jar.toFile())) {
            JarEntry descriptor = archive.getJarEntry("META-INF/maven/io.github.brasil4j/brasil4j-core/pom.properties");
            assertNotNull(descriptor);
            Properties coordinates = new Properties();
            try (InputStream input = archive.getInputStream(descriptor)) {
                coordinates.load(input);
            }
            String version = System.getProperty("brasil4j.reactor.version");
            assertNotNull(version);
            assertFalse(version.isEmpty());
            assertEquals(version, coordinates.getProperty("version"));
            assertEquals("brasil4j-core-" + version + ".jar", jar.getFileName().toString());
        }
    }

    @ParameterizedTest(name = "fragmento {0}")
    @MethodSource("cases")
    void confereFragmento(String id, String[] expected) throws Exception {
        CtpsGroup group = readGroup(id);
        CtpsCheckResult result = ESOCIAL_S2200_V2_5.check(group);
        assertEquals(expected[1], result.getStatus().name());
        List<String> expectedFields = expected[2].isEmpty() ? Collections.emptyList() : Arrays.asList(expected[2].split(","));
        List<String> fields = result.getAffectedFields().stream().map(Enum::name).collect(Collectors.toList());
        assertEquals(expectedFields, fields);
        assertDiagnostico(result, expected[1], expectedFields);
        if (group != null) {
            assertEquals("[Grupo CTPS oculto]", group.toString());
        }
        if ("CONFORMING".equals(expected[1])) {
            CtpsRecord record = result.getRecord().get();
            assertEquals(decode(expected[3]), record.getNumber());
            assertEquals(decode(expected[4]), record.getSeries());
            assertEquals(expected[5], record.getUf().getSigla());
            assertEquals("[Registro CTPS oculto]", record.toString());
        } else {
            assertFalse(result.getRecord().isPresent());
        }
    }

    @Test
    void preservaProvenienciaEDiagnosticoNoJar() {
        for (CtpsOrigin origin : CtpsOrigin.values()) {
            CtpsGroup group = CtpsGroup.of("0123456", "7890", "PR", origin, false);
            CtpsCheckResult result = ESOCIAL_S2200_V2_5.check(group);
            assertEquals(origin, result.getOrigin().get());
            assertSame(Uf.PR, result.getRecord().get().getUf());
            assertSame(origin == CtpsOrigin.PHYSICAL ? Uf.PR : null, result.getIssuingUf().orElse(null));
            assertEquals("[Grupo CTPS oculto]", group.toString());
            assertDiagnostico(result, "CONFORMING", Collections.emptyList());
        }
        CtpsCheckResult migrated = ESOCIAL_S2200_V2_5.check(CtpsGroup.of("123", "4", "PR", UNDETERMINED, true));
        assertTrue(migrated.hasSourceLoss());
        assertEquals("123", migrated.getRecord().get().getNumber());
        assertEquals("4", migrated.getRecord().get().getSeries());
        assertDiagnostico(migrated, "CONFORMING", Collections.emptyList());
    }

    @Test
    void usaCpfComoIdentificadorDigitalNoMesmoJar() {
        assertEquals(CtpsProfile.class.getProtectionDomain().getCodeSource().getLocation(),
                Cpf.class.getProtectionDomain().getCodeSource().getLocation());
        assertTrue(Cpf.parser(CpfValidationMode.FORMAT_ONLY).parse("28001238938").isValid());
        assertTrue(Cpf.parser(CpfValidationMode.NUMERIC).parse("28001238938").isValid());
        assertTrue(Cpf.parser(CpfValidationMode.FORMAT_ONLY).parse("28001238939").isValid());
        assertFalse(Cpf.parser(CpfValidationMode.NUMERIC).parse("28001238939").isValid());
    }

    static Stream<Arguments> cases() throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource("cases.tsv"), StandardCharsets.UTF_8))) {
            assertEquals("id\tstatus\tfields\tnumber\tseries\tuf\tsuite", reader.readLine());
            List<String[]> rows = reader.lines().map(line -> line.split("\t", -1)).collect(Collectors.toList());
            rows.forEach(row -> {
                assertEquals(7, row.length);
                assertTrue("MAIN".equals(row[6]) || "EXTRA".equals(row[6]), row[6]);
            });
            List<String[]> main = rows.stream().filter(row -> "MAIN".equals(row[6])).collect(Collectors.toList());
            List<String> mainIds = main.stream().map(row -> row[0]).collect(Collectors.toList());
            List<String> extraIds = rows.stream().filter(row -> "EXTRA".equals(row[6]))
                    .map(row -> row[0]).sorted().collect(Collectors.toList());
            Map<String, Long> totals = main.stream()
                    .collect(Collectors.groupingBy(row -> row[1], Collectors.counting()));
            assertEquals(IntStream.rangeClosed(1, 11).mapToObj(i -> String.format(Locale.ROOT, "%02d", i))
                    .collect(Collectors.toList()), mainIds);
            assertEquals(Arrays.asList("extra-ctps-vazia", "extra-documentos-vazio", "extra-numero-vazio",
                    "extra-omitido-e-invalido", "extra-sem-documentos"), extraIds);
            assertEquals(Long.valueOf(1), totals.get("ABSENT"));
            assertEquals(Long.valueOf(1), totals.get("INCOMPLETE"));
            assertEquals(Long.valueOf(5), totals.get("OUT_OF_PROFILE"));
            assertEquals(Long.valueOf(4), totals.get("CONFORMING"));
            return rows.stream().map(row -> Arguments.of(row[0], row));
        }
    }

    private static void assertDiagnostico(CtpsCheckResult result, String status, List<String> fields) {
        assertEquals("CtpsCheckResult[profile=ESOCIAL_S2200_V2_5, status=" + status
                + ", affectedFields=" + fields + "]", result.toString());
    }

    private static CtpsGroup readGroup(String id) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (InputStream input = resource(id + ".xml")) {
            Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            assertEquals(NS, root.getNamespaceURI());
            assertEquals("eSocial", root.getLocalName());
            Element event = child(root, "evtAdmissao");
            assertNotNull(event);
            Element worker = child(event, "trabalhador");
            assertNotNull(worker);
            Element group = child(child(worker, "documentos"), "CTPS");
            return group == null ? null : CtpsGroup.of(
                    text(group, "nrCtps"), text(group, "serieCtps"), text(group, "ufCtps"), UNDETERMINED, false);
        }
    }

    private static Element child(Element parent, String localName) {
        if (parent != null) {
            for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node instanceof Element && NS.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName())) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    private static String text(Element parent, String localName) {
        Element element = child(parent, localName);
        return element == null ? null : element.getTextContent();
    }

    private static InputStream resource(String name) {
        InputStream stream = CtpsJarIT.class.getResourceAsStream(ROOT + name);
        assertNotNull(stream, name);
        return stream;
    }

    private static String decode(String expected) {
        return expected.replace("\\u0020", " ");
    }
}
