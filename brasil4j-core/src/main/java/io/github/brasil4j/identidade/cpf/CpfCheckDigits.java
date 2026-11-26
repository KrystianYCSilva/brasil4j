package io.github.brasil4j.identidade.cpf;

/** Implementa o cálculo da regra REGRA_VALIDA_CPF do manual da e-Financeira. */
final class CpfCheckDigits {
    private CpfCheckDigits() {
    }

    static boolean matches(String canonical) {
        return canonical.charAt(9) == '0' + calculate(canonical, 9)
                && canonical.charAt(10) == '0' + calculate(canonical, 10);
    }

    private static int calculate(String canonical, int length) {
        int sum = 0;
        int firstWeight = 10 - length;
        for (int index = 0; index < length; index++) {
            sum += (canonical.charAt(index) - '0') * (firstWeight + index);
        }
        int remainder = sum % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
