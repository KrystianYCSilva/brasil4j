# Implementation Plan: Código IBGE de município e UF

**Branch**: `005-municipio-ibge-uf` (identificador da spec) · **Date**: 2026-11-26 · **Spec**: [spec.md](spec.md)

## Summary

Criar um valor estrutural Java e uma adaptação idiomática Kotlin. Reutilizar o reator da biblioteca. Os serviços preservam suas bordas e semânticas de cadastro e consulta.

## Technical Context

- Java 8, Kotlin com bytecode JVM 1.8. Maven Wrapper, suites JUnit e Failsafe pelos JARs.
- Dois artefatos existentes. Núcleo sem dependências de runtime. Sem armazenamento ou rede.
- Tabela fixa de 27 UFs, com geografia do IBGE e DDDs da Anatel/MCom. Busca por nome completo normalizado no enum, preservando o nome oficial na saída. Checagem da entrada e comparação com custo constante para o domínio.

## Constitution Check

- SDD. Contrato, tarefas e análise nesta spec. Aprovação do recorte antes de iniciar código.
- DDD. O contexto Geografia atende aos usos de código municipal e UF [identificados no C1](research.md). O [contrato](contracts/api.md) define o valor estrutural e a projeção da UF. Existência e cadastro ficam com o consumidor.
- TDD. Registrar red, green e refactor por comportamento e provar consumo real pelos JARs.
- Plataforma. Verificar JDK 8 e 21, bytecode 52, JaCoCo ≥90% linhas e ≥85% branches, DocLint e Dokka. Aplicar japicmp à v0.2.0, comparando ambos os JARs com os artefatos verificados da v0.1.0.
- Proveniência. Acrescentar a fonte e os vetores a `docs/fontes.md` no incremento de código. Usar casos públicos e sintéticos.

## Project Structure

`brasil4j-core` em `io.github.brasil4j.geografia` para `CodigoMunicipio` e `Uf`. `brasil4j-kotlin` em `io.github.brasil4j.kotlin.geografia` para a value class, reutilizando a UF Java. Testes unitários e de consumo seguem Surefire/Failsafe de cada módulo.

## Phase 0 — Research

Comparação de consumidores e alternativas em [research.md](research.md).

## Phase 1 — Design & Contracts

[Modelo](data-model.md), [contrato público](contracts/api.md) e [quickstart](quickstart.md). O desenho preserva as fronteiras da constituição. A geração de evidência executável fica nas tarefas futuras.

## Phase 2 — Tasks

[Tarefas em ordem de execução](tasks.md). A versão-alvo é v0.2.0. O incremento tem adoção a avaliar separadamente em Core e Censo. A T001 registra aprovação, revisão de partida e ativação da spec antes do código. A T008 propaga o contrato à documentação junto da implementação. A publicação da v0.1.0 segue pela Spec 004.
