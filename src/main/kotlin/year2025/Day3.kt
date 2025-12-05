package year2025


fun day3(inputLines: List<String>) {
    val pt1Sum2 = inputLines.sumOf { it.findLargest(2) }
    println("[Part1] Output Joltage: $pt1Sum2")
    // 17166 (Correct)

    val pt2Sum = inputLines.sumOf { it.findLargest(12) }
    println("[Part2] Output Joltage: $pt2Sum")
    // 169455656060468 (Wrong, new startIdx is relative to old -> has to be added to old)
    // 169077317650774 (Correct)

}

fun String.findLargest(numOfDigits: Int): Long {
    var out = ""
    var startIdx = 0
    for (i in 1..numOfDigits) {
        //print("$startIdx, $numOfDigits, $i, ${numOfDigits-i}, ")
        val idexedDigit = this.drop(startIdx).dropLast(numOfDigits - i).withIndex().maxByOrNull { it.value.digitToInt() }
        if (idexedDigit == null) throw IllegalArgumentException("Line not long enough?!?: $this, (${this.length}), broke at d$i")
        //println(", ${idexedDigit.index}, ${idexedDigit.value}")
        startIdx += idexedDigit.index + 1
        out += idexedDigit.value.digitToInt()
    }
    return out.toLong()
}

val v2tests = mapOf(
    "987654321111111" to 987654321111,
    "811111111111119" to 811111111119,
    "234234234234278" to 434234234278,
    "818181911112111" to 888911112111
)

fun main() {
    for ((input, result) in v2tests) {
        println("Test: $input")
        val res = input.findLargest(12)
        println("Result: ${result == res}")
    }
}

