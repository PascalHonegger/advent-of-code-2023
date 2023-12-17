import java.util.*
import kotlin.collections.LinkedHashMap

fun main() {
    data class Position(val x: Int, val y: Int)

    class CityBlock(
        val position: Position,
        val heatLoss: Int,
        val neighbors: MutableList<CityBlock> = mutableListOf(),
    ) {
        fun connectWith(neighbor: CityBlock) {
            neighbors.add(neighbor)
            neighbor.neighbors.add(this)
        }

        override fun hashCode(): Int = position.hashCode()
        override fun equals(other: Any?): Boolean = this === other || other is CityBlock && position == other.position
    }

    data class CityNode(
        val block: CityBlock,
        val direction: Char,
        val howMuchStraight: Int,
        val isLegalEndOrTurningPoint: Boolean
    ) {
        val inverseDirection = when (direction) {
            'S' -> 'N'
            'E' -> 'W'
            'N' -> 'S'
            'W' -> 'E'
            else -> error("Unknown direction")
        }
    }

    val directions = listOf('S', 'E', 'N', 'W')

    fun List<String>.toCityBlocks(): Map<Position, CityBlock> {
        val result = mutableMapOf<Position, CityBlock>()
        forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                val position = Position(x, y)
                val block = CityBlock(position, c.digitToInt())
                result[position] = block
                result[Position(x - 1, y)]?.connectWith(block)
                result[Position(x, y - 1)]?.connectWith(block)
            }
        }
        return result
    }

    fun CityBlock.toNode(possibleStraights: IntRange) = directions.flatMap { direction ->
        (1..possibleStraights.last).map { howMuchStraight ->
            CityNode(
                this,
                direction,
                howMuchStraight,
                howMuchStraight in possibleStraights
            )
        }
    }

    fun pathFind(
        start: List<CityNode>,
        end: List<CityNode>,
        nodes: Map<CityBlock, List<CityNode>>,
    ): Int {
        val distances = start.associateWithTo(LinkedHashMap()) { 0 }
        val priorityQueue = PriorityQueue(compareBy(distances::getValue))
        start.filter { it.howMuchStraight == 1 }.forEach { priorityQueue.offer(it) }
        while (priorityQueue.isNotEmpty()) {
            val current = priorityQueue.poll()!!
            val distance = distances.getValue(current)
            for (neighborNodes in current.block.neighbors.flatMap(nodes::getValue)) {
                when (neighborNodes.direction) {
                    'S' -> if (neighborNodes.block.position.y <= current.block.position.y) continue
                    'E' -> if (neighborNodes.block.position.x <= current.block.position.x) continue
                    'N' -> if (neighborNodes.block.position.y >= current.block.position.y) continue
                    'W' -> if (neighborNodes.block.position.x >= current.block.position.x) continue
                }

                when (neighborNodes.direction) {
                    current.inverseDirection -> continue
                    current.direction -> if (neighborNodes.howMuchStraight - current.howMuchStraight != 1) continue
                    else -> if (!current.isLegalEndOrTurningPoint || neighborNodes.howMuchStraight != 1) continue
                }

                val newDistance = distance + neighborNodes.block.heatLoss
                val neighborDistance = distances[neighborNodes]
                if (neighborDistance == null || newDistance < neighborDistance) {
                    if (neighborNodes.isLegalEndOrTurningPoint && neighborNodes in end) {
                        return newDistance
                    }
                    distances[neighborNodes] = newDistance
                    priorityQueue.offer(neighborNodes)
                }
            }
        }
        error("Didn't find distance to end")
    }

    fun part1(input: List<String>): Int {
        val blocks = input.toCityBlocks()
        val nodes = blocks.values.associateWith { it.toNode(1..3) }
        val start = nodes.getValue(blocks.getValue(Position(0, 0)))
        val end = nodes.getValue(blocks.getValue(Position(input[0].lastIndex, input.lastIndex)))
        return pathFind(start, end, nodes)
    }

    fun part2(input: List<String>): Int {
        val blocks = input.toCityBlocks()
        val nodes = blocks.values.associateWith { it.toNode(4..10) }
        val start = nodes.getValue(blocks.getValue(Position(0, 0)))
        val end = nodes.getValue(blocks.getValue(Position(input[0].lastIndex, input.lastIndex)))
        return pathFind(start, end, nodes)
    }

    val testInput = readInput("Day17_test")
    check(part1(testInput) == 102)
    check(part2(testInput) == 94)
    check(part2(readInput("Day17_test2")) == 71)

    val input = readInput("Day17")
    part1(input).println()
    part2(input).println()
}
