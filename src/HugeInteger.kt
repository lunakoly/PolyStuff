import kotlin.math.max

/**
 * The Killer of BigInteger
 *
 * Internal 'digits' are stored as numbers
 * of base 10^ELEMENT_SIZE
 */
class HugeInteger private constructor(private val elements: ArrayList<Int>) {
    companion object {
        /**
         * The amount of digits encoded
         * with one element
         */
        const val ELEMENT_SIZE = 9 // debug: 1
        /**
         * Element maximum value + 1
         */
        const val ELEMENT_BASE = 1_000_000_000 // debug: 10

        /**
         * Returns new HugeInteger constructed from String
         */
        fun from(representation: String): HugeInteger {
            val elements = arrayListOf<Int>()

            // iterate string starting from the end
            // and pack every ELEMENT_SIZE chars into elements
            //     nnnnnnnnnnnnn
            //        ---------^
            //     --^
            // or whatever left

            try {
                for (it in representation.length downTo 1 step ELEMENT_SIZE) {
                    if (it > ELEMENT_SIZE)
                        elements.add(representation.substring(it - ELEMENT_SIZE, it).toInt())
                    else
                        elements.add(representation.substring(0, it).toInt())
                }
            } catch (e: Exception) {
                elements.add(0)
            }

            return HugeInteger(elements)
        }

        /**
         * Returns new HugeInteger constructed from Int
         */
        fun from(representation: Int): HugeInteger {
            val elements = arrayListOf<Int>()
            var rest = representation

            do {
                elements.add(rest % ELEMENT_BASE)
                rest /= ELEMENT_BASE
            } while (rest > 0)

            return HugeInteger(elements)
        }

        /**
         * Returns new zero HugeInteger
         */
        fun zero() = HugeInteger(arrayListOf(0))
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(String.format("%d", elements.last()))

        for (it in elements.size - 2 downTo 0)
            builder.append(String.format("%09d", elements[it])) // debug: %01d

        return builder.toString()
    }

    operator fun compareTo(other: HugeInteger): Int {
        // getOrElse(it) { 0 } allows us to automatically handle
        // trailing zeroes so that we may skip checking whether one
        // number is longer than another

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

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    operator fun plus(other: HugeInteger): HugeInteger {
        val elements = arrayListOf<Int>()

        var rest = 0
        var it = 0

        while (
            it <  this.elements.size ||
            it < other.elements.size ||
            rest != 0
        ) {
            val sum = this.elements.getOrElse(it) { 0 } + other.elements.getOrElse(it) { 0 } + rest

            rest = if (sum < ELEMENT_BASE) {
                elements.add(sum)
                0
            } else {
                elements.add(sum - ELEMENT_BASE)
                1
            }

            it++
        }

        return HugeInteger(elements)
    }

    operator fun minus(other: HugeInteger): HugeInteger {
        if (other > this)
            throw Exception("HugeInteger does not support operations involving negative numbers")

        val elements = arrayListOf<Int>()

        var rest = 0
        var it = 0

        while (
            it <  this.elements.size ||
            it < other.elements.size
        ) {
            val difference = this.elements.getOrElse(it) { 0 } - other.elements.getOrElse(it) { 0 } + rest

            rest = if (difference >= 0) {
                elements.add(difference)
                0
            } else {
                elements.add(ELEMENT_BASE + difference)
                -1
            }

            it++
        }

        // clear trailing zeroes
        while (elements.size > 1 && elements.last() == 0)
            elements.removeAt(elements.lastIndex)

        return HugeInteger(elements)
    }

    operator fun times(other: HugeInteger): HugeInteger {
        val elements = arrayListOf<Int>()

        // here we merely do the multiplication
        // as if we would do it manually on a shit of
        // paper

        //   XXXXX
        //      YY
        //   -----
        //  zZZZZZ
        // wWWWWW
        // -------
        // TTTTTTT

        // the resulting length is a sum of lengths as it can
        // be seen here

        for (it in 0 until this.elements.size + other.elements.size)
            elements.add(0)

        var rest = 0
        var i = 0

        // iterating `i` is like iterating `X`s
        // iterating `j` is going over `Y`s
        // and we should remember to watch for the `rest`
        // that appears because of additions in `Z` and `W`

        while (i < this.elements.size) {
            var j = 0

            while (j < other.elements.size || rest != 0) {
                // bin numbers can be really big
                // and so my last times() test will fail if I
                // don't do toLong()
                val value = elements[i + j] + this.elements[i].toLong() * other.elements.getOrElse(j) { 0 } + rest

                // `Z` and `W` tend to overlap sometimes.
                // elements[i + j] refers to the current cell

                elements[i + j] = (value % ELEMENT_BASE).toInt()
                rest = (value / ELEMENT_BASE).toInt()
                j++
            }

            i++
        }

        // trailing zeroes
        while (elements.size > 1 && elements.last() == 0)
            elements.removeAt(elements.lastIndex)

        return HugeInteger(elements)
    }

    operator fun div(other: HugeInteger): HugeInteger {
        if (other > this)
            return HugeInteger.zero()

        val elements = arrayListOf<Int>()

        // the same way as if we would
        // calculate on the paper

        // XXXX   | YY
        //  ZZ    |
        // ---
        //  WW

        // the dividend here is XXX
        // let's iterate all X incrementally
        // and find such `dividend` that it'll
        // be greater than `other`

        var dividendElements = arrayListOf<Int>()

        for (it in this.elements.size - 1 downTo 0) {
            // we construct dividend from separate `X`s
            dividendElements.add(0, this.elements[it])
            var dividend = HugeInteger(dividendElements)

            if (dividend < other) {
                elements.add(0)
                continue
            }

            // divide manually
            var quotient = 0

            while (dividend >= other) {
                dividend -= other
                quotient++
            }

            // remember our new dividend digits
            // and use them later
            dividendElements = dividend.elements
            elements.add(quotient)
        }

        // the order of digits of our result
        // needs to be inverted
        elements.reverse()

        // clear trailing zeroes
        while (elements.size > 1 && elements.last() == 0)
            elements.removeAt(elements.lastIndex)

        return HugeInteger(elements)
    }

    operator fun rem(other: HugeInteger): HugeInteger {
        if (other > this)
            return this

        val quotient = this / other
        val lowerBorder = quotient * other
        return this - lowerBorder
    }
}