fun main() {
    abstract class Gate(val name: String) {
        val inputs: MutableList<Gate> = mutableListOf()
        val outputs: MutableList<Gate> = mutableListOf()
        var receivedHighSignals = 0L
            private set
        var receivedLowSignals = 0L
            private set

        fun sendSignal(source: Gate, signal: Boolean): Boolean? {
            if (signal) receivedHighSignals++ else receivedLowSignals++
            // println("${source.name} -${if (signal) "high" else "low"}-> $name")
            return handleSignal(source, signal)
        }

        protected abstract fun handleSignal(source: Gate, signal: Boolean): Boolean?
    }

    class NandGate(name: String) : Gate(name) {
        val states = lazy {
            BooleanArray(inputs.size)
        }

        override fun handleSignal(source: Gate, signal: Boolean): Boolean {
            states.value[inputs.indexOf(source)] = signal
            return !states.value.all { it }
        }
    }

    class FlipFlopGate(name: String) : Gate(name) {
        var state = false
        override fun handleSignal(source: Gate, signal: Boolean): Boolean? {
            if (signal) return null
            state = !state
            return state
        }
    }

    class BroadcasterGate(name: String) : Gate(name) {
        init {
            check(name == "broadcaster")
        }

        override fun handleSignal(source: Gate, signal: Boolean): Boolean {
            return signal
        }
    }

    class DeadEndGate(name: String) : Gate(name) {
        override fun handleSignal(source: Gate, signal: Boolean) = null
    }

    class ButtonGate(name: String) : Gate(name) {
        override fun handleSignal(source: Gate, signal: Boolean) = error("Button cannot receive signals")
    }

    fun List<String>.toGates(): Map<String, Gate> {
        val gates = mutableMapOf<String, Gate>()
        for (line in this) {
            val moduleName = line.takeWhile { it != ' ' }
            if (moduleName.startsWith('%')) {
                val actualName = moduleName.removePrefix("%")
                gates[actualName] = FlipFlopGate(actualName)
            } else if (moduleName.startsWith('&')) {
                val actualName = moduleName.removePrefix("&")
                gates[actualName] = NandGate(actualName)
            } else {
                gates[moduleName] = BroadcasterGate(moduleName)
            }
        }
        for (line in this) {
            val (moduleName, output) = line.split(" -> ")
            val actualName = moduleName.removePrefix("%").removePrefix("&")
            val gate = gates.getValue(actualName)
            for (out in output.split(", ")) {
                val outGate = gates.getOrPut(out) { DeadEndGate(out) }
                gate.outputs.add(outGate)
                outGate.inputs.add(gate)
            }
        }
        return gates
    }

    fun part1(input: List<String>): Long {
        val gates = input.toGates()
        val buttonPresses = 1_000
        val buttonGate = ButtonGate("button")
        val broadcaster = gates.getValue("broadcaster")

        data class SignalStep(val source: Gate, val destination: Gate, val signal: Boolean)
        repeat(buttonPresses) {
            val workingSet = ArrayDeque<SignalStep>()
            val initialSignal = broadcaster.sendSignal(buttonGate, false)!!
            workingSet.addAll(broadcaster.outputs.map { SignalStep(broadcaster, it, initialSignal) })
            while (workingSet.isNotEmpty()) {
                val (source, destination, signal) = workingSet.removeFirst()
                val nextSignal = destination.sendSignal(source, signal)
                if (nextSignal != null) {
                    workingSet.addAll(destination.outputs.map { SignalStep(destination, it, nextSignal) })
                }
            }
        }
        return gates.values.sumOf { it.receivedHighSignals } * gates.values.sumOf { it.receivedLowSignals }
    }

    fun part2(input: List<String>): Long {
        val gates = input.toGates()

        buildString {
            // Plantuml visualization
            appendLine("@startuml")
            appendLine("skinparam hideEmptyAttributes true")
            for (gate in gates.values) {
                appendLine("object ${gate.name}")
            }
            for (gate in gates.values) {
                for (output in gate.outputs) {
                    appendLine("${gate.name} --> ${output.name}")
                }
            }
            appendLine("@enduml")
        }.println()

        val buttonGate = ButtonGate("button")
        val broadcaster = gates.getValue("broadcaster")
        val rx = gates.getValue("rx")
        val mf = rx.inputs.single()
        check(mf is NandGate)
        check(mf.inputs.size == 4)
        check(mf.inputs.all { it is NandGate })
        val cycleSizes = mf.inputs.associateWith { null as Long? }.toMutableMap()
        data class SignalStep(val source: Gate, val destination: Gate, val signal: Boolean)
        while (cycleSizes.values.any { it == null }) {
            val beforeSignals = mf.states.value.copyOf()
            val workingSet = ArrayDeque<SignalStep>()
            val initialSignal = broadcaster.sendSignal(buttonGate, false)!!
            workingSet.addAll(broadcaster.outputs.map { SignalStep(broadcaster, it, initialSignal) })
            while (workingSet.isNotEmpty()) {
                val (source, destination, signal) = workingSet.removeFirst()
                val nextSignal = destination.sendSignal(source, signal)
                if (nextSignal != null) {
                    workingSet.addAll(destination.outputs.map { SignalStep(destination, it, nextSignal) })
                }
                for (i in beforeSignals.indices) {
                    if (beforeSignals[i] != mf.states.value[i]) {
                        val gate = mf.inputs[i]
                        val cycleSize = broadcaster.receivedLowSignals
                        cycleSizes[gate] = cycleSize
                    }
                }
            }
        }
        val (a, b, c, d) = cycleSizes.values.map { it!! }
        return findLCM(findLCM(a, b), findLCM(c, d))
    }

    val testInput1 = readInput("Day20_test")
    val testInput2 = readInput("Day20_test2")
    check(part1(testInput1) == 32000000L)
    check(part1(testInput2) == 11687500L)

    val input = readInput("Day20")
    part1(input).println()
    part2(input).println()
}
