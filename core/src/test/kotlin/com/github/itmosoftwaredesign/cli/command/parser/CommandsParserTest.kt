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
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createFile

class CommandsParserTest {

    private lateinit var commandsParser: CommandsParser
    private lateinit var environment: Environment
    private lateinit var workingDirectory: Path

    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        workingDirectory = Path.of("/test/directory")
        every { environment.workingDirectory } returns workingDirectory
        commandsParser = CommandsParser(environment)
    }

    @Test
    fun `should parse simple command without redirections`() {
        val input = "echo Hello"
        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", "Hello"), parsedCommands.commandTokens)
        assertEquals(System.`in`, parsedCommands.inputStream)
        assertEquals(System.out, parsedCommands.outputStream)
        assertEquals(System.err, parsedCommands.errorStream)
    }

    @Test
    fun `should parse input redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "input.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cat < $fileName"
        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cat"), parsedCommands.commandTokens)
        assert(parsedCommands.inputStream is FileInputStream)
    }

    @Test
    fun `should parse output redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "output.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd > $fileName"
        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cmd"), parsedCommands.commandTokens)
        assert(parsedCommands.outputStream is FileOutputStream)
    }

    @Test
    fun `should parse append output redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "output.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd >> $fileName"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cmd"), parsedCommands.commandTokens)
        assert(parsedCommands.outputStream is FileOutputStream)
    }

    @Test
    fun `should parse error stream redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "error.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd 2> $fileName"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cmd"), parsedCommands.commandTokens)
        assert(parsedCommands.errorStream is FileOutputStream)
    }

    @Test
    fun `should parse append error stream redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "error.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd 2>> $fileName"
        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cmd"), parsedCommands.commandTokens)
        assert(parsedCommands.errorStream is FileOutputStream)
    }

    @Test
    fun `should parse both output and error stream redirection`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val fileName = "error.txt"
        workingDirectory.resolve(fileName)
            .createFile()

        val input = "cmd &> output.txt"
        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cmd"), parsedCommands.commandTokens)
        assert(parsedCommands.outputStream == parsedCommands.errorStream)
    }

    @Test
    fun `should throw IllegalArgumentException when file not found`() {
        val input = "cat < nonexistent.txt"

        every { environment.workingDirectory.resolve("nonexistent.txt").toFile() } throws FileNotFoundException()

        assertThrows<IllegalArgumentException> {
            commandsParser.parse(input).first()
        }
    }

    @Test
    fun `should tokenize string with spaces and quotes`() {
        val input = "echo 'hello world' \"Kotlin CLI\""
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("echo", "hello world", "Kotlin CLI"), tokens)
    }

    @Test
    fun `should tokenize equals in the middle`() {
        val input = "do key=value"
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("do", "key=value"), tokens)
    }

    @Test
    fun `should replace new line in single quotes`() {
        val input = "echo 'hello\\nworld'"
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("echo", "hello\nworld"), tokens)
    }

    @Test
    fun `should keep new line in double quotes`() {
        val input = "echo \"hello\\nworld\""
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("echo", "hello\\nworld"), tokens)
    }

    @Test
    fun `should keep dash at the end`() {
        val input = "echo \\"
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("echo", "\\"), tokens)
    }

    @Test
    fun `should keep unknown replacement`() {
        val input = "echo \\b"
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("echo", "\\b"), tokens)
    }

    @Test
    fun `should keep unknown replacement in quotes`() {
        val input = "echo '\\b'"
        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("echo", "\\b"), tokens)
    }

    @Test
    fun `should handle set variable value command`() {
        val input = "test=me"
        every { environment.getVariable("HOME") } returns "/home/user"

        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("set", "test", "me"), tokens)
    }

    @Test
    fun `should handle command with 1st = symbol`() {
        val input = "=me"

        val tokens = commandsParser.parse(input).first().commandTokens

        assertEquals(listOf("=me"), tokens)
    }

    @Test
    fun `should handle variable substitution`() {
        val input = "echo \$HOME"
        every { environment.getVariable("HOME") } returns "/home/user"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", "/home/user"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable substitution when no variable is found`() {
        val input = "echo \$HOME"
        every { environment.getVariable("HOME") } returns null

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable substitution with numbers and underscores`() {
        val input = "echo \$HOME_1"
        every { environment.getVariable("HOME_1") } returns "/home/user"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", "/home/user"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable substitution with brackets`() {
        val input = "echo \${HOME}"
        every { environment.getVariable("HOME") } returns "/home/user"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", "/home/user"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable substitution with incomplete brackets`() {
        val input = "echo \${HOME"
        every { environment.getVariable("HOME") } returns "/home/user"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", "/home/user"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable substitution with brackets at the end`() {
        val input = "echo \${"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable system dollar substitution`() {
        val input = "echo \$$"
        val value = UUID.randomUUID().toString()
        every { environment.getVariable("$") } returns value

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", value), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable system ask substitution`() {
        val input = "echo \$?"
        val value = UUID.randomUUID().toString()
        every { environment.getVariable("?") } returns value

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo", value), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable system ask substitution when no value`() {
        val input = "echo \$?"
        every { environment.getVariable("?") } returns null

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle variable substitution at the end`() {
        val input = "echo \$"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("echo"), parsedCommands.commandTokens)
    }

    @Test
    fun `should parse cd upper directory`() {
        val input = "cd .."

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cd", ".."), parsedCommands.commandTokens)
    }

    @Test
    fun `should parse cd and empty`() {
        val input = "cd "

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cd"), parsedCommands.commandTokens)
    }

    @Test
    fun `should parse ls and empty`() {
        val input = "ls "

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("ls"), parsedCommands.commandTokens)
    }

    @Test
    fun `should parse cd and directory`() {
        val input = "cd src/main"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("cd", "src/main"), parsedCommands.commandTokens)
    }

    @Test
    fun `should parse ls and directory`() {
        val input = "ls src/main"

        val parsedCommands = commandsParser.parse(input).first()

        assertEquals(listOf("ls", "src/main"), parsedCommands.commandTokens)
    }

    @Test
    fun `should handle parsing several commands`() {
        val input = "echo \"input\" | wc | echo \"output\""

        val parsedCommands = commandsParser.parse(input)

        assertEquals(listOf("echo", "input"), parsedCommands.first().commandTokens)
        assertEquals(listOf("wc"), parsedCommands[1].commandTokens)
        assertEquals(listOf("echo", "output"), parsedCommands.last().commandTokens)
    }

    @Test
    fun `should handle pipeline streams between commands`() {
        val input = "echo \"input\" | wc | echo \"output\""

        val parsedCommands = commandsParser.parse(input)

        assertEquals(System.`in`, parsedCommands.first().inputStream)
        assert(parsedCommands.first().outputStream is PipedOutputStream)
        assert(parsedCommands[1].inputStream is PipedInputStream)
        assert(parsedCommands[1].outputStream is PipedOutputStream)
        assert(parsedCommands.last().inputStream is PipedInputStream)
        assertEquals(System.out, parsedCommands.last().outputStream)
    }

    @Test
    fun `should handle streams redirections within pipeline`() {
        val workingDirectory = Files.createTempDirectory("command-parser-test")
        every { environment.workingDirectory } returns workingDirectory

        val outputFileName = "output.txt"
        workingDirectory.resolve(outputFileName)
            .createFile()

        val inputFileName = "input.txt"
        workingDirectory.resolve(inputFileName)
            .createFile()

        val input = "cat < input.txt | wc > output.txt"

        val parsedCommands = commandsParser.parse(input)

        assert(parsedCommands.first().inputStream is FileInputStream)
        assert(parsedCommands.first().outputStream is PipedOutputStream)
        assert(parsedCommands.last().inputStream is PipedInputStream)
        assert(parsedCommands.last().outputStream is FileOutputStream)
    }
}
