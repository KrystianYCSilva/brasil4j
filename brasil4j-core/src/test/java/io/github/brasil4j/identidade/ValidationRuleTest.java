package io.github.brasil4j.identidade;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ValidationRuleTest {
    private static final Rejection CURTO = Rejection.of(RejectionReason.INVALID_FORMAT, "CURTO");
    private static final Rejection PREFIXO = Rejection.of(RejectionReason.POLICY, "PREFIXO");

    @Test
    void criaRegraAPartirDePredicado() {
        ValidationRule<String> regra = ValidationRule.of(texto -> texto.length() >= 3, CURTO);
        assertEquals(Optional.empty(), regra.check("abc"));
        assertEquals(Optional.of(CURTO), regra.check("ab"));
    }

    @Test
    void regraNulaAceitaQualquerValor() {
        assertEquals(Optional.empty(), ValidationRule.<String>accept().check("qualquer"));
    }

    @Test
    void encadeiaEmOrdemComCurtoCircuito() {
        List<String> chamadas = new ArrayList<>();
        ValidationRule<String> tamanho = valor -> {
            chamadas.add("tamanho");
            return valor.length() >= 3 ? Optional.empty() : Optional.of(CURTO);
        };
        ValidationRule<CharSequence> prefixo = valor -> {
            chamadas.add("prefixo");
            return valor.toString().startsWith("00") ? Optional.empty() : Optional.of(PREFIXO);
        };
        ValidationRule<String> cadeia = tamanho.and(prefixo);

        assertEquals(Optional.of(CURTO), cadeia.check("0"));
        assertEquals(Arrays.asList("tamanho"), chamadas);
        chamadas.clear();
        assertEquals(Optional.of(PREFIXO), cadeia.check("123"));
        assertEquals(Arrays.asList("tamanho", "prefixo"), chamadas);
        assertEquals(Optional.empty(), cadeia.check("001"));
    }

    @Test
    void recusaNulos() {
        assertThrows(NullPointerException.class, () -> ValidationRule.of(null, CURTO));
        assertThrows(NullPointerException.class, () -> ValidationRule.of(texto -> true, null));
        assertThrows(NullPointerException.class, () -> ValidationRule.<String>accept().and(null));
    }
}
