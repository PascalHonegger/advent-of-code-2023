fun main() {
    data class Mapping(val range: LongRange, val offset: Long)
    data class GardenLayout(val seeds: List<Long>, val allMappings: List<List<Mapping>>)

    fun List<String>.toGardenLayout(): GardenLayout {
        val iterator = listIterator()
        val seeds = iterator.next().removePrefix("seeds: ").asSpaceSeparatedLongs()
        iterator.next() // empty line
        val mappings = buildList {
            while (iterator.hasNext()) {
                check(iterator.next().endsWith("map:"))
                var line = iterator.next()
                val mappings = buildList {
                    while (line.isNotBlank()) {
                        val (destinationRangeStart, sourceRangeStart, rangeLength) = line.asSpaceSeparatedLongs()
                        val offset = destinationRangeStart - sourceRangeStart
                        add(Mapping(sourceRangeStart..<(sourceRangeStart + rangeLength), offset))
                        line = (if (iterator.hasNext()) iterator.next() else "")
                    }
                }
                add(mappings)
            }
        }

        return GardenLayout(
            seeds = seeds,
            allMappings = mappings
        )
    }

    fun part1(input: List<String>): Long {
        val gardenLayout = input.toGardenLayout()
        return gardenLayout.seeds.minOf { seed ->
            gardenLayout.allMappings.fold(seed) { acc, mappings ->
                val offset = mappings.find { acc in it.range }?.offset ?: 0
                acc + offset
            }
        }
    }

    fun part2(input: List<String>): Long {
        val gardenLayout = input.toGardenLayout()
        return gardenLayout.seeds
            .asSequence()
            .windowed(2, 2)
            .flatMap { (start, length) -> start ..< (start + length) }
            .minOf { seed ->
                gardenLayout.allMappings.fold(seed) { acc, mappings ->
                    val offset = mappings.find { acc in it.range }?.offset ?: 0
                    acc + offset
                }
            }
    }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == 35L)
    check(part2(testInput) == 46L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}
