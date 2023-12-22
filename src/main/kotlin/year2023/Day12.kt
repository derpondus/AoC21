package year2023

fun day12(inputLines: List<String>) {
    val parsedData = inputLines
        .map {
            val splitLine = it.split(" ")
            SpringRow(
                splitLine[0].toList(),
                splitLine[1].split(",").map { it.toInt() }
            )
        }

    println(
        parsedData.take(1).map {
            val groupPos = mutableListOf<Int>()
            val posPointer  = 0
            val groupPointer = 0

            while(groupPointer >= 0) {

            }
        }
    )

    val unknown = "UNKNOWN"
    println("[Part1] Value is: ${unknown}")
    println("[Part2] Value is: ${unknown}")
}

data class SpringRow(
    val springs: List<Char>,
    val groups: List<Int>
)