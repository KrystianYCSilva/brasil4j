package io.github.brasil4j.identidade;

import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentParsersTest {
    static final class Codigo extends AbstractDocument {
        Codigo(String canonical) {
            super(canonical);
        }
    }

    private static final DocumentMask MASCARA = DocumentMask.of("999.999-999");
    private static final Rejection PREFIXO_ZERO = Rejection.of(RejectionReason.UNSUPPORTED_BASE, "PREFIXO_ZERO");

    static DocumentParser<Codigo> codigo() {
        return DocumentParsers.builder(Codigo::new)
                .mask(MASCARA)
                .checkDigits(Modulus11.builder().build())
                .errorMessage("Código inválido.")
                .build();
    }

    @Nested
    class Contrato extends DocumentParserContractTest<Codigo> {
        @Override
        protected DocumentParser<Codigo> parser() {
            return codigo();
        }

        @Override
        protected String validInput() {
            return "123.456-789";
        }

        @Override
        protected String errorMessage() {
            return "Código inválido.";
        }
    }

    @Test
    void executaOPipelineNaOrdemDoModelo() {
        List<String> chamadas = new ArrayList<>();
        DocumentParser<Codigo> parser = DocumentParsers.builder(texto -> {
                    chamadas.add("fabrica");
                    return new Codigo(texto);
                })
                .normalizer(texto -> {
                    chamadas.add("normalizacao");
                    return MASCARA.unmask(texto);
                })
                .canonicalRule(texto -> {
                    chamadas.add("canonica");
                    return texto.startsWith("0") ? Optional.of(PREFIXO_ZERO) : Optional.empty();
                })
                .checkDigits(Modulus11.builder().build())
                .documentRule(documento -> {
                    chamadas.add("documento");
                    return Optional.empty();
                })
                .build();

        assertTrue(parser.isValid("123456789"));
        assertEquals(Arrays.asList("normalizacao", "canonica", "fabrica", "documento"), chamadas);
        chamadas.clear();
        assertEquals(Optional.of(PREFIXO_ZERO), parser.parse("012345678").getRejection());
        assertEquals(Arrays.asList("normalizacao", "canonica"), chamadas);
        chamadas.clear();
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                parser.parse("123456780").getRejection());
        assertEquals(Arrays.asList("normalizacao", "canonica"), chamadas);
    }

    @Test
    void regrasCanonicasEDeDocumentoAcumulamEmOrdem() {
        List<String> chamadas = new ArrayList<>();
        DocumentParser<Codigo> parser = DocumentParsers.builder(Codigo::new)
                .mask(MASCARA)
                .canonicalRule(texto -> chamada(chamadas, "c1"))
                .canonicalRule(texto -> chamada(chamadas, "c2"))
                .documentRule(documento -> chamada(chamadas, "d1"))
                .documentRule(documento -> chamada(chamadas, "d2"))
                .build();
        assertTrue(parser.isValid("000000000"));
        assertEquals(Arrays.asList("c1", "c2", "d1", "d2"), chamadas);
    }

    @Test
    void semDvEMensagemPadrao() {
        DocumentParser<Codigo> parser = DocumentParsers.builder(Codigo::new).mask(MASCARA).build();
        assertTrue(parser.isValid("123.456-780"));
        assertEquals("Documento inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> parser.parseOrThrow("x")).getMessage());
    }

    @Test
    void algoritmoForaDoDominioDaNormalizacaoViraFormatoInvalido() {
        DocumentParser<Codigo> parser = DocumentParsers.builder(Codigo::new)
                .mask(DocumentMask.of("XXX"))
                .checkDigits(Modulus11.builder().build())
                .build();
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)), parser.parse("AB1").getRejection());
    }

    @Test
    void decoracaoDeInterpretadorDoConsumidorUsaMensagemPadrao() {
        DocumentParser<Codigo> proprio = texto -> ValidationResult.valid(new Codigo("1"));
        DocumentParser<Codigo> decorado = proprio.withPolicy(documento -> false);
        assertEquals("Documento inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> decorado.parseOrThrow("1")).getMessage());
        assertEquals("Documento inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> ((DocumentParser<Codigo>) texto -> ValidationResult.invalid(PREFIXO_ZERO)).parseOrThrow("1"))
                .getMessage());
    }

    @Test
    void decoracaoPreservaAExcecaoDoInterpretadorDoConsumidorNaRejeicaoNativa() {
        IllegalStateException propria = new IllegalStateException("erro do consumidor");
        DocumentParser<Codigo> proprio = new DocumentParser<Codigo>() {
            @Override
            public ValidationResult<Codigo> parse(String input) {
                return "1".equals(input) ? ValidationResult.valid(new Codigo("1")) : ValidationResult.invalid(PREFIXO_ZERO);
            }

            @Override
            public Codigo parseOrThrow(String input) {
                if (!"1".equals(input)) {
                    throw propria;
                }
                return new Codigo("1");
            }
        };
        DocumentParser<Codigo> decorado = proprio.withPolicy(documento -> true);
        assertSame(propria, assertThrows(IllegalStateException.class, () -> decorado.parseOrThrow("2")));
        DocumentParser<Codigo> encadeado = proprio.withPolicy(documento -> true).withRule(documento -> Optional.empty())
                .withPolicy(documento -> true, "TERCEIRA");
        assertSame(propria, assertThrows(IllegalStateException.class, () -> encadeado.parseOrThrow("2")));
        assertEquals("Documento inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> encadeado.withPolicy(documento -> false).parseOrThrow("1")).getMessage());
        assertEquals("CPF inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> io.github.brasil4j.identidade.cpf.Cpf.parser().withPolicy(cpf -> true)
                        .withPolicy(cpf -> false).parseOrThrow("01234567890")).getMessage());
        assertEquals(new Codigo("1"), decorado.parseOrThrow("1"));
        assertEquals("Documento inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> proprio.withPolicy(documento -> false).parseOrThrow("1")).getMessage());
    }

    @Test
    void decoracaoEncadeadaInterpretaAOrigemUmaUnicaVez() {
        int[] chamadas = new int[2];
        DocumentParser<Codigo> proprio = new DocumentParser<Codigo>() {
            @Override
            public ValidationResult<Codigo> parse(String input) {
                chamadas[0]++;
                return ValidationResult.invalid(PREFIXO_ZERO);
            }

            @Override
            public Codigo parseOrThrow(String input) {
                chamadas[1]++;
                throw new IllegalStateException("erro do consumidor");
            }
        };
        DocumentParser<Codigo> encadeado = proprio.withPolicy(c -> true).withPolicy(c -> true).withPolicy(c -> true);
        assertThrows(IllegalStateException.class, () -> encadeado.parseOrThrow("x"));
        assertEquals(0, chamadas[0]);
        assertEquals(1, chamadas[1]);
        encadeado.parse("x");
        assertEquals(1, chamadas[0]);
    }

    @Test
    void regraDeDigitoVerificadorReutilizavel() {
        ValidationRule<String> dv = ValidationRule.checkDigits(Modulus11.builder().build());
        assertEquals(Optional.empty(), dv.check("123456789"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)), dv.check("123456780"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)), dv.check("12A456789"));
        assertThrows(NullPointerException.class, () -> ValidationRule.checkDigits(null));
    }

    @Test
    void validaAConfiguracao() {
        assertThrows(NullPointerException.class, () -> DocumentParsers.builder(null));
        DocumentParsers.Builder<Codigo> builder = DocumentParsers.builder(Codigo::new);
        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(NullPointerException.class, () -> builder.mask(null));
        assertThrows(NullPointerException.class, () -> builder.normalizer(null));
        assertThrows(NullPointerException.class, () -> builder.canonicalRule(null));
        assertThrows(NullPointerException.class, () -> builder.checkDigits(null));
        assertThrows(NullPointerException.class, () -> builder.documentRule(null));
        assertThrows(NullPointerException.class, () -> builder.errorMessage(null));
        builder.mask(MASCARA);
        assertThrows(IllegalStateException.class, () -> builder.normalizer(MASCARA::unmask));
        DocumentParsers.Builder<Codigo> outro = DocumentParsers.builder(Codigo::new).normalizer(MASCARA::unmask);
        assertThrows(IllegalStateException.class, () -> outro.mask(MASCARA));
    }

    @Test
    void fabricaOuNormalizadorQueDevolvemNuloSaoErrosDeConfiguracao() {
        DocumentParser<Codigo> fabricaNula = DocumentParsers.builder(texto -> (Codigo) null).mask(MASCARA).build();
        assertThrows(NullPointerException.class, () -> fabricaNula.parse("123456789"));
        DocumentParser<Codigo> normalizadorNulo = DocumentParsers.builder(Codigo::new)
                .normalizer(texto -> null).build();
        assertThrows(NullPointerException.class, () -> normalizadorNulo.parse("123456789"));
    }

    private static Optional<Rejection> chamada(List<String> chamadas, String nome) {
        chamadas.add(nome);
        return Optional.empty();
    }
}
