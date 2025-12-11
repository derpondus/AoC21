package year2025

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
    // 296006754704850 (correct) (16 min)
}

fun countPaths(devices: Map<String, List<String>>, from: String, to: String): Long {
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
