# Feature Specification: Composição de validação e formatação de CPF, CNPJ e NIS

- **Identificador da spec** `007-composicao-identidade`
- **Criada em** 2026-11-27
- **Status** Especificada, pronta para planejamento
- **Base** [Recorte revisado](../../docs/descoberta/007-composicao-identidade/recorte.md) e [pareceres consolidados](../../docs/descoberta/007-composicao-identidade/revisao.md).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Conferir uma lista de campos de identidade (Priority: P1)

Como integrador que recebe campos de um leiaute ou sistema de origem, quero reunir CPF, CNPJ e NIS na mesma lista e aplicar a regra escolhida para cada campo. Assim consigo relacionar a decisão e a apresentação de cada item à sua posição, inclusive quando textos idênticos representam documentos de tipos diferentes.

**Why this priority** A conferência de campos de tipos distintos é o primeiro caso de uso. A decisão e a saída de um item precisam permanecer associadas durante a travessia.

**Independent Test** Percorrer uma lista sintética com os três tipos, incluindo um resultado rejeitado antes de um aceito. Conferir associação, ordem e independência das decisões.

**Acceptance Scenarios**

1. Dada a lista com `01234567898` como CPF em `FORMAT_ONLY`, o mesmo texto como NIS com a política da UEM, e `12ABC34501DE35` como CNPJ em `ALPHANUMERIC`, ao avaliar os itens nessa ordem, obter aceitação do CPF, rejeição do NIS pela política e aceitação do CNPJ. Cada resultado corresponde ao tipo declarado na montagem.
2. Dado o CPF `28001238939` e o CNPJ `12ABC34501DE36`, cada um avaliado em `FORMAT_ONLY` e no seu modo com DV (`NUMERIC` no CPF e `ALPHANUMERIC` no CNPJ), obter aceitação na conferência de forma e rejeição nativa no modo com DV. Ao solicitar a máscara no item CNPJ aceito por forma, obter `12.ABC.345/01DE-36`.
3. Dado um item aceito sem apresentação configurada, obter a decisão de aceitação e ausência de saída. Dado um item aceito com apresentação configurada, obter a decisão de aceitação e a saída da função associada ao próprio item.

### User Story 2 - Compor a validação do NIS com uma regra do consumidor (Priority: P2)

Como integrador, quero fornecer uma regra opcional para compor com o validador do NIS, tanto na conferência direta quanto em uma lista. Assim posso aplicar as regras do meu fluxo sobre os dígitos recebidos, preservando inclusive os zeros iniciais.

