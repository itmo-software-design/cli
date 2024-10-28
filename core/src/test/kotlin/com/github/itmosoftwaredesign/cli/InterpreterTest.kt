package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser
import com.github.itmosoftwaredesign.cli.command.parser.ParsedCommand
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream

class InterpreterTest {

    private lateinit var environment: Environment
    private lateinit var commandRegistry: CommandRegistry
    private lateinit var commandParser: CommandParser
    private lateinit var inputStream: InputStream
    private lateinit var interpreter: Interpreter

    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        commandRegistry = mockk(relaxed = true)
        commandParser = mockk(relaxed = true)
    }

    @Test
    fun `should execute known command`() {
        val commandMock = mockk<Command>(relaxed = true)
        val parsedCommand = mockk<ParsedCommand>(relaxed = true)
        val commandTokens = listOf("echo", "Hello")

        inputStream = ByteArrayInputStream("echo Hello\nexit\n".toByteArray())
        every { parsedCommand.commandTokens } returns commandTokens
        every { commandParser.parse("echo Hello") } returns parsedCommand
        every { commandRegistry["echo"] } returns commandMock

        interpreter = Interpreter(environment, commandParser, commandRegistry, inputStream)
        interpreter.run()

        verify {
            commandMock.execute(
                environment,
                parsedCommand.inputStream,
                parsedCommand.outputStream,
                parsedCommand.errorStream,
                listOf("Hello")
            )
        }
    }

    @Test
    fun `should run external process on unknown command`() {
        inputStream = ByteArrayInputStream("ls\n".toByteArray())
        interpreter = spyk(Interpreter(environment, commandParser, commandRegistry, inputStream))
        every { environment.getVariableNames() } returns setOf()

        val parsedCommand = mockk<ParsedCommand>()
        every { commandParser.parse(any()) } returns parsedCommand
        every { parsedCommand.commandTokens } returns listOf("unknownCommand")
        every { commandRegistry["unknownCommand"] } returns null

        interpreter.run()

        verify { interpreter.runExternalCommand(parsedCommand, listOf()) }
    }

    @Test
    fun `should handle unknown command and run external program`() {
        val parsedCommand = mockk<ParsedCommand>(relaxed = true)
        val input = "externalCommand arg1\nexit\n"
        val inputStream = ByteArrayInputStream(input.toByteArray())

        every { parsedCommand.commandTokens } returns listOf("externalCommand", "arg1")
        every { commandParser.parse("externalCommand arg1") } returns parsedCommand
        every { commandRegistry["externalCommand"] } returns null

        val mockProcess = mockk<Process>(relaxed = true)
        val mockProcessBuilder = mockk<ProcessBuilder>(relaxed = true)

        mockkConstructor(ProcessBuilder::class)

        every {
            constructedWith<ProcessBuilder>(OfTypeMatcher<List<String>>(List::class))
                .directory(any())
        } returns mockProcessBuilder
        every { mockProcessBuilder.start() } returns mockProcess
        every { mockProcess.waitFor() } returns 0

        interpreter = Interpreter(environment, commandParser, commandRegistry, inputStream)
        interpreter.run()

        verify { mockProcess.waitFor() }
    }

    @Test
    fun `should pass environment variables to external program`() {
        val parsedCommand = mockk<ParsedCommand>(relaxed = true)
        val input = "externalCommand arg1\nexit\n"
        val inputStream = ByteArrayInputStream(input.toByteArray())

        every { parsedCommand.commandTokens } returns listOf("externalCommand", "arg1")
        every { commandParser.parse("externalCommand arg1") } returns parsedCommand
        every { commandRegistry["externalCommand"] } returns null
        every { environment.getVariableNames() } returns setOf("ENV_VAR")
        every { environment.getVariable("ENV_VAR") } returns "value"

        val mockProcessBuilder = mockk<ProcessBuilder>(relaxed = true)

        mockkConstructor(ProcessBuilder::class)
        every {
            constructedWith<ProcessBuilder>(OfTypeMatcher<List<String>>(List::class))
                .directory(any())
        } returns mockProcessBuilder

        val environmentMap = mockk<MutableMap<String, String>>(relaxed = true)
        every { mockProcessBuilder.environment() } returns environmentMap

        val mockProcess = mockk<Process>(relaxed = true)
        every { mockProcessBuilder.start() } returns mockProcess
        every { mockProcess.waitFor() } returns 0

        interpreter = Interpreter(environment, commandParser, commandRegistry, inputStream)
        interpreter.run()

        verify { mockProcess.waitFor() }
        verify { environmentMap["ENV_VAR"] = "value" }
    }

    @Test
    fun `should handle command execution exception`() {
        val commandMock = mockk<Command>(relaxed = true)
        val parsedCommand = mockk<ParsedCommand>(relaxed = true)
        val commandTokens = listOf("failingCommand")

        inputStream = ByteArrayInputStream("failingCommand\nexit\n".toByteArray())
        every { parsedCommand.commandTokens } returns commandTokens
        every { commandParser.parse("failingCommand") } returns parsedCommand
        every { commandRegistry["failingCommand"] } returns commandMock
        every {
            commandMock.execute(environment, any(), any(), any(), any())
        } throws RuntimeException("Execution failed")

        val errorOutput = ByteArrayOutputStream()
        System.setErr(PrintStream(errorOutput))

        interpreter = Interpreter(environment, commandParser, commandRegistry, inputStream)
        interpreter.run()

        assert(errorOutput.toString().contains("Command 'failingCommand' execution exception"))
    }
}
