package pl.karol202.plumber

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LateValTest
{
    private class ExceptionNull : Exception()
    private class ExceptionAssigned : Exception()

    @Test
    fun testSetAndGet()
    {
        var tested by LateVal<Int>({ ExceptionNull() }, { ExceptionAssigned() })
        tested = 42
        assertEquals(42, tested)
    }

    @Test
    fun testOnlyGet()
    {
        val tested by LateVal<Int>({ ExceptionNull() }, { ExceptionAssigned() })
        assertFailsWith<ExceptionNull> { println(tested) }
    }

    @Test
    fun testDoubleSet()
    {
        var tested by LateVal<Int>({ ExceptionNull() }, { ExceptionAssigned() })
        tested = 42
        assertFailsWith<ExceptionAssigned> { tested = 43 }
    }
}
