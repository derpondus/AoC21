import Dot.Companion.toDot
import FoldInstruction.Companion.toFoldInstruction
import java.io.BufferedReader
import java.io.FileReader
import java.lang.reflect.Modifier
import java.time.LocalDate
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

val daysToRun: List<Int> = listOf(13)

//region main
@ExperimentalTime
fun main(args: Array<String>) {
    (
            if(args.isNotEmpty()) args.map { it.toInt() }
            else if(daysToRun.isNotEmpty()) daysToRun
            else listOf( LocalDate.now().dayOfMonth )
    )
    .map { it.coerceIn(1..24) }
    .forEach {
        println("-".repeat(50))
        println("Day $it")
        val func: KFunction<*>? = getFunctionFromFile("Main", "day$it")
            ?: getFunctionFromFile("Day$it", "day$it")
            ?: run { println("Method not found"); null }
        val time = measureTime {
            func?.call()
        }
        println(time.toComponents { hours, minutes, seconds, nanoseconds -> "Exec Time: " +
                "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:" +
                "${seconds.toString().padStart(2, '0')}.${nanoseconds.toString().padStart(9, '0')}"
        })
        println()
    }
}

fun getFunctionFromFile(fileName: String, funcName: String): KFunction<*>? {
    val selfRef = ::getFunctionFromFile
    val currentClass = selfRef.javaMethod!!.declaringClass
    val classDefiningFunctions = currentClass.classLoader.loadClass("${fileName}Kt")
    val javaMethod  = classDefiningFunctions.methods.find { it.name == funcName && Modifier.isStatic(it.modifiers)}
    return javaMethod?.kotlinFunction
}

//endregion
//region Day1

fun day1() {
    val inputLines = BufferedReader(FileReader("./src/main/resources/day1.txt")).readLines()

    println("Part1: ${calcDay1(inputLines, 1)}")
    println("Part2: ${calcDay1(inputLines, 3)}")
}

fun calcDay1(inputLines: List<String>, windowSize: Int = 1): Int {
    var counter = 0
    for (i in 0 until inputLines.size-windowSize) {
        if(inputLines.subList(i, i+windowSize).sumOf { it.toInt() } < inputLines.subList(i+1, i+windowSize+1).sumOf { it.toInt() }) counter++
    }
    return counter
}

//endregion
//region Day2

fun day2() {
    val inputLines = BufferedReader(FileReader("./src/main/resources/day2.txt")).readLines().map { it.split(" ") }

    calcPath(inputLines, true)
    calcPath(inputLines, false)
}

fun calcPath(inputLines: List<List<String>>, part1: Boolean = true) {
    var aim: Int = 0
    var depth: Int = 0
    var position: Int = 0

    inputLines.forEach { when(it[0]) {
        "forward" -> {
            position += it[1].toInt()
            if(!part1) depth += aim*it[1].toInt()
        }
        "down" -> if(part1) depth += it[1].toInt() else aim += it[1].toInt()
        "up" -> if(part1) depth -= it[1].toInt() else aim -= it[1].toInt()
        else -> println("${if(part1) 1 else 2}Else -> $it")
    } }

    println("Part ${if(part1) 1 else 2}:${if(part1) "" else " Aim=$aim"} Depth=$depth Position=$position Mult=${depth*position}")
}

//endregion
//region Day3

fun day3() {
    val inputLines = BufferedReader(FileReader("./src/main/resources/day3.txt")).readLines()

    println("Power Consumption: ${calcPowerConsumption(inputLines)}")
    println("Live Support: ${calcLiveSupportRating(inputLines)}")

}

fun calcPowerConsumption(inputLines: List<String>): Int {
    val counts: List<MutableList<Int>> = List(12) { MutableList(2) { 0 } }
    /*
    listOf(
        mutableListOf(0,0), mutableListOf(0,0), mutableListOf(0,0), mutableListOf(0,0),
        mutableListOf(0,0), mutableListOf(0,0), mutableListOf(0,0), mutableListOf(0,0),
        mutableListOf(0,0), mutableListOf(0,0), mutableListOf(0,0), mutableListOf(0,0),
    )
     */
    for(line in inputLines) {
        for(j in line.indices) {
            counts[j][line[j].digitToInt()]++
        }
    }

    var gamma: String = ""
    var epsilon: String = ""
    for(iList in counts) {
        if(iList[0] > iList[1]) {
            gamma += "0"
            epsilon += "1"
        } else {
            gamma += "1"
            epsilon += "0"
        }
    }

    val gammaNum: Int = gamma.toInt(2)
    val epsilonNum: Int = epsilon.toInt(2)
    return gammaNum * epsilonNum
}

