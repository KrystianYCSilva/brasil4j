# Data Model: CPF

Cpf é um valor imutável identificado por 11 caracteres canônicos. A base tem nove caracteres e os DVs informados são os dois últimos. CpfValidationMode modela a decisão de conferir o algoritmo.

## Invariantes

- Nulo, vazio, Unicode, espaços e máscara parcial falham estruturalmente em ambos os modos.
- Um DV divergente passa somente na conferência de formato. O valor continua devolvendo o DV informado.
