interface Chip8 {
    var drawFlag: Boolean
    var screenPixels: Array<Array<Boolean>>
    var key: ByteArray

    fun initialize(): Unit

    fun loadGame(gameName: String): Unit

    fun emulateCycle(): Unit

    fun printPixelScreen(): Unit
}