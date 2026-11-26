package io.github.brasil4j.identidade.cnpj;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Define como interpretar a apresentação e quais verificações adicionais aplicar ao CNPJ. */
public enum CnpjValidationMode {
    /** Conferência de DV para bases numéricas e alfanuméricas. É o modo padrão. */
    ALPHANUMERIC(requiring(CnpjCheckDigits::matches)),

    /** Conferência de DV para bases compostas apenas de dígitos. */
    NUMERIC(requiring(canonical -> CnpjType.of(canonical) == CnpjType.NUMERIC && CnpjCheckDigits.matches(canonical))),

    /** Verifica somente a apresentação, com dois caracteres numéricos na posição do DV. */
    FORMAT_ONLY(CnpjFormatter::canonicalize);

    private final UnaryOperator<String> strategy;

    CnpjValidationMode(UnaryOperator<String> strategy) {
        this.strategy = strategy;
    }

    String validatedCanonical(String input) {
        return strategy.apply(input);
    }

    private static UnaryOperator<String> requiring(Predicate<String> check) {
        return input -> {
            String canonical = CnpjFormatter.canonicalize(input);
            return canonical != null && check.test(canonical) ? canonical : null;
        };
    }
}
