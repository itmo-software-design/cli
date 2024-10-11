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
}
