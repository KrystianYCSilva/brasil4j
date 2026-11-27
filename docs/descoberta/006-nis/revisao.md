# Revisão do recorte da Spec 006

**Data** 2026-11-26 · **Base** `8e94704689ac4a48b174a29ab264f0f5eba0164d`

**Decisão de prontidão.** O contrato estrutural está tecnicamente delimitado. A recomendação é manter a Spec 006 em descoberta até confirmar um ponto de uso em que o tipo seja útil. A CVU é a primeira candidata. O recorte pode ser promovido quando houver um ponto de entrada ou contrato de integração verificável e exemplos sintéticos que mostrem a apresentação recebida, a ausência e a etapa responsável pela validação. Esse critério pode ser satisfeito sem compromisso de adoção por parte da UEM.

[Pesquisa complementar de leiautes oficiais](leiautes-oficiais.md). Posterior aos pareceres, ela acrescenta contratos verificáveis de arquivo e o caso de importação histórica. Também confirma usos documentados no SAL e no Conectividade Social. A escolha de uma integração concreta segue em aberto.

Os pedidos de revisão partiram dos mesmos dois documentos. Os hashes identificam os arquivos originais do pacote.

| Documento | SHA-256 |
| --- | --- |
| [Recorte](recorte.md) | `D6226F05607D0C06053337EBB83716A7951D75751539CDFB8071C18CD942E4C4` |
| [Matriz C1](../c1/nis-matriz.md) | `1B41F66F86D643B3F950EA29A75E8BB8D25CABD7E7DC7AF794A88E4015C84BD2` |

## Parecer do Claude

**Precisa de ajustes antes de formalizar.** O valor estrutural atende à proposta de preservar 11 dígitos ASCII e proteger a representação textual. O Claude pediu um ponto de uso comprovado com exemplos sintéticos, uma decisão explícita sobre a máscara do SIPREV e a caracterização do impacto para os legados. Sugeriu aceitar a máscara integral do SIPREV junto com a forma canônica. Também recomendou ocultação neutra, origem no consumidor e prova da representação textual tanto em Java quanto em Kotlin.

O Claude conferiu os hashes, leu a constituição e os artefatos anteriores e recalculou vetores. Sua leitura dos legados externos se apoiou nos documentos do pacote.

## Parecer do Antigravity

**Aprovado com ressalvas** para formalizar somente o valor estrutural. O Antigravity recomenda exigir a forma canônica, definir exatamente a ocultação na saída padrão e nos erros e declarar que a evidência da UEM cobre coleta e conferência cadastral. Considerou que localizar o formulário, confirmar o tratamento da ausência e identificar o DV aplicável podem ficar para a adoção. Orientou tratar conversões numéricas na fronteira de cada leiaute.

O Antigravity reproduziu manualmente os casos 07, 17, 19 e 20 e distinguiu o DV de PIS/Pasep publicado pela Caixa para o FGTS do cálculo de número de benefício que aparece na decisão CMRI. O parecer se baseou nos dois documentos incorporados ao pedido. Conferi as fontes citadas e o método de Censo Superior na checagem abaixo. A execução via `agy` em PowerShell retornou um parecer parcial no limite de tempo e concluiu com sucesso após a retomada da mesma conversa.

## Checagem complementar

- **CVU/UEM.** A publicação institucional confirma a coleta de PIS/Pasep ou NIT junto a estudantes e a conferência da inscrição com dados da DAA. Ainda faltam identificar o ponto de entrada do software, a apresentação aceita, o tratamento da ausência e as validações aplicadas. [UEM · PAS 2026](https://noticias.uem.br/index.php?Itemid=211&catid=986&id=31881%3Apas-2026-abre-inscricoes-para-fiscais-de-prova-veja-como-se-inscrever&option=com_content&view=article)
- **Declarações.** No checkout `ca3b3d9`, as chamadas qualificadas a `Nis.of` e `Nis.ofProvisionallyValidated` encontradas estão em testes. A fachada `GovValidators` é chamada pelo próprio tipo e pela suíte de testes. `Tipo13` e `Tipo30` do leiaute GFIP/SEFIP modelam um NIT textual de 11 posições, porém a busca pelos acessores nessa implementação encontrou declarações e testes. Isso acrescenta um contrato candidato, com integração em produção ainda por demonstrar.
- **Censo Superior.** O DDL em `9117641` declara `CPCFPN BIGINT` (linha 98) e mapeia a coluna para `NU_PIS_PASEP` (linha 256). Reexecutei em Corretto 21.0.13 o corpo de `ZkUtils.verificaPIS` em `7b75b74`, isolado com os vetores sintéticos do C1. Aceitou 02, 07, 17, 19 e 22 e rejeitou 08 e 20. O caso 07 distingue esse helper de `PisStrategy`. A sondagem caracteriza o método em isolamento.
- **Caixa.** Na p. 25, o exemplo impresso usa a base `1002412610`. A soma ponderada dá 90, o resto é 2 e o DV é 9. O documento diz “Se resto igual a um, número de inscrição é inválido.” A cláusula vale para o processo PIS/Pasep documentado no caderno. [Caixa · Caderno do Agente Financeiro v016](https://www.caixa.gov.br/Downloads/fgts-conectividade-social/CADERNO_AGENTE_FINANCEIRO_V_016_05_04_2024.pdf#page=25)
- **Critério de promoção.** A Spec 005 usou códigos observados em fluxos consumidores como evidência. A constituição pede demanda identificada e contrato para novos contextos. A quantidade de consumidores da descoberta de IBGE foi uma condição daquela rodada. Para esta decisão, importa demonstrar pelo menos uma operação em que a validação de forma e a preservação de zeros resolvam uma necessidade concreta. [Spec 005](../../../specs/005-municipio-ibge-uf/spec.md) · [Constituição](../../../.specify/memory/constitution.md)

## Impacto de migração

As colunas de legado abaixo se referem especificamente aos modos com DV registrados no C1. O modo exclusivamente estrutural de Declarações tem comportamento próprio, registrado na matriz.

| Casos do C1 | Declarações DV provisório | UEM `PisStrategy` | Proposta canônica |
| --- | --- | --- | --- |
| 02–06, 11 e 22 | Aceita | Aceita | Rejeita por apresentação. O caso 02 seria o único adicional aceito caso se adote a máscara exata do SIPREV. |
| 07, 08, 09, 16, 18 e 20 | Rejeita | Rejeita | Aceita por forma. Uma integração que exige DV ou rejeita repetições precisa preservar essa política. |
| 17 | Aceita | Aceita | Aceita por forma. A Caixa rejeita pelo resto 1 no processo de FGTS documentado. |

O formato canônico segue como proposta para um primeiro contrato. A máscara SIPREV é evidência de uma apresentação naquele sistema. A demanda da CVU por essa máscara ainda precisa ser confirmada. Uma decisão de adotar a máscara teria de preservar a verificação da posição exata dos separadores. [Dataprev · RV_03](https://www.gov.br/previdencia/pt-br/outros/imagens/2015/07/rgrva_RegrasValidacao.pdf)

Para o aceite da futura spec, incluir a representação padrão Java e Kotlin sem exposição do número, inclusive por interpolação ou quando o valor Kotlin for tratado como `Any`. Verificar também que erros de entrada não reproduzem o valor recebido. Conversão numérica e consulta cadastral exigem os contratos dos consumidores. Uma regra de DV precisa de fonte aplicável ao respectivo fluxo. A CTPS fica para sua própria spec.
