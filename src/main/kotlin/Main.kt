import java.io.BufferedReader
import java.io.FileReader
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.reflect.Modifier
import java.time.LocalDate
import kotlin.math.abs
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

val daysToRun: List<Int> = listOf()

//region main
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
        getFunctionFromFile("Main", "day$it")?.call()
            ?: getFunctionFromFile("Day$it", "day$it")?.call()
            ?: println("Method not found")
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

    Map().run {
        lineList.forEach { addLine(it, true) }
        println("No Diag: ${countMultiple()}")
    }

    Map().run {
        lineList.forEach { addLine(it) }
        println("All: ${countMultiple()}")
    }

}

class Map {
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
    val lineList = BufferedReader(FileReader("./src/main/resources/day7.txt")).readLines().map { Line.of(it) }


}

//endregion
//region Day8

fun day8() {}

//endregion
//region Day9

fun day9() {}

//endregion
//region Day10

fun day10() {}

//endregion
//region Day11

fun day11() {}

//endregion
//region Day12

fun day12() {}

//endregion
//region Day13

fun day13() {}

//endregion
//region Day14

fun day14() {}

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


