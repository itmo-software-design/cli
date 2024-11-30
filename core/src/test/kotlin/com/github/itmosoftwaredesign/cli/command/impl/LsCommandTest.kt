import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.impl.LsCommand
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.command.SuccessResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

class LsCommandTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should list files in current directory`() {
        // Arrange
        val testDir = tempDir.toFile()
        File(testDir, "file1.txt").createNewFile()
        File(testDir, "file2.txt").createNewFile()
        File(testDir, "subdir").mkdir()

        val environment = Environment(workingDirectory = testDir.toPath())
        val lsCommand = LsCommand()
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        // Act
        val result = lsCommand.execute(
            environment,
            ByteArrayInputStream(ByteArray(0)),
            outputStream,
            errorStream,
            emptyList()
        )

        // Assert
        val output = outputStream.toString().trim().lines()
        assertEquals(listOf("file1.txt", "file2.txt", "subdir"), output.sorted())
        assertTrue(result is SuccessResult)
    }

    @Test
    fun `should list files in specified directory`() {
        // Arrange
        val testDir = tempDir.toFile()
        val subDir = File(testDir, "subdir")
        subDir.mkdir()
        File(subDir, "file1.txt").createNewFile()
        File(subDir, "file2.txt").createNewFile()

        val environment = Environment(workingDirectory = testDir.toPath())
        val lsCommand = LsCommand()
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        // Act
        val result = lsCommand.execute(
            environment,
            ByteArrayInputStream(ByteArray(0)),
            outputStream,
            errorStream,
            listOf("subDir")
        )

        // Assert
        val output = outputStream.toString().trim().lines()
        assertEquals(listOf("file1.txt", "file2.txt"), output.sorted())
        assertTrue(result is SuccessResult)
    }

    @Test
    fun `should handle non-existent directory`() {
        // Arrange
        val testDir = tempDir.toFile()
        val environment = Environment(workingDirectory = testDir.toPath())
        val lsCommand = LsCommand()
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        // Act
        val result = lsCommand.execute(
            environment,
            ByteArrayInputStream(ByteArray(0)),
            outputStream,
            errorStream,
            listOf("nonexistent")
        )

        // Assert
        assertTrue(result is ErrorResult)
    }

    @Test
    fun `should handle a single file as argument`() {
        // Arrange
        val testDir = tempDir.toFile()
        val testFile = File(testDir, "file1.txt")
        testFile.createNewFile()

        val environment = Environment(workingDirectory = testDir.toPath())
        val lsCommand = LsCommand()
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        // Act
        val result = lsCommand.execute(
            environment,
            ByteArrayInputStream(ByteArray(0)),
            outputStream,
            errorStream,
            listOf("file1.txt")
        )

        // Assert
        val output = outputStream.toString().trim()
        assertEquals("file1.txt", output)
        assertTrue(result is SuccessResult)
    }

}
