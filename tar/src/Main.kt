import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File


const val CHUNK_SIZE = 16

val PARAMETERS = HashMap<String, String>()
val SOURCES = ArrayList<String>()


fun getFileSize(file: File): Long {
    val reader = file.bufferedReader()
    val chunk = CharArray(CHUNK_SIZE)
    var size: Long = 0

    var read = reader.read(chunk)

    while (read != -1) {
        size += read
        read = reader.read(chunk)
    }

    reader.close()
    return size
}

fun appendFileContents(source: File, target: BufferedWriter) {
    val reader = source.bufferedReader()
    val chunk = CharArray(CHUNK_SIZE)
    var read = reader.read(chunk)

    // println("FOLDING...")

    while (read != -1) {
        // println("Size: $read")
        // println(chunk)

        target.write(chunk, 0, read)
        read = reader.read(chunk)
    }

    reader.close()
}

fun extractFileContents(source: BufferedReader, target: File, size: Long) {
    val writer = target.bufferedWriter()
    val chunk = CharArray(CHUNK_SIZE)
    var read: Long = 0

    // println("EXTRACTING...")

    while (read < size - CHUNK_SIZE) {
        source.read(chunk)
        writer.write(chunk)
        read += CHUNK_SIZE

        // println("Size: CHUNK")
        // println(chunk)
    }

    val rest = (size - read).toInt()
    source.read(chunk, 0, rest)
    writer.write(chunk, 0, rest)

    // println("Size: $rest")
    // println(chunk)

    writer.close()
}

fun pack() {
    val header = Json.Dictionary()
    val files = Json.List()
    header["files"] = files

    for (source in SOURCES) {
        val item = Json.Dictionary()
        item["name"] = Json.Item(source, true)
        item["size"] = Json.Item(getFileSize(File(source)).toString(), true)
        files.add(item)
    }

    val output = File(PARAMETERS["output"]).bufferedWriter()
    output.write(header.toString())

    for (source in SOURCES)
        appendFileContents(File(source), output)

    output.close()
}

fun unpack() {
    val input = File(PARAMETERS["unpack"])
    val reader = input.bufferedReader()
    val header = StreamJsonParser.parse(reader)

    try {
        for (source in header["files"] as Json.List) {
            val target = File(source["name"].value)
            extractFileContents(reader, target, source["size"].value.toLong())
        }

    } catch (e: Exception) {
        println("Error > Corrupted file header")
        return
    }

    reader.close()
}

fun main(args: Array<String>) {
    var it = 0

    // fill global state with user data
    while (it < args.size) {
        when (args[it]) {
            "-u" -> {
                if (it + 1 >= args.size) {
                    println("Error > Option `-u` requires a file")
                    return
                } else {
                    PARAMETERS["unpack"] = args[it + 1]
                    it++
                }
            }

            "-out" -> {
                if (it + 1 >= args.size) {
                    println("Error > Option `-out` requires a file")
                    return
                } else {
                    PARAMETERS["output"] = args[it + 1]
                    it++
                }
            }

            else -> {
                SOURCES.add(args[it])
            }
        }

        it++
    }

    // validate global state and do stuff
    if (PARAMETERS["unpack"] != null) {
        if (PARAMETERS["output"] != null) {
            println("Error > Output file can't be specified for unpacking")
            return
        }

        if (SOURCES.size > 0) {
            println("Error > Source files can't be specified for unpacking")
            return
        }

        unpack()
    } else {
        if (PARAMETERS["output"] == null) {
            println("Error > Output file must be specified via `-out`")
            return
        }

        pack()
    }
}