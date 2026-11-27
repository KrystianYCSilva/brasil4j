# Feature Specification: Conferência estrutural de NIS, PIS, Pasep e NIT

- **Identificador da spec** `006-nis-estrutural`
- **Criada em** 2026-11-26
- **Status** Recorte aprovado para especificação por Krystian em 2026-11-26
- **Base** [Descoberta da 006](../../docs/descoberta/006-nis/recorte.md), [pareceres](../../docs/descoberta/006-nis/revisao.md) e [leiautes oficiais](../../docs/descoberta/006-nis/leiautes-oficiais.md).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Conferir a inscrição extraída de um arquivo trabalhista histórico (Priority: P1)

Como responsável pela importação ou conferência de um arquivo trabalhista histórico, quero verificar a apresentação do campo de inscrição e recuperar seu valor textual exato para identificar registros fora do formato esperado e preservar os zeros iniciais durante o processamento.

**Why this priority** A leitura do leiaute pode admitir entradas mais amplas que a forma canônica. O S-2200 do eSocial 2.5 fornece o primeiro caso de uso para conferência do campo. Os leiautes da CQC e do CAGED histórico corroboram o formato de 11 dígitos. [Evidência de leiautes e sondagem](../../docs/descoberta/006-nis/leiautes-oficiais.md)

**Independent Test** Extrair o campo de exemplos sintéticos de registros históricos, passá-lo como texto à conferência e verificar o resultado esperado em cada cenário.

**Acceptance Scenarios**

1. Dado um campo `nisTrab` extraído com o texto `00000000019`, ao conferir a inscrição e recuperar explicitamente seu valor canônico, obter exatamente `00000000019`.
2. Dados `1234567897` e `A1234567897`, admitidos pela declaração de campo no XSD histórico inspecionado, ao conferir a inscrição, obter erro de formato para ambos.
3. Dados nulo, vazio, 12 dígitos ASCII, espaços, sinais, pontuação, dígitos Unicode ou a forma mascarada `012.34567.89-7`, ao conferir o campo, obter erro de formato sem ecoar o valor recebido.
4. Dados `00000000000`, `01234567898` e `00000000060`, todos com 11 dígitos ASCII, ao conferir cada campo, obter aceitação estrutural. A interpretação de preenchimento, DV e consistência cadastral segue a regra do fluxo consumidor.
5. Dados dois campos com o mesmo texto canônico, mesmo quando os leiautes os rotulam de formas diferentes, ao comparar os valores de inscrição, obter igualdade. Textos canônicos diferentes são distintos.

### User Story 2 - Exibir o resultado da conferência sem expor a inscrição (Priority: P2)

Como responsável por relatórios de conferência e diagnósticos de importação, quero uma representação textual padrão que oculte todos os dígitos para poder registrar o resultado da conferência com segurança.

**Independent Test** Conferir dois valores sintéticos distintos, comparar suas representações textuais padrão e examinar a mensagem de uma rejeição.

**Acceptance Scenarios**

1. Dados dois valores estruturais distintos, ao obter a representação textual padrão, obter o mesmo marcador de ocultação para ambos, sem expor qualquer dígito da inscrição.
2. Dada uma entrada inválida, ao consultar a mensagem de erro, obter indicação de formato inválido. A mensagem não reproduz a entrada nem fragmentos do identificador.
3. Dado um valor estrutural, ao solicitar explicitamente o texto canônico para o fluxo de importação, obter todos os 11 dígitos na ordem original.

### User Story 3 - Usar regras e apresentações do consumidor (Priority: P2)

Como mantenedor de uma integração, quero consultar se o campo tem forma canônica sem usar exceções para entradas inválidas e aplicar minhas regras e apresentações sobre inscrições estruturais. A UEM pode plugar sua conferência de DV como política própria.

**Independent Test** Consultar texto nulo, inválido e canônico. Aplicar uma política e uma apresentação sintéticas, contando as chamadas e observando a representação padrão do resultado.

**Acceptance Scenarios**

1. Dado texto válido, vazio, nulo ou fora da gramática, a consulta estrutural devolve a classificação correspondente.
2. Dada uma política explícita, ela recebe a inscrição somente após a conferência estrutural e decide a aceitação adicional. Uma recusa da política não altera o comportamento da fábrica estrutural.
3. Dada uma apresentação escolhida pelo consumidor, a formatação explícita usa a inscrição estrutural. A representação padrão continua com o marcador de ocultação.

