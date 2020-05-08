package minesweeper

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val MINE = -1
private const val FREE = 0
private const val SIZE = 5

class Game(private val size: Int, private val minesCount: Int) {
    private val cells = Array(size) { IntArray(size) }

    private val marked = mutableListOf<Field>()

    private val opened = mutableListOf<Field>()

    private val markedAllMines get() = marked.size == minesCount && marked.all { it.mine }

    private val openedAllFree get() = opened.size == (size * size - minesCount)

    val completed get() = gameOver || markedAllMines || openedAllFree

    var gameOver = false
        private set

    private var Field.value
        get() = cells[x][y]
        set(value: Int) { cells[x][y] = value }

    private var Field.mine
        get() = value == MINE
        set(flag: Boolean) { value = MINE }

    private val Field.free get() = value == FREE

    private val Field.label get() = when(this) {
        in marked -> "*"
        in opened -> if (free) "/" else value.toString()
        else -> if (gameOver && mine) "X" else "."
    }

    private fun Field.addNearMine() = ++value

    private fun generateMines(exclude: Field) {
        repeat(minesCount) {
            // Find free field.
            var field: Field
            do {
                field = Field.random()
            } while (field == exclude || field.mine)

            // Add mines.
            field.run {
                mine = true
                neighbours.filterNot { it.mine }.forEach { it.addNearMine() }
            }
        }
    }

    fun open(field: Field) {
        if (opened.isEmpty()) {
            generateMines(field)
        }

        // Un-mark automatically.
        if (field in marked) {
            marked.remove(field)
        }

        if (field.mine) {
            gameOver = true
            marked.clear()
            return
        }

        opened.add(field)

        if (field.free) {
            field.neighbours
                .filterNot { it in opened }
                .filterNot { it.mine }
                .forEach { open(it) }
        }
    }

    fun mark(field: Field) {
        when (field) {
            in opened -> Unit
            in marked -> marked.remove(field)
            else -> marked.add(field)
        }
    }

    override fun toString(): String {
        val lines = cells.mapIndexed { x: Int, row: IntArray ->
            val line = row.mapIndexed { y, _ -> Field(x, y).label }
            "${x + 1}|${line.joinToString("")}|"
        }

        val table = lines.joinToString("\n")
        val header = (1..size).joinToString("")
        val border = "—".repeat(size)

        return "" +
            " │$header|\n" +
            "—│$border|\n" +
            "$table\n" +
            "—│$border|\n"
    }
}

data class Field(val x: Int, val y: Int) {
    constructor(x: String, y: String) : this(x.toInt() - 1, y.toInt() - 1)

    val neighbours get(): List<Field> = mutableListOf<Field>().apply {
        for (i in max(0, x - 1)..min(x + 1, SIZE - 1)) {
            for (j in max(0, y - 1)..min(y + 1, SIZE - 1)) {
                add(Field(i, j))
            }
        }
    }

    companion object {
        fun random() = Field(Random.nextInt(0, SIZE), Random.nextInt(0, SIZE))
    }
}

fun main() {
    print("How many mines do you want on the field? ")

    val mines = readLine()!!.toInt()
    val game = Game(SIZE, mines)

    println(game)

    while (!game.completed) {
        print("Set/unset mines marks or claim a cell as free: ")

        val (y, x, action) = readLine()!!.split(' ')
        val field = Field(x, y)

        when (action) {
            "free" -> game.open(field)
            "mine" -> game.mark(field)
        }

        println("\n$game")
    }

    println(if (game.gameOver) {
        "You stepped on a mine and failed!"
    } else {
        "Congratulations! You found all mines!"
    })
}
