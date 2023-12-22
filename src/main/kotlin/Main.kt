import AoCYearConfig.Companion.asAoCYearConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.reflect.Modifier
import java.time.LocalDateTime
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

val daysToRun: List<String> = listOf("2023:12") //"2023:5"

//region main
const val sessionCookiePropertyID = "sessionCookie"

@ExperimentalTime
fun main(args: Array<String>) {
    (
            if(args.isNotEmpty()) args.map { it.asAoCYearConfig() }
            else if(daysToRun.isNotEmpty()) daysToRun.map { it.asAoCYearConfig() }
            else LocalDateTime.now().let { listOf(AoCYearConfig(it.year, listOf(it.minusHours(6).dayOfMonth))) }
    )
        .forEach { it.execute(); println() }
}

data class AoCYearConfig(val year: Int, val days: List<Int>) {
    companion object {
        fun String.asAoCYearConfig(): AoCYearConfig {
            val split = this.split(":", limit = 2)
            return AoCYearConfig(
                split[0].toInt(),
                split[1].split(",").map { it.toInt().coerceIn(1..25) }
            )
        }
    }

    @ExperimentalTime
    fun execute() {
        println("=".repeat(50))
        println("Year: $year")
        println("=".repeat(50))

        days.forEach {
            println("-".repeat(50))
            println("Day: $it")
            val func: KFunction<*> = getFunctionFromFile("year$year.Main", "day$it")
                ?: getFunctionFromFile("year$year.Day$it", "day$it")
                ?: run { System.err.println("Method not found for day: $it"); return@forEach }
            val data = if(func.parameters.size == 1) loadDataForEventDay(year, it) else null
            val time = data?.let { measureTime { func.call(it) } } ?: measureTime { func.call() }
            println(time.toComponents { hours, minutes, seconds, nanoseconds -> "Exec Time:" +
                    " ${hours.toString().padStart(2, '0')}" +
                    ":${minutes.toString().padStart(2, '0')}" +
                    ":${seconds.toString().padStart(2, '0')}" +
                    ".${nanoseconds.toString().padStart(9, '0')}"
            })
            println("-".repeat(50))
            println()
        }
    }

    private fun getFunctionFromFile(fileName: String, funcName: String): KFunction<*>? {
        val selfRef = ::getFunctionFromFile
        val currentClass = selfRef.javaMethod!!.declaringClass
        val classDefiningFunction = try { currentClass.classLoader.loadClass("${fileName}Kt") } catch(e: Throwable) { return null }
        val javaMethod  = classDefiningFunction.methods.find { it.name == funcName && Modifier.isStatic(it.modifiers)}
        return javaMethod?.kotlinFunction
    }

    private fun loadDataForEventDay(year: Int, day: Int): List<String> {
        val inputFile = File("./src/main/resources/event_$year/day$day.txt")
        inputFile.parentFile.mkdirs()
        if(!inputFile.createNewFile()) return BufferedReader(FileReader(inputFile)).readLines()
        val lines = ServerConnector.loadData(year, day)
        inputFile.writeText(lines.joinToString("\n"))
        return lines
    }
}

object ServerConnector {
    private val httpClient = HttpClient(CIO)

    fun loadData(year: Int, day: Int): List<String> {
        println("[Loading Input from Server]")
        var response: String
        runBlocking {
            response =
                httpClient.get("https://adventofcode.com/$year/day/$day/input") {
                    cookie("session", System.getenv(sessionCookiePropertyID))
                }.body()
        }
        return response.split("\n").run { subList(0,size-1) }
    }
}


//TODO: var watcher with frame -> day registers watcher -> displays current value of watcher
// |    watcher gets var reference -> loads value via reflection concurrently

//endregion