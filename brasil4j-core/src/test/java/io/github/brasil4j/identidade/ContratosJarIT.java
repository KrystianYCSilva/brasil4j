package io.github.brasil4j.identidade;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.composicao.DocumentCheck;
import io.github.brasil4j.identidade.composicao.DocumentCheckResult;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import io.github.brasil4j.identidade.nis.NisRules;
import io.github.brasil4j.identidade.nis.NisValidationMode;
import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Consome os contratos da Spec 010 a partir do JAR empacotado do núcleo. */
class ContratosJarIT {
    static final class Matricula extends AbstractDocument implements SecurityMaskedDocument {
        private static final DocumentMask MASK = DocumentMask.of("999.999-999");
        static final DocumentParser<Matricula> PARSER = DocumentParsers.builder(Matricula::new)
                .mask(MASK).checkDigits(Modulus11.builder().build()).errorMessage("Matrícula inválida.").build();

        private Matricula(String canonical) {
            super(canonical);
        }

        @Override
        public String applySecurityMask(char maskChar) {
            return MASK.applySecure(getCanonicalValue(), 3, 1, maskChar);
        }
    }

    @Test
    void carregaOsContratosDoJarDoNucleo() throws IOException, URISyntaxException {
        for (Class<?> tipo : Arrays.asList(Document.class, AbstractDocument.class, DocumentParser.class,
                DocumentParsers.class, ValidationResult.class, Rejection.class, DocumentFormatter.class,
                Modulus11.class, DocumentMask.class, NisValidationMode.class, NisRules.class)) {
            assertOrigemNoNucleo(tipo);
        }
    }

    @Test
    void documentoDoConsumidorEmListaComOsNativos() {
        List<DocumentCheck<?>> itens = Arrays.asList(
                DocumentCheck.of("123.456-789", Matricula.PARSER),
                DocumentCheck.of("28001238939", Cpf.parser(CpfValidationMode.NUMERIC)),
                DocumentCheck.of("00000000000", Nis.parser(NisValidationMode.CAIXA_FGTS_V016)
                        .withRule(NisRules.repeatedDigits())),
                DocumentCheck.of("12ABC34501DE35", Cnpj.parser()));
        List<Optional<RejectionReason>> motivos = new ArrayList<>();
        for (DocumentCheck<?> item : itens) {
            DocumentCheckResult resultado = item.evaluate();
            motivos.add(resultado.getRejection().map(Rejection::getReason));
        }
        assertEquals(Arrays.asList(Optional.empty(), Optional.of(RejectionReason.INVALID_CHECK_DIGITS),
                Optional.of(RejectionReason.INVALID_CHECK_DIGITS), Optional.empty()), motivos);
    }

    @Test
    void apresentacoesComESemOcultacao() {
        assertEquals("***.345.678-**", DocumentFormatter.<Cpf>secure().apply(Cpf.parse("01234567890")));
        assertEquals("012.345.678-90", DocumentFormatter.<Cpf>masked().apply(Cpf.parse("01234567890")));
        assertEquals("***.456-78*", Matricula.PARSER.parseOrThrow("123456789").applySecurityMask());
        assertEquals("[documento oculto]", Matricula.PARSER.parseOrThrow("123456789").toString());
        assertTrue(Nis.parser().isValid("01234567898"));
        assertFalse(Nis.parser(NisValidationMode.LEGACY_MOD11).isValid("01234567898"));
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
}
