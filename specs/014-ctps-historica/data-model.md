# Modelo de dados · Spec 014

| Entidade | Campos e invariantes |
| --- | --- |
| Perfil `CtpsProfile` | Escolha explícita. O primeiro perfil é `ESOCIAL_S2200_V2_5`. |
| Grupo `CtpsGroup` | Presença representada pela instância. Número, série e UF são textos brutos anuláveis. Texto vazio representa campo fornecido. Origem obrigatória e indicação booleana de possível perda no sistema de origem. |
| Origem `CtpsOrigin` | `PHYSICAL`, `CPF_PROJECTION` ou `UNDETERMINED`. A indicação de perda é independente da origem e do desfecho. |
| Registro `CtpsRecord` | Número e série originais e UF como `Uf`. Existe apenas quando os três campos satisfazem o perfil. A sigla da enumeração reproduz o texto de UF aceito. |
| Resultado `CtpsCheckResult` | Perfil, estado, conjunto imutável de campos afetados e registro opcional. Origem e indicação de perda são observáveis para todo grupo presente. |

## Decisão e precedência

| Ordem | Condição | Desfecho | Campos afetados |
| --- | --- | --- | --- |
| 1 | Grupo ausente | `ABSENT` | Nenhum |
| 2 | Grupo presente com algum campo nulo | `INCOMPLETE` | Todos os campos nulos |
| 3 | Três campos fornecidos, algum fora do perfil | `OUT_OF_PROFILE` | Todos os campos que violam as facetas |
| 4 | Três campos conformes | `CONFORMING` | Nenhum |

O comprimento considera pontos de código. A UF exige sigla ASCII maiúscula exata de uma das 27 UFs. A conferência preserva a sequência de caracteres entregue. Um texto vazio conta como campo fornecido e viola a faceta correspondente. A UF de expedição é derivada apenas de registro conforme de origem `PHYSICAL`.

## Limites de representação

Os textos brutos do grupo servem à conferência. Número e série ficam acessíveis explicitamente pelo registro conforme. Diagnósticos mostram somente perfil, desfecho, nomes de campos afetados ou marcador fixo. A CTPS Digital usa o CPF informado em seu fluxo, inclusive quando há outro registro que declara uma projeção histórica.
