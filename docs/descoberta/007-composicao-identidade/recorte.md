# Descoberta da Spec 007 · Composição de validação e formatação

**Data** 2026-11-27 · **Base** `5fd9dae` (integra a Spec 006) · **Estado** recorte revisado, pronto para formalização. [Pareceres e decisões](revisao.md)

## Primeiro caso de uso

Um integrador confere um conjunto ordenado de campos cujo tipo já vem do leiaute ou do cadastro de origem. O conjunto pode reunir CPF, CNPJ e NIS de registros distintos. Para cada campo, o consumidor associa o texto ao tipo, informa o modo nativo quando aplicável e escolhe se há regra adicional ou apresentação. O integrador consegue identificar o resultado pela posição ou pelo rótulo mantido no seu fluxo. Um CPF e um NIS podem ter o mesmo comprimento e o mesmo texto, portanto a procedência do campo orienta a escolha da regra.

## Comportamento de partida

| Documento | Validação na entrada | Formatação explícita |
| --- | --- | --- |
| CPF | `CpfValidator` aceita texto canônico ou a máscara completa. O padrão `FORMAT_ONLY` confere a forma e `NUMERIC` acrescenta o DV. | `CpfFormatter.format(String)` usa o parse padrão. `Cpf.applyMask()` atua sobre um valor construído. |
| CNPJ | `CnpjValidator` usa por padrão `ALPHANUMERIC` com DV, que também admite bases numéricas. Há ainda `NUMERIC` com DV e `FORMAT_ONLY`, que admite base numérica ou alfanumérica ASCII em maiúsculas, com ou sem máscara completa. | `CnpjFormatter.format(String)` usa o parse padrão. `Cnpj.applyMask()` atua sobre um valor construído no modo escolhido. |
| NIS | Forma literal de 11 dígitos ASCII e zeros à esquerda. A sobrecarga de `NisValidator` executa a política apenas após a forma. | `NisFormatter` recebe um `Nis` estrutural e uma função explícita do consumidor. |

Os valores Java são finais e os três tipos Kotlin são value classes que delegam ao núcleo. CPF e NIS ocultam o valor em `toString()`. O `toString()` atual de CNPJ devolve o texto canônico. A formatação pública pode revelar o documento. O fluxo composto precisa manter decisões e diagnósticos livres do texto bruto, da forma canônica e da saída formatada por padrão.

## Alternativas

| Alternativa | Vantagem | Critério para a Spec 007 |
| --- | --- | --- |
| Funções do JDK, com `Predicate`/`Function` tipados e operações vinculadas à entrada antes de entrar na lista | Já funciona com a API atual. Mantém interoperabilidade Java 8 e reduz superfície pública. | É o controle da comparação. Listas paralelas de `BooleanSupplier` e `Supplier<String>` exigem coordenação de índice e permitem que a saída seja acionada sem a checagem. Uma função única por item evita essa lacuna, mas cada consumidor ainda teria de definir e aplicar a mesma semântica de resultado. |
| Contratos funcionais próprios no ponto de variação e item que associa tipo, checagem e apresentação | Preserva a associação entre entrada, modo e saída em uma única lista. O compilador pode verificar a associação no ponto em que cada item é montado. | Preferir as interfaces do JDK enquanto elas expressem o contrato. Uma interface nova precisa comunicar ou impor uma semântica recorrente que as funções atuais deixariam a cada consumidor. O contrato de um item pronto para execução é um candidato mais concreto que um `Validator<T>` ou `Formatter<T>` que só renomeia `test` e `apply`. |
| Superinterface nos valores ou despacho genérico sobre `Object`, strings ou tipo em runtime | Oferece uma coleção de valores sob um tipo comum. | A conferência começa antes da construção do valor. Uma superinterface ou inferência de tipo acrescenta acoplamento e deixa a interpretação da entrada em aberto. Manter os valores existentes e associar cada operação ao seu tipo na montagem. |

## Proposta para formalização

