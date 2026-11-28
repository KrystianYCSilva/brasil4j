package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Motor matemático imutável para apuração marginal por faixas progressivas parametrizadas pelo
 * consumidor (com ou sem teto contributivo), sem embutir tabelas normativas ou legais.
 *
 * @param <T> subtipo monetário da tabela
 */
public final class TabelaProgressiva<T extends Dinheiro> {
    private static final int MAXIMO_FAIXAS_LIMITADAS = 999;

    private final MoedaPadrao moeda;
    private final BiFunction<BigDecimal, MetodoArredondamento, T> construtorTipo;
    private final List<FaixaProgressiva<T>> faixas;

    private TabelaProgressiva(
            MoedaPadrao moeda,
            BiFunction<BigDecimal, MetodoArredondamento, T> construtorTipo,
            List<FaixaProgressiva<T>> faixas) {
        this.moeda = moeda;
        this.construtorTipo = construtorTipo;
        this.faixas = faixas;
    }

    /**
     * Inicia a construção de uma tabela progressiva tipada em {@link Real} ({@code BRL}).
     *
     * @return builder de uso único para {@link Real}
     */
    public static Builder<Real> emReais() {
        return new Builder<>((MoedaPadrao) Moeda.BRL, Real::new);
    }

    /**
     * Inicia a construção de uma tabela progressiva tipada em {@link Dolar} ({@code USD}).
     *
     * @return builder de uso único para {@link Dolar}
     */
    public static Builder<Dolar> emDolares() {
        return new Builder<>((MoedaPadrao) Moeda.USD, Dolar::new);
    }

    /**
     * Inicia a construção de uma tabela progressiva tipada em {@link Euro} ({@code EUR}).
     *
     * @return builder de uso único para {@link Euro}
     */
    public static Builder<Euro> emEuros() {
        return new Builder<>((MoedaPadrao) Moeda.EUR, Euro::new);
    }

