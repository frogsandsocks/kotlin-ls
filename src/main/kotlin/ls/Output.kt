package ls

interface Output {

    fun print(data: String)
    fun println(data: String)
    fun closeFile()
}