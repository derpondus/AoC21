package year2025

import kotlin.math.max
import kotlin.math.min


data class Range(
    val start: Long,
    val end: Long,
) {
    fun contains(value: Long) = value in start..end

    fun overlaps(other: Range) = max(start, other.start) <= min(end, other.end)

    fun hullWith(other: Range) = Range(min(start, other.start), max(end, other.end))

    fun numValues() = end + 1 - start
}

fun day5(inputLines: List<String>) {
    val delimiterIndex = inputLines.indexOfFirst { it.isBlank() }
    val ranges = inputLines.take(delimiterIndex)
        .map {
            val (start, end) = it.split('-')
            Range(start.toLong(), end.toLong())
        }
        .sortedBy { it.start }
    val ids = inputLines.drop(delimiterIndex + 1)
        .map { it.toLong() }

    val normalizedRanges = mutableListOf<Range>()
    val lastRange = ranges.reduce { currentRange, nextRange ->
        if (currentRange.overlaps(nextRange))
            currentRange.hullWith(nextRange)
        else {
            normalizedRanges.add(currentRange)
            nextRange
        }
    }
    normalizedRanges.add(lastRange)

    val p1Count = ids.count { ranges.firstOrNull { range -> range.contains(it) } != null }
    println("[Part1] Number of Fresh: $p1Count")
    // 726 (correct)

    val p2Count = normalizedRanges.sumOf { it.numValues() }
    println("[Part2] Number of Possible Fresh: $p2Count")
    // 354226555270043 (correct)
}

fun main() {
    day5(listOf(
        "3-5",
        "14-15",
        "20-20",
        "",
        "1",
    ))
}