fun calcLiveSupportRating(inputLines: List<String>): Int {
    var oxygenLines: List<String> = inputLines
    var oxygenIndex: Int = 0
    while(oxygenLines.size > 1) {
        val bitTarget = mostBitAtIndex(oxygenLines, oxygenIndex)
        oxygenLines = oxygenLines.filter { it[oxygenIndex].digitToInt() == bitTarget }
        oxygenIndex++
    }
    val oxygenGeneratorRating = oxygenLines[0].toInt(2)

    var co2Lines: List<String> = inputLines
    var co2Index: Int = 0
    while(co2Lines.size > 1) {
        val bitTarget = if(mostBitAtIndex(co2Lines, co2Index) == 1) 0 else 1
        co2Lines = co2Lines.filter { it[co2Index].digitToInt() == bitTarget }
        co2Index++
    }
    val co2ScrubberRating = co2Lines[0].toInt(2)
    return oxygenGeneratorRating * co2ScrubberRating
}

fun mostBitAtIndex(input: List<String>, index: Int): Int {
    var bits: Pair<Int, Int> = 0 to 0
    for(line in input) {
        bits = if(line[index].digitToInt() == 0) bits.first+1 to bits.second
        else bits.first to bits.second+1
    }
    return if(bits.first > bits.second) 0 else 1
}

//endregion
//region Day4

fun day4() {
    val inputLines = BufferedReader(FileReader("./src/main/resources/day4.txt")).readLines()
    val drawList = inputLines[0].split(",").map { it.toInt() }
    val boardList = inputLines
        .subList(1, inputLines.size)
        .chunked(6)
        .map { stringList -> stringList
            .subList(1, stringList.size)
            .map { it.chunked(3).map { it.trim().toInt() }.toMutableList() }
        }
        .map { Board(it) }

    println("first winning Boards' score: ${playGame(drawList, boardList)}")
    println("last  winning Boards' score: ${playGame(drawList, boardList, true)}")
}

fun playGame(drawList: List<Int>, boardList: List<Board>, lose: Boolean = false): Int {
    val mutBoardList = boardList.toMutableList()
    for (nextNumber in drawList) {
        val badBoards = mutableListOf<Pair<Board, Int>>()
        for (board in mutBoardList) {
            val result = board.play(nextNumber)
            //println("\n$result\n$board")
            if(result.first) {
                if(!lose) return result.second
                else badBoards.add(board to result.second)
            }
        }
        mutBoardList.removeIf { badBoards.map { it.first }.contains(it) }
        if(mutBoardList.isEmpty()) return badBoards.last().second
        badBoards.removeAll { true }
    }
    return -1
}

class Board(input: List<MutableList<Int>>) {
    private val boardData: List<MutableList<Int>> = input
    private val marks: List<MutableList<Boolean>> = input.map { it.map { false }.toMutableList() }

    override fun toString() = boardData
        .mapIndexed { x, line -> line.mapIndexed{ y, int -> marks[x][y] to int } }
        .joinToString("\n") { it.joinToString(" ") { (if(it.first) "X" else it.second.toString()).padStart(2) } }

    fun play(number: Int): Pair<Boolean, Int> {
        mark(number)
        return isWin() to score(number)
    }

    private fun mark(number: Int) {
        for (x in boardData.indices) {
            for (y in boardData[x].indices) {
                if (number == boardData[x][y]) mark(x, y)
            }
        }
    }

    private fun mark(x: Int, y: Int) {
        marks[x][y] = true
    }

    private fun isWin(): Boolean {
        //rows
        marks.forEach { if(it.reduce { acc, bool -> acc && bool }) return true }

        //colums
        for(i in marks[0].indices) {
            marks.map { it[i] }.let { if(it.reduce { acc, bool -> acc && bool }) return true }
        }

        return false
    }

    private fun score(number: Int) = boardData
        .flatten()
        .zip(marks.flatten())
        .reduce { acc, pair -> if(!acc.second) { if(!pair.second) acc.first + pair.first to acc.second else acc } else pair }
        .first * number
}

//endregion
//region Day5

fun day5() {
    val lineList = BufferedReader(FileReader("./src/main/resources/day5.txt")).readLines().map { Line.of(it) }

    SubmarineMap().run {
        lineList.forEach { addLine(it, true) }
        println("No Diag: ${countMultiple()}")
    }

    SubmarineMap().run {
        lineList.forEach { addLine(it) }
        println("All: ${countMultiple()}")
    }

}

