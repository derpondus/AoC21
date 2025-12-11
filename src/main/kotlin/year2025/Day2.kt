package year2025

import java.util.stream.LongStream.range
import kotlin.math.abs
import kotlin.streams.asSequence


fun day2(inputLines: List<String>) {
    val numSequence = inputLines.asSequence()
        .flatMap { it.split(',') }
        //.drop(4)
        //.take(1)
        .flatMap {
            val (from, to) = it.split('-')
            //println("from: $from, to: $to")
            range(from.toLong(), to.toLong()+1).asSequence()
        }
    val pt1Sum = numSequence
        .sumOf {
            val num = it.toString()
            if (num.length % 2 == 1) return@sumOf 0
            val firstPart = num.take(num.length / 2)
            val secondPart = num.takeLast(num.length / 2)
            if (firstPart != secondPart) return@sumOf 0
            it
        }

    println("[Part1] Number of matches: $pt1Sum")
    // (1) 793 (is the count, i should have summed)
    // (2) 18595663903 (correct)

    val pt2Sum = numSequence
    // app2
        .sumOf {
            val num = it.toString()
            // 12234512541
            // go through patternLength 1 to floor(num.length/2)
            //  if num.length / pl -> int
            //  yes? if pattern * (num.length / pl) == num
            //      yes? is splittable -> return it
            //      no? next
            //  no? next
            // end: not splittable = real = return 0
            for (pl: Int in 1 until Math.floorDiv(num.length, 2) + 1) {
                if (num.length % pl != 0) continue
                val rebuildNum = num.take(pl).repeat(num.length / pl)
                if (num == rebuildNum) return@sumOf it
            }
            0
        }


    println("[Part2] Number of matches: $pt2Sum")
    // (1)  5_147_024_348 (too low) (should have known that: it's lower than part 1)
    // app1 -> app2
    // (_)  4_951_403_915 (def too low) (ignores splits into two parts (off by one))
    // (2) 19_058_204_438 (correct)
}

/*
app1
.sumOf {
            val num = it.toString()
            // 12234512541
            // init: pattern = 1
            // take first -> check next: is equal?
            // yes? check next
            // no? make new pattern the current cache
            // end: pattern > length/2
            var pattern = num[0].toString()
            var idxInPattern = 0
            num.drop(1).forEachIndexed { i, c ->
                if (c == num[idxInPattern]) {
                    idxInPattern++
                    if (idxInPattern == pattern.length) idxInPattern = 0
                } else {
                    pattern = num.take(i) + c
                    idxInPattern = 0
                    if (i+1 > num.length/2) return@sumOf 0
                }
            }

            if (pattern.length >= num.length/2 || idxInPattern != 0) return@sumOf 0
            it
        }
 */