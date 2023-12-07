fun main() {
    val cardValues = "AKQJT98765432"
    val cardValuesWithJokerAsWeakest = "AKQT98765432J"
    val joker = 'J'
    val cardValuesWithoutJoker = cardValues.filter { it != joker }
    data class Card(val value: Char) {
        override fun toString(): String {
            return value.toString()
        }
    }

    data class Hand(val cards: List<Card>, val bid: Long)

    fun List<Card>.jokerPermutations(): List<List<Card>> = buildList {
        fun addJokerPermutations(cards: List<Card>) {
            add(cards)
            cards.forEachIndexed { index, card ->
                if (card.value == joker) {
                    for (value in cardValuesWithoutJoker) {
                        addJokerPermutations(cards.toMutableList().also { it[index] = Card(value) })
                    }
                }
            }
        }

        addJokerPermutations(this@jokerPermutations)
    }

    fun String.toHand(): Hand {
        val (cards, bid) = this.split(" ")
        return Hand(cards.map { Card(it) }, bid.toLong())
    }

    fun Hand.calculateHandStrength(
        cardOrder: String,
        fiveOfKind: Boolean,
        fourOfKind: Boolean,
        fullHouse: Boolean,
        threeOfKind: Boolean,
        twoPair: Boolean,
        onePair: Boolean,
        highCard: Boolean,
    ): Long {
        val cardStrengths = cards
            .map { cardOrder.reversed().indexOf(it.value).toLong() }
            .reduce { acc, i -> acc * 100 + i }

        return when {
            fiveOfKind -> 9_0000000000 + cardStrengths
            fourOfKind -> 8_0000000000 + cardStrengths
            fullHouse -> 7_0000000000 + cardStrengths
            threeOfKind -> 6_0000000000 + cardStrengths
            twoPair -> 5_0000000000 + cardStrengths
            onePair -> 4_0000000000 + cardStrengths
            highCard -> 3_0000000000 + cardStrengths
            else -> error("Invalid hand")
        }
    }

    fun Hand.basicHandStrength(): Long {
        val cardCounts = cards.groupingBy { it.value }.eachCount()

        return calculateHandStrength(
            cardOrder = cardValues,
            fiveOfKind = cardCounts.size == 1,
            fourOfKind = cardCounts.size == 2 && cardCounts.values.any { it == 4 },
            fullHouse = cardCounts.size == 2 && cardCounts.values.any { it == 3 },
            threeOfKind = cardCounts.size == 3 && cardCounts.values.any { it == 3 },
            twoPair = cardCounts.size == 3 && cardCounts.values.count { it == 2 } == 2,
            onePair = cardCounts.size == 4 && cardCounts.values.any { it == 2 },
            highCard = cardCounts.size == 5,
        )
    }

    fun Hand.jokerHandStrength(): Long {
        val cardCountsWithJoker = cards.groupingBy { it.value }.eachCount()
        val numberOfJokers = cardCountsWithJoker[joker] ?: 0
        val cardCounts = cardCountsWithJoker.filterKeys { it != joker }

        fun nOfKind(n: Int): Boolean {
            return numberOfJokers == n || cardCounts.values.any { it + numberOfJokers == n }
        }

        return calculateHandStrength(
            cardOrder = cardValuesWithJokerAsWeakest,
            fiveOfKind = nOfKind(5),
            fourOfKind = nOfKind(4),
            fullHouse = cardCounts.values.any { it == 2 } && cardCounts.values.any { it == 3 } ||
                    cardCounts.values.any { it == 1 } && cardCounts.values.any { it == 3 } && numberOfJokers == 1 ||
                    cardCounts.values.any { it == 3 } && numberOfJokers == 2 ||
                    cardCounts.values.count { it == 2 } == 2 && numberOfJokers == 1 ||
                    cardCounts.values.any { it == 1 } && cardCounts.values.any { it == 2 } && numberOfJokers == 2 ||
                    cardCounts.values.count { it == 1 } == 2 && numberOfJokers == 3 ||
                    cardCounts.values.any { it == 2 } && numberOfJokers == 3,
            threeOfKind = nOfKind(3),
            twoPair = cardCounts.values.count { it + numberOfJokers == 2 } == 2,
            onePair = cardCounts.values.any { it + numberOfJokers == 2 },
            highCard = cardCounts.size == 5 && numberOfJokers == 0,
        )
    }

    fun part1(input: List<String>): Long {
        return input
            .map { it.toHand() }
            .sortedBy { it.basicHandStrength() }
            .mapIndexed { index, hand ->
                (index + 1) * hand.bid
            }
            .sum()
    }

    fun part2(input: List<String>): Long {
        return input
            .map { it.toHand() }
            .sortedBy { it.jokerHandStrength() }
            .mapIndexed { index, hand ->
                (index + 1) * hand.bid
            }
            .sum()
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 6440L)
    check(part2(testInput) == 5905L)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}