class SubmarineMap {
    private val map: MutableList<MutableList<Int>> = MutableList(1000) { MutableList(1000) { 0 } }

    fun addLine(line: Line, ignoreDiag: Boolean = false) {
        if(line.isHorizontal()) {
            for(i in min(line.start.y,line.end.y)..max(line.start.y,line.end.y)) {
                map[line.start.x][i] += 1
            }
            return
        }

        if(line.isVertical()) {
            for(i in min(line.start.x, line.end.x)..max(line.start.x, line.end.x)) {
                map[i][line.start.y] += 1
            }
            return
        }

        if(!ignoreDiag && line.isDiagonal()) {
            val xDir: Int = if((line.end.x - line.start.x) > 0) 1 else -1
            val yDir: Int = if((line.end.y - line.start.y) > 0) 1 else -1
            for(i in 0..abs(line.start.x - line.end.x)) {
                map[line.start.x+xDir*i][line.start.y+yDir*i] += 1
            }
            return
        }
    }

    fun countMultiple() = map.flatten().filterNot { it == 1 || it == 0 }.count()

    override fun toString() = map.joinToString("\n") { it.joinToString("") { it.toString() } }
}

class Point(val x: Int, val y: Int) {
    companion object {
        private const val delimiter = ","
        fun of(s:String) = s.split(delimiter).let { Point(it[0].toInt(),it[1].toInt()) }
    }
    override fun toString() = "$x$delimiter$y"
}

class Line(var start: Point, var end: Point) {
    companion object {
        private const val delimiter = " -> "
        fun of(s:String) = s.split(delimiter).let { Line(Point.of(it[0]), Point.of(it[1])) }
    }
    fun isDiagonal() = !isHorizontal() && !isVertical()
    fun isAcutallyDiagonal() = abs(start.x-end.x) == abs(start.y-end.y)
    fun isHorizontal() = start.x == end.x
    fun isVertical() = start.y == end.y
    override fun toString() = "$start$delimiter$end"
}

//endregion
//region Day6

fun day6() {
    val fish = mutableListOf<Long>(
        4,3,3,5,4,1,2,1,3,1,1,1,1,1,2,4,1,3,3,1,1,1,1,2,3,1,1,1,4,1,1,2,1,2,2,1,1,1,1,1,5,1,1,2,1,1,1,1,1,1,
        1,1,1,3,1,1,1,1,1,1,1,1,5,1,4,2,1,1,2,1,3,1,1,2,2,1,1,1,1,1,1,1,1,1,1,4,1,3,2,2,3,1,1,1,4,1,1,1,1,5,
        1,1,1,5,1,1,3,1,1,2,4,1,1,3,2,4,1,1,1,1,1,5,5,1,1,1,1,1,1,4,1,1,1,3,2,1,1,5,1,1,1,1,1,1,1,5,4,1,5,1,
        3,4,1,1,1,1,2,1,2,1,1,1,2,2,1,2,3,5,1,1,1,1,3,5,1,1,1,2,1,1,4,1,1,5,1,4,1,2,1,3,1,5,1,4,3,1,3,2,1,1,
        1,2,2,1,1,1,1,4,5,1,1,1,1,1,3,1,3,4,1,1,4,1,1,3,1,3,1,1,4,5,4,3,2,5,1,1,1,1,1,1,2,1,5,2,5,3,1,1,1,1,
        1,3,1,1,1,1,5,1,2,1,2,1,1,1,1,2,1,1,1,1,1,1,1,3,3,1,1,5,1,3,5,5,1,1,1,2,1,2,1,5,1,1,1,1,2,1,1,1,2,1
    )
    var fishMap = fish.groupBy { it }.mapValues { it.value.size.toLong() }.toMutableMap()

    for(day in 1..256) {
       fishMap = simulateDay(fishMap)
       if(day == 80 || day == 256) println("Day $day: ${fishMap.map { it.value }.sum()}")
    }
}

fun simulateDay(fish: MutableList<Long>) = fish.flatMap { if(it-1 < 0) listOf(6,8) else listOf(it-1) }.toMutableList()
fun simulateDay(fish: MutableMap<Long, Long>): MutableMap<Long, Long> {
    val resFish = mutableMapOf<Long, Long>()
    fish.forEach {
        when(it.key) {
            0L -> {
                resFish[6] = (resFish[6]?:0).plus(it.value)
                resFish[8] = (resFish[8]?:0).plus(it.value)
            }
            else -> resFish[it.key-1] = (resFish[it.key-1]?:0).plus(it.value)
        }
    }
    return resFish
}


