package io.github.brasil4j;

import io.github.brasil4j.identidade.cpf.Cpf;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** JA-14: the packaged core JAR declares a stable JPMS module name. */
class ModuleNameJarIT {
    @Test
    void declaraNomeDeModuloEstavel() throws Exception {
        File jar = new File(Cpf.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        try (JarFile arquivo = new JarFile(jar)) {
            assertEquals("io.github.brasil4j.core",
                    arquivo.getManifest().getMainAttributes().getValue("Automatic-Module-Name"));
        }
    }
}
