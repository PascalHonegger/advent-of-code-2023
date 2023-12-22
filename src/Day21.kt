fun main() {
    data class Point(val x: Int, val y: Int) {
        fun up() = Point(x, y - 1)
        fun down() = Point(x, y + 1)
        fun left() = Point(x - 1, y)
        fun right() = Point(x + 1, y)
    }

    val empty = '.'
    val stone = '#'
    val start = 'S'

    fun List<String>.toGarden(): Map<Point, Char> {
        val garden = mutableMapOf<Point, Char>()
        forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                garden[Point(x, y)] = c
            }
        }
        return garden
    }

    fun part1(input: List<String>, numSteps: Int): Int {
        // Parse Input
        val garden = input.toGarden()

        fun getOrStone(point: Point) = garden.getOrDefault(point, stone)

        // Find start
        val startPoint = garden.entries.single { it.value == start }.key

        var visitable = setOf(startPoint)

        // Start
        repeat(numSteps) {
            val nextVisitables = mutableSetOf<Point>()
            // Simulate take step
            for (point in visitable) {
                nextVisitables += mapOf(
                    point.up() to getOrStone(point.up()),
                    point.down() to getOrStone(point.down()),
                    point.left() to getOrStone(point.left()),
                    point.right() to getOrStone(point.right()),
                ).filter { it.value != stone }.keys
            }
            visitable = nextVisitables
        }

        return visitable.size
    }

    fun part2(input: List<String>, numSteps: Int): Long {
        // Parse Input
        val garden = input.toGarden()

        val width = input.first().length
        val height = input.size

        fun smartModulo(what: Int, max: Int): Int {
            val remainder = what % max
            return if (remainder < 0) max + remainder else remainder
        }

        fun getInfinitely(point: Point): Char {
            val newX = smartModulo(point.x, width)
            val newY = smartModulo(point.y, height)
            return garden.getValue(Point(x = newX, y = newY))
        }

        // Find start
        val startPoint = garden.entries.single { it.value == start }.key

        var visitable = setOf(startPoint)

        // Start
        repeat(numSteps) {

            // Visualize it
            val minX = -1 * width
            val maxX = 2 * width
            val minY = -1 * height
            val maxY = 2 * height
            (minY..maxY).forEach { y ->
                (minX..maxX).forEach { x ->
                    val point = Point(x, y)
                    val value = getInfinitely(point)
                    print(if (point in visitable) 'O' else value)
                }
                println()
            }

            val nextVisitables = mutableSetOf<Point>()

            fun addIfNotAStone(point: Point) {
                val value = getInfinitely(point)
                if (value != stone) {
                    nextVisitables += point
                }
            }

            // Simulate take step
            for (point in visitable) {
                addIfNotAStone(point.up())
                addIfNotAStone(point.down())
                addIfNotAStone(point.left())
                addIfNotAStone(point.right())
            }
            visitable = nextVisitables
        }

        return visitable.size.toLong()
    }

    val testInput = readInput("Day21_test")
    check(part1(testInput, numSteps = 1) == 2)
    check(part1(testInput, numSteps = 2) == 4)
    check(part1(testInput, numSteps = 3) == 6)
    check(part1(testInput, numSteps = 6) == 16)
    check(part2(testInput, numSteps = 6) == 16L)
    check(part2(testInput, numSteps = 10) == 50L)
    check(part2(testInput, numSteps = 50) == 1_594L)
    check(part2(testInput, numSteps = 100) == 6_536L)
    check(part2(testInput, numSteps = 500) == 167_004L)
    check(part2(testInput, numSteps = 1_000) == 668_697L)
    check(part2(testInput, numSteps = 5_000) == 16_733_044L)

    val input = readInput("Day21")
    part1(input, numSteps = 64).println()
    part2(input, numSteps = 26_501_365).println()
}
