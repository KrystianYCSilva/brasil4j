# C1 · comparação NIS, PIS, Pasep e NIT

Executado em 2026-11-26 com Corretto 21.0.13. O compilador usou `--release 11` e os fontes locais de Declarações `ca3b3d9` e UEM `9dd29d1`. A sondagem chamou `Nis.of`, `Nis.ofProvisionallyValidated`, `NisValidator.isValid` e `PisStrategy.validate`. Os dois caminhos de DV de Declarações deram o mesmo resultado. Vetores sintéticos, sem consulta a titulares.

| Caso | Entrada | Declarações estrutura | Declarações DV provisório | UEM PisStrategy |
| --- | --- | --- | --- | --- |
| 01 Canônico | `01234567897` | `01234567897` | aceita | aceita |
| 02 Máscara do legado | `012.34567.89-7` | `01234567897` | aceita | aceita |
| 03 Pontuação deslocada | `0.1234567897` | `01234567897` | aceita | aceita |
| 04 Máscara parcial | `012.34567897` | `01234567897` | aceita | aceita |
| 05 Símbolo interno | `012!34567897` | `01234567897` | aceita | aceita |
| 06 Espaço externo | ` 01234567897 ` | `01234567897` | aceita | aceita |
| 07 Zeros repetidos | `00000000000` | `00000000000` | rejeita | rejeita |
| 08 Dígitos repetidos | `11111111111` | `11111111111` | rejeita | rejeita |
| 09 DV divergente | `01234567898` | `01234567898` | rejeita | rejeita |
| 10 Dez dígitos | `0123456789` | rejeita | rejeita | rejeita |
| 11 Sinal explícito | `+01234567897` | `01234567897` | aceita | aceita |
| 12 Dígitos Unicode | `０１２３４５６７８９７` | rejeita | rejeita | rejeita |
| 13 Nulo | nulo | rejeita | rejeita | rejeita |
| 14 Vazio | `""` | rejeita | rejeita | rejeita |
| 15 Vários zeros iniciais | `00000000019` | `00000000019` | aceita | aceita |
| 16 DV divergente com zeros iniciais | `00000000063` | `00000000063` | rejeita | rejeita |
| 17 Resto 1 e DV zero | `00000000060` | `00000000060` | aceita | aceita |
| 18 Resto 10 e DV divergente | `00000000050` | `00000000050` | rejeita | rejeita |
| 19 Resto zero sem repetição | `00000000280` | `00000000280` | aceita | aceita |
| 20 Resto zero com DV divergente | `00000000281` | `00000000281` | rejeita | rejeita |
| 21 Base repetida com DV válido | `11111111116` | `11111111116` | aceita | aceita |
| 22 Máscara alternativa | `012.3456.789-7` | `01234567897` | aceita | aceita |

Os casos 19–22 foram reproduzidos em Corretto 21.0.13 na consolidação do parecer Antigravity. Os casos 19 e 20 distinguem o resto zero da guarda de onze dígitos iguais. O caso 22 caracteriza a tolerância dos legados. A proposta canônica não aceita nenhuma das duas máscaras.

## Força da evidência

- O domínio DATASUS descreve NIS/PIS/PASEP como `VARCHAR2(11)`, com dígitos de 0 a 9. A exigência de exatamente 11 algarismos vem do campo PIS/PASEP/NIT da RAIS ano-base 2024. A RAIS orienta informar a inscrição mais antiga se houver múltiplas inscrições. Isso sustenta a estrutura para os leiautes consultados e mostra por que origem e correspondência cadastral precisam ficar explícitas. [DATASUS](https://datasus.saude.gov.br/faq/quais-sao-as-classes-de-dados/) · [RAIS 2024](https://rais.gov.br/sitio/rais_ftp/ManualRAIS2024.pdf)
- A Caixa documenta seu processo de cadastramento e o leiaute de envio. O eSocial distingue qualificação na base cadastral da transmissão dos leiautes simplificados. As fontes consultadas nesta rodada não estabeleceram fórmula de DV para assumir como oficial nem uma máscara obrigatória geral. [Caixa · Cadastro NIS](https://www.caixa.gov.br/servicos/nis/Paginas/default.aspx) · [Caixa · Leiaute](https://www.caixa.gov.br/Downloads/cadastro-nis/Instrucoes_Layout_Padrao_Empresa.pdf) · [eSocial · Qualificação cadastral](https://www.gov.br/esocial/pt-br/empresas/consulta-qualificacao-cadastral/qualificacao-cadastral-1)
- Pontuação deslocada, máscara parcial e sinais aceitos pelos legados decorrem da remoção de tudo que estiver fora de ASCII 0–9. Os dois validadores legados rejeitam sequências repetidas antes do cálculo. A reprodução dos dez dígitos repetidos confirmou que o bloqueio altera o resultado da conferência matemática apenas para onze zeros. Os demais repetidos também falham pelo cálculo isolado. Isso caracteriza as duas bases e deixa a procedência normativa em aberto.

## Decisão e retomada

Propor 11 dígitos ASCII canônicos no padrão, sem remover caracteres antes da validação. Discutir máscara somente com contrato de entrada confirmado. O padrão estrutural não usa DV nem bloqueia repetições. Proteger a saída textual conforme a constituição. A opção matemática permanece candidata técnica, com fonte, revisão independente e novos vetores sintéticos exigidos antes de entrar na spec. Localizar um chamador de produção, começando pela UEM, para comparar a migração com manter a tolerância local. Nenhuma alteração nos serviços.
