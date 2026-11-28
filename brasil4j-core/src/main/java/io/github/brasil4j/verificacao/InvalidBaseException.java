package io.github.brasil4j.verificacao;

/**
 * Sinaliza que a base recebida está fora do domínio do algoritmo de dígitos verificadores, por exemplo
 * por comprimento ou caractere não aceito. É uma entrada inválida, não um defeito de configuração.
 *
 * <p>Estende {@link IllegalArgumentException}, de modo que quem já captura essa exceção continua
 * funcionando. Uma {@code IllegalArgumentException} que não seja desta classe indica defeito do
 * algoritmo e não é convertida em rejeição de formato por {@code ValidationRule.checkDigits}.
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
