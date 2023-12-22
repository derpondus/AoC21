package year2021

import year2021.Dot.Companion.toDot
import year2021.FoldInstruction.Companion.toFoldInstruction
import year2021.InsertionRule.Companion.toInsertionRule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import java.util.*
import kotlin.math.*
import kotlin.system.exitProcess

//region Day1

fun day1(inputLines: List<String>) {
    println("Part1: ${calcDay1(inputLines, 1)}")
    println("Part2: ${calcDay1(inputLines, 3)}")
}

fun calcDay1(inputLines: List<String>, windowSize: Int = 1): Int =
    inputLines.asSequence()
        .map { it.toInt() }
        .windowed(windowSize)
        .windowed(2)
        .map { (first, second) -> first.sum() < second.sum() }
        .count { it }
/*
{
    var counter = 0
    for (i in 0 until inputLines.size-windowSize) {
        if(inputLines.subList(i, i+windowSize).sumOf { it.toInt() } < inputLines.subList(i+1, i+windowSize+1).sumOf { it.toInt() }) counter++
    }
    return counter
}
*/

//endregion
//region Day2

fun day2(inputLines: List<String>) {
    val instructions = inputLines.map { it.split(" ") }

    calcPath(instructions, true)
    calcPath(instructions, false)
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

fun day3(inputLines: List<String>) {
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

fun day4(inputLines: List<String>) {
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

fun day5(inputLines: List<String>) {
    val lineList = inputLines.map { Line.of(it) }

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
fun List<Long>.calcFuelTriangular(mean : Int) = sumOf { abs(mean.toLong() - it) *(abs(mean.toLong() - it) +1) / 2 }

//endregion
//region Day8

fun day8(inputLines: List<String>) {
    val lineList = inputLines.splitToSegmentListStruct()

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

fun day9(inputLines: List<String>) {
    val caveMap = CaveMap(inputLines.map { it.chunked(1).map { it.toInt() } })

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

fun day10(inputLines: List<String>) {
    val navigationLines = inputLines.map { NavigationLine(it) }
    println("Total score of corrupted Lines: ${navigationLines.filter { it.state is CORRUPTED }.sumOf { it.score }}")
    println("Middle score of incomplete Lines: ${navigationLines.filter { it.state is INCOMPLETE }.map { it.score }.sorted().run { get(size/2) }}")
}

class NavigationLine(line: String) {
    val chunkStack: Stack<Char> = Stack()
    val state: NavLineState = parseLine(line)

    val score get(): Long = when(state) {
        is CORRECT -> 0
        is INCOMPLETE -> chunkStack.foldRight(0L) { next: Char?, acc: Long ->
            (acc * 5) + when(next?.closingChar) {
                ')' -> 1
                ']' -> 2
                '}' -> 3
                '>' -> 4
                else -> throw IllegalStateException("Read unknown Char From Stack while calculating score: $next")
            }
        }
        is CORRUPTED -> when(state.actual) {
            ')' -> 3
            ']' -> 57
            '}' -> 1197
            '>' -> 25137
            else -> 0
        }
    }

    private fun parseLine(line: String): NavLineState {
        val lineIterator = line.iterator()
        while(lineIterator.hasNext()) {
            when(val next = lineIterator.nextChar()) {
                '(', '{', '[', '<' -> chunkStack.push(next)
                ')', '}', ']', '>' -> {
                    val expected = chunkStack.pop().closingChar ?: throw IllegalStateException("Read unknown Char From Stack")
                    if(expected != next) return CORRUPTED(expected, next)
                }
                else -> throw IllegalStateException("Line contains unknown Char: $next")
            }
        }
        return if(chunkStack.isEmpty()) CORRECT else INCOMPLETE
    }

    private val Char.closingChar: Char?
        get() = when(this) {
            '(' -> ')'
            '{' -> '}'
            '[' -> ']'
            '<' -> '>'
            else -> null
        }
}

sealed interface NavLineState
object CORRECT : NavLineState
object INCOMPLETE : NavLineState
class CORRUPTED(val expected: Char, val actual: Char): NavLineState


//endregion
//region Day11

fun day11(inputLines: List<String>) {
    val initialState = inputLines.map { it.toCharArray().map { it.digitToInt() }.toMutableList() }.toMutableList()

    OctoGridSimulator.reset(initialState.map { it.map { it }.toMutableList() }.toMutableList())
    for(i in 0 until 100) {
        OctoGridSimulator.step()
    }
    println("FlashCounter after 100 steps: ${OctoGridSimulator.flashCounter}")

    OctoGridSimulator.reset(initialState.map { it.map { it }.toMutableList() }.toMutableList())
    var stepCounter = 0
    while(OctoGridSimulator.energyLevelGrid.flatten().count { it == 0 } != 100) {
        OctoGridSimulator.step()
        stepCounter++
    }
    println("StepCounter when simulFlash is $stepCounter")
}

object OctoGridSimulator {
    var energyLevelGrid: MutableList<MutableList<Int>> = mutableListOf(mutableListOf(0))
    var flashCounter = 0

    fun reset(energyLevelGrid: MutableList<MutableList<Int>>) {
        this.energyLevelGrid = energyLevelGrid
        flashCounter = 0
    }

    fun step() {
        for(y in energyLevelGrid.indices) {
            for(x in energyLevelGrid[y].indices) {
                energyLevelGrid[y][x]++
                if(energyLevelGrid[y][x] == 10) flash(x,y)
            }
        }

        for(y in energyLevelGrid.indices) {
            for(x in energyLevelGrid[y].indices) {
                if(energyLevelGrid[y][x] > 10) energyLevelGrid[y][x] = 0
            }
        }
    }

    private fun flash(centr_x: Int, centr_y: Int) {
        flashCounter++
        energyLevelGrid[centr_y][centr_x] = 11
        for(x in centr_x-1..centr_x+1) {
            for(y in centr_y-1..centr_y+1) {
                if(x < 0 || 9 < x || y < 0 || 9 < y) continue
                if(x == centr_x && y == centr_y) continue
                if(energyLevelGrid[y][x] == 11) continue

                energyLevelGrid[y][x]++
                if(energyLevelGrid[y][x] == 10) flash(x, y)
            }
        }
    }
}

//endregion
//region Day12

fun day12(inputLines: List<String>) {
    val caveList = mutableListOf<Cave>()
    inputLines.map { line -> line.split("-")
        .map { name -> caveList.find { it.name == name } ?: Cave(name).also {
            if(name.first().isUpperCase()) it.isBig = true
            caveList.add(it)
        } }
        .run { get(0) to get(1) }
    }.forEach {
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

operator fun String.times(n: Int) = repeat(n)

fun day13(inputLines: List<String>) {
    val dots = inputLines.filter { it.firstOrNull()?.isDigit() == true }.map { it.toDot() }
    val foldInstructions = inputLines.filter { it.firstOrNull() == 'f' }.map { it.split(" ").last().toFoldInstruction() }
    val firstFoldInstruction = foldInstructions.first()

    fun List<Dot>.applyFoldInstruction(instruction: FoldInstruction): List<Dot> {
        return map { it.clone().fold(instruction) }.groupBy { it }.keys.toList()
    }
    fun List<Dot>.applyFoldInstructions(instructions: List<FoldInstruction>): List<Dot> {
        return instructions.fold(this) { currDotList, foldInstruction -> currDotList.applyFoldInstruction(foldInstruction) }
    }
    fun List<Dot>.visualize(dot: String = "##", noDot: String = " "*dot.length) {
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
    dots.applyFoldInstructions(foldInstructions).visualize("██")
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

fun day14(inputLines: List<String>) {
    val template = inputLines.first()
    val insertionRules = inputLines.filter { it.contains(" -> ") }.map { it.toInsertionRule() }
    /*fun String.applyInsertionRules(insertionRules: List<InsertionRule>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(get(0))
        for(i in 0..length-2) {
            stringBuilder.append(insertionRules.filter { it.left == get(i) && it.right == get(i+1)}.first().mid)
            stringBuilder.append(get(i+1))
        }
        return stringBuilder.toString()
    }
    fun String.repeatInsertionRules(n: Int, insertionRules: List<InsertionRule>): String {
        var returnValue = this
        for(i in 1..n) {
            print("It: $i ... ")
            returnValue = returnValue.applyInsertionRules(insertionRules)
            println("DONE")
        }
        return returnValue
    }

    val after10Times = template.repeatInsertionRules(10, insertionRules)
    val after10TimesCharCount = after10Times.toCharArray().groupBy { it }.mapValues { it.value.size }

    println("Part1: ${after10TimesCharCount.maxOf { it.value } - after10TimesCharCount.minOf { it.value }}")*/
    val polymerState = PolymerState(
        (0..template.length-2).map { template.substring(it..it+1) }.toMutableCounter(),
        template.toCharArray().toMutableCounter()
    )

    polymerState.simulateGrowth(10, insertionRules)
    println("After 10 iterations: ${polymerState.calcMetric()}")
    polymerState.simulateGrowth(30, insertionRules)
    println("After 40 iterations: ${polymerState.calcMetric()}")

}

fun <T> Iterable<T>.toCounter() = groupBy { it }.mapValues { it.value.size.toLong() }
fun <T> Iterable<T>.toMutableCounter() = toCounter().toMutableMap()
fun CharArray.toCounter() = groupBy { it }.mapValues { it.value.size.toLong() }
fun CharArray.toMutableCounter() = toCounter().toMutableMap()

class PolymerState(private var insertionPairCount: MutableMap<String, Long>, private val charCount: MutableMap<Char, Long>) {
    private fun simulateGrowth(insertionRules: List<InsertionRule>) {
        val tempIPC: MutableMap<String, Long> = mutableMapOf()
        insertionPairCount.forEach { entry ->
            val iRule = insertionRules.find { it.getTarget() == entry.key }
            if(iRule != null) {
                tempIPC.saveAdd(iRule.getLeftReplPair(), entry.value)
                tempIPC.saveAdd(iRule.getRightReplPair(), entry.value)
                charCount.saveAdd(iRule.mid, entry.value)
            } else {
                tempIPC[entry.key] = tempIPC.getOrDefault(entry.key, 0).plus(entry.value)
            }
        }
        insertionPairCount = tempIPC
    }
    fun simulateGrowth(iterations: Int, insertionRules: List<InsertionRule> ) {
        for(i in 1..iterations) {
            simulateGrowth(insertionRules)
        }
    }
    fun calcMetric() = charCount.maxOf { it.value } - charCount.minOf { it.value }

    private fun <K> MutableMap<K, Long>.saveAdd(key: K, value: Long) { set(key, getOrDefault(key, 0).plus(value)) }
}

class InsertionRule(val left: Char, val right: Char, val mid: Char) {
    companion object { fun String.toInsertionRule() = this.toCharArray().filter { it.isUpperCase() }.run { InsertionRule(get(0), get(1), get(2)) } }
    fun getTarget() = "$left$right"
    fun getLeftReplPair() = "$left$mid"
    fun getRightReplPair() = "$mid$right"
}


//endregion
//region Day15

fun day15(inputLines: List<String>) {
    println("Risk of shortest:")

    val chitonCave = ChitonCave(inputLines.mapIndexed { y, row -> row.toCharArray().toList().mapIndexed() { x, risk -> Chiton(x,y,risk.digitToInt()) } })
    val start = chitonCave.getChiton(0,0)

    var end = chitonCave.getChiton(chitonCave.width-1, chitonCave.height-1)
    chitonCave.shortestPath(start, end)
    println(" -> Small Map: ${end.riskUntilHere}")

    chitonCave.increaseMapSize(5)
    end = chitonCave.getChiton(chitonCave.width-1, chitonCave.height-1)
    chitonCave.shortestPath(start, end)
    println(" -> Large Map: ${end.riskUntilHere}")
}

class ChitonCave(private var chitonRiskMap: List<List<Chiton>>) {

    fun Chiton.getNeighbors() = listOfNotNull(
        chitonRiskMap.getOrNull(y-1)?.getOrNull(x),
        chitonRiskMap.getOrNull(y+1)?.getOrNull(x),
        chitonRiskMap.getOrNull(y)?.getOrNull(x-1),
        chitonRiskMap.getOrNull(y)?.getOrNull(x+1),
    )

    val width get() = chitonRiskMap[0].size
    val height get() = chitonRiskMap.size

    fun getChiton(x:Int, y:Int) = chitonRiskMap[y][x]

    fun shortestPath(start: Chiton, end: Chiton): Chiton {
        //Dijkstra
        start.riskUntilHere = 0
        val openPaths: MutableSet<Chiton> = mutableSetOf(start)
        while(openPaths.isNotEmpty()) {
            val curr = openPaths.minByOrNull { it.riskUntilHere }!!
            if(!openPaths.remove(curr)) throw AssertionError("has to be removed");
            curr.visited = true
            if(curr == end) break
            curr.getNeighbors().forEach {
                if(it.visited) return@forEach
                if(it.riskUntilHere > curr.riskUntilHere + it.risk) {
                    it.prev = curr
                    it.riskUntilHere = curr.riskUntilHere + it.risk
                }
                openPaths.add(it)
            }
        }

        //backtracking
        var curr = end
        while(curr != start) {
            val tempCurr = curr
            curr = curr.prev!!
            curr.next = tempCurr
        }

        return start
    }

    fun increaseMapSize(n: Int) {
        val newChitonRiskMap: MutableList<MutableList<Chiton>> = mutableListOf()
        for(i in 0 until n) {
            chitonRiskMap.forEach { row ->
                val line: MutableList<Chiton> = mutableListOf()
                for(j in 0 until n) {
                    line.addAll(row.map { Chiton(it.x + j * row.size, it.y + i * chitonRiskMap.size, (it.risk+i+j-1).rem(9)+1) })
                }
                newChitonRiskMap.add(line)
            }
        }
        chitonRiskMap = newChitonRiskMap
    }
}

class Chiton(val x: Int, val y: Int, val risk: Int) {
    var prev: Chiton? = null
    var next: Chiton? = null
    var riskUntilHere = Int.MAX_VALUE
    var visited: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chiton

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "Chiton(x=$x, y=$y, risk=$risk, riskUntilHere=$riskUntilHere, visited=$visited, prev=${if(prev != null) "notNull" else "null"}, next=${if(next != null) "notNull" else "null"})"
    }
}

//endregion
//region Day16

fun day16(inputLines: List<String>) {
    val bitsPackage = BitsPackage.of(inputLines[0].map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("").iterator()) //inputLines[1] SkullZ_Input

    //println(bitsPacket)
    //println("Formula: ${bitsPacket?.getFormula()}")
    println("Packet versionSum: ${bitsPackage?.getVersionSum()}")
    println("Packet Value: ${bitsPackage?.getValue()}")
}

fun CharIterator.next(len: Int): String? = (0 until len).map { if(hasNext()) next() else return null }.joinToString("")

abstract class BitsPackage(val version: Int) {
    companion object {
        fun of(input: CharIterator): BitsPackage? {
            val version = input.next(3)?.toInt(2) ?: return null
            val id = input.next(3)?.toInt(2) ?: return null

            return when(id) {
                4 -> LiteralBitsPackage.of(version, input)
                else -> OperatorBitsPackage.of(version, input, OperatorType.of(id))
            }
        }
    }

    abstract fun getVersionSum(): Int
    abstract fun getValue(): Long

    abstract fun getFormula(): String
    abstract fun getRekSubNum(): Int
}


class LiteralBitsPackage(version: Int, val number: Long): BitsPackage(version) {
    companion object {
        fun of(version: Int, input: CharIterator): LiteralBitsPackage? {
            val numberString: StringBuilder = StringBuilder()
            var calcNext = true
            while(calcNext) {
                val next = input.next(5) ?: return null
                if(next.elementAt(0) == '0') calcNext = false
                numberString.append(next.substring(1,next.length))
            }
            return LiteralBitsPackage(version, numberString.toString().toLong(2))
        }
    }

    override fun getVersionSum() = version
    override fun getValue(): Long = number

    override fun getRekSubNum(): Int = 1
    override fun getFormula(): String = number.toString()

    override fun toString(): String = "Literal($number)"
}

class OperatorBitsPackage(version: Int, val subBitsPackages: List<BitsPackage>, val type: OperatorType): BitsPackage(version) {
    companion object {
        fun of(version: Int, input: CharIterator, type: OperatorType): OperatorBitsPackage? {
            val subBitsPackages: MutableList<BitsPackage> = mutableListOf()

            val isLength = input.next(1)?.equals("0") ?: return null
            if(isLength) {
                val length = input.next(15)?.toInt(2) ?: return null
                val subIterator = input.next(length)?.iterator() ?: return null
                var calcNext = true
                while(calcNext) {
                    val next = of(subIterator)
                    if(next == null) calcNext = false
                    else subBitsPackages.add(next)
                }

            } else {
                val count = input.next(11)?.toInt(2) ?: return null
                for(i in 0 until count) {
                    of(input)?.let { subBitsPackages.add(it) } ?: println("[WARN] Couldn't parse promised package")
                }
            }

            if(subBitsPackages.isEmpty()) return null
            return OperatorBitsPackage(version, subBitsPackages.toList(), type)
        }
    }


    override fun getVersionSum() = subBitsPackages.sumOf { it.getVersionSum() } + version
    override fun getValue(): Long = type.aggregate(subBitsPackages.map { it.getValue() })

    override fun getRekSubNum(): Int = subBitsPackages.sumOf { it.getRekSubNum() } + 1
    override fun getFormula() = when(type) {
        OperatorType.SUM -> subBitsPackages.run { if(size == 1) return get(0).getFormula() else joinToString(" + ", "(", ")") { it.getFormula() } }
        OperatorType.PRODUCT -> subBitsPackages.run { if(size == 1) return get(0).getFormula() else joinToString(" * ", "(", ")") { it.getFormula() } }
        OperatorType.MIN -> subBitsPackages.minOf { it.getFormula() }
        OperatorType.MAX -> subBitsPackages.maxOf { it.getFormula() }
        OperatorType.GREATER -> "[${subBitsPackages[0].getFormula()} > ${subBitsPackages[1].getFormula()}]"
        OperatorType.LESS -> "[${subBitsPackages[0].getFormula()} < ${subBitsPackages[1].getFormula()}]"
        OperatorType.EQUAL -> "[${subBitsPackages[0].getFormula()} = ${subBitsPackages[1].getFormula()}]"
    }

    override fun toString(): String = "Operator($type) {\n\t${subBitsPackages.flatMap { it.toString().split("\n") }.joinToString("\n\t") }\n}"
}

enum class OperatorType(val id: Int, val aggregate: (input: List<Long>) -> Long) {
    SUM(0, Iterable<Long>::sum),
    PRODUCT(1, { it.reduce(Long::times) }),
    MIN(2, { it.minOf { it } }),
    MAX(3, { it.maxOf { it } }),
    GREATER(5, { if(it[0] > it[1]) 1 else 0 }),
    LESS(6, { if(it[0] < it[1]) 1 else 0 }),
    EQUAL(7, { if(it[0] == it[1]) 1 else 0 });

    companion object { fun of(ID: Int): OperatorType = values().find { it.id == ID } ?: throw kotlin.AssertionError("OperatorType not found") }
}

//endregion
//region Day17

val targetX = 230..283
val targetY = -107..-57

fun day17() {
    val hitList = mutableListOf<Trajectory>()
    for (y in abs(targetY.first) downTo targetY.first) {
        for (x in 0..targetX.last) {
            Trajectory(x, y).run {
                if (result == TrajectoryResult.HIT) { hitList.add(this) }
            }
        }
    }

    println("Highest: ${hitList.maxOf { it.positions.maxOf { it.second } }}")
    println("TrajectoryNum: ${hitList.size}")
}

/*
fun day17_2() {
    var minXTra = 0
    var maxXTra = 0
    var maxYTra = abs(targetY.first)-1
    var yTra = 8

    while(!targetX.contains(minXTra.gaussSum())) { minXTra++ }
    maxXTra = minXTra

    do {
        testTrajectory(maxXTra, maxYTra)
        maxXTra++
    } while (targetX.contains(maxXTra.gaussSum()))

    //testTrajectory(targetX.first, targetY.first)
    println("$minXTra, $maxXTra, 8 $maxYTra")


    /*
    while(true) {
        when(testTrajectory(xTra, yTra, false)) {
            TrajectoryResult.BELOW -> xTra += 1
            TrajectoryResult.RIGHT -> break
            TrajectoryResult.HIT -> { yTra += 1; xTra = 0 }
        }
        println()
    }
    yTra -= 1
    xTra = 0

    while(true) {
        when(testTrajectory(xTra, yTra, false)) {
            TrajectoryResult.BELOW, TrajectoryResult.HIT -> xTra += 1
            TrajectoryResult.RIGHT -> { testTrajectory(xTra-1, yTra); println("Found id: ${xTra-1}, $yTra"); break }
        }
    }*/

    /*while(true) {
        println("2 Numbers")
        val horizontal = readln().toInt()
        val vertical = readln().toInt()
        testTrajectory(horizontal, vertical)
    }*/

}

fun Int.gaussSum() = (this * this + this)/2
*/

class Trajectory(var horizontal: Int, var vertical: Int) {
    val positions: List<Pair<Int, Int>>
    val result: TrajectoryResult

    init {
        val tempPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val probe = Probe().apply { initVelocity(horizontal, vertical) }
        while(true) {
            if(probe.yPos < targetY.minOf { it }) { result = TrajectoryResult.BELOW; break }
            if(probe.xPos > targetX.maxOf { it }) { result = TrajectoryResult.RIGHT; break }
            if(targetX.contains(probe.xPos) && targetY.contains(probe.yPos)) { result = TrajectoryResult.HIT; break }
            probe.step()
            tempPositions.add(probe.xPos to probe.yPos)
        }
        positions = tempPositions.toList()
    }

    fun print(mapStep: Int = 1) {
        val xMax = (targetX.toList() + positions.map { it.first } + listOf(0)).maxOf { it }
        val yValues = targetY.toList() + positions.map { it.second } + listOf(0)
        val yMax = yValues.maxOf { it }
        val yMin = yValues.minOf { it }

        for(y in yMax downTo yMin step mapStep) {
            for(x in 0..xMax step mapStep) {
                if(x == 0 && y == 0) print("S")
                else if(positions.contains(x to y)) print("#")
                else if(targetX.contains(x) && targetY.contains(y)) print("T")
                else print(".")
            }
            println()
        }
    }

    override fun toString() = "Trajectory(horizontal=$horizontal, vertical=$vertical, returnValue=$result, positions=$positions)"
}

class Probe {
    var xPos: Int = 0
    var yPos: Int = 0
    var xVel: Int = 0
    var yVel: Int = 0

    fun initVelocity(horizontal: Int, vertical: Int) {
        xVel = horizontal
        yVel = vertical
    }

    fun step() {
        xPos += xVel
        yPos += yVel
        if(xVel > 0) xVel -= 1
        if(xVel < 0) xVel += 1
        yVel -= 1
    }
}

enum class TrajectoryResult { BELOW,RIGHT,HIT }

//endregion
//region Day18

fun day18(inputLines: List<String>) {
    val snailFishNumberLRs = inputLines.map { SnailFishNumberLR.of(it) }

    //Test Deserialization, Representation-Transformation, Serialization of Numbers
    inputLines.zip(snailFishNumberLRs).forEach { (inputLine, snailFishNumber) ->
        if(inputLine != snailFishNumber.recursiveRepresentation().toString()) throw AssertionError("DRTS-Test failed: $inputLine != ${snailFishNumber.recursiveRepresentation()}")
    }

    println("Result Magnitude: ${snailFishNumberLRs.reduce(SnailFishNumberLR::plus).magnitude()}")

    val allSums = mutableListOf<SnailFishNumberLR>()
    for(i in snailFishNumberLRs.indices) {
        for(j in snailFishNumberLRs.indices) {
            if(i == j) continue
            allSums.add((snailFishNumberLRs[i] + snailFishNumberLRs[j]))
        }
    }
    println("Largest Magnitude: ${allSums.map { it.magnitude() }.maxOf { it }}")
}

class SnailFishNumberLR(private val digits: MutableList<SnailFishNumberLSDigit>) {
    companion object {
        fun of(str: String): SnailFishNumberLR {
            val digitList: MutableList<SnailFishNumberLSDigit> = mutableListOf()
            var currentDepth = 0
            val strIterator = str.iterator()
            while(strIterator.hasNext()) {
                when(val next = strIterator.next()) {
                    '[' -> currentDepth++
                    ']' -> currentDepth--
                    ',' -> {}
                    in '0'..'9' -> digitList.add(SnailFishNumberLSDigit(next.digitToInt().toLong(), currentDepth))
                }
            }
            if(currentDepth != 0) throw AssertionError("ParsingError - depth is $currentDepth at end of Formula")
            return SnailFishNumberLR(digitList)
        }
    }

    operator fun plus(other: SnailFishNumberLR) = SnailFishNumberLR((digits + other.digits).map { it.clone() }.onEach { it.depth++ }.toMutableList()).apply { reduce() }

    fun reduce() { @Suppress("ControlFlowWithEmptyBody") while(reduceStep()) { } }

    private fun reduceStep() = tryExplode() || trySplit()

    private fun tryExplode(): Boolean {
        for((index, digit) in digits.withIndex()) {
            if(digit.depth > 4 && digit.depth == digits[index+1].depth) {
                digits.getOrNull(index-1)?.run { value += digit.value } //add to left
                digits.getOrNull(index+2)?.run { value += digits[index+1].value } //add to right

                digit.run { value = 0; digit.depth-- } //edit first: replace by 0
                digits.removeAt(index+1) //remove second

                return true
            }
        }
        return false
    }

    private fun trySplit(): Boolean {
        for((index, digit) in digits.withIndex()) {
            if(digit.value >= 10) {
                val newDepth = digit.depth+1
                val lowerValue = floor(digit.value.toDouble()/2).toLong()
                val higherValue = ceil(digit.value.toDouble()/2).toLong()

                digit.run { depth = newDepth; value = lowerValue } //edit first
                digits.add(index+1, SnailFishNumberLSDigit(higherValue, newDepth)) //add second
                return true
            }
        }
        return false
    }

    fun magnitude() = recursiveRepresentation().magnitude()

    fun recursiveRepresentation() = rRInternal()

    //beautiful... naaah!
    private fun rRInternal(currentIndex: Int = 1, currentDigit: SnailFishNumberLSDigit? = null, nextDigits: Iterator<SnailFishNumberLSDigit> = digits.iterator()): SnailFishNumberRR {
        var curr = currentDigit ?: if(nextDigits.hasNext()) nextDigits.next() else throw AssertionError("Couldn't parse first element: curI=$currentIndex, curD=$currentDigit, digits=$digits")
        if(curr.depth == 0) { if(!nextDigits.hasNext()) return curr.toSnailFishRegular() else throw AssertionError("If Number contains depth=0 Digit no other digit may occur! digits=$digits") }
        val firstContent = if(currentIndex < curr.depth) rRInternal(currentIndex+1, curr, nextDigits) else curr.toSnailFishRegular()
        if(!nextDigits.hasNext()) throw AssertionError("Couldn't parse second element: first=$firstContent, curI=$currentIndex, curD=$currentDigit, digits=$digits")
        curr = nextDigits.next()
        val secondContent = if(currentIndex < curr.depth) rRInternal(currentIndex+1, curr,  nextDigits) else curr.toSnailFishRegular()
        return SnailFishNumberRRPair(firstContent, secondContent)
    }

    override fun toString(): String = "SnailFishNumberLR(digits=$digits)"
}

class SnailFishNumberLSDigit(var value: Long, var depth: Int) {
    fun toSnailFishRegular() = SnailFishNumberRRDigit(value)
    override fun toString(): String {
        return "SFD(v=$value, d=$depth)"
    }
    fun clone() = SnailFishNumberLSDigit(value, depth)
}

interface SnailFishNumberRR {
    fun magnitude(): Long
    fun listRepresentation(): SnailFishNumberLR = SnailFishNumberLR(toDigitList().toMutableList())
    fun toDigitList(): List<SnailFishNumberLSDigit>
}

class SnailFishNumberRRPair(val first: SnailFishNumberRR, val second: SnailFishNumberRR): SnailFishNumberRR {
    override fun magnitude(): Long = 3*first.magnitude() + 2*second.magnitude()
    override fun toString(): String = "[$first,$second]"
    override fun toDigitList(): List<SnailFishNumberLSDigit> = (first.toDigitList() + second.toDigitList()).onEach { it.depth++ }
}
class SnailFishNumberRRDigit(private val value: Long): SnailFishNumberRR {
    override fun magnitude(): Long = value
    override fun toString(): String = value.toString()
    override fun toDigitList(): List<SnailFishNumberLSDigit> = mutableListOf(SnailFishNumberLSDigit(value, 0))
}

//endregion
//region Day19

fun day19(inputLines: List<String>) {
    val trenchMap = TrenchMap.of(inputLines.iterator())

    println("Number of Beacons: ${trenchMap.beaconPositions.size}")
    println("Maximum scanner distance: ${trenchMap.maximumScannerDistance()}")
}

class TrenchMap(scanners: List<Scanner>) {
    companion object {
        fun of(input: Iterator<String>): TrenchMap {
            val scanners = mutableListOf<Scanner>()
            var beaconPosList = mutableListOf<PositionVektor3D>()
            while(input.hasNext()) {
                val next = input.next()
                when {
                    next.isBlank() -> scanners.add(Scanner(beaconPosList))
                    next.contains("---") -> beaconPosList = mutableListOf()
                    next.contains(",") -> beaconPosList.add(PositionVektor3D.of(next))
                }
            }
            if(beaconPosList.isNotEmpty()) scanners.add(Scanner(beaconPosList))
            return TrenchMap(scanners)
        }
    }

    val scannerPositions = mutableMapOf<PositionVektor3D, Scanner>()
    val beaconPositions = mutableSetOf<PositionVektor3D>()

    init {
        println("|---Parsing" + "-".repeat(scanners.size-10) + "|")
        print("|-")

        val remaining: Queue<Scanner> = LinkedList(scanners)
        addScanner(remaining.poll(), PositionVektor3D(0,0,0))

        while(remaining.isNotEmpty()) {
            val next = remaining.poll()

            var matched = false
            for((knownScannerPos, knownScanner) in scannerPositions) {
                val nextRelPos = knownScanner.match(next)
                if(nextRelPos != null) {
                    print("-")
                    addScanner(next, knownScannerPos + nextRelPos)
                    matched = true
                    break
                }
            }
            if(!matched) remaining.add(next)
        }
        println("|")
        println("|---Done" + "-".repeat(scanners.size-7) + "|")
        println()
    }

    private fun addScanner(scanner: Scanner, position: PositionVektor3D) {
        scannerPositions[position] = scanner
        scanner.beaconPositions.forEach { relPos -> beaconPositions.add(position + relPos) }
    }

    fun maximumScannerDistance(): Int {
        if(scannerPositions.isEmpty()) return -1
        var currMaxDist: Int = 0
        for((position1, _) in scannerPositions) {
            for ((position2, _) in scannerPositions) {
                currMaxDist = max(position1.hamiltonDistance(position2), currMaxDist)
            }
        }
        return currMaxDist
    }
}

class Scanner(val beaconPositions: List<PositionVektor3D>) {

    /**
     * @returns the relative position if possible
     */
    fun match(other: Scanner): PositionVektor3D? {
        for(rotation in RelativeRotation.all()) {
            val rotatedOther = other.asRotated(rotation)
            for (otherBeaconPos in rotatedOther.beaconPositions) {
                for (myBeaconPos in beaconPositions) {
                    val relativeScannerPosition = myBeaconPos + otherBeaconPos.asBackwards()
                    if (verifyPosition(rotatedOther, relativeScannerPosition)) {
                        other.applyRotation(rotation)
                        return relativeScannerPosition
                    }
                }
            }
        }
        return null
    }

    private fun verifyPosition(other: Scanner, relativeScannerPosition: PositionVektor3D): Boolean {
        var matchCount = 0

        for (otherBeaconPos in other.beaconPositions) {
            if (beaconPositions.contains(relativeScannerPosition + otherBeaconPos)) {
                matchCount++
            }
        }
        return matchCount >= 12
    }

    fun asRotated(relativeRotation: RelativeRotation) = this.clone().applyRotation(relativeRotation)
    fun applyRotation(relativeRotation: RelativeRotation): Scanner {
        return this.turnSideUp(relativeRotation.newTop).rotate(Axis.Z, relativeRotation.rotations)
    }

    private fun turnSideUp(side: Side): Scanner {
        when(side) {
            Side.TOP -> {}
            Side.BOT -> rotate(Axis.Y, 2) //not same as rotate(Axis.X, 2)
            Side.LEFT -> rotate(Axis.Y, 1)
            Side.RIGHT -> rotate(Axis.Y, 3)
            Side.FRONT -> rotate(Axis.X, 1)
            Side.BACK -> rotate(Axis.X, 3)
        }
        return this
    }

    private fun rotate(fix: Axis, amount: Int = 1): Scanner {
        beaconPositions.forEach { it.rotate(fix, amount) }
        return this
    }

    fun clone() = Scanner(beaconPositions.map { it.clone() }.toList())

    override fun toString() = "S($beaconPositions)"
}

class PositionVektor3D(var x: Int, var y: Int, var z: Int) {
    companion object {
        fun of(s: String) = s.split(',').map { it.toInt() }.run { PositionVektor3D(get(0), get(1), get(2)) }
    }

    operator fun plus(other: PositionVektor3D) = PositionVektor3D(x + other.x, y + other.y, z + other.z)
    operator fun plusAssign(other: PositionVektor3D) {
        x += other.x
        y += other.y
        z += other.z
    }

    fun asRotated(fix: Axis, amount: Int = 1) = this.clone().rotate(fix, amount)
    fun rotate(fix: Axis, amount: Int = 1): PositionVektor3D {
        for (i in 0 until amount) {
            when(fix) {
                Axis.X -> y = z.also { z = -y }
                Axis.Y -> z = x.also { x = -z }
                Axis.Z -> x = y.also { y = -x }
            }
        }
        return this
    }

    fun asBackwards() = this.clone().backwards()
    fun backwards() = this.apply { x*=-1; y*=-1; z*=-1;  }

    fun hamiltonDistance(other: PositionVektor3D): Int = abs(x-other.x) + abs(y-other.y) + abs(z-other.z)

    fun clone() = PositionVektor3D(x,y,z)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PositionVektor3D

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

    override fun toString(): String {
        return "(x=$x, y=$y, z=$z)"
    }
}

class RelativeRotation(val newTop: Side, val rotations: Int) {
    companion object { fun all() = Side.values().flatMap { side -> (0..3).map { RelativeRotation(side, it) } } }
}

enum class Side { TOP,BOT,LEFT,RIGHT,FRONT,BACK }
enum class Axis { X,Y,Z }

//endregion
//region Day20

fun day20(inputLines: List<String>) {
    val enhancementStateArray = inputLines.first().toCharArray().map { PixelState.of(it) }.toTypedArray()
    val initialImage = inputLines.drop(2)
    val nonCoreState = PixelState.DARK

    val infiniteImage = InfiniteImage.of(initialImage, nonCoreState)
    var scoreAfter2Enhancements: Any? = null

    println("Initial:")
    infiniteImage.print()
    println()
    for(i in 0 until 50) {
        println("enhancing: $i")
        infiniteImage.enhance(enhancementStateArray)
        infiniteImage.print()
        if(i == 2) scoreAfter2Enhancements = infiniteImage.getLitPixels()
        println()
    }
    println("Number of Lit Pixels ( 2): $scoreAfter2Enhancements")
    println("Number of Lit Pixels (50): ${infiniteImage.getLitPixels()}")
}

class InfiniteImage(private var lightCorePixels: MutableSet<Position2D>, private var nonCoreState: PixelState = PixelState.DARK) {
    companion object {
        fun of(value: List<String>, nonCoreState: PixelState = PixelState.DARK): InfiniteImage {
            val lightCorePixels: MutableSet<Position2D> = mutableSetOf()
            for((y,line) in value.withIndex()) {
                for ((x,char) in line.withIndex()) {
                    if(PixelState.of(char) == PixelState.LIGHT) lightCorePixels.add(Position2D(x,y))
                }
            }
            return InfiniteImage(lightCorePixels, nonCoreState)
        }
    }

    private val coreXRange
        get(): IntRange {
            //println(lightCorePixels.size)
            val min = lightCorePixels.minOf { it.x }
            val max = lightCorePixels.maxOf { it.x }
            return min..max
        }
    private val coreYRange get() = lightCorePixels.minOf { it.y }..lightCorePixels.maxOf { it.y }

    fun getLitPixels() = if(nonCoreState == PixelState.DARK) lightCorePixels.size else Float.POSITIVE_INFINITY

    fun enhance(lookupTable: Array<PixelState>, squareSideLength: Int = 3) {
        val newLightPixels: MutableSet<Position2D> = mutableSetOf()
        val cornerOffset = squareSideLength/2
        for(y in coreYRange.first-cornerOffset..coreYRange.last+cornerOffset) {
            for(x in coreXRange.first-cornerOffset..coreXRange.last+cornerOffset) {
                val targetPosition = Position2D(x,y)
                //val listSquare = getSquareAround(targetPosition)
                //val ratingSquare = listSquare.stateSquareToRating()
                //val newState = lookupNewPixel(lookupTable, ratingSquare)

                //println("$targetPosition - $listSquare - $ratingSquare - $newState")

                if(lookupNewPixel(lookupTable, getSquareAround(targetPosition).stateSquareToRating()) == PixelState.LIGHT)
                    newLightPixels.add(targetPosition)
            }
        }

        //println("${coreXRange.first} $squareSideLength - ${coreYRange.first} $squareSideLength")
        val outsidePosition = Position2D(coreXRange.first-squareSideLength, coreYRange.first-squareSideLength)

        nonCoreState = lookupNewPixel(lookupTable, getSquareAround(outsidePosition).stateSquareToRating())
        lightCorePixels = newLightPixels
    }

    private fun lookupNewPixel(lookupTable: Array<PixelState>, number: Int) = lookupTable.getOrNull(number) ?: throw IllegalStateException(
        if((0 until 512).contains(number)) "Lookup failed: lookupTable too small: ${lookupTable.size}"
        else "Lookup failed: number out of range $number"
    )

    private fun getState(position: Position2D): PixelState {
        if(!isCore(position)) return nonCoreState
        if(lightCorePixels.contains(position)) return PixelState.LIGHT
        return PixelState.DARK
    }

    private fun getSquareAround(center: Position2D, sideLength: Int = 3): List<PixelState> {
        val resultingStates = mutableListOf<PixelState>()
        val cornerOffset = sideLength/2
        for (y in center.y-cornerOffset..center.y+cornerOffset) {
            for (x in center.x-cornerOffset..center.x+cornerOffset) {
                resultingStates.add(getState(Position2D(x,y)))
            }
        }
        return resultingStates.toList()
    }

    private fun List<PixelState>.stateSquareToRating() = map { if(it == PixelState.LIGHT) '1' else '0' }.joinToString("").toInt(2)

    private fun isCore(position: Position2D): Boolean = coreXRange.contains(position.x) && coreYRange.contains(position.y)

    fun print() {
        for(y in coreYRange) {
            for(x in coreXRange) {
                print(if(getState(Position2D(x,y)) == PixelState.LIGHT) '#' else '.')
            }
            println()
        }
        //println(this)
    }

    override fun toString(): String {
        return "InfiniteImage(lightCorePixels=$lightCorePixels, nonCoreState=$nonCoreState)"
    }
}

class Position2D(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Position2D

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "Position2D(x=$x, y=$y)"
    }
}

enum class PixelState {
    LIGHT, DARK;
    companion object { fun of(value: Char) = if(value == '.') DARK else LIGHT }
}

//endregion
//region Day21

//Player 1 starting position: 1
//Player 2 starting position: 6

fun day21() {
    val initialGameState = GameState(
        listOf(
            PlayerState(0, 0),
            PlayerState(0, 5),
        ),
        0
    )

    //beautiful blob of code
    val usedDeterministicDice = DeterministicDice()
    val deterministicResults = GameController.executeDeterministicGame(initialGameState, 1000, dice = usedDeterministicDice)
        .mapIndexed { index, player -> (index+1) to player.points }
    println("DeterministicDice - Results are:")
    deterministicResults.forEach { (playerID, points) -> println("Player $playerID: $points Points") }
    val sortedDeterministicResults = deterministicResults.sortedByDescending { it.second }
    sortedDeterministicResults.firstOrNull()?.run { println("And the winner is: Player $first with $second Points") }
        ?: println("Winner couldn't be determined!")
    sortedDeterministicResults.lastOrNull()?.run { println("Metric is: ${usedDeterministicDice.numberOfRolls * second }") }
        ?: println("Loser couldn't be determined! Therefore Metric couldn't be calculated!")

    println()

    //another beautiful blob of code
    val diracResults = GameController.executeDiracGame(initialGameState, 21)
        .mapIndexed { index, points -> (index+1) to points }
    println("DiracDie - Results are:")
    diracResults.forEach { (playerID, points) -> println("Player $playerID: $points Wins") }
    val diracWinner = diracResults.maxByOrNull { it.second }
    if(diracWinner != null) println("And the winner is: Player ${diracWinner.first} with ${diracWinner.second} Wins")
    else println("There is no winner!")
}

object GameController {

    fun executeDeterministicGame(initialState: GameState, minPointsToWin: Int, boardSize: Int = 10, dice: DeterministicDice = DeterministicDice()): List<PlayerState> {
        var currentGameState: GameState = initialState

        while(currentGameState.players.indexOfFirst { it.points >= minPointsToWin } == -1) {
            currentGameState = executeState(currentGameState, dice, boardSize)
                .firstNotNullOfOrNull { if(it.second == 1L) it.first else null }
                ?: throw IllegalStateException("deterministic dice should always return 1 Universe")
        }

        return currentGameState.players
    }

    fun executeDiracGame(initialState: GameState, minPointsToWin: Int, boardSize: Int = 10): List<Long> {
        val winCounter = MutableList(initialState.players.size) { 0L }
        val pendingStates: MutableMap<GameState, Long> = mutableMapOf(initialState to 1L)

        while(pendingStates.isNotEmpty()) {
            //println(pendingStates.size)
            val (nextState, nextEncodedGameCount) = pendingStates.entries.first()
            pendingStates.remove(nextState) ?: throw AssertionError("has to be removed $nextState should have been in $pendingStates")

            val winnerIndex = nextState.players.indexOfFirst { it.points >= minPointsToWin }
            if(winnerIndex != -1) {
                winCounter[winnerIndex] += nextEncodedGameCount
                continue
            }

            executeState(nextState, DiracDie, boardSize).forEach { (resultingState, multiplicity) ->
                val oldMultiplicity = pendingStates.getOrDefault(resultingState, 0L)
                pendingStates[resultingState] = oldMultiplicity + multiplicity * nextEncodedGameCount
            }
        }

        return winCounter
    }

    fun executeState(state: GameState, dice: Dice, boardSize: Int = 10): List<Pair<GameState, Long>> =
        dice.rollThreeTimes().map { (diceValue, diceValueMultiplicity) ->
            GameState(
                state.players.mapIndexed { index, player ->
                    var points = player.points
                    var position = player.position

                    if(index == state.currPlayerIndex) {
                        position = (position + diceValue) % boardSize
                        points += position + 1
                    }

                    PlayerState(points, position)
                },
                (state.currPlayerIndex + 1) % state.players.size,
            ) to diceValueMultiplicity.toLong()
        }
}

interface Dice {
    fun rollThreeTimes(): List<Pair<Int, Int>>
}

class DeterministicDice(initValue: Int = 0, val numberOfValues: Int = 100): Dice {
    var lastValue = initValue % numberOfValues
    var numberOfRolls = 0

    override fun rollThreeTimes(): List<Pair<Int, Int>> = listOf(
        (rollOnce() + rollOnce() + rollOnce()) to 1
    )

    private fun rollOnce(): Int {
        lastValue = (lastValue + 1) % numberOfValues
        numberOfRolls++
        return lastValue
    }

}

object DiracDie: Dice {
    override fun rollThreeTimes() = listOf(
        3 to 1,
        4 to 3,
        5 to 6,
        6 to 7,
        7 to 6,
        8 to 3,
        9 to 1,
    )
}

data class GameState(
    val players: List<PlayerState>,
    val currPlayerIndex: Int,
)

data class PlayerState(
    val points: Int,
    val position: Int,
)

/*
fun day21_1() {
    var p1Points = 0
    var p1Position = 0
    var p2Points = 0
    var p2Position = 5

    var rollCounter = 0
    var p1Turn = true
    var currDiceValue = 0

    while(p1Points<1000 && p2Points<1000) {
        var currentRollResult = 0
        for(i in 0 until 3) {
            currentRollResult += currDiceValue + 1
            currDiceValue = (currDiceValue + 1) % 100
            rollCounter++
        }
        if(p1Turn) {
            p1Position = (p1Position + currentRollResult) % 10
            p1Points += p1Position + 1
        } else {
            p2Position = (p2Position + currentRollResult) % 10
            p2Points += p2Position + 1
        }
        p1Turn = !p1Turn
    }

    if(p1Points > p2Points) {
        println("P1 Won")
        println("Metric: ${p2Points*rollCounter}")
    } else {
        println("P2 Won")
        println("Metric: ${p1Points*rollCounter}")
    }
}
*/


//endregion
//region Day22

fun day22(inputLines: List<String>) {
    val instructions = inputLines.map {
        val (state, rangeX, rangeY, rangeZ) = it.split(" ", ",")

        ReactorInstruction(
            ReactorNodeState.valueOf(state.uppercase()),
            Cube(
                SimpleIntRange.of(rangeX.substring(2)),
                SimpleIntRange.of(rangeY.substring(2)),
                SimpleIntRange.of(rangeZ.substring(2)),
            ),
        )
    }

    for(instruction in instructions.take(20)) { ReactorController.executeInstruction(instruction) }
    println("On after 20 instructions: ${ReactorController.activatedNodes}")

    for(instruction in instructions.drop(20)) { ReactorController.executeInstruction(instruction) }
    println("On after all instructions: ${ReactorController.activatedNodes}")
}

object ReactorController {
    private val fixedNodeCubes: MutableList<BreakableCube> = mutableListOf()

    val activatedNodes get() = fixedNodeCubes.map { it.size }.reduce(Long::plus)

    fun executeInstruction(instruction: ReactorInstruction) {
        when(instruction.targetState) {
            ReactorNodeState.ON -> {
                val instructionCube = BreakableCube(instruction.targetArea)
                for(fixed in fixedNodeCubes) {
                    instructionCube.breakRespecting(fixed)
                }
                fixedNodeCubes.add(instructionCube)
            }
            ReactorNodeState.OFF -> {
                for(fixed in fixedNodeCubes) {
                    fixed.removeAt(instruction.targetArea)
                }
            }
        }
    }
}

class BreakableCube(val base: Cube) {
    private val ignoredParts: MutableList<BreakableCube> = mutableListOf()

    val size: Long get() = base.size - (ignoredParts.map { it.size }.reduceOrNull(Long::plus) ?: 0L)

    /**
     * Assumes that the fixed Cube already broke (or broke by) every cube that previously broke this cube
     */
    fun breakRespecting(fixed: BreakableCube) {
        if(!base.intersects(fixed.base)) return

        val baseIntersection = BreakableCube(base.intersection(fixed.base))

        for (fixedIgnoredPart in fixed.ignoredParts) {
            baseIntersection.breakRespecting(fixedIgnoredPart)
        }

        ignoredParts.add(baseIntersection)
    }

    fun removeAt(target: Cube) {
        if(!base.intersects(target)) return

        val targetIntersection = BreakableCube(base.intersection(target))

        for (ignoredPart in ignoredParts) {
            targetIntersection.breakRespecting(ignoredPart)
        }

        ignoredParts.add(targetIntersection)
    }
}

data class ReactorInstruction(
    val targetState: ReactorNodeState,
    val targetArea: Cube,
)

enum class ReactorNodeState { ON, OFF }

data class Cube(
    val xRange: SimpleIntRange,
    val yRange: SimpleIntRange,
    val zRange: SimpleIntRange,
) {
    val size: Long get() = xRange.size * yRange.size * zRange.size

    fun contains(point3d: Point3d): Boolean = xRange.contains(point3d.x) && yRange.contains(point3d.y) && zRange.contains(point3d.z)
    fun intersects(other: Cube): Boolean = xRange.intersects(other.xRange) && yRange.intersects(other.yRange) && zRange.intersects(other.zRange)

    fun intersection(other: Cube): Cube = Cube(xRange.intersection(other.xRange), yRange.intersection(other.yRange), zRange.intersection(other.zRange))
}

data class Point3d(
    val x: Int,
    val y: Int,
    val z: Int,
)

class SimpleIntRange(
    val first: Int,
    val last: Int,
) {
    companion object {
        fun of(s: String): SimpleIntRange {
            val (first,last) = s.split("..")
            return SimpleIntRange(first.toInt(), last.toInt())
        }
    }

    val size: Long get() = last.toLong() - first.toLong() + 1L

    fun intersects(other: SimpleIntRange): Boolean = max(first, other.first) <= min(last, other.last)
    fun contains(value: Int): Boolean = value in first..last
    fun isEmpty() = first > last
    fun isNotEmpty() = !isEmpty()

    fun intersection(other: SimpleIntRange): SimpleIntRange = SimpleIntRange(max(first, other.first), min(last, other.last))

    override fun equals(other: Any?): Boolean =
        other is IntRange && (isEmpty() && other.isEmpty() || first == other.first && last == other.last)

    override fun hashCode(): Int {
        var result = first
        result = 31 * result + last
        return result
    }
}

/*
fun day22_1_2test(inputLines: List<String>) {
    //1
    val instructions = inputLines.map {
        val (state, rangeX, rangeY, rangeZ) = it.split(" ", ",")

        ReactorInstruction(
            ReactorNodeState.valueOf(state.uppercase()),
            Cube(
                SimpleIntRange.of(rangeX.substring(2)),
                SimpleIntRange.of(rangeY.substring(2)),
                SimpleIntRange.of(rangeZ.substring(2)),
            ),
        )
    }

    //3 loops -> find actual value from bottom to top in instructions -> add it to counter of that value (ON-counter, OFF-counter)
    val reversedInstructions = instructions.reversed()
    var onCounter = 0
    var offCounter = 0

    for(x in -50..50) {
        for(y in -50..50) {
            point@for(z in -50..50) {
                val point = Point3d(x,y,z)
                for(instruction in reversedInstructions) {
                    if(instruction.targetArea.contains(point)) {
                        when(instruction.targetState) {
                            ReactorNodeState.ON -> { onCounter++; continue@point }
                            ReactorNodeState.OFF -> { offCounter++; continue@point }
                        }
                    }
                }
                offCounter++
            }
        }
    }

    println("On: $onCounter")
    println("Off: $offCounter")
    println("Sum is 1030301 (101*101*101)? ${onCounter + offCounter == 1030301}")

    //2test
    var max = 0
    var sum = 0
    for((baseCounter, baseInstruction) in instructions.withIndex()) {
        print("$baseCounter -> ")
        var intersectCount = 0;
        for(otherInstruction in instructions) {
            //print("piep")
            if(baseInstruction.targetArea.intersects(otherInstruction.targetArea)) {
                //print("++")
                intersectCount++
            }
            //println()
        }
        sum += intersectCount
        print(intersectCount)
        if(intersectCount >= max) {
            println(" NEWMAX")
            max = intersectCount
        }
        else println()
    }
    println("Sum: $sum")

}
*/

//endregion
//region Day23

/*
#############
#...........#
###D#C#A#B###
  #C#D#A#B#
  #########
*/

fun day23() {
    val initialState = listOf(
        Amphipod(AmphipodId.A,AmphipodPosition.ROOM_C1), Amphipod(AmphipodId.A,AmphipodPosition.ROOM_C2),
        Amphipod(AmphipodId.B,AmphipodPosition.ROOM_D1), Amphipod(AmphipodId.B,AmphipodPosition.ROOM_D1),
        Amphipod(AmphipodId.C,AmphipodPosition.ROOM_A2), Amphipod(AmphipodId.C,AmphipodPosition.ROOM_B1),
        Amphipod(AmphipodId.D,AmphipodPosition.ROOM_A1), Amphipod(AmphipodId.D,AmphipodPosition.ROOM_B2),
    )


}

data class Amphipod(
    val id: AmphipodId,
    val position: AmphipodPosition,
)

enum class AmphipodId(val scoreMultiplicator: Int) {
    A(1),
    B(10),
    C(100),
    D(1000)
}

enum class AmphipodPosition {
    HW_A1, HW_A2,
    ROOM_A1, ROOM_A2,
    HW_AB,
    ROOM_B1, ROOM_B2,
    HW_BC,
    ROOM_C1, ROOM_C2,
    HW_CD,
    ROOM_D1, ROOM_D2,
    HW_D1, HW_D2,
    ;
}

//endregion
//region Day24

fun day24(inputLines: List<String>) {}

//endregion
//region Day25

fun day25(inputLines: List<String>) {}

//endregion
