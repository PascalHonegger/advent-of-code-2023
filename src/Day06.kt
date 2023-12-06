import kotlin.math.ceil
import kotlin.math.sqrt

fun main() {
    data class Race(val raceDuration: Long, val recordDistance: Long)

    fun Race.possibleChargeDuration(): Long {
        // distance = chargeDuration * (raceDuration - chargeDuration)
        // distance = chargeDuration * raceDuration - chargeDuration^2
        // 0 = chargeDuration^2 - chargeDuration * raceDuration + distance
        // chargeDuration = (raceDuration +- sqrt(raceDuration^2 - 4 * distance)) / 2
        val discriminant = raceDuration * raceDuration - 4 * recordDistance
        val sqrt = sqrt(discriminant.toDouble())
        val maxChargeTime = (raceDuration + sqrt) / 2
        val minChargeTime = (raceDuration - sqrt) / 2

        // We want the difference between the next bigger integer than minChargeTime and the next smaller integer than maxChargeTime
        return ceil(maxChargeTime).toLong() - (minChargeTime.toLong() + 1)
    }

    fun List<String>.toRaces(): List<Race> {
        val (times, distances) = this
        val timeNumbers = times.removePrefix("Time: ").asSpaceSeparatedLongs()
        val distanceNumbers = distances.removePrefix("Distance: ").asSpaceSeparatedLongs()
        return timeNumbers.zip(distanceNumbers).map { (time, distance) -> Race(time, distance) }
    }

    fun List<String>.toLongRace(): Race {
        val (times, distances) = this
        val time = times.removePrefix("Time: ").replace(" ", "").toLong()
        val distance = distances.removePrefix("Distance: ").replace(" ", "").toLong()
        return Race(time, distance)
    }

    fun part1(input: List<String>): Long {
        return input
            .toRaces()
            .map { it.possibleChargeDuration() }
            .product()
    }

    fun part2(input: List<String>): Long {
        return input
            .toLongRace()
            .possibleChargeDuration()
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288L)
    check(part2(testInput) == 71503L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
