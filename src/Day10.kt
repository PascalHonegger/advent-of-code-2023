fun main() {
    data class Pipe(val north: Boolean, val east: Boolean, val south: Boolean, val west: Boolean)

    val empty = Pipe(north = false, east = false, south = false, west = false)
    val vertical = Pipe(north = true, east = false, south = true, west = false)
    val horizontal = Pipe(north = false, east = true, south = false, west = true)
    val northEast = Pipe(north = true, east = true, south = false, west = false)
    val northWest = Pipe(north = true, east = false, south = false, west = true)
    val southWest = Pipe(north = false, east = false, south = true, west = true)
    val southEast = Pipe(north = false, east = true, south = true, west = false)
    val start = Pipe(north = true, east = true, south = true, west = true)
    val pipeMapping = mapOf(
        '|' to vertical,
        '-' to horizontal,
        'L' to northEast,
        'J' to northWest,
        '7' to southWest,
        'F' to southEast,
        '.' to empty,
        'S' to start,
    )

    data class Coordinate(val x: Int, val y: Int) {
        fun north() = Coordinate(x, y - 1)
        fun south() = Coordinate(x, y + 1)
        fun west() = Coordinate(x - 1, y)
        fun east() = Coordinate(x + 1, y)
        override fun toString() = "($x, $y)"
    }

    data class PipeSystem(
        val pipes: MutableMap<Coordinate, Pipe> = mutableMapOf(),
        val mainLoop: MutableSet<Coordinate> = mutableSetOf(),
    ) {
        operator fun get(x: Int, y: Int) = get(Coordinate(x, y))
        operator fun get(coordinate: Coordinate) = pipes[coordinate]
        operator fun set(x: Int, y: Int, value: Pipe) = set(Coordinate(x, y), value)
        operator fun set(coordinate: Coordinate, value: Pipe) = pipes.set(coordinate, value)
        val xIndices get() = 0..pipes.keys.maxOf { it.x }
        val yIndices get() = 0..pipes.keys.maxOf { it.y }
        override fun toString(): String {
            return yIndices.joinToString("\n") { y ->
                xIndices.joinToString("") { x ->
                    when (val pipe = get(x, y) ?: error("No pipe at $x, $y")) {
                        empty -> " "
                        vertical -> "│"
                        horizontal -> "─"
                        northEast -> "└"
                        northWest -> "┘"
                        southWest -> "┐"
                        southEast -> "┌"
                        else -> error("Unknown pipe: $pipe")
                    }
                }
            }
        }
    }

    fun PipeSystem.adjacent(coordinate: Coordinate): List<Coordinate> {
        val pipe = this[coordinate] ?: error("No pipe at $coordinate")
        return listOfNotNull(
            coordinate.north().takeIf { pipe.north && get(it)?.south == true },
            coordinate.east().takeIf { pipe.east && get(it)?.west == true },
            coordinate.south().takeIf { pipe.south && get(it)?.north == true },
            coordinate.west().takeIf { pipe.west && get(it)?.east == true },
        )
    }

    fun List<String>.toPipeSystem(): PipeSystem {
        val pipeSystem = PipeSystem()
        forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                pipeSystem[x, y] = pipeMapping.getValue(c)
            }
        }

        val (startCoordinate) = pipeSystem.pipes.entries.single { it.value == start }
        pipeSystem[startCoordinate] = when (pipeSystem.adjacent(startCoordinate).toSet()) {
            setOf(startCoordinate.north(), startCoordinate.east()) -> northEast
            setOf(startCoordinate.north(), startCoordinate.west()) -> northWest
            setOf(startCoordinate.south(), startCoordinate.west()) -> southWest
            setOf(startCoordinate.south(), startCoordinate.east()) -> southEast
            else -> error("Start must have exactly two connected pipes")
        }

        var workingSet = listOf(startCoordinate)
        while (pipeSystem.mainLoop.addAll(workingSet)) {
            workingSet = workingSet.flatMap { pipeSystem.adjacent(it) }.filter { it !in pipeSystem.mainLoop }
        }

        return pipeSystem
    }

    fun part1(input: List<String>): Int = input.toPipeSystem().mainLoop.size / 2

    fun part2(input: List<String>): Int = input.toPipeSystem().run {
        val enclosedCoordinates = mutableSetOf<Coordinate>()

        for (y in yIndices) {
            var isEnclosed = false
            var horizontalWallStart: Pipe? = null
            for (x in xIndices) {
                val coordinate = Coordinate(x, y)
                if (coordinate in mainLoop) {
                    when (val pipe = get(coordinate)) {
                        horizontal, empty -> Unit
                        vertical -> isEnclosed = !isEnclosed
                        southEast, southWest, northEast, northWest -> {
                            if (horizontalWallStart == null) {
                                horizontalWallStart = pipe
                            } else {
                                if (horizontalWallStart.north && pipe.south || horizontalWallStart.south && pipe.north) {
                                    // Toggle if the horizontal wall builds a vertical wall with extra steps
                                    isEnclosed = !isEnclosed
                                }
                            }
                        }

                        else -> error("Unknown pipe: $pipe")
                    }
                } else if (isEnclosed) {
                    enclosedCoordinates += coordinate
                }
            }
        }

        enclosedCoordinates.size
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 8)
    check(part2(readInput("Day10_test1")) == 4)
    check(part2(readInput("Day10_test2")) == 8)
    check(part2(readInput("Day10_test3")) == 10)

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}
