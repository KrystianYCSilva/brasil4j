package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * Rotinas aritméticas, de rateio exato e de validação compartilhadas no pacote {@code financas}.
 */
final class AritmeticaMonetaria {
    static final int TAMANHO_MAXIMO_STRING = 64;
    static final int PRECISAO_MAXIMA = 38;
    static final int ESCALA_MINIMA = -18;
    static final int ESCALA_MAXIMA = 38;
    static final int MAXIMO_ITENS_DISTRIBUICAO = 10_000;

    private static final int PRECISAO_INTERMEDIARIA_MAXIMA = 180;
    private static final int ESCALA_INTERMEDIARIA_MINIMA = -60;
    private static final int ESCALA_INTERMEDIARIA_MAXIMA = 180;
    private static final int CASAS_EXPANSAO_DIVISAO_CUSTOM = 34;
    private static final BigInteger DEZ_ELEVADO_34 = BigInteger.TEN.pow(CASAS_EXPANSAO_DIVISAO_CUSTOM);

    private AritmeticaMonetaria() {
    }

    static MoedaPadrao capturarSnapshot(Moeda moeda) {
        return ValidacaoMoeda.capturarSnapshot(moeda);
    }

    static MetodoArredondamento exigirArredondamento(MetodoArredondamento arredondamento) {
        if (arredondamento == null) {
            throw new IllegalArgumentException("A política de arredondamento não pode ser nula.");
        }
        return arredondamento;
    }

