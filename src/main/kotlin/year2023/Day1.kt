package year2023


fun String.toDigit() = when (this) {
    "one" -> 1
    "two" -> 2
    "three" -> 3
    "four" -> 4
    "five" -> 5
    "six" -> 6
    "seven" -> 7
    "eight" -> 8
    "nine" -> 9
    else -> this.elementAt(0).digitToInt()
}

fun day1(inputLines: List<String>) {
    val simple = inputLines.stream()
        .map { it.find { it.isDigit() }?.digitToInt()!! * 10 + it.findLast { it.isDigit() }?.digitToInt()!! }
        .toList().sum()

    val advanced = inputLines.stream()
        .map { """(?=(one|two|three|four|five|six|seven|eight|nine|\d))""".toRegex().findAll(it).run {
            this.first().groups[1]!!.value.toDigit() * 10 + this.last().groups[1]!!.value.toDigit()
        } }
        .toList().sum()

    println("[Part1] Simple sum is: $simple")
    println("[Part2] Advanced sum is: $advanced")
}