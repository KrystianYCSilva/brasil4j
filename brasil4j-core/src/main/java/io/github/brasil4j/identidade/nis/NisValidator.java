package io.github.brasil4j.identidade.nis;

import java.util.Objects;
import java.util.function.Predicate;

/** Confere a forma de uma inscrição e permite aplicar uma política do consumidor. */
public final class NisValidator {
    private NisValidator() {
    }

    /**
     * Confere a apresentação de uma inscrição.
     *
     * @param input texto literal a conferir, possivelmente nulo
     * @return se a entrada tem exatamente 11 dígitos ASCII
     */
    public static boolean isValid(String input) {
        return Nis.hasCanonicalFormat(input);
    }

    /**
     * Confere a apresentação e aplica a política ao valor estrutural obtido.
     * A política só é executada para entrada canônica. Suas exceções são propagadas.
     *
     * @param input texto literal a conferir, possivelmente nulo
     * @param policy regra adicional do consumidor, obrigatória
     * @return se a entrada é canônica e atende à política
     * @throws NullPointerException se a política for nula
     */
    public static boolean isValid(String input, Predicate<? super Nis> policy) {
        Objects.requireNonNull(policy, "Política de validação do NIS obrigatória.");
        Nis nis = Nis.parseOrNull(input);
        return nis != null && policy.test(nis);
    }
}
