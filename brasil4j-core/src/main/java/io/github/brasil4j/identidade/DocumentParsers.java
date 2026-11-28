package io.github.brasil4j.identidade;

import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.CheckDigitAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Montagem de interpretadores de documento. A biblioteca constrói CPF, CNPJ e NIS por este mesmo
 * builder, disponível para documentos do consumidor.
 *
 * <p>O interpretador montado segue a ordem: ausência ({@code null} ou vazio), normalização, regras
 * canônicas, dígitos verificadores, construção do documento e regras de documento. Cada etapa roda no
 * máximo uma vez e a primeira rejeição encerra a interpretação.
 */
public final class DocumentParsers {
    private static final Rejection MISSING = Rejection.of(RejectionReason.MISSING);
    private static final Rejection INVALID_FORMAT = Rejection.of(RejectionReason.INVALID_FORMAT);

    private DocumentParsers() {
    }

    /**
     * Inicia a montagem de um interpretador.
     *
     * @param factory construção do documento a partir do valor canônico já conferido
     * @param <D> documento produzido
     * @return construtor
     * @throws NullPointerException se a fábrica for nula
     */
    public static <D extends Document> Builder<D> builder(Function<String, D> factory) {
        return new Builder<>(Objects.requireNonNull(factory, "Fábrica obrigatória."));
    }

    static <D extends Document> DocumentParser<D> decorate(DocumentParser<D> origin, ValidationRule<? super D> rule) {
        boolean configured = origin instanceof ConfiguredParser;
        String message = configured
                ? ((ConfiguredParser<?>) origin).errorMessage
                : ValidationResult.DEFAULT_ERROR_MESSAGE;
        return new ConfiguredParser<D>(message) {
            @Override
            public ValidationResult<D> parse(String input) {
                return check(origin.parse(input));
            }

            @Override
            public D parseOrThrow(String input) {
                ValidationResult<D> result = origin.parse(input);
                if (!configured && !result.isValid()) {
                    // A rejeição nativa de um interpretador do consumidor mantém a exceção dele.
                    return origin.parseOrThrow(input);
                }
                return check(result).orElseThrow(message);
            }

            private ValidationResult<D> check(ValidationResult<D> result) {
                if (!result.isValid()) {
                    return result;
                }
                Optional<Rejection> rejection = rule.check(result.getDocument().get());
                return rejection.isPresent() ? ValidationResult.invalid(rejection.get()) : result;
            }
        };
    }

    /** Interpretador interno que preserva a mensagem de erro através das decorações. */
    abstract static class ConfiguredParser<D extends Document> implements DocumentParser<D> {
        private final String errorMessage;

        ConfiguredParser(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public D parseOrThrow(String input) {
            return parse(input).orElseThrow(errorMessage);
        }
    }

    /**
     * Construtor mutável de interpretadores. Regras canônicas e de documento se acumulam na ordem das
     * chamadas. A máscara e o normalizador são alternativos.
     *
     * @param <D> documento produzido
     */
    public static final class Builder<D extends Document> {
        private final Function<String, D> factory;
        private final List<ValidationRule<String>> canonicalRules = new ArrayList<>();
        private final List<ValidationRule<? super D>> documentRules = new ArrayList<>();
        private Function<String, Optional<String>> normalizer;
        private boolean masked;
        private CheckDigitAlgorithm checkDigits;
        private String errorMessage = ValidationResult.DEFAULT_ERROR_MESSAGE;

        private Builder(Function<String, D> factory) {
            this.factory = factory;
        }

        /**
         * Normaliza pela máscara: aceita o canônico ou a máscara completa exata.
         *
         * @param mask máscara do documento
         * @return este construtor
         * @throws NullPointerException se a máscara for nula
         * @throws IllegalStateException se um normalizador já tiver sido definido
         */
        public Builder<D> mask(DocumentMask mask) {
            Objects.requireNonNull(mask, "Máscara obrigatória.");
            if (normalizer != null && !masked) {
                throw new IllegalStateException("Normalizador já definido.");
            }
            this.normalizer = mask::unmask;
            this.masked = true;
            return this;
        }

