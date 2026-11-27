# Feature Specification: Contratos, polimorfismo e composição para documentos

- **Identificador da spec** `010-contratos-documentos`
- **Criada em** 2026-11-27
- **Status** Especificada, com uma clarificação pendente
- **Base** [Recorte](../../docs/descoberta/010-contratos-documentos/recorte.md) e [pareceres consolidados](../../docs/descoberta/010-contratos-documentos/revisao.md). [Constituição v0.5.0](../../.specify/memory/constitution.md).
- **Numeração** A 010 segue a 008 (núcleo monetário) e a 009 (exemplos de utilização), em andamento em paralelo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Criar um documento próprio no padrão da biblioteca (Priority: P1)

Como integrador, quero definir um documento que a biblioteca não oferece, reaproveitando os blocos que ela usa nos seus próprios tipos: máscara de apresentação, cálculo de dígito verificador, regras adicionais e um resultado com motivo de rejeição. Assim meu documento se comporta como CPF, CNPJ e NIS e pode ser conferido nos mesmos fluxos.

**Why this priority** Extensibilidade pelo consumidor é o objetivo central. Ela só existe de verdade se os tipos da biblioteca forem construídos pelos mesmos blocos públicos.

**Independent Test** Montar nos testes um documento sintético com máscara própria e um módulo 11 configurado. Interpretar entradas válidas, mascaradas, com formato inválido e com DV divergente. Conferir o resultado, o motivo, a igualdade, a representação oculta e o uso numa lista heterogênea com CPF, CNPJ e NIS.

**Acceptance Scenarios**

1. Dado um documento sintético de nove dígitos com máscara `999.999-999` e DV calculado por um módulo 11 configurado pelo consumidor, ao interpretar a forma canônica e a mascarada de um valor válido, obter aceitação nas duas e o mesmo valor canônico.
2. Dado o mesmo documento, ao interpretar um texto de forma inválida e outro com DV divergente, obter rejeição com motivos distintos, de formato e de dígito verificador, sem exceção e sem a entrada em nenhum diagnóstico.
3. Dado o documento sintético numa lista ordenada com CPF, CNPJ e NIS, ao avaliar a lista, obter uma decisão por item pelo mesmo contrato comum, sem converter tipos e sem alterar a biblioteca.

### User Story 2 - Depender de contratos, e não de utilitários estáticos (Priority: P1)

Como integrador, quero que meu código receba um interpretador de documento por contrato, para trocá-lo por outra configuração ou por um dublê nos testes, e quero saber por que uma entrada foi rejeitada sem capturar exceções.

**Why this priority** Inversão de dependência e substituição polimórfica são a base de todos os outros fluxos. O motivo da rejeição atende a uma necessidade concreta de diagnóstico que os validadores booleanos atuais não cobrem.

**Independent Test** Escrever um componente de teste que recebe o interpretador por parâmetro. Exercitá-lo com o CPF em cada modo, com o CNPJ em cada modo e com um dublê. Conferir aceitação, motivo de rejeição e a ausência de exceção para entradas inválidas.

**Acceptance Scenarios**

1. Dado o CPF `28001238939`, ao interpretar pela configuração só de formato e pela configuração com DV, obter aceitação na primeira e rejeição por dígito verificador na segunda.
2. Dado o CNPJ `12ABC34501DE36`, ao interpretar pela configuração alfanumérica com DV, obter rejeição por dígito verificador. Pela configuração numérica, obter rejeição por base não suportada. Pela configuração de formato, obter aceitação e a máscara `12.ABC.345/01DE-36`.
3. Dado o CNPJ `00000000000000`, ao interpretar por uma configuração com DV, obter rejeição, preservando o bloqueio de 14 zeros (F-05).
4. Dado texto nulo, vazio ou só com espaços, ao interpretar por qualquer configuração, obter rejeição ordinária com motivo, sem exceção.

### User Story 3 - Compor regras e escolher validadores de DV do NIS (Priority: P2)

Como integrador, quero acrescentar minhas regras a um interpretador e, para o NIS, escolher explicitamente um validador de DV identificado pela sua fonte e pelo seu alcance. O padrão do NIS continua sendo só tamanho e formato.

**Why this priority** Composição de regras reaproveita a política da 007 por contrato. O DV do NIS foi pedido como opção, e a proveniência exige identificar fonte e alcance sem chamar a regra de oficial.

