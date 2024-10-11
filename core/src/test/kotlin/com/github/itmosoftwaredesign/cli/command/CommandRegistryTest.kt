package com.github.itmosoftwaredesign.cli.command

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandRegistryTest {


    private lateinit var commandRegistry: CommandRegistry
    private lateinit var mockCommand: Command

    @BeforeEach
    fun setUp() {
        mockCommand = mockk(relaxed = true)
        commandRegistry = CommandRegistry()
    }

    @Test
    fun `should initialize empty registry`() {
        assertNull(commandRegistry["nonexistent"])
    }

    @Test
    fun `should initialize registry with commands`() {
        val initialCommands = mapOf("cmd" to mockCommand)
        commandRegistry = CommandRegistry(initialCommands)

        assertEquals(mockCommand, commandRegistry["cmd"])
    }

    @Test
    fun `should register new command`() {
        commandRegistry.register("newCmd", mockCommand)

        assertEquals(mockCommand, commandRegistry["newCmd"])
    }

    @Test
    fun `should unregister command`() {
        commandRegistry.register("cmd", mockCommand)

        assertNotNull(commandRegistry["cmd"])

        commandRegistry.unregister("cmd")

        assertNull(commandRegistry["cmd"])
    }

    @Test
    fun `should return null for non-existing command`() {
        assertNull(commandRegistry["nonexistent"])
    }
}