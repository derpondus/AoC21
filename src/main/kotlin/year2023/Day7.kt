package year2023

fun day7(inputLines: List<String>) {
    val preppedData = inputLines
        .map { it.split(" ") }
        .map { D7Hand(it[0], it[1].toInt()) }

    val p1Result = preppedData
        .sortedBy { it.p1Valuation }
        .onEachIndexed { index, hand -> hand.rank = index + 1 }
        .sumOf { it.bid * it.rank!! }

    val p2Result = preppedData
        .sortedBy { it.p2Valuation }
        .onEachIndexed { index, hand -> hand.rank = index + 1 }
        .sumOf { it.bid * it.rank!! }

    println("[Part1] Value is: $p1Result")
    println("[Part2] Value is: $p2Result")
}

data class D7Hand(
    val hand: String,
    val bid: Int,
)  {
    var rank: Int? = null

    private val p1SetWiseValueStr = hand
        .groupBy { it }
        .map { it.value.size }
        .sortedDescending()
        .take(2)
        .joinToString("")
        .padEnd(2, '0')

    private val p1CardWiseValueStr = hand
        .replace('2', '0')
        .replace('3', '1')
        .replace('4', '2')
        .replace('5', '3')
        .replace('6', '4')
        .replace('7', '5')
        .replace('8', '6')
        .replace('9', '7')
        .replace('T', '8')
        .replace('J', '9')
        .replace('A', 'C')
        .replace('K', 'B')
        .replace('Q', 'A')

    val p1Valuation = (p1SetWiseValueStr + p1CardWiseValueStr).toInt(13)

    // ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

    private val p2SetWiseValueStr = hand
        .groupBy { it }
        .let {
            val jokerNum = it['J']?.size

            it.toMutableMap()
                .apply { this['J'] = listOf() }
                .map { it.value.size }
                .sortedDescending()
                .toMutableList()
                .apply { this[0] += jokerNum ?: 0 }
        }
        .take(2)
        .joinToString("")
        .padEnd(2, '0')

    private val p2CardWiseValueStr = hand
        .replace('J', '0')
        .replace('2', '1')
        .replace('3', '2')
        .replace('4', '3')
        .replace('5', '4')
        .replace('6', '5')
        .replace('7', '6')
        .replace('8', '7')
        .replace('9', '8')
        .replace('T', '9')
        .replace('A', 'C')
        .replace('K', 'B')
        .replace('Q', 'A')

    val p2Valuation = (p2SetWiseValueStr + p2CardWiseValueStr).toInt(13)
}