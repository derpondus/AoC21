package year2023

import kotlin.math.abs
import kotlin.math.max

fun day11(inputLines: List<String>) {
    val skyImage = inputLines.map { it.toList() }

    val emptyRows = mutableListOf<Int>()
    val emptyColumns = mutableListOf<Int>()
    skyImage.forEachIndexed { i, it -> if (it.filterNot { it == '.' }.isEmpty()) emptyRows.add(i) }


    for (j in 0 until skyImage[0].size) {
        var empty = true
        for (row in skyImage) {
            if (row[j] != '.') empty = false
        }
        if (empty) emptyColumns.add(j)
    }

    val sky = skyImage/*.flatMapIndexed { i, row ->
        val outRow = row.flatMapIndexed { j, char -> if (j in emptyColumns) listOf(char, char) else listOf(char) }
        if (i in emptyRows) listOf(outRow, outRow) else listOf(outRow)
    }*/

    val galaxies = mutableListOf<Pair<Int, Int>>()
    sky.forEachIndexed { i, row -> row.forEachIndexed { j, char -> if (char == '#') galaxies.add(i to j) } }

    fun task(rowMult: Long, colMult: Long): Long {
        var sum = 0L
        for ((index, galaxy) in galaxies.withIndex()) {
            for (galaxy2 in galaxies.slice(index until galaxies.size)) {
                val emptyRowsOnLine = emptyRows.filter { it in galaxy.first..galaxy2.first || it in galaxy2.first..galaxy.first }.size
                val emptyColsOnLine = emptyColumns.filter { it in galaxy.second..galaxy2.second || it in galaxy2.second..galaxy.second  }.size
                sum += (galaxy2 - galaxy).length() + emptyRowsOnLine * max(0, (rowMult - 1)) + emptyColsOnLine * max(0, (colMult - 1))
            }
        }
        return sum
    }


    /*sky.forEach {
        it.forEach {
            print(it)
        }
        println()
    }*/

    val unknown = "UNKNOWN"
    println("[Part1] Value is: ${task(2, 2)}") // 9274989
    println("[Part2] Value is: ${task(1_000_000, 1_000_000)}")
}

operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = (first - other.first) to (second - other.second)
fun Pair<Int, Int>.length() = abs(first) + abs(second)