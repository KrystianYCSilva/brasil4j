package io.github.brasil4j.identidade.composicao;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.ACCEPTED;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_VALIDATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentCheckTest {
    @Test
    void avaliaListaMistaNaOrdemDoChamador() {
        String input = "01234567898";
        List<DocumentCheck<?>> items = Arrays.asList(
                DocumentCheck.cpf(input, CpfValidationMode.FORMAT_ONLY),
                DocumentCheck.nis(input),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC));

        assertEquals(Arrays.asList(ACCEPTED, ACCEPTED, ACCEPTED), statuses(items));
        assertEquals(Collections.emptyList(), statuses(Collections.emptyList()));
    }

    @Test
    void rejeicaoNativaNaoInterrompeALista() {
        List<DocumentCheck<?>> items = Arrays.asList(
                DocumentCheck.cpf("01234567898", CpfValidationMode.FORMAT_ONLY),
                DocumentCheck.nis("012.34567.89-7"),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC));

        assertEquals(Arrays.asList(ACCEPTED, REJECTED_BY_VALIDATION, ACCEPTED), statuses(items));
        assertEquals(ACCEPTED, DocumentCheck.nis("00000000000").evaluate().getStatus());
    }

    @Test
    void exigeModoNaMontagem() {
        String cpf = "280.012.389-38";
        String cnpj = "12.ABC.345/01DE-35";
        NullPointerException cpfError = assertThrows(NullPointerException.class, () -> DocumentCheck.cpf(cpf, null));
        NullPointerException cnpjError = assertThrows(NullPointerException.class, () -> DocumentCheck.cnpj(cnpj, null));

        assertFalse(cpfError.toString().contains(cpf));
        assertFalse(cpfError.toString().contains("28001238938"));
        assertFalse(cnpjError.toString().contains(cnpj));
        assertFalse(cnpjError.toString().contains("12ABC34501DE35"));
    }

    @Test
    void aplicaModoExplicitoDoCpf() {
        assertEquals(ACCEPTED, DocumentCheck.cpf("280.012.389-38", CpfValidationMode.NUMERIC)
                .evaluate().getStatus());
        assertEquals(ACCEPTED, DocumentCheck.cpf("280.012.389-39", CpfValidationMode.FORMAT_ONLY)
                .evaluate().getStatus());
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cpf("280.012.389-39", CpfValidationMode.NUMERIC)
                .evaluate().getStatus());
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cpf("280.012389-38", CpfValidationMode.FORMAT_ONLY)
                .evaluate().getStatus());
    }

    @Test
    void aplicaModoExplicitoDoCnpj() {
        assertEquals(ACCEPTED, DocumentCheck.cnpj("11.222.333/0001-81", CnpjValidationMode.NUMERIC)
                .evaluate().getStatus());
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.NUMERIC)
                .evaluate().getStatus());
        assertEquals(ACCEPTED, DocumentCheck.cnpj("12.ABC.345/01DE-35", CnpjValidationMode.ALPHANUMERIC)
                .evaluate().getStatus());
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cnpj("12.abc.345/01DE-35", CnpjValidationMode.FORMAT_ONLY)
                .evaluate().getStatus());
    }

    @Test
    void formataValorNoModoUtilizadoNaConferencia() {
        String input = "12ABC34501DE36";
        AtomicInteger calls = new AtomicInteger();
        DocumentCheckResult accepted = DocumentCheck.cnpj(input, CnpjValidationMode.FORMAT_ONLY)
                .withFormatter(cnpj -> {
                    calls.incrementAndGet();
                    return cnpj.applyMask();
                }).evaluate();
        DocumentCheckResult rejected = DocumentCheck.cnpj(input, CnpjValidationMode.ALPHANUMERIC)
                .withFormatter(Cnpj::applyMask).evaluate();

        assertEquals(ACCEPTED, accepted.getStatus());
        assertEquals(Optional.of("12.ABC.345/01DE-36"), accepted.getFormattedValue());
        assertEquals(REJECTED_BY_VALIDATION, rejected.getStatus());
        assertEquals(Optional.empty(), rejected.getFormattedValue());
        assertEquals(1, calls.get());
        assertEquals(Optional.of("012.345.678-98"), DocumentCheck.cpf("01234567898", CpfValidationMode.FORMAT_ONLY)
                .withFormatter(Cpf::applyMask).evaluate().getFormattedValue());
    }

    private static List<DocumentCheckResult.Status> statuses(List<DocumentCheck<?>> items) {
        return items.stream()
                .map(DocumentCheck::evaluate)
                .map(DocumentCheckResult::getStatus)
                .collect(Collectors.toList());
    }
}
