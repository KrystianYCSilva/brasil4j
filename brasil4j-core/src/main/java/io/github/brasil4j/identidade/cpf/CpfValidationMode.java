package io.github.brasil4j.identidade.cpf;

import io.github.brasil4j.verificacao.Modulus11;

import java.util.function.UnaryOperator;

/** Define como interpretar a apresentação do CPF e se os dígitos verificadores serão conferidos. */
public enum CpfValidationMode {
    /** Verifica apenas tamanho e formato. É o modo padrão. */
    FORMAT_ONLY(CpfFormatter::canonicalize),

    /** Acrescenta a conferência matemática dos dígitos verificadores pelo módulo 11. */
    NUMERIC(input -> {
        String canonical = CpfFormatter.canonicalize(input);
        return canonical != null && Modulus11.cpf().verify(canonical) ? canonical : null;
    });

    private final UnaryOperator<String> strategy;

    CpfValidationMode(UnaryOperator<String> strategy) {
        this.strategy = strategy;
    }

    String validatedCanonical(String input) {
        return strategy.apply(input);
    }
}