**Independent Test** Aplicar ao NIS os vetores C1 07, 09, 15, 16, 17, 18, 19, 20 e 21 sem validador de DV, com o validador da fórmula publicada pela Caixa FGTS v016 e com o validador da convenção legada. Conferir também o encadeamento de uma regra do consumidor com código simbólico próprio.

**Acceptance Scenarios**

1. Dado o NIS na configuração padrão, ao interpretar os vetores com DV divergente (09, 16, 18 e 20), obter aceitação estrutural, como na Spec 006.
2. Dado o validador da fórmula publicada no Caderno do Agente Financeiro v016 da Caixa, ao interpretar `00000000060` (caso 17, resto 1), obter rejeição por dígito verificador. Ao interpretar `00000000280` (caso 19) e `00000000019` (caso 15), obter aceitação.
3. Dado o validador da convenção legada observada em Declarações e UEM, ao interpretar o caso 17, obter aceitação. Ao interpretar `00000000000` (caso 07), obter rejeição pela guarda de onze algarismos iguais. Ao interpretar `11111111116` (caso 21), obter aceitação.
4. Dado um interpretador com uma regra do consumidor que exige o prefixo `00` e tem o código `PREFIXO_IMPORTACAO`, ao interpretar `01234567898`, obter rejeição por política com esse código. A regra não é executada quando a validação nativa rejeita.
5. Dado o validador da Caixa FGTS v016, o tratamento de onze algarismos iguais é [NEEDS CLARIFICATION: aplicar ou não a guarda de algarismos repetidos, cuja fonte é o SIPREV RV_03, com outro alcance? Sem a guarda, `00000000000` é aceito pela fórmula isolada].

### User Story 4 - Migrar sem quebra (Priority: P3)

Como mantenedor de um sistema que já usa a biblioteca, quero atualizar a versão sem mudar o comportamento observado e com orientação clara para a nova API.

**Why this priority** A compatibilidade aditiva foi decidida para a API já publicada. A orientação reduz o custo de migração.

**Independent Test** Rodar as suítes existentes de CPF, CNPJ, NIS e da Spec 007 sem alterar asserções. Comparar a API com as baselines publicadas. Conferir que cada utilitário deprecado indica o substituto.

**Acceptance Scenarios**

1. Dadas as chamadas existentes de interpretação, validação booleana e formatação, obter os mesmos resultados, as mesmas mensagens e os mesmos tipos exatos de exceção.
2. Dado um utilitário estático substituído, a documentação pública o marca como obsoleto e indica o equivalente por contrato.

### Edge Cases

- Texto nulo ou vazio produz o motivo de ausência. Texto só com espaços, máscara parcial e CNPJ em minúsculas produzem o motivo de formato. Todos são rejeições ordinárias.
- Modo obrigatório nulo nas fábricas de CPF e CNPJ continua gerando o erro de argumento atual. Uma regra, uma política ou uma apresentação nulas falham na montagem.
- A interpretação estrita continua lançando o erro de argumento exato, com a mensagem atual, que não contém a entrada.
- Um documento do consumidor que não personalize a representação textual tem saída oculta por padrão. Apenas o CNPJ mantém a representação canônica atual.
- Um algoritmo de DV que recebe caracteres fora do domínio configurado, comprimento incorreto ou nulo falha de forma explícita, sem produzir DV inválido.
- `DocumentCheck` mantém a semântica da 007: `withPolicy` substitui a política anterior do item. No interpretador, as regras se encadeiam.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** A biblioteca deve oferecer contratos públicos para o valor de documento, segregados por capacidade: valor canônico, apresentação mascarada e dígito verificador. CPF e CNPJ devem atender às três capacidades. NIS deve atender apenas ao valor canônico.
- **FR-002** A biblioteca deve oferecer uma base reutilizável para valores de documento, com igualdade pelo tipo e pelo valor canônico e representação textual oculta por padrão.
- **FR-003** A biblioteca deve oferecer um contrato de interpretação que devolve um resultado de aceitação ou rejeição sem lançar exceção para entradas inválidas. Também deve oferecer uma forma estrita que lança o erro de argumento exato com mensagem segura.
- **FR-004** Toda rejeição deve informar um motivo entre ausência, formato inválido, dígito verificador divergente, base não suportada e política. Pode informar também um código simbólico definido pelo consumidor. Rejeições, resultados e mensagens nunca contêm a entrada, o valor canônico ou a saída formatada.
- **FR-005** O consumidor deve poder compor regras sobre o valor canônico e sobre o documento construído, em cadeia e com interrupção na primeira rejeição, e decorar qualquer interpretador com regras e políticas adicionais.
- **FR-006** A biblioteca deve oferecer uma montagem de interpretadores com máscara ou normalizador, algoritmo de DV, regras e mensagem de erro. CPF, CNPJ e NIS devem ser construídos por essa mesma montagem.
- **FR-007** A biblioteca deve oferecer, em blocos independentes de contexto, uma máscara declarativa (dígito, letra, alfanumérico e literais), que remove e aplica a apresentação, e um algoritmo de módulo 11 configurável por valor de caractere, pesos e mapeamento do resto.
- **FR-008** Os módulos 11 de CPF e CNPJ devem produzir os mesmos resultados das regras atuais para todos os vetores existentes e para uma amostra gerada e congelada. O bloqueio de 14 zeros do CNPJ deve continuar nos modos com DV.
- **FR-009** O NIS deve continuar validando por padrão apenas tamanho e formato. Os validadores de DV devem ser opcionais e nomeados pela fonte e pelo alcance: a fórmula publicada no Caderno do Agente Financeiro v016 da Caixa, com alcance de PIS/Pasep no FGTS, e a convenção técnica legada observada em Declarações e UEM. Nenhum artefato pode descrever esses validadores como regra oficial do NIS.
- **FR-010** A biblioteca deve oferecer apresentações reutilizáveis: canônica, mascarada e com ocultação parcial validada. Uma ocultação que exporia todos os caracteres deve ocultar o valor inteiro.
- **FR-011** O item de conferência da Spec 007 deve aceitar qualquer interpretador, inclusive de documentos do consumidor, e expor o motivo da rejeição. Deve preservar os três estados, a semântica de substituição de política, a ordem de execução e a privacidade.
- **FR-012** O artefato Kotlin deve oferecer conversões tolerantes a nulo que devolvam os tipos Kotlin de CPF, CNPJ e NIS, uma conversão genérica para qualquer interpretador e a conferência de DV no CNPJ. Tudo deve delegar ao núcleo.
- **FR-013** A API publicada até a v0.2.0 e a API de NIS da Spec 006 devem permanecer compatíveis em binário e em fonte. Utilitários estáticos substituídos devem ser marcados como obsoletos, com indicação do substituto.

### Key Entities *(include if feature involves data)*

- **Documento** Valor imutável com forma canônica. Capacidades opcionais: apresentação mascarada e dígito verificador.
- **Interpretador** Estratégia que converte texto num resultado. Pode ser decorada com regras.
- **Resultado e rejeição** Desfecho da interpretação. Uma rejeição tem motivo e, opcionalmente, um código simbólico.
- **Regra** Verificação componível sobre o valor canônico ou sobre o documento.
- **Máscara e algoritmo de DV** Blocos reutilizáveis por qualquer documento.
- **Validador de DV com proveniência** Algoritmo identificado por fonte, versão e alcance registrados.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** Um documento sintético do consumidor é definido só com os blocos públicos, sem alterar a biblioteca. Ele produz os mesmos quatro desfechos da US1 (aceitação canônica, aceitação mascarada, formato inválido e DV divergente) e participa de uma lista com os três tipos nativos.
- **SC-002** CPF, CNPJ, NIS e o documento sintético passam pela mesma suíte de contrato, com zero exceções para entradas inválidas e zero ocorrências da entrada em diagnósticos.
- **SC-003** Os cálculos de DV de CPF e CNPJ coincidem com as regras atuais em 100% dos vetores existentes e da amostra congelada.
- **SC-004** Os nove vetores C1 da US3 produzem, nas três configurações do NIS, exatamente os desfechos previstos.
- **SC-005** As suítes existentes ficam verdes sem alteração de asserções. A comparação de API contra as baselines publicadas registra zero incompatibilidades binárias ou de fonte.
- **SC-006** Cada ocorrência da palavra "oficial" no código e na documentação remete a uma fonte normativa confirmada em `docs/fontes.md`.

## Assumptions

- Os tipos de documento da 010 são CPF, CNPJ e NIS. O código de município permanece no contexto de geografia e pode adotar os blocos compartilhados depois.
- O documento sintético de exemplo existe apenas nos testes e não é publicado.
- A pesquisa que estenda a fórmula da Caixa ao Cadastro NIS, ao NIT ou ao Pasep é opcional. Uma reclassificação depende de fonte normativa confirmada pelo usuário.
- O módulo 10 e outros algoritmos ficam para quando uma spec os exigir, como a 008.
- O plano é responsável pelas assinaturas, pacotes e ordem das etapas, e pela coordenação dos arquivos compartilhados com a 008 e a 009.
