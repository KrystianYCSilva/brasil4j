# Feature Specification: Núcleo monetário, rateio sem perda e conversão cambial

- **Feature Branch**: `008-nucleo-monetario`
- **Created**: 2026-11-27
- **Status**: Ready for implementation (revisada com parecer independente)
- **Base**: [Recorte de descoberta F0](../../docs/descoberta/financeiro/recorte.md)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Representar e operar valores monetários com segurança de moeda e arredondamento explícito (Priority: P1)

Como desenvolvedor de um sistema financeiro, administrativo ou fiscal brasileiro, quero representar valores em Real, Dólar, Euro ou qualquer outra moeda/unidade com casas decimais definidas e política explícita de arredondamento (incluindo a norma ABNT NBR 5891, arredondamento financeiro comercial e truncamento). Assim evito erros de ponto flutuante binário, arredondamentos silenciosos divergentes e a soma acidental de moedas incompatíveis.

**Why this priority**: É a fundação de toda a trilha financeira. Corrige imediatamente o uso de ponto flutuante binário, textos soltos e arredondamentos inconsistentes identificado nos sistemas consumidores.

**Independent Test**: Construir valores em Real, Dólar, Euro, Iene (0 casas) e unidade customizada (3 e 4 casas) a partir de texto decimal exato e de unidades mínimas inteiras; validar os três casos da ABNT NBR 5891 contra o modo comercial e o truncamento; verificar a rejeição de entradas inexatas no modo estrito e a proteção contra mistura de moedas distintas.

**Acceptance Scenarios**:

1. **Given** os textos decimais `"10.245"`, `"10.255"` e `"10.2450001"` para Real (`2` casas decimais), **When** construídos com arredondamento `ABNT_NBR_5891`, `FINANCEIRO` e `TRUNCAMENTO`, **Then** obter respectivamente: em `ABNT_NBR_5891` `"10.24"`, `"10.26"` e `"10.25"`; em `FINANCEIRO` `"10.25"`, `"10.26"` e `"10.25"`; e em `TRUNCAMENTO` `"10.24"`, `"10.25"` e `"10.24"`.
2. **Given** um valor base em Real `"100.00"` com política `ABNT_NBR_5891`, **When** executadas quatro operações independentes partindo de `"100.00"` (`"100.00" + "25.50"`, `"100.00" - "10.25"`, `"100.00" * 2` e `"100.00" / 3`), **Then** obter respectivamente `"125.50"`, `"89.75"`, `"200.00"` e `"33.33"` em Real, preservando a escala `2` em todas as saídas.
3. **Given** a construção estrita (`exato`, sem tolerância a casas excedentes) para Real (`2` casas) e Iene (`0` casas), **When** informados `"10.5"` para Real e `"500"` para Iene, **Then** aceitar normalizando Real para `"10.50"` e Iene para `"500"`; **When** informados `"10.555"` ou `"10.500"` para Real, ou `"500.5"` para Iene no modo estrito, **Then** rejeitar a entrada sem arredondar em silêncio.
4. **Given** um valor em Real `"10.00"` construído com `ABNT_NBR_5891` e outro valor `"10.00"` obtido pela fábrica geral de dinheiro para a moeda `BRL` com `FINANCEIRO`, **When** comparados por igualdade, **Then** considerar ambos iguais, do mesmo tipo concreto de Real e com o mesmo código de espalhamento (`hash`).
5. **Given** um valor em Real e um valor em Dólar tratados sob o contrato monetário comum, **When** solicitada soma, subtração ou comparação de grandeza entre eles, **Then** rejeitar a operação por incompatibilidade de moeda.

---

### User Story 2 - Parcelar e alocar valores proporcionalmente sem perda de centavos (Priority: P2)

Como responsável por faturamento, cobrança ou escrituração de documentos fiscais, quero dividir um valor monetário em parcelas iguais ou alocá-lo proporcionalmente a pesos (como valores de itens de uma nota) sem perder nem criar centavos residuais. Assim a soma das partes concilia exatamente com o valor total do documento.

**Why this priority**: A divergência de `0,01` na soma de parcelas ou de rateios de frete/desconto por item é uma falha recorrente em boletos e leiautes fiscais. Depende apenas do valor monetário da US1.

**Independent Test**: Parcelar valores indivisíveis (positivos, negativos e zero) em `N` partes com resíduo nas primeiras ou nas últimas parcelas e alocar valores por listas de pesos variados (incluindo pesos iguais a zero misturados a pesos positivos), conferindo que a soma das partes reproduz exatamente o valor de origem em 100% dos casos.

**Acceptance Scenarios**:

