package io.github.brasil4j.identidade.composicao;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentCheckResultTest {
    @Test
    void distingueSaidaAusenteDeTextoVazioOuEspacos() {
        DocumentCheck<?> item = DocumentCheck.nis("00000000019");

        assertEquals(Optional.empty(), item.evaluate().getFormattedValue());
        assertEquals(Optional.of(""), item.withFormatter(value -> "").evaluate().getFormattedValue());
        assertEquals(Optional.of("  "), item.withFormatter(value -> "  ").evaluate().getFormattedValue());
    }
}