//endregion
//region Day7

fun day7() {
    val inputList = listOf<Long>(
        1101,1,29,67,1102,0,1,65,1008,65,35,66,1005,66,28,1,67,65,20,4,0,1001,65,1,65,1106,0,8,99,35,67,101,99,105,32,110,39,101,115,116,32,112,97,115,32,117,110,101,32,105,110,116,99,111,100,101,32,112,114,111,103,114,97,109,10,436,430,89,4,797,118,1591,1350,376,737,57,653,729,863,647,9,109,388,220,686,334,739,147,750,1009,657,86,162,275,1315,148,27,1076,153,780,67,1025,898,584,377,67,125,225,50,8,599,145,724,15,594,64,1050,16,452,3,852,17,32,72,721,1390,1043,28,27,709,463,113,146,1148,45,258,13,1025,50,97,1033,220,94,414,306,609,258,1080,331,1241,365,612,90,213,845,1234,1466,811,42,1682,340,212,283,3,186,20,1048,363,200,830,73,26,1155,517,102,136,481,437,967,30,26,508,95,124,124,528,154,314,959,22,42,30,462,190,1243,624,355,377,174,530,58,8,363,1098,791,70,23,1634,99,85,172,83,654,1761,155,665,458,755,478,424,301,353,342,333,520,117,616,397,1148,271,311,37,5,885,143,862,576,234,683,685,150,593,10,480,298,297,500,45,1045,5,12,105,85,14,303,851,189,246,18,727,347,580,72,933,736,51,452,393,292,457,950,1027,55,818,734,298,98,38,153,173,717,189,664,627,943,411,189,68,350,676,74,162,727,224,1160,51,14,1160,18,1092,99,983,238,519,113,89,863,1290,85,47,66,553,155,11,828,29,901,140,434,234,647,756,341,16,1004,707,2,1001,185,149,799,189,659,1134,463,355,84,1341,750,1479,717,108,188,24,519,779,732,201,452,118,5,22,1111,175,53,345,704,119,406,80,216,922,560,639,284,932,624,636,31,524,1827,1670,553,590,743,1187,436,127,283,343,298,186,74,291,440,154,1782,355,15,269,114,253,615,357,357,437,1439,638,524,509,112,509,170,83,1693,808,635,1038,703,879,1048,1158,1095,96,56,1004,1046,33,1,945,121,16,327,599,394,130,219,472,920,97,288,1688,355,223,92,133,645,626,154,531,824,103,1148,136,417,364,271,705,293,1789,1671,575,860,10,1827,661,52,108,265,126,165,1096,1345,157,1559,103,372,257,1758,1,1277,18,146,257,57,992,567,1507,227,197,340,641,5,717,969,529,336,1388,71,397,122,146,1011,341,169,721,247,1140,406,16,473,456,136,384,43,648,100,155,230,397,614,563,371,341,79,318,41,910,1067,908,426,1423,186,67,1067,485,1065,179,98,76,336,1018,142,1181,31,714,193,33,376,131,272,905,1104,97,606,93,118,267,1760,1052,29,293,186,243,1098,875,123,773,272,522,179,663,238,110,1083,1586,5,586,138,331,79,44,306,389,402,992,617,477,137,738,585,837,86,731,29,572,42,52,827,459,956,34,527,590,369,409,69,218,347,457,803,428,40,916,806,251,802,1066,152,685,657,230,502,19,630,67,786,777,880,257,415,1004,47,273,257,143,284,456,244,137,251,779,159,486,217,396,438,12,1009,441,56,89,489,449,2,205,131,353,84,78,1319,95,665,202,165,1025,1015,662,165,148,507,142,18,815,999,32,64,439,16,743,132,257,544,705,0,152,792,721,416,377,648,333,43,194,519,192,378,506,272,615,77,775,1647,1061,457,77,114,950,27,351,76,211,1321,197,497,685,6,53,262,746,850,782,323,615,413,422,179,12,154,120,1735,317,917,199,220,876,21,357,536,30,1048,84,627,864,909,261,206,87,545,252,462,306,251,1913,1017,256,406,280,463,394,143,186,557,820,969,708,429,72,191,213,656,161,9,109,999,400,32,458,174,903,25,106,322,37,97,1126,641,851,508,394,86,241,287,4,44,564,184,1122,288,535,808,287,220,313,1427,85,372,163,205,267,340,867,54,43,613,107,1050,213,176,792,394,172,54,770,1839,88,1732,211,1882,635,300,1319,226,669,205,1277,10,1519,275,1106,566,293,1161,45,543,114,381,1336,581,6,371,75,647,963,179,521,121,275,352,19,7,237,234,905,340,1032,415,116,56,132,1270,223,172,126,172,911,849,89,764,690,349,767,312,119,279,22,66,54,398,1000,615,38,403,8,111,615,99,907,1251,1521,1177,84,675,1003,301,191,346,182,119,1388,999,939,354,123,449,252,531,702,124,79,161,20,657,95,879,1646,382,27,608,282,262,329,4,868,401,422,403,52,206,882,431,1709,1198,893,174,366,2,176,210,149,873,1371,505,582,691,45,373,577,236,124,619,77,48,686,923,78,374,956,1191,311,211,361,1139,156,314,240,106,1728,71,1109,170,29,58,65,110,1612,280,62,942,1322,75,755,1289,606,377,1434,241,49,372,222,526,163,441,175,20,401,38,137,48,61
    )
    var fuelLinear: Long = Long.MAX_VALUE
    var fuelTriangular: Long = Long.MAX_VALUE

    for(i in 0..10000) {
        fuelLinear = min(fuelLinear, inputList.calcFuelLinear(i))
        fuelTriangular = min(fuelTriangular, inputList.calcFuelTriangular(i))
    }
    println("Linear: $fuelLinear")
    println("Triangular: $fuelTriangular")
}

