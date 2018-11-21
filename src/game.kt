import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*


val myChip8 : Chip8 = Chip8Impl(false)

fun main(args: Array<String>) {

    val gamePanel = GamePanel()
    initializeGraphics(gamePanel)

    myChip8.initialize()
    myChip8.loadGame("rom/pong2.c8")

    while (true) {
        myChip8.emulateCycle()

        if (myChip8.drawFlag) {
            gamePanel.repaint()
            myChip8.drawFlag = false
        }

        Thread.sleep(1)
    }
}

fun initializeGraphics(gamePanel: GamePanel) {

    val frame = JFrame()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isResizable = false

    frame.contentPane.add(gamePanel, BorderLayout.CENTER)

    frame.title = "Chip-8 Emulator"
    frame.pack()
    frame.isVisible = true
}

class GamePanel : JPanel(), KeyListener {
    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_ESCAPE -> System.exit(0)
            KeyEvent.VK_1 -> myChip8.key[0x1] = 1
            KeyEvent.VK_2 -> myChip8.key[0x2] = 1
            KeyEvent.VK_3 -> myChip8.key[0x3] = 1
            KeyEvent.VK_4 -> myChip8.key[0xC] = 1

            KeyEvent.VK_Q -> myChip8.key[0x4] = 1
            KeyEvent.VK_W -> myChip8.key[0x5] = 1
            KeyEvent.VK_E -> myChip8.key[0x6] = 1
            KeyEvent.VK_R -> myChip8.key[0xD] = 1

            KeyEvent.VK_A -> myChip8.key[0x7] = 1
            KeyEvent.VK_S -> myChip8.key[0x8] = 1
            KeyEvent.VK_D -> myChip8.key[0x9] = 1
            KeyEvent.VK_F -> myChip8.key[0xE] = 1

            KeyEvent.VK_Z -> myChip8.key[0xA] = 1
            KeyEvent.VK_X -> myChip8.key[0x0] = 1
            KeyEvent.VK_C -> myChip8.key[0xB] = 1
            KeyEvent.VK_V -> myChip8.key[0xF] = 1
            else -> println("Not pressed implemented ${e.keyCode}")
        }
    }

    override fun keyReleased(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_1 -> myChip8.key[0x1] = 0
            KeyEvent.VK_2 -> myChip8.key[0x2] = 0
            KeyEvent.VK_3 -> myChip8.key[0x3] = 0
            KeyEvent.VK_4 -> myChip8.key[0xC] = 0

            KeyEvent.VK_Q -> myChip8.key[0x4] = 0
            KeyEvent.VK_W -> myChip8.key[0x5] = 0
            KeyEvent.VK_E -> myChip8.key[0x6] = 0
            KeyEvent.VK_R -> myChip8.key[0xD] = 0

            KeyEvent.VK_A -> myChip8.key[0x7] = 0
            KeyEvent.VK_S -> myChip8.key[0x8] = 0
            KeyEvent.VK_D -> myChip8.key[0x9] = 0
            KeyEvent.VK_F -> myChip8.key[0xE] = 0

            KeyEvent.VK_Z -> myChip8.key[0xA] = 0
            KeyEvent.VK_X -> myChip8.key[0x0] = 0
            KeyEvent.VK_C -> myChip8.key[0xB] = 0
            KeyEvent.VK_V -> myChip8.key[0xF] = 0
            else -> println("Not released implemented ${e.keyCode}")
        }
    }

    val chip8Width = 64
    val chip8height = 32
    val pixelSize = 16

    init {
        preferredSize = Dimension(chip8Width * pixelSize, chip8height * pixelSize)
        background = Color.BLACK
        isFocusable = true
        addKeyListener(this)
        requestFocusInWindow()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.BLACK
        g.fillRect(0, 0, chip8Width * pixelSize, chip8height * pixelSize)

        g.color = Color.WHITE // white on black screen

        for (i in 0 until myChip8.screenPixels.size) {
            for (j in 0 until myChip8.screenPixels[i].size) {
                if (myChip8.screenPixels[i][j]) {
                    g.fillRect(j * pixelSize, i * pixelSize, pixelSize, pixelSize)
                }
            }
        }
    }

}
