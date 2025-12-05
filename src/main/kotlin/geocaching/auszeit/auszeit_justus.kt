package geocaching.auszeit

import year2023.toDigit
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.floor

private fun loadData(): List<String> {
    val inputFile = File("./src/main/resources/geocaching/auszeit.txt")
    inputFile.parentFile.mkdirs()
    if(!inputFile.exists()) throw IllegalArgumentException("File not found: ${inputFile.absolutePath}")
    return BufferedReader(FileReader(inputFile)).readLines()
}

enum class Direction(val x: Int, val y: Int) {
    N(-1, 0),
    NE(-1, 1),
    E(0, 1),
    SE(1, 1),
    S(1, 0),
    SW(1, -1),
    W(0, -1),
    NW(-1, -1)
}

fun main() {
    val inputLines = loadData()
        .map { it.split("") }
        .map { it.subList(1, it.size - 2) }
        .map { it.map { try { it.toInt() } catch (e: NumberFormatException) { -1 }} }
    for (line in inputLines) {
        println(line)
    }

    val size = inputLines.size
    println()
    var locations = mutableListOf(Pair((size/2), (size/2)))

    do {
        val newLocations = mutableListOf<Pair<Int, Int>>()
        for (location in locations) {
            val x = location.first
            val y = location.second
            val value = inputLines[x][y]
            if (value == -1) {
                continue
            }
            val direction = Direction.entries[value]
            val newX = x + direction.x
            val newY = y + direction.y
            newLocations.add(Pair(newX, newY))
        }
        locations = newLocations
    } while (locations.isNotEmpty())


}