    /**
     * Inicia a construção de uma tabela progressiva para qualquer {@link Moeda}, produzindo
     * instâncias canônicas ({@link Real}, {@link Dolar}, {@link Euro} ou {@link QuantiaMonetaria})
     * sob o supertipo {@link Dinheiro} e validando a moeda em tempo de execução.
     *
     * @param moeda moeda alvo não nula
     * @return builder de uso único para {@link Dinheiro}
     * @throws IllegalArgumentException se a moeda for nula ou inválida
     */
    public static Builder<Dinheiro> paraMoeda(Moeda moeda) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        return new Builder<>(m, (v, a) -> Dinheiro.instanciarCanonico(m, v, a));
    }

    /**
     * Retorna a moeda canônica desta tabela.
     *
     * @return moeda da tabela
     */
    public Moeda getMoeda() {
        return moeda;
    }

    /**
     * Retorna a lista imutável de faixas configuradas nesta tabela.
     *
     * @return faixas da tabela
     */
    public List<FaixaProgressiva<T>> getFaixas() {
        return faixas;
    }

    /**
     * Apura {@code baseCalculo} no modo {@link ModoApuracaoProgressiva#NO_TOTAL} usando a política
     * retida em {@code baseCalculo}.
     *
     * @param baseCalculo base não negativa ({@code >= 0}) na moeda desta tabela
     * @return resultado auditável da apuração
     * @throws IllegalArgumentException se a base for nula, negativa ou de moeda divergente
     */
    public ApuracaoProgressiva<T> apurar(T baseCalculo) {
        exigirBaseValida(baseCalculo);
        return apurar(baseCalculo, ModoApuracaoProgressiva.NO_TOTAL, baseCalculo.getArredondamento());
    }

    /**
     * Apura {@code baseCalculo} no {@code modo} informado usando a política retida em {@code baseCalculo}.
     *
     * @param baseCalculo base não negativa ({@code >= 0}) na moeda desta tabela
     * @param modo modo de arredondamento progressivo
     * @return resultado auditável da apuração
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public ApuracaoProgressiva<T> apurar(T baseCalculo, ModoApuracaoProgressiva modo) {
        exigirBaseValida(baseCalculo);
        return apurar(baseCalculo, modo, baseCalculo.getArredondamento());
    }

    /**
     * Apura {@code baseCalculo} aplicando o {@code modo} e a política {@code arredondamento} informados.
     *
     * @param baseCalculo base não negativa ({@code >= 0}) na moeda desta tabela
     * @param modo modo de arredondamento progressivo ({@code NO_TOTAL} ou {@code POR_FAIXA})
     * @param arredondamento política de arredondamento a aplicar e reter nos montantes resultantes
     * @return resultado auditável da apuração
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public ApuracaoProgressiva<T> apurar(
            T baseCalculo, ModoApuracaoProgressiva modo, MetodoArredondamento arredondamento) {
        exigirBaseValida(baseCalculo);
        if (modo == null) {
            throw new IllegalArgumentException("O modo de apuração progressiva não pode ser nulo.");
        }
        AritmeticaMonetaria.exigirArredondamento(arredondamento);

        int n = faixas.size();
        int casas = moeda.getCasasDecimais();
        BigDecimal zeroEscalado = BigDecimal.ZERO.setScale(casas, RoundingMode.UNNECESSARY);
        BigDecimal valorBase = baseCalculo.valor;

        List<T> basesNaFaixa = new ArrayList<>(n);
        BigDecimal[] valoresExatos = new BigDecimal[n];
        List<T> arredondadosIndividuais = new ArrayList<>(n);
        BigDecimal somaExata = BigDecimal.ZERO;

        for (int i = 0; i < n; i++) {
            FaixaProgressiva<T> f = faixas.get(i);
            BigDecimal limInf = f.getLimiteInferior().valor;
            BigDecimal fatiaDecimal;
            if (valorBase.compareTo(limInf) <= 0) {
                fatiaDecimal = zeroEscalado;
            } else if (f.getLimiteSuperior().isPresent()) {
                BigDecimal limSup = f.getLimiteSuperior().get().valor;
                BigDecimal tetoFatia = valorBase.compareTo(limSup) <= 0 ? valorBase : limSup;
                fatiaDecimal = tetoFatia.subtract(limInf);
            } else {
                fatiaDecimal = valorBase.subtract(limInf);
            }
            T baseFatia = construtorTipo.apply(fatiaDecimal, arredondamento);
            basesNaFaixa.add(baseFatia);

            BigDecimal prodExato =
                    AritmeticaMonetaria.normalizarDecimalPlano(fatiaDecimal.multiply(f.getAliquota().getFator()));
            valoresExatos[i] = prodExato;
            somaExata = somaExata.add(prodExato);

            arredondadosIndividuais.add(f.getAliquota().aplicarSobre(baseFatia, arredondamento));
        }

        BigDecimal valorExatoTotal = AritmeticaMonetaria.normalizarDecimalPlano(somaExata);
        T valorTotal;
        List<T> valoresApurados = new ArrayList<>(n);

        if (modo == ModoApuracaoProgressiva.POR_FAIXA) {
            BigDecimal acumulador = zeroEscalado;
            for (int i = 0; i < n; i++) {
                T ind = arredondadosIndividuais.get(i);
                valoresApurados.add(ind);
                acumulador = AritmeticaMonetaria.somarEscalado(acumulador, ind.valor, casas);
            }
            valorTotal = construtorTipo.apply(acumulador, arredondamento);
        } else {
            BigDecimal totalEscalado = AritmeticaMonetaria.aplicarArredondamentoIntermediario(
                    valorExatoTotal, casas, arredondamento);
            valorTotal = construtorTipo.apply(totalEscalado, arredondamento);
            BigDecimal[] reconciliados =
                    AritmeticaMonetaria.reconciliarFaixasMaioresRestos(totalEscalado, casas, valoresExatos);
            for (int i = 0; i < n; i++) {
                valoresApurados.add(construtorTipo.apply(reconciliados[i], arredondamento));
            }
        }

        List<FaixaApurada<T>> itensMemoria = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            itensMemoria.add(new FaixaApurada<>(
                    faixas.get(i),
                    basesNaFaixa.get(i),
                    valoresExatos[i],
                    arredondadosIndividuais.get(i),
                    valoresApurados.get(i)));
        }

        T baseComPolitica = construtorTipo.apply(valorBase, arredondamento);
        BigDecimal diferenca = valorTotal.valor.subtract(valorExatoTotal);

        return new ApuracaoProgressiva<>(
                baseComPolitica,
                modo,
                arredondamento,
                Collections.unmodifiableList(itensMemoria),
                valorExatoTotal,
                valorTotal,
                diferenca);
    }

    private void exigirBaseValida(T baseCalculo) {
        if (baseCalculo == null) {
            throw new IllegalArgumentException("A base de cálculo não pode ser nula.");
        }
        if (!moeda.equals(baseCalculo.moeda)) {
            throw new IllegalArgumentException(
                    "A moeda da base (" + baseCalculo.moeda + ") diverge da moeda da tabela (" + moeda + ").");
        }
        if (baseCalculo.isNegativo()) {
            throw new IllegalArgumentException("A base de cálculo da tabela progressiva não pode ser negativa.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TabelaProgressiva)) {
            return false;
        }
        TabelaProgressiva<?> outra = (TabelaProgressiva<?>) obj;
        return Objects.equals(moeda, outra.moeda) && Objects.equals(faixas, outra.faixas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moeda, faixas);
    }

    @Override
    public String toString() {
        return "TabelaProgressiva[moeda=" + moeda + ", faixas=" + faixas.size() + "]";
    }

    /**
     * Construtor mutável, não <i>thread-safe</i> e de uso único para {@link TabelaProgressiva}.
     *
     * @param <T> subtipo monetário da tabela
     */
    public static final class Builder<T extends Dinheiro> {
        private final MoedaPadrao moeda;
        private final BiFunction<BigDecimal, MetodoArredondamento, T> construtorTipo;
        private final List<FaixaProgressiva<T>> acumulador = new ArrayList<>();
        private T ultimoLimite;
        private BigDecimal impostoMaximoAcumuladoExato = BigDecimal.ZERO;
        private boolean encerrado;

        Builder(MoedaPadrao moeda, BiFunction<BigDecimal, MetodoArredondamento, T> construtorTipo) {
            this.moeda = moeda;
            this.construtorTipo = construtorTipo;
            this.ultimoLimite =
                    construtorTipo.apply(BigDecimal.ZERO.setScale(moeda.getCasasDecimais(), RoundingMode.UNNECESSARY), MetodoArredondamento.ABNT_NBR_5891);
        }

        /**
         * Adiciona uma faixa limitada {@code (limiteAnterior, limiteSuperior]} interpretando o texto
         * no modo estrito {@code exato} da moeda desta tabela.
         *
         * @param limiteSuperior texto decimal sem casas excedentes e estritamente maior que o limite anterior
         * @param aliquota alíquota não negativa ({@code >= 0%})
         * @return este builder
         * @throws IllegalStateException se o builder já tiver sido encerrado com {@link #faixaAcima(Percentual)}
         * @throws IllegalArgumentException se o limite ou a alíquota forem inválidos
         */
        public Builder<T> faixaAte(String limiteSuperior, Percentual aliquota) {
            exigirAberto();
            BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(limiteSuperior);
            BigDecimal escalado = AritmeticaMonetaria.escalarExato(parsed, moeda.getCasasDecimais());
            T lim = construtorTipo.apply(escalado, MetodoArredondamento.ABNT_NBR_5891);
            return faixaAte(lim, aliquota);
        }

        /**
         * Adiciona uma faixa limitada {@code (limiteAnterior, limiteSuperior]} na moeda desta tabela.
         *
         * @param limiteSuperior limite superior estritamente maior que o anterior (e {@code > 0} na 1ª faixa)
         * @param aliquota alíquota não negativa ({@code >= 0%})
         * @return este builder
         * @throws IllegalStateException se o builder já tiver sido encerrado com {@link #faixaAcima(Percentual)}
         * @throws IllegalArgumentException se o limite, a moeda, a ordem ou a alíquota forem inválidos
         */
        public Builder<T> faixaAte(T limiteSuperior, Percentual aliquota) {
            exigirAberto();
            if (acumulador.size() >= MAXIMO_FAIXAS_LIMITADAS) {
                throw new IllegalArgumentException("A tabela progressiva suporta no máximo 999 faixas limitadas.");
            }
            if (limiteSuperior == null) {
                throw new IllegalArgumentException("O limite superior da faixa não pode ser nulo.");
            }
            if (!moeda.equals(limiteSuperior.moeda)) {
                throw new IllegalArgumentException(
                        "A moeda do limite (" + limiteSuperior.moeda + ") diverge da moeda da tabela (" + moeda + ").");
            }
            if (limiteSuperior.valor.compareTo(ultimoLimite.valor) <= 0) {
                throw new IllegalArgumentException(
                        "Os limites superiores das faixas devem ser positivos e estritamente crescentes.");
            }
            validarAliquotaNaoNegativa(aliquota);

            T limCanonico = construtorTipo.apply(limiteSuperior.valor, MetodoArredondamento.ABNT_NBR_5891);
            BigDecimal parcelaDeduzir = AritmeticaMonetaria.normalizarDecimalPlano(
                    ultimoLimite.valor.multiply(aliquota.getFator()).subtract(impostoMaximoAcumuladoExato));

            acumulador.add(new FaixaProgressiva<>(
                    acumulador.size() + 1, ultimoLimite, limCanonico, aliquota, parcelaDeduzir));

            BigDecimal larguraFaixa = limCanonico.valor.subtract(ultimoLimite.valor);
            impostoMaximoAcumuladoExato =
                    impostoMaximoAcumuladoExato.add(larguraFaixa.multiply(aliquota.getFator()));
            ultimoLimite = limCanonico;
            return this;
        }

        /**
         * Define a faixa final aberta {@code (ultimoLimite, +infinito)}, sela este builder e devolve
         * a {@link TabelaProgressiva} imutável.
         *
         * @param aliquotaFinal alíquota não negativa ({@code >= 0%}; use {@link Percentual#ZERO} para tabelas com teto)
         * @return tabela progressiva imutável pronta para uso
         * @throws IllegalStateException se o builder já tiver sido encerrado
         * @throws IllegalArgumentException se {@code aliquotaFinal} for nula ou negativa
         */
        public TabelaProgressiva<T> faixaAcima(Percentual aliquotaFinal) {
            exigirAberto();
            validarAliquotaNaoNegativa(aliquotaFinal);
            encerrado = true;

            BigDecimal parcelaDeduzir = AritmeticaMonetaria.normalizarDecimalPlano(
                    ultimoLimite.valor.multiply(aliquotaFinal.getFator()).subtract(impostoMaximoAcumuladoExato));
            acumulador.add(new FaixaProgressiva<>(
                    acumulador.size() + 1, ultimoLimite, null, aliquotaFinal, parcelaDeduzir));

            return new TabelaProgressiva<>(
                    moeda, construtorTipo, Collections.unmodifiableList(new ArrayList<>(acumulador)));
        }

        private void exigirAberto() {
            if (encerrado) {
                throw new IllegalStateException(
                        "Este Builder de TabelaProgressiva já foi encerrado por faixaAcima.");
            }
        }

        private static void validarAliquotaNaoNegativa(Percentual aliquota) {
            if (aliquota == null || aliquota.isNegativo()) {
                throw new IllegalArgumentException(
                        "A alíquota de uma faixa progressiva deve ser não nula e não negativa (>= 0%).");
            }
        }
    }
}
