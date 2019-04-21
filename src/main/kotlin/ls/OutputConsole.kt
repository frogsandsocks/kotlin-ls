package ls

class OutputConsole : Output {

    override fun print(data: String) { kotlin.io.print(data) }
    override fun println(data: String) { kotlin.io.println(data) }
    override fun closeFile() {}
}