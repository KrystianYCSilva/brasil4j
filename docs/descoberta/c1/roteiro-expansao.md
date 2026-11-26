# brasil4j · C1 e expansão

## Resultado do C1 em 2026-11-26

O recorte de [município IBGE e UF](../../../specs/005-municipio-ibge-uf/spec.md) tem pontos de uso no Obrigações Core e no Censo Superior/UEM. O desenho conserva sete dígitos ASCII, expõe prefixo e resolve/coincide UF de forma explícita. A [matriz NIS](nis-matriz.md) caracteriza os dois legados em 18 casos. O [inventário](inventario-expansao.csv) indexa as tabelas documentais dos dois anexos e os tipos de referência do roteiro. As linhas preservam paralelos e variações intencionalmente. A contagem é de registros de evidência, sem pretensão de contar documentos únicos.

**Proveniência dos anexos.** HTML `D95CB4E0B5046F7FB25A89E1A29AFEC78EB55F0EF7067C90D0094E92DBD4DE4A`. Markdown `F2EECCAC02A87DEEEFF3DF3BD3BA42F720F2BC56199F737765BE901988866FE4`. As colunas de situação e canal registram achados dos anexos para conferência. Campos candidatos, horizontes e checagens registram a triagem do projeto. Referências sem URL recuperável exigem consulta ao emissor. Uso concreto confirmado nesta rodada para IBGE. A lista de capitais, entidades e a cronologia dos anexos orientam jurisdição e vigência e seguem como índices de pesquisa.

## Sequência de entrega

| Onde | O que entra | Evidência de promoção |
| --- | --- | --- |
| C1 | IBGE/UF em Spec Kit. NIS e inventário em descoberta. | Pareceres independentes e aprovação do recorte antes do código. |
| Médio · Identidade | NIS/PIS/Pasep/NIT → RG/CIN → **passaporte comum** → CNH → título. CTPS física, RNM/CRNM e matrícula civil em specs próprias na sequência. | Modelo e leiaute determinados, caso consumidor e teste de compatibilidade histórica. Passaporte começa por número, emissão, autoridade e vencimento impressos. [PF · Normas](https://www.gov.br/pf/pt-br/assuntos/passaporte/legislacao/instrucao-normativa-173) |
| Médio · Referências | CEP, Endereço, Município e TipoLogradouro. Regiões, UF com capital e DDD, País e mapeamentos INEP/eSocial. Datas e moeda, calendário com caso de vencimento. | Definir edição e direitos de redistribuição por tabela. No CEP, estrutura offline e catálogo como decisões distintas. Para dinheiro, `BigDecimal` e política explícita após conferir a norma ABNT aplicável. [Correios](https://www.correios.com.br/enviar/precisa-de-ajuda/guia-de-enderecamento/guia-de-enderecamento) · [Anatel](https://www.anatel.gov.br/dadosabertos/PDA/Codigo_Nacional/Glossario_Metadados.pdf) · [eSocial · Tabela 06](https://www.gov.br/esocial/pt-br/documentacao-tecnica/leiautes-esocial-versao-1-3-nt-02-2024-rev-29-10-2024/tabelas.html) · [Catálogo ABNT](https://www.abntcatalogo.com.br/) |
| Longo | CNS, RENAVAM e documentos viários, documentos militares, conselhos e diplomas, CNO/CAEPF/CEI, NIRE, matrículas imóveis/rurais, CNAE, NCM, IE/IM. | Um caso por família, fonte oficial do campo estrutural e fronteira de autenticidade. Primeira IE/IM só com jurisdição e consumidor confirmados. NF e Pix permanecem adiados. |

## Contrato transversal

Construtor recebe identificador textual imutável. Em documento que representa exemplar, setters alteram o registro local. Campos derivados têm leitura apenas. Objetos editáveis têm identidade de instância e datas usam `LocalDate`. Preferir composição para emissor, local de votação e dados do exemplar. Usar interfaces quando houver variação real. Manter disponibilidade dos modelos históricos sem `@Deprecated`; JavaDoc/KDoc registram vigência, transição e exceções.

Validação padrão usa tamanho e formato sustentados pela fonte. DV técnico reproduzível sem regra oficial entra só como opção explícita após revisão. Distinguir validade de apresentação, DV, vigência, ocorrência em cadastro e autenticidade. Usar fontes públicas e exemplos sintéticos. Entrega com Spec Kit, TDD, JDK 8/21, documentação, provas pelos JARs e comparação de API a partir da v0.2.

## Alternativas e lacunas

| Alternativa | Evidência e decisão |
| --- | --- |
| Manter regras locais | Menor integração se só um consumidor precisa da regra. Comparar o mesmo fluxo e a tolerância à entrada. A evidência atual de IBGE favorece padronizar valor e prefixo, com adoção ainda a confirmar. |
| Declarações | `CodigoMunicipio` em `ca3b3d9` admite espaços externos. POM declara Java 11. Disponibilidade do artefato e licença aplicável pendentes. Usar como referência de comportamento nesta rodada. |
| Stella | `caelum-stella` 2.2.0 como linha de comparação para documentos, com Apache-2.0 no upstream. O relato de compatibilidade Java 8 na issue 316 deve ser reproduzido no JAR fixado por documento antes de recomendação de adoção. [Projeto Stella](https://github.com/caelum/caelum-stella) · [Issue de compatibilidade](https://github.com/caelum/caelum-stella/issues/316) |
| Jollyday e Tupinamba | Evidência de leitura e execução separadas no [C1 anterior](retomada.md). Antes da spec de calendário, confirmar política de um vencimento real e comparar o mesmo vetor, com fixação de versões. |

A publicação v0.1.0 segue a trilha da Spec 004. A descoberta não altera código dos serviços ou a versão publicada.
