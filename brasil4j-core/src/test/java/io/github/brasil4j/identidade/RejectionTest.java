package io.github.brasil4j.identidade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RejectionTest {
    @Test
    void identificaMotivoECodigoSemConteudoDoDocumento() {
        Rejection simples = Rejection.of(RejectionReason.INVALID_FORMAT);
        Rejection codificada = Rejection.of(RejectionReason.POLICY, "PREFIXO_IMPORTACAO");

        assertEquals(RejectionReason.INVALID_FORMAT, simples.getReason());
        assertEquals(Optional.empty(), simples.getCode());
        assertEquals(Optional.of("PREFIXO_IMPORTACAO"), codificada.getCode());
        assertEquals("Rejection[INVALID_FORMAT]", simples.toString());
        assertEquals("Rejection[POLICY:PREFIXO_IMPORTACAO]", codificada.toString());
    }

    @Test
    void comparaPorMotivoECodigo() {
        Rejection rejeicao = Rejection.of(RejectionReason.POLICY, "A");
        assertEquals(rejeicao, Rejection.of(RejectionReason.POLICY, "A"));
        assertEquals(rejeicao.hashCode(), Rejection.of(RejectionReason.POLICY, "A").hashCode());
        assertNotEquals(rejeicao, Rejection.of(RejectionReason.POLICY, "B"));
        assertNotEquals(rejeicao, Rejection.of(RejectionReason.POLICY));
        assertNotEquals(rejeicao, Rejection.of(RejectionReason.MISSING, "A"));
        assertNotEquals(rejeicao, "POLICY");
        assertEquals(Rejection.of(RejectionReason.MISSING), Rejection.of(RejectionReason.MISSING));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void recusaCodigoVazio(String codigo) {
        assertThrows(IllegalArgumentException.class, () -> Rejection.of(RejectionReason.POLICY, codigo));
    }

    @Test
    void recusaNulos() {
        assertThrows(NullPointerException.class, () -> Rejection.of(null));
        assertThrows(NullPointerException.class, () -> Rejection.of(null, "A"));
        assertThrows(NullPointerException.class, () -> Rejection.of(RejectionReason.POLICY, null));
    }
}
