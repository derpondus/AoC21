package year2023

fun day3(inputLines: List<String>) {
    val p1Result = mutableSetOf<Pair<Int, Int>>()
    val p2Result = mutableListOf<Int>()

    val around = mutableListOf(
        -1 to -1,
        -1 to 0,
        -1 to 1,
        0 to -1,
        //0 to 0,
        0 to 1,
        1 to -1,
        1 to 0,
        1 to 1,
    )

    inputLines.forEachIndexed { lineIndex, line ->
        line.forEachIndexed { charIndex, char ->
            if (!char.isDigit() && char != '.') {
                around.forEach {
                    if (inputLines[lineIndex + it.first][charIndex + it.second].isDigit()) {
                        var xOffset = 1
                        while (charIndex + it.second - xOffset >= 0 && inputLines[lineIndex + it.first][charIndex + it.second - xOffset].isDigit()) {
                            xOffset++
                        }
                        p1Result.add((lineIndex + it.first) to (charIndex + it.second - xOffset + 1))
                    }

                }
            }
            if (char == '*') {
                val numberLoc = mutableSetOf<Pair<Int,Int>>()
                around.forEach {
                    if (inputLines[lineIndex + it.first][charIndex + it.second].isDigit()) {
                        var xOffset = 1
                        while (charIndex + it.second - xOffset >= 0 && inputLines[lineIndex + it.first][charIndex + it.second - xOffset].isDigit()) {
                            xOffset++
                        }
                        numberLoc.add((lineIndex + it.first) to (charIndex + it.second - xOffset + 1))
                    }
                }
                if (numberLoc.size == 2) {
                    val numbers = numberLoc.map {
                        var number = ""
                        var xPos = it.second
                        do {
                            number += inputLines[it.first][xPos]
                            xPos++
                        } while (xPos < inputLines[0].length && inputLines[it.first][xPos].isDigit())
                        number.toInt()
                    }

                    p2Result.add(numbers.reduce { it1, it2 -> it1 * it2 })
                }
            }
        }
    }

    val p1ResValue = p1Result.sumOf {
        var number = ""
        var xPos = it.second
        do {
            number += inputLines[it.first][xPos]
            xPos++
        } while (xPos < inputLines[0].length && inputLines[it.first][xPos].isDigit())
        number.toInt()
    }


    println("[Part1] Value is: $p1ResValue")
    println("[Part2] Value is: ${p2Result.sum()}")
}