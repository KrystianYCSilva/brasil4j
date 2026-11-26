package io.github.brasil4j.identidade.cnpj;

/**
 * Implementa o módulo 11 do Anexo XV da IN RFB 2.119/2022.
 *
 * <p>A apresentação já foi restrita a ASCII quando o cálculo começa. Em Java,
 * subtrair {@code '0'} subtrai 48 do código do caractere. Assim, {@code '9'}
 * vale 9, {@code 'A'} vale 17 e {@code 'Z'} vale 42.
 *
 * <p>Os pesos vão de 2 a 9 da direita para a esquerda e reiniciam em 2.
 * O primeiro DV considera os 12 caracteres da base. O segundo considera a
 * base e o primeiro DV, que já foi conferido antes da segunda etapa.
 *
 * @see <a href="https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj">Manual de cálculo do DV da Receita Federal</a>
 */
final class CnpjCheckDigits {
    private CnpjCheckDigits() {
    }

    static boolean matches(String canonical) {
        if ("00000000000000".equals(canonical)) {
            return false;
        }
        int first = calculate(canonical, 12);
        return canonical.charAt(12) == '0' + first
                && canonical.charAt(13) == '0' + calculate(canonical, 13);
    }

    private static int calculate(String canonical, int length) {
        int sum = 0;
        for (int offset = 0; offset < length; offset++) {
            int value = canonical.charAt(length - 1 - offset) - '0';
            sum += value * (2 + offset % 8);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
