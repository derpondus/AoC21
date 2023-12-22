package year2023

fun day9(inputLines: List<String>) {
    val diffListStacks = inputLines.map {
        val lists = mutableListOf<List<Int>>()
        var currList = it.split(" ").map { it.toInt() }
        while (!currList.all { it == 0 }) {
            lists.add(currList)
            currList = currList.dropLast(1).zip(currList.drop(1)).map { (v1, v2) -> v2 - v1 }
        }
        lists
    }

    val unknown = "UNKNOWN"
    println("[Part1] Value is: ${diffListStacks.sumOf { it.map { it.last() }.reduceRight { v, acc -> v + acc } }}")
    println("[Part2] Value is: ${diffListStacks.sumOf { it.map { it.first() }.reduceRight { v, acc -> v - acc } }}")
}