1. **Given** o valor em Real `"100.00"`, **When** parcelado em `3` cotas com política de resíduo nas primeiras parcelas e com política de resíduo nas últimas parcelas, **Then** obter respectivamente `["33.34", "33.33", "33.33"]` e `["33.33", "33.33", "33.34"]`, ambos somando exatamente `"100.00"`.
2. **Given** o valor negativo em Real `"-100.00"`, **When** parcelado em `3` cotas com resíduo nas primeiras parcelas, **Then** obter `["-33.34", "-33.33", "-33.33"]`, cuja soma é exatamente `"-100.00"`.
3. **Given** o valor em Real `"100.00"` e os pesos `[1, 1, 1]`, `[0, 1, 1, 1]` ou `[70, 20, 10]`, **When** alocado proporcionalmente pelo método dos maiores restos, **Then** obter `["33.34", "33.33", "33.33"]` no primeiro caso, `["0.00", "33.34", "33.33", "33.33"]` no segundo (peso zero nunca recebe unidade residual) e `["70.00", "20.00", "10.00"]` no terceiro, preservando a soma `"100.00"`.
4. **Given** um valor em Iene `"100"` (`0` casas decimais), **When** parcelado em `3` cotas com resíduo nas primeiras parcelas, **Then** distribuir a unidade mínima inteira obtendo `["34", "33", "33"]`.

---

### User Story 3 - Converter valores entre moedas por taxa de câmbio explícita e reversível (Priority: P3)

Como integrador de operações em moeda estrangeira, quero converter valores entre Real, Dólar, Euro e outras moedas informando explicitamente a cotação (com todas as suas casas decimais) e a direção entre moeda de origem e moeda de destino, inclusive invertendo a taxa de forma exata e involutiva. Assim evito perda de precisão na cotação e impeço o erro de multiplicar quando deveria dividir.

**Why this priority**: Complementa o uso multi-moeda com rastreabilidade e segurança direcional, mantendo a biblioteca 100% offline.

**Independent Test**: Criar taxas de câmbio diretas e invertidas com cotações de 4 a 8 casas decimais, converter valores entre Real, Dólar, Euro e Iene com diferentes políticas de arredondamento (inclusive `TRUNCAMENTO` e `PISO`), verificar involução `taxa.inverter().inverter() == taxa` e rejeição para pares incompatíveis.

**Acceptance Scenarios**:

1. **Given** uma taxa de câmbio declarando `1 USD = 5.4832 BRL` e um valor de `"100.00"` em Dólar com política `ABNT_NBR_5891`, **When** aplicada a conversão para Real, **Then** obter `"548.32"` como instância de Real.
2. **Given** a mesma taxa `1 USD = 5.4832 BRL` invertida de forma exata (`5.4832 BRL = 1 USD`) e um valor de `"548.32"` em Real, **When** aplicada a conversão para Dólar tanto em `ABNT_NBR_5891` quanto em `TRUNCAMENTO` ou `PISO`, **Then** obter exatamente `"100.00"` como instância de Dólar, sem perda por arredondamento intermediário da cotação, e observar `taxa.inverter().inverter()` idêntica à taxa original.
3. **Given** uma taxa de câmbio `1 USD = 5.4832 BRL` e um valor em Real `"548.32"`, **When** chamado o método tipado `paraDolar` passando essa taxa do par `(USD, BRL)`, **Then** aplicar automaticamente a direção correspondente ao par e devolver `"100.00"` em Dólar; **When** passada uma taxa de par incompatível (`EUR/BRL`) ou chamada a conversão genérica com moeda de origem divergente, **Then** rejeitar a operação.

---

### User Story 4 - Operar valores monetários de forma idiomática em consumidores Kotlin (Priority: P4)

Como desenvolvedor Kotlin, quero criar e combinar valores em Real, Dólar, Euro e outras moedas usando extensões expressivas e operadores aritméticos e relacionais nativos da linguagem, mantendo exatamente as mesmas invariantes, exceções e garantias de tipo do núcleo.

**Why this priority**: Garante paridade ergonômica no segundo artefato oficial do projeto sobre o mesmo domínio validado nas histórias anteriores.

**Independent Test**: Executar em Kotlin adições, subtrações, multiplicações (nos dois sentidos `valor * n` e `n * valor`), divisões, negação, comparações, rateio/alocação e conversão cambial a partir do artefato empacotado, verificando paridade de resultados e falhas com o núcleo.

**Acceptance Scenarios**:

1. **Given** dois valores em Real criados em Kotlin (`"100.00"` e `"25.50"`), **When** operados com `+`, `-`, `*`, `/`, negação unária e comparação `<` / `>`, **Then** produzir os mesmos valores, escalas e tipos do núcleo, mantendo incompatível em tempo de compilação a soma direta entre Real e Dólar.

