package com.github.itmosoftwaredesign.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

class EnvironmentTest {

    private lateinit var environment: Environment

    @BeforeEach
    fun setUp() {
        environment = Environment()
    }

    @Test
    fun `should set and get environment variables`() {
        environment.setVariable("key", "value")

        assertEquals("value", environment.getVariable("key"))
    }

    @Test
    fun `should return null for nonexistent variable`() {
        assertNull(environment.getVariable("nonexistent"))
    }

    @Test
    fun `should change and retrieve working directory`() {
        val path = Path.of("/new/directory")
        environment.workingDirectory = path

        assertEquals(path, environment.workingDirectory)
    }

    @Test
    fun `should have empty path as default working directory`() {
        assertEquals(Path.of(""), environment.workingDirectory)
    }
}
