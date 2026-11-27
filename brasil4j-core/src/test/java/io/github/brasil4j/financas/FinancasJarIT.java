package io.github.brasil4j.financas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancasJarIT {

    @Test
    void consomeApiMonetariaPeloJarEmpacotado() {
        URL origem = Real.class.getProtectionDomain().getCodeSource().getLocation();
        assertNotNull(origem);
        assertTrue(origem.getPath().endsWith(".jar"), () -> "Esperado JAR empacotado, obtido: " + origem);

        Real total = Real.de("100.00", MetodoArredondamento.ABNT_NBR_5891);
        List<Real> parcelas = total.ratear(3);
        assertEquals(Arrays.asList(Real.de("33.34"), Real.de("33.33"), Real.de("33.33")), parcelas);

        TaxaCambio ptax = TaxaCambio.umDolarEmReais("5.4832");
        Dolar emDolar = Real.de("548.32", MetodoArredondamento.TRUNCAMENTO).paraDolar(ptax);
        assertEquals(Dolar.de("100.00"), emDolar);

        Moeda jpy = Moeda.iso("JPY", 0);
        Dinheiro ienes = Dinheiro.de("100", jpy);
        assertEquals(
                Arrays.asList(
                        QuantiaMonetaria.de("34", jpy),
                        QuantiaMonetaria.de("33", jpy),
                        QuantiaMonetaria.de("33", jpy)),
                ienes.ratear(3));
    }

    @Test
    void inicializacaoDeClassesIsoladaEConcorrenteSemDeadlockClinit() throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String cp = System.getProperty("java.class.path");

        for (String modo : new String[] {
            "MOEDA", "REAL_ZERO", "DINHEIRO_DE", "ARREDONDAMENTO", "TAXA_CAMBIO",
            "CONCORRENTE_MOEDA_ARRED_1", "CONCORRENTE_MOEDA_ARRED_2"
        }) {
            Process proc = new ProcessBuilder(
                            javaBin, "-cp", cp, ProbeInicializacaoLimpa.class.getName(), modo)
                    .redirectErrorStream(true)
                    .start();
            boolean terminou = proc.waitFor(15, TimeUnit.SECONDS);
            if (!terminou) {
                proc.destroyForcibly();
            }
            String saida = lerTudo(proc.getInputStream());
            assertTrue(terminou, () -> "Timeout/deadlock na inicialização de classes para modo " + modo + ": " + saida);
            assertEquals(0, proc.exitValue(), () -> "Falha em modo " + modo + ": " + saida);
        }
    }

    private static String lerTudo(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public static final class ProbeInicializacaoLimpa {
        public static void main(String[] args) throws Throwable {
            String modo = args[0];
            if ("MOEDA".equals(modo)) {
                if (!"R$".equals(Moeda.BRL.getSimbolo())) {
                    throw new AssertionError();
                }
            } else if ("REAL_ZERO".equals(modo)) {
                if (!"BRL 0.00".equals(Real.ZERO.toString())) {
                    throw new AssertionError();
                }
            } else if ("DINHEIRO_DE".equals(modo)) {
                if (!"BRL 10.24".equals(Dinheiro.de("10.245", Moeda.BRL).toString())) {
                    throw new AssertionError();
                }
            } else if ("ARREDONDAMENTO".equals(modo)) {
                if (!new BigDecimal("10.24")
                        .equals(MetodoArredondamento.ABNT_NBR_5891.arredondar(new BigDecimal("10.245"), 2))) {
                    throw new AssertionError();
                }
            } else if ("TAXA_CAMBIO".equals(modo)) {
                if (!"1 USD = 5 BRL".equals(TaxaCambio.umDolarEmReais("5.00").toString())) {
                    throw new AssertionError();
                }
            } else if (modo.startsWith("CONCORRENTE_")) {
                CyclicBarrier barreira = new CyclicBarrier(2);
                AtomicReference<Throwable> erro = new AtomicReference<>();
                Thread t1 = new Thread(() -> {
                    try {
                        barreira.await();
                        if (!"BRL".equals(Moeda.BRL.getCodigo())) {
                            throw new AssertionError();
                        }
                    } catch (Throwable t) {
                        erro.compareAndSet(null, t);
                    }
                });
                Thread t2 = new Thread(() -> {
                    try {
                        barreira.await();
                        BigDecimal r = MetodoArredondamento.FINANCEIRO.arredondar(new BigDecimal("1.005"), 2);
                        if (!new BigDecimal("1.01").equals(r)) {
                            throw new AssertionError();
                        }
                    } catch (Throwable t) {
                        erro.compareAndSet(null, t);
                    }
                });
                t1.start();
                t2.start();
                t1.join(10_000L);
                t2.join(10_000L);
                if (erro.get() != null) {
                    throw erro.get();
                }
            }
        }
    }
}
