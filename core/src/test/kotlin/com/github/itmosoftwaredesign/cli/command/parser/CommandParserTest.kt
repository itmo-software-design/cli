package com.github.itmosoftwaredesign.cli.command.parser

import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createFile

class CommandParserTest {

    private lateinit var commandParser: CommandParser
    private lateinit var environment: Environment
    private lateinit var workingDirectory: Path

    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        workingDirectory = Path.of("/test/directory")
        every { environment.workingDirectory } returns workingDirectory
        commandParser = CommandParser(environment)
    }

    @Test
    fun `should parse simple command without redirections`() {
        val input = "echo Hello"
        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", "Hello"), parsedCommand.commandTokens)
        assertEquals(System.`in`, parsedCommand.inputStream)
        assertEquals(System.out, parsedCommand.outputStream)
        assertEquals(System.err, parsedCommand.errorStream)
    }

    @Test
    fun `should parse input redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "input.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cat < $fileName"
        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("cat"), parsedCommand.commandTokens)
        assert(parsedCommand.inputStream is FileInputStream)
    }

    @Test
    fun `should parse output redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "output.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd > $fileName"
        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("cmd"), parsedCommand.commandTokens)
        assert(parsedCommand.outputStream is FileOutputStream)
    }

    @Test
    fun `should parse append output redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "output.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd >> $fileName"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("cmd"), parsedCommand.commandTokens)
        assert(parsedCommand.outputStream is FileOutputStream)
    }

    @Test
    fun `should parse error stream redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "error.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd 2> $fileName"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("cmd"), parsedCommand.commandTokens)
        assert(parsedCommand.errorStream is FileOutputStream)
    }

    @Test
    fun `should parse append error stream redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "error.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd 2>> $fileName"
        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("cmd"), parsedCommand.commandTokens)
        assert(parsedCommand.errorStream is FileOutputStream)
    }

    @Test
    fun `should parse both output and error stream redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "error.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd &> output.txt"
        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("cmd"), parsedCommand.commandTokens)
        assert(parsedCommand.outputStream == parsedCommand.errorStream)
    }

    @Test
    fun `should throw IllegalArgumentException when file not found`() {
        val input = "cat < nonexistent.txt"

        every { environment.workingDirectory.resolve("nonexistent.txt").toFile() } throws FileNotFoundException()

        assertThrows<IllegalArgumentException> {
            commandParser.parse(input)
        }
    }

    @Test
    fun `should tokenize string with spaces and quotes`() {
        val input = "echo 'hello world' \"Kotlin CLI\""
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("echo", "hello world", "Kotlin CLI"), tokens)
    }

    @Test
    fun `should tokenize equals in the middle`() {
        val input = "do key=value"
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("do", "key=value"), tokens)
    }

    @Test
    fun `should replace new line in single quotes`() {
        val input = "echo 'hello\\nworld'"
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("echo", "hello\nworld"), tokens)
    }

    @Test
    fun `should keep new line in double quotes`() {
        val input = "echo \"hello\\nworld\""
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("echo", "hello\\nworld"), tokens)
    }

    @Test
    fun `should keep dash at the end`() {
        val input = "echo \\"
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("echo", "\\"), tokens)
    }

    @Test
    fun `should keep unknown replacement`() {
        val input = "echo \\b"
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("echo", "\\b"), tokens)
    }

    @Test
    fun `should keep unknown replacement in quotes`() {
        val input = "echo '\\b'"
        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("echo", "\\b"), tokens)
    }

    @Test
    fun `should handle set variable value command`() {
        val input = "test=me"
        every { environment.getVariable("HOME") } returns "/home/user"

        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("set", "test", "me"), tokens)
    }

    @Test
    fun `should handle command with 1st = symbol`() {
        val input = "=me"

        val tokens = commandParser.parse(input).commandTokens

        assertEquals(listOf("=me"), tokens)
    }

    @Test
    fun `should handle variable substitution`() {
        val input = "echo \$HOME"
        every { environment.getVariable("HOME") } returns "/home/user"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", "/home/user"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable substitution when no variable is found`() {
        val input = "echo \$HOME"
        every { environment.getVariable("HOME") } returns null

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable substitution with numbers and underscores`() {
        val input = "echo \$HOME_1"
        every { environment.getVariable("HOME_1") } returns "/home/user"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", "/home/user"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable substitution with brackets`() {
        val input = "echo \${HOME}"
        every { environment.getVariable("HOME") } returns "/home/user"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", "/home/user"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable substitution with incomplete brackets`() {
        val input = "echo \${HOME"
        every { environment.getVariable("HOME") } returns "/home/user"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", "/home/user"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable substitution with brackets at the end`() {
        val input = "echo \${"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable system dollar substitution`() {
        val input = "echo \$$"
        val value = UUID.randomUUID().toString()
        every { environment.getVariable("$") } returns value

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", value), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable system ask substitution`() {
        val input = "echo \$?"
        val value = UUID.randomUUID().toString()
        every { environment.getVariable("?") } returns value

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo", value), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable system ask substitution when no value`() {
        val input = "echo \$?"
        every { environment.getVariable("?") } returns null

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo"), parsedCommand.commandTokens)
    }

    @Test
    fun `should handle variable substitution at the end`() {
        val input = "echo \$"

        val parsedCommand = commandParser.parse(input)

        assertEquals(listOf("echo"), parsedCommand.commandTokens)
    }
}