        /**
         * Normaliza por uma função própria, para documentos sem máscara de comprimento fixo.
         *
         * @param normalizer função que devolve o canônico, ou vazio para formato inválido
         * @return este construtor
         * @throws NullPointerException se a função for nula
         * @throws IllegalStateException se uma máscara já tiver sido definida
         */
        public Builder<D> normalizer(Function<String, Optional<String>> normalizer) {
            Objects.requireNonNull(normalizer, "Normalizador obrigatório.");
            if (masked) {
                throw new IllegalStateException("Máscara já definida.");
            }
            this.normalizer = normalizer;
            return this;
        }

        /**
         * Acrescenta uma regra sobre o valor canônico, executada antes do dígito verificador.
         *
         * @param rule regra
         * @return este construtor
         * @throws NullPointerException se a regra for nula
         */
        public Builder<D> canonicalRule(ValidationRule<String> rule) {
            canonicalRules.add(Objects.requireNonNull(rule, "Regra obrigatória."));
            return this;
        }

        /**
         * Define o algoritmo de dígitos verificadores. Uma divergência gera
         * {@link RejectionReason#INVALID_CHECK_DIGITS}; um canônico fora do domínio do algoritmo gera
         * {@link RejectionReason#INVALID_FORMAT}.
         *
         * @param algorithm algoritmo
         * @return este construtor
         * @throws NullPointerException se o algoritmo for nulo
         */
        public Builder<D> checkDigits(CheckDigitAlgorithm algorithm) {
            this.checkDigits = Objects.requireNonNull(algorithm, "Algoritmo obrigatório.");
            return this;
        }

        /**
         * Acrescenta uma regra sobre o documento construído.
         *
         * @param rule regra
         * @return este construtor
         * @throws NullPointerException se a regra for nula
         */
        public Builder<D> documentRule(ValidationRule<? super D> rule) {
            documentRules.add(Objects.requireNonNull(rule, "Regra obrigatória."));
            return this;
        }

        /**
         * Define a mensagem de {@link DocumentParser#parseOrThrow(String)}, que não deve conter dados do
         * documento.
         *
         * @param message mensagem
         * @return este construtor
         * @throws NullPointerException se a mensagem for nula
         */
        public Builder<D> errorMessage(String message) {
            this.errorMessage = Objects.requireNonNull(message, "Mensagem obrigatória.");
            return this;
        }

        /**
         * Cria o interpretador imutável com a configuração atual.
         *
         * @return interpretador
         * @throws IllegalStateException se nenhuma máscara ou normalizador tiver sido definido
         */
        public DocumentParser<D> build() {
            if (normalizer == null) {
                throw new IllegalStateException("Máscara ou normalizador obrigatório.");
            }
            Function<String, D> factory = this.factory;
            Function<String, Optional<String>> normalizer = this.normalizer;
            List<ValidationRule<String>> canonicalRules = Collections.unmodifiableList(new ArrayList<>(this.canonicalRules));
            List<ValidationRule<? super D>> documentRules =
                    Collections.unmodifiableList(new ArrayList<>(this.documentRules));
            ValidationRule<String> checkDigits = this.checkDigits == null ? null : ValidationRule.checkDigits(this.checkDigits);
            return new ConfiguredParser<D>(errorMessage) {
                @Override
                public ValidationResult<D> parse(String input) {
                    if (input == null || input.isEmpty()) {
                        return ValidationResult.invalid(MISSING);
                    }
                    Optional<String> normalized = Objects.requireNonNull(normalizer.apply(input),
                            "Normalizador produziu nulo.");
                    if (!normalized.isPresent()) {
                        return ValidationResult.invalid(INVALID_FORMAT);
                    }
                    String canonical = normalized.get();
                    for (ValidationRule<String> rule : canonicalRules) {
                        Optional<Rejection> rejection = rule.check(canonical);
                        if (rejection.isPresent()) {
                            return ValidationResult.invalid(rejection.get());
                        }
                    }
                    if (checkDigits != null) {
                        Optional<Rejection> rejection = checkDigits.check(canonical);
                        if (rejection.isPresent()) {
                            return ValidationResult.invalid(rejection.get());
                        }
                    }
                    D document = Objects.requireNonNull(factory.apply(canonical), "Fábrica produziu nulo.");
                    for (ValidationRule<? super D> rule : documentRules) {
                        Optional<Rejection> rejection = rule.check(document);
                        if (rejection.isPresent()) {
                            return ValidationResult.invalid(rejection.get());
                        }
                    }
                    return ValidationResult.valid(document);
                }
            };
        }
    }
}
