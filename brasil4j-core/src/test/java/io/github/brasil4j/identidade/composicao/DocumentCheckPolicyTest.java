package io.github.brasil4j.identidade.composicao;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import io.github.brasil4j.identidade.nis.NisValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.ACCEPTED;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_POLICY;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_VALIDATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentCheckPolicyTest {
    @Test
    void regraLocalFuncionaNoValidadorDiretoENaListaMista() {
        Predicate<Nis> policy = nis -> nis.getCanonicalValue().startsWith("00");
        String accepted = "00000000019";
        String rejected = "01234567898";
        assertTrue(NisValidator.isValid(accepted, policy));
        assertFalse(NisValidator.isValid(rejected, policy));
        assertEquals(ACCEPTED, DocumentCheck.nis(accepted).withPolicy(policy).evaluate().getStatus());
        List<DocumentCheck<?>> list = Arrays.asList(
                DocumentCheck.cpf(rejected, CpfValidationMode.FORMAT_ONLY).withPolicy(Cpf.class::isInstance),
                DocumentCheck.nis(rejected).withPolicy(policy),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC)
                        .withPolicy(Cnpj.class::isInstance));

        assertEquals(Arrays.asList(ACCEPTED, REJECTED_BY_POLICY, ACCEPTED), list.stream()
                .map(DocumentCheck::evaluate).map(DocumentCheckResult::getStatus).collect(Collectors.toList()));
    }

    @Test
    void politicasDistintasNaoAlteramAValidacaoEstrutural() {
        DocumentCheck<Nis> check = DocumentCheck.nis("00000000019");
        assertEquals(ACCEPTED, check.withPolicy(nis -> true).evaluate().getStatus());
        assertEquals(REJECTED_BY_POLICY, check.withPolicy(nis -> false).evaluate().getStatus());
        assertEquals(ACCEPTED, check.evaluate().getStatus());
    }

    @Test
    void preservaOModoNativoAoConfigurarPolitica() {
        String cpf = "280.012.389-39";
        String cnpj = "12ABC34501DE36";

        assertEquals(ACCEPTED, DocumentCheck.cpf(cpf, CpfValidationMode.FORMAT_ONLY)
                .withPolicy(valor -> true).evaluate().getStatus());
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cpf(cpf, CpfValidationMode.NUMERIC)
                .withPolicy(valor -> true).evaluate().getStatus());
        assertEquals(ACCEPTED, DocumentCheck.cnpj(cnpj, CnpjValidationMode.FORMAT_ONLY)
                .withPolicy(valor -> true).evaluate().getStatus());
        assertEquals(REJECTED_BY_VALIDATION, DocumentCheck.cnpj(cnpj, CnpjValidationMode.ALPHANUMERIC)
                .withPolicy(valor -> true).evaluate().getStatus());
    }

    @Test
    void usaUmaInstanciaEmOrdemParaPoliticaEFormatacao() {
        List<String> calls = new ArrayList<>();
        AtomicReference<Nis> value = new AtomicReference<>();
        DocumentCheckResult result = DocumentCheck.nis("00000000019")
                .withPolicy(nis -> {
                    calls.add("politica");
                    value.set(nis);
                    return true;
                })
                .withFormatter(nis -> {
                    calls.add("formatador");
                    assertSame(value.get(), nis);
                    return nis.getCanonicalValue();
                }).evaluate();

        assertEquals(Arrays.asList("politica", "formatador"), calls);
        assertEquals(Optional.of("00000000019"), result.getFormattedValue());
    }

    @Test
    void pulaEtapasAposUmaRejeicao() {
        AtomicInteger policyCalls = new AtomicInteger();
        AtomicInteger formatterCalls = new AtomicInteger();
        Predicate<Nis> policy = nis -> {
            policyCalls.incrementAndGet();
            return false;
        };
        DocumentCheckResult invalid = DocumentCheck.nis("012.34567.89-7")
                .withPolicy(policy)
                .withFormatter(nis -> Integer.toString(formatterCalls.incrementAndGet())).evaluate();
        assertEquals(0, policyCalls.get());

        DocumentCheckResult denied = DocumentCheck.nis("00000000019")
                .withPolicy(policy)
                .withFormatter(nis -> Integer.toString(formatterCalls.incrementAndGet())).evaluate();
        assertEquals(1, policyCalls.get());
        assertEquals(0, formatterCalls.get());
        assertEquals(REJECTED_BY_VALIDATION, invalid.getStatus());
        assertEquals(REJECTED_BY_POLICY, denied.getStatus());
        assertEquals(Optional.empty(), invalid.getFormattedValue());
        assertEquals(Optional.empty(), denied.getFormattedValue());
    }

    @ParameterizedTest(name = "C1 {0}")
    @MethodSource("vetoresC1")
    void reproduzResultadosDaPoliticaExternaComRegraSintetica(
            String id, String input, boolean validStructure, DocumentCheckResult.Status expected) {
        List<String> policyInputs = new ArrayList<>();
        List<String> formatterInputs = new ArrayList<>();
        DocumentCheckResult result = DocumentCheck.nis(input)
                .withPolicy(nis -> {
                    String canonical = nis.getCanonicalValue();
                    policyInputs.add(canonical);
                    return Arrays.asList("00000000019", "00000000060").contains(canonical);
                })
                .withFormatter(nis -> {
                    formatterInputs.add(nis.getCanonicalValue());
                    return nis.getCanonicalValue();
                }).evaluate();

        assertEquals(expected, result.getStatus(), id);
        assertEquals(validStructure ? Collections.singletonList(input) : Collections.emptyList(), policyInputs);
        assertEquals(expected == ACCEPTED ? Collections.singletonList(input) : Collections.emptyList(),
                formatterInputs);
        assertEquals(expected == ACCEPTED ? Optional.of(input) : Optional.empty(), result.getFormattedValue());
    }

    static Stream<Arguments> vetoresC1() throws IOException {
        InputStream csv = Objects.requireNonNull(
                DocumentCheckPolicyTest.class.getResourceAsStream("/nis/nis-c1.csv"));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            List<Arguments> cases = new ArrayList<>();
            reader.lines()
                    .filter(line -> line.matches("(02|09|15|17),.*"))
                    .map(line -> line.split(","))
                    .forEach(row -> {
                        boolean validStructure = Boolean.parseBoolean(row[2]);
                        DocumentCheckResult.Status expected = !validStructure ? REJECTED_BY_VALIDATION
                                : "09".equals(row[0]) ? REJECTED_BY_POLICY : ACCEPTED;
                        cases.add(Arguments.of(row[0], row[1].substring(1, row[1].length() - 1),
                                validStructure, expected));
                    });
            assertEquals(4, cases.size());
            return cases.stream();
        }
    }
}
