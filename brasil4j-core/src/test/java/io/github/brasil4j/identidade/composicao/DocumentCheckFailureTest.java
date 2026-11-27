package io.github.brasil4j.identidade.composicao;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.ACCEPTED;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_POLICY;
import static io.github.brasil4j.identidade.composicao.DocumentCheckResult.Status.REJECTED_BY_VALIDATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentCheckFailureTest {
    @Test
    void exigeFuncaoQuandoConfigurada() {
        DocumentCheck<Nis> check = DocumentCheck.nis("00000000019");

        assertThrows(NullPointerException.class, () -> check.withPolicy(null));
        assertThrows(NullPointerException.class, () -> check.withFormatter(null));
    }

    @Test
    void rejeitaSaidaNulaQuandoApresentacaoFoiConfigurada() {
        DocumentCheck<Nis> check = DocumentCheck.nis("00000000019").withFormatter(nis -> null);

        assertThrows(NullPointerException.class, check::evaluate);
    }

    @Test
    void expoeSomenteAsFabricasComModoAdequado() throws NoSuchMethodException {
        assertFabricaTipada(Cpf.class, "cpf", String.class, CpfValidationMode.class);
        assertFabricaTipada(Cnpj.class, "cnpj", String.class, CnpjValidationMode.class);
        assertFabricaTipada(Nis.class, "nis", String.class);
        assertThrows(NoSuchMethodException.class, () -> DocumentCheck.class.getMethod("cpf", String.class));
        assertThrows(NoSuchMethodException.class, () -> DocumentCheck.class.getMethod("cnpj", String.class));
        long nisFactories = Arrays.stream(DocumentCheck.class.getMethods())
                .filter(method -> "nis".equals(method.getName())).count();
        assertEquals(1, nisFactories);
    }

    private static void assertFabricaTipada(Class<?> tipo, String nome, Class<?>... parametros)
            throws NoSuchMethodException {
        ParameterizedType retorno = assertInstanceOf(ParameterizedType.class,
                DocumentCheck.class.getMethod(nome, parametros).getGenericReturnType());
        assertEquals(DocumentCheck.class, retorno.getRawType());
        assertEquals(Arrays.asList(tipo), Arrays.asList(retorno.getActualTypeArguments()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void textoAusenteOuEmBrancoProduzRejeicaoNativa(String input) {
        List<DocumentCheck<?>> items = Arrays.asList(
                DocumentCheck.cpf(input, CpfValidationMode.NUMERIC),
                DocumentCheck.cnpj(input, CnpjValidationMode.ALPHANUMERIC),
                DocumentCheck.nis(input));
        AtomicInteger calls = new AtomicInteger();

        for (DocumentCheck<?> item : items) {
            DocumentCheckResult result = item
                    .withPolicy(value -> {
                        calls.incrementAndGet();
                        return true;
                    })
                    .withFormatter(value -> Integer.toString(calls.incrementAndGet())).evaluate();

            assertEquals(REJECTED_BY_VALIDATION, result.getStatus());
            assertEquals(Optional.empty(), result.getFormattedValue());
        }
        assertEquals(0, calls.get());
    }

    @Test
    void substituiSomenteAFuncaoSelecionadaPreservandoAOrigem() {
        List<String> calls = new ArrayList<>();
        DocumentCheck<Nis> original = DocumentCheck.nis("00000000019")
                .withPolicy(nis -> {
                    calls.add("original");
                    return true;
                })
                .withFormatter(nis -> "original");

        DocumentCheck<Nis> newPolicy = original.withPolicy(nis -> {
            calls.add("nova");
            return true;
        });
        DocumentCheck<Nis> newFormatter = original.withFormatter(nis -> "nova");

        assertEquals(Optional.of("original"), newPolicy.evaluate().getFormattedValue());
        assertEquals(Optional.of("nova"), newFormatter.evaluate().getFormattedValue());
        assertEquals(Optional.of("original"), original.evaluate().getFormattedValue());
        assertEquals(Arrays.asList("nova", "original", "original"), calls);
    }

    @Test
    void reavaliaEmOrdemNaThreadDoChamador() {
        Thread caller = Thread.currentThread();
        List<String> calls = new ArrayList<>();
        DocumentCheck<Nis> check = DocumentCheck.nis("00000000019")
                .withPolicy(nis -> {
                    assertSame(caller, Thread.currentThread());
                    calls.add("politica");
                    return true;
                })
                .withFormatter(nis -> {
                    assertSame(caller, Thread.currentThread());
                    calls.add("formatador");
                    return "saida";
                });

        assertEquals(ACCEPTED, check.evaluate().getStatus());
        assertEquals(ACCEPTED, check.evaluate().getStatus());
        assertEquals(Arrays.asList("politica", "formatador", "politica", "formatador"), calls);
    }

    @Test
    void recalculaDecisaoQuandoAPoliticaMuda() {
        AtomicBoolean allowed = new AtomicBoolean(true);
        DocumentCheck<Nis> check = DocumentCheck.nis("00000000019").withPolicy(nis -> allowed.get());

        assertEquals(ACCEPTED, check.evaluate().getStatus());
        allowed.set(false);
        assertEquals(REJECTED_BY_POLICY, check.evaluate().getStatus());
    }

    @Test
    void propagaAFalhaOriginalSemPerderResultadosJaColetados() {
        IllegalArgumentException policyError = new IllegalArgumentException("falha da política");
        IllegalArgumentException formatterError = new IllegalArgumentException("falha do formatador");
        DocumentCheck<Nis> check = DocumentCheck.nis("00000000019");
        List<DocumentCheckResult> collected = new ArrayList<>();
        collected.add(check.evaluate());
        AtomicInteger formatterCalls = new AtomicInteger();
        DocumentCheck<Nis> policyFails = check.withPolicy(nis -> { throw policyError; })
                .withFormatter(nis -> Integer.toString(formatterCalls.incrementAndGet()));

        assertSame(policyError, assertThrows(IllegalArgumentException.class, policyFails::evaluate));
        assertEquals(0, formatterCalls.get());
        assertSame(formatterError, assertThrows(IllegalArgumentException.class,
                check.withFormatter(nis -> { throw formatterError; })::evaluate));
        assertEquals(1, collected.size());
        assertEquals(ACCEPTED, collected.get(0).getStatus());
    }

    @ParameterizedTest
    @MethodSource("documentosParaDiagnostico")
    void preservaPrivacidadeDosDiagnosticosEComportamentoDosValores(
            DocumentCheck<?> check, String input, String canonical, Object nativeValue, String nativeDisplay) {
        String formatted = "apresentação para o consumidor " + canonical;
        DocumentCheck<?> configured = check.withPolicy(document -> {
            assertEquals(nativeValue, document);
            assertEquals(nativeDisplay, document.toString());
            return true;
        }).withFormatter(document -> formatted);
        DocumentCheckResult result = configured.evaluate();
        assertEquals(Optional.of(formatted), result.getFormattedValue());

        NullPointerException policyError = assertThrows(NullPointerException.class, () -> check.withPolicy(null));
        NullPointerException formatterError = assertThrows(NullPointerException.class,
                () -> check.withFormatter(null));
        NullPointerException outputError = assertThrows(NullPointerException.class,
                check.withFormatter(document -> null)::evaluate);

        for (String diagnostic : Arrays.asList(configured.toString(), result.toString(),
                policyError.toString(), formatterError.toString(), outputError.toString())) {
            assertFalse(diagnostic.contains(input));
            assertFalse(diagnostic.contains(canonical));
            assertFalse(diagnostic.contains(formatted));
        }
        assertEquals(nativeDisplay, nativeValue.toString());
    }

    static Stream<Arguments> documentosParaDiagnostico() {
        return Stream.of(
                Arguments.of(DocumentCheck.cpf("280.012.389-39", CpfValidationMode.FORMAT_ONLY),
                        "280.012.389-39", "28001238939", Cpf.parse("28001238939", CpfValidationMode.FORMAT_ONLY),
                        "***.***.***-**"),
                Arguments.of(DocumentCheck.cnpj("12.ABC.345/01DE-36", CnpjValidationMode.FORMAT_ONLY),
                        "12.ABC.345/01DE-36", "12ABC34501DE36",
                        Cnpj.parse("12ABC34501DE36", CnpjValidationMode.FORMAT_ONLY), "12ABC34501DE36"),
                Arguments.of(DocumentCheck.nis("00000000019"),
                        "00000000019", "00000000019", Nis.parse("00000000019"), "[NIS oculto]"));
    }
}
