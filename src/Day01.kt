fun main() {
    val spelledOutDigits = mapOf(
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
    )

    fun String.replaceSpelledOutLettersWithDigits(): String {
        return buildString {
            var workingSet = this@replaceSpelledOutLettersWithDigits
            while (workingSet.isNotEmpty()) {
                val matchingDigits = spelledOutDigits.entries.find { workingSet.startsWith(it.key) }
                if (matchingDigits != null) {
                    append(matchingDigits.value.digitToChar())
                } else {
                    append(workingSet.first())
                }
                workingSet = workingSet.drop(1)
            }
        }
    }

    fun part1(input: List<String>): Int {
        return input.sumOf {
            val digits = it.filter { c -> c.isDigit() }
            10 * digits.first().digitToInt() + digits.last().digitToInt()
        }
    }

    fun part2(input: List<String>): Int {
        val adjustedInput = input.map {
            it.replaceSpelledOutLettersWithDigits()
        }
        return part1(adjustedInput)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 142)
    val test2Input = readInput("Day01_test2")
    check(part2(test2Input) == 281)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
