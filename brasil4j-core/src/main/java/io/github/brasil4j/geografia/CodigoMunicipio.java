package io.github.brasil4j.geografia;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Código estrutural de município do IBGE. A projeção identifica a UF pelo prefixo.
 * Consulta de existência, vigência cadastral e conferência do dígito verificador
 * pertencem ao catálogo ou ao consumidor.
 */
public final class CodigoMunicipio {
    private static final Pattern FORMATO = Pattern.compile("[0-9]{7}");

    private final String valorCanonico;

    private CodigoMunicipio(String valorCanonico) {
        this.valorCanonico = valorCanonico;
    }

    /**
     * Interpreta exatamente sete dígitos ASCII, sem alterar a apresentação.
     *
     * @param entrada código textual
     * @return código com sua representação original
     * @throws IllegalArgumentException se a entrada for nula ou estiver fora do formato
     */
    public static CodigoMunicipio parse(String entrada) {
        if (entrada == null || !FORMATO.matcher(entrada).matches()) {
            throw new IllegalArgumentException("O código de município deve conter sete dígitos ASCII.");
        }
        return new CodigoMunicipio(entrada);
    }

    /**
     * Retorna o código preservando os sete dígitos, inclusive zeros à esquerda.
     *
     * @return valor canônico
     */
    public String getCanonicalValue() {
        return valorCanonico;
    }

    /**
     * Retorna os dois primeiros dígitos, que representam o prefixo de UF.
     *
     * @return prefixo textual, inclusive quando desconhecido
     */
    public String getUfCode() {
        return valorCanonico.substring(0, 2);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object outro) {
        return this == outro || outro instanceof CodigoMunicipio
                && valorCanonico.equals(((CodigoMunicipio) outro).valorCanonico);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return valorCanonico.hashCode();
    }

    /**
     * Retorna a apresentação canônica do código público.
     *
     * @return os sete dígitos do código
     */
    @Override
    public String toString() {
        return valorCanonico;
    }

    /**
     * Resolve a unidade federativa associada ao prefixo do código.
     *
     * @return UF conhecida ou vazio para prefixo sem correspondência
     */
    public Optional<Uf> getUf() {
        return Uf.fromIbgeCode(getUfCode());
    }

    /**
     * Confere se a unidade federativa informada corresponde ao prefixo.
     *
     * @param uf unidade federativa a conferir
     * @return verdadeiro para correspondência de prefixo, ou falso para UF nula ou divergente
     */
    public boolean matchesUf(Uf uf) {
        return uf != null && uf.getIbgeCode().equals(getUfCode());
    }
}
