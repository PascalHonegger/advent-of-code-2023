fun main() {
    val start = "in"
    val accepted = "A"
    val rejected = "R"
    val maxPossibleRange = 1..4000

    fun IntRange.inverted() = when {
        first == maxPossibleRange.first -> (last + 1)..maxPossibleRange.last
        last == maxPossibleRange.last -> maxPossibleRange.first..<first
        else -> error("Can't invert range $this")
    }

    infix fun IntRange.intersectedWith(other: IntRange) = (maxOf(first, other.first))..(minOf(last, other.last))

    data class PartRating(val x: Int, val m: Int, val a: Int, val s: Int) {
        val total: Int get() = x + m + a + s
    }

    data class Transition(
        val to: String,
        val xRange: IntRange,
        val mRange: IntRange,
        val aRange: IntRange,
        val sRange: IntRange,
    )

    data class Workflow(val name: String, val transitions: List<Transition>)

    fun String.toWorkflow(): Workflow {
        val (name, conditionsPart) = split("{")
        val conditions = conditionsPart.removeSuffix("}").split(",")

        var transitionXRange = maxPossibleRange
        var transitionMRange = maxPossibleRange
        var transitionARange = maxPossibleRange
        var transitionSRange = maxPossibleRange

        fun createTransition(
            to: String,
            xRange: IntRange = transitionXRange,
            mRange: IntRange = transitionMRange,
            aRange: IntRange = transitionARange,
            sRange: IntRange = transitionSRange,
        ): Transition = Transition(
            to = to,
            xRange = xRange,
            mRange = mRange,
            aRange = aRange,
            sRange = sRange,
        )

        val transitions = conditions.map { conditionString ->
            if (":" !in conditionString) {
                return@map createTransition(conditionString)
            }
            val (conditionPart, to) = conditionString.split(":")
            val compareWith = conditionPart.drop(2).toInt()
            val range =
                if (conditionPart[1] == '<') maxPossibleRange.first..<compareWith else (compareWith + 1)..maxPossibleRange.last
            when (conditionPart[0]) {
                's' -> {
                    val transition = createTransition(
                        to, sRange = transitionSRange intersectedWith range
                    )
                    transitionSRange = transitionSRange intersectedWith range.inverted()
                    transition
                }

                'x' -> {
                    val transition = createTransition(
                        to, xRange = transitionXRange intersectedWith range,
                    )
                    transitionXRange = transitionXRange intersectedWith range.inverted()
                    transition
                }

                'm' -> {
                    val transition = createTransition(
                        to, mRange = transitionMRange intersectedWith range,
                    )
                    transitionMRange = transitionMRange intersectedWith range.inverted()
                    transition
                }

                'a' -> {
                    val transition = createTransition(
                        to, aRange = transitionARange intersectedWith range,
                    )
                    transitionARange = transitionARange intersectedWith range.inverted()
                    transition
                }

                else -> error("Unknown condition: $conditionString")
            }
        }

        return Workflow(name, transitions)
    }

    fun List<String>.toWorkflows() = map { it.toWorkflow() }.associateBy { it.name }

    fun String.toPartRating(): PartRating {
        val (x, m, a, s) = removeSurrounding("{", "}").split(",").map { it.split("=").last().toInt() }
        return PartRating(x, m, a, s)
    }

    fun part1(input: List<String>): Long {
        val emptyRow = input.indexOfFirst { it.isEmpty() }
        val workflows = input.subList(0, emptyRow).toWorkflows()
        val partRatings = input.subList(emptyRow + 1, input.size).map { it.toPartRating() }

        var sum = 0L
        for (partRating in partRatings) {
            var current = start
            while (current != accepted && current != rejected) {
                current = workflows.getValue(current).transitions.first {
                    partRating.a in it.aRange && partRating.m in it.mRange && partRating.x in it.xRange && partRating.s in it.sRange
                }.to
            }
            if (current == accepted) {
                sum += partRating.total.toLong()
            }
        }
        return sum
    }

    fun part2(input: List<String>): Long {
        val workflows = input.takeWhile { it.isNotEmpty() }.toWorkflows()
        val startWorkflow = workflows.getValue(start)

        val nextSteps = ArrayDeque<Pair<Workflow, List<Transition>>>()
        nextSteps.add(startWorkflow to emptyList())

        var total = 0L
        while (nextSteps.isNotEmpty()) {
            val (workflow, history) = nextSteps.removeFirst()
            for (transition in workflow.transitions) {
                when (transition.to) {
                    accepted -> {
                        var validXRange = transition.xRange
                        var validMRange = transition.mRange
                        var validARange = transition.aRange
                        var validSRange = transition.sRange
                        for (h in history) {
                            validXRange = validXRange intersectedWith h.xRange
                            validMRange = validMRange intersectedWith h.mRange
                            validARange = validARange intersectedWith h.aRange
                            validSRange = validSRange intersectedWith h.sRange
                        }

                        val x = validXRange.simpleSize.toLong()
                        val m = validMRange.simpleSize.toLong()
                        val a = validARange.simpleSize.toLong()
                        val s = validSRange.simpleSize.toLong()
                        total += x * m * a * s
                    }

                    rejected -> Unit
                    else -> nextSteps.add(workflows.getValue(transition.to) to history + transition)
                }
            }
        }
        return total
    }

    val testInput = readInput("Day19_test")
    check(part1(testInput) == 19114L)
    check(part2(testInput) == 167409079868000L)

    val input = readInput("Day19")
    part1(input).println()
    part2(input).println()
}
