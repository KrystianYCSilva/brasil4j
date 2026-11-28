# Feature Specification: Conferência estrutural de CTPS em registros históricos

- **Identificador da spec** `014-ctps-historica`
- **Branch** `feature/014-ctps-historica`
- **Criada em** 2026-11-28
- **Status** Implementada, em verificação para integração
- **Base** [Descoberta de CTPS](../../docs/descoberta/ctps/recorte.md) e [pareceres de Claude e Agy](../../docs/descoberta/ctps/revisao.md).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Conferir campos legados do S-2200 v2.5 (Priority: P1)

Como integrador de arquivos trabalhistas históricos, quero conferir o grupo de CTPS depois de extrair seus campos. Preciso reconhecer a ausência do grupo, identificar problemas de preenchimento e preservar os textos de número e série para conciliar o registro com a fonte.

**Why this priority** O leiaute tem um grupo opcional com campos obrigatórios quando presente. Um verificador do perfil permite aplicar as facetas publicadas de forma uniforme. [XSD oficial](https://www.gov.br/esocial/pt-br/documentacao-tecnica/manuais/esquemas_xsd_v02-05-00_nt18.zip)

**Independent Test** Conferir os 11 fragmentos sintéticos descritos abaixo, mantendo o texto extraído e informando a origem do grupo quando presente.

**Acceptance Scenarios**

1. Dado um registro sem o grupo CTPS, obter o desfecho de grupo ausente. Dado um grupo presente sem série, obter o desfecho de grupo incompleto e a identificação do campo ausente.
2. Dado número `0000123`, série `00004` e UF `PR`, obter conformidade estrutural e recuperar o número e a série exatamente como fornecidos.
3. Dados os pares `A0` e `X`, `A-0` e `1A`, ou dois espaços e um espaço, com UF válida, preservar esses caracteres e obter conformidade às facetas do perfil. Tais desfechos descrevem a forma do fragmento.
4. Dado número com um ou 12 caracteres, série com seis caracteres, UF `XX` ou UF `pr`, obter desfecho fora do perfil e identificar o campo em desacordo sem incluir seu conteúdo nos diagnósticos padrão.

### User Story 2 - Preservar a proveniência em migrações (Priority: P2)

Como responsável por migrar cadastros antigos, quero distinguir a origem dos campos e registrar quando o armazenamento anterior pode ter descartado zeros. Preciso evitar que dados resultantes de uma convenção de preenchimento sejam interpretados como prova de emissão de carteira física.

**Why this priority** A transição do eSocial admitiu projeção de CPF em campos legados. O mapeamento de RH examinado na UEM guarda número e série em colunas inteiras. [Evidências](../../docs/descoberta/ctps/recorte.md)

**Independent Test** Conferir a mesma tripla com as três origens declaradas e repetir a conferência com dados de armazenamento numérico sinalizados pelo consumidor.

**Acceptance Scenarios**

1. Dados número `0123456`, série `7890` e UF `PR` em registros marcados como físico, projeção de CPF e indeterminado, preservar cada origem declarada. A coincidência dos textos com um CPF não determina a origem. A UF só é interpretada como de expedição para origem física declarada.
2. Dados textos `123` e `4` provenientes de colunas numéricas, com a indicação de origem com perda, preservar os textos e a indicação. A operação não preenche posições com zeros.
3. Ao registrar o desfecho ou exibir o diagnóstico padrão, identificar somente o perfil, o estado e os campos afetados, conforme necessário. A recuperação dos dados originais exige acesso explícito.
4. Dado um CPF de um fluxo digital, usar o contrato de CPF existente no modo escolhido pelo consumidor. O resultado descreve a conferência do identificador na forma escolhida. A evidência de emissão, titularidade ou vínculo vem do fluxo consumidor.

### Edge Cases

- Ausência do grupo é diferente de grupo presente vazio. Campo nulo ou omitido em grupo presente indica grupo incompleto. Texto vazio fornecido tem comprimento zero e fica fora dos limites de número e série.
- Preservar a diferença entre texto vazio e texto só com espaços. A conformidade de conteúdo segue as facetas do perfil escolhido.
- UF utiliza uma das 27 siglas maiúsculas. A entrada original pode ser recuperada conforme a operação; a conferência nunca corrige caixa ou preenche campos.
- A CTPS Digital usa CPF. A validação de apresentação ou DV do CPF é a da Spec 003. Habilitação, titularidade, vínculo e apresentação ao empregador pertencem ao fluxo consumidor.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** A conferência DEVE aplicar o perfil do grupo `trabalhador/documentos/CTPS` do S-2200 v2.5. DEVE distinguir grupo ausente, grupo presente incompleto, grupo com campo fora do perfil e grupo estruturalmente conforme. Em um grupo presente, campo omitido ou nulo determina o desfecho incompleto. Com os três campos fornecidos, o resultado depende das facetas de comprimento e UF.
- **FR-002** A conferência DEVE aceitar número de 2 a 11 caracteres e série de 1 a 5 caracteres conforme as facetas de comprimento do XSD. As duas facetas medem caracteres como no XSD. Número e série são textos sem padrão adicional. A conferência DEVE exigir UF exatamente no conjunto de 27 siglas maiúsculas do XSD.
- **FR-003** O resultado DEVE permitir recuperar explicitamente os textos originais em uma conferência conforme. A conferência DEVE preservar zeros, caixa e espaços, sem conversão numérica, preenchimento de posições ou normalização implícita.
- **FR-004** Para grupo presente, o consumidor DEVE declarar a origem como física, projeção de CPF ou indeterminada. A conferência DEVE preservar a origem e a indicação de perda informadas pelo consumidor. Só a origem física atribui o significado de expedição à UF. O mesmo conteúdo pode ser recebido de origens distintas.
- **FR-005** As representações e mensagens diagnósticas produzidas pela biblioteca DEVEM ocultar os textos e identificadores. A biblioteca DEVE preservar a autonomia do consumidor para mapear ausência, inconsistência e evidência documental.
- **FR-006** O identificador da CTPS Digital DEVE ser tratado pelo contrato de CPF existente. A conferência estrutural do perfil histórico não deve criar ou validar a habilitação digital, inferir emissão física ou gerar a projeção `7+4`.

### Key Entities *(include if feature involves data)*

- **Grupo CTPS legado** — Presença declarada e, quando informado, número, série e UF como textos de origem.
- **Proveniência** — Origem física, projeção de CPF ou indeterminada e indicação de perda informada pelo consumidor.
- **Resultado de conferência** — Desfecho estrutural por perfil, campos afetados e acesso explícito aos valores conferidos.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** Classificar os 11 fragmentos sintéticos em quatro grupos presentes conformes, um grupo ausente, um grupo incompleto e cinco grupos com campo fora dos limites ou da lista de UFs.
- **SC-002** Recuperar integralmente número e série em todos os grupos presentes conformes, inclusive os que contêm zeros, letras, pontuação ou espaços.
- **SC-003** Preservar todas as três origens declaradas para a mesma tripla e a indicação de perda, sem inferir a origem nem inserir zeros.
- **SC-004** Observar zero exposições de identificadores nos diagnósticos sintéticos da conferência.

## Assumptions

- O consumidor identifica a versão do leiaute, extrai os campos, declara sua origem ou a informa como indeterminada e sinaliza eventual perda no armazenamento de origem.
- A evidência técnica caracteriza as facetas do XSD v2.5 e o mapeamento legado. O edital da UEM caracteriza a aceitação institucional de carteira física ou digital na contratação. [Recorte revisado](../../docs/descoberta/ctps/recorte.md)
- Outros leiautes, DV da carteira física, reconhecimento de documentos, extração de XML/PDF, OCR, emissão, prova de vínculo e consultas a serviços cadastrais estão fora deste incremento.
