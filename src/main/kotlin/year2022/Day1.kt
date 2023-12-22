package year2022



fun day1(inputLines: List<String>) {
    val elfCalories = Day1.prepareData(inputLines).map { it.sum() }.sortedDescending()
    println("[Part1] Max elf calories: ${elfCalories[0]}")
    println("[Part2] elf calories sum: ${elfCalories.subList(0,3).sum()}")
}

object Day1 {
    fun prepareData(inputLines: List<String>): List<List<Int>> {
        val result = mutableListOf<List<Int>>()
        var current = mutableListOf<Int>()
        inputLines.forEach {
            if(current.size != 0 && it.isEmpty()) {
                result.add(current)
                current = mutableListOf()
            }
            else current.add(it.toInt())
        }
        return result
    }
}