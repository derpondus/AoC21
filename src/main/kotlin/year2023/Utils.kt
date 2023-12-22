package year2023

import kotlin.math.abs

enum class Side { TOP,BOT,LEFT,RIGHT,FRONT,BACK }
enum class Axis { X,Y,Z }

class Point2(val x: Int, val y: Int) {
    companion object {
        private const val delimiter = ","
        fun of(s: String, delimiter: String = Point2.delimiter) = s.split(delimiter).let {
            Point2(it[0].toInt(),it[1].toInt())
        }
    }
    fun toString(delimiter: String) = "$x$delimiter$y"
    override fun toString() = "$x$delimiter$y"
}

class Line2(var start: Point2, var end: Point2) {
    companion object {
        private const val delimiter = " -> "
        fun of(s:String, delimiter: String = Line2.delimiter) = s.split(delimiter).let {
            Line2(Point2.of(it[0]), Point2.of(it[1]))
        }
    }
    fun isDiagonal() = abs(start.x-end.x) == abs(start.y-end.y)
    fun isHorizontal() = start.x == end.x
    fun isVertical() = start.y == end.y
    fun toString(delimiter: String) = "$start$delimiter$end"
    override fun toString() = "$start$delimiter$end"
}

class Point3(val x: Int, val y: Int, val z: Int) {
    companion object {
        private const val delimiter = ","
        fun of(s: String, delimiter: String = Point3.delimiter) = s.split(delimiter).let {
            Point3(it[0].toInt(),it[1].toInt(),it[2].toInt())
        }
    }
    fun print(delimiter: String) = "$x$delimiter$y$delimiter$z"
    override fun toString() = "$x$delimiter$y$delimiter$z"
}

class Line3(var start: Point3, var end: Point3) {
    companion object {
        private const val delimiter = " -> "
        fun of(s:String, delimiter: String = Line3.delimiter) = s.split(delimiter).let {
            Line3(Point3.of(it[0]), Point3.of(it[1]))
        }
    }
    fun isDiagonal() = abs(start.x-end.x) == abs(start.y-end.y) && abs(start.y-end.y) == abs(start.z-end.z)
    fun isXParallel() = start.x == end.x
    fun isYParallel() = start.y == end.y
    fun isZParallel() = start.z == end.z
    fun toString(delimiter: String) = "$start$delimiter$end"
    override fun toString() = "$start$delimiter$end"
}