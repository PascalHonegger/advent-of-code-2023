import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src", "$name.txt")
    .readLines()

/**
 * Reads whole text from the given input txt file.
 */
fun readTextInput(name: String) = Path("src", "$name.txt")
    .readText().trim()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * Calculate product by multiplying all elements together
 */
fun Iterable<Int>.product() = reduce(Int::times)

/**
 * Calculate product by multiplying all elements together
 */
fun Iterable<Long>.product() = reduce(Long::times)


/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

/** Converts "1 2  3" to [1, 2, 3] */
fun String.asSpaceSeparatedInts() = split(" ").filter { it.isNotBlank() }.map { it.toInt() }
fun String.asSpaceSeparatedLongs() = split(" ").filter { it.isNotBlank() }.map { it.toLong() }

inline fun <reified T> MutableMap<T, Int>.increase(key: T, by: Int = 1): Int =
    compute(key) { _, value ->
        (value ?: 0) + by
    }!!

inline fun <reified T> List<T>.repeated(n: Int) = buildList<T> {
    repeat(n) {
        addAll(this@repeated)
    }
}

/**
 * Taken from https://www.baeldung.com/kotlin/lcm
 */
fun findLCM(a: Long, b: Long): Long {
    val larger = if (a > b) a else b
    val maxLcm = a * b
    var lcm = larger
    while (lcm <= maxLcm) {
        if (lcm % a == 0L && lcm % b == 0L) {
            return lcm
        }
        lcm += larger
    }
    return maxLcm
}

/**
 * Safe transpose a list of unequal-length lists.
 *
 * Example:
 * transpose(List(List(1, 2, 3), List(4, 5, 6), List(7, 8)))
 * -> List(List(1, 4, 7), List(2, 5, 8), List(3, 6))
 *
 * Inspired by https://gist.github.com/clementgarbay/49288c006252955c2a3c6139a61ca92a
 */
fun <E> List<List<E>>.transpose(): List<List<E>> {
    // Helpers
    fun <E> List<E>.head(): E = this.first()
    fun <E> List<E>.tail(): List<E> = this.takeLast(this.size - 1)
    fun <E> E.append(xs: List<E>): List<E> = listOf(this).plus(xs)

    return filter { it.isNotEmpty() }.let { ys ->
        if (ys.isNotEmpty()) {
            ys.map { it.head() }.append(ys.map { it.tail() }.transpose())
        } else {
            emptyList()
        }
    }
}
