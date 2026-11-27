package io.github.brasil4j.identidade.cnpj;

import io.github.brasil4j.identidade.AbstractDocument;
import io.github.brasil4j.identidade.CheckDigitDocument;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.identidade.MaskedDocument;
import io.github.brasil4j.identidade.SecurityMaskedDocument;
import io.github.brasil4j.mascara.DocumentMask;

import java.util.EnumMap;
import java.util.Map;

import static io.github.brasil4j.identidade.cnpj.CnpjValidationMode.ALPHANUMERIC;

/**
 * Valor imutável de um CNPJ completo, com igualdade pelo valor canônico.
 * A construção padrão verifica a estrutura e os DVs. A existência da inscrição é cadastral.
 */
public final class Cnpj extends AbstractDocument
        implements MaskedDocument, SecurityMaskedDocument, CheckDigitDocument {
    static final DocumentMask MASK = DocumentMask.of("XX.XXX.XXX/XXXX-99");
    private static final Map<CnpjValidationMode, DocumentParser<Cnpj>> PARSERS = new EnumMap<>(CnpjValidationMode.class);

    static {
        for (CnpjValidationMode mode : CnpjValidationMode.values()) {
            PARSERS.put(mode, mode.configure(DocumentParsers.builder(Cnpj::new).mask(MASK))
                    .errorMessage("CNPJ inválido.").build());
        }
    }

    private Cnpj(String canonical) {
        super(canonical);
    }

    /**
     * Obtém o interpretador do modo padrão alfanumérico, que confere os DVs e aceita também bases numéricas.
     *
     * @return interpretador reutilizável
     */
    public static DocumentParser<Cnpj> parser() {
        return parser(ALPHANUMERIC);
    }

    /**
     * Obtém o interpretador do modo informado.
     *
     * @param mode nível de verificação
     * @return interpretador reutilizável
     * @throws IllegalArgumentException se o modo for nulo
     */
    public static DocumentParser<Cnpj> parser(CnpjValidationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Modo de validação do CNPJ obrigatório.");
        }
        return PARSERS.get(mode);
    }

    /**
     * Interpreta um CNPJ no modo alfanumérico, que aceita também CNPJs numéricos.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @return valor do CNPJ
     * @throws IllegalArgumentException se a entrada for inválida
     */
    public static Cnpj parse(String input) {
        return parse(input, ALPHANUMERIC);
    }

    /**
     * Interpreta um CNPJ com o nível de verificação informado.
     *
     * @param input CNPJ canônico ou com máscara completa
     * @param mode nível de verificação
     * @return valor do CNPJ
     * @throws IllegalArgumentException se a entrada for inválida ou se o modo for nulo
     */
    public static Cnpj parse(String input, CnpjValidationMode mode) {
        return parser(mode).parseOrThrow(input);
    }

    /**
     * Obtém a representação canônica.
     *
     * @return os 14 caracteres do CNPJ, sem pontuação e com zeros preservados
     */
    @Override
    public String getCanonicalValue() {
        return super.getCanonicalValue();
    }

    /**
     * Obtém a base do documento.
     *
     * @return a base de 12 caracteres, composta de raiz e ordem
     */
    @Override
    public String getBase() {
        return getCanonicalValue().substring(0, 12);
    }

    /**
     * Obtém a raiz do documento.
     *
     * @return os oito caracteres da raiz
     */
    public String getRoot() {
        return getCanonicalValue().substring(0, 8);
    }

    /**
     * Obtém a ordem do estabelecimento.
     *
     * @return os quatro caracteres da ordem do estabelecimento
     */
    public String getBranch() {
        return getCanonicalValue().substring(8, 12);
    }

    /**
     * Obtém os dígitos verificadores.
     *
     * @return os dois dígitos verificadores informados
     */
    @Override
    public String getCheckDigits() {
        return getCanonicalValue().substring(12);
    }

    /**
     * Confere os DVs pelo módulo 11 do Anexo XV da IN RFB 2.119/2022, com o bloqueio de 14 zeros (F-05).
     *
     * @return se os dígitos informados coincidem com o cálculo e o valor não é formado só por zeros
     */
    @Override
    public boolean hasValidCheckDigits() {
        return CnpjValidationMode.hasCheckDigits(getCanonicalValue());
    }

    /**
     * Classifica a base de acordo com sua composição.
     *
     * @return o tipo numérico ou alfanumérico, conforme a base
     */
    public CnpjType getType() {
        return CnpjType.of(getCanonicalValue());
    }

    /**
     * Formata o valor com a máscara completa.
     *
     * @return o CNPJ com máscara completa, preservando a base e os DVs do valor
     */
    @Override
    public String applyMask() {
        return MASK.apply(getCanonicalValue());
    }

    /**
     * Aplica a máscara de segurança, que oculta os dois primeiros caracteres e os dois DVs,
     * como em {@code **.ABC.345/01DE-**}.
     *
     * @param maskChar caractere de ocultação, que não pode ser letra nem dígito
     * @return o CNPJ parcialmente ocultado
     * @throws IllegalArgumentException se o caractere for letra ou dígito
     */
    @Override
    public String applySecurityMask(char maskChar) {
        return MASK.applySecure(getCanonicalValue(), 2, 2, maskChar);
    }

    /**
     * Representa o documento pelo seu valor canônico.
     *
     * @return o texto canônico do CNPJ
     */
    @Override
    public String toString() {
        return getCanonicalValue();
    }
}
