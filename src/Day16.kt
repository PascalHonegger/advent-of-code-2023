fun main() {
    data class Position(val x: Int, val y: Int) {
        fun north() = Position(x, y - 1)
        fun south() = Position(x, y + 1)
        fun east() = Position(x + 1, y)
        fun west() = Position(x - 1, y)
    }

    abstract class Direction
    val north = object : Direction() {}
    val south = object : Direction() {}
    val east = object : Direction() {}
    val west = object : Direction() {}

    abstract class Laser {
        abstract val pos: Position
        abstract fun next(field: Char): Sequence<Direction>
    }
    data class North(override val pos: Position) : Laser() {
        override fun next(field: Char) = sequence {
            when (field) {
                '.',
                '|',
                -> yield(north)

                '/' -> yield(east)
                '\\' -> yield(west)
                '-' -> {
                    yield(east)
                    yield(west)
                }
            }
        }
    }

    data class South(override val pos: Position) : Laser() {
        override fun next(field: Char) = sequence {
            when (field) {
                '.',
                '|',
                -> yield(south)

                '/' -> yield(west)
                '\\' -> yield(east)
                '-' -> {
                    yield(east)
                    yield(west)
                }
            }
        }
    }

    data class East(override val pos: Position) : Laser() {
        override fun next(field: Char) = sequence {
            when (field) {
                '.',
                '-',
                -> yield(east)

                '/' -> yield(north)
                '\\' -> yield(south)
                '|' -> {
                    yield(north)
                    yield(south)
                }
            }
        }
    }

    data class West(override val pos: Position) : Laser() {
        override fun next(field: Char) = sequence {
            when (field) {
                '.',
                '-',
                -> yield(west)

                '/' -> yield(south)
                '\\' -> yield(north)
                '|' -> {
                    yield(north)
                    yield(south)
                }
            }
        }
    }

    fun traceTheLaser(input: List<String>, start: Laser): Int {
        println(start)
        val workingSet = ArrayDeque<Laser>()
        workingSet.add(start)
        val visited = mutableSetOf<Laser>()
        while (workingSet.isNotEmpty()) {
            val current = workingSet.removeFirst()
            val field = input.getOrNull(current.pos.y)?.getOrNull(current.pos.x) ?: continue

            if (!visited.add(current)) {
                // We already traced this laser
                continue
            }
            workingSet.addAll(current.next(field).map {
                when (it) {
                    north -> North(current.pos.north())
                    south -> South(current.pos.south())
                    east -> East(current.pos.east())
                    west -> West(current.pos.west())
                    else -> error("Unknown direction")
                }
            })
        }
        return visited.distinctBy { it.pos }.size
    }

    fun part1(input: List<String>): Int = traceTheLaser(input, East(Position(0, 0)))

    fun part2(input: List<String>): Int {
        var max = 0
        val maxCol = input.first().lastIndex
        val maxRow = input.lastIndex
        for (row in 0..maxRow) {
            max = maxOf(max, traceTheLaser(input, East(Position(0, row))))
            max = maxOf(max, traceTheLaser(input, West(Position(maxCol, row))))
        }
        for (col in 0..maxCol) {
            max = maxOf(max, traceTheLaser(input, South(Position(col, 0))))
            max = maxOf(max, traceTheLaser(input, North(Position(col, maxRow))))
        }

        return max
    }

    val testInput = readInput("Day16_test")
    check(part1(testInput) == 46)
    check(part2(testInput) == 51)

    val input = readInput("Day16")
    part1(input).println()
    part2(input).println()
}