1. O consumidor prepara uma única lista ordenada de itens. Uma fábrica por documento associa o texto e as funções ao tipo Java concreto. Um item genérico com operação que retorna um resultado comum permite percorrer a lista com `<?>` em Java e `<*>` em Kotlin, sem casts. A travessia e a correlação ficam com o consumidor. As funções são `Predicate` e `Function` do JDK. Kotlin acessa o núcleo com conversão SAM e recebe o valor Java concreto, como já ocorre no callback de NIS.
2. Exigir modo explícito para CPF e CNPJ na nova operação. NIS começa pela mesma forma estrutural da 006. Cada execução interpreta o texto uma vez. A política opcional de qualquer um dos três tipos recebe o valor construído. Quando a política aceita, ou quando está ausente, a apresentação configurada recebe a mesma instância. O fluxo de CNPJ em `FORMAT_ONLY` pode aplicar `Cnpj::applyMask` diretamente ao valor. A formatação textual existente continua usando seu modo padrão.
3. O resultado por item distingue rejeição pela validação nativa, rejeição pela política adicional e aceitação no modo escolhido. A apresentação é um dado opcional do resultado aceito, recuperado explicitamente. O resultado não expõe o objeto de valor. Texto nulo, vazio ou só com espaços é rejeição nativa. A rejeição nativa impede os dois callbacks e a rejeição da política impede a apresentação. O consumidor pode omitir a apresentação. Modo obrigatório, política informada e apresentação informada precisam ser não nulos na montagem. Na execução, uma apresentação configurada pode devolver qualquer texto não nulo, inclusive vazio. Exceções de callbacks propagam-se intactas. A ordem, a continuidade após rejeições esperadas e a retenção de resultados anteriores em caso de exceção são controladas pela iteração do consumidor.
4. O item é imutável e reutilizável. A execução é síncrona, sem cache de resultados, e cada callback configurado que for alcançado roda uma vez por execução. Reexecutar o item repete o processamento. Os diagnósticos próprios da API, incluindo mensagens de erro e as representações padrão do item e do resultado, omitem texto bruto, valor canônico e saída formatada. O item não carrega rótulo arbitrário. Quem fornece um callback é responsável pelo texto de suas exceções e pelo destino da apresentação recuperada.
5. Preservar assinaturas e comportamentos de CPF/CNPJ e os contratos da Spec 006. O plano deve comparar com a tag `v0.2.0` e com `5fd9dae`, que contém o NIS. Exercitar a API pelos JARs com Java e Kotlin em JDK 8 e 21. Assinaturas, nomes e representação da ausência no resultado ficam para o desenho do plano.

## Prova de encaixe com o DV da UEM

O consumidor pode fornecer `nis -> ValidationUtils.isPis(nis.getCanonicalValue())` como política e escolher separadamente a sua apresentação. A Spec 006 já oferece esse gancho. O ganho da 007 é percorrer CPF, CNPJ e NIS mantendo a decisão e a saída associadas no mesmo item. O helper e a estratégia foram conferidos no checkout local de `uem-npd-zk-utils` em `9dd29d1`. Os testes da biblioteca podem reproduzir os desfechos com callbacks sintéticos, sem importar o algoritmo ou o checkout da UEM. A revisão da 006 distingue esse helper das outras rotinas da UEM e registra o alcance da sondagem, sem evidência de um chamador de produção para essa API. [Evidência do consumidor](../006-nis/recorte.md) · [Sondagem Java 8 e 21](../../../specs/006-nis-estrutural/analysis.md#contraprova-isolada-com-a-uem)

| Vetor sintético do C1 | Forma do NIS | Com a política da UEM | Saída da operação composta |
| --- | --- | --- | --- |
| 15, `00000000019` | Válida | Aceita | A função recebe os zeros iniciais. A saída depende da apresentação escolhida. |
| 17, `00000000060` | Válida | Aceita | Confirma a política local com DV zero no caso de resto 1. |
| 09, `01234567898` | Válida | Rejeita | Apresentação não é executada. |
| 02, `012.34567.89-7` | Inválida | Não é consultada. O legado isolado aceita a máscara. | Apresentação não é executada. |

Os vetores e a divergência entre políticas ficam na [matriz C1](../c1/nis-matriz.md) e na [descoberta de leiautes da 006](../006-nis/leiautes-oficiais.md). O novo contrato deve aceitar políticas diferentes para o mesmo NIS. A CTPS, a extração do XML, o cadastro, uma regra de DV embutida e a publicação no Maven Central ficam fora do recorte.

## Cenários para a futura especificação

- Uma lista em ordem de entrada com o mesmo `01234567898` como CPF em `FORMAT_ONLY` e como NIS com política equivalente à da UEM. O CPF é aceito e o NIS é rejeitado pela política. Um CNPJ alfanumérico completa a lista. Cada callback recebe o tipo associado e roda apenas na etapa prevista.
- Dois itens com o mesmo NIS canônico e políticas distintas. Uma regra aceita e outra rejeita, sem alterar o resultado estrutural, a igualdade ou o marcador textual de NIS. Repetir com entrada mascarada para conferir zero chamadas.
- CPF sintético `28001238939` e CNPJ `12ABC34501DE36` passam em `FORMAT_ONLY` e falham em seus modos com DV adequados ao tipo. Formatar o CNPJ conferido como `12.ABC.345/01DE-36`. Exercitar CNPJ em minúsculas e máscara parcial do CPF como regressões de rejeição.
- Texto nulo, vazio e só com espaços, lista vazia do consumidor, reexecução do mesmo item, exceção de política e de formatação, saídas nula e vazia, ausência de apresentação e propagação de falhas sem mistura com rejeições esperadas.
- Um consumidor Java 8 e um Kotlin compilam contra os JARs e constroem a lista sem casts. Comparar ergonomia, nullability, diagnósticos, boxing e estabilidade das assinaturas Kotlin com a alternativa só JDK.
