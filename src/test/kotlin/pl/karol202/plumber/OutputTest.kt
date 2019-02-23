package pl.karol202.plumber

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class OutputTest
{
    @Test
    fun testGetOrNull()
    {
        val value = Output.Value("cat")
        val noValue = Output.NoValue<String>()

        assertEquals("cat", value.getOrNull())
        assertEquals(null, noValue.getOrNull())
    }

    @Test
    fun testFold()
    {
        val value = Output.Value("dog")
        val noValue = Output.NoValue<String>()

        assertEquals("dog", value.fold({ it }, { fail() }))
        assertEquals("no", noValue.fold({ fail() }, { "no" }))
    }
}
