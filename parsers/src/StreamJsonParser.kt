import java.io.Reader

/**
 * O(n) JSON parser that uses
 * character stream reader as input
 */
object StreamJsonParser {
    private fun getNext(input: Reader): Char {
        var next = input.read().toChar()

        while (
            next == '\n' ||
            next == '\t' ||
            next == ' '
        ) next = input.read().toChar()

        return next
    }

    private fun parseString(input: Reader): String {
        var next = input.read().toChar()
        val result = StringBuilder()

        while (next != '"') {
            if (next == '\\')
                next = input.read().toChar()

            result.append(next)
            next = input.read().toChar()
        }

        return result.toString()
    }

    private fun parseNumber(first: Char, input: Reader): String {
        val result = StringBuilder().append(first)
        var next = input.read().toChar()

        while (next in '0'..'9') {
            result.append(next)
            next = input.read().toChar()
        }

        if (next == '.') {
            result.append(next)
            next = input.read().toChar()

            var isCorrect = false

            while (next in '0'..'9') {
                isCorrect = true
                result.append(next)
                next = input.read().toChar()
            }

            if (!isCorrect)
                throw Exception("No digits after `.` found")
        }

        if (next == 'e' || next == 'E') {
            result.append(next)
            next = input.read().toChar()

            if (next == '-') {
                result.append(next)
                next = input.read().toChar()
            }

            var isCorrect = false

            while (next in '0'..'9') {
                isCorrect = true
                result.append(next)
                next = input.read().toChar()
            }

            if (!isCorrect)
                throw Exception("No digits after `.` found")
        }

        return result.toString()
    }

    private fun parseList(input: Reader): Json.List {
        val list = Json.List()
        var next = getNext(input)

        while (next != ']') {
            list.add(parseObject(input, next))
            next = getNext(input)

            if (next == ',')
                next = getNext(input)
            else if (next != ']')
                throw Exception("`,` or `]` expected but `$next` found")
        }

        return list
    }

    private fun parseDictionary(input: Reader): Json.Dictionary {
        val dictionary = Json.Dictionary()
        var next = getNext(input)

        while (next != '}') {
            if (next != '"')
                throw Exception("`\"` expected but `$next` found")

            val key = parseString(input)
            next = getNext(input)

            if (next != ':')
                throw Exception("`:` expected but `$next` found")

            val value = parseObject(input)
            dictionary[key] = value
            next = getNext(input)

            if (next == ',')
                next = getNext(input)
            else if (next != '}')
                throw Exception("`,` or `}` expected but `$next` found")
        }

        return dictionary
    }

    private fun parseObject(input: Reader, next: Char = getNext(input)): Json.Object {
        if (next == '{')
            return parseDictionary(input)

        if (next == '[')
            return parseList(input)

        if (next == '"')
            return Json.Item(parseString(input), true)

        if (next == 't') {
            if (
                input.read().toChar() != 'r' ||
                input.read().toChar() != 'u' ||
                input.read().toChar() != 'e'
            ) throw Exception("`true` expected but some unknown identifier found")
            return Json.Item("true", false)
        }

        if (next == 'f') {
            if (
                input.read().toChar() != 'a' ||
                input.read().toChar() != 'l' ||
                input.read().toChar() != 's' ||
                input.read().toChar() != 'e'
            ) throw Exception("`false` expected but some unknown identifier found")
            return Json.Item("false", false)
        }

        if (next == '-' || next in '0'..'9')
            return Json.Item(parseNumber(next, input), false)

        throw Exception("Syntax error")
    }

    /**
     * Parses Json string representation
     * to Json.Object tree
     */
    fun parse(input: Reader): Json.Object {
        return try{
            parseObject(input)
        } catch (e: Exception) {
            println("Error occurred during parsing: ${e.message}")
            Json.Item("Nothing", true)
        }
    }
}