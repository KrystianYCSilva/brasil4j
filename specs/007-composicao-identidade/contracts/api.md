# Contrato público planejado · Spec 007

## Java · `brasil4j-core`

Os novos tipos ficam no pacote `io.github.brasil4j.identidade.composicao`. `Cpf`, `Cnpj`, `Nis` e seus modos são os tipos do núcleo já existentes.

### `public final class DocumentCheck<T>`

| Assinatura | Contrato |
| --- | --- |
| `public static DocumentCheck<Cpf> cpf(String input, CpfValidationMode mode)` | Configura CPF em `FORMAT_ONLY` ou `NUMERIC`. |
| `public static DocumentCheck<Cnpj> cnpj(String input, CnpjValidationMode mode)` | Configura CNPJ em `FORMAT_ONLY`, `NUMERIC` ou `ALPHANUMERIC`. |
| `public static DocumentCheck<Nis> nis(String input)` | Configura a conferência estrutural da Spec 006. A política e a saída podem ser acrescentadas pelo consumidor. |
| `public DocumentCheck<T> withPolicy(Predicate<? super T> policy)` | Devolve um item com a política informada. Substitui uma política anterior nesse novo item. Para acumular regras, o consumidor pode combinar predicados com `and`. |
| `public DocumentCheck<T> withFormatter(Function<? super T, String> formatter)` | Devolve um item com o formatador informado. Substitui um formatador anterior nesse novo item. |
| `public DocumentCheckResult evaluate()` | Confere o texto uma vez, aplica a política opcional e produz a apresentação opcional quando houver aceitação. Cada avaliação é síncrona e independente. |

As fábricas aceitam `input` nulo para permitir montar itens a partir de campos ausentes. O modo de CPF e CNPJ é obrigatório. Cada fábrica começa sem política adicional e sem saída configurada. Modo nulo ou callback passado explicitamente como nulo lança `NullPointerException` durante a montagem. `withPolicy` e `withFormatter` preservam o item de origem e a configuração da outra etapa.

### `public final class DocumentCheckResult`

| Assinatura | Contrato |
| --- | --- |
| `public Status getStatus()` | Um valor de `DocumentCheckResult.Status`: `REJECTED_BY_VALIDATION`, `REJECTED_BY_POLICY` ou `ACCEPTED`. |
| `public Optional<String> getFormattedValue()` | Um `Optional` não nulo. É presente quando um item aceito executa o formatador e obtém texto não nulo, inclusive `""` ou espaços. Nos demais resultados é vazio. |

Os dois tipos têm construção controlada pela biblioteca, sem construtor público. O resultado oferece decisão e saída. A posição e os rótulos ficam com o consumidor. A representação diagnóstica dos dois tipos contém somente informação estrutural sob controle da biblioteca. Não se define igualdade por conteúdo para esses tipos auxiliares.

### Sequência e falhas

1. `evaluate()` aplica o modo já associado à fábrica. Entrada rejeitada pela regra nativa, inclusive nulo, vazio ou só espaços, gera `REJECTED_BY_VALIDATION`. Política e formatador recebem zero chamadas.
2. A política configurada recebe a instância produzida pela fábrica, uma vez. `false` gera `REJECTED_BY_POLICY`, sem apresentar o documento. A aceitação é relativa ao modo e à política do item.
3. Na aceitação, o formatador configurado recebe a mesma instância, uma vez. Para formato nativo de CPF ou CNPJ, o consumidor pode passar `Cpf::applyMask` ou `Cnpj::applyMask`. Em NIS, pode escolher qualquer apresentação adequada ao fluxo. Retorno nulo do formatador gera `NullPointerException` durante `evaluate()`.
4. A biblioteca propaga a mesma exceção lançada pela política ou pelo formatador. As mensagens que a biblioteca produz para erros de configuração e saída nula omitem entrada, forma canônica e texto formatado. A saída do formatador pode conter o documento e é de recuperação explícita.

## Validador de NIS e composição

`Predicate<? super Nis>` é o contrato de uma regra que o consumidor quer acrescentar **depois da conferência estrutural**. Um `Predicate<Nis>` pode ser passado diretamente a `NisValidator.isValid(input, policy)` ou a `DocumentCheck.nis(input).withPolicy(policy)`. Na primeira forma, o consumidor obtém o booleano da Spec 006. Na lista, obtém a decisão da conferência e pode configurar separadamente a apresentação. Regras locais podem conferir DV, aplicar critérios de aceitação do importador ou combinar condições sobre o `Nis` canônico. O exemplo da UEM adapta `ValidationUtils.isPis(nis.getCanonicalValue())` no próprio sistema consumidor.

## Kotlin · consumo do contrato Java

- A lista tem tipo `List<DocumentCheck<*>>`. As fábricas inferem os tipos Java concretos de `T`. Lambdas e referências de método são convertidas em `Predicate` e `Function` por SAM. O parâmetro da política de NIS é o `Nis` Java, igual ao da extensão `isValidNis(policy)` da Spec 006.
- O texto pode ser `String?`. O resultado usa as propriedades Java vistas no Kotlin como `status` e `formattedValue`. Para converter a saída explicitamente, use `val saida: String? = resultado.formattedValue.orElse(null)`. Um `""` continua distinto de `null`.
- O artefato Kotlin mantém as value classes e as extensões existentes. Para esta etapa, o acesso à nova operação usa o núcleo Java, sem criar assinaturas Kotlin duplicadas nem exigir casts da lista. Veja os [exemplos de uso](../quickstart.md).
