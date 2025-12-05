package year2024

import kotlin.math.abs


fun day2(inputLines: List<String>) {
    val testLines = listOf(
        "72 74 75 78 81 84 85"
    )
    val lines  = testLines
    val reports = lines.map { it.split(" ").map { it.toInt() } }


    val part1Results = reports.map { report ->
        report.windowed(2, 1, false) { (a, b) ->
            val change = b-a
            if (abs(change) > 3) 0
            else if (change > 0) 1
            else if (change < 0) -1
            else 0
        }.sum() == report.size - 1
    }
    val part1Valid = part1Results.count { it }
    println("[Part1] Valid reports: $part1Valid")



    println("[INFO] Lines in data: ${reports.size}")
    println("[INFO] Data is \n$reports")
}