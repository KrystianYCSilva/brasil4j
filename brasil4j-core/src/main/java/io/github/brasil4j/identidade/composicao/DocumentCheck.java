package io.github.brasil4j.identidade.composicao;

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
 * @param <T> tipo do valor construído pela regra nativa
 */
public final class DocumentCheck<T> {
    private final String input;
    private final Function<String, T> parser;
    private final Predicate<? super T> policy;
    private final Function<? super T, String> formatter;

    private DocumentCheck(String input, Function<String, T> parser,
                          Predicate<? super T> policy, Function<? super T, String> formatter) {
        this.input = input;
        this.parser = parser;
        this.policy = policy;
        this.formatter = formatter;
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
        return new DocumentCheck<>(input, text -> Cpf.parse(text, mode), null, null);
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
        return new DocumentCheck<>(input, text -> Cnpj.parse(text, mode), null, null);
    }

    /**
     * Prepara uma conferência estrutural de NIS.
     *
     * @param input texto de entrada, que pode ser nulo
     * @return item de conferência
     */
    public static DocumentCheck<Nis> nis(String input) {
        return new DocumentCheck<>(input, Nis::parse, null, null);
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
     * Constrói o valor pela regra nativa, aplica a política e só então formata um valor aceito.
     * Cada chamada é síncrona e repete as etapas. Exceções da política e do formatador
     * são propagadas ao chamador.
     *
     * @return decisão da avaliação
     * @throws NullPointerException se o formatador produzir nulo
     */
    public DocumentCheckResult evaluate() {
        T document;
        try {
            document = parser.apply(input);
        } catch (IllegalArgumentException invalidInput) {
            return new DocumentCheckResult(DocumentCheckResult.Status.REJECTED_BY_VALIDATION, null);
        }

        if (policy != null && !policy.test(document)) {
            return new DocumentCheckResult(DocumentCheckResult.Status.REJECTED_BY_POLICY, null);
        }

        String formattedValue = formatter == null ? null
                : Objects.requireNonNull(formatter.apply(document), "Saída do formatador obrigatória");
        return new DocumentCheckResult(DocumentCheckResult.Status.ACCEPTED, formattedValue);
    }
}
