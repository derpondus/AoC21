package year2025

import kotlin.collections.map
import kotlin.collections.mutableListOf

fun day6(inputLines: List<String>) {
    val operators = inputLines.last().trim().split("\\s+".toRegex())
    val numbersP1 = inputLines.dropLast(1)
        .fold(operators.map { mutableListOf<Long>() }) { out, line ->
            line
                .trim()
                .split("\\s+".toRegex())
                .map { it.toLong() }
                .forEachIndexed { idx, num -> out[idx].add(num) }
            out
        }

    val p1Sum = operators.zip(numbersP1)
        .sumOf { (op, nums) ->
        when (op) {
            "+" -> nums.sum()
            "*" -> nums.reduce { acc, num -> acc * num }
            else -> throw IllegalStateException("Unknown op: $op")
        }
    }

    println("[Part1] Sum of results: $p1Sum")
    // 5595593539811 (correct)

    val numbersP2 = inputLines.dropLast(1)
        .foldIndexed(inputLines.first().map { CharArray(inputLines.size-1) }) { lineIdx, out, line ->
            line.mapIndexed { charIdx, c -> out[charIdx][lineIdx] = c }
            out
        }
        .map { it.joinToString("") }
        .fold(mutableListOf(mutableListOf<Long>())) { out, numStr ->
            if (numStr.isBlank()) out.add(mutableListOf())
            else {
                out.last().add(numStr.trim().toLong())
            }
            out
        }

    val p2Sum = operators.zip(numbersP2)
        .sumOf { (op, nums) ->
            when (op) {
                "+" -> nums.sum()
                "*" -> nums.reduce { acc, num -> acc * num }
                else -> throw IllegalStateException("Unknown op: $op")
            }
        }

    println("[Part2] Sum of results: $p2Sum")
    // 10153315705125 (correct)
}
