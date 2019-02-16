/**
 * Simple regex-driven parser for Json
 */
object RegexJsonParser {
    data class Fetcher(val text: String, var index: Int)

    private val NUMBER = """-?\d+(?:.\d+(?:[eE]-?\d+)?)?""".toRegex()
    private val STRING = """"(?:\\"|[^"])*"""".toRegex()
    private val FALSE = """false""".toRegex()
    private val TRUE = """true""".toRegex()

    private fun skipIndent(out: Fetcher) {
        val result = """[\n\t ]+""".toRegex().find(out.text, out.index)

        if (result != null && result.range.start == out.index)
            out.index = result.range.endInclusive + 1
    }

    private fun expect(char: Char, out: Fetcher): Boolean {
        skipIndent(out)

        if (out.text[out.index] == char) {
            out.index++
            return true
        }

        return false
    }

    private fun read(pattern: Regex, out: Fetcher): MatchResult? {
        skipIndent(out)
        val result = pattern.find(out.text, out.index)

        if (result != null && result.range.start == out.index) {
            out.index = result.range.endInclusive + 1
            return result
        }

        return null
    }

    private fun parseList(out: Fetcher): Json.List {
        val list = Json.List()

        do {
            list.add(parseObject(out))
        } while (expect(',', out))

        return list
    }

    private fun parseDictionary(out: Fetcher): Json.Dictionary {
        val dictionary = Json.Dictionary()

        do {
            val match = read(STRING, out)
                ?: continue

            val key = match.value.substring(1, match.value.length - 1)

            if (!expect(':', out))
                continue

            val value = parseObject(out)
            dictionary[key] = value

        } while (expect(',', out))

        return dictionary
    }

    private fun parseObject(out: Fetcher): Json.Object {
        if (expect('{', out)) {
            val result = parseDictionary(out)

            if (!expect('}', out))
                throw Exception("`}` expected but `${out.text[out.index]}` found at ${out.text.substring(out.index, out.index + 10)}")

            return result
        }

        if (expect('[', out)) {
            val result = parseList(out)

            if (!expect(']', out))
                throw Exception("`]` expected but `${out.text[out.index]}` found at ${out.text.substring(out.index, out.index + 10)}")

            return result
        }

        skipIndent(out)
        var match = read(STRING, out)

        if (match != null)
            return Json.Item(match.value.substring(1, match.value.length - 1), true)

        match = read(NUMBER, out)
            ?: read(FALSE, out)
            ?: read(TRUE, out)
            ?: throw Exception("illegal symbol `${out.text[out.index]}` at ${out.text.substring(out.index, out.index + 10)}")

        return Json.Item(match.value, false)
    }

    /**
     * Parses Json string representation
     * to Json.Object tree
     */
    fun parse(text: String): Json.Object {
        return try {
            parseObject(Fetcher(text, 0))
        } catch (e: Exception) {
            println("Error occurred during parsing: ${e.message}")
            Json.Item("Nothing", true)
        }
    }
}