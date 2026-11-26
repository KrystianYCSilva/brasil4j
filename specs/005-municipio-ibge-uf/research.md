# Research: Código IBGE de município e UF

## Fonte e vetores

A [tabela de códigos de municípios do IBGE](https://www.ibge.gov.br/explica/codigos-dos-municipios.php), consultada em 2026-11-26, estabelece sete dígitos e prefixo UF de dois dígitos. Ela também confirma os quatro códigos abaixo. As suítes dos consumidores forneceram os pontos de entrada e os exemplos. A [referência F-08](../../docs/fontes.md) liga a regra a esta base.

| Código | Município e UF | Origem do vetor |
| --- | --- | --- |
| `3550308` | São Paulo, SP `35` | Obrigações Core, `FundamentosCadastroTest` e `FundamentosRestTest` |
| `4115200` | Maringá, PR `41` | Censo Superior/UEM, `MunicipioRepositoryIntegrationTest` |
| `4113700` | Londrina, PR `41` | Censo Superior/UEM, `MunicipioRepositoryIntegrationTest` |
| `3509502` | Campinas, SP `35` | Censo Superior/UEM, `MunicipioRepositoryIntegrationTest` |

### Snapshot UF

Transcrição dos 27 pares do [IBGE · Localidades](https://servicodados.ibge.gov.br/api/v1/localidades/estados) em 2026-11-26, conferida com a tabela de códigos e a [Malha Municipal do IBGE](https://www.ibge.gov.br/geociencias/organizacao-do-territorio/malhas-territoriais/15774-malhas.html?edicao=24048&t=downloads). Este recorte fixa a referência dos testes por data de consulta. Um catálogo de municípios por edição fica para outra spec.

| Região | Pares sigla e código |
| --- | --- |
| Norte | RO 11, AC 12, AM 13, RR 14, PA 15, AP 16, TO 17 |
| Nordeste | MA 21, PI 22, CE 23, RN 24, PB 25, PE 26, AL 27, SE 28, BA 29 |
| Sudeste | MG 31, ES 32, RJ 33, SP 35 |
| Sul | PR 41, SC 42, RS 43 |
| Centro-Oeste | MS 50, MT 51, GO 52, DF 53 |

## Metadados de UF

Nomes, siglas e capitais foram conferidos no Mapa Político do Brasil do IBGE (base BC250 de 2023). Os códigos nacionais por UF foram conferidos na relação da Anatel e na publicação do Ministério das Comunicações de agosto de 2026. Consulta em 2026-11-26. O código 61 ocorre em DF e GO. [IBGE · Mapa Político](https://geoftp.ibge.gov.br/cartas_e_mapas/mapas_do_brasil/politico/brasil_politico5000k_2023.pdf) · [Anatel · Códigos Nacionais por UF](https://www.anatel.gov.br/Portal/verificaDocumentos/documento.asp?assuntoPublicacao=null&caminhoRel=null&documentoPath=telefonia_fixa%2Fstfc%2Fcod_prestadoras%2Flista_csp_codigos_nacionais.pdf&filtro=1&numeroPublicacao=141766) · [MCom · Lista atual](https://www.gov.br/mcom/pt-br/noticias/2026/agosto/saiba-a-importancia-da-discagem-direta-a-distancia-ddd)

A busca remove diacríticos, ajusta caixa com localidade neutra e aplica `String.trim()`. Essa tolerância é própria da busca por nome. As saídas preservam a grafia oficial, e a busca por código IBGE continua exata.

| Sigla | Código IBGE | Nome oficial | Capital | DDDs |
| --- | --- | --- | --- | --- |
| RO | 11 | Rondônia | Porto Velho | 69 |
| AC | 12 | Acre | Rio Branco | 68 |
| AM | 13 | Amazonas | Manaus | 92, 97 |
| RR | 14 | Roraima | Boa Vista | 95 |
| PA | 15 | Pará | Belém | 91, 93, 94 |
| AP | 16 | Amapá | Macapá | 96 |
| TO | 17 | Tocantins | Palmas | 63 |
| MA | 21 | Maranhão | São Luís | 98, 99 |
| PI | 22 | Piauí | Teresina | 86, 89 |
| CE | 23 | Ceará | Fortaleza | 85, 88 |
| RN | 24 | Rio Grande do Norte | Natal | 84 |
| PB | 25 | Paraíba | João Pessoa | 83 |
| PE | 26 | Pernambuco | Recife | 81, 87 |
| AL | 27 | Alagoas | Maceió | 82 |
| SE | 28 | Sergipe | Aracaju | 79 |
| BA | 29 | Bahia | Salvador | 71, 73, 74, 75, 77 |
| MG | 31 | Minas Gerais | Belo Horizonte | 31, 32, 33, 34, 35, 37, 38 |
| ES | 32 | Espírito Santo | Vitória | 27, 28 |
| RJ | 33 | Rio de Janeiro | Rio de Janeiro | 21, 22, 24 |
| SP | 35 | São Paulo | São Paulo | 11, 12, 13, 14, 15, 16, 17, 18, 19 |
| PR | 41 | Paraná | Curitiba | 41, 42, 43, 44, 45, 46 |
| SC | 42 | Santa Catarina | Florianópolis | 47, 48, 49 |
| RS | 43 | Rio Grande do Sul | Porto Alegre | 51, 53, 54, 55 |
| MS | 50 | Mato Grosso do Sul | Campo Grande | 67 |
| MT | 51 | Mato Grosso | Cuiabá | 65, 66 |
| GO | 52 | Goiás | Goiânia | 61, 62, 64 |
| DF | 53 | Distrito Federal | Brasília | 61 |

## Consumidores e adoção

| Ponto observado | Contrato atual e impacto | Avaliação de adoção |
| --- | --- | --- |
| Obrigações Core `6e31e39`, Java 21 no POM, `Municipio.kt` | A entrada brasileira exige o texto de sete dígitos ASCII e coerência com a UF declarada. O prefixo sem UF correspondente falha na conferência. Espaços e sinais falham no formato. País, identificador interno e existência seguem no serviço. | Pendente com a manutenção do cadastro de municípios do Core. Confirmar interesse em substituir a checagem local sem alterar a borda. |
| Censo Superior/UEM `9117641`, Java 11 no POM, `ReferenciaController.listarMunicipios` | O controller recebe um `Long` e pode filtrar também por UF. A conversão textual já ocorreu antes da chamada ao controller. A biblioteca recebe texto canônico recomposto apenas se integrada. A gramática da requisição permanece responsabilidade do framework e do serviço. | Pendente com a manutenção do endpoint de referências do Censo. Confirmar interesse e se há necessidade de mudar tolerâncias de entrada ou tratar prefixos desconhecidos. |

A proposta é disponibilizar um valor estrutural reutilizável com adoção separada. O responsável pela consolidação do C1 registra essa opção na T001 para aprovação de Krystian. A adoção em cada serviço exige confirmação de sua manutenção.

## Alternativas e limites de verificação

- Manter a checagem local tem baixo custo de integração. O ganho do brasil4j é compartilhar valor e tabela de UFs em Java e Kotlin.
- `CodigoMunicipio` em Declarações `ca3b3d9` usa `trim`. O Java 11 do módulo é compatível com os JDKs declarados pelos dois consumidores estudados, mas excede a linha mínima Java 8 do brasil4j. Licença aplicável, artefato consumível e ausência da projeção de UF pesam na comparação.
- As sondagens em Corretto 8u512 e 21.0.13 confirmaram ida e volta textual dos quatro vetores. Em Java 8 e 21, `Long.parseLong` aceitou sinal, zero adicional e dígitos Unicode que falham na gramática proposta. `0012345` perdeu zeros no caminho numérico. Esses resultados caracterizam a conversão Java em isolamento. A validação HTTP depende de executar o serviço com entradas originais. [Evidências do C1](../../docs/descoberta/c1/retomada.md)
