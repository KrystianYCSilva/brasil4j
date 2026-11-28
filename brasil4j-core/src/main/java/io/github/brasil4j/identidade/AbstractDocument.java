package io.github.brasil4j.identidade;

import java.util.Objects;

/**
 * Implementação esqueleto de um documento identificado pela classe concreta e pelo valor canônico.
 * Destina-se a valores sem estado além do canônico. A representação textual é oculta por padrão.
 *
 * <p>A subclasse é responsável por validar o valor canônico antes de chamar o construtor; esta classe
 * só recusa {@code null}.
 */
public abstract class AbstractDocument implements Document {
    private final String canonical;

    /**
     * Inicializa o documento com o valor canônico já validado.
     *
     * @param canonical valor canônico
     * @throws NullPointerException se o valor for nulo
     */
    protected AbstractDocument(String canonical) {
        this.canonical = Objects.requireNonNull(canonical, "Valor canônico obrigatório.");
    }

    /** {@inheritDoc} */
    @Override
    public String getCanonicalValue() {
        return canonical;
    }

    /**
     * Compara pela classe concreta e pelo valor canônico. Subclasses não devem redefinir a igualdade;
     * o método não é final apenas para preservar a compatibilidade binária dos tipos já publicados.
     *
     * @param other objeto a comparar
     * @return verdadeiro para a mesma classe e o mesmo valor canônico
     */
    @Override
    public boolean equals(Object other) {
        return this == other || other != null && getClass() == other.getClass()
                && canonical.equals(((AbstractDocument) other).canonical);
    }

    /**
     * Calcula o hash pelo valor canônico, coerente com {@link #equals(Object)}.
     *
     * @return hash do valor canônico
     */
    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    /**
     * Representa o documento sem revelar o conteúdo. Subclasses podem expor o canônico quando ele for público.
     *
     * @return marcador fixo de ocultação
     */
    @Override
    public String toString() {
        return "[documento oculto]";
    }
}
