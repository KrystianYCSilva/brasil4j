package io.github.brasil4j.identidade.cpf;

import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.verificacao.Modulus11;

import java.util.function.UnaryOperator;

/** Define como interpretar a apresentação do CPF e se os dígitos verificadores serão conferidos. */
public enum CpfValidationMode {
    /** Verifica apenas tamanho e formato. É o modo padrão. */
    FORMAT_ONLY(builder -> builder),

    /** Acrescenta a conferência matemática dos dígitos verificadores pelo módulo 11. */
    NUMERIC(builder -> builder.checkDigits(Modulus11.cpf()));

    private final UnaryOperator<DocumentParsers.Builder<Cpf>> configuration;

    CpfValidationMode(UnaryOperator<DocumentParsers.Builder<Cpf>> configuration) {
        this.configuration = configuration;
    }

    DocumentParsers.Builder<Cpf> configure(DocumentParsers.Builder<Cpf> builder) {
        return configuration.apply(builder);
    }

    String validatedCanonical(String input) {
        return Cpf.parser(this).parse(input).map(Cpf::getCanonicalValue).getDocument().orElse(null);
    }
}
