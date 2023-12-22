package year2023

import kotlin.math.pow

fun day4(inputLines: List<String>) {
    val preppedData = inputLines
        .map { """Card\s+(\d+):([\d\s]+) \| ([\d\s]+)""".toRegex().matchEntire(it) }
        .map { it?.groupValues?.run {
            Card(
                this[1].toInt(),
                this[2].trim().split("""\s+""".toRegex()).map { it.toInt() },
                this[3].trim().split("""\s+""".toRegex()).map { it.toInt() },
            )
        } }
        .requireNoNulls()
        .onEach { card -> card.winningCount = card.winningNumbers.filter { card.myNumbers.contains(it) }.size }

    for ((index, card) in preppedData.withIndex()) {
        for (aheadIndex in (index + 1) until (index + 1 + card.winningCount)) {
            preppedData[aheadIndex].count += card.count
        }
    }

    println("[Part1] Value is: ${preppedData.sumOf { if (it.winningCount == 0) 0 else 2.0.pow(it.winningCount.toDouble() - 1).toInt() }}")
    println("[Part2] Value is: ${preppedData.sumOf { it.count }}")
}

data class Card(
    var number: Int,
    var winningNumbers: List<Int>,
    var myNumbers: List<Int>,
) {
    var winningCount: Int = 0
    var count: Int = 1
}