fun List<Long>.calcFuelLinear(mean : Int) = sumOf { abs(mean.toLong() - it) }
fun List<Long>.calcFuelTriangular(mean : Int) = sumOf { abs(mean.toLong() - it)*(abs(mean.toLong() - it)+1) / 2 }

//endregion
//region Day8

fun day8() {
    val lineList = BufferedReader(FileReader("./src/main/resources/day8.txt")).readLines().splitToSegmentListStruct()

    val targetNumsDecoded = lineList.map { (allNumsEncoded, targetNumsEncoded) ->
        val allNumsDecoded = allNumsEncoded.map { SegmentDigit(it) }.decode()
        targetNumsEncoded.map { it.decodeSegmentDisplayNumber(allNumsDecoded) }
    }

    println("Part1: ${targetNumsDecoded.map { it.count { listOf(1,4,7,8).contains(it) } }.sum()}")
    println("Part2: ${targetNumsDecoded.map { it.joinToString("").toInt() }.sum()}")
}

fun List<String>.splitToSegmentListStruct() = map { displayInfoString ->
    displayInfoString.split(" | ").let { it[0].split(" ") to it[1].split(" ") }
}

fun String.decodeSegmentDisplayNumber(allNumsDecoded: List<SegmentDigit>) =
    allNumsDecoded.find { it.codeString.toSortedSet() == toSortedSet() }!!.decodedNumber!!

class SegmentDigit(
    val codeString: String
) {
    var state: BitSet = codeString.segmentsToBits().toBitSet()
    var decodedNumber: Int? = null

    fun and(other: SegmentDigit): SegmentDigit = this.clone().apply { state.and(other.state) }
    fun cardinality(): Int = state.cardinality()

    fun clone(): SegmentDigit = SegmentDigit(codeString).also { it.decodedNumber = decodedNumber }
    override fun toString() = "SegmentDigit[$codeString, $state, $decodedNumber]"
}

fun String.segmentsToBits(): String {
    var returnString = ""
    for(char in 'a'..'g') {
        returnString += if(contains(char)) 1 else 0
    }
    return returnString.reversed()
}

fun String.toBitSet(): BitSet = BitSet.valueOf(ByteArray(1).apply { set(0, toByte(2)) } )

