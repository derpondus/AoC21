package year2023

import kotlin.math.min

fun day5(inputLines: List<String>) {
    val seedsP1 = inputLines[0].split(" ").drop(1).map { it.trim().toLong() }
    val seedsP2 = seedsP1.chunked(2)

    val converters: MutableList<D5Converter> = mutableListOf()
    var currentConverter: D5Converter? = null
    for (line in inputLines.drop(2)) {
        if (line.matches("""[a-zA-Z].*""".toRegex())) currentConverter = D5Converter(line.split(" ")[0])
        if (line.matches("""\d.*""".toRegex())) currentConverter!!.addRange(line.split(" ").map { it.toLong() })
        if (line.isBlank()) converters.add(currentConverter!!)
    }
    converters.add(currentConverter!!)

    val absValPerPercent = seedsP2.sumOf { it[1] } / 10

    println("[Bruteforcing P2 ...]")
    println("[1234567890]")
    print("[")
    val p2Result = d5p2numbers(seedsP2)
        .onEach { if (it % absValPerPercent == 0L) print("#") }
        .fold(Long.MAX_VALUE) { minVal, it -> min(minVal, converters.fold(it) { seed, conv -> conv.map(seed) }) }
    println("]")

    println("[Part1] Value is: ${seedsP1.minOfOrNull { converters.fold(it) { seed, conv -> conv.map(seed) } }}")
    println("[Part2] Value is: $p2Result")
}

fun d5p2numbers(seeds: List<List<Long>>) = sequence {
    for (seedList in seeds) {
        yieldAll(seedList[0]..seedList[0] + seedList[1])
    }
}

// Convenience for parsing input
fun D5Converter.addRange(list: List<Long>) = this.addRange(list[1], list[0], list[2])

data class D5Converter(
    val name: String
) {
    private val ranges: MutableList<Pair<LongRange, Long>> = mutableListOf()

    fun addRange(sourceStart: Long, targetStart: Long, length: Long) {
        ranges.add((sourceStart until sourceStart + length) to (targetStart - sourceStart))
    }

    fun map(input: Long): Long = input + (ranges.find { it.first.contains(input) }?.second ?: 0)
}