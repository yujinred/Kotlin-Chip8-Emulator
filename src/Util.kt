fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) {pos ->
    ints[pos].toByte()
}

fun Byte.toPositiveInt() : Int {
    return this.toInt() and 0xFF
}