fun List<SegmentDigit>.decode(): List<SegmentDigit> {
    val one = find { it.cardinality() == 2 }!!.also { it.decodedNumber = 1 }
    val four = find { it.cardinality() == 4 }!!.also { it.decodedNumber = 4 }
    find { it.cardinality() == 3 }!!.also { it.decodedNumber = 7 }
    find { it.cardinality() == 7 }!!.also { it.decodedNumber = 8 }
    filter { it.cardinality() == 5 }.forEach {
        if(one.and(it).cardinality() == 2) it.decodedNumber = 3
        else if(four.and(it).cardinality() == 2) it.decodedNumber = 2
        else it.decodedNumber = 5
    }
    filter { it.cardinality() == 6 }.forEach {
        if(one.and(it).cardinality() == 1) it.decodedNumber = 6
        else if(four.and(it).cardinality() == 4) it.decodedNumber = 9
        else it.decodedNumber = 0
    }

    return this
}
/*

    println(lineList.sumOf {
        it.split(" | ")[1].split(" ").map { possibleNumbers(it)[0] }.count { listOf(1, 4, 7, 8).contains(it) }
    })



    val indexList = List(7) { BitSet() }
    lineList
        .map {
            CharRange('a','g').zip(it.split(" | ")[0].split(" ").map { it.segmentsToBits() }.transpose()).map { it.first to position[it.second] }
        }

    // bcedagf ebadf gcdfe gfcead bcedgf dfeca ac dgca ace cafbge

    // 2 -> 1
    // 3 -> 7
    // 4 -> 4
    // 5 -> 2,3,5,
    // 6 -> 0,6,9
    // 7 -> 8

    // 0 -> 6 - (abcefg)
    // 1 -> 2 - eid - (cf)
    // 2 -> 5 - (acdeg)
    // 3 -> 5 - (acdfg)
    // 4 -> 4 - eid - (bcdf)
    // 5 -> 5 - (abdfg)
    // 6 -> 6 - (abdefg)
    // 7 -> 3 - eid - (acf)
    // 8 -> 7 - eid - (abcdefg)
    // 9 -> 6 - (abcdfg)

    // 1 -> (cf)
    // 1,4 -> (bd)
    // 1,7 -> a
    // 7,4 -> (bd)

    //5er contains 1 -> das ist 3
    // restl. 5er contains

    // 6er !contains 1 -> das ist 6
    // restl. 6er contains 4 -> das ist 9
    // anderer: 0
    // c fix, f fix

    // (abc_efg)
    // (ab_defg)
    // (abcd_fg)

    // (a_cde_g)
    // (a_cd_fg)
    // (ab_d_fg)


    // 0 -> 6     - (abc_efg)
    // 1 -> c = 2 - (__c__f_)
    // 2 -> 5     - (a_cde_g)
    // 3 -> 5     - (a_cd_fg)
    // 4 -> c = 4 - (_bcd_fg)
    // 5 -> 5     - (ab_d_fg)
    // 6 -> 6     - (ab_defg)
    // 7 -> c = 3 - (a_c__fg)
    // 8 -> c = 7 - (abcdefg)
    // 9 -> 6     - (abcd_fg)

}

 00
1  2
 33
4  5
 66

fun List<String>.transpose(): List<String> {
    val transposedList = MutableList(get(0).length) { "" }
    forEachIndexed { index, s -> transposedList[index] += s }
    return transposedList
}

val position: Map<String, Char> = mapOf(
    "1011011111" to 'a',
    "1000111011" to 'b',
    "1111111011" to 'c',
    "0011111011" to 'd',
    "1110001010" to 'e',
    "1001111111" to 'f',
    "1011011011" to 'g',
)

fun <K,V> MutableMap<K, V>.firstForValueOrNull(value: V): K? = entries.find { it.value == value }?.key

// 2 -> 1
// 3 -> 7
// 4 -> 4
// 5 -> 2,3,5,
// 6 -> 0,6,9
// 7 -> 8
fun List<String>.segmentsForNum(number: Int): String? = when(number) {
    1 -> find { it.length == 2 }
    7 -> find { it.length == 3 }
    4 -> find { it.length == 4 }
    //2,3,5 -> takeWhile { it.length == 5 }
    //0,6,9 -> takeWhile { it.length == 6 }
    8 -> find { it.length == 7 }
    else -> null
}



fun String.diffUnsorted(other: String): Pair<Int, Int> {
    var thisMore = 0
    var otherMore = 0
    forEach { if(!other.contains(it)) thisMore++ }
    other.forEach { if(!this.contains(it)) otherMore++ }
    return thisMore to otherMore
}

fun String.containsAllSegCharsForNumber(allSegNums: List<String>, number: Int) = containsAll(allSegNums.segmentsForNum(number)?.toCharArray())
fun String.containsAll(charList: CharArray?) = charList?.map { contains(it) }?.reduce(Boolean::and) ?: false

fun decodeSegmentString(segNumber: String, mapping: Map<Char, Char>): String {
    return segNumber
        .toCharArray()
        .map { mapping[it] }
        .mapNotNull { it }
        .sorted()
        .joinToString("")
}

fun parseNumber(segments: String): Int {
    return when(segments) {
        "abcefg" -> 0
        "cf" -> 1
        "acdeg" -> 2
        "acdfg" -> 3
        "bcdf" -> 4
        "abdfg" -> 5
        "abdefg" -> 6
        "acf" -> 7
        "abcdefg" -> 8
        "abcdfg" -> 9
        else -> -1
    }
}

fun possibleNumbers(segments: String, ): List<Int> {
    return when(segments.length) {
        2 -> listOf(1)
        3 -> listOf(7)
        4 -> listOf(4)
        7 -> listOf(8)
        else -> listOf()
    }
}

*/

