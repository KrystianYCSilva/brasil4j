# Pesquisa · Spec 014

## D1 · Autoridade e limites do perfil histórico

- **Decisão** Usar `trabalhador/documentos/CTPS` de `evtAdmissao.xsd` no namespace `http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00`. O ZIP da NT 18 tem SHA-256 `64329D891A2D072BA4731724D03089B7D74809A9007AD52650628F1A175DBB19`. A NT 19 manteve os XSDs. [ZIP oficial](https://www.gov.br/esocial/pt-br/documentacao-tecnica/manuais/esquemas_xsd_v02-05-00_nt18.zip) · [NT 19/2020](https://www.gov.br/esocial/pt-br/noticias/publicada-nota-tecnica-no-19-2020-com-ajustes-diversos-nos-leiautes-do-esocial)
- **Motivo** O grupo é opcional. `TCtps` define a sequência obrigatória `nrCtps`, `serieCtps`, `ufCtps`. Os dois primeiros são `xs:string` com limites inclusivos de 2 a 11 e 1 a 5 caracteres e conservam espaços. A enumeração de UF coincide com `Uf`. [Recorte e sondagem](../../docs/descoberta/ctps/recorte.md#contraprova-do-xsd-oficial)
- **Alternativas avaliadas** Máscara geral e gramática só de dígitos excluiriam campos aceitos pelo XSD. `Uf.fromName` admitiria tolerâncias alheias ao perfil.

## D2 · Origem física, projeção histórica e identificação digital

- **Decisão** Exigir origem declarada em todo grupo presente. `PHYSICAL` permite interpretar como de expedição a UF de um resultado conforme. `CPF_PROJECTION` cobre a projeção nos campos legados e `UNDETERMINED` registra falta de evidência. A perda de informação é uma indicação independente. Para CTPS Digital, usar os modos da API de CPF. [Orientação de transição do eSocial](https://www.gov.br/esocial/pt-br/noticias/simplificacao-do-esocial-veja-como-preencher-o-grupo-ctps) · [MTE · CTPS Digital](https://www.gov.br/trabalho-e-emprego/pt-br/servicos/trabalhador/carteira-de-trabalho/perguntas-frequentes-carteira-de-trabalho-digital/)
- **Motivo** Na projeção histórica a UF podia vir da residência ou do estabelecimento. Os editais da UEM evidenciam o uso institucional e o mapeamento numérico de RH evidencia perda potencial de zeros. [Evidências da UEM](../../docs/descoberta/ctps/recorte.md#uso-documentado-na-uem)
- **Alternativas avaliadas** Inferir origem pela tripla confundiria projeção e carteira física. Recompor CPF ou preencher zeros reconstruiria dados sem evidência.

## D3 · Composição e resultado

- **Decisão** Expor o perfil como Strategy nomeada em `CtpsProfile`, a entrada estruturada como `CtpsGroup` e o resultado com desfecho, campos afetados e acesso explícito ao registro conforme. Reusar `Uf` e manter a proveniência no resultado de todo grupo presente. Kotlin delega aos mesmos tipos Java.
- **Motivo** Na 010, `DocumentParser<D>` recebe um texto e produz um valor de documento com forma canônica. A conferência histórica recebe três campos e metadados. Ausência legítima e incompletude pedem desfechos próprios. O desenho preserva a representação de rejeições esperadas como resultados. [Contratos da 010](../010-contratos-documentos/contracts/api.md)
- **Alternativas avaliadas** Concatenar campos exigiria uma gramática artificial. Um tipo digital duplicaria o CPF. Uma interface para perfis de terceiros exigiria decisões adicionais de extensão.

## D4 · Comprimento Unicode e verificação

- **Decisão** Contar pontos de código no texto entregue pelo consumidor. Para os testes, versionar fragmentos sintéticos e expectativas. Usar JAXP apenas para a extração e para uma comparação offline isolada dos 11 vetores principais com o XSD.
- **Motivo** A sondagem do `TCtps` confirmou a matriz principal. Nos JDKs 8 e 21, o provedor JAXP embutido contou unidades UTF-16 em casos suplementares. A norma usa caracteres XML. Os testes suplementares seguem a norma, com expectativas explícitas. [XML Schema Datatypes, §§4.3.2 e 4.3.3](https://www.w3.org/TR/xmlschema-2/#rf-minLength) · [XML 1.0, §2.2](https://www.w3.org/TR/REC-xml/#charsets)
- **Alternativas avaliadas** `String.length()` reproduziria a divergência do provedor. Baixar o XSD durante o build tornaria a suíte dependente de rede. Conferir só classes compiladas deixaria a fronteira de consumo dos JARs sem cobertura.