---

### Edge Cases

- Texto nulo, vazio, acima do limite de comprimento (`64` caracteres), com espaços externos/internos, separador de milhar, vírgula decimal, letras, expoente científico ou dígitos não ASCII na construção textual canônica é rejeitado imediatamente.
- Valores decimais, fatores, cotações ou pesos com precisão superior a `38` dígitos significativos ou escala fora de `[-18, 38]` são rejeitados com erro explícito para prevenir exaustão de memória.
- O vetor `"-0.004"` em moeda de `2` casas resulta em `"0.00"` nas políticas `ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO` e `TETO`, e em `"-0.01"` nas políticas `PISO` e `PARA_CIMA`; sempre que o resultado arredondado for zero, sua escala é exatamente a da moeda (`"0.00"`).
- Política customizada de arredondamento fornecida pelo consumidor que devolva nulo, escala incorreta ou valor diferente dos dois vizinhos imediatos (`FLOOR` ou `CEILING` do operando na escala alvo) é rejeitada imediatamente.
- Implementação customizada de moeda fornecida pelo consumidor que altere seu estado entre leituras ou após a construção não afeta o código, as casas decimais, o símbolo, a igualdade nem o `hash` do valor monetário ou taxa já criados.
- Divisão por zero, parcelamento em `0` ou número negativo de cotas, alocação com lista de pesos nula/vazia, peso nulo/negativo ou soma de pesos igual a zero são rejeitados com erro explícito.
- Conversão para unidades mínimas inteiras de 64 bits falha explicitamente se o montante ultrapassar a capacidade representável, nunca sofrendo truncamento silencioso por *overflow*.
- Taxa de câmbio com moeda de origem igual à moeda de destino, ou com cotação nula, zero ou negativa, é rejeitada na criação.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A biblioteca DEVE representar uma moeda por seu código textual não vazio em ASCII visível (`1..16` caracteres sem espaços), número de casas decimais entre `0` e `18` e símbolo textual não vazio (`1..16` caracteres sem caracteres de controle), disponibilizando constantes prontas para Real (`BRL`, `2`, `"R$"`), Dólar americano (`USD`, `2`, `"US$"`) e Euro (`EUR`, `2`, `"€"`), fábrica para códigos ISO 4217 de três letras maiúsculas ASCII, comparação explícita de equivalência monetária por `(código, casas decimais)` e extensão aberta para unidades definidas pelo consumidor.
- **FR-002**: A biblioteca DEVE oferecer políticas explícitas de arredondamento decimal incluindo `ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`, `PARA_CIMA`, `TETO`, `PISO` e políticas customizadas definidas pelo consumidor, validando que toda política customizada devolva um dos dois vizinhos imediatos (`FLOOR` ou `CEILING`) na escala alvo.
- **FR-003**: A biblioteca DEVE selar a hierarquia monetária sob um supertipo comum que só possa ser estendido pelos quatro tipos finais da própria biblioteca — Real, Dólar, Euro (escala fixa `2` e bloqueio em tempo de compilação de soma/subtração entre si) e um tipo geral para demais moedas — impedindo subtipagem externa que viole imutabilidade ou simetria de igualdade.
- **FR-004**: A fábrica geral de dinheiro DEVE produzir canonicamente instâncias de Real para `("BRL", 2)` (com moeda canônica `BRL`), Dólar para `("USD", 2)` (`USD`) e Euro para `("EUR", 2)` (`EUR`), garantindo igualdade e código de espalhamento simétricos baseados apenas em `(código da moeda, casas decimais, valor escalado)`.
- **FR-005**: A construção de valores monetários NÃO DEVE aceitar tipos de ponto flutuante binário; DEVE validar limites seguros de tamanho/precisão (`<= 64` caracteres em texto; precisão `<= 38` e escala em `[-18, 38]` em decimais), aceitar texto decimal canônico (`[+-]?[0-9]+(\.[0-9]+)?`), valores decimais exatos, inteiros explícitos de unidades principais (`deInteiro`) e quantidades inteiras de unidades mínimas, capturando em leitura única (*snapshot*) cópia imutável dos metadados da moeda.
- **FR-006**: Cada tipo monetário DEVE disponibilizar construção com arredondamento (que ajusta entradas usando a política escolhida e retém essa política para operações inexatas posteriores, assumindo `ABNT_NBR_5891` quando omitida) e construção estrita (`exato`, que rejeita entradas com escala textual/decimal maior que as casas decimais da moeda).
- **FR-007**: Os valores monetários DEVEM suportar adição e subtração exatas na mesma moeda, negação, valor absoluto, troca de política retida, consulta de sinal, multiplicação exata por inteiro (com detecção de excesso dos limites seguros), multiplicação exata por fator decimal seguida de arredondamento único, divisão por inteiro não zero com arredondamento único nos modos padrão e entrega do quociente decimal exato à política customizada quando terminante e extração exata em unidades mínimas inteiras com detecção de *overflow*. Para divisão não terminante com política customizada, aplica-se a aproximação documentada no contrato.
- **FR-008**: Os valores monetários DEVEM suportar parcelamento em `N >= 1` partes iguais (`PRIMEIRAS_PARCELAS` ou `ULTIMAS_PARCELAS`, preservando o sinal e a política retida) e alocação proporcional a pesos não negativos com soma positiva (pelo método exato dos maiores restos com desempate pela ordem de entrada, onde peso zero jamais recebe resíduo), garantindo soma das partes idêntica ao valor original.
- **FR-009**: A biblioteca DEVE representar taxas de câmbio imutáveis entre moedas distintas (`origem != destino`) com cotação estritamente positiva e inversão exata e involutiva (`inverter().inverter()` igual à original), suportando conversão estrita na direção da taxa e conversões tipadas em Real/Dólar/Euro que aceitem qualquer direção do respectivo par de moedas sem perda por arredondamento intermediário.
- **FR-010**: A representação textual padrão (`toString`) dos valores monetários e taxas de câmbio DEVE ser pública, determinística, sem notação científica e independente da localidade do sistema operacional.
- **FR-011**: O módulo de adaptação Kotlin DEVE oferecer extensões de construção, operadores (`+`, `-`, `*` comutativo com inteiros, `/`, negação unária, comparação) e atalhos de alocação e câmbio para os quatro tipos finais e o supertipo comum, delegando integralmente todas as regras ao núcleo.
- **FR-012**: A entrega DEVE preservar 100% de compatibilidade binária e de código-fonte com os contratos públicos já existentes nos dois artefatos publicados.