**Why this priority** A política da UEM dá um caso concreto em que uma inscrição conforme à forma pode ser rejeitada em uma segunda etapa. [Matriz C1](../../docs/descoberta/c1/nis-matriz.md) · [Contraprova da Spec 006](../006-nis-estrutural/analysis.md#contraprova-isolada-com-a-uem)

**Independent Test** Usar a mesma regra do importador no validador direto e em um item. Avaliar também os vetores sintéticos do C1 com uma política de teste que registre os valores e a contagem de chamadas.

**Acceptance Scenarios**

1. Dado `01234567898` em dois itens de NIS, um sem política adicional e outro com a política de teste da UEM, obter aceitação estrutural no primeiro e rejeição pela política no segundo. O formatador do segundo item recebe zero chamadas.
2. Dados `00000000060` e `00000000019`, ao avaliar cada NIS com a política de teste, obter aceitação e entregar os 11 dígitos exatos à política. Quando houver apresentação, ela recebe a mesma instância entregue à política.
3. Dada a entrada mascarada `012.34567.89-7`, ao avaliar o NIS com política e apresentação configuradas, obter rejeição nativa e zero chamadas às duas funções. O resultado independe da tolerância do helper legado da UEM à máscara.
4. Dada uma regra de importação que exige prefixo `00`, aplicar a mesma regra ao NIS no validador direto e em um item de lista. Obter aceitação nos dois fluxos para `00000000019` e rejeição pela regra nos dois para `01234567898`.

### User Story 3 - Distinguir rejeições, falhas e diagnósticos (Priority: P3)

Como responsável por acompanhar a conferência, quero distinguir um campo rejeitado de uma falha da operação e conseguir registrar a decisão de um item sem divulgar o documento ou a saída formatada.

**Why this priority** O fluxo consumidor precisa continuar após rejeições esperadas e decidir como tratar falhas de configuração ou execução. A saída formatada pode conter o documento completo.

**Independent Test** Avaliar itens sintéticos com funções instrumentadas que rejeitam, lançam falhas ou formam uma saída identificável. Conferir os resultados e as representações diagnósticas.

**Acceptance Scenarios**

1. Dado um item com política que rejeita o valor construído, obter rejeição pela política e ausência de saída. Dada uma política ou apresentação que lança uma exceção própria, receber a mesma exceção no chamador. Resultados anteriores já coletados pelo consumidor permanecem acessíveis.
2. Dado um item com uma regra adicional que aceita e uma apresentação que produz texto, ao avaliar o mesmo item duas vezes, observar uma chamada à política seguida de uma chamada à apresentação em cada execução, na thread de quem chama.
3. Dados itens com CPF, CNPJ e NIS e saídas identificáveis, nenhuma representação diagnóstica do item ou resultado nem mensagem de erro produzida pela biblioteca inclui a entrada, o valor canônico ou a saída formatada. A saída de sucesso pode ser recuperada explicitamente.
4. Dado um item com modo obrigatório nulo ou função fornecida como nula, obter erro na montagem. Dado um item aceito cuja apresentação devolve nulo, obter erro na execução. Se a apresentação devolve `""`, obter aceitação com saída presente e vazia.

### Edge Cases

- Texto nulo, vazio ou composto somente por espaços produz rejeição nativa, com zero chamadas à política e à apresentação. A máscara parcial de CPF e o CNPJ com letras minúsculas preservam as rejeições das regras existentes.
- Uma configuração com modo obrigatório, política fornecida ou apresentação fornecida nulos falha na montagem. Omitir uma política ou uma apresentação é uma escolha válida.
- Apresentação configurada que retorna nulo gera erro de execução. Texto não nulo, inclusive vazio ou com espaços, é uma saída presente.
- A lista pode estar vazia ou conter o mesmo documento mais de uma vez, com políticas diferentes. A travessia de lista vazia avalia zero itens. Cada ocorrência é avaliada segundo sua própria configuração.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** O consumidor deve poder montar uma única lista ordenada com itens CPF, CNPJ e NIS. A associação entre o tipo declarado, a entrada e suas funções deve ser conferida na montagem. Durante a travessia, o consumidor deve conseguir obter a decisão e a saída opcional de cada item por um contrato comum, sem descobrir o tipo pelo texto nem converter o tipo de cada item.
- **FR-002** A operação composta deve exigir modo explícito para CPF (`FORMAT_ONLY` ou `NUMERIC`) e CNPJ (`FORMAT_ONLY`, `NUMERIC` ou `ALPHANUMERIC`) e aplicar integralmente a semântica do modo escolhido. Para NIS, deve exigir 11 dígitos ASCII literais, preservar zeros iniciais e rejeitar máscara. A forma é suficiente para aceitação nativa de NIS, inclusive com repetições ou DV divergente.
- **FR-003** Cada execução deve interpretar a entrada uma vez e, quando aceita pela validação nativa, produzir o valor do tipo declarado. Uma política adicional fornecida para qualquer um dos três tipos deve receber esse valor uma vez. Se a validação nativa rejeitar, nenhuma política ou apresentação deve ser executada.
- **FR-004** Uma apresentação fornecida deve executar uma vez após a aceitação da validação nativa e da política, quando houver. Deve receber a mesma instância analisada pela política e respeitar o modo já usado na construção, inclusive em `FORMAT_ONLY`. A rejeição pela política deve impedir a apresentação.
- **FR-005** O resultado por item deve distinguir rejeição nativa, rejeição pela política e aceitação. A aceitação indica atendimento ao modo e às políticas configuradas. A saída textual é opcional e disponível por acesso explícito, apenas em resultados aceitos com apresentação configurada. O resultado não expõe o objeto de valor construído.
- **FR-006** Nulo, vazio e texto composto somente por espaços devem produzir rejeição nativa. Omissão de política ou apresentação deve ser válida. Modo obrigatório ou função fornecida com valor nulo deve gerar erro na montagem. Saída nula de uma apresentação configurada deve gerar erro de execução. Uma saída vazia não nula deve ser preservada. Exceções das funções do consumidor devem propagar-se intactas.
- **FR-007** O item deve ser imutável e reutilizável. Cada avaliação deve ocorrer de forma síncrona na thread de quem chama e repetir o processamento, sem reaproveitar decisões ou saídas de avaliações anteriores. Uma rejeição é um resultado ordinário por item. A travessia, a ordem, a interrupção após falha e a coleta de resultados pertencem ao consumidor.
- **FR-008** Representações diagnósticas do item e do resultado e erros produzidos pela biblioteca devem omitir o texto bruto, canônico e formatado. Metadados de correlação arbitrários ficam no fluxo consumidor. A aplicação de políticas ou apresentações não deve alterar a representação padrão nem a igualdade dos valores existentes.
- **FR-009** O consumidor deve poder reutilizar a mesma regra adicional na validação direta e em um item de NIS. Cada item pode omitir essa regra e escolher sua apresentação separadamente. O DV da UEM é um exemplo de regra fornecida pelo consumidor.
- **FR-010** A operação deve preservar os contratos públicos existentes de CPF, CNPJ e da Spec 006 nos dois artefatos.

### Key Entities *(include if feature involves data)*

- **Item de conferência** — Associa tipo declarado, entrada textual, modo quando aplicável, política adicional opcional e apresentação opcional. Pode ser usado isoladamente ou numa lista heterogênea.
- **Decisão de conferência** — Desfecho de um item, com uma das duas rejeições ou aceitação e, quando produzida, uma saída textual de acesso explícito.
- **Política adicional e apresentação** — Funções opcionais configuradas pelo consumidor e aplicadas ao valor do tipo declarado na ordem definida.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** A lista sintética de três itens da US1 produz, na ordem da travessia, duas aceitações e uma rejeição pela política entre elas. Cada decisão e saída opcional permanece associada ao campo correto, inclusive para os dois itens de texto idêntico.
- **SC-002** Os quatro ensaios do par CPF/CNPJ em `FORMAT_ONLY` e nos modos com DV produzem duas aceitações por forma e duas rejeições nativas. A apresentação do CNPJ aceito por forma produz `12.ABC.345/01DE-36`.
- **SC-003** Os quatro vetores C1 selecionados com a política da UEM produzem duas aceitações (15 e 17), uma rejeição pela política (09) e uma rejeição nativa (02). No caso mascarado são zero chamadas à política e à apresentação. Nos dois aceitos, a política recebe o valor canônico com zeros iniciais intactos.
- **SC-004** Em duas avaliações consecutivas de um item aceito com as duas funções configuradas, observar a sequência política e apresentação em cada avaliação, com quatro chamadas no total. Em cada rejeição pela política, observar zero chamadas à apresentação.
- **SC-005** Nos ensaios dos três tipos, contar zero ocorrências do texto bruto, canônico ou formatado nos diagnósticos produzidos pela biblioteca. Nas duas falhas injetadas, uma de política e uma de apresentação, o chamador recebe a exceção original.
- **SC-006** Os contratos públicos dos três tipos têm zero regressões de compatibilidade nos dois artefatos, incluindo modos, tratamento de entradas e representação padrão.

## Assumptions

- O integrador conhece o tipo pelo leiaute ou pela origem e mantém posição ou rótulo fora do item. A escolha de omitir um campo ausente do processamento também pertence ao consumidor.
- A política da UEM é um exemplo verificado em isolamento com dados sintéticos. A descoberta não identificou chamador de produção para o helper avaliado. O código da UEM permanece no consumidor. [Evidência](../../docs/descoberta/007-composicao-identidade/recorte.md#prova-de-encaixe-com-o-dv-da-uem)
- A compatibilidade será planejada contra a `v0.2.0` e o commit `5fd9dae`, que inclui a Spec 006. As assinaturas e a estratégia de verificação dos dois artefatos pertencem ao plano.
- CTPS, extração de arquivos, consultas a cadastros, DV de NIS embutido, um processador de lote da biblioteca e publicação no Maven Central ficam fora desta entrega.
