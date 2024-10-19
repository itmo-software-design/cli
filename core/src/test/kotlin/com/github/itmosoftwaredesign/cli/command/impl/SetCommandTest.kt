package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.*


class SetCommandTest {
    private lateinit var environment: Environment
    private lateinit var setCommand: SetCommand
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        setCommand = SetCommand()
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should set variable when one argument provided`() {
        val variableName = UUID.randomUUID().toString()
        val arguments = listOf(variableName)

        val result = setCommand.execute(environment, System.`in`, outputStream, errorStream, arguments)

        verify { environment.setVariable(variableName, null) }
        assertEquals(0, result.exitCode)
    }

    @Test
    fun `should set variable with value when multiple arguments provided`() {
        val variableName = UUID.randomUUID().toString()
        val variableValue = "value1 value2"
        val arguments = listOf(variableName, "value1", "value2")

        val result = setCommand.execute(environment, System.`in`, outputStream, errorStream, arguments)

        verify { environment.setVariable(variableName, variableValue) }
        assertEquals(0, result.exitCode)
    }

    @Test
    fun `should return error when no arguments provided`() {
        val result = setCommand.execute(environment, System.`in`, outputStream, errorStream, emptyList())

        val errorMessage = "At least one argument excepted"
        assertEquals(errorMessage, errorStream.toString().trim())
        assertEquals(1, result.exitCode)
    }
}