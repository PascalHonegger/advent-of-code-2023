fun main() {
    data class Point2D(val x: Int, val y: Int) {
        init {
            check(x >= 0 && y >= 0)
        }
    }

    data class Point3D(val x: Int, val y: Int, val z: Int) {
        init {
            check(x >= 0 && y >= 0 && z >= 1)
        }
    }

    data class Cube(val start: Point3D, val end: Point3D) {
        val minX = minOf(start.x, end.x)
        val maxX = maxOf(start.x, end.x)
        val minY = minOf(start.y, end.y)
        val maxY = maxOf(start.y, end.y)
        val minZ = minOf(start.z, end.z)
        val maxZ = maxOf(start.z, end.z)

        val points2D = sequence {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    yield(Point2D(x, y))
                }
            }
        }

        fun movedDownBy(amount: Int) = Cube(
            start.copy(z = start.z - amount),
            end.copy(z = end.z - amount)
        )
    }

    fun String.toCoordinate(): Point3D {
        val (x, y, z) = split(",").map { it.toInt() }
        return Point3D(x, y, z)
    }

    fun String.toCube(): Cube {
        val (start, end) = split("~")
        return Cube(start.toCoordinate(), end.toCoordinate())
    }

    fun List<String>.toDroppedCubes(): Triple<List<Cube>, Map<Cube, Set<Cube>>, Map<Cube, Set<Cube>>> {
        val cubes = map { it.toCube() }
        val minZ = 1
        val maxZ = cubes.maxOf { it.maxZ }
        val workingSet = cubes.toMutableList()
        val movedDownCubes = mutableListOf<Cube>()
        val lowestPoints = mutableMapOf<Point2D, Cube>()
        val cubeSupports = mutableMapOf<Cube, MutableSet<Cube>>().withDefault { mutableSetOf() }
        val cubeSupportedBy = mutableMapOf<Cube, MutableSet<Cube>>().withDefault { mutableSetOf() }
        for (z in minZ..maxZ) {
            val iterator = workingSet.listIterator()
            while (iterator.hasNext()) {
                val cube = iterator.next()
                if (cube.minZ == z) {
                    iterator.remove()
                    var moveDownBy = Int.MAX_VALUE
                    for (point2D in cube.points2D) {
                        val lowestPoint = lowestPoints[point2D]?.maxZ ?: 0
                        moveDownBy = minOf(moveDownBy, cube.minZ - (lowestPoint + 1))
                    }
                    val movedDownCube = cube.movedDownBy(moveDownBy)
                    for (point2D in movedDownCube.points2D) {
                        lowestPoints[point2D]?.takeIf { lowestCube -> lowestCube.maxZ == movedDownCube.minZ - 1 }
                            ?.let { lowestCube ->
                                cubeSupportedBy.getOrPut(movedDownCube) { mutableSetOf() }.add(lowestCube)
                                cubeSupports.getOrPut(lowestCube) { mutableSetOf() }.add(movedDownCube)
                            }
                        lowestPoints[point2D] = movedDownCube
                    }
                    movedDownCubes.add(movedDownCube)
                }
            }
        }
        check(workingSet.isEmpty())
        check(movedDownCubes.size == cubes.size)
        return Triple(movedDownCubes, cubeSupports, cubeSupportedBy)
    }

    fun part1(input: List<String>): Int {
        val (movedDownCubes, cubeSupports, cubeSupportedBy) = input.toDroppedCubes()
        return movedDownCubes.count { cube ->
            cubeSupports.getValue(cube).all { cubeSupportedBy.getValue(it).size > 1 }
        }
    }

    fun part2(input: List<String>): Int {
        val (movedDownCubes, cubeSupports, cubeSupportedBy) = input.toDroppedCubes()
        return movedDownCubes.sumOf { cube ->
            val totalDroppedCubes = mutableSetOf(cube)
            var maybeDroppedCubes = cubeSupports.getValue(cube)
            while (maybeDroppedCubes.isNotEmpty()) {
                val actuallyDroppedCubes = maybeDroppedCubes.filter { totalDroppedCubes.containsAll(cubeSupportedBy.getValue(it)) }
                totalDroppedCubes += actuallyDroppedCubes
                maybeDroppedCubes = actuallyDroppedCubes.flatMap(cubeSupports::getValue).toSet()
            }
            totalDroppedCubes.size - 1 // Do not count the cube itself
        }
    }

    val testInput = readInput("Day22_test")
    check(part1(testInput) == 5)
    check(part2(testInput) == 7)

    val input = readInput("Day22")
    part1(input).println()
    part2(input).println()
}
