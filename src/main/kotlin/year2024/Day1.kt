package year2024

import kotlin.math.abs


fun day1(inputLines: List<String>) {
    val data = inputLines.map { it.split("   ").let { it[0].toInt() to it[1].toInt() } }
    val list1 = data.map { it.first }.sorted()
    val list2 = data.map { it.second }.sorted()

    val sortedData = list1.zip(list2)
    println("[Part1] Total sum is ${sortedData.sumOf { abs(it.first - it.second) }}")

    val multilist2 = list2.fold(mutableMapOf<Int, Int>()) { acc, locationID ->
        acc.apply { put(locationID, getOrDefault(locationID, 0) + 1) }
    }

    val part2 = list1.sumOf { it * multilist2.getOrDefault(it, 0) }
    println("[Part2] Total similarity score is $part2")
}