//endregion
//region Day9

fun day9() {
    val caveMap = CaveMap(BufferedReader(FileReader("./src/main/resources/day9.txt")).readLines().map { it.chunked(1).map { it.toInt() } })

    println("LocalLows: ${caveMap.localLows().sumOf { it.height + 1 }}")
    println("Basins: ${caveMap.basins().map { it.size }.sortedDescending().take(3).reduce(Int::times)}")
}

class CaveMap(private val heightmap: List<List<Int>>) {
    fun localLows(): List<HeightPosition> {
        val localLows: MutableList<HeightPosition> = mutableListOf()
        for(x in heightmap.indices) {
            for(y in heightmap[x].indices) {
                val currPos = HeightPosition(x,y,heightmap[x][y])
                if(currPos.neighbors(heightmap).map { currPos.height < it.height }.reduce(Boolean::and))
                    localLows.add(currPos)
            }
        }
        return localLows
    }

    fun basins(): List<Basin> = localLows().map { Basin(it, heightmap) }

    class HeightPosition(val x: Int, val y: Int, val height: Int): Comparable<HeightPosition> {
        fun neighbors(heightmap: List<List<Int>>) = listOfNotNull(
            heightmap.getOrNull(x-1)?.getOrNull(y)?.let { HeightPosition(x - 1, y, it) },
            heightmap.getOrNull(x+1)?.getOrNull(y)?.let { HeightPosition(x + 1, y, it) },
            heightmap.getOrNull(x)?.getOrNull(y-1)?.let { HeightPosition(x, y - 1, it) },
            heightmap.getOrNull(x)?.getOrNull(y+1)?.let { HeightPosition(x, y + 1, it) },
        )

        override fun compareTo(other: HeightPosition): Int = height - other.height
        override fun toString() = "P($x,$y,$height)"
        override fun equals(other: Any?): Boolean = if(other is HeightPosition) (x == other.x) && (y == other.y) else false
        override fun hashCode(): Int = (31 * (31 * 0 + x) + y)
    }

    class Basin(val low: HeightPosition, heightmap: List<List<Int>>) {
        val contents: MutableList<HeightPosition> = mutableListOf(low)

        init {
            val explorationStack: Stack<HeightPosition> = Stack()
            explorationStack.push(low)
            while(!explorationStack.empty()) {
                val curr = explorationStack.pop()
                curr.neighbors(heightmap)
                    .filter { it.isRelevant() && it.isNew() && curr < it }
                    .forEach {
                        contents.add(it)
                        explorationStack.pushIfNotContained(it)
                    }
            }
        }

        val size get() = contents.size

        override fun toString() = "Basin($low,$size,$contents)"
        private fun HeightPosition.isRelevant() = this.height != 9
        private fun HeightPosition.isNew() = !contents.contains(this)
        private fun <T> Stack<T>.pushIfNotContained(value: T) { if(!contains(value)) push(value) }
    }
}

//endregion
//region Day10

fun day10() {
    val lineList = BufferedReader(FileReader("./src/main/resources/day10.txt")).readLines()


}

//endregion
//region Day11

fun day11() {}

//endregion
//region Day12

fun day12() {
    val caveList = mutableListOf<Cave>()
    BufferedReader(FileReader("./src/main/resources/day12.txt")).readLines()
        .map { line -> line.split("-")
            .map { name -> caveList.find { it.name == name } ?: Cave(name).also {
                if(name.first().isUpperCase()) it.isBig = true
                caveList.add(it)
            } }
            .run { get(0) to get(1) }
        }
        .forEach {
            it.first.connected.add(it.second)
            it.second.connected.add(it.first)
        }

    val start = caveList.find { it.name == "start" } ?: exitProcess(1)
    val end = caveList.find { it.name == "end" } ?: exitProcess(1)
    println("No small visiting: ${start.pathsTo(end).size}")
    println("A bit small visiting: ${start.pathsTo(end, allowSmallRepetitionOnce = true).size}")
}

