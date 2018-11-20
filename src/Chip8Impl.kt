import java.io.File
import kotlin.random.Random

class Chip8Impl (override var drawFlag: Boolean) : Chip8 {
    override var screenPixels: Array<Array<Boolean>> = Array(32) {
        Array(64) {
            false
        }
    }

    override var key = ByteArray(16)
    var vRegister = ByteArray(16)
    var stack = IntArray(16)
    var memory = ByteArray(4096)

    var pc = 0x200
    var opCode = 0
    var I = 0
    var sp = 0

    var delay_timer = 0

    var chip8_fontset =  byteArrayOfInts(
    0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
    0x20, 0x60, 0x20, 0x20, 0x70, // 1
    0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
    0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
    0x90, 0x90, 0xF0, 0x10, 0x10, // 4
    0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
    0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
    0xF0, 0x10, 0x20, 0x40, 0x40, // 7
    0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
    0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
    0xF0, 0x90, 0xF0, 0x90, 0x90, // A
    0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
    0xF0, 0x80, 0x80, 0x80, 0xF0, // C
    0xE0, 0x90, 0x90, 0x90, 0xE0, // D
    0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
    0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    )

    override fun initialize() {
        pc = 0x200
        opCode = 0
        I = 0
        sp = 0

        // load the font set
        for (i in 0 until 80) {
            memory[i] = chip8_fontset[i]
        }

    }

    override fun loadGame(gameName: String) {
        val buffer = File(gameName).readBytes()

        for (i in 0 until buffer.size) {
            memory[i + 0x200] = buffer[i]
        }

//        for (i in 0 until buffer.size step 2) {
//            print(String.format("%02X%02X ", buffer[i], buffer[i+1]))
//            if (i % 16 == 0) println()
//        }
    }

