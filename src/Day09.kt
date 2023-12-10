fun main() {
    fun List<Long>.extrapolatedHistory(): List<List<Long>> = buildList {
        var workingSet = this@extrapolatedHistory
        while (workingSet.any { it != 0L }) {
            add(workingSet)
            workingSet = workingSet.zipWithNext { a, b -> b - a }
        }
    }

    fun part1(input: List<String>): Long = input
        .map { it.asSpaceSeparatedLongs() }.sumOf { row -> row.extrapolatedHistory().sumOf { it.last() } }

    fun part2(input: List<String>): Long = input
        .map { it.asSpaceSeparatedLongs() }.sumOf { row ->
            row
                .extrapolatedHistory()
                .asReversed()
                .fold(0L) { acc, it ->
                    it.first() - acc
                }
        }

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 114L)
    check(part2(testInput) == 2L)

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}