class Cave(val name: String) {
    var isBig: Boolean = false
    val connected: MutableList<Cave> = mutableListOf()

    fun pathsTo(target: Cave, toIgnore: List<Cave> = emptyList(), allowSmallRepetitionOnce: Boolean = false): MutableList<MutableList<Cave>> {
        //println("$this, $target, $toIgnore")
        if(target == this) return mutableListOf(mutableListOf(this))
        return connected.filter { nextCave -> nextCave.name != "start" && (nextCave.isBig || allowSmallRepetitionOnce || !toIgnore.contains(nextCave)) }
            .flatMap { nextCave ->
                nextCave.pathsTo(target, if(!isBig) toIgnore + this else toIgnore, allowSmallRepetitionOnce && !toIgnore.contains(nextCave))
                    .map { it.add(0, this); it }
            }
            .toMutableList()
    }

    override fun toString(): String = "Cave(name='$name')"
    override fun equals(other: Any?): Boolean =
        if(other is Cave) name == other.name
        else super.equals(other)
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isBig.hashCode()
        result = 31 * result + connected.hashCode()
        return result
    }
}



//endregion
//region Day13

fun day13() {
    val lineList = BufferedReader(FileReader("./src/main/resources/day13.txt")).readLines()
    val dots = lineList.filter { it.firstOrNull()?.isDigit() == true }.map { it.toDot() }
    val foldInstructions = lineList.filter { it.firstOrNull() == 'f' }.map { it.split(" ").last().toFoldInstruction() }
    val firstFoldInstruction = foldInstructions.first()

    fun List<Dot>.applyFoldInstruction(instruction: FoldInstruction): List<Dot> {
        return map { it.clone().fold(instruction) }.groupBy { it }.keys.toList()
    }
    fun List<Dot>.applyFoldInstructions(instructions: List<FoldInstruction>): List<Dot> {
        return instructions.fold(this) { currDotList, foldInstruction -> currDotList.applyFoldInstruction(foldInstruction) }
    }
    fun List<Dot>.visualize(dot: String = "##", noDot: String = "  ") {
        val maxX = maxOf { it.x }
        val maxY = maxOf { it.y }
        for(y in 0..maxY) {
            for(x in 0..maxX) {
                if(find { it.x == x && it.y == y } != null) print(dot)
                else print(noDot)
            }
            println()
        }
    }

    println("DotNum after first fold: ${dots.applyFoldInstruction(firstFoldInstruction).size}")
    println("Dots after all folds:")
    dots.applyFoldInstructions(foldInstructions).visualize("██", "  ")
}

class Dot(var x: Int, var y: Int) {
    companion object { fun String.toDot() = split(",").run { Dot(get(0).toInt(), get(1).toInt()) } }

    fun fold(instruction: FoldInstruction): Dot {
        when(instruction.axis) {
            FoldAxis.X -> x = foldComponent(x, instruction.pos)
            FoldAxis.Y -> y = foldComponent(y, instruction.pos)
        }
        return this
    }

    private fun foldComponent(oldValue: Int, foldPos: Int): Int {
        if(oldValue < foldPos) return oldValue
        return 2*foldPos - oldValue
    }

    fun clone(): Dot = Dot(x, y)
    override fun toString(): String = "Dot[$x,$y]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dot

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }


}
class FoldInstruction(val axis: FoldAxis, val pos: Int) {
    companion object { fun String.toFoldInstruction() = split("=").run { FoldInstruction(FoldAxis.valueOf(get(0).uppercase()), get(1).toInt()) } }
}

enum class FoldAxis { X,Y }

//endregion
//region Day14

fun day14() {
    val lineList = BufferedReader(FileReader("./src/main/resources/day14.txt")).readLines()


}

//endregion
//region Day15

fun day15() {}

//endregion
//region Day16

fun day16() {}

//endregion
//region Day17

fun day17() {}

//endregion
//region Day18

fun day18() {}

//endregion
//region Day19

fun day19() {}

//endregion
//region Day20

fun day20() {}

//endregion
//region Day21

fun day21() {}

//endregion
//region Day22

fun day22() {}

//endregion
//region Day23

fun day23() {}

//endregion
//region Day24

fun day24() {}

//endregion


