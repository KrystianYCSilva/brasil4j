package io.github.brasil4j.financas;

/**
 * Validações e fábricas de {@link Moeda} isoladas em classe sem supertipos, garantindo um grafo
 * acíclico estrito de inicialização de classes ({@code ValidacaoMoeda -> Moeda -> MoedaPadrao})
 * sem possibilidade de deadlock entre múltiplas threads (JLS §12.4.2).
 */
final class ValidacaoMoeda {
    private static final int TAMANHO_MAXIMO_TEXTO = 16;
    private static final int CASAS_DECIMAIS_MAXIMAS = 18;
    private static final int CASAS_DECIMAIS_PADRAO = 2;

    private ValidacaoMoeda() {
    }

    static Moeda criarConstanteInicial(String codigo, String simbolo) {
        validarCodigoAsciiVisivel(codigo);
        validarSimbolo(simbolo);
        return new MoedaPadrao(codigo, CASAS_DECIMAIS_PADRAO, simbolo);
    }

    static Moeda criarIso(String codigo, int casasDecimais) {
        if (codigo == null || codigo.length() != 3) {
            throw new IllegalArgumentException(
                    "O código ISO 4217 deve conter exatamente 3 letras maiúsculas ASCII.");
        }
        for (int i = 0; i < 3; i++) {
            char c = codigo.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException(
                        "O código ISO 4217 deve conter exatamente 3 letras maiúsculas ASCII.");
            }
        }
        validarCasasDecimais(casasDecimais);
        Moeda padraoBrl = Moeda.BRL;
        if (casasDecimais == CASAS_DECIMAIS_PADRAO) {
            switch (codigo) {
                case "BRL":
                    return padraoBrl;
                case "USD":
                    return Moeda.USD;
                case "EUR":
                    return Moeda.EUR;
                default:
                    break;
            }
        }
        return new MoedaPadrao(codigo, casasDecimais, codigo);
    }

    static MoedaPadrao criarGeral(String codigo, int casasDecimais, String simbolo) {
        validarCodigoAsciiVisivel(codigo);
        validarCasasDecimais(casasDecimais);
        validarSimbolo(simbolo);
        @SuppressWarnings("unused")
        Moeda init = Moeda.BRL;
        return new MoedaPadrao(codigo, casasDecimais, simbolo);
    }

    static MoedaPadrao capturarSnapshot(Moeda moeda) {
        if (moeda == null) {
            throw new IllegalArgumentException("A moeda não pode ser nula.");
        }
        @SuppressWarnings("unused")
        Moeda init = Moeda.BRL;
        if (moeda instanceof MoedaPadrao) {
            return (MoedaPadrao) moeda;
        }
        String cod = moeda.getCodigo();
        int casas = moeda.getCasasDecimais();
        String simb = moeda.getSimbolo();
        return criarGeral(cod, casas, simb);
    }

    static MoedaPadrao normalizarMoedaCanonica(Moeda moeda) {
        MoedaPadrao snap = capturarSnapshot(moeda);
        if (snap.getCasasDecimais() == CASAS_DECIMAIS_PADRAO) {
            String cod = snap.getCodigo();
            if ("BRL".equals(cod)) {
                return (MoedaPadrao) Moeda.BRL;
            }
            if ("USD".equals(cod)) {
                return (MoedaPadrao) Moeda.USD;
            }
            if ("EUR".equals(cod)) {
                return (MoedaPadrao) Moeda.EUR;
            }
        }
        return snap;
    }

    static boolean mesmaMoeda(Moeda a, Moeda b) {
        if (a == null || b == null) {
            return false;
        }
        String codA = a.getCodigo();
        int casasA = a.getCasasDecimais();
        String codB = b.getCodigo();
        int casasB = b.getCasasDecimais();
        if (codigoAsciiVisivelInvalido(codA)
                || casasDecimaisInvalidas(casasA)
                || codigoAsciiVisivelInvalido(codB)
                || casasDecimaisInvalidas(casasB)) {
            return false;
        }
        return casasA == casasB && codA.equals(codB);
    }

    static void validarCasasDecimais(int casasDecimais) {
        if (casasDecimaisInvalidas(casasDecimais)) {
            throw new IllegalArgumentException("As casas decimais devem estar entre 0 e 18.");
        }
    }

    private static boolean casasDecimaisInvalidas(int casasDecimais) {
        return casasDecimais < 0 || casasDecimais > CASAS_DECIMAIS_MAXIMAS;
    }

    private static boolean codigoAsciiVisivelInvalido(String codigo) {
        if (codigo == null || codigo.isEmpty() || codigo.length() > TAMANHO_MAXIMO_TEXTO) {
            return true;
        }
        for (int i = 0; i < codigo.length(); i++) {
            char c = codigo.charAt(i);
            if (c < 0x21 || c > 0x7E) {
                return true;
            }
        }
        return false;
    }

    private static void validarCodigoAsciiVisivel(String codigo) {
        if (codigoAsciiVisivelInvalido(codigo)) {
            throw new IllegalArgumentException(
                    "O código da moeda deve conter entre 1 e 16 caracteres ASCII visíveis sem espaços.");
        }
    }

    private static void validarSimbolo(String simbolo) {
        if (simbolo == null || simbolo.isEmpty() || simbolo.length() > TAMANHO_MAXIMO_TEXTO) {
            throw new IllegalArgumentException("O símbolo da moeda deve conter entre 1 e 16 caracteres.");
        }
        for (int i = 0; i < simbolo.length(); ) {
            char ch = simbolo.charAt(i);
            if (Character.isSurrogate(ch)) {
                if (!Character.isHighSurrogate(ch)
                        || i + 1 >= simbolo.length()
                        || !Character.isLowSurrogate(simbolo.charAt(i + 1))) {
                    throw new IllegalArgumentException("O símbolo da moeda contém surrogate Unicode isolado inválido.");
                }
            }
            int cp = simbolo.codePointAt(i);
            int tipo = Character.getType(cp);
            if (Character.isISOControl(cp)
                    || tipo == Character.CONTROL
                    || tipo == Character.FORMAT
                    || tipo == Character.LINE_SEPARATOR
                    || tipo == Character.PARAGRAPH_SEPARATOR
                    || tipo == Character.SURROGATE
                    || tipo == Character.UNASSIGNED) {
                throw new IllegalArgumentException(
                        "O símbolo da moeda não pode conter caracteres de controle ou formatação invisível.");
            }
            i += Character.charCount(cp);
        }
    }
}
