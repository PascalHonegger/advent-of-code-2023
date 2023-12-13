fun main() {
    fun List<String>.toStoneMap(): List<List<List<Char>>> = buildList {
        var workingSet = this@toStoneMap
        while (true) {
            val emptyRow = workingSet.indexOfFirst { it.isEmpty() }
            if (emptyRow == -1) {
                add(workingSet.map { it.toList() })
                break
            }
            add(workingSet.subList(0, emptyRow).map { it.toList() })
            workingSet = workingSet.subList(emptyRow + 1, workingSet.size)
        }
    }

    fun List<Char>.countDifferences(other: List<Char>): Int =
        zip(other).count { (a, b) -> a != b }

    fun List<List<Char>>.findVerticalMirror(smudges: Int): Int? = windowed(2)
        .mapIndexedNotNull { index, (a, b) ->
            if (a.countDifferences(b) <= smudges) index else null
        }.singleOrNull {
            var topOfReflection = it
            var bottomOfReflection = it + 1
            var remainingSmudges = smudges
            while (topOfReflection >= 0 && bottomOfReflection < size) {
                val differences = this[topOfReflection].countDifferences(this[bottomOfReflection])
                if (differences == 1 && remainingSmudges == 1) {
                    remainingSmudges = 0
                } else if (differences > 0) {
                    return@singleOrNull false
                }
                topOfReflection--
                bottomOfReflection++
            }
            return@singleOrNull remainingSmudges == 0
        }

    fun List<List<Char>>.findHorizontalMirror(smudges: Int) =
        transpose().findVerticalMirror(smudges)

    fun part1(input: List<String>, smudges: Int = 0): Int = input.toStoneMap().sumOf { stoneMap ->
        val horizontal = stoneMap.findVerticalMirror(smudges)?.let { (it + 1) * 100 } ?: 0
        val vertical = stoneMap.findHorizontalMirror(smudges)?.let { it + 1 } ?: 0
        horizontal + vertical
    }

    fun part2(input: List<String>): Int = part1(input, smudges = 1)

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 405)
    check(part2(testInput) == 400)

    val input = readInput("Day13")
    part1(input).println()
    part2(input).println()
}
