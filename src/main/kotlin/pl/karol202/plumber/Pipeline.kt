package pl.karol202.plumber

abstract class Pipeline<I, O>
{
	internal abstract val firstElement: PipelineElementWithSuccessor<I, *, I, O>
	internal abstract val lastElement: PipelineElementWithPredecessor<*, O, I, O>

	fun transformForward(input: I) = firstElement.transformForward(input)

	fun transformBackward(input: O) = lastElement.transformBackward(input)
}

class OpenPipeline<I, O> internal constructor(override val firstElement: StartPipelineTerminator<I, O>,
											  override val lastElement: EndPipelineTerminator<I, O>) :
		Pipeline<I, O>()
{
	companion object
	{
		fun <I, O> fromLayer(layer: MiddleLayer<I, O>): OpenPipeline<I, O>
		{
			val endTerminator = EndPipelineTerminator<I, O>()
			val middleElement = MiddlePipelineElement(layer, endTerminator)
			val startTerminator = StartPipelineTerminator(middleElement)
			return OpenPipeline(startTerminator, endTerminator)
		}
	}

	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>): OpenPipeline<I, NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return OpenPipeline(leftElement.firstElement as StartPipelineTerminator<I, NO>,
							rightElement.lastElement as EndPipelineTerminator<I, NO>)
	}

	operator fun plus(rightPipeline: RightClosedPipeline<O>): RightClosedPipeline<I>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return RightClosedPipeline(leftElement.firstElement as StartPipelineTerminator<I, Unit>,
								   rightElement.lastElement as LastPipelineElement<*, I>)
	}

	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>) = this + rightLayer.toPipeline()

	operator fun plus(rightLayer: LastLayer<O>) = this + rightLayer.toPipeline()
}

class LeftClosedPipeline<O> internal constructor(override val firstElement: FirstPipelineElement<*, O>,
												 override val lastElement: EndPipelineTerminator<Unit, O>) :
		Pipeline<Unit, O>()
{
	companion object
	{
		fun <O> fromLayer(layer: FirstLayer<O>): LeftClosedPipeline<O>
		{
			val endTerminator = EndPipelineTerminator<Unit, O>()
			val firstElement = FirstPipelineElement(layer, endTerminator)
			return LeftClosedPipeline(firstElement, endTerminator)
		}
	}

	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>): LeftClosedPipeline<NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return LeftClosedPipeline(leftElement.firstElement as FirstPipelineElement<*, NO>,
								  rightElement.lastElement as EndPipelineTerminator<Unit, NO>)
	}

	operator fun plus(rightPipeline: RightClosedPipeline<O>): ClosedPipeline
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return ClosedPipeline(leftElement.firstElement as FirstPipelineElement<*, Unit>,
							  rightElement.lastElement as LastPipelineElement<*, Unit>)
	}

	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>) = this + rightLayer.toPipeline()

	operator fun plus(rightLayer: LastLayer<O>) = this + rightLayer.toPipeline()
}

class RightClosedPipeline<I> internal constructor(override val firstElement: StartPipelineTerminator<I, Unit>,
												  override val lastElement: LastPipelineElement<*, I>) :
		Pipeline<I, Unit>()
{
	companion object
	{
		fun <I> fromLayer(layer: LastLayer<I>): RightClosedPipeline<I>
		{
			val lastElement = LastPipelineElement<I, I>(layer)
			val startTerminator = StartPipelineTerminator(lastElement)
			return RightClosedPipeline(startTerminator, lastElement)
		}
	}
}

class ClosedPipeline internal constructor(override val firstElement: FirstPipelineElement<*, Unit>,
										  override val lastElement: LastPipelineElement<*, Unit>) :
		Pipeline<Unit, Unit>()
