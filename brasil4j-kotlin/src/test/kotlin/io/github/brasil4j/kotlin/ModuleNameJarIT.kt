package io.github.brasil4j.kotlin

import io.github.brasil4j.kotlin.identidade.Cpf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.util.jar.JarFile

/** JA-14: the packaged Kotlin JAR declares a stable JPMS module name. */
class ModuleNameJarIT {
    @Test
    fun `declara nome de modulo estavel`() {
        val jar = File(Cpf::class.java.protectionDomain.codeSource.location.toURI())
        JarFile(jar).use { arquivo ->
            assertEquals("io.github.brasil4j.kotlin", arquivo.manifest.mainAttributes.getValue("Automatic-Module-Name"))
        }
    }
}
