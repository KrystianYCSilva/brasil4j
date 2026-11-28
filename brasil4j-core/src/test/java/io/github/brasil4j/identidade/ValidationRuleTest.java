package io.github.brasil4j.identidade;

import io.github.brasil4j.verificacao.CheckDigitAlgorithm;
import io.github.brasil4j.verificacao.InvalidBaseException;
import io.github.brasil4j.verificacao.Modulus11;
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

    @Test
    void algoritmoMalConfiguradoNaoViraFormatoInvalido() {
        CheckDigitAlgorithm defeituoso = new CheckDigitAlgorithm() {
            @Override
            public int digitCount() {
                return 1;
            }

            @Override
            public Optional<String> compute(String base) {
                throw new IllegalStateException("configuração incoerente");
            }
        };
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> ValidationRule.checkDigits(defeituoso).check("123"));
        assertEquals("configuração incoerente", erro.getMessage());
    }

    @Test
    void baseForaDoDominioContinuaSendoFormatoInvalido() {
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                ValidationRule.checkDigits(Modulus11.builder().build()).check("1A3"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                ValidationRule.checkDigits(Modulus11.builder().build()).check("1"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                ValidationRule.checkDigits(Modulus11.pisCaixaFgtsV016()).check("123456789012"));
    }

    @Test
    void algoritmoPodeSinalizarBaseForaDoDominioPelaExcecaoDedicada() {
        CheckDigitAlgorithm proprio = new CheckDigitAlgorithm() {
            @Override
            public int digitCount() {
                return 1;
            }

            @Override
            public Optional<String> compute(String base) {
                throw new InvalidBaseException("base recusada");
            }
        };
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                ValidationRule.checkDigits(proprio).check("123"));
    }

    @Test
    void algoritmoLegadoPodeSinalizarBaseForaDoDominioComIllegalArgumentException() {
        CheckDigitAlgorithm legado = new CheckDigitAlgorithm() {
            @Override
            public int digitCount() {
                return 1;
            }

            @Override
            public Optional<String> compute(String base) {
                if (!base.equals("12")) {
                    throw new IllegalArgumentException("base recusada");
                }
                return Optional.of("3");
            }
        };
        ValidationRule<String> regra = ValidationRule.checkDigits(legado);

        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)), regra.check("1A3"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)), regra.check("124"));
        assertEquals(Optional.empty(), regra.check("123"));
    }
}
