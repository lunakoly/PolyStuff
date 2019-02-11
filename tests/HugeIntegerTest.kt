import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class HugeIntegerTest {
    @Test
    fun testToString() {
        println("Testing HugeInteger {${HugeInteger.ELEMENT_SIZE}, ${HugeInteger.ELEMENT_BASE}}")

        assertEquals("0", HugeInteger.zero().toString())
        assertEquals("0", HugeInteger.from(0).toString())
        assertEquals("0", HugeInteger.from("0").toString())

        assertEquals("136", HugeInteger.from("136").toString())
        assertEquals("1234567890987654321", HugeInteger.from("1234567890987654321").toString())

        assertEquals("124155", HugeInteger.from(124155).toString())
    }

    private fun testSinglePlusOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() + Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger.from(a) + HugeInteger.from(b)).toString())
        println("$a + $b = $proper")
    }

    @Test
    fun plus() {
        testSinglePlusOperation("10", "11")
        testSinglePlusOperation("99999999", "1")
        testSinglePlusOperation("999999999", "1")
        testSinglePlusOperation("1600000000", "1600000000")
        testSinglePlusOperation("921436", "226274")
    }

    @Test
    fun compareTo() {
        assert(HugeInteger.from("0") == HugeInteger.zero())
        assert(HugeInteger.from("10") > HugeInteger.from("0"))
        assert(HugeInteger.from("15") < HugeInteger.from("16"))
        assert(HugeInteger.from("1600000000") < HugeInteger.from("1600000001"))
        assert(HugeInteger.from("1600000000") > HugeInteger.from("1001"))
        assert(HugeInteger.from("16000000014124235235240") >= HugeInteger.from("1001"))
        assert(HugeInteger.from("16") != HugeInteger.from("5"))
    }

    private fun testSingleMinusOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() - Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger.from(a) - HugeInteger.from(b)).toString())
        println("$a - $b = $proper")
    }

    @Test
    fun minus() {
        testSingleMinusOperation("21", "10")
        testSingleMinusOperation("10", "10")
        testSingleMinusOperation("0", "0")
        testSingleMinusOperation("1", "0")
        testSingleMinusOperation("1000000000", "999999999")
    }

    private fun testSingleTimesOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() * Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger.from(a) * HugeInteger.from(b)).toString())
        println("$a * $b = $proper")
    }

    @Test
    fun testTimes() {
        testSingleTimesOperation("3", "7")
        testSingleTimesOperation("31", "72")
        testSingleTimesOperation("31114", "7354622")
    }

    private fun testSingleDivOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() / Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger.from(a) / HugeInteger.from(b)).toString())
        println("$a / $b = $proper")
    }

    @Test
    fun testDiv() {
        testSingleDivOperation("0", "10")
        testSingleDivOperation("10", "2")
        testSingleDivOperation("31114", "1532")
        testSingleDivOperation("111111", "111")
    }

    private fun testSingleRemOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() % Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger.from(a) % HugeInteger.from(b)).toString())
        println("$a % $b = $proper")
    }

    @Test
    fun testRem() {
        testSingleRemOperation("0", "10")
        testSingleRemOperation("10", "2")
        testSingleRemOperation("15", "3")
        testSingleRemOperation("20", "3")
        testSingleRemOperation("10", "2")
        testSingleRemOperation("11", "2")
        testSingleRemOperation("201414", "52352")
    }
}