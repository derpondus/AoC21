package year2023

import kotlin.math.max

fun day6(inputLines: List<String>) {
    val p1Times = inputLines[0].split("""\s+""".toRegex()).drop(1).map { it.toLong() }
    val p1Dists = inputLines[1].split("""\s+""".toRegex()).drop(1).map { it.toLong() }
    val p1Races = p1Times.zip(p1Dists).map { D6Race(it.first, it.second) }

    val p1Result = p1Races
        .map { race -> race.genDistRange().filter { it > race.maxDist }.toList().size }
        .reduce { a: Int, b: Int -> a * b }

    val p2Time = inputLines[0].split("""\s+""".toRegex()).drop(1).joinToString("").toLong()
    val p2Dist = inputLines[1].split("""\s+""".toRegex()).drop(1).joinToString("").toLong()
    val p2Race = D6Race(p2Time, p2Dist)

    var p2counter = 0
    p2Race.genDistRange().filter { it > p2Race.maxDist }.forEach { _ -> p2counter++ }

    println("[Part1] Value is: $p1Result")
    println("[Part2] Value is: $p2counter")
}

data class D6Race(
    val time: Long,
    val maxDist: Long,
) {
    private fun dist(holdTime: Long): Long = max(0, time - max(0, holdTime)) * holdTime

    fun genDistRange(): Sequence<Long> = sequence { (0..time).map { yield(dist(it)) } }
}