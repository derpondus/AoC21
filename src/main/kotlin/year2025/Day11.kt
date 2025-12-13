package year2025

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

fun day11(inputLines: List<String>) {
    val devices = inputLines.associate {
        val (name, rest) = it.split(": ")
        name to rest.split(" ")
    }

    val youToOutPathCount = countPaths(devices, "you", "out")
    println("[Part1] Number of Paths: $youToOutPathCount")
    // 494 (correct)

    println("----- ----- ----- ----- -----")

    val svrToDacPathCount = countPaths(devices, "svr", "dac")
    val svrToFftPathCount = countPaths(devices, "svr", "fft")
    val dacToFftPathCount = countPaths(devices, "dac", "fft")
    val fftToDacPathCount = countPaths(devices, "fft", "dac")
    val dacToOutPathCount = countPaths(devices, "dac", "out")
    val fftToOutPathCount = countPaths(devices, "fft", "out")

    val dacThenFftPathCount = svrToDacPathCount * dacToFftPathCount * fftToOutPathCount
    val fftThenDacPathCount = svrToFftPathCount * fftToDacPathCount * dacToOutPathCount
    val p2PathCount = dacThenFftPathCount + fftThenDacPathCount
    println("[Part2] Number of Paths: $p2PathCount")
    // 29690647128450 (too low) (done with calculator as I added the parts instead of multiplying)
    // 296006754704850 (correct) (~ 16 min - approach 1)
    // 296006754704850 (correct) (~ 25 ms - approach 2)
}

const val useApproach = 1
fun countPaths(devices: Map<String, List<String>>, from: String, to: String) = when(useApproach) {
    1 -> countPaths1(devices, from, to)
    2 -> countPaths2(devices, from, to)
    else -> throw IllegalArgumentException("Invalid approach")
}

fun countPaths1(devices: Map<String, List<String>>, from: String, to: String): Long {
    val openPointers = devices.keys.associateWith { 0L }.toMutableMap()
    openPointers[from] = 1L
    var arrivedCount = 0L
    var loopVar = 0
    while (true) {
        if (loopVar == 0) {
            print("        $from -> $to : ${openPointers.values.sum()}\r")
            loopVar = 10000
        } else loopVar--

        val (dId, numPaths) = openPointers.firstNotNullOfOrNull { if (it.value > 0) it else null } ?: break
        openPointers[dId] = 0
        val nextDevices = devices[dId]!!
        if (nextDevices.contains(to)) arrivedCount += numPaths
        nextDevices.filterNot { it == to || it == "out" }.forEach { openPointers[it] = openPointers[it]!! + numPaths }
    }
    println("        $from -> $to : $arrivedCount")
    return arrivedCount
}

fun countPaths2(devices: Map<String, List<String>>, from: String, to: String): Long {
    val modDevices = devices.toMutableMap()
    modDevices["out"] = listOf("out")
    modDevices[to] = listOf(to)

    val pointerState = (0L until devices.size + 1)
        .fold(
            mapOf(from to 1L)
        ) { acc, _ -> acc
            .flatMap { (dId, count) -> modDevices[dId]!!.map { it to count } }
            .groupBy { it.first }
            .mapValues { it.value.sumOf { it.second } }
        }
    println("        $from -> $pointerState[$to]")
    return pointerState[to] ?: 0
}

fun main() {
    println("----- ----- ----- ----- -----")
    println("Running Justus' Input")
    println("----- ----- ----- ----- -----")
    val inputFile = File("./src/main/resources/event_2025/day11_justus.txt")
    val lines = with(BufferedReader(FileReader(inputFile))) { readLines() }
    day11(lines)
    // 268938231075584 (by justus algo: too low)
    // 417190406827152 (correct)
}