### Edge Cases

- Espaços externos, separadores, sinal e algarismos visualmente semelhantes fora de ASCII são entradas inválidas. A conferência avalia a apresentação recebida sem corrigir esses caracteres.
- O leiaute de origem determina se o campo ausente é permitido e distingue uma inscrição informada de um valor padrão de protocolo. Uma chamada com entrada nula ou vazia resulta em erro de formato.
- Uma origem numérica pode já ter descartado a largura original. A conferência exige o texto do campo e a interpretação dessa origem fica com o consumidor.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** — A conferência deve aceitar exatamente 11 caracteres ASCII de `0` a `9` e rejeitar toda entrada fora dessa gramática, inclusive nulo, vazio e entrada mascarada. Deve avaliar o texto antes de qualquer transformação.
- **FR-002** — A inscrição aceita deve ser imutável, preservar todos os 11 caracteres, inclusive zeros iniciais, e permitir a recuperação explícita desse texto.
- **FR-003** — A aceitação pela fábrica e pela consulta estrutural deve depender exclusivamente da gramática do FR-001. Valores com algarismos repetidos ou divergência matemática em relação a um DV passam quando cumprem a gramática.
- **FR-004** — A igualdade de inscrições deve depender somente do texto canônico. Rótulo, emissor, origem e vínculo cadastral são informações do fluxo consumidor.
- **FR-005** — A representação textual padrão deve usar um marcador fixo que oculte todos os dígitos da inscrição. Mensagens de erro devem indicar falha de formato sem reproduzir o valor recebido ou parte dele.
- **FR-006** — A consulta estrutural devolve uma classificação booleana. Quando o consumidor fornece uma política adicional, somente um valor estrutural chega à política, e seu resultado determina a aceitação adicional.
- **FR-007** — O consumidor pode fornecer uma função de formatação explícita que recebe a inscrição já conferida e produz texto. A apresentação padrão continua oculta.

### Key Entities

- **Inscrição estrutural** — Valor textual imutável de 11 dígitos, aplicável a campos denominados NIS, PIS, Pasep ou NIT quando o leiaute requer a forma canônica.
- **Campo do arquivo de origem** — Texto extraído pelo consumidor segundo o leiaute aplicável. O rótulo do campo e suas regras de preenchimento pertencem ao arquivo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** — Nos quatro vetores sintéticos de campo histórico `00000000019`, `01234567897`, `1234567897` e `A1234567897`, a conferência preserva integralmente os dois primeiros e rejeita os dois últimos.
- **SC-002** — A conferência classifica os 22 vetores da [matriz do C1](../../docs/descoberta/c1/nis-matriz.md) conforme a gramática. Aceita 01, 07–09 e 15–21. Rejeita 02–06, 10–14 e 22. Um vetor adicional de 12 dígitos é rejeitado.
- **SC-003** — Duas ocorrências do mesmo texto canônico com rótulos distintos têm igualdade. Uma comparação com outro texto canônico resulta em desigualdade.
- **SC-004** — Dois valores canônicos distintos produzem o mesmo marcador de ocultação. Uma entrada canônica solicitada explicitamente pode ser recuperada integralmente. Em todos os rejeitados do SC-002, a mensagem comunica erro de formato sem expor o texto recebido.
- **SC-005** — A consulta estrutural classifica os mesmos vetores de SC-002. Uma política sintética aceita um valor canônico e rejeita outro, sem ser chamada com uma entrada fora da gramática. Uma apresentação sintética recebe o valor canônico completo, preserva os zeros conforme sua regra e não altera o marcador padrão.

## Assumptions

- O primeiro caso é conferir isoladamente o campo textual de um arquivo trabalhista histórico. O consumidor extrai o campo, seleciona o leiaute, aplica a política de ausência e decide como tratar cada registro. O recorte entrega o valor e a conferência de forma.
- A aceitação padrão indica conformidade estrutural. Algoritmos de DV, consulta cadastral, comprovação de titularidade, origem inferida, máscara de entrada, conversão numérica e CTPS ficam fora desta entrega. Uma política ou apresentação explícita é fornecida e mantida pelo consumidor.
- Os exemplos de aceite são sintéticos. Os leiautes históricos servem de referência documental. A spec não pressupõe adoção por um serviço específico.
