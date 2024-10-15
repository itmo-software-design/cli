package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

class ChangeDirectoryCommandTest {

    private lateinit var environment: Environment
    private lateinit var changeDirectoryCommand: ChangeDirectoryCommand
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        changeDirectoryCommand = ChangeDirectoryCommand()
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should change working directory when correct argument provided`() {
        val currentDir = Files.createTempDirectory("current")
        currentDir.resolve("child").createDirectory()
        val newDir = "child"
        val expectedDir = currentDir.resolve(newDir).normalize().toAbsolutePath()

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(newDir))

        verify { environment.workingDirectory = expectedDir }
    }

    @Test
    fun `should not change working directory when not existed directory provided`() {
        val currentDir = Path.of(UUID.randomUUID().toString())
        val newDir = "child"

        every { environment.workingDirectory } returns currentDir

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(newDir))

        verify (exactly = 0) { environment.workingDirectory = any() }
    }

    @Test
    fun `should not change working directory when not directory provided`() {
        val currentDir = Files.createTempDirectory("current")
        currentDir.resolve("child").createFile()
        val newDir = "child"

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(newDir))

        verify (exactly = 0) { environment.workingDirectory = any() }
    }

    @Test
    fun `should write error when no arguments provided`() {
        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, emptyList())

        val errorMessage = "Change directory command except expect 1 argument"
        assertEquals(errorMessage, errorStream.toString().trim())
    }

    @Test
    fun `should write error when too many arguments provided`() {
        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf("dir1", "dir2"))

        val errorMessage = "Change directory command except expect 1 argument"
        assertEquals(errorMessage, errorStream.toString().trim())
    }
}