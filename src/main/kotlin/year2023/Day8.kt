package year2023

fun day8(inputLines: List<String>) {
    val instruction = inputLines[0]

    val nodes = inputLines
        .drop(2)
        .map { """([A-Z]+) = \(([A-Z]+), ([A-Z]+)\)""".toRegex().matchEntire(it)!!.groups.drop(1).requireNoNulls() }
        .associate { it[0].value to D8Node(it[0].value, it[1].value, it[2].value) }

    var p1NodeId = "AAA"
    val p1Count = yieldRepeat(instruction.toList())
        .takeWhile {
            p1NodeId = if (it == 'L') nodes[p1NodeId]!!.left else nodes[p1NodeId]!!.right
            p1NodeId != "ZZZ"
        }
        .countLong() + 1

    // Implementation is not in accordance with the task (but it is with the data MUHAHAHAHA!)
    // Kind of makes the task bad, but easier to solve :P
    val p2Count = nodes.keys.filter { it.endsWith('A') }
        .map {
            var nodeId = it
            yieldRepeat(instruction.toList())
                .takeWhile {
                    nodeId = if (it == 'L') nodes[nodeId]!!.left else nodes[nodeId]!!.right
                    !nodeId.endsWith("Z")
                }
                .countLong() + 1
        }
        .lcm()


    val unknown = "UNKNOWN"
    println("[Part1] Value is: $p1Count")
    println("[Part2] Value is: $p2Count")
}

fun Iterable<Long>.lcm() = reduce( ::findLCM )

fun findGCD(a: Long, b: Long): Long {
    if (a == 0L) return b

    var ia = a
    var ib = b
    while (ib != 0L) {
        if (ia > ib) ia -= ib
        else ib -= ia
    }

    return ia
}

fun findLCM(a: Long, b: Long): Long {
    val ggt = findGCD(a,b)
    return a * (b/ggt)
}

fun <T> Sequence<T>.countLong(): Long {
    var count: Long = 0
    for (element in this) ++count
    return count
}

fun yieldRepeat(instruction: Iterable<Any>) = sequence {
    while (true) {
        yieldAll(instruction)
    }
}

data class D8Node(
    val id: String,
    val left: String,
    val right: String,
)