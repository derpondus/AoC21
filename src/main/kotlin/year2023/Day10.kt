package year2023

import kotlin.math.ceil

fun day10(inputLines: List<String>) {
    val pipeMap = inputLines.map { it.toList() }

    // Expanding
    val pipeMap2 = mutableListOf<MutableList<Char>>()
    pipeMap.forEach {
        pipeMap2.addAll(listOf(mutableListOf(), mutableListOf(), mutableListOf()))
        it.forEach {
            val dirs = it.directions()
            val up = if (dirs.contains(Direction.UP)) '|' else '.'
            val left = if (dirs.contains(Direction.LEFT)) '-' else '.'
            val right = if (dirs.contains(Direction.RIGHT)) '-' else '.'
            val down = if (dirs.contains(Direction.DOWN)) '|' else '.'

            pipeMap2[pipeMap2.size - 3].addAll(listOf('.', up, '.'))
            pipeMap2[pipeMap2.size - 2].addAll(listOf(left, it, right))
            pipeMap2[pipeMap2.size - 1].addAll(listOf('.', down, '.'))
        }
    }

    /*pipeMap2.forEach {
        it.forEach { print(it) }
        println()
    }*/

    val startPos = Pair(
        pipeMap2.indexOfFirst { it.contains('S') },
        pipeMap2.find { it.contains('S') }!!.indexOfFirst { it == 'S' }
    )

    val loop = mutableListOf(startPos)

    val possibleStartDir = Direction.entries.find { (startPos + it.relPos).getChar(pipeMap2).directions().contains(it.other()) }!!

    // Discovery
    println("Discovering inside space - will take a minute")
    print("|")
    var currPos = startPos + possibleStartDir.relPos
    var lastDirection: Direction = possibleStartDir
    while (currPos != startPos) {
        loop.add(currPos)
        lastDirection = currPos.getChar(pipeMap2).directions().minus(lastDirection.other())[0]
        currPos += lastDirection.relPos
    }

    val inValue = loop.minBy { it.first } + Direction.RIGHT.relPos + Direction.DOWN.relPos

    val marked = mutableListOf(inValue)
    val inside = mutableListOf<Pair<Int,Int>>()
    var counter = 0
    while (marked.isNotEmpty()) {
        val curr = marked.removeFirst()
        inside.add(curr)
        marked.addAll(Direction.entries
            .map { curr + it.relPos }
            .filterNot { loop.contains(it) }
            .filterNot { marked.contains(it) }
            .filterNot { inside.contains(it) }
        )
        counter++
        if (counter % 1000 == 0) print("-")
    }
    println("|")

    val unknown = "UNKNOWN"
    println("[Part1] Value is: ${ceil(((loop.size.toDouble() / 3) / 2)).toInt()}")
    println("[Part2] Value is: ${inside.filter { ((it.first - 1) % 3 == 0) && ((it.second - 1) % 3 == 0) }.size}")
}

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = Pair(
    this.first + other.first,
    this.second + other.second,
)

fun Pair<Int, Int>.getChar(map: List<List<Char>>) = map[first][second]

fun Char.directions() = when (this) {
    '|' -> listOf(Direction.UP, Direction.DOWN)
    '-' -> listOf(Direction.LEFT, Direction.RIGHT)
    'L' -> listOf(Direction.UP, Direction.RIGHT)
    'J' -> listOf(Direction.UP, Direction.LEFT)
    '7' -> listOf(Direction.LEFT, Direction.DOWN)
    'F' -> listOf(Direction.RIGHT, Direction.DOWN)
    '.' -> listOf()
    'S' -> listOf(Direction.UP, Direction.DOWN) // listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
    else -> throw Exception("WTF, unknown char,")
}

enum class Direction(
    val relPos: Pair<Int, Int>
) {
    UP(-1 to 0),
    DOWN(1 to 0),
    LEFT(0 to -1),
    RIGHT(0 to 1);

    fun other() = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}