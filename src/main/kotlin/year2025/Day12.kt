package year2025

import AOC_GUROBI_ENV
import addConstr
import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRB.DoubleAttr
import com.gurobi.gurobi.GRBModel
import com.gurobi.gurobi.GRBVar
import eq
import geq
import grbSum
import leq
import times
import unaryPlus
import kotlin.math.max

data class Present(val pattern: List<List<Boolean>>) {
    fun width(rotation: Rotation) = when(rotation) {
        Rotation.None, Rotation.OneHalf -> pattern.maxOf { it.size }
        Rotation.OneQuarter, Rotation.ThreeQuarters -> pattern.size
    }
    fun height(rotation: Rotation) = when(rotation) {
        Rotation.None, Rotation.OneHalf -> pattern.size
        Rotation.OneQuarter, Rotation.ThreeQuarters -> pattern.maxOf { it.size }
    }

    fun covers(rotation: Rotation, xOffset: Int, yOffset: Int): Boolean {
        val (relXIdx, relYIdx) = when(rotation) {
            Rotation.None -> Pair(xOffset, yOffset)
            Rotation.OneQuarter -> Pair(yOffset, width(rotation) - 1 - xOffset)
            Rotation.OneHalf -> Pair(width(rotation) - 1 - xOffset, height(rotation) - 1 - yOffset)
            Rotation.ThreeQuarters -> Pair(height(rotation) - 1 - yOffset, xOffset)
        }
        return pattern.getOrNull(relYIdx)?.getOrNull(relXIdx) ?: false
    }

    val blockCount by lazy { pattern.flatten().count { it } }
}

enum class Rotation {
    None,
    OneQuarter,
    OneHalf,
    ThreeQuarters;
}

data class Task(
    val region: Pair<Int, Int>,
    val presentCounts: Map<Present, Int>,
) {
    fun checkSimple(): Boolean {
        val regionBlocks = region.first * region.second
        val accPresentBlocks = presentCounts.entries.sumOf { (p, c) ->  p.blockCount * c }
        if (accPresentBlocks > regionBlocks) return false

        val globalConvexSquareSideLen = presentCounts.keys.maxOf { p -> max(p.width(Rotation.None), p.height(Rotation.None)) }
        val globalBlocksCount = (region.first / globalConvexSquareSideLen) * (region.second / globalConvexSquareSideLen)
        val globalPresentCount = presentCounts.values.sum()
        if (globalPresentCount <= globalBlocksCount) return true

        throw IllegalStateException("Problem is not simple")
    }
}

fun day12(inputLines: List<String>) {
    val data = inputLines
        .fold(mutableListOf(mutableListOf<String>())) { acc, line ->
            if (line.isNotBlank()) acc.also { it.last().add(line) }
            else acc.also { it.add(mutableListOf()) }
        }
    val presents = data.dropLast(1).map { Present(it.drop(1).map { line -> line.map { it == '#' } }) }
    val tasks = data.last()
        .map { it.split(": ") }
        .map { (regionStr, countsStr) ->
            Task(
            regionStr.split("x").let { Pair(it[0].toInt(), it[1].toInt()) },
            presents.zip(countsStr.split(" ").map { it.toInt() }).toMap()
            )
        }

    val p1Count = try {
        tasks.count { it.checkSimple() }
    } catch (e: IllegalStateException) {
        println(e.message)
        tasks.count { task ->
            val (model, _) = Day12ModelBuilder.buildModelP1(task.presentCounts, task.region)
            model.optimize()
            model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL
        }
        // OutOfMemoryError(Java Heap Space) (not a problem since it's not executed)
    }
    println("[Part1] Number of Lines: $p1Count")
    // 505 (correct)

    // Part2 doesn't exist
}

object Day12ModelBuilder {
    fun buildModelP1(presentCounts: Map<Present, Int>, region: Pair<Int, Int>): Pair<GRBModel, Map<List<Int>, GRBVar>> {
        val (regionWidth, regionHeight) = region
        val presents = presentCounts.keys.toList()

        // === Gurobi model ===
        val model = GRBModel(AOC_GUROBI_ENV)

        // --- Variables ---
        // Is-placed-at variables: whether the upId-th present is located at position with rotation
        val x = presents.withIndex().flatMap { (pId, present) ->
            val count = presentCounts[present]!!
            (0 until count).flatMap { upId ->
                Rotation.entries.withIndex().flatMap { (rId, rotation) ->
                    (0 until regionWidth - present.width(rotation)).flatMap { x ->
                        (0 until regionHeight - present.height(rotation)).map { y ->
                            val variable = model.addVar(
                                0.0,
                                1.0,
                                0.0,
                                GRB.BINARY,
                                "x_present_${pId}_${upId}_at_x${x}_y${y}_with_${rotation}_rotation"
                            )!!

                            listOf(pId, upId, rId, x, y) to variable
                        }
                    }
                }
            }
        }.toMap()

        model.update()  // update to make the variables known

        // --- Constraints ---
        // Each position can only be covered by at most one present
        (0 until regionWidth).forEach { xPos ->
            (0 until regionHeight).forEach { yPos ->
                val lhs = x.entries.grbSum { (key, v) ->
                    val (pId, _, rId, px, py) = key
                    val rotation = Rotation.entries[rId]
                    val present = presents[pId]

                    // Relative to px, py = top-left corner of the boundary of the present
                    val xOffset = xPos - px
                    val yOffset = yPos - py

                    val covers = present.covers(rotation, xOffset, yOffset)

                    covers.asInt() * v
                }
                model.addConstr(lhs leq 1, "coverage_at_x${xPos}_y${yPos}")!!
            }
        }

        // Parity Constraints: If I place a present, I have placed all previous presents of the same shape
        presents.forEachIndexed { pId, present1 ->
            val count1 = presentCounts[present1]!!
            (0 until count1).forEach { upId1 ->
                (0 until count1).filter { upId2 -> upId2 > upId1 }.forEach { upId2 ->
                    val lhs = x.filterKeys { (pIdKey, upIdKey, _, _, _) -> pIdKey == pId && upIdKey == upId1 }.values.grbSum { +it }
                    val rhs = x.filterKeys { (pIdKey, upIdKey, _, _, _) -> pIdKey == pId && upIdKey == upId2 }.values.grbSum { +it }
                    model.addConstr(lhs geq rhs, "parity_of_${present1}_${upId1}_and_${upId2}")!!
                }
            }
        }

        // Exclusion Constraints: Set exactly one of the placement variables to 1 per present+idx
        presents.forEachIndexed { pId, present1 ->
            val count1 = presentCounts[present1]!!
            (0 until count1).forEach { upId1 ->
                val lhs = x.filterKeys { (pIdKey, upIdKey, _, _, _) -> pIdKey == pId && upIdKey == upId1 }.values.grbSum { +it }
                model.addConstr(lhs eq 1, "exactly_one_of_${present1}_${upId1}")!!
            }
        }

        // --- Return the model ---
        model.update()
        return Pair(model, x)
    }
}
