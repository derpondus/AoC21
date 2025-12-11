package year2025

import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

data class Vec3D(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun of(repr: String): Vec3D {
            val (x, y, z) = repr.split(",")
            return Vec3D(x.toInt(), y.toInt(), z.toInt())
        }
    }

    val length by lazy { sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2)) }

    operator fun minus(vec3D: Vec3D): Vec3D {
        return Vec3D(x - vec3D.x, y - vec3D.y, z - vec3D.z)
    }
}

data class Box(val pos: Vec3D) {
    var circuit: Circuit = Circuit(listOf(this))

    fun distanceTo(other: Box): Double = (pos - other.pos).length
}

data class Circuit(
    val boxes: List<Box>
) {
    operator fun plus(other: Circuit): Circuit {
        return Circuit(boxes + other.boxes)
    }
}

fun day8(inputLines: List<String>) {
    val boxes = inputLines.map { Box(Vec3D.of(it)) }
    val sortedPairs = boxes
        .flatMapIndexed { idx, box1 -> boxes.drop(idx + 1).map { box2 -> box1 to box2 } }
        .sortedBy { (box1, box2) -> box1.distanceTo(box2) }

    //println(sortedPairs.take(2000).map { (box1, box2) -> box1.distanceTo(box2) })

    sortedPairs.take(1000).forEach { clusteringStep(it) }

    val p1Product = boxes.groupingBy { it.circuit }
        .eachCount()
        .values
        .sortedDescending()
        .take(3)
        .reduce { acc, count -> acc * count }

    println("[Part1] Product of 3 largest circuits: $p1Product")
    // 512 (too low) (only ever added the one closest not its cluster)
    // 66640 (correct)

    val lastMergingPair = sortedPairs.drop(1000).find {
        val newClusterSize = clusteringStep(it)
        newClusterSize == boxes.size
    }

    val p2Product = lastMergingPair?.let { (box1, box2) -> box1.pos.x * box2.pos.x }

    println("[Part2] X-Product of last merging-pair: $p2Product")
    // 78894156 (correct)
}

fun clusteringStep(boxPair: Pair<Box, Box>): Int {
    val (box1, box2) = boxPair
    if (box1.circuit == box2.circuit) { return box1.circuit.boxes.size }
    val newCircuit = box1.circuit + box2.circuit
    box1.circuit.boxes.forEach { it.circuit = newCircuit }
    box2.circuit.boxes.forEach { it.circuit = newCircuit }
    return newCircuit.boxes.size
}
