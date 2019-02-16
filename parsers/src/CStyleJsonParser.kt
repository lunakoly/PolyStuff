import CStyleLexer.Fetcher
import CStyleLexer.scan
import CStyleLexer.scanFloat
import CStyleLexer.scanString
import CStyleLexer.scanKeyword
import CStyleLexer.scanNonBlank

/**
 * 'C-Style' parser for Json that uses
 * CStyleLexer for lexical analysis
 */
object CStyleJsonParser {
    private fun parseList(out: Fetcher): Json.List {
        val list = Json.List()

        do {
            list.add(parseObject(out))
        } while (scan(",", out))

        return list
    }

    private fun parseDictionary(out: Fetcher): Json.Dictionary {
        val dictionary = Json.Dictionary()

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

    private fun parseObject(out: Fetcher): Json.Object {
        if (scan("{", out)) {
            val value = parseDictionary(out)

            if (!scan("}", out))
                return Json.Dictionary()

            return value
        }

        if (scan("[", out)) {
            val value = parseList(out)

            if (!scan("]", out))
                return Json.List()

            return value
        }

        if (scanString(out))
            return Json.Item(out.value.toString(), true)

        if (
            scanKeyword("false", out) ||
            scanKeyword("true", out) ||
            scanFloat(out)
        ) return Json.Item(out.value.toString(), false)

        scanNonBlank(out)
        return Json.Item(out.value.toString(), true)
    }

    /**
     * Parses Json string representation
     * to Json.Object tree
     */
    fun parse(text: String): Json.Object {
        val out = Fetcher(text, 0)
        return parseObject(out)
    }
}