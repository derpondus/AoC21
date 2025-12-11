package year2025

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Pos2D(val x: Long, val y: Long)
data class Line2D(val pos1: Pos2D, val pos2: Pos2D) {
    val xRange by lazy { min(pos1.x, pos2.x)..max(pos1.x, pos2.x) }
    val yRange by lazy { min(pos1.y, pos2.y)..max(pos1.y, pos2.y) }
    val alignment by lazy { if (pos1.x == pos2.x) Alignment.Vertical else Alignment.Horizontal }

    fun toSequence(): Sequence<Pos2D> = sequence {
        when (alignment) {
            Alignment.Vertical -> yieldAll(yRange.map { Pos2D(pos1.x, it) })
            Alignment.Horizontal -> yieldAll(xRange.map { Pos2D(it, pos1.y) })
        }
    }

    enum class Alignment {
        Horizontal,
        Vertical
    }
}
data class Rect2D(val pos1: Pos2D, val pos2: Pos2D) {
    val minCorner by lazy { Pos2D(min(pos1.x, pos2.x), min(pos1.y, pos2.y)) }
    val maxCorner by lazy { Pos2D(max(pos1.x, pos2.x), max(pos1.y, pos2.y)) }
    val area by lazy { (abs(pos1.x - pos2.x) + 1) * (abs(pos1.y - pos2.y) + 1) }

    /** Border is false **/
    fun containsInner(pos: Pos2D): Boolean {
        val innerXRange = minCorner.x + 1 until maxCorner.x
        val isInXRange = pos.x in innerXRange

        val innerYRange = minCorner.y + 1 until maxCorner.y
        val isInYRange = pos.y in innerYRange

        return isInXRange && isInYRange
    }
}

fun day9(inputLines: List<String>) {
    val positions = inputLines
        .map { it.split(",") }
        .map { Pos2D(it[0].toLong(), it[1].toLong()) }
    val lines = (positions + positions.first())
        .zipWithNext { pos1, pos2 -> Line2D(pos1, pos2) }

    val rects = positions
        .flatMapIndexed { idx, pos1 -> positions.drop(idx + 1).map { pos2 -> Rect2D(pos1, pos2) } }
        .sortedByDescending { it.area }
    println(rects.map { "$it, ${it.area}" })

    println("[Part1] Largest unconstrained area: ${rects.first().area}")
    // 4740017887 (too low) (didn't account for BOTH positions to be inclusive)
    // 4740155680 (correct)

    val p2Rects = rects.filterIndexed { idx, rect ->
        val progressPercent = (idx.toDouble() / rects.size.toDouble() * 100).toInt()
        print("Progress: [${"-".repeat(progressPercent)}${" ".repeat(100-progressPercent)}] ($idx/${rects.size})\r")
        val noLineInside = lines.asSequence()
            .flatMap { it.toSequence() }
            .none { rect.containsInner(it) }

        noLineInside
    }
    println("Progress: [${"-".repeat(100)}] (${rects.size}/${rects.size})")

    println("[Part2] Largest constrained area: ${p2Rects.firstOrNull()?.area}")
    println("Sanity-Check: ${p2Rects.maxByOrNull { it.area }?.area}")
    // (_) 4740155680 (pt1 comparison (pt2 needs to be smaller))
    // (1) 274273475  (too low)
    // (2) 1543501936 (correct in 00:07:30.625738800) (even though I haven't checked being "inside" of the polygon)
}

val testCases = mapOf(
    listOf("0,0", "10,0", "10,4", "15,4", "15,6", "10,6", "10,10", "0,10") to 121,
    listOf("1,0", "3,0", "3,6", "16,6", "16,0", "18,0", "18,9", "13,9", "13,7", "6,7", "6,9", "1,9") to 30,
    listOf("2,2", "5,2", "5,0", "7,0", "7,2", "9,2", "9,6", "7,6", "7,10", "4,10", "4,12", "2,12", "2,10", "0,10", "0,6") to 54,
    listOf("0,3", "0,0", "10,0", "10,10", "0,10", "0,7", "6,7", "6,3") to 44
)

fun main() {
    val rect= Rect2D(Pos2D(1,0), Pos2D(16,6))
    val pos = Pos2D(3,0)
    rect.containsInner(pos)

    for ((input, output) in testCases) {
        day9(input)
        println("Correct output: $output")
    }
}
