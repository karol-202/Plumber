package pl.karol202.plumber

abstract class Pipeline<I, O> internal constructor(anyElement: PipelineElement<*, *, I, O>)
{
	private val firstElement = anyElement.firstElement
	private val lastElement = anyElement.lastElement

	fun transformForward(input: I) = firstElement.transformForward(input)

	fun transformBackward(input: O) = lastElement.transformBackward(input)
}

class OpenPipeline<I, O> internal constructor(anyElement: PipelineElement<*, *, I, O>) :
		Pipeline<I, O>(anyElement)

class LeftClosedPipeline<O> internal constructor(anyElement: PipelineElement<*, *, Unit, O>) :
		Pipeline<Unit, O>(anyElement)

class RightClosedPipeline<I> internal constructor(anyElement: PipelineElement<*, *, I, Unit>) :
		Pipeline<I, Unit>(anyElement)

class ClosedPipeline internal constructor(anyElement: PipelineElement<*, *, Unit, Unit>) :
		Pipeline<Unit, Unit>(anyElement)