package year2024

import kotlin.math.abs


data class Report(
    val values: List<Int>,
) {
    val direction by lazy { values
        .windowed(2, 1, false)
        .mapNotNull { (a, b) -> SequenceDir.of(a, b) }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }?.key ?: SequenceDir.Up
    }

    val safe by lazy { checkSafety(values) }

    val safeWithDampener by lazy { values.indices.any {
        checkSafety(values.mapIndexedNotNull { idx, i -> if (idx == it) null else i })
    } }

    private fun checkSafety(values: List<Int>) = values
        .windowed(2, 1, false)
        .all { validPair(it[0], it[1]) }

    private fun validPair(a: Int, b: Int): Boolean {
        val notTooFast = abs(a-b) in 1..3
        val validDirection = direction.validSequence(a, b)
        return notTooFast && validDirection
    }

    enum class SequenceDir {
        Up,
        Down;

        companion object {
            fun of(a: Int, b: Int) = entries.firstOrNull { it.validSequence(a, b) }
        }

        fun validSequence(num1: Int, num2: Int) = when(this) {
            Up -> num1 < num2
            Down -> num1 > num2
        }
    }
}

fun day2(inputLines: List<String>) {
    val reports = inputLines.map { Report(it.split(" ").map { it.toInt() }) }

    val pt1Count = reports.count { it.safe }
    println("[Part1] Safe reports: $pt1Count")
    // 598 (correct)

    val pt2Count = reports.count { it.safeWithDampener }
    println("[Part2] Safe reports with dampener: $pt2Count")
    // 634 (correct)
}

fun main() {
    val testLines = listOf(
        "7 6 4 2 1",
        "1 2 7 8 9",
        "9 7 6 2 1",
        "1 3 2 4 5",
        "8 6 4 4 1",
        "1 3 6 7 9",
    )
    day2(testLines)
}