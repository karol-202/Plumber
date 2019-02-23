package pl.karol202.plumber

import org.junit.Test
import kotlin.test.assertEquals

class PipelineTest
{
	object TransitiveUniLayerIncrement : TransitiveLayer<Int, Int>
	{
		override fun transform(input: Int) = input + 1
	}

	object TransitiveBiLayerDouble : TransitiveBiLayer<Int, Int>
	{
		override fun transform(input: Int) = input * 2

		override fun transformBack(input: Int) = input / 2
	}

	object TransitiveUniLayerDecrement : TransitiveLayerWithFlowControl<Int, Int>
	{
		override fun transformWithFlowControl(input: Int) = Output.Value(input - 1)
	}

	object TransitiveBiLayerIncrementDecrement : TransitiveBiLayer<Int, Int>
	{
		override fun transform(input: Int) = input + 1

		override fun transformBack(input: Int) = input - 1
	}

	object TransitiveBiLayerString : TransitiveBiLayerWithFlowControl<Int, String>
	{
		override fun transformWithFlowControl(input: Int) = Output.Value(input.toString())

		override fun transformBackWithFlowControl(input: String) = input.toIntOrNull()?.let { Output.Value(it) }
				?: Output.NoValue<Int>()
	}

	object CreatorUniLayer : CreatorLayer<Int>
	{
		override fun transform(input: Unit) = 8
	}

	object ConsumerUniLayer : ConsumerLayerWithFlowControl<Int>
	{
		override fun transformWithFlowControl(input: Int) = Output.NoValue<Unit>()
	}

	object TerminalBiLayer : pl.karol202.plumber.TerminalBiLayerWithFlowControl<Int>
	{
		override fun transformWithFlowControl(input: Int) = Output.Value(Unit)

		override fun transformBackWithFlowControl(input: Unit) = Output.Value(100)
	}

	//Open pipelines

	@Test
	fun testOpenUniPipelineFromOneUniLayers()
	{
		val pipeline: OpenUniPipeline<Int, Int> = TransitiveUniLayerIncrement.toOpenUniPipeline()
		assertEquals(Output.Value(50), pipeline.transform(49))
	}

	@Test
	fun testOpenUniPipelineFromTwoUniLayers()
	{
		val pipeline: OpenUniPipeline<Int, Int> = TransitiveUniLayerIncrement + TransitiveUniLayerDecrement
		assertEquals(Output.Value(8), pipeline.transform(8))
	}

	@Test
	fun testUniPipelineFromThreeMixedLayers()
	{
		val pipeline: OpenUniPipeline<Int, String> =
			TransitiveUniLayerIncrement + TransitiveBiLayerDouble + TransitiveBiLayerString
		assertEquals(Output.Value("6"), pipeline.transform(2))
	}

	@Test
	fun testUniPipelineFromTwoBiLayers()
	{
		val pipeline: OpenUniPipeline<Int, Int> =
			(TransitiveBiLayerDouble + TransitiveBiLayerIncrementDecrement).toOpenUniPipeline()
		assertEquals(Output.Value(11), pipeline.transform(5))
	}

	@Test
	fun testUniPipelineInvertedFromTwoBiLayers()
	{
		val pipeline: OpenUniPipeline<Int, Int> = (TransitiveBiLayerDouble + TransitiveBiLayerIncrementDecrement).invert()
				.toOpenUniPipeline()
		assertEquals(Output.Value(5), pipeline.transform(11))
	}

	@Test
	fun testBiPipelineFromOneBiLayer()
	{
		val pipeline: OpenBiPipeline<Int, String> = TransitiveBiLayerString.toOpenBiPipeline()
		assertEquals(Output.Value("7"), pipeline.transform(7))
		assertEquals(Output.Value(9), pipeline.transformBack("9"))
		assertEquals(Output.NoValue(), pipeline.transformBack("a"))
	}

	@Test
	fun testBiPipelineFromTwoBiLayers()
	{
		val pipeline: OpenBiPipeline<Int, Int> = TransitiveBiLayerDouble + TransitiveBiLayerIncrementDecrement
		assertEquals(Output.Value(11), pipeline.transform(5))
		assertEquals(Output.Value(5), pipeline.transformBack(11))
	}

	//Closed pipelines

	@Test
	fun testUniPipelineFromCreatorUniLayer()
	{
		val pipeline: LeftClosedUniPipeline<Int, *> = CreatorUniLayer.toLeftClosedUniPipeline()
		assertEquals(Output.Value(8), pipeline.transform())
	}

	@Test
	fun testUniPipelineFromConsumerUniLayer()
	{
		val pipeline: RightClosedUniPipeline<Int, *> = ConsumerUniLayer.toRightClosedUniPipeline()
		assertEquals(Output.NoValue(), pipeline.transform(0))
	}

	@Test
	fun testUniPipelineFromCreatorUniLayerAndTerminalBiLayer()
	{
		val pipeline: ClosedUniPipeline = CreatorUniLayer + TerminalBiLayer
		assertEquals(Output.Value(Unit), pipeline.transform())
	}

	@Test
	fun testUniPipelineFromTerminalBiLayerAndTransitiveBiLayer()
	{
		val pipeline: LeftClosedUniPipeline<Int, *> =
			(TerminalBiLayer + TransitiveBiLayerDouble).toLeftClosedUniPipeline()
		assertEquals(Output.Value(200), pipeline.transform())
	}

	@Test
	fun testBiPipelineFromTerminalBiLayerAndTransitiveBiLayer()
	{
		val pipeline: LeftClosedBiPipeline<Int, *> = TerminalBiLayer + TransitiveBiLayerDouble
		assertEquals(Output.Value(200), pipeline.transform())
		assertEquals(Output.Value(Unit), pipeline.transformBack(5))
	}

	@Test
	fun testBiPipelineFromTransitiveBiLayerAndTerminalBiLayer()
	{
		val pipeline: RightClosedBiPipeline<Int, *> = TransitiveBiLayerDouble + TerminalBiLayer
		assertEquals(Output.Value(Unit), pipeline.transform(-80))
		assertEquals(Output.Value(50), pipeline.transformBack())
	}
}