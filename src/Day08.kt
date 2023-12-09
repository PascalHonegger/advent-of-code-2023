fun main() {
    data class Mapping(val from: String, val left: String, val right: String) {
        operator fun get(leftOrRight: Char) = if (leftOrRight == 'L') left else right
        override fun toString() = "$from = ($left, $right)"
    }
    data class LoopPossibility(val end: Mapping, val steps: Long)
    data class Loop(val start: Mapping, val loopSize: Long, val possibilities: List<LoopPossibility>)

    fun String.toMapping(): Mapping {
        val (from, to) = split(" = ")
        val (left, right) = to.removeSurrounding("(", ")").split(", ")
        return Mapping(from, left, right)
    }

    fun parseInput(input: List<String>): Pair<String, Map<String, Mapping>> {
        val leftRightPattern = input.first()
        val mappings = input.drop(2).map { it.toMapping() }
        val lookup = mappings.associateBy { it.from }
        return leftRightPattern to lookup
    }

    fun part1(input: List<String>): Long {
        val (leftRightPattern, lookup) = parseInput(input)
        var iterator = leftRightPattern.iterator()
        var steps = 0L
        var current = "AAA"
        while (current != "ZZZ") {
            val mapping = lookup.getValue(current)
            if (!iterator.hasNext()) iterator = leftRightPattern.iterator()
            val nextPattern = iterator.nextChar()
            val next = if (nextPattern == 'R') mapping.right else mapping.left
            current = next
            steps++
        }
        return steps
    }

    fun part2(input: List<String>): Long {
        val (leftRightPattern, lookup) = parseInput(input)

        fun findLoop(start: Mapping): Loop {
            var currentPatternIndex = 0
            fun nextPattern(): Char {
                if (currentPatternIndex == leftRightPattern.lastIndex) {
                    currentPatternIndex = 0
                } else {
                    currentPatternIndex++
                }
                return leftRightPattern[currentPatternIndex]
            }

            var currentPattern = leftRightPattern.first()

            val firstStep = lookup.getValue(start[currentPattern])
            val visited = mutableMapOf<Pair<Mapping, Int>, Long>()
            var current = firstStep
            var steps = 1L
            while (true) {
                currentPattern = nextPattern()
                val next = lookup.getValue(current[currentPattern])
                // println("$start $currentPatternIndex: $current -> $next")
                current = next
                steps++

                if (currentPatternIndex == 1 && current == firstStep) {
                    break
                }

                if (next.from.endsWith('Z')) {
                    val visitedKey = next to currentPatternIndex
                    if (visitedKey in visited) {
                        println("Dubious workaround code had to be used: ${visited[visitedKey]!!} vs. ${steps - visited[visitedKey]!!}")
                        break
                    }
                    check(visitedKey !in visited) { "Inner loop detected: $visitedKey" }
                    visited[visitedKey] = steps
                }
            }

            return Loop(
                start = start,
                loopSize = steps,
                possibilities = visited.map { (key, value) -> LoopPossibility(key.first, value) }
            )
        }

        return lookup
            .values
            .filter { it.from.endsWith('A') }
            .map { findLoop(it) /* .also { it.println() } */ }
            .let { loops ->
                val smallestLoop = loops.map { loop -> loop.possibilities.minOf { it.steps } }
                // get LCM of all loop sizes

                var result = smallestLoop[0]
                for (i in 1 until smallestLoop.size) {
                    result = findLCM(result, smallestLoop[i])
                }
                return result
            }
    }

    val testInput = readInput("Day08_test")
    val testInput2 = readInput("Day08_test2")
    check(part1(testInput) == 2L)
    check(part2(testInput2) == 6L)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}
