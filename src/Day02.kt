fun main() {
    data class Draw(val reds: Int, val greens: Int, val blues: Int)
    data class Game(val id: Int, val draws: List<Draw>)

    fun String.toDraw(): Draw {
        // 8 green, 6 blue, 20 red
        val parts = split(", ")
        val colors = parts.map { it.split(" ") }.associate { (amount, color) -> color to amount.toInt() }
        return Draw(
            reds = colors.getOrDefault("red", 0),
            greens = colors.getOrDefault("green", 0),
            blues = colors.getOrDefault("blue", 0)
        )
    }

    fun String.toGame(): Game {
        val (gamePart, drawsPart) = split(": ")
        val gameId = gamePart.drop("Game ".length).toInt()
        val draws = drawsPart.split("; ").map { it.toDraw() }
        return Game(
            id = gameId,
            draws = draws
        )
    }


    fun part1(input: List<String>): Int {
        // at most 12 red cubes, 13 green cubes, and 14 blue cubes
        return input
            .map { it.toGame() }
            .filter { game -> game.draws.none { it.reds > 12 || it.greens > 13 || it.blues > 14 } }
            .sumOf { it.id }
    }

    fun part2(input: List<String>): Int {
        return input
            .map { it.toGame() }
            .sumOf { game ->
                val minReds = game.draws.maxOf { it.reds }
                val minGreens = game.draws.maxOf { it.greens }
                val minBlues = game.draws.maxOf { it.blues }
                minReds * minGreens * minBlues
            }
    }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 8)
    check(part2(testInput) == 2286)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
