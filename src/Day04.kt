fun main() {
    data class Card(val cardNumber: Int, val winningNumbers: Set<Int>, val numbers: Set<Int>) {
        val hits get() = winningNumbers.intersect(numbers).size
    }

    // Input: Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
    fun String.toCard(): Card {
        val (cardNumber, cardContent) = split(": ")
        val (winningNumbers, numbers) = cardContent.split(" | ")
        return Card(
            cardNumber = cardNumber.removePrefix("Card").trim().toInt(),
            winningNumbers = winningNumbers.asSpaceSeparatedInts().toSet(),
            numbers = numbers.asSpaceSeparatedInts().toSet(),
        )
    }

    fun part1(input: List<String>): Int {
        return input
            .map { it.toCard() }
            .sumOf {
                if (it.hits == 0) {
                    0
                } else {
                    1 shl (it.hits - 1)
                }
            }
    }

    fun part2(input: List<String>): Int {
        val amountOfCardsByNumber = mutableMapOf<Int, Int>()
        input
            .map { it.toCard() }
            .forEach { card ->
                val totalCardCopies = amountOfCardsByNumber.increase(card.cardNumber)
                for (i in 1..card.hits) {
                    amountOfCardsByNumber.increase(card.cardNumber + i, by = totalCardCopies)
                }
            }
        return amountOfCardsByNumber.values.sum()
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
