package pl.karol202.plumber

sealed class Pipeline<I, O>
{
	internal abstract val firstElement: PipelineElementWithSuccessor<I, *, I, O, *, *>
	internal abstract val lastElement: PipelineElementWithPredecessor<*, O, I, O, *, *>

	fun transformForward(input: I) = firstElement.transformForward(input)

	fun transformBackward(input: O) = lastElement.transformBackward(input)
}

class OpenPipeline<I, O> internal constructor(override val firstElement: StartPipelineTerminator<I, O, O>,
											  override val lastElement: EndPipelineTerminator<I, O, I>) :
		Pipeline<I, O>()
{
	companion object
	{
		fun <I, O> fromLayer(layer: MiddleLayer<I, O>): OpenPipeline<I, O>
		{
			val endTerminator = EndPipelineTerminator<I, O, I>()
			val middleElement = MiddlePipelineElement(layer, endTerminator)
			val startTerminator = StartPipelineTerminator(middleElement)
			return OpenPipeline(startTerminator, endTerminator)
		}
	}

	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>): OpenPipeline<I, NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return OpenPipeline(leftElement.firstElement as StartPipelineTerminator<I, NO, NO>,
							rightElement.lastElement as EndPipelineTerminator<I, NO, I>)
	}

	operator fun <LEI> plus(rightPipeline: RightClosedPipeline<O, LEI>): RightClosedPipeline<I, LEI>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return RightClosedPipeline(leftElement.firstElement as StartPipelineTerminator<I, Unit, LEI>,
								   rightElement.lastElement as LastPipelineElement<LEI, I, I>)
	}

	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>) = this + rightLayer.toPipeline()

	operator fun plus(rightLayer: LastLayer<O>) = this + rightLayer.toPipeline()
}

class LeftClosedPipeline<O, FEO> internal constructor(override val firstElement: FirstPipelineElement<FEO, O, O>,
                                                      override val lastElement: EndPipelineTerminator<Unit, O, FEO>) :
		Pipeline<Unit, O>()
{
	companion object
	{
		fun <O> fromLayer(layer: FirstLayer<O>): LeftClosedPipeline<O, O>
		{
			val endTerminator = EndPipelineTerminator<Unit, O, O>()
			val firstElement = FirstPipelineElement(layer, endTerminator)
			return LeftClosedPipeline(firstElement, endTerminator)
		}
	}

	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>): LeftClosedPipeline<NO, FEO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return LeftClosedPipeline(leftElement.firstElement as FirstPipelineElement<FEO, NO, NO>,
								  rightElement.lastElement as EndPipelineTerminator<Unit, NO, FEO>)
	}

	operator fun <LEI> plus(rightPipeline: RightClosedPipeline<O, LEI>): ClosedPipeline
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return ClosedPipeline(leftElement.firstElement as FirstPipelineElement<FEO, Unit, LEI>,
							  rightElement.lastElement as LastPipelineElement<LEI, Unit, FEO>)
	}

	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>) = this + rightLayer.toPipeline()

	operator fun plus(rightLayer: LastLayer<O>) = this + rightLayer.toPipeline()
}

class RightClosedPipeline<I, LEI> internal constructor(override val firstElement: StartPipelineTerminator<I, Unit, LEI>,
                                                       override val lastElement: LastPipelineElement<LEI, I, I>) :
		Pipeline<I, Unit>()
{
	companion object
	{
		fun <I> fromLayer(layer: LastLayer<I>): RightClosedPipeline<I, I>
		{
			val lastElement = LastPipelineElement<I, I, I>(layer)
			val startTerminator = StartPipelineTerminator(lastElement)
			return RightClosedPipeline(startTerminator, lastElement)
		}
	}
}

class ClosedPipeline internal constructor(override val firstElement: FirstPipelineElement<*, Unit, *>,
										  override val lastElement: LastPipelineElement<*, Unit, *>) :
		Pipeline<Unit, Unit>()
