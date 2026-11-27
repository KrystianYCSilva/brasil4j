package io.github.brasil4j.identidade;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {
    private static final Rejection FORMATO = Rejection.of(RejectionReason.INVALID_FORMAT);

    @Test
    void valido() {
        ValidationResult<String> resultado = ValidationResult.valid("documento");
        assertTrue(resultado.isValid());
        assertEquals(Optional.of("documento"), resultado.getDocument());
        assertEquals(Optional.empty(), resultado.getRejection());
        assertEquals(Optional.of(9), resultado.map(String::length).getDocument());
        assertEquals("aceito:documento", resultado.fold(valor -> "aceito:" + valor, rejeicao -> "rejeitado"));
        assertEquals("documento", resultado.orElseThrow());
        assertEquals("documento", resultado.orElseThrow("mensagem"));
        assertEquals("documento", resultado.orElseThrow(rejeicao -> new IllegalStateException()));
        assertEquals("ValidationResult[VALID]", resultado.toString());
    }

    @Test
    void invalido() {
        ValidationResult<String> resultado = ValidationResult.invalid(FORMATO);
        assertFalse(resultado.isValid());
        assertEquals(Optional.empty(), resultado.getDocument());
        assertEquals(Optional.of(FORMATO), resultado.getRejection());
        ValidationResult<Integer> mapeado = resultado.map(String::length);
        assertFalse(mapeado.isValid());
        assertEquals(Optional.of(FORMATO), mapeado.getRejection());
        assertEquals("INVALID_FORMAT",
                resultado.fold(valor -> "aceito", rejeicao -> rejeicao.getReason().name()));
        assertEquals("Documento inválido.",
                assertThrowsExactly(IllegalArgumentException.class, resultado::orElseThrow).getMessage());
        assertEquals("CPF inválido.", assertThrowsExactly(IllegalArgumentException.class,
                () -> resultado.orElseThrow("CPF inválido.")).getMessage());
        IllegalStateException falha = new IllegalStateException();
        assertSame(falha, assertThrows(IllegalStateException.class,
                () -> resultado.orElseThrow(rejeicao -> falha)));
        assertEquals("ValidationResult[INVALID:Rejection[INVALID_FORMAT]]", resultado.toString());
    }

    @Test
    void recusaNulos() {
        assertThrows(NullPointerException.class, () -> ValidationResult.valid(null));
        assertThrows(NullPointerException.class, () -> ValidationResult.invalid(null));
        ValidationResult<String> valido = ValidationResult.valid("x");
        assertThrows(NullPointerException.class, () -> valido.map(null));
        assertThrows(NullPointerException.class, () -> valido.map(valor -> null));
        assertThrows(NullPointerException.class, () -> valido.fold(null, rejeicao -> 1));
        assertThrows(NullPointerException.class, () -> valido.fold(valor -> 1, null));
        assertThrows(NullPointerException.class, () -> valido.orElseThrow((String) null));
        assertThrows(NullPointerException.class,
                () -> valido.orElseThrow((Function<Rejection, RuntimeException>) null));
        ValidationResult<String> invalido = ValidationResult.invalid(FORMATO);
        assertThrows(NullPointerException.class, () -> invalido.map(null));
        assertThrows(NullPointerException.class, () -> invalido.fold(valor -> 1, null));
        assertThrows(NullPointerException.class, () -> invalido.fold(null, rejeicao -> 1));
        assertThrows(NullPointerException.class, () -> invalido.orElseThrow((String) null));
        assertThrows(NullPointerException.class, () -> invalido.orElseThrow(rejeicao -> null));
    }
}
