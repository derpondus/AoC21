package year2025

enum class Direction {
    LEFT,
    RIGHT
}

data class Move(
    val direction: Direction,
    val amount: Int,
) {
    fun toVal() = if (direction == Direction.LEFT) -amount else amount
}



fun day1(inputLines: List<String>) {
    val data = inputLines
        .map { Move(
            if (it[0] == 'L') Direction.LEFT else Direction.RIGHT,
            it.substring(1).toInt()
        ) }
        .toList()

    var zeroCounterPt1 = 0
    var zeroCounterPt2 = 0

    data.fold(50) { pos, move: Move ->
        var newPos = Math.floorMod(pos + move.toVal(), 100)
        if (newPos < 0) newPos += 100

        zeroCounterPt2 += move.amount / 100

        val remaining = move.amount % 100
        var position = pos
        if (move.direction == Direction.LEFT) {
            position = (100 - position) % 100
        }
        if (position + remaining >= 100) {
            zeroCounterPt2++
        }

        if (newPos == 0) zeroCounterPt1++

        newPos
    }

    println("[Part1] ZeroCounter: $zeroCounterPt1")
    println("[Part2] ZeroCounter: $zeroCounterPt2")
}

fun main() {
    val testLines = listOf(
        "L68",
        "L30",
        "R48",
        "L5",
        "R60",
        "L55",
        "L1",
        "L99",
        "R14",
        "L82"
    )
    //day1(testLines)
    day1(listOf(
        "R30",
        "R120"
    ))
}