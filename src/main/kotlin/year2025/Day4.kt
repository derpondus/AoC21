package year2025

import kotlin.math.max
import kotlin.math.min

data class GridPos(
    val hasRoll: Boolean,
) {
    var removed: Boolean = false
}

data class Grid(
    val values: List<List<GridPos>>,
) {
    fun getAdjacentPos(rowIdx: Int, colIdx: Int): List<GridPos> {
        val minCol = max(colIdx - 1, 0)
        val maxCol = min(colIdx + 1, values[0].size - 1)

        val out = mutableListOf<GridPos>()
        if (rowIdx > 0) values[rowIdx - 1].slice(minCol..maxCol).forEach { out.add(it) }
        if (minCol == colIdx - 1) out.add(values[rowIdx][minCol])
        if (maxCol == colIdx + 1) out.add(values[rowIdx][maxCol])
        if (rowIdx < values.size - 1) values[rowIdx + 1].slice(minCol..maxCol).forEach { out.add(it) }
        return out
    }

    fun getLonely(): List<GridPos> {
        return values
            .flatMapIndexed { rowIdx, row ->
                val rowSum = row
                    .mapIndexedNotNull { colIdx, pos ->
                        if (!pos.hasRoll || pos.removed) return@mapIndexedNotNull null  // irrelevant
                        val numAdjRolls = getAdjacentPos(rowIdx, colIdx)
                            .map { it.hasRoll && !it.removed }
                            .count { it }
                        if (numAdjRolls < 4) pos  // accessible
                        else null  // not accessible
                    }
                rowSum
            }
    }
}

fun day4(inputLines: List<String>) {
    val grid = Grid(inputLines.map { line -> line.map { GridPos(hasRoll = it == '@') } })

    var lonely = grid.getLonely()

    val pt1Count = lonely.size
    println("[Part1] Accessible Rolls: $pt1Count")
    // 1363 (correct)

    var pt2Count = 0
    while (lonely.isNotEmpty()) {
        pt2Count += lonely.size
        //grid.values.forEach {
        //    println(it.joinToString { if(it.removed) "X" else if (it.hasRoll) "@" else "." })
        //}
        println("Removed: ${lonely.size}, [$pt2Count]")
        lonely.forEach { it.removed = true }
        lonely = grid.getLonely()
    }
    println("[Part2] Eventually Accessible Rolls: $pt2Count")
    // 8184 (correct)
}