### Key Entities *(include if feature involves data)*

- **Moeda**: Descritor monetário composto por código identificador ASCII visível (`1..16`), casas decimais (`0..18`) e símbolo textual (`1..16`).
- **Política de Arredondamento**: Regra determinística que reduz um valor decimal exato a um de seus vizinhos imediatos na escala da moeda.
- **Dinheiro (`Real`, `Dolar`, `Euro`, `QuantiaMonetaria`)**: Hierarquia selada e imutável de quantia associada a uma moeda imutável, valor exato na escala da moeda e política de arredondamento retida.
- **Política de Resíduo**: Regra de distribuição das unidades mínimas indivisíveis no parcelamento (`PRIMEIRAS_PARCELAS` ou `ULTIMAS_PARCELAS`).
- **Taxa de Câmbio**: Relação imutável, exata e involutivamente invertível entre duas moedas distintas.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos vetores de arredondamento de borda (empate par, empate ímpar, resíduo além da terceira casa, `"-0.004"` nos seis modos e políticas customizadas válidas/inválidas) produzem o resultado ou rejeição esperados com arredondamento único nos modos padrão e com entrega do quociente exato em divisões terminantes com política customizada.
- **SC-002**: Em 100% dos cenários parametrizados de parcelamento (`1` a `120` parcelas, valores positivos, negativos e zero, moedas de `0`, `2` e `3` casas) e de alocação proporcional por pesos (incluindo pesos zero), a soma algébrica das partes geradas é exatamente igual ao montante original.
- **SC-003**: 100% das tentativas de operar moedas distintas, extrapolar limites de tamanho/precisão, violar vizinhos em política customizada, dividir por zero, criar câmbio degenerado (`origem == destino`) ou mutar externamente uma moeda após ou durante a leitura falham de forma determinística ou preservam estado imutável intacto.
- **SC-004**: Consumidores dos dois artefatos empacotados executam os mesmos cenários (incluindo carregamento inicial isolado de cada ponto de entrada) nos dois ambientes suportados obtendo saídas 100% idênticas.
- **SC-005**: A cobertura verificada nos dois módulos permanece igual ou superior a 90% de linhas e 85% de desvios condicionais, com zero alertas na geração da documentação pública.
- **SC-006**: A verificação automatizada de compatibilidade contra a linha de base vigente reporta zero quebras binárias ou de código-fonte nos tipos públicos existentes.

## Assumptions

- Formatação localizada (`"R$ 1.234,56"`), perfis SPED/CNAB, escrita por extenso, percentuais/cálculo por dentro, calculadora funcional encadeada, faixas progressivas, juros e amortização pertencem a futuros incrementos do domínio financeiro.
- Os objetos de valor financeiros não implementam serialização binária nativa da plataforma (seguindo o mesmo padrão dos tipos de Identidade e Geografia existentes), privilegiando representação textual canônica explícita.
