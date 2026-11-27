package io.github.brasil4j.identidade.cnpj;

import io.github.brasil4j.verificacao.Modulus11;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Define como interpretar a apresentação e quais verificações adicionais aplicar ao CNPJ. */
public enum CnpjValidationMode {
    /** Conferência de DV para bases numéricas e alfanuméricas. É o modo padrão. */
    ALPHANUMERIC(requiring(CnpjValidationMode::hasCheckDigits)),

    /** Conferência de DV para bases compostas apenas de dígitos. */
    NUMERIC(requiring(canonical -> CnpjType.of(canonical) == CnpjType.NUMERIC && hasCheckDigits(canonical))),

    /** Verifica somente a apresentação, com dois caracteres numéricos na posição do DV. */
    FORMAT_ONLY(CnpjFormatter::canonicalize);

    private static final String ALL_ZEROS = "00000000000000";

    private final UnaryOperator<String> strategy;

    CnpjValidationMode(UnaryOperator<String> strategy) {
        this.strategy = strategy;
    }

    String validatedCanonical(String input) {
        return strategy.apply(input);
    }

    /** Bloqueio de 14 zeros (F-05) seguido do módulo 11 do Anexo XV da IN RFB 2.119/2022. */
    static boolean hasCheckDigits(String canonical) {
        return !ALL_ZEROS.equals(canonical) && Modulus11.cnpj().verify(canonical);
    }

    private static UnaryOperator<String> requiring(Predicate<String> check) {
        return input -> {
            String canonical = CnpjFormatter.canonicalize(input);
            return canonical != null && check.test(canonical) ? canonical : null;
        };
    }
}
