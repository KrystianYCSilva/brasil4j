package io.github.brasil4j.identidade.cnpj;

import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import io.github.brasil4j.identidade.ValidationRule;
import io.github.brasil4j.verificacao.Modulus11;

import java.util.function.UnaryOperator;

/** Define como interpretar a apresentação e quais verificações adicionais aplicar ao CNPJ. */
public enum CnpjValidationMode {
    /** Conferência de DV para bases numéricas e alfanuméricas. É o modo padrão. */
    ALPHANUMERIC(builder -> builder.canonicalRule(notAllZeros()).checkDigits(Modulus11.cnpj())),

    /** Conferência de DV para bases compostas apenas de dígitos. */
    NUMERIC(builder -> builder.canonicalRule(numericBase()).canonicalRule(notAllZeros())
            .checkDigits(Modulus11.cnpj())),

    /** Verifica somente a apresentação, com dois caracteres numéricos na posição do DV. */
    FORMAT_ONLY(builder -> builder);

    private static final String ALL_ZEROS = "00000000000000";

    private final UnaryOperator<DocumentParsers.Builder<Cnpj>> configuration;

    CnpjValidationMode(UnaryOperator<DocumentParsers.Builder<Cnpj>> configuration) {
        this.configuration = configuration;
    }

    DocumentParsers.Builder<Cnpj> configure(DocumentParsers.Builder<Cnpj> builder) {
        return configuration.apply(builder);
    }

    String validatedCanonical(String input) {
        return Cnpj.parser(this).parse(input).map(Cnpj::getCanonicalValue).getDocument().orElse(null);
    }

    /** Bloqueio de 14 zeros (F-05) seguido do módulo 11 do Anexo XV da IN RFB 2.119/2022. */
    static boolean hasCheckDigits(String canonical) {
        return !ALL_ZEROS.equals(canonical) && Modulus11.cnpj().verify(canonical);
    }

    private static ValidationRule<String> notAllZeros() {
        return ValidationRule.of(canonical -> !ALL_ZEROS.equals(canonical),
                Rejection.of(RejectionReason.INVALID_CHECK_DIGITS));
    }

    private static ValidationRule<String> numericBase() {
        return ValidationRule.of(canonical -> CnpjType.of(canonical) == CnpjType.NUMERIC,
                Rejection.of(RejectionReason.UNSUPPORTED_BASE));
    }
}
