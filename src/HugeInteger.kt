import kotlin.math.max

/**
 * Killer of BigInteger
 */
class HugeInteger(representation: String = "0") {
    /**
     * The amount of digits encoded
     * with one element
     */
    private val ELEMENT_SIZE = 1//9
    /**
     * The actual maximum element value
     */
    private val ELEMENT_MAX = 9//999_999_999
    /**
     * Internal 'digits' as number of base 10^ELEMENT_SIZE
     */
    private val elements = arrayListOf<Int>()

    init {
        try {
            // iterate string starting from the end
            // and pack every 9 chars into elements
            //     nnnnnnnnnnnnn
            //        ---------^
            //     --^
            // or whatever left
            for (it in representation.length downTo 1 step ELEMENT_SIZE) {
                if (it > ELEMENT_SIZE)
                    elements.add(representation.substring(it - ELEMENT_SIZE, it).toInt())
                else
                    elements.add(representation.substring(0, it).toInt())
            }
        } catch (e: Exception) {
            elements.add(0)
        }
    }

    constructor(representation: Int) : this(representation.toString())

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(String.format("%d", elements.last()))

        for (it in elements.size - 2 downTo 0)
            builder.append(String.format("%01d", elements[it]))

        return builder.toString()
    }

    operator fun compareTo(other: HugeInteger): Int {
        for (it in max(this.elements.size - 1, other.elements.size - 1) downTo 0) {
            if (
                this.elements.getOrElse(it) { 0 } > other.elements.getOrElse(it) { 0 }
            ) return 1

            if (
                this.elements.getOrElse(it) { 0 } < other.elements.getOrElse(it) { 0 }
            ) return -1
        }

        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (other is HugeInteger)
            return this.compareTo(other) == 0
        return false
    }

    operator fun plus(other: HugeInteger): HugeInteger {
        val result = HugeInteger()
        result.elements.clear()

        var rest = 0
        var it = 0

        while (
            it <  this.elements.size ||
            it < other.elements.size ||
            rest != 0
        ) {
            val sum = this.elements.getOrElse(it) { 0 } + other.elements.getOrElse(it) { 0 } + rest

            rest = if (sum <= ELEMENT_MAX) {
                result.elements.add(sum)
                0
            } else {
                result.elements.add(sum - ELEMENT_MAX - 1)
                1
            }

            it++
        }

        return result
    }

    operator fun minus(other: HugeInteger): HugeInteger {
        if (other > this)
            throw Exception("HugeInteger does not support operations involving negative numbers")

        val result = HugeInteger()
        result.elements.clear()

        var rest = 0
        var it = 0

        while (
            it <  this.elements.size ||
            it < other.elements.size
        ) {
            val difference = this.elements.getOrElse(it) { 0 } - other.elements.getOrElse(it) { 0 } + rest

            rest = if (difference >= 0) {
                result.elements.add(difference)
                0
            } else {
                result.elements.add(ELEMENT_MAX + 1 + difference)
                -1
            }

            it++
        }

        while (result.elements.size > 1 && result.elements.last() == 0)
            result.elements.removeAt(result.elements.lastIndex)

        return result
    }
}