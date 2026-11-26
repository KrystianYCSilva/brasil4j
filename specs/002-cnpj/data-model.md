# Data Model: CNPJ

Cnpj é um valor imutável definido pelo canônico de 14 caracteres. Base = raiz de oito e ordem de quatro. Os dois DVs são os caracteres finais. CnpjType representa a composição da base. CnpjValidationMode representa uma política de entrada.

## Invariantes

- Zeros à esquerda e DVs 00 são preservados. A sequência de 14 zeros passa apenas no modo de formato.
- DV com letra, espaços, minúsculas, Unicode, pontuação deslocada e máscara parcial são inválidos em todos os modos.
- O tipo descreve a presença de letras e independe do modo escolhido.
