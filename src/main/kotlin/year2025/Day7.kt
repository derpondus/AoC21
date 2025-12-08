package year2025

fun day7(inputLines: List<String>) {
    var p1Count = 0
    val outBeams = inputLines.drop(1)
        .fold(inputLines.first().map { if (it == 'S') 1L else 0L }.toMutableList()) { out, line ->
            line.forEachIndexed { index, c ->
                if (c == '^' && out[index] > 0) {
                    out[index - 1] += out[index]
                    out[index + 1] += out[index]
                    out[index] = 0
                    p1Count++
                }
            }
            out
        }
    val p2Count = outBeams.sum()

    println("[Part1] Number of beams: $p1Count")
    // 82 (incorrect) (should have counted the splits instead of resulting beams)
    // 1628 (correct)

    println("[Part1] Number of timelines: $p2Count")
    // 27055852018812 (correct)
}
