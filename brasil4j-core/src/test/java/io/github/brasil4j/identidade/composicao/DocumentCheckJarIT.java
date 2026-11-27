package io.github.brasil4j.identidade.composicao;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import io.github.brasil4j.identidade.nis.NisValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.ACCEPTED;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_POLICY;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_VALIDATION;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentCheckJarIT {
    @Test
    void carregaOsTiposDeComposicaoDoJarDoNucleo() throws IOException, URISyntaxException {
        assertOrigemNoNucleo(DocumentCheck.class);
        assertOrigemNoNucleo(DocumentCheckResult.class);
        assertOrigemNoNucleo(NisValidator.class);
    }

    @Test
    void percorreListaMistaEConservaOResultadoDeCadaCampo() {
        String campo = "01234567898";
        Predicate<Nis> politicaDoImportador = nis -> nis.getCanonicalValue().startsWith("00");
        assertFalse(NisValidator.isValid(campo, politicaDoImportador));

        List<DocumentCheck<?>> itens = Arrays.asList(
                DocumentCheck.cpf(campo, CpfValidationMode.FORMAT_ONLY).withFormatter(Cpf::applyMask),
                DocumentCheck.nis(campo).withPolicy(politicaDoImportador),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC).withFormatter(Cnpj::applyMask));
        List<DocumentCheckResult> resultados = itens.stream().map(DocumentCheck::evaluate)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(ACCEPTED, REJECTED_BY_POLICY, ACCEPTED),
                resultados.stream().map(DocumentCheckResult::getStatus).collect(Collectors.toList()));
        assertEquals(Optional.of("012.345.678-98"), resultados.get(0).getFormattedValue());
        assertEquals(Optional.empty(), resultados.get(1).getFormattedValue());
        assertEquals(Optional.of("12.ABC.345/01DE-35"), resultados.get(2).getFormattedValue());
        DocumentCheckResult nisSemFormato = DocumentCheck.nis("00000000019").evaluate();
        assertEquals(ACCEPTED, nisSemFormato.getStatus());
        assertEquals(Optional.empty(), nisSemFormato.getFormattedValue());
    }

    @Test
    void formataCnpjConferidoPelaFormaEProtegeDiagnosticos() {
        String entrada = "12ABC34501DE36";
        String formatado = "12.ABC.345/01DE-36";
        DocumentCheck<Cnpj> item = DocumentCheck.cnpj(entrada, CnpjValidationMode.FORMAT_ONLY)
                .withFormatter(Cnpj::applyMask);
        DocumentCheckResult resultado = item.evaluate();

        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cnpj(entrada, CnpjValidationMode.ALPHANUMERIC)
                .evaluate().getStatus());
        assertEquals(ACCEPTED, resultado.getStatus());
        assertEquals(Optional.of(formatado), resultado.getFormattedValue());
        for (String diagnostico : Arrays.asList(item.toString(), resultado.toString())) {
            assertFalse(diagnostico.contains(entrada));
            assertFalse(diagnostico.contains(formatado));
        }
    }

    @ParameterizedTest(name = "caso C1 {0}")
    @CsvSource({
        "02, REJECTED_BY_VALIDATION",
        "09, REJECTED_BY_POLICY",
        "15, ACCEPTED",
        "17, ACCEPTED"
    })
    void usaOMesmoPredicadoNoValidadorENaComposicao(String caso,
            DocumentCheckResult.Status esperado) throws IOException {
        String entrada = entradaC1(caso);
        List<String> conferidos = new ArrayList<>();
        Predicate<Nis> politica = nis -> {
            conferidos.add(nis.getCanonicalValue());
            return nis.getCanonicalValue().startsWith("00");
        };
        List<String> esperados = esperado == REJECTED_BY_VALIDATION ? emptyList() : singletonList(entrada);
        assertEquals(esperado == ACCEPTED, NisValidator.isValid(entrada, politica));
        assertEquals(esperados, conferidos);
        conferidos.clear();

        List<String> apresentados = new ArrayList<>();
        DocumentCheckResult resultado = DocumentCheck.nis(entrada).withPolicy(politica).withFormatter(nis -> {
            apresentados.add(nis.getCanonicalValue());
            return "campo:" + nis.getCanonicalValue();
        }).evaluate();

        assertEquals(esperado, resultado.getStatus());
        assertEquals(esperados, conferidos);
        assertEquals(esperado == ACCEPTED ? singletonList(entrada) : emptyList(), apresentados);
        assertEquals(esperado == ACCEPTED ? Optional.of("campo:" + entrada) : Optional.empty(),
                resultado.getFormattedValue());
    }

    private static void assertOrigemNoNucleo(Class<?> tipo) throws IOException, URISyntaxException {
        Path target = Paths.get(System.getProperty("basedir"), "target");
        Path origem = Paths.get(tipo.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
        List<Path> artefatos = new ArrayList<>();
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(target, "brasil4j-core-*.jar")) {
            for (Path jar : jars) {
                if (!jar.getFileName().toString().matches(".*-(sources|javadoc|tests)\\.jar")) {
                    artefatos.add(jar.toRealPath());
                }
            }
        }
        assertEquals(artefatos, singletonList(origem), tipo.getName());
    }

    private static String entradaC1(String caso) throws IOException {
        InputStream recurso = DocumentCheckJarIT.class.getResourceAsStream("/nis/nis-c1.csv");
        assertNotNull(recurso, "/nis/nis-c1.csv");
        String prefixo = caso + ",\"";
        try (BufferedReader csv = new BufferedReader(new InputStreamReader(recurso, StandardCharsets.UTF_8))) {
            String linha = csv.lines().filter(texto -> texto.startsWith(prefixo)).findFirst()
                    .orElseThrow(() -> new AssertionError("Caso C1 ausente " + caso));
            return linha.substring(prefixo.length(), linha.indexOf('"', prefixo.length()));
        }
    }
}
