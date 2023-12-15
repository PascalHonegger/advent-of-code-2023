fun main() {
    fun String.holidayAsciiHash(): Int {
        var current = 0
        forEach { c ->
            val ascii = c.code
            current += ascii
            current *= 17
            current %= 256
        }
        return current
    }

    fun part1(input: String): Int = input.split(",").sumOf { it.holidayAsciiHash() }

    fun part2(input: String): Int {
        val boxes = (0 ..< 256).map { LinkedHashMap<String, Int>() }
        for (instruction in input.split(",")) {
            if (instruction.endsWith('-')) {
                val operation = instruction.removeSuffix("-")
                boxes[operation.holidayAsciiHash()].remove(operation)
            } else {
                val (operation, focalLength) = instruction.split("=")
                boxes[operation.holidayAsciiHash()][operation] = focalLength.toInt()
            }
        }

        return boxes.sumOfIndexed { boxIndex, box ->
            box.values.sumOfIndexed { operationIndex, focalLength ->
                (boxIndex + 1) * (operationIndex + 1) * focalLength
            }
        }
    }

    val testInput = readTextInput("Day15_test")
    check("HASH".holidayAsciiHash() == 52)
    check(part1(testInput) == 1320)
    check(part2(testInput) == 145)

    val input = readTextInput("Day15")
    part1(input).println()
    part2(input).println()
}
