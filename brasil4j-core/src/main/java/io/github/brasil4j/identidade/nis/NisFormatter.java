package io.github.brasil4j.identidade.nis;

import java.util.Objects;
import java.util.function.Function;

/**
 * Aplica uma apresentação fornecida explicitamente pelo consumidor da inscrição.
 *
 * @deprecated Aplique a função diretamente, como {@code formatter.apply(nis)}, ou use um
 *             {@link io.github.brasil4j.identidade.DocumentFormatter}, por exemplo
 *             {@code DocumentFormatter.secure()}. Remoção prevista para a versão 1.0.
 */
@Deprecated
public final class NisFormatter {
    private NisFormatter() {
    }

    /**
     * Formata um valor estrutural com a função fornecida.
     * A função pode expor os dígitos e suas exceções são propagadas ao chamador.
     * A representação textual padrão da inscrição continua oculta.
     *
     * @param nis inscrição estrutural, obrigatória
     * @param formatter função de formatação, obrigatória
     * @return texto definido pela função
     * @throws NullPointerException se a inscrição, a função ou o resultado forem nulos
     */
    public static String format(Nis nis, Function<? super Nis, String> formatter) {
        Objects.requireNonNull(nis, "Inscrição NIS obrigatória.");
        Objects.requireNonNull(formatter, "Função de formatação do NIS obrigatória.");
        return Objects.requireNonNull(formatter.apply(nis), "Resultado de formatação do NIS obrigatório.");
    }
}
