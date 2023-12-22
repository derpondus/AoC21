package year2022

import year2022.Day2.p1LineScore
import year2022.Day2.p2LineScore

fun day2(inputLines: List<String>) {
    println("[Part1] Sum of own points for inferred Strategy: ${inputLines.sumOf { p1LineScore(it) }}")
    println("[Part2] Sum of own points for actual Strategy: ${inputLines.sumOf { p2LineScore(it) }}")
}
object Day2 {
    private fun playScore(letter: String) = when(letter) {
        "X" -> 1
        "Y" -> 2
        "Z" -> 3
        else -> throw Error("Letter must be one of XYZ but was $letter")
    }

    private fun winScore(letter: String) = when(letter) {
        "X" -> 0
        "Y" -> 3
        "Z" -> 6
        else -> throw Error("Letter must be one of XYZ but was $letter")
    }

    fun p1LineScore(line: String) = playScore(line.split(" ")[1]) + when(line) {
        "A Z", "B X", "C Y" -> 0
        "A X", "B Y", "C Z" -> 3
        "A Y", "B Z", "C X" -> 6
        else -> throw Error("Line must consist of Letters from ABCXYZ but was $line")
    }

    fun p2LineScore(line: String) = winScore(line.split(" ")[1]) + when(line) {
        "A Y", "B X", "C Z" -> 1
        "A Z", "B Y", "C X" -> 2
        "A X", "B Z", "C Y" -> 3
        else -> throw Error("Line must consist of Letters from ABCXYZ but was $line")
    }
}