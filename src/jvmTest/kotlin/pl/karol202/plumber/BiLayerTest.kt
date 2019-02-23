package pl.karol202.plumber

import org.junit.Test
import kotlin.test.assertEquals

class BiLayerTest
{
    class IncrementBiLayer : TransitiveBiLayer<Int, Int>
    {
        override fun transform(input: Int) = input + 1

        override fun transformBack(input: Int) = input - 1
    }

    class IncrementBiLayerWFC : TransitiveBiLayerWithFlowControl<Int, Int>
    {
        override fun transformWithFlowControl(input: Int) = Output.Value(input + 1)

        override fun transformBackWithFlowControl(input: Int) = Output.NoValue<Int>()
    }

    @Test
    fun testLayer()
    {
        val incrementBiLayer = IncrementBiLayer()
        assertEquals(2, incrementBiLayer.transform(1))
        assertEquals(51, incrementBiLayer.transformBack(52))
    }

    @Test
    fun testInvert()
    {
        val incrementBiLayer = IncrementBiLayer().invert()
        assertEquals(0, incrementBiLayer.transform(1))
        assertEquals(53, incrementBiLayer.transformBack(52))
    }

    @Test
    fun testLayerWFC()
    {
        val incrementBiLayer = IncrementBiLayerWFC()
        assertEquals(Output.Value(2), incrementBiLayer.transformWithFlowControl(1))
        assertEquals(Output.NoValue(), incrementBiLayer.transformBackWithFlowControl(52))
    }

    @Test
    fun testInvertWFC()
    {
        val incrementBiLayer = IncrementBiLayerWFC().invert()
        assertEquals(Output.NoValue(), incrementBiLayer.transformWithFlowControl(1))
        assertEquals(Output.Value(53), incrementBiLayer.transformBackWithFlowControl(52))
    }
}
