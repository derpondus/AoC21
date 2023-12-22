package year2023

fun day2(inputLines: List<String>) {
    val preppedData = inputLines
        .map { it.split(": ") }
        .map { it[0].split(" ")[1].toInt() to it[1].split("; ")
                .map { it.split(", ")
                    .map { it.split(" ") }
                    .map { it[0].toInt() to it[1] }
                }
        }

    val task1Value = preppedData
        .filter {
            it.second.flatten().all {
                it.first <= when (it.second) {
                    "red" -> 12;
                    "green" -> 13;
                    "blue" -> 14
                    else -> throw Error("Unknown color ${it.second}")
                }
            }

        }
        .sumOf { it.first }

    val task2Value = preppedData.sumOf {
        it.second.flatten()
            .groupBy { it.second }
            .map {
                it.value.maxOf { it.first }
            }
            .reduce { a: Int, b: Int -> a * b }
    }


    println("[Part1] Value is: $task1Value")
    println("[Part2] Value is: $task2Value")
}