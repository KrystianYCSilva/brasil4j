# Spec 001 · Validação estrutural de CNPJ

**Feature:** `001-cnpj`  
**Criada:** 25/11/2026  
**Status:** rascunho de descoberta para revisão no C2  
**Requisitos relacionados:** RF-01–RF-04 e RNF-01–RNF-03 em [requisitos](../../requisitos.md).

## Cenários e testes de aceitação

### História 1 · Obter um CNPJ canônico (P1)

Como integrador Java ou Kotlin, quero receber um CNPJ válido como valor para armazená-lo e compará-lo de forma consistente. **Teste independente:** consumir a API a partir de cada linguagem com as mesmas entradas.

1. Dado `12.ABC.345/01DE-35`, quando interpretado, então o resultado canônico é `12ABC34501DE35`.
2. Dado `00000000000191`, quando interpretado, então o resultado mantém todos os zeros à esquerda.
3. Dadas as formas canônica e formatada da mesma inscrição, quando comparadas, então os valores são iguais e têm o mesmo hash.

### História 2 · Rejeitar apresentação ambígua ou DV inválido (P1)

Como integrador de cadastro, quero distinguir entradas aceitas de entradas inválidas antes de criar um valor. **Teste independente:** executar os casos negativos da [matriz](matriz-cnpj.md).

1. Dadas a pontuação deslocada `1.2ABC34501DE35` e a máscara parcial `90.024.778/000123`, quando analisadas, então são rejeitadas pela operação estrita.
2. Dados `12ABC34501DE36` e `12ABC34501DEA5`, quando analisados, então são rejeitados por DV incorreto e por formato, respectivamente.

### História 3 · Avaliar adoção no cadastro existente (P2)

Como mantenedor do Obrigações Core, quero identificar entradas cujo resultado mudaria antes de decidir uma adoção. **Teste independente:** comparar as saídas do contrato candidato com o ponto de uso do Core, em ambiente isolado.

1. Dada a máscara parcial presente no teste do Core, quando se compara o comportamento, então o legado produz `90024778000123` e a lib rejeita a apresentação estrita.
2. Dadas entradas atualmente aceitas com caixa distinta ou espaços externos, registrar representação, efeito no consumidor e política aprovada antes de trocar o validador.

## Requisitos funcionais

- **CNPJ-01.** A operação estrita aceita 12 caracteres ASCII de `A–Z` ou `0–9` mais dois DVs ASCII `0–9`, em forma canônica ou com máscara completa `XX.XXX.XXX/XXXX-DD`. O rascunho adota ASCII maiúsculo e sem espaços, inclusive externos, nas duas apresentações.
- **CNPJ-02.** O parser rejeita nulo, vazio, comprimento incorreto, caracteres fora do alfabeto, pontuação deslocada, máscara parcial e letra no DV. Para a primeira API, a falha proposta de parsing é `IllegalArgumentException` sem reproduzir a entrada. O reconhecimento do motivo pode permanecer interno.
- **CNPJ-03.** Calcular os dois DVs conforme F-02, usando o primeiro no cálculo do segundo. A sequência integralmente composta de zeros é rejeitada pela política candidata F-05.
- **CNPJ-04.** O resultado é imutável e devolve a forma canônica de 14 caracteres. Igualdade e hash usam esse valor.
- **CNPJ-05.** A operação informa validade estrutural. Existência, situação cadastral, unicidade e correspondência de raiz com estabelecimento são decisões do consumidor.

**Entidade-chave:** `Cnpj`, objeto de valor de inscrição completa.  
**Casos de fronteira:** zeros à esquerda, DV terminado em zero, letras nas posições de raiz e ordem, dígitos Unicode e entradas com caracteres que expandem em maiúsculas.  
**Fora da fatia:** parsing tolerante, políticas opt-in, geração, CPF, NIS, tipo público de raiz, extensão Kotlin e alterações em serviços.

## Critérios de sucesso

- **CS-01.** 100% dos casos acordados na matriz reproduzem a mesma decisão e o mesmo valor canônico em Java e Kotlin.
- **CS-02.** O artefato compila para Java 8 e os dois consumidores executam em Java 8. A verificação em JDK 21 também passa.
- **CS-03.** Máscara parcial, caixa, espaços e pontuação deslocada têm decisão registrada antes de adoção no Core.
- **CS-04.** Cada regra possui fonte e cobertura em teste de contrato. A entrega registra red, green e refactor para cada invariante introduzida.

**Questões de promoção:** confirmar as apresentações efetivamente enviadas ao cadastro, verificar a alternativa publicada com a matriz e demonstrar benefício de um contrato compartilhado. O [registro C1](README.md) indica como resolver cada lacuna.
