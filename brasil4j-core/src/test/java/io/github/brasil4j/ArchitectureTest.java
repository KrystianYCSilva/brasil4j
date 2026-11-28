package io.github.brasil4j;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.github.brasil4j.geografia.Uf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Package rules AR-4 to AR-9 from {@code .context/patterns/architecture.md}, checked on the production bytecode
 * with plain JUnit tests (no extra test engine).
 */
class ArchitectureTest {
    private static JavaClasses producao;

    @BeforeAll
    static void importarClassesDeProducao() {
        producao = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.brasil4j");
    }

    @Test
    void blocosCompartilhadosNaoDependemDeContextos() {
        noClasses().that().resideInAnyPackage("..verificacao..", "..mascara..")
                .should().dependOnClassesThat().resideInAnyPackage("..identidade..", "..geografia..", "..financas..")
                .because("AR-4: verificacao e mascara são blocos sem contexto")
                .check(producao);
    }

    @Test
    void identidadeNaoDependeDeOutrosContextos() {
        noClasses().that().resideInAPackage("..identidade..")
                .should().dependOnClassesThat().resideInAnyPackage("..geografia..", "..financas..")
                .because("AR-5: contextos não dependem uns dos outros")
                .check(producao);
    }

    @Test
    void geografiaNaoDependeDeOutrosPacotes() {
        noClasses().that().resideInAPackage("..geografia..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..identidade..", "..financas..", "..verificacao..", "..mascara..")
                .because("AR-5: contextos não dependem uns dos outros")
                .check(producao);
    }

    @Test
    void financasNaoDependemDeOutrosPacotes() {
        noClasses().that().resideInAPackage("..financas..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..identidade..", "..geografia..", "..verificacao..", "..mascara..")
                .because("AR-5: contextos não dependem uns dos outros")
                .check(producao);
    }

    @Test
    void documentosNaoSeConhecem() {
        slices().matching("io.github.brasil4j.identidade.(cpf|cnpj|nis)..")
                .should().notDependOnEachOther()
                .because("AR-6: só a composicao conhece todos os documentos")
                .check(producao);
    }

    @Test
    void semCiclosEntrePacotes() {
        slices().matching("io.github.brasil4j.(**)")
                .should().beFreeOfCycles()
                .because("AR-7: sem ciclos entre pacotes")
                .check(producao);
    }

    @Test
    void semLogNemReflexao() {
        noClasses()
                .should().accessClassesThat().resideInAPackage("java.util.logging..")
                .orShould().accessClassesThat().resideInAPackage("java.lang.reflect..")
                .orShould().accessField(System.class, "out")
                .orShould().accessField(System.class, "err")
                .because("AR-8: o núcleo não registra log nem usa reflexão")
                .check(producao);
    }

    @Test
    void ctpsHistoricaDependeSoDoJdkEDaUf() {
        classes().that().resideInAPackage("..trabalhista.ctps..")
                .should().onlyDependOnClassesThat(resideInAnyPackage("java..", "..trabalhista.ctps..")
                        .or(type(Uf.class)))
                .because("AR-9: CTPS histórica usa a tabela de UFs")
                .check(producao);
    }

    @Test
    void outrosPacotesNaoDependemDeCtpsHistorica() {
        noClasses().that().resideOutsideOfPackage("..trabalhista.ctps..")
                .should().dependOnClassesThat().resideInAPackage("..trabalhista.ctps..")
                .because("AR-9: o perfil histórico fica no pacote trabalhista")
                .check(producao);
    }
}
