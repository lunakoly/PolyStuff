import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class HugeIntegerTest {

    @Test
    fun testCreation() {
        HugeInteger()
        HugeInteger("10")
        HugeInteger(10)
    }

    @Test
    fun testToString() {
        assertEquals("0", HugeInteger().toString())
        assertEquals("0", HugeInteger("0").toString())
        assertEquals("136", HugeInteger("136").toString())
        assertEquals("1234567890987654321", HugeInteger("1234567890987654321").toString())
    }

    private fun testSinglePlusOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() + Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger(a) + HugeInteger(b)).toString())
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
        assert(HugeInteger("0") == HugeInteger())
        assert(HugeInteger("10") > HugeInteger("0"))
        assert(HugeInteger("15") < HugeInteger("16"))
        assert(HugeInteger("1600000000") < HugeInteger("1600000001"))
        assert(HugeInteger("1600000000") > HugeInteger("1001"))
        assert(HugeInteger("16000000014124235235240") >= HugeInteger("1001"))
        assert(HugeInteger("16") != HugeInteger("5"))
    }

    private fun testSingleMinusOperation(a: String, b: String) {
        val proper = (Integer.parseInt(a).toLong() - Integer.parseInt(b)).toString()
        assertEquals(proper, (HugeInteger(a) - HugeInteger(b)).toString())
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
}