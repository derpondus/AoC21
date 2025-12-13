package year2024

import kotlin.math.abs


fun day3(inputLines: List<String>) {
    val data = inputLines.joinToString("")

    val p1Sum = Regex("mul\\((\\d+),(\\d+)\\)")
        .findAll(data)
        .sumOf { it.groupValues[1].toInt() * it.groupValues[2].toInt() }
    println("[Part1] Sum of Multiplications: $p1Sum")
    // 175015740

    var enabled = true
    val p2Sum = Regex("(mul\\((\\d+),(\\d+)\\))|(do\\(\\))|(don't\\(\\))")
        .findAll(data)
        .sumOf {
            val (mul, num1, num2, doTxt, dontTxt) = it.destructured
            if (mul.isNotEmpty() && enabled) return@sumOf num1.toInt() * num2.toInt()
            else if (doTxt.isNotEmpty()) enabled = true
            else if (dontTxt.isNotEmpty()) enabled = false
            0
        }
    println("[Part2] Sum of enabled Multiplications: $p2Sum")
    // (_) 175015740 (part 1) (part 2 should be smaller or equal)
    // (1) 112272912 (correct)
}

/*
    val data = inputLines.map { it.split(" ")[1].trim().toDouble() }
    println("THIS IS FAKE DATA FROM NOT_DAY_3 !!!")
    println("Data ${(data.sum()/data.size).toString().take(4)}")
 */
