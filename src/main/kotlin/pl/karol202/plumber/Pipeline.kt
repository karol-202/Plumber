package pl.karol202.plumber

interface Pipeline<I, O>
{
	fun transformForward(input: I): O

	fun transformBackward(input: O): I
}

class OpenPipeline<I, O>(private val firstElement: PipelineElement<I, *, I, O>,
                         private val lastElement: PipelineElement<*, O, I, O>) :
		Pipeline<I, O>
{
	override fun transformForward(input: I) = firstElement.transformForward(input)

	override fun transformBackward(input: O) = lastElement.transformBackward(input)
}

class LeftClosedPipeline<O>(private val firstElement: FirstPipelineElement<*, O>,
                            private val lastElement: PipelineElement<*, O, Unit, O>) :
		Pipeline<Unit, O>
{
	override fun transformForward(input: Unit) = firstElement.transformForward(input)

	override fun transformBackward(input: O) = lastElement.transformBackward(input)
}

class RightClosedPipeline<I>(private val firstElement: PipelineElement<I, *, I, Unit>,
                             private val lastElement: LastPipelineElement<*, I>) :
		Pipeline<I, Unit>
{
	override fun transformForward(input: I) = firstElement.transformForward(input)

	override fun transformBackward(input: Unit) = lastElement.transformBackward(input)
}

class ClosedPipeline(private val firstElement: FirstPipelineElement<*, Unit>,
                     private val lastElement: LastPipelineElement<*, Unit>) :
		Pipeline<Unit, Unit>
{
	override fun transformForward(input: Unit) = firstElement.transformForward(input)

	override fun transformBackward(input: Unit) = lastElement.transformBackward(input)
}