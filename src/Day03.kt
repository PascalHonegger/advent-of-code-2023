fun main() {
    fun Char.isSymbol() = this != '.' && !this.isDigit()

    fun List<String>.findAdjacent(row: Int, col: Int, predicate: (Char) -> Boolean): Pair<Int, Int>? {
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                if (predicate(this.getOrNull(row + i)?.getOrNull(col + j) ?: '.')) {
                    return row + i to col + j
                }
            }
        }
        return null
    }

    fun part1(input: List<String>): Int {
        var sumOfSerials = 0
        input.forEachIndexed { row, line ->
            var serial = 0
            var serialBordersSymbol = false
            line.forEachIndexed { col, char ->
                if (char.isDigit()) {
                    serial *= 10
                    serial += char.digitToInt()

                    if (input.findAdjacent(row, col) { it.isSymbol() } != null) {
                        serialBordersSymbol = true
                    }
                } else {
                    if (serialBordersSymbol) {
                        sumOfSerials += serial
                    }
                    serialBordersSymbol = false
                    serial = 0
                }
            }
            if (serialBordersSymbol) {
                sumOfSerials += serial
            }
        }
        return sumOfSerials
    }

    fun part2(input: List<String>): Int {
        var gearRatios = 0
        val serialsByGear = mutableMapOf<Pair<Int, Int>, Int>()
        input.forEachIndexed { row, line ->
            var serial = 0
            var adjacentGear: Pair<Int, Int>? = null
            line.forEachIndexed { col, char ->
                if (char.isDigit()) {
                    serial *= 10
                    serial += char.digitToInt()
                    if (adjacentGear == null) {
                        adjacentGear = input.findAdjacent(row, col) { it == '*' }
                    }
                } else {
                    adjacentGear?.let {
                        if (serialsByGear[it] != null) {
                            gearRatios += serialsByGear[it]!! * serial
                            serialsByGear.remove(it)
                        } else {
                            serialsByGear[it] = serial
                        }
                    }
                    adjacentGear = null
                    serial = 0
                }
            }
            adjacentGear?.let {
                if (serialsByGear[it] != null) {
                    gearRatios += serialsByGear[it]!! * serial
                    serialsByGear.remove(it)
                } else {
                    serialsByGear[it] = serial
                }
            }
        }

        return gearRatios
    }

    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
