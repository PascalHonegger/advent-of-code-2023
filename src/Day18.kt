/*
Interesting experiment, but it is actually slightly slower than the simple data class approach.

@JvmInline
value class Point(private val value: Long) {
    constructor(x: Int, y: Int) : this((x.toLong() shl 32) or (y.toLong() and 0xffffffffL))

    val x get() = (value shr 32).toInt()
    val y get() = value.toInt()

    fun plusX(delta: Int) = Point(value + (delta.toLong() shl 32))
    fun plusY(delta: Int) = Point(x, y + delta) // Point(value + (delta.toLong() and 0xffffffffL))

    override fun toString() = "($x, $y)"
}*/

fun main() {
    data class Point(val x: Int, val y: Int) {
        fun plusX(delta: Int) = Point(x + delta, y)
        fun plusY(delta: Int) = Point(x, y + delta)
    }
    data class DigPlan(val direction: Char, val distance: Int)
    data class Corner(
        val point: Point,
        val plan: DigPlan,
        var previousPlan: DigPlan,
        var nextPlan: DigPlan,
        val previousPreviousPlan: DigPlan,
    )

    fun String.toDigPlan() = split(" ").let { (direction, distance) ->
        DigPlan(
            direction = direction[0],
            distance = distance.toInt(),
        )
    }

    fun String.toCorrectedDigPlan() = split(" ").let { (_, _, color) ->
        DigPlan(
            direction = color[7].let {
                when (it) {
                    '0' -> 'R'
                    '1' -> 'D'
                    '2' -> 'L'
                    '3' -> 'U'
                    else -> error("Unknown direction: $it")
                }
            },
            distance = color.substring(2..6).toInt(16),
        )
    }

    fun List<DigPlan>.drawOutline(): List<Point> {
        var current = Point(0, 0)
        val holes = mutableListOf<Point>()
        for (digPlan in this) {
            val next = when (digPlan.direction) {
                'U' -> (1..digPlan.distance).map { Point(x = current.x, y = current.y + it) }
                'D' -> (1..digPlan.distance).map { Point(x = current.x, y = current.y - it) }
                'L' -> (1..digPlan.distance).map { Point(x = current.x - it, y = current.y) }
                'R' -> (1..digPlan.distance).map { Point(x = current.x + it, y = current.y) }
                else -> error("Unknown direction: ${digPlan.direction}")
            }
            holes.addAll(next)
            current = next.last()
        }
        return holes
    }

    fun List<DigPlan>.drawCorners(): List<Corner> {
        var point = Point(0, 0)
        val corners = mutableListOf<Corner>()
        forEachIndexed { index, currentPlan ->
            val previousPreviousPlan = getOrElse(index - 2) { if (index == 0) get(lastIndex - 1) else get(lastIndex) }
            val previousPlan = getOrElse(index - 1) { get(lastIndex) }
            val nextPlan = getOrElse(index + 1) { get(0) }
            corners.add(Corner(point, currentPlan, previousPlan, nextPlan, previousPreviousPlan))
            point = when (currentPlan.direction) {
                'U' -> point.plusY(currentPlan.distance)
                'D' -> point.plusY(-currentPlan.distance)
                'L' -> point.plusX(-currentPlan.distance)
                'R' -> point.plusX(currentPlan.distance)
                else -> error("Unknown direction: ${currentPlan.direction}")
            }
        }
        return corners
    }

    fun List<DigPlan>.fillOutline(): Long {
        val corners = drawCorners()
        val cornersByPoint = corners.associateBy { it.point }
        val outlineByY = drawOutline().sortedBy { it.x }.groupBy { it.y }

        var coloredPoints = 0L
        val minY = corners.minOf { it.point.y }
        val maxY = corners.maxOf { it.point.y }

        for (y in maxY downTo minY) {
            var isEnclosed = false

            val outlinesOnThisRow = outlineByY.getValue(y)
            var currentOutlineIndex = 0

            var treatNextCornerAsHole = false

            while (currentOutlineIndex < outlinesOnThisRow.lastIndex) {
                val point = outlinesOnThisRow[currentOutlineIndex]
                val corner = cornersByPoint[point]
                if (corner != null && !treatNextCornerAsHole) {
                    val distance = if (corner.plan.direction == 'R') {
                        if (corner.previousPlan.direction == 'U' && corner.nextPlan.direction == 'U' || corner.previousPlan.direction == 'D' && corner.nextPlan.direction == 'D') {
                            // Toggle if the horizontal wall builds a vertical wall with extra steps
                            isEnclosed = !isEnclosed
                        }

                        corner.plan.distance
                    } else if (corner.previousPlan.direction == 'L') {
                        if (corner.plan.direction == 'U' && corner.previousPreviousPlan.direction == 'U' || corner.plan.direction == 'D' && corner.previousPreviousPlan.direction == 'D') {
                            // Toggle if the horizontal wall builds a vertical wall with extra steps
                            isEnclosed = !isEnclosed
                        }

                        corner.previousPlan.distance
                    } else {
                        error("Dubious corner at $point")
                    }

                    currentOutlineIndex += distance
                    coloredPoints += distance

                    treatNextCornerAsHole = true
                } else {
                    coloredPoints++

                    if (treatNextCornerAsHole) {
                        treatNextCornerAsHole = false
                    } else {
                        isEnclosed = !isEnclosed
                    }

                    val nextOutline = outlinesOnThisRow[currentOutlineIndex + 1]
                    val delta = nextOutline.x - (point.x + 1)
                    if (isEnclosed) {
                        coloredPoints += delta
                    }
                    currentOutlineIndex++
                }
            }
            coloredPoints++
        }

        return coloredPoints
    }

    fun part1(input: List<String>): Long = input
        .map { it.toDigPlan() }
        .fillOutline()

    fun part2(input: List<String>): Long = input
        .map { it.toCorrectedDigPlan() }
        .fillOutline()

    val testInput = readInput("Day18_test")
    check(part1(testInput) == 62L)
    check(part2(testInput) == 952408144115L)

    val input = readInput("Day18")
    part1(input).println()
    part2(input).println()
}
