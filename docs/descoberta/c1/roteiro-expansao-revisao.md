# brasil4j · Revisão do roteiro de expansão

**Data** 2026-11-26 · **Base** `8e94704` (`v0.2.0`) · **Estado** proposta para revisão · **Autor** Claude, a pedido de Krystian.

Revisa o [roteiro de expansão](roteiro-expansao.md) a partir dos mesmos anexos já indexados no [inventário](inventario-expansao.csv). Os hashes conferidos nesta rodada coincidem com os registrados no roteiro: HTML `D95CB4E0B5046F7FB25A89E1A29AFEC78EB55F0EF7067C90D0094E92DBD4DE4A`, Markdown `F2EECCAC02A87DEEEFF3DF3BD3BA42F720F2BC56199F737765BE901988866FE4`. A proposta reorganiza as ondas pelo que a biblioteca entrega melhor (formato e DV com fonte pública, em objetos de valor imutáveis e sem dependências de runtime), e não pela família documental.

**Ressalva de proveniência.** Os algoritmos citados abaixo são hipóteses de triagem. Cada um exige fonte oficial em [fontes.md](../../fontes.md), com autoridade, revisão e vetor, na fase de descoberta do item. Nenhum deles está confirmado por esta revisão.

## Camadas de escopo

Os anexos distinguem documento, cadastro, identificador e validador. A distinção vira filtro de escopo:

| Camada | Definição | Exemplos | Decisão |
| --- | --- | --- | --- |
| A · Valor estrutural | Formato e DV com algoritmo público | CPF, CNPJ, NIS, título de eleitor, CNH, RENAVAM, CNS, chave de acesso de DF-e | Núcleo da biblioteca |
| B · Tabela de referência | Dados embarcados com edição e licença | UF, município, CNAE, NCM, país | Admitida com edição versionada; tabelas extensas em artefato separado |
| C · Exemplar com atributos | Identificador acompanhado de emissor, UF e datas | CNH, CIN, passaporte, CTPS física | Somente composição imutável de valores |
| D · Verificação do emissor | Consulta online, QR assinado, VALIDAR/ITI, Sintegra | Situação cadastral, autenticidade | Fora do escopo: exige rede e autenticação e contraria o núcleo sem dependências |

**Critério de promoção.** Algoritmo público com fonte oficial, consumidor identificado com ponto de entrada e vetores oficiais ou sintéticos conferidos. É a régua aplicada à [Spec 006](../006-nis/recorte.md).

## Ajustes ao roteiro vigente

1. **Ordem da onda de identidade.** O roteiro propõe NIS → RG/CIN → passaporte → CNH → título. RG não tem regra nacional (27 regras estaduais) e passaporte tem apenas formato. Título e CNH têm DV, e o eSocial (S-2200) já demanda CNH, RG, RNE/RNM e conselho de classe para RH. Proposta: priorizar identificadores com DV e consumidor.
2. **Contrato transversal.** O roteiro prevê setters em documentos que representam exemplar e identidade de instância. Isso aproxima a biblioteca de entidades de cadastro, que a constituição e os requisitos deixam com os consumidores. Proposta: na camada C, apenas objetos de valor imutáveis, por exemplo `Cnh(numero, uf, validade)` com métodos `with…()` quando necessário.
3. **Chave de acesso de DF-e.** O inventário classifica NF-e, CT-e e MDF-e como adiados. A chave de 44 posições é camada A: `cUF`, `AAMM`, CNPJ, modelo, série, número, `tpEmis`, `cNF` e DV em módulo 11. Ela compõe `Uf` e `Cnpj`, já entregues, e atende o Obrigações Core. Proposta: manter o documento fiscal adiado e promover a chave.

## Ondas

| Onda | Tema e consumidor | Itens em ordem | Versão alvo |
| --- | --- | --- | --- |
| 1 · Trabalho e previdência | RH e UEM (eSocial, CVU) | NIS/PIS/Pasep/NIT ([Spec 006](../../../specs/006-nis-estrutural/spec.md)) → título de eleitor (sequencial, código de UF e dois DV; reaproveita `Uf`) → número de registro da CNH (11 dígitos, dois DV) → número e série da CTPS, só estrutural | 0.3.x |
| 2 · Fiscal e contábil | Obrigações Core | Chave de acesso de DF-e → CNO/CEI e CAEPF → CIB (antigo NIRF; alfanumérico segundo os anexos, desenho próximo ao CNPJ) → Inscrição Estadual, começando pelas UFs do consumidor | 0.4.x |
| 3 · Referências | Ambos os consumidores | CNAE, NCM, país (códigos Bacen/IBGE), CEP estrutural. Cada tabela exige edição, direito de redistribuição e cadência de atualização | 0.5.x, possivelmente em `brasil4j-tabelas` |
| 4 · Saúde, trânsito e registros | Sob demanda confirmada | CNS (definitivo e provisório) → RENAVAM → placa (modelo antigo e Mercosul, com conversão) → matrícula de certidão civil → CNM imobiliário, CCIR e CAR | 0.6+ |
| Fora | — | Verificação online e de QR (camada D), conselhos sem DV público (OAB, CRM), RG estadual genérico, documentos municipais (alvará, inscrição municipal, NFS-e local) | — |

A 1.0 é candidata após a onda 2, quando o contrato estrutural terá passado por identidade, trabalho e fiscal.

## Fases por item

Cada item tem spec própria e segue o funil do C1.

| Fase | Conteúdo | Portão |
| --- | --- | --- |
| F0 · Descoberta | `docs/descoberta/NNN-*` com fonte oficial do formato e do DV, matriz de legados e consumidor com ponto de entrada | Pareceres de Claude e Antigravity e aceite do mantenedor |
| F1 · Spec Kit | spec → clarify → plan → tasks → analyze | Análise sem inconsistência crítica |
| F2 · Núcleo Java | TDD com um teste por invariante; validar apresentação antes de normalizar; modo de DV explícito quando a fonte for operacional; representação textual mascarada para dado pessoal | Cobertura e DocLint do build |
| F3 · Kotlin | Value class que delega ao núcleo | KDoc e Dokka sem avisos |
| F4 · Prova | Testes pelos JARs em JDK 8 e 21, bytecode 52, japicmp contra a última tag | CI hospedado verde |
| F5 · Release | Versão minor, changelog e bundle da [Spec 004](../../../specs/004-publicacao-central/spec.md) | Tag após CI |

## Decisões de arquitetura antes da onda 2

- **Pacotes por domínio.** `identidade`, `trabalho`, `fiscal`, `geografia`, `saude`, `transito`.
- **Módulo 11 compartilhado.** A rotina se repete na maioria dos itens. Uma classe pública entra na API e na comparação japicmp. Opções: utilitário package-private por pacote ou pacote `internal` documentado e excluído da comparação.
- **Tabelas fora do núcleo.** NCM tem milhares de linhas e edição frequente. No núcleo, infla o JAR e acopla releases à edição da tabela.

## Pendências para revisão

- Confirmar fonte oficial de DV para título de eleitor, CNH, chave de acesso, CNO, CAEPF, CIB e CNS antes de cada F0.
- Confirmar consumidores: campos de documento do S-2200 no RH e chave de acesso no Obrigações Core.
- Decidir entre manter ou substituir o contrato transversal do roteiro vigente.
- Decidir o tratamento do módulo 11 compartilhado e da separação de tabelas.
