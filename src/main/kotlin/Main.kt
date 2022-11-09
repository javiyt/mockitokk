import java.io.File
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlin.io.path.absolute

fun main(args: Array<String>) {
    val parser = ArgParser("mockitokk")
    val dir by parser.option(ArgType.String, shortName = "d", fullName = "dir", description = "Directory to parse")
        .required()
    val override by parser.option(ArgType.Boolean, shortName = "o", fullName = "override", description = "Override " +
        "original file").default(true)

    parser.parse(args)

    File(dir).walk()
        .filter { it.isFile && it.extension == "kt" }
        .forEach {
            val content = it.readText(Charsets.UTF_8)
            if (!content.contains("mockito")) {
                return@forEach
            }

            File(if (override) it.absolutePath else it.toMockkFile())
                .writeText(content.removeImport().replaceMock().replaceGivenReturns().replaceVerify())
        }
}

fun String.removeImport() = this.split("(\r\n|\n)".toRegex())
    .filter { !it.contains("mockito") }
    .joinToString("\n")

fun String.replaceMock() = this.replace("mock<(.*)>".toRegex(), "mockk<$1>")
    .replace("mock\\(\\)".toRegex(), "mockk()")
    .replace("@Mock", "@MockK")

// TODO: take into account `when`
fun String.replaceGivenReturns() = this.replace(
    "given\\((.*)\\)(\n|)(\t|)(\\s*|)\\.willReturn\\((.*)\\)".toRegex()
) {
    "every { ${it.groupValues[1]} } returns ${it.groupValues.last()}"
}

// TODO: take into account `when`
fun String.replaceGivenThrows() = this.replace(
    "given\\((.*)\\)(\n|)(\t|)(\\s*|)\\.thenThrow\\((.*)\\)".toRegex()
) {
    "every { ${it.groupValues[1]} } throws ${it.groupValues.last()}"
}

fun String.replaceVerify() = this.replace(
    "verify\\((.*), times\\(([0-9])\\)\\)(\n|)(\t|)(\\s*|)\\.(.*)".toRegex()
) {
    "verify(exactly = ${it.groupValues[2]}) { ${it.groupValues[1]}.${it.groupValues.last()} }"
}

fun File.toMockkFile() = "${this.toPath().absolute().parent}/${this.nameWithoutExtension}Mockk.${this.extension}"