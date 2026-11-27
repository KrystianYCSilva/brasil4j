package io.github.brasil4j.identidade.composicao;

import io.github.brasil4j.identidade.Document;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import io.github.brasil4j.identidade.ValidationResult;
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Configuração imutável e reutilizável de uma conferência de documento.
 * A representação textual padrão do item omite o conteúdo do documento.
 *
 * <p>O item aceita qualquer {@link DocumentParser}, inclusive de documentos do consumidor. A política
 * do item, definida por {@link #withPolicy(Predicate)}, substitui a anterior e roda depois do
 * interpretador. As regras do próprio interpretador se encadeiam.
 *
 * @param <T> tipo do valor construído pela regra nativa
 */
public final class DocumentCheck<T> {
    private static final Rejection POLICY = Rejection.of(RejectionReason.POLICY);

    private final String input;
    private final Function<String, ValidationResult<T>> parser;
    private final Predicate<? super T> policy;
    private final Function<? super T, String> formatter;

    private DocumentCheck(String input, Function<String, ValidationResult<T>> parser,
                          Predicate<? super T> policy, Function<? super T, String> formatter) {
        this.input = input;
        this.parser = parser;
        this.policy = policy;
        this.formatter = formatter;
    }

    /**
     * Prepara uma conferência com qualquer interpretador.
     *
     * @param input texto de entrada, que pode ser nulo
     * @param parser interpretador obrigatório
     * @param <D> documento interpretado
     * @return item de conferência
     * @throws NullPointerException se o interpretador for nulo
     */
    public static <D extends Document> DocumentCheck<D> of(String input, DocumentParser<D> parser) {
        Objects.requireNonNull(parser, "Interpretador obrigatório");
        return new DocumentCheck<>(input, parser::parse, null, null);
    }

    /**
     * Prepara uma conferência de CPF com o modo indicado.
     *
     * @param input texto de entrada, que pode ser nulo
     * @param mode modo obrigatório de validação
     * @return item de conferência
     * @throws NullPointerException se o modo for nulo
     */
    public static DocumentCheck<Cpf> cpf(String input, CpfValidationMode mode) {
        Objects.requireNonNull(mode, "Modo de validação obrigatório");
        return of(input, Cpf.parser(mode));
    }

    /**
     * Prepara uma conferência de CNPJ com o modo indicado.
     *
     * @param input texto de entrada, que pode ser nulo
     * @param mode modo obrigatório de validação
     * @return item de conferência
     * @throws NullPointerException se o modo for nulo
     */
    public static DocumentCheck<Cnpj> cnpj(String input, CnpjValidationMode mode) {
        Objects.requireNonNull(mode, "Modo de validação obrigatório");
        return of(input, Cnpj.parser(mode));
    }

    /**
     * Prepara uma conferência estrutural de NIS.
     *
     * @param input texto de entrada, que pode ser nulo
     * @return item de conferência
     */
    public static DocumentCheck<Nis> nis(String input) {
        return of(input, Nis.parser());
    }

    /**
     * Substitui a regra adicional aplicada depois da conferência nativa.
     *
     * @param policy regra aplicada ao valor aceito na etapa nativa
     * @return item com a nova regra, preservando o item de origem
     * @throws NullPointerException se a regra for nula
     */
    public DocumentCheck<T> withPolicy(Predicate<? super T> policy) {
        return new DocumentCheck<>(input, parser, Objects.requireNonNull(policy, "Política obrigatória"), formatter);
    }

    /**
     * Substitui a apresentação da entrada aceita em uma nova conferência.
     *
     * @param formatter função que produz a apresentação
     * @return item com a nova função, preservando o item de origem
     * @throws NullPointerException se a função for nula
     */
    public DocumentCheck<T> withFormatter(Function<? super T, String> formatter) {
        return new DocumentCheck<>(input, parser, policy,
                Objects.requireNonNull(formatter, "Formatador obrigatório"));
    }

    /**
     * Interpreta a entrada uma vez, aplica a política e só então formata um valor aceito.
     * Cada chamada é síncrona e repete as etapas. Exceções da política e do formatador
     * são propagadas ao chamador.
     *
     * @return decisão da avaliação
     * @throws NullPointerException se o formatador produzir nulo
     */
    public DocumentCheckResult evaluate() {
        ValidationResult<T> result = parser.apply(input);
        if (!result.isValid()) {
            Rejection rejection = result.getRejection().get();
            DocumentCheckResult.Status status = rejection.getReason() == RejectionReason.POLICY
                    ? DocumentCheckResult.Status.REJECTED_BY_POLICY
                    : DocumentCheckResult.Status.REJECTED_BY_VALIDATION;
            return new DocumentCheckResult(status, null, rejection);
        }
        T document = result.getDocument().get();

        if (policy != null && !policy.test(document)) {
            return new DocumentCheckResult(DocumentCheckResult.Status.REJECTED_BY_POLICY, null, POLICY);
        }

        String formattedValue = formatter == null ? null
                : Objects.requireNonNull(formatter.apply(document), "Saída do formatador obrigatória");
        return new DocumentCheckResult(DocumentCheckResult.Status.ACCEPTED, formattedValue, null);
    }
}
