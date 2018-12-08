package pl.karol202.plumber

abstract class Pipeline<I, O>
{
	protected abstract val firstElement: PipelineElementWithSuccessor<I, *, I, O>
	protected abstract val lastElement: PipelineElementWithPredecessor<*, O, I, O>

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
			val element = MiddlePipelineElement(layer, endTerminator)
			val startTerminator = StartPipelineTerminator(element)
			return OpenPipeline(startTerminator, endTerminator)
		}
	}

	fun <SO> plus(next: OpenPipeline<O, SO>): OpenPipeline<I, SO> =
			lastElement.insertAtEndAndReturnBeginning(next.firstElement)
}

class LeftClosedPipeline<O> internal constructor(override val firstElement: FirstPipelineElement<*, O>,
                                                 override val lastElement: EndPipelineTerminator<Unit, O>) :
		Pipeline<Unit, O>(),
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
}

class RightClosedPipeline<I> internal constructor(override val firstElement: StartPipelineTerminator<I, Unit>,
                                                  override val lastElement: LastPipelineElement<*, I>) :
		Pipeline<I, Unit>(),
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