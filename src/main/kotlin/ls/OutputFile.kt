package ls
import java.io.File

class OutputFile(pathToFile: String) : Output {

    init { if (!File(pathToFile).canWrite()) { throw IllegalAccessException("File is not writable") } }

    private val writer = File(pathToFile).bufferedWriter()

    override fun println(data: String) {
        writer.write(data)
        writer.newLine()
    }

    override fun print(data: String) { writer.write(data) }

    override fun close() { writer.close() }
}