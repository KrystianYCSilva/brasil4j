package io.github.brasil4j.identidade.composicao;

import io.github.brasil4j.identidade.AbstractDocument;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.Modulus11;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.ACCEPTED;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_POLICY;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_VALIDATION;
import static org.junit.jupiter.api.Assertions.*;

class DocumentCheckOfTest {
    /** Documento sintético do consumidor, fora da biblioteca. */
    static final class Protocolo extends AbstractDocument {
        static final DocumentParser<Protocolo> PARSER = DocumentParsers.builder(Protocolo::new)
                .mask(DocumentMask.of("999.999-999"))
                .checkDigits(Modulus11.builder().build())
                .build();

        private Protocolo(String canonical) {
            super(canonical);
        }
    }

    @Test
    void listaHeterogeneaComDocumentoDoConsumidor() {
        List<DocumentCheck<?>> itens = Arrays.asList(
                DocumentCheck.of("123.456-789", Protocolo.PARSER),
                DocumentCheck.cpf("01234567898", CpfValidationMode.FORMAT_ONLY),
                DocumentCheck.nis("01234567898").withPolicy(nis -> nis.getCanonicalValue().startsWith("00")),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC),
                DocumentCheck.of("123.456-780", Protocolo.PARSER));

        List<DocumentCheckResult.Status> estados = new ArrayList<>();
        for (DocumentCheck<?> item : itens) {
            estados.add(item.evaluate().getStatus());
        }
        assertEquals(Arrays.asList(ACCEPTED, ACCEPTED, REJECTED_BY_POLICY, ACCEPTED, REJECTED_BY_VALIDATION), estados);
    }

    @Test
    void expoeOMotivoDaRejeicao() {
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                DocumentCheck.of("123.456-780", Protocolo.PARSER).evaluate().getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.MISSING)),
                DocumentCheck.of(null, Protocolo.PARSER).evaluate().getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.POLICY)),
                DocumentCheck.of("123456789", Protocolo.PARSER).withPolicy(p -> false).evaluate().getRejection());
        assertEquals(Optional.empty(), DocumentCheck.of("123456789", Protocolo.PARSER).evaluate().getRejection());
        assertEquals(Optional.of(Rejection.of(RejectionReason.UNSUPPORTED_BASE)),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.NUMERIC).evaluate().getRejection());
    }

    @Test
    void politicaDoInterpretadorViraRejeicaoPorPolitica() {
        DocumentCheckResult resultado = DocumentCheck.of("12ABC34501DE35",
                Cnpj.parser().withPolicy(cnpj -> cnpj.getBranch().equals("0001"), "SOMENTE_MATRIZ")).evaluate();
        assertEquals(REJECTED_BY_POLICY, resultado.getStatus());
        assertEquals(Optional.of(Rejection.of(RejectionReason.POLICY, "SOMENTE_MATRIZ")), resultado.getRejection());
    }

    @Test
    void apresentacaoDoDocumentoDoConsumidor() {
        DocumentCheckResult resultado = DocumentCheck.of("123456789", Protocolo.PARSER)
                .withFormatter(Protocolo::getCanonicalValue).evaluate();
        assertEquals(Optional.of("123456789"), resultado.getFormattedValue());
    }

    @Test
    void recusaInterpretadorNulo() {
        assertThrows(NullPointerException.class, () -> DocumentCheck.of("123456789", null));
    }
}
