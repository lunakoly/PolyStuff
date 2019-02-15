fun main(args: Array<String>) {
    val result = JSON.parse("""
        {
            "files": [
                {
                    "name": "fileA.txt",
                    "size": 101424
                },
                {
                    "name": "fileB.txt",
                    "size": 225435
                }
            ]
        }
    """.trimIndent())

    for (file in result["files"] as JSON.List) {
        println("====")
        println("Name: " + file["name"])
        println("Size: " + file["size"] + " bytes")
    }
}