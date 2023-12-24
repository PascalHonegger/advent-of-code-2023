import kotlin.time.measureTime

fun main() {
    val empty = '.'
    val forest = '#'
    val steepSlopeUp = '^'
    val steepSlopeRight = '>'
    val steepSlopeDown = 'v'
    val steepSlopeLeft = '<'
    val steepSlopes = listOf(steepSlopeUp, steepSlopeRight, steepSlopeDown, steepSlopeLeft)

    data class Point(val x: Int, val y: Int)

    fun Point.up() = Point(x, y - 1)
    fun Point.right() = Point(x + 1, y)
    fun Point.down() = Point(x, y + 1)
    fun Point.left() = Point(x - 1, y)
    fun Point.neighbors() = listOf(up(), right(), down(), left())

    data class NodeLink(val distance: Int, val passesThroughSlope: Boolean)

    data class Node(val point: Point, val neighbors: MutableSet<Pair<Node, NodeLink>>) {
        override fun toString(): String = "Node($point)"
        override fun hashCode(): Int = point.hashCode()
        override fun equals(other: Any?): Boolean = other is Node && point == other.point
    }

    fun List<String>.toIslandMap2(): Map<Point, Node> {
        val map = mutableMapOf<Point, Char>().withDefault { forest }
        forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                if (c != forest) {
                    map[Point(x, y)] = c
                }
            }
        }

        val start = map.entries.single { it.key.y == 0 && it.value == empty }.key
        val end = map.entries.single { it.key.y == this@toIslandMap2.lastIndex && it.value == empty }.key
        fun Point.isCorner() = neighbors().count { map.getValue(it) != forest } >= 3

        val nodes = map
            .filterKeys { point ->
                map.getValue(point) == empty && (point == start || point == end || point.isCorner())
            }.mapValues { (point, _) -> Node(point, mutableSetOf()) }

        for (node in nodes.values) {
            fun traverse(allowedSteepSlope: Char, next: (Point) -> Point) {
                val firstStep = next(node.point)
                if (map.getValue(firstStep) == forest) {
                    return
                }
                var previousPoint = node.point
                var currentPoint = firstStep
                var distance = 1
                var passesThroughSlope = false
                while (currentPoint !in nodes) {
                    val nextPoint = currentPoint.neighbors().single { map.getValue(it) != forest && it != previousPoint }
                    previousPoint = currentPoint
                    currentPoint = nextPoint
                    distance++
                    if (map.getValue(currentPoint) != allowedSteepSlope && map.getValue(currentPoint) in steepSlopes) {
                        passesThroughSlope = true
                    }
                }
                node.neighbors.add(nodes.getValue(currentPoint) to NodeLink(distance, passesThroughSlope))
            }
            traverse(steepSlopeUp, Point::up)
            traverse(steepSlopeRight, Point::right)
            traverse(steepSlopeDown, Point::down)
            traverse(steepSlopeLeft, Point::left)
        }

        return nodes
    }

    fun doIt(input: List<String>, allowPassingThroughSlopes: Boolean): Int {
        val islandMap = input.toIslandMap2()
        val start = islandMap.entries.single { it.key.y == 0 }.value
        val end = islandMap.entries.single { it.key.y == input.lastIndex }.value
        var longestPath = 0
        val workingSet = ArrayDeque<Triple<Node, Int, Set<Node>>>()
        workingSet.add(Triple(start, 0, setOf(start)))
        while (workingSet.isNotEmpty()) {
            val (current, pathSize, path) = workingSet.removeFirst()
            if (current == end) {
                longestPath = maxOf(longestPath, pathSize)
            } else {
                for (neighbor in current.neighbors) {
                    val (next, link) = neighbor
                    if ((allowPassingThroughSlopes || !link.passesThroughSlope) && next !in path) {
                        workingSet.addFirst(Triple(next, pathSize + link.distance, path + next))
                    }
                }
            }
        }
        return longestPath
    }

    fun part1(input: List<String>): Int = doIt(input, false)

    fun part2(input: List<String>): Int = doIt(input, true)

    val testInput = readInput("Day23_test")
    measureTime {
        check(part1(testInput) == 94)
    }.also { println("Part1 took $it") }
    measureTime {
        check(part2(testInput) == 154)
    }.also { println("Part2 took $it") }

    val input = readInput("Day23")
    measureTime {
        part1(input).println()
    }.also { println("Part1 for real took $it") }
    measureTime {
        part2(input).println()
    }.also { println("Part2 for real took $it") }
}
