package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.CommandInterrupted
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class ExitCommandTest {

    private lateinit var environment: Environment
    private lateinit var exitCommand: ExitCommand
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        exitCommand = ExitCommand()
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should return last exit code when no arguments provided`() {
        every { environment.lastExitCode } returns 0

        val result = exitCommand.execute(environment, System.`in`, outputStream, errorStream, emptyList())

        assert(result is CommandInterrupted)
        assertEquals(0, (result as CommandInterrupted).exitCode)
    }

    @Test
    fun `should return specified exit code when one argument provided`() {
        val result = exitCommand.execute(environment, System.`in`, outputStream, errorStream, listOf("2"))

        assert(result is CommandInterrupted)
        assertEquals(2, (result as CommandInterrupted).exitCode)
    }

    @Test
    fun `should return error when more than one argument is provided`() {
        val result = exitCommand.execute(environment, System.`in`, outputStream, errorStream, listOf("2", "extra"))

        val errorMessage = "exit command except expect 1 argument"
        assertEquals(errorMessage, errorStream.toString().trim())
        assert(result is ErrorResult)
        assertEquals(1, result.exitCode)
    }

}