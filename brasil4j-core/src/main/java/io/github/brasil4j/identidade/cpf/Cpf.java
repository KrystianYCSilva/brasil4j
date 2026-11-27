package io.github.brasil4j.identidade.cpf;

import io.github.brasil4j.identidade.AbstractDocument;
import io.github.brasil4j.identidade.CheckDigitDocument;
import io.github.brasil4j.identidade.MaskedDocument;
import io.github.brasil4j.identidade.SecurityMaskedDocument;
import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.Modulus11;

import static io.github.brasil4j.identidade.cpf.CpfValidationMode.FORMAT_ONLY;

/**
 * Valor imutável de um CPF, com igualdade pelo valor canônico.
 * A construção padrão confere a apresentação. A conferência do DV é opcional.
 */
public final class Cpf extends AbstractDocument
        implements MaskedDocument, SecurityMaskedDocument, CheckDigitDocument {
    static final DocumentMask MASK = DocumentMask.of("999.999.999-99");

    private Cpf(String canonical) {
        super(canonical);
    }

    /**
     * Interpreta um CPF com verificação de formato.
     *
     * @param input CPF canônico ou com máscara completa
     * @return valor do CPF
     * @throws IllegalArgumentException se o formato da entrada for inválido
     */
    public static Cpf parse(String input) {
        return parse(input, FORMAT_ONLY);
    }

    /**
     * Interpreta um CPF com o nível de verificação informado.
     *
     * @param input CPF canônico ou com máscara completa
     * @param mode nível de verificação
     * @return valor do CPF
     * @throws IllegalArgumentException se a entrada for inválida ou se o modo for nulo
     */
    public static Cpf parse(String input, CpfValidationMode mode) {
        String canonical = CpfValidator.validatedCanonical(input, mode);
        if (canonical == null) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        return new Cpf(canonical);
    }

    /**
     * Obtém o valor canônico.
     *
     * @return os 11 dígitos informados, sem pontuação e com zeros preservados
     */
    @Override
    public String getCanonicalValue() {
        return super.getCanonicalValue();
    }

    /**
     * Obtém a base do CPF.
     *
     * @return os nove dígitos da base
     */
    @Override
    public String getBase() {
        return getCanonicalValue().substring(0, 9);
    }

    /**
     * Obtém os dígitos verificadores.
     *
     * @return os dois dígitos verificadores informados, inclusive no modo de formato
     */
    @Override
    public String getCheckDigits() {
        return getCanonicalValue().substring(9);
    }

    /**
     * Compara os dígitos informados ao resultado do módulo 11.
     *
     * @return se os dígitos informados coincidem com o cálculo pelo módulo 11
     */
    @Override
    public boolean hasValidCheckDigits() {
        return Modulus11.cpf().verify(getCanonicalValue());
    }

    /**
     * Aplica a máscara completa.
     *
     * @return o CPF com máscara completa, exibindo todos os dígitos
     */
    @Override
    public String applyMask() {
        return MASK.apply(getCanonicalValue());
    }

    /**
     * Aplica a máscara de segurança, que oculta os três primeiros dígitos e os dois DVs,
     * como em {@code ***.345.678-**}.
     *
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @return o CPF parcialmente ocultado
     * @throws IllegalArgumentException se o caractere for letra ou dígito
     */
    @Override
    public String applySecurityMask(char maskChar) {
        return MASK.applySecure(getCanonicalValue(), 3, 2, maskChar);
    }

    /**
     * Representa o CPF em texto com ocultação dos dígitos.
     *
     * @return uma representação que oculta todos os dígitos do CPF
     */
    @Override
    public String toString() {
        return "***.***.***-**";
    }
}
