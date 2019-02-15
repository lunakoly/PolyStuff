import CStyleLexer.Fetcher
import CStyleLexer.scan
import CStyleLexer.scanFloat
import CStyleLexer.scanString
import CStyleLexer.scanKeyword
import CStyleLexer.scanNonBlank

/**
 * Holds functionality needed
 * for JSON representation
 */
object JSON {
    /**
     * Any node of a JSON tree. Holds izi methods
     * for automatic unsafe casting to simplify
     * the usage of the parsed tree (provided the user
     * knows what the tree looks like)
     */
    interface Object {
        val value get() = (this as Item).value

        operator fun get(key: String): Object {
            return (this as Dictionary)[key]
        }

        operator fun get(index: Int): Object {
            return (this as List)[index]
        }
    }

    /**
     * Wrapper for String
     */
    class Item(override var value: String) : Object {
        override fun toString(): String {
            return value
        }
    }

    /**
     * Wrapper for List
     */
    class List : ArrayList<Object>(), Object {
        override operator fun get(index: Int): Object {
            return super<ArrayList>.get(index)
        }
    }

    /**
     * Wrapper for Dictionary
     */
    class Dictionary : HashMap<String, Object>(), Object {
        override operator fun get(key: String): Object {
            return super<HashMap>.get(key)!!
        }
    }


    private fun parseList(out: Fetcher): List {
        val list = List()

        do {
            list.add(parseObject(out))
        } while (scan(",", out))

        return list
    }

    private fun parseDictionary(out: CStyleLexer.Fetcher): Dictionary {
        val dictionary = Dictionary()

        do {
            if (!scanString(out))
                continue

            val key = out.value.toString()

            if (!scan(":", out))
                continue

            val value = parseObject(out)
            dictionary[key] = value

        } while (scan(",", out))

        return dictionary
    }

    private fun parseObject(out: Fetcher): Object {
        if (scan("{", out)) {
            val value = parseDictionary(out)

            if (!scan("}", out))
                return Dictionary()

            return value
        }

        if (scan("[", out)) {
            val value = parseList(out)

            if (!scan("]", out))
                return List()

            return value
        }

        if (
            scanKeyword("false", out) ||
            scanKeyword("true", out) ||
            scanString(out) ||
            scanFloat(out)
        ) return Item(out.value.toString())

        scanNonBlank(out)
        return Item(out.value.toString())
    }

    /**
     * Parses JSON string representation
     * to JSON.Object tree
     */
    fun parse(text: String): Object {
        val out = Fetcher(text, 0)
        return parseObject(out)
    }
}