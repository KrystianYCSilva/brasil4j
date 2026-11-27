# Spec 006 em descoberta · NIS, PIS, Pasep e NIT

**Data** 2026-11-26 · **Base brasil4j** `8e94704689ac4a48b174a29ab264f0f5eba0164d` (`v0.2.0`) · **Estado** proposta para revisão independente. A [matriz do C1](../c1/nis-matriz.md) contém os 22 casos reproduzidos nos legados.

## Uso na UEM

A CVU solicita PIS/Pasep ou NIT aos estudantes de graduação e pós-graduação que se inscrevem como fiscais. A UEM documenta essa coleta no PAS e no Vestibular de Verão de 2026. No PAS, a efetivação da inscrição depende da correspondência das informações exigidas com os registros da DAA para estudantes e da PRH para servidores. Para os servidores, a orientação de inscrição lista matrícula funcional e CPF. [UEM · PAS 2026](https://noticias.uem.br/index.php?Itemid=211&catid=986&id=31881%3Apas-2026-abre-inscricoes-para-fiscais-de-prova-veja-como-se-inscrever&option=com_content&view=article) · [UEM · Vestibular de Verão 2026](https://asc.uem.br/index.php?Itemid=211&catid=986%3Apgina-central&id=31947%3Ainscricoes-para-fiscais-do-vestibular-de-verao-vao-ate-sexta-feira-30&option=com_content&view=article) · [CVU · Regulamento de fiscais do PAS](https://vestibular.uem.br/cvu/fis_regulamento.zul?id=64)

O caso candidato é receber e preservar a inscrição informada pelo estudante antes da conferência com a DAA. O fluxo público comprova a coleta e a conferência cadastral. Formatos aceitos, tratamento de zeros iniciais e validação matemática do formulário continuam sem evidência direta. A busca foi em fontes públicas e checkouts locais, sem envio de inscrições ou acesso a cadastros.

| Evidência local | Resultado |
| --- | --- |
| `uem-npd-zk-utils` em `9dd29d1`, `ValidationUtils.java:53` e `PisStrategy.java:24–35` | `ValidationUtils.isPis` delega a validação ao enum e à estratégia. O chamador encontrado para essa API foi o teste. |
| `censosuperior` em `7b75b74`, `ZkUtils.java:570` e `RhFuncionario.java:133–136` | Há um helper de DV com testes e um campo `NU_PIS_PASEP` mapeado em `BigInteger`. A busca pelos acessores do campo encontrou suas declarações. A passagem por um número perde a largura original. |
| `censo-superior-service` em `9117641`, `docs/DDL/TABELAS_RH.sql:255–258` | O DDL registra o campo de RH e sua origem em `CPCFPN`. O mapeamento evidencia presença do dado em outro contexto. |

O código da inscrição da CVU e uma ligação dele com os validadores acima ficaram em aberto. Para avaliar adoção, o responsável pelo formulário precisa identificar o ponto de entrada e confirmar, com exemplos sintéticos, a apresentação recebida, o tratamento de ausência e a eventual regra de DV. A conferência com a DAA continua uma operação do fluxo da UEM.

## Fontes oficiais e alcance

| Fonte | O que sustenta |
| --- | --- |
| [DATASUS · Classes de dados](https://datasus.saude.gov.br/faq/quais-sao-as-classes-de-dados/) | Campo textual `VARCHAR2(11)` com caracteres 0 a 9 para NIS/PIS/Pasep. |
| [MTE · Manual RAIS ano-base 2024](https://rais.gov.br/sitio/rais_ftp/ManualRAIS2024.pdf), p. 25, A.2 | Onze algarismos quando o campo PIS/Pasep/NIT é informado. O campo é opcional desde o ano-base 2021. A escolha da inscrição mais antiga se aplica àquele leiaute. |
| [Caixa · Caderno do Agente Financeiro v016](https://www.caixa.gov.br/Downloads/fgts-conectividade-social/CADERNO_AGENTE_FINANCEIRO_V_016_05_04_2024.pdf#page=25), vigência indicada de 2024-04-05, p. 25 | Fórmula publicada para PIS/Pasep no contexto operacional do FGTS. Dez algarismos recebem pesos `3, 2, 9, 8, 7, 6, 5, 4, 3, 2`, da esquerda para a direita. Aplica-se módulo 11. Resto 0 dá DV 0, resto 1 invalida a inscrição e os restos 2 a 10 dão `11 − resto`. |
| [Dataprev · SIPREV Gestão, Regras de Validação v1.3](https://www.gov.br/previdencia/pt-br/outros/imagens/2015/07/rgrva_RegrasValidacao.pdf), p. 5, RV_03 | Onze algarismos, apresentação `XXX.XXXXX.XX-X`, verificação pelo padrão do NIT e rejeição de algarismos todos iguais no SIPREV. O documento remete ao padrão para o cálculo. |
| [Casa Civil · decisão CMRI 185/2022](https://www.gov.br/casacivil/pt-br/assuntos/colegiados/comissao-mista-de-reavaliacao-de-informacoes-cmri/decisoes-de-recurso-de-4a-instancia/2022/decisao-185-2022-nup-03005-187594-2022-16.pdf), p. 2 | Registra a resposta do INSS sobre NIT de 11 algarismos, com DV final em módulo 11, e identifica os gestores de NIT, NIS, PIS e Pasep. O material público consultado não detalha pesos e exceções do NIT. Os nove pesos e a regra de restos 0 e 1 publicados nessa mesma página pertencem ao número do benefício. |

A fórmula da Caixa estabelece o DV para o processo documentado. A pesquisa ainda não confirmou a extensão dessa fórmula ao Cadastro NIS, ao NIT do INSS ou a todo o acervo do Pasep administrado pelo Banco do Brasil. A comparação abaixo aplica somente a fórmula publicada, sem acrescentar a regra de repetição do SIPREV ou regras cadastrais.

| Caso do C1 | Soma e resto | Fórmula Caixa | Validadores de DV do C1 |
| --- | --- | --- | --- |
| 17 · `00000000060` | 12, resto 1 | Rejeita | Aceitam com DV 0 |
| 19 · `00000000280` | 22, resto 0 | Aceita DV 0 | Aceitam |
| 20 · `00000000281` | 22, resto 0 | Rejeita DV divergente | Rejeitam |
| 07 · `00000000000` | 0, resto 0 | Cálculo isolado dá DV 0 | Rejeitam pela guarda de repetição |
| 18 · `00000000050` | 10, resto 10 | Rejeita, DV esperado 1 | Rejeitam |

A conferência independente recalculou as somas acima e reproduziu o exemplo impresso do caderno (soma 90, resto 2, DV 9). A coluna de legados é a evidência executada no C1. Os valores da tabela são sintéticos.

## Recorte para revisão

- **Valor estrutural comum.** Receber texto com exatamente 11 caracteres ASCII de `0` a `9`. Validar antes de transformar e preservar integralmente a entrada, inclusive zeros iniciais. A igualdade considera o texto canônico. Fica com o consumidor a semântica de campo ausente e de origem da inscrição. O texto por si só não atribui origem nem estabelece correspondência entre inscrições.
- **Apresentação e privacidade.** Rejeitar comprimento divergente, nulo, vazio, espaços, sinais, separadores e dígitos Unicode. Disponibilizar o texto canônico por acesso explícito. Ocultar o número completo na representação textual padrão e nos erros. Avaliar uma máscara somente com contrato de entrada confirmado.
- **Semântica estrutural.** Onze algarismos repetidos passam pelo contrato de forma. Uma representação estrutural tampouco aplica DV. A aceitação significa apenas que o texto tem a estrutura esperada. Cadastro, titularidade, situação e escolha entre inscrições continuam com o fluxo consumidor.
- **Alternativas.** Manter a validação local é suficiente se o contrato da CVU for específico ao formulário. O valor comum é candidato quando preservar a representação textual e proteger a saída traz benefício compartilhado. Avaliar uma futura política de DV somente para um fluxo explicitamente identificado, com fonte aplicável e impacto de migração documentado. A CTPS terá spec própria.
- **Aceite proposto.** Conferir os 22 casos do C1 contra a regra estrutural e acrescentar entrada de 12 algarismos, ida e volta textual com zeros iniciais e exposição por erro e representação padrão. Provar o mesmo comportamento nos dois artefatos consumidos em Java 8 e 21. Inspecionar eventual adaptação numérica em separado, com ausência e largura definidas pelo leiaute de origem.

Na conferência dos 22 vetores da matriz, a gramática proposta aceita os casos 01, 07–09 e 15–21. Rejeita os casos 02–06, 10–14 e 22. O resultado caracteriza a proposta de formato e pode ser usado para revisar os futuros testes.

## Pedido de revisão

Entregar [este recorte](recorte.md) e a [matriz do C1](../c1/nis-matriz.md) aos dois revisores na mesma revisão do Git. Pedir parecer em leitura com caminhos ou fontes, achados reproduzíveis, impacto para consumidores e recomendação de escopo.

- **Claude.** Verifique se a coleta da CVU e os mapeamentos de RH justificam o valor comum frente à validação local. Avalie o contrato mínimo para Java 8 e Kotlin, a necessidade de representar a origem e o que exigiria confirmação do responsável pela CVU. Indique quais mudanças seriam incompatíveis com o formulário ou com a tolerância dos legados.
- **Antigravity.** Tente refutar a leitura da p. 25 do caderno e a aplicabilidade ao NIT/NIS/Pasep fora do contexto FGTS. Reproduza a divergência de resto 1, o resto 0 com e sem repetição, o DV incorreto, a máscara exata do SIPREV e a perda de largura na fronteira numérica. Verifique se as fontes da UEM comprovam coleta, formatos aceitos e/ou cálculo local. Cite a evidência para cada afirmação.

Após os pareceres, registrar as decisões e submeter o recorte para formalização no Spec Kit. O trabalho desta rodada é de descoberta.