    static void exigirBigDecimalPadrao(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("O valor decimal não pode ser nulo.");
        }
        if (valor.getClass() != BigDecimal.class) {
            throw new IllegalArgumentException(
                    "Subclasses customizadas de BigDecimal não são aceitas; forneça java.math.BigDecimal padrão.");
        }
    }

    static BigDecimal parseDecimalCanonico(String texto) {
        if (texto == null || texto.isEmpty() || texto.length() > TAMANHO_MAXIMO_STRING) {
            throw new IllegalArgumentException("Representação textual monetária inválida.");
        }
        int inicio = 0;
        char primeiro = texto.charAt(0);
        if (primeiro == '+' || primeiro == '-') {
            inicio = 1;
            if (texto.length() == 1) {
                throw new IllegalArgumentException("Representação textual monetária inválida.");
            }
        }
        int indicePonto = -1;
        int digitosAntes = 0;
        int digitosDepois = 0;
        for (int i = inicio; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '.') {
                if (indicePonto >= 0) {
                    throw new IllegalArgumentException("Representação textual monetária inválida.");
                }
                indicePonto = i;
            } else if (c >= '0' && c <= '9') {
                if (indicePonto < 0) {
                    digitosAntes++;
                } else {
                    digitosDepois++;
                }
            } else {
                throw new IllegalArgumentException("Representação textual monetária inválida.");
            }
        }
        if (digitosAntes == 0 || (indicePonto >= 0 && digitosDepois == 0)) {
            throw new IllegalArgumentException("Representação textual monetária inválida.");
        }
        BigDecimal parsed = new BigDecimal(texto);
        validarLimitesDecimal(parsed);
        return parsed;
    }

    static void validarLimitesDecimal(BigDecimal valor) {
        exigirBigDecimalPadrao(valor);
        if (valor.precision() > PRECISAO_MAXIMA
                || valor.scale() < ESCALA_MINIMA
                || valor.scale() > ESCALA_MAXIMA) {
            throw new IllegalArgumentException(
                    "O valor decimal excede os limites suportados de precisão (38) ou escala (-18..38).");
        }
    }

    static void validarLimitesIntermediariosArredondamento(BigDecimal valor) {
        exigirBigDecimalPadrao(valor);
        if (valor.precision() > PRECISAO_INTERMEDIARIA_MAXIMA
                || valor.scale() < ESCALA_INTERMEDIARIA_MINIMA
                || valor.scale() > ESCALA_INTERMEDIARIA_MAXIMA) {
            throw new IllegalArgumentException(
                    "O valor decimal excede os limites suportados de arredondamento.");
        }
    }

    static void validarResultadoMonetario(BigDecimal valor) {
        if (valor.precision() > PRECISAO_MAXIMA) {
            throw new IllegalArgumentException("O resultado monetário excede a precisão máxima suportada (38 dígitos).");
        }
    }

    static BigDecimal normalizarZero(BigDecimal escalado, int casasDecimais) {
        if (escalado.signum() == 0) {
            return BigDecimal.ZERO.setScale(casasDecimais);
        }
        return escalado;
    }

    static BigDecimal aplicarArredondamentoSeguro(
            BigDecimal valor, int casasDecimais, MetodoArredondamento arredondamento) {
        validarLimitesDecimal(valor);
        ValidacaoMoeda.validarCasasDecimais(casasDecimais);
        exigirArredondamento(arredondamento);

        if (arredondamento instanceof ModoArredondamentoPadrao) {
            BigDecimal res = valor.setScale(casasDecimais, ((ModoArredondamentoPadrao) arredondamento).roundingMode());
            validarResultadoMonetario(res);
            return normalizarZero(res, casasDecimais);
        }
        return aplicarPoliticaCustomizadaValidandoVizinhos(valor, casasDecimais, arredondamento);
    }

    static BigDecimal aplicarPoliticaCustomizadaValidandoVizinhos(
            BigDecimal valorIntermediario, int casasDecimais, MetodoArredondamento customizado) {
        BigDecimal floor = normalizarZero(valorIntermediario.setScale(casasDecimais, RoundingMode.FLOOR), casasDecimais);
        BigDecimal ceiling =
                normalizarZero(valorIntermediario.setScale(casasDecimais, RoundingMode.CEILING), casasDecimais);

        BigDecimal candidato = customizado.arredondar(valorIntermediario, casasDecimais);
        exigirBigDecimalPadrao(candidato);
        if (candidato.scale() != casasDecimais) {
            throw new IllegalArgumentException(
                    "A política de arredondamento customizada deve retornar valor na escala da moeda.");
        }
        BigDecimal normalizado = normalizarZero(candidato, casasDecimais);
        if (!floor.equals(normalizado) && !ceiling.equals(normalizado)) {
            throw new IllegalArgumentException(
                    "A política de arredondamento customizada deve retornar um dos vizinhos imediatos (FLOOR ou CEILING).");
        }
        validarResultadoMonetario(normalizado);
        return normalizado;
    }

    static BigDecimal escalarExato(BigDecimal valor, int casasDecimais) {
        validarLimitesDecimal(valor);
        ValidacaoMoeda.validarCasasDecimais(casasDecimais);
        if (valor.scale() > casasDecimais) {
            throw new IllegalArgumentException(
                    "A entrada possui mais casas decimais que o permitido pela moeda no modo exato.");
        }
        BigDecimal escalado = valor.setScale(casasDecimais);
        validarResultadoMonetario(escalado);
        return normalizarZero(escalado, casasDecimais);
    }

    static BigDecimal deInteiroEscalado(long unidadesPrincipais, int casasDecimais) {
        ValidacaoMoeda.validarCasasDecimais(casasDecimais);
        BigDecimal res = BigDecimal.valueOf(unidadesPrincipais).setScale(casasDecimais);
        validarResultadoMonetario(res);
        return normalizarZero(res, casasDecimais);
    }

    static BigDecimal deUnidadesMinimasEscalado(long unidadesMinimas, int casasDecimais) {
        ValidacaoMoeda.validarCasasDecimais(casasDecimais);
        BigDecimal res = BigDecimal.valueOf(unidadesMinimas, casasDecimais);
        validarResultadoMonetario(res);
        return normalizarZero(res, casasDecimais);
    }

    static BigDecimal somarEscalado(BigDecimal a, BigDecimal b, int casasDecimais) {
        BigDecimal soma = a.add(b);
        validarResultadoMonetario(soma);
        return normalizarZero(soma, casasDecimais);
    }

    static BigDecimal subtrairEscalado(BigDecimal a, BigDecimal b, int casasDecimais) {
        BigDecimal dif = a.subtract(b);
        validarResultadoMonetario(dif);
        return normalizarZero(dif, casasDecimais);
    }

    static BigDecimal multiplicarPorLong(BigDecimal valor, long fator, int casasDecimais) {
        BigDecimal prod = valor.multiply(BigDecimal.valueOf(fator)).setScale(casasDecimais);
        validarResultadoMonetario(prod);
        return normalizarZero(prod, casasDecimais);
    }

    static BigDecimal multiplicarPorDecimal(
            BigDecimal valor, BigDecimal fator, int casasDecimais, MetodoArredondamento arredondamento) {
        validarLimitesDecimal(fator);
        exigirArredondamento(arredondamento);
        BigDecimal prodExato = valor.multiply(fator);
        if (arredondamento instanceof ModoArredondamentoPadrao) {
            BigDecimal res = prodExato.setScale(casasDecimais, ((ModoArredondamentoPadrao) arredondamento).roundingMode());
            validarResultadoMonetario(res);
            return normalizarZero(res, casasDecimais);
        }
        return aplicarPoliticaCustomizadaValidandoVizinhos(prodExato, casasDecimais, arredondamento);
    }

    static BigDecimal dividirPorLong(
            BigDecimal valor, long divisor, int casasDecimais, MetodoArredondamento arredondamento) {
        if (divisor == 0L) {
            throw new IllegalArgumentException("O divisor não pode ser zero.");
        }
        return dividirExato(valor, BigDecimal.valueOf(divisor), casasDecimais, arredondamento);
    }

    static BigDecimal dividirExato(
            BigDecimal numerador, BigDecimal denominador, int casasDestino, MetodoArredondamento arredondamento) {
        exigirArredondamento(arredondamento);
        if (denominador == null || denominador.signum() == 0) {
            throw new IllegalArgumentException("O divisor não pode ser nulo ou zero.");
        }
        if (arredondamento instanceof ModoArredondamentoPadrao) {
            BigDecimal quociente =
                    numerador.divide(denominador, casasDestino, ((ModoArredondamentoPadrao) arredondamento).roundingMode());
            validarResultadoMonetario(quociente);
            return normalizarZero(quociente, casasDestino);
        }

        BigDecimal intermediario;
        try {
            intermediario = numerador.divide(denominador);
        } catch (ArithmeticException ex) {
            intermediario = aproximarDivisaoNaoTerminante(numerador, denominador, casasDestino);
        }
        return aplicarPoliticaCustomizadaValidandoVizinhos(intermediario, casasDestino, arredondamento);
    }

    private static BigDecimal aproximarDivisaoNaoTerminante(
            BigDecimal numerador, BigDecimal denominador, int casasDestino) {
        BigDecimal escaladoAlvo = numerador.movePointRight(casasDestino);
        int escalaComum = Math.max(0, Math.max(escaladoAlvo.scale(), denominador.scale()));
        BigInteger numInt = escaladoAlvo.setScale(escalaComum).unscaledValue();
        BigInteger denInt = denominador.setScale(escalaComum).unscaledValue();

        BigInteger quociente = numInt.multiply(DEZ_ELEVADO_34).divide(denInt);
        long sinal = ((numInt.signum() > 0) == (denInt.signum() > 0)) ? 1L : -1L;
        BigInteger comSticky = quociente.multiply(BigInteger.TEN).add(BigInteger.valueOf(sinal));
        return new BigDecimal(comSticky, casasDestino + CASAS_EXPANSAO_DIVISAO_CUSTOM + 1);
    }

    static BigDecimal[] ratearEscalado(
            BigDecimal valor, int casasDecimais, int parcelas, PoliticaResiduo politica) {
        if (parcelas < 1 || parcelas > MAXIMO_ITENS_DISTRIBUICAO) {
            throw new IllegalArgumentException(
                    "A quantidade de parcelas deve estar entre 1 e 10.000.");
        }
        if (politica == null) {
            throw new IllegalArgumentException("A política de resíduo não pode ser nula.");
        }
        BigInteger total = valor.unscaledValue();
        BigInteger n = BigInteger.valueOf(parcelas);
        BigInteger[] qr = total.divideAndRemainder(n);
        BigInteger quociente = qr[0];
        int restoAbs = qr[1].abs().intValue();
        BigInteger ajuste = BigInteger.valueOf(total.signum());

        BigDecimal[] resultado = new BigDecimal[parcelas];
        for (int i = 0; i < parcelas; i++) {
            boolean recebeResiduo = (politica == PoliticaResiduo.PRIMEIRAS_PARCELAS)
                    ? (i < restoAbs)
                    : (i >= parcelas - restoAbs);
            BigInteger cota = recebeResiduo ? quociente.add(ajuste) : quociente;
            resultado[i] = normalizarZero(new BigDecimal(cota, casasDecimais), casasDecimais);
        }
        return resultado;
    }

    static BigDecimal[] alocarHamiltonEscalado(
            BigDecimal valor, int casasDecimais, List<BigDecimal> pesos) {
        if (pesos == null) {
            throw new IllegalArgumentException("A lista de pesos não pode ser nula.");
        }
        Object[] bruto = pesos.toArray();
        if (bruto.length == 0 || bruto.length > MAXIMO_ITENS_DISTRIBUICAO) {
            throw new IllegalArgumentException(
                    "A lista de pesos deve conter entre 1 e 10.000 elementos.");
        }
        int k = bruto.length;
        BigDecimal[] copiaPesos = new BigDecimal[k];
        int escalaMax = 0;
        for (int i = 0; i < k; i++) {
            Object item = bruto[i];
            if (!(item instanceof BigDecimal)) {
                throw new IllegalArgumentException("O peso de alocação não pode ser nulo.");
            }
            BigDecimal p = (BigDecimal) item;
            validarLimitesDecimal(p);
            if (p.signum() < 0) {
                throw new IllegalArgumentException("Os pesos de alocação não podem ser negativos.");
            }
            copiaPesos[i] = p;
            if (p.scale() > escalaMax) {
                escalaMax = p.scale();
            }
        }

        BigInteger[] pesosInt = new BigInteger[k];
        BigInteger somaPesos = BigInteger.ZERO;
        for (int i = 0; i < k; i++) {
            pesosInt[i] = copiaPesos[i].setScale(escalaMax).unscaledValue();
            somaPesos = somaPesos.add(pesosInt[i]);
        }
        if (somaPesos.signum() <= 0) {
            throw new IllegalArgumentException("A soma dos pesos de alocação deve ser estritamente positiva.");
        }

        int sinal = valor.signum();
        BigDecimal[] resultado = new BigDecimal[k];
        if (sinal == 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(casasDecimais);
            Arrays.fill(resultado, zero);
            return resultado;
        }

        BigInteger totalAbs = valor.unscaledValue().abs();
        BigInteger[] cotas = new BigInteger[k];
        BigInteger[] restos = new BigInteger[k];
        BigInteger somaCotas = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger[] qr = totalAbs.multiply(pesosInt[i]).divideAndRemainder(somaPesos);
            cotas[i] = qr[0];
            restos[i] = qr[1];
            somaCotas = somaCotas.add(qr[0]);
        }

        int faltantes = totalAbs.subtract(somaCotas).intValueExact();
        if (faltantes > 0) {
            Integer[] indices = new Integer[k];
            for (int i = 0; i < k; i++) {
                indices[i] = i;
            }
            Arrays.sort(indices, (a, b) -> {
                int cmp = restos[b].compareTo(restos[a]);
                return cmp != 0 ? cmp : a.compareTo(b);
            });
            for (int r = 0; r < faltantes; r++) {
                int idx = indices[r];
                cotas[idx] = cotas[idx].add(BigInteger.ONE);
            }
        }

        for (int i = 0; i < k; i++) {
            BigInteger cotaComSinal = sinal < 0 ? cotas[i].negate() : cotas[i];
            resultado[i] = normalizarZero(new BigDecimal(cotaComSinal, casasDecimais), casasDecimais);
        }
        return resultado;
    }
}
