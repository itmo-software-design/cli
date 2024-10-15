
import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Echo` command definition
 *
 * @author gkashin
 * @since 0.0.1
 */
class EchoCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ) {
        if (arguments.isEmpty()) {
            var c: Int
            while ((inputStream.read().also { c = it }) != -1) {
                outputStream.write(c)
            }
            return
        }
        val joined = arguments.joinToString(separator = " ", postfix = "\n")
        try {
            outputStream.write(joined.toByteArray())
        } catch (e: IOException) {
            errorStream.writeLineUTF8("Output stream write exception, reason: ${e.message}")
        }
    }
}
