package io.github.brasil4j.identidade.nis;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Confere a forma de uma inscrição e permite aplicar uma política do consumidor.
 *
 * @deprecated Use o contrato injetável {@link Nis#parser()}, por exemplo
 *             {@code Nis.parser().withPolicy(policy).isValid(input)}, ou uma configuração de
 *             {@link NisValidationMode}. Remoção prevista para a versão 1.0.
 */
@Deprecated
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
        return Nis.parser().isValid(input);
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
        return Nis.parser().withPolicy(policy).isValid(input);
    }
}
