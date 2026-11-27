# Data Model · Spec 009 — Módulo vitrine `brasil4j-exemplos`

Não introduz novas entidades ou tipos de produção na API pública de `brasil4j-core` ou `brasil4j-kotlin`.

| Componente | Caminho | Restrição Canônica |
| --- | --- | --- |
| `ModuloExemplos.producao` | `brasil4j-exemplos/src/main` | Inexistente (zero classes de produção). |
| `ModuloExemplos.publicacao` | `brasil4j-exemplos/pom.xml` e `pom.xml` | Excluído do perfil `central` e marcado com `maven.deploy.skip=true`, `gpg.skip=true` e `maven.source.skip=true`. |
| `ModuloExemplos.vetores` | `brasil4j-exemplos/src/test/**` | Exclusivamente dados sintéticos ou códigos públicos oficiais (ex.: IBGE `3550308`, `4115200`), sem dados pessoais reais. |
