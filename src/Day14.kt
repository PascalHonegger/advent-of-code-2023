fun main() {
    val rounded = 'O'
    val squared = '#'
    val empty = '.'

    data class Position(val x: Int, val y: Int) {
        fun north() = Position(x, y - 1)
        fun south() = Position(x, y + 1)
        fun east() = Position(x + 1, y)
        fun west() = Position(x - 1, y)
    }

    fun List<String>.toPositionMap() = buildMap {
        this@toPositionMap.forEachIndexed { y, row ->
            row.forEachIndexed { col, c ->
                put(Position(col, y), c)
            }
        }
    }

    fun Map<Position, Char>.tilted(direction: Char) = toMutableMap().apply {
        var hasChanged = true
        while (hasChanged) {
            hasChanged = false
            toMap().forEach { (pos, c) ->
                val next = when (direction) {
                    'W' -> pos.west()
                    'E' -> pos.east()
                    'N' -> pos.north()
                    'S' -> pos.south()
                    else -> error("Unknown direction $direction")
                }
                if (c == rounded && getOrDefault(next, squared) == empty) {
                    put(pos, empty)
                    put(next, rounded)
                    hasChanged = true
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        val positionMap = input.toPositionMap()
        val maxY = positionMap.keys.maxOf { it.y } + 1
        val tilted = positionMap.tilted('N')
        return tilted.filterValues { it == rounded }.keys.sumOf { maxY - it.y }
    }

    fun part2(input: List<String>): Int {
        var map = input.toPositionMap()
        val visited = mutableMapOf<Map<Position, Char>, Int>()
        var iteration = 0
        val totalIterations = 1_000_000_000
        while (iteration < totalIterations) {
            map = map.tilted('N').tilted('W').tilted('S').tilted('E')
            val existing = visited.putIfAbsent(map, iteration)
            if (existing != null) {
                val cycleLength = iteration - existing
                val remainingIterations = totalIterations - iteration
                val skippableCycles = remainingIterations / cycleLength
                val skip = skippableCycles * cycleLength
                if (skip > 0) {
                    println("Found cycle of length $cycleLength at iteration $iteration, skipping $skip iterations")
                    iteration += skip
                }
            }
            iteration++
        }
        val maxY = map.keys.maxOf { it.y } + 1
        return map.filterValues { it == rounded }.keys.sumOf { maxY - it.y }
    }

    val testInput = readInput("Day14_test")
    check(part1(testInput) == 136)
    check(part2(testInput) == 64)

    val input = readInput("Day14")
    part1(input).println()
    part2(input).println()
}