    override fun emulateCycle() {
        // fetch Opcode
        val top = memory[pc].toInt() and 0xFF shl 8
        val bottom = memory[pc + 1].toInt() and 0xFF
        opCode = top or bottom
        println("Executing: 0x${String.format("%04X ", opCode)}")

        // decode and execute Opcode
        val parameterX = (opCode and 0x0F00) shr 8
        val parameterY = (opCode and 0x00F0) shr 4
        when (opCode and 0xF000) {
            0x0000 -> {
                when (opCode and 0x000F) {
                    // clear the screen
                    0x0000 -> {
                        for (i in 0 until screenPixels.size) {
                            for (j in 0 until screenPixels[i].size) {
                                screenPixels[i][j] = false
                            }
                        }
                        drawFlag = true
                        pc += 2
                    }
                    // return from subroutine
                    0x000E -> {
                        --sp
                        pc = stack[sp]
                        pc += 2
                    }
                    else -> {
                        println("Unknown opcode: 0x${String.format("%04X ", opCode)}")
                        pc += 2
                    }
                }
            }
            0x1000 -> {
                pc = opCode and 0x0FFF
            }
            0x2000 -> {
                stack[sp] = pc
                ++sp
                pc = opCode and 0x0FFF
            }
            0x3000 -> {
                pc += if (vRegister[parameterX] == (opCode and 0x00FF).toByte()) {
                    4
                } else {
                    2
                }
            }
            0x4000 -> {
                pc += if (vRegister[parameterX] != (opCode and 0x00FF).toByte()) {
                    4
                } else {
                    2
                }
            }
            0x5000 -> {
                if (vRegister[parameterX] == vRegister[parameterY]) {
                    pc += 4
                } else {
                    pc += 2
                }
            }
            0x6000 -> {
                vRegister[parameterX] = (opCode and 0x00FF).toByte()
                pc += 2

            }
            0x7000 -> {
                vRegister[parameterX] = vRegister[parameterX].plus((opCode and 0x00FF).toByte()).toByte()
                pc += 2
            }
            0x8000 -> {
                val param1 = vRegister[parameterX].toPositiveInt()
                val param2 = vRegister[parameterY].toPositiveInt()
                when (opCode and 0x000F) {
                    0x0000 -> {
                        vRegister[parameterX] = param2.toByte()
                        pc += 2
                    }
                    0x0001 -> {
                        vRegister[parameterX] = (param1 or param2).toByte()
                        pc += 2
                    }
                    0x0002 -> {
                        vRegister[parameterX] = (param1 and param2).toByte()
                        pc += 2
                    }
                    0x0003 -> {
                        vRegister[parameterX] = (param1 xor param2).toByte()
                        pc += 2
                    }
                    0x0004 -> {
                        if(param2 > (0xFF - param1)) {
                            vRegister[0xF] = 1
                        } else {
                            vRegister[0xF] = 0
                        }
                        vRegister[parameterX] = param1.plus(param2).toByte()
                        pc += 2
                    }
                    0x0005 -> {
                        if(param2 > param1) {
                            vRegister[0xF] = 0
                        } else {
                            vRegister[0xF] = 1
                        }
                        vRegister[parameterX] = param1.minus(param2).toByte()
                        pc += 2
                    }
                    0x0006 -> {
                        vRegister[0xF] = (param1 and 0x1).toByte()
                        vRegister[parameterX] = (param1 shr 1).toByte()
                        pc += 2
                    }
                    0x0007 -> {
                        if (param1 > param2) {
                            vRegister[0xF] = 0
                        } else {
                            vRegister[0xF] = 1
                        }
                        vRegister[parameterX] = (param2 - param1).toByte()
                        pc += 2
                    }
                    0x000E -> {
                        vRegister[0xF] = (param1 shr 7).toByte()
                        vRegister[parameterX] = (param1 shl 1).toByte()
                        pc += 2
                    }
                    else -> {
                        println("Unknown opcode: 0x${String.format("%04X ", opCode)}")
                    }

                }
            }
            0x9000 -> {
                val param1 = vRegister[parameterX]
                val param2 = vRegister[parameterY]
                pc += if (param1 != param2) {
                    4
                } else {
                    2
                }
            }
            0xA000 -> {
                I = opCode and 0x0FFF
                pc += 2
            }
            0xB000 -> {
                pc = (opCode and 0x0FFF) + vRegister[0]
            }
            0xC000 -> {
                vRegister[parameterX] = ((Random.nextInt() % 0xFF) and (opCode and 0x00FF)).toByte()
                pc += 2
            }
            0xD000 -> {
                val x: Int = vRegister[parameterX].toPositiveInt()
                val y: Int = vRegister[parameterY].toPositiveInt()
                val height = opCode and 0x000F
                var pixel: Int

                vRegister[0xF] = 0
                for (yline in 0 until height) {
                    pixel = memory[I + yline].toPositiveInt()
                    for (xline in 0 until 8) {
                        if ((pixel and (0x80 shr xline)) != 0) {
                            if (screenPixels[(y + yline) % 32][(x + xline) % 64]) {
                                vRegister[0xF] = 1
                            }
                            screenPixels[(y + yline) % 32][(x + xline) % 64] = screenPixels[(y + yline) % 32][(x + xline) % 64] xor true
                        }
                    }
                }

                drawFlag = true
                pc += 2
            }
            0xE000 -> {
                when (opCode and 0x00FF) {
                    0x009E -> {
                        if (key[vRegister[parameterX].toPositiveInt()].toPositiveInt() != 0) {
                            pc += 4
                        } else {
                            pc += 2
                        }
                    }
                    0x00A1 -> {
                        if (key[vRegister[parameterX].toPositiveInt()].toPositiveInt() == 0) {
                            pc += 4
                        } else {
                            pc += 2
                        }
                    }
                    else -> {
                        println("Unknown opcode: 0x${String.format("%04X ", opCode)}")
                    }
                }
            }
            0xF000 -> {
                when (opCode and 0x00FF) {
                    0x0007 -> {
                        vRegister[parameterX] = delay_timer.toByte()
                        pc += 2
                    }
                    0x000A -> {
                        var keyPress = false

                        for (i in 0 until 16) {
                            if (!key[i].equals(0)) {
                                vRegister[parameterX] = i.toByte()
                                keyPress = true
                            }
                        }

                        if (!keyPress)
                            return

                        pc += 2
                    }
                    0x0015 -> {
                        delay_timer = vRegister[parameterX].toPositiveInt()
                        pc += 2
                    }
                    0x0018 -> {
                        // ignoring sound since it's a nice to have and I'm too lazy to implement it
                        pc += 2
                    }
                    0x001E -> {
                        if (I + vRegister[parameterX].toPositiveInt() > 0xFFF) {
                            vRegister[0xF] = 1
                        } else {
                            vRegister[0xF] = 2
                        }
                        I += vRegister[parameterX].toPositiveInt()
                        pc += 2
                    }
                    0x0029 -> {
                        I = vRegister[parameterX].toPositiveInt() * 0x5
                        pc += 2
                    }
                    0x0033 -> {
                        memory[I] = (vRegister[parameterX].toPositiveInt() / 100).toByte()
                        memory[I + 1] = (vRegister[parameterX].toPositiveInt() / 10 % 10).toByte()
                        memory[I + 2] = (vRegister[parameterX].toPositiveInt() % 100 % 10).toByte()
                        pc += 2
                    }
                    0x0055 -> {
                        for (i in 0..parameterX) {
                            memory[I + i]  = vRegister[i]
                        }
                    }
                    0x0065 -> {
                        for (i in 0..parameterX) {
                            vRegister[i] = memory[I + i]
                        }
                        I += parameterX + 1
                        pc += 2
                    }
                    else -> {
                        println("Unknown opcode: 0x${String.format("%04X ", opCode)}")
                    }

                }
            }
            else -> {
                println("Unknown opcode: 0x${String.format("%04X ", opCode)}")
            }
        }

        // Update Timers
        if (delay_timer > 0) {
            --delay_timer
        }

    }

    override fun printPixelScreen() {
        for (i in 0 until screenPixels.size) {
            for (j in 0 until screenPixels[i].size) {
                if (screenPixels[i][j]) {
                    print('*')
                } else {
                    print(' ')
                }
            }
            println()
        }
    }

    override fun setKeys() {





    }

}