# Data Model: Fundação JVM

O reator e seus dois artefatos são unidades de entrega. A fundação não introduz valores de domínio.

## Invariantes

- Falha de ferramenta incompatível é detectada pelo Enforcer.
- A primeira fundação pode empacotar módulos vazios. A verificação de bytecode exige classes a partir das specs de domínio.
