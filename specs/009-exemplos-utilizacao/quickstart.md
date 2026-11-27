# Quickstart · Spec 009 — Executando os exemplos (`brasil4j-exemplos`)

```powershell
# Executar apenas o módulo de exemplos (após compilar o reator)
.\mvnw.cmd test -pl brasil4j-core,brasil4j-kotlin,brasil4j-exemplos

# Executar todo o projeto (inclui automaticamente brasil4j-exemplos)
.\mvnw.cmd clean verify

# Conferir que o perfil de publicação Central exclui brasil4j-exemplos do reator
.\mvnw.cmd help:active-profiles -Pcentral
```
