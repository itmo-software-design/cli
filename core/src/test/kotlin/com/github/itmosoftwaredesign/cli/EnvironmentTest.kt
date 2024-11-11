package com.github.itmosoftwaredesign.cli

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.*

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
    fun `should get system variable - exitCode`() {
        val exitCode = UUID.randomUUID().hashCode()
        environment.lastExitCode = exitCode

        assertEquals("$exitCode", environment.getVariable("?"))
    }

    @Test
    fun `should get system variable - pid`() {
        val pid = UUID.randomUUID().mostSignificantBits
        mockkStatic(ProcessHandle::class)

        val processHandle = mockk<ProcessHandle>(relaxed = true)
        every { ProcessHandle.current() } returns processHandle
        every { processHandle.pid() } returns pid

        assertEquals("$pid", environment.getVariable("$"))
    }

    @Test
    fun `should get system variable - working directory`() {
        val workingDirectory = UUID.randomUUID().toString()
        environment.workingDirectory = Path.of(workingDirectory)

        assertEquals(workingDirectory, environment.getVariable("PWD"))
    }

    @Test
    fun `should remove environment variables with null value`() {
        environment.setVariable("key", "value")
        assertEquals("value", environment.getVariable("key"))

        environment.setVariable("key", null)

        assertTrue(environment.getVariableNames().isEmpty())
    }

    @Test
    fun `should get environment variable names`() {
        environment.setVariable("key", "value")

        assertEquals(setOf("key"), environment.getVariableNames())
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

    @Test
    fun `should set and get exit code`() {
        val exitCode = UUID.randomUUID().hashCode()
        environment.lastExitCode = exitCode

        assertEquals(exitCode, environment.lastExitCode)
    }

}
