import java.time.LocalDateTime
import kotlin.time.measureTimedValue

fun main() {
    val operational = '.'
    val damaged = '#'
    val unknown = '?'

    data class ConditionRecord(val conditions: List<Char>, val contiguousGroups: List<Int>)

    fun String.toConditionRecord() = split(' ').let { (conditions, contiguousGroups) ->
        ConditionRecord(conditions = conditions.let {
            buildList {
                for (condition in it) {
                    if (condition == unknown || condition == damaged || condition == operational && lastOrNull() != operational) {
                        add(condition)
                    }
                }
            }
        }, contiguousGroups = contiguousGroups.split(',').map { it.toInt() })
    }

    data class CacheKey(
        val conditionIndex: Int,
        val groupIndex: Int,
        val isFirstCase: Boolean,
    )

    val cache = mutableMapOf<CacheKey, Long>()

    fun countPossibleFits(
        record: ConditionRecord,
        conditionIndex: Int,
        groupIndex: Int,
        currentGroupParts: Int,
        sumOfRemainingGroupParts: Int,
        forceCondition: Char = '*',
    ): Long {
        if (sumOfRemainingGroupParts > record.conditions.size - conditionIndex) {
            return 0
        }
        val hasGroup = groupIndex <= record.contiguousGroups.lastIndex
        if (conditionIndex == record.conditions.size) {
            return if (!hasGroup && currentGroupParts == 0 || (groupIndex == record.contiguousGroups.lastIndex && currentGroupParts == record.contiguousGroups.last())) {
                1
            } else {
                0
            }
        }

        return when (if (forceCondition != '*') forceCondition else record.conditions[conditionIndex]) {
            operational -> when {
                !hasGroup || currentGroupParts == 0 -> cache.getOrPut(CacheKey(conditionIndex, groupIndex, true)) {
                    countPossibleFits(
                        record = record,
                        conditionIndex = conditionIndex + 1,
                        groupIndex = groupIndex,
                        currentGroupParts = 0,
                        sumOfRemainingGroupParts = sumOfRemainingGroupParts,
                    )
                }

                currentGroupParts == record.contiguousGroups[groupIndex] -> cache.getOrPut(CacheKey(conditionIndex, groupIndex, false)) {
                    countPossibleFits(
                        record = record,
                        conditionIndex = conditionIndex + 1,
                        groupIndex = groupIndex + 1,
                        currentGroupParts = 0,
                        sumOfRemainingGroupParts = sumOfRemainingGroupParts,
                    )
                }

                else -> 0
            }

            damaged -> when {
                !hasGroup || currentGroupParts == record.contiguousGroups[groupIndex] -> 0

                else -> countPossibleFits(
                    record = record,
                    conditionIndex = conditionIndex + 1,
                    groupIndex = groupIndex,
                    currentGroupParts = currentGroupParts + 1,
                    sumOfRemainingGroupParts = sumOfRemainingGroupParts - 1,
                )
            }

            unknown -> countPossibleFits(
                record = record,
                conditionIndex = conditionIndex,
                groupIndex = groupIndex,
                currentGroupParts = currentGroupParts,
                forceCondition = operational,
                sumOfRemainingGroupParts = sumOfRemainingGroupParts,
            ) + countPossibleFits(
                record = record,
                conditionIndex = conditionIndex,
                groupIndex = groupIndex,
                currentGroupParts = currentGroupParts,
                forceCondition = damaged,
                sumOfRemainingGroupParts = sumOfRemainingGroupParts,
            )

            else -> error("Unexpected record character")
        }
    }

    var count: Long

    fun countPossibleFits(record: ConditionRecord): Long {
        count = 0
        cache.clear()
        val (res, time) = measureTimedValue {
            countPossibleFits(
                record,
                conditionIndex = 0,
                groupIndex = 0,
                currentGroupParts = 0,
                sumOfRemainingGroupParts = record.contiguousGroups.sum()
            )
        }
        if (time.inWholeMilliseconds >= 100) {
            println("Took $time for ${record.conditions.joinToString("")} ${record.contiguousGroups.joinToString(",")} to $res")
        }
        count++
        if (count % 10 == 0L) {
            println("${LocalDateTime.now()}: Counted $count")
        }
        return res
    }

    fun part1(input: List<String>): Long =
        input.map { it.toConditionRecord() }.sumOf { record -> countPossibleFits(record) }

    fun part2(input: List<String>): Long = input.map { it.toConditionRecord() }.map {
        ConditionRecord(
            conditions = (it.conditions + unknown).repeated(5).dropLast(1),
            contiguousGroups = it.contiguousGroups.repeated(5)
        )
    }.sumOf { record -> countPossibleFits(record) }

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 21L)
    check(part2(testInput) == 525152L)

    val input = readInput("Day12")
    part1(input).println()
    part2(input).println()
}
