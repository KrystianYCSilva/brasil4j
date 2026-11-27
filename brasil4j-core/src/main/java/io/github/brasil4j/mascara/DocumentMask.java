package io.github.brasil4j.mascara;

import java.util.Objects;
import java.util.Optional;

/**
 * Máscara declarativa de apresentação. No padrão, {@code 9} aceita um dígito ASCII, {@code A} uma letra
 * ASCII maiúscula, {@code X} um dígito ou letra maiúscula, e qualquer outro caractere é um literal.
 * O valor canônico tem só os caracteres dos slots, na ordem. As instâncias são imutáveis.
 */
public final class DocumentMask {
    private final String pattern;
    private final int slots;

    private DocumentMask(String pattern, int slots) {
        this.pattern = pattern;
        this.slots = slots;
    }

    /**
     * Cria uma máscara a partir do padrão.
     *
     * @param pattern padrão com pelo menos um slot, por exemplo {@code 999.999.999-99}
     * @return máscara
     * @throws NullPointerException se o padrão for nulo
     * @throws IllegalArgumentException se o padrão não tiver slots
     */
    public static DocumentMask of(String pattern) {
        Objects.requireNonNull(pattern, "Padrão obrigatório.");
        int slots = 0;
        for (int index = 0; index < pattern.length(); index++) {
            if (isSlot(pattern.charAt(index))) {
                slots++;
            }
        }
        if (slots == 0) {
            throw new IllegalArgumentException("Padrão de máscara sem slots.");
        }
        return new DocumentMask(pattern, slots);
    }

    /**
     * Quantidade de caracteres do valor canônico.
     *
     * @return número de slots do padrão
     */
    public int canonicalLength() {
        return slots;
    }

    /**
     * Obtém o valor canônico a partir da forma canônica ou da máscara completa exata.
     *
     * @param input texto a interpretar, possivelmente nulo
     * @return valor canônico, ou vazio quando o texto não corresponde a nenhuma das duas formas
     */
    public Optional<String> unmask(String input) {
        if (input == null) {
            return Optional.empty();
        }
        if (isCanonical(input)) {
            return Optional.of(input);
        }
        if (input.length() != pattern.length()) {
            return Optional.empty();
        }
        StringBuilder canonical = new StringBuilder(slots);
        for (int index = 0; index < pattern.length(); index++) {
            char expected = pattern.charAt(index);
            char actual = input.charAt(index);
            if (isSlot(expected)) {
                if (!accepts(expected, actual)) {
                    return Optional.empty();
                }
                canonical.append(actual);
            } else if (expected != actual) {
                return Optional.empty();
            }
        }
        return Optional.of(canonical.toString());
    }

    /**
     * Aplica a máscara completa, sem ocultação.
     *
     * @param canonical valor canônico conforme os slots
     * @return valor com a máscara
     * @throws NullPointerException se o valor for nulo
     * @throws IllegalArgumentException se o valor não corresponder aos slots
     */
    public String apply(String canonical) {
        return applySecure(canonical, 0, 0, '*');
    }

    /**
     * Aplica a máscara e oculta os primeiros e os últimos slots, preservando os literais.
     * Quando a soma das quantidades alcança o número de slots, todos os slots são ocultados.
     *
     * @param canonical valor canônico conforme os slots
     * @param hiddenPrefix quantidade de slots ocultos no início, não negativa
     * @param hiddenSuffix quantidade de slots ocultos no fim, não negativa
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @return valor com a máscara de segurança
     * @throws NullPointerException se o valor for nulo
     * @throws IllegalArgumentException se o valor não corresponder aos slots, se alguma quantidade for
     *         negativa ou se o caractere for letra ou dígito
     */
    public String applySecure(String canonical, int hiddenPrefix, int hiddenSuffix, char maskChar) {
        Objects.requireNonNull(canonical, "Valor canônico obrigatório.");
        if (hiddenPrefix < 0 || hiddenSuffix < 0) {
            throw new IllegalArgumentException("Quantidade de ocultação negativa.");
        }
        if (Character.isLetterOrDigit(maskChar)) {
            throw new IllegalArgumentException("Caractere de ocultação não pode ser letra nem dígito.");
        }
        if (!isCanonical(canonical)) {
            throw new IllegalArgumentException("Valor fora dos slots da máscara.");
        }
        StringBuilder output = new StringBuilder(pattern.length());
        int slot = 0;
        for (int index = 0; index < pattern.length(); index++) {
            char expected = pattern.charAt(index);
            if (isSlot(expected)) {
                boolean hidden = slot < hiddenPrefix || slot >= slots - hiddenSuffix;
                output.append(hidden ? maskChar : canonical.charAt(slot));
                slot++;
            } else {
                output.append(expected);
            }
        }
        return output.toString();
    }

    private boolean isCanonical(String value) {
        if (value.length() != slots) {
            return false;
        }
        int slot = 0;
        for (int index = 0; index < pattern.length(); index++) {
            char expected = pattern.charAt(index);
            if (isSlot(expected) && !accepts(expected, value.charAt(slot++))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSlot(char character) {
        return character == '9' || character == 'A' || character == 'X';
    }

    private static boolean accepts(char slot, char character) {
        boolean digit = character >= '0' && character <= '9';
        boolean letter = character >= 'A' && character <= 'Z';
        return slot == '9' ? digit : slot == 'A' ? letter : digit || letter;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return other instanceof DocumentMask && pattern.equals(((DocumentMask) other).pattern);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    /**
     * Representa a máscara pelo seu padrão.
     *
     * @return padrão da máscara
     */
    @Override
    public String toString() {
        return pattern;
    }
}
