# Feature Specification: Bundle para Maven Central

**Feature Branch**: `004-publicacao-central` (identificador lógico)
**Created**: 2026-11-26
**Status**: Approved
**Input**: Preparar e validar localmente a v0.1.0 sob `io.github.brasil4j`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gerar um bundle verificável (Priority: P1)

Como mantenedor, quero gerar um ZIP de publicação em JDK 21 e inspecioná-lo localmente.

**Why this priority**: A inspeção encontra arquivos ausentes e assinaturas inválidas antes de qualquer envio.

**Independent Test**: Executar o perfil e verificar o conteúdo e as assinaturas do ZIP.

**Acceptance Scenarios**:

1. Dado o reator 0.1.0 e uma chave de teste, quando Maven executa `deploy -Pcentral`, então gera um bundle local com o pai e os dois módulos.
2. Dado o bundle, quando inspecionado, então inclui POMs e JARs assinados, checksums válidos, fontes e documentação reais, e bytecode 52.
3. Dado o build fora do perfil, quando executado em JDK 8, então a publicação e a assinatura ficam inativas.

### User Story 2 - Validar o namespace (Priority: P2)

Como publicador, quero saber qual prova o Central exige para manter `io.github.brasil4j`.

**Why this priority**: O nome do repositório GitHub está sob uma conta com identificador diferente.

**Independent Test**: Seguir o [guia](quickstart.md#namespace) até o Portal indicar a modalidade da prova.

**Acceptance Scenarios**:

1. Dado um pedido para o namespace exato, quando o Portal fornecer a chave de verificação por hospedagem, então o mantenedor sabe em qual proprietário GitHub criar a prova.
2. Dado que o Portal não oferece essa modalidade ou que o identificador não é controlado pelo projeto, então o guia aponta para o suporte da Sonatype.

### Edge Cases

- Perfil com JDK 8 ou sem chave deve falhar antes de gerar o bundle.
- Dados de teste e arquivos locais de IA ficam fora dos classificados.
- O checkout atual conserva a referência `v0.1.0`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** — Manter o GAV e incluir no ZIP o POM pai e os dois módulos, com seus metadados de projeto, licença, mantenedor e SCM.
- **FR-002** — Anexar `sources` e `javadoc` reais para Java e Kotlin antes da assinatura. Executar o perfil em JDK 21 e manter Java 8 como runtime mínimo.
- **FR-003** — Assinar POMs e JARs com GPG por chave em variável de ambiente. Criar e conferir checksums e assinaturas no ZIP.
- **FR-004** — Gerar o bundle local com envio desabilitado e preservar a tag atual.
- **FR-005** — Documentar como solicitar e verificar o namespace exato no Portal.

### Key Entities

- **Bundle** — Repositório Maven compactado com três POMs e seis JARs, suas assinaturas e checksums.
- **Namespace** — Permissão de publicação para `io.github.brasil4j`, verificada pelo Central Portal.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** — JDK 21 produz exatamente os nove arquivos Maven esperados. Todos têm assinaturas e checksums válidos.
- **SC-002** — O build regular em JDK 8 passa e a tag `v0.1.0` conserva o mesmo commit.
- **SC-003** — O guia identifica a prova por hospedagem e a condição de aceite `Verified`.

## Assumptions

- A inspeção local usa uma chave efêmera. A publicação exige chave do mantenedor e namespace verificado.
- O futuro bundle de publicação deve ser rastreável por `v0.1.0-central`, uma referência separada.
