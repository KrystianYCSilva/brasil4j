package io.github.brasil4j.verificacao;

/**
 * Sinaliza que a base recebida está fora do domínio do algoritmo de dígitos verificadores, por exemplo
 * por comprimento ou caractere não aceito. É uma entrada inválida, não um defeito de configuração.
 *
 * <p>{@code ValidationRule.checkDigits} converte esta exceção em rejeição de formato.
 * A regra também reconhece outras {@link IllegalArgumentException} para compatibilidade
 * com algoritmos legados.
 */
public class InvalidBaseException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção.
     *
     * @param message descrição da violação, sem o conteúdo do documento
     */
    public InvalidBaseException(String message) {
        super(message);
    }
}
