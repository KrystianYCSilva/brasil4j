# C1 · decisão sobre o primeiro incremento do brasil4j

**Base compartilhada para revisão 0.1 · 25/11/2026.** Descoberta de 30/11 a 13/12/2026, com cinco horas de trabalho e uma hora de revisão e fechamento. Esta rodada antecipa a preparação documental.

## Registro de decisão

| Item | Registro |
| --- | --- |
| Problema | O cadastro do Core aceita representações que uma API de entrada estrita rejeitaria. Isso é observável. Ainda falta uma necessidade confirmada de bloquear essas representações ou um segundo consumidor que precise do mesmo contrato. |
| Consumidor candidato | Obrigações Core, `POST /api/v1/empresas`. Declarações tem outra validação estrutural, com tolerância distinta. Uma eventual adoção deve ser confirmada com o responsável de cada sistema. |
| Resultado observável | Java e Kotlin consomem o mesmo JAR, com 27 casos de comparação e runtime Java 8, preservando zeros e tornando diferenças do cadastro explícitas. |
| Decisão preliminar | Manter CNPJ como spec piloto. Se a necessidade for exclusiva do Core, uma correção local após ajuste explícito do contrato tem menor custo de integração. Um pré-filtro estrito seguido do JAR publicado reproduziu os 27 resultados da spec candidata em Java 8 e 21, inclusive os valores canônicos. Promover implementação própria se houver ganho compartilhado e insuficiência comprovada dessas alternativas. |
| Menor fatia estimada | Cinco horas como teto para validar na revisão: 45 min de build, 2 h de ciclos TDD para o núcleo e a matriz, 1 h de contrato Java/Kotlin, 1 h de verificação em JDK 8/21 e 15 min de fechamento. A estimativa pressupõe ambiente pronto e API acordada. Documentos adicionais, extensão Kotlin, publicação e adoção em serviços exigem outros incrementos. |

## Base comparada e responsabilidade

O catálogo de trabalho está em `docs/roadmap-pessoal`, HEAD `fed626fb04292e655f3bc983536bb5a7aebb5517`, sem AGENTS.md ou constituição local no checkout inspecionado. Os seis arquivos da revisão `4f174e8286f229e30fda10daf573a05dd1426318` são textualmente equivalentes aos do catálogo após normalizar fim de linha. O arquivo local adicional `docs/panorama/lib-br-c1-decisao.md` registra o nome, coordenadas, visão v0.1 e diretivas escolhidos. Sua versão lida tem SHA-256 `A9018C74FF816778F7CF49F5CB9FD105622B8AA79E99213A409BB9F7A825D791`. O catálogo foi usado em leitura.

A biblioteca global de agentes estava limpa em `main` / `35c7b83`. O Core estava limpo em `main` / `6e31e390de5b8b87bfebf09b00ce7ebfe3a2ea1e`. O Codex é o único escritor no brasil4j nesta rodada, branch `main` recém-inicializada, sem SHA-base Git até o primeiro commit. Arquivos sob responsabilidade: README.md, AGENTS.md, constituição, docs e spec 001. A revisão dos outros agentes é em leitura sobre o mesmo pacote. Para futuras escritas paralelas, identificar responsável, SHA-base, branch ou worktree e arquivos.

## Plano e ponto de retomada

| Bloco | Tempo | Próxima ação e saída |
| --- | --- | --- |
| Inventário | 1 h | Usar o [inventário atualizado](inventario.md) e confirmar identidade de consumidores compartilhados com seus mantenedores. |
| Hipótese CNPJ | 2 h | Usar a [matriz executada](matriz-cnpj.md). Com o dono do cadastro, enumerar apresentação dos clientes e rodar a matriz na borda HTTP com dados sintéticos. Capturar código HTTP e canônico e decidir se máscara parcial, caixa e espaços exigem adaptação. |
| Reserva | 1 h | Se o reuso do CNPJ não se confirmar, localizar um fluxo que calcule vencimento ou ajuste por dia útil e validar um perfil de contagem com seu dono. Comparar `java.time` + fonte por vigência com Jollyday. O upstream consultado declara Java 17 e requer mapeamento JAXB ou Jackson. Caso de fronteira candidato: 20/11/2023 e 20/11/2024, com vigência conforme a lei e jurisdição explícita. O plano histórico do Declarações Gov Core, em `docs/hist/v1-inicial/plan/QWEN-PLAN-V1.md` dentro do módulo, registra dias úteis para cálculos de período. A busca nas fontes consultadas ainda deixou em aberto um ponto de chamada e seu contrato. IBGE depende de dois consumidores com contrato compartilhado demonstrável. [Jollyday](https://github.com/focus-shift/jollyday) · [Lei 14.759/2023](https://www.planalto.gov.br/ccivil_03/_ato2023-2026/2023/lei/l14759.htm). |
| Consolidação | 1 h | Usar os 27 resultados da sondagem de pré-filtro + JAR. Comparar custo de manutenção, disponibilidade do artefato e semântica de API com a correção local. Obter evidência de intenção de adoção fora do Core ou registrar lacuna com responsável. |
| Revisão e fechamento | 1 h | Colher os dois [pareceres](revisao.md). O Codex consolida. Krystian decide promoção, correção local, adoção ou investigação de calendário. |

Para promover ao C2, o Codex retoma a partir da [spec 001](../../../specs/002-cnpj/spec.md) e das linhas 10, 11, 16–18 e 26 da matriz. Confirmados demanda, contrato e alternativa, seguir spec, esclarecer, planejar, gerar tarefas e analisar. Caso a evidência de adoção fique pendente, usar o C2 para concluir a descoberta. O C3 é pausa e a implementação é candidata ao C4 após autorização. O eSocial segue em espera, Gate 1 em `c40ffbd` com registro `032c30d`.

Insumos desta revisão: `lib-br-plano-refinado-v0.3.md` (SHA-256 `0ED7DCE818413520C0B6835A9AEC4A710F99B81778A7E8A97553471BD1DAFA69`), `lib-br-c1-sintese.md` (SHA-256 `255C321727EA8C824D0DB7F90CD651DDA9571B5B82CA35379A086C1C028F7CD8`) e a decisão do catálogo acima.
