import kotlin.math.abs

fun main() {
    data class Star(val num: Int, val x: Long, val y: Long)
    data class StarSystem(val stars: List<Star>, val maxX: Long, val maxY: Long)

    fun List<String>.toStarSystem(): StarSystem {
        val stars = mutableListOf<Star>()
        var starIndex = 1
        forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                if (c == '#') {
                    stars += Star(starIndex++, x.toLong(), y.toLong())
                }
            }
        }
        return StarSystem(
            stars = stars,
            maxX = stars.maxOf { it.x },
            maxY = stars.maxOf { it.y }
        )
    }

    fun part1(input: List<String>, emptySpaceFactor: Long = 2L): Long {
        val starSystem = input.toStarSystem()
        val starsByX = starSystem.stars.groupBy { it.x }
        val starsByY = starSystem.stars.groupBy { it.y }

        val emptyX = (0..starSystem.maxX).filter { it !in starsByX }
        val emptyY = (0..starSystem.maxY).filter { it !in starsByY }

        val adjustedStars = starSystem.stars.map { star ->
            val emptyXs = emptyX.count { it < star.x }
            val emptyYs = emptyY.count { it < star.y }
            star.copy(
                x = star.x + emptyXs * (emptySpaceFactor - 1L),
                y = star.y + emptyYs * (emptySpaceFactor - 1L),
            )
        }

        val workingSet = ArrayDeque(adjustedStars)
        var sumOfDistances = 0L
        while (workingSet.isNotEmpty()) {
            val current = workingSet.removeFirst()
            for (other in workingSet) {
                val distanceX = abs(current.x - other.x)
                val distanceY = abs(current.y - other.y)
                val distance = distanceX + distanceY
                sumOfDistances += distance
            }
        }

        return sumOfDistances
    }

    fun part2(input: List<String>): Long = part1(input, emptySpaceFactor = 1_000_000L)

    val testInput = readInput("Day11_test")
    check(part1(testInput, emptySpaceFactor = 2L) == 374L)
    check(part1(testInput, emptySpaceFactor = 10L) == 1030L)
    check(part1(testInput, emptySpaceFactor = 100L) == 8410L)

    val input = readInput("Day11")
    part1(input).println()
    part2(input).println()
}
