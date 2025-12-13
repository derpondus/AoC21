package year2024

data class CharGrid(
    val values: List<List<Char>>,
) {
    fun findWordAt(word: String, rowIdx: Int, colIdx: Int, dir: Direction): Boolean =
        word.withIndex().all { (i, c) ->
            val (xOffset, yOffset) = dir.offsetForIdx(i)
            charAtOrNull(rowIdx + yOffset, colIdx + xOffset) == c
        }

    fun findWordAt(word: String, rowIdx: Int, colIdx: Int) =
        Direction.entries.filter { findWordAt(word, rowIdx, colIdx, it) }

    fun findAllOfWord(word: String) = values.flatMapIndexed { rowIdx, row ->
        row.flatMapIndexed { colIdx, _ -> findWordAt(word, rowIdx, colIdx).map { Triple(rowIdx, colIdx, it) } }
    }.toSet()

    fun charAtOrNull(rowIdx: Int, colIdx: Int) = values.getOrNull(rowIdx)?.getOrNull(colIdx)
}

enum class Direction(
    val xOffset: Int,
    val yOffset: Int
) {
    UP(0, -1),
    UP_RIGHT(1, -1),
    RIGHT(1, 0),
    DOWN_RIGHT(1, 1),
    DOWN(0, 1),
    DOWN_LEFT(-1, 1),
    LEFT(-1, 0),
    UP_LEFT(-1, -1);

    fun offsetForIdx(idx: Int) = Pair(idx*xOffset, idx*yOffset)
}

fun day4(inputLines: List<String>) {
    val grid = CharGrid(inputLines.map { it.toList() })

    val xmasInstances = grid.findAllOfWord("XMAS")

    println("[Part1] Number of XMAS Instances: ${xmasInstances.size}")
    // 2639 (correct)

    val validDirs = listOf(Direction.UP_RIGHT, Direction.DOWN_RIGHT, Direction.DOWN_LEFT, Direction.UP_LEFT)
    val xOfMasInstances = grid.findAllOfWord("MAS")
        .filter { (_, _, dir) -> dir in validDirs }
        .groupBy { (rowIdx, colIdx, dir) ->
            val (xOffset, yOffset) = dir.offsetForIdx(1)
            Pair(rowIdx + yOffset, colIdx + xOffset)
        }
        .filterValues { it.size > 1 }
    println("[Part2] Number of X-MAS Instances: ${xOfMasInstances.size}")
    // 203 (too low) (searched for XMAS, needed to search for MAS)
    // 1656 (too low)
    // 2005 (correct)
}
