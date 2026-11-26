# C1 · retomada pós-CPF e CNPJ

**Registro de descoberta em 2026-11-26.** Base do brasil4j `1a0d045` em `main`, com a Spec 004 de publicação em andamento no checkout. A decisão do catálogo estava em arquivos locais de `docs/roadmap-pessoal` sobre `fed626f`. A biblioteca global estava em `35c7b83`, com arquivos locais adicionais. As referências foram usadas em leitura. Este registro complementa o [C1 original](README.md).

## Encaminhamento

| Candidato | Evidência | Próxima decisão |
| --- | --- | --- |
| **Código de município IBGE e código da UF** | Dois pontos de uso reais e uma implementação anterior. A tabela do IBGE define o mesmo identificador em todos os casos. | Melhor candidato à próxima spec, sujeito ao aceite da fatia e da intenção de uso. Começar por interpretar sete dígitos ASCII e expor/decompor o código da UF. |
| Calendário e dias úteis | O plano histórico de Declarações prevê o cálculo. A busca nos fontes versionados do módulo e nos pontos examinados do Core deixou em aberto o chamador de um vencimento e sua política. | Manter como diferencial da v0.2. Antes de promover, nomear um vencimento real e confirmar jurisdição, fonte, sábado e direção do ajuste com o responsável. |
| NIS/PIS/PASEP | `uem-npd-zk-utils` `9dd29d1` tem `ValidationUtils.validatePis` delegando a `PisStrategy`. A evidência dessa busca identifica uma utilidade, sem confirmar o chamador final nem a origem normativa do cálculo. O eSocial simplificado deixou de exigir NIS em seus leiautes. | Buscar um consumidor que precise da regra e documentar fonte, formato e finalidade. Avaliar DV em separado. [Qualificação cadastral do eSocial](https://www.gov.br/esocial/pt-br/empresas/consulta-qualificacao-cadastral/qualificacao-cadastral-1) |
| Complementos de identidade e integração fiscal | A raiz do CNPJ já é exposta por `Cnpj.getRoot()`. O catálogo mantém `RaizCnpj`, `CpfCnpj` e chaves fiscais no horizonte. | Exigir um contrato de uso próprio para novos tipos. A chave de 44 posições permanece candidata da v0.3. |

## IBGE

- **Obrigações Core `6e31e39`**. `Municipio.kt` exige sete dígitos ASCII e confere o prefixo contra a UF. `FundamentosCadastroTest.kt:15` e `FundamentosRestTest.kt:88` registram o comportamento de cadastro, inclusive UF divergente. A entidade mantém identidade interna e condições próprias para localidades estrangeiras.
- **Censo Superior/UEM `9117641`**. `ReferenciaController.java:113` aceita `codigoIbge` como `Long` para consultar município, sozinho ou com UF. `MunicipioRepositoryIntegrationTest.java:24` usa os pares 41/4115200, 41/4113700 e 35/3509502 em testes do repositório. As representações na borda diferem dos tipos internos do Core.
- **Declarações `ca3b3d9`**. `declaracoes-gov-core-domain` tem `CodigoMunicipio.of` e testes. Aceita espaços externos por `trim`. A busca versionada achou essa classe e seus testes, sem chamada de produção à fábrica. O POM pai declara Java 11. A licença aplicável e a disponibilidade de um artefato consumível seguem a confirmar. Essa referência dá um contraexemplo para a política de espaços.
- **Fonte e demonstração**. O IBGE define sete dígitos e prefixo de dois dígitos para UF. Uma sondagem isolada em Corretto 8u512 e 21.0.13 preservou `3550308`, `4115200`, `4113700` e `3509502` na conversão entre texto e inteiro para os casos observados. A checagem ASCII rejeitou sinal, zero acrescentado, seis dígitos, espaço e dígitos Unicode. A conversão Java de `Long` sozinha aceitou sinal positivo, zero acrescentado e dígitos Unicode. As suítes dos serviços foram consultadas por leitura. [Tabela e exemplos do IBGE](https://www.ibge.gov.br/explica/codigos-dos-municipios.php)
- **Alternativas**. Manter as checagens locais tem custo baixo e preserva a tolerância de cada borda. Adotar o módulo de Declarações implica Java 11, confirmação de licença/disponibilidade e adaptação à sua política de apresentação. Uma pequena API do brasil4j vale a pena se os consumidores quiserem a mesma decomposição e a mesma checagem de coerência. Separar valor estrutural, existência em edição do catálogo e regras cadastrais. Um algoritmo de DV, carga nacional e consulta remota exigem decisão própria.

**Resultado de aceite proposto.** Demonstrar, sobre os quatro códigos da fonte, preservação da forma canônica, extração da UF, coerência com UF informada e conversão sem mudar as buscas válidas. Confirmar com os consumidores a política de UF desconhecida, espaços e sinais. Para medir regressões, reproduzir o resultado observável nos dois pontos de entrada usando fixtures públicas ou sintéticas.

## Calendário e dias úteis

A busca partiu do `QWEN-PLAN-V1.md:202` de Declarações `ca3b3d9`. Esse plano prevê `DiasUteisUtils` e catálogo nacional. O `commons-time` da UEM `23d4a78` é evidência de utilitários de tempo. A fatia inicial candidata é `LocalDate` e feriados nacionais com vigência, com política explícita para calcular ou ajustar um prazo.

| Caso | Regra de referência | Tupinamba em JDK 21 |
| --- | --- | --- |
| 20/11/2023 e 20/11/2024 | A Lei 14.759 entrou em vigor em 22/12/2023. A primeira ocorrência anual foi em 2024. Regras estaduais ou municipais podem ter datas anteriores. | Ambas retornaram dia útil. |
| 16 e 17/02/2026 | O art. 6 da Resolução CMN 4.880 exclui segunda e terça de Carnaval no seu escopo. A portaria federal de 2026 as classifica como pontos facultativos em seu próprio escopo. | Só a terça retornou feriado nacional. |
| 03/04 e 04/06/2026 | A Lei 9.093 trata feriados religiosos por lei municipal, incluindo Sexta-feira da Paixão. A portaria federal de 2026 apresenta Paixão de Cristo como feriado nacional para o público abrangido e Corpus Christi como ponto facultativo. Corpus Christi também é excluído pelo CMN em seu próprio escopo. | Ambas retornaram feriado nacional. |

O teste compilou apenas `FeriadoUtils` do acervo `53ac1c8` com `--release 11` e o executou em Corretto 21.0.13. O POM histórico declara Java 11 e a licença de incorporação segue sem confirmação. A leitura do Jollyday Focus Shift, em `main`, confirma Apache-2.0, Java 17, mapeamento JAXB ou Jackson e início de 20/11 em 2024 no XML brasileiro. O mesmo XML lista feriados móveis no calendário nacional. Isso serve como comparação de dados. A leitura não prova o comportamento de uma release ou de uma política de vencimento. Um cálculo local pode bastar para um único prazo. O Jollyday pode atender apps em Java 17 que aceitem suas dependências e semântica. O brasil4j ganha força se um consumidor precisar de Java 8, política explícita e regras rastreáveis por vigência.

[Lei 14.759/2023](https://www.planalto.gov.br/ccivil_03/_ato2023-2026/2023/lei/l14759.htm) · [Lei 9.093/1995](https://www.planalto.gov.br/ccivil_03/leis/l9093.htm) · [Portaria MGI 11.460/2025](https://legis.sigepe.gov.br/sigepe-bgp-ws-legis/legis-service/download/?id=0026440285-ALPDF%2F2025) · [Resolução CMN 4.880/2020](https://www.bcb.gov.br/estabilidadefinanceira/exibenormativo?numero=4880&tipo=RESOLU%C3%87%C3%83O+CMN) · [Jollyday](https://github.com/focus-shift/jollyday) · [XML para o Brasil](https://raw.githubusercontent.com/focus-shift/jollyday/main/jollyday-core/src/main/resources/holidays/Holidays_br.xml)

## Revisão e retomada

- **Claude.** Conferir se o valor compartilhado justifica uma API em Java 8, frente a manter a checagem local ou adotar Declarações. Conferir fronteira de localidades e menor desenho utilizável também em Kotlin.
- **Antigravity.** Tentar refutar o contrato comum com exemplos reais da tabela, UF divergente, entradas convertidas prematuramente para `Long` e diferenças de tolerância. Conferir se a matriz de calendário preserva o escopo e a vigência de cada fonte.

O Codex consolida os dois pareceres sobre esta mesma base. Confirmado o recorte, o próximo incremento segue `spec → plan → tasks → analyze` no Spec Kit. Após aprovação, cada comportamento entra em TDD com contrato Java e Kotlin. A publicação da v0.1.0 segue a trilha já preparada e aguarda o namespace.
