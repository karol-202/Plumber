package pl.karol202.plumber

interface Layer<I, O>
{
	fun transformForward(input: I): O

	fun transformBackward(input: O): I
}

interface FirstLayer<O> : Layer<Unit, O>, RightExpandablePipelinePart<O>
{
	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<I, O>
	{
		TODO("not implemented")
	}
}

interface MiddleLayer<I, O> : Layer<I, O>, LeftExpandablePipelinePart<I>, RightExpandablePipelinePart<O>

interface LastLayer<I> : Layer<I, Unit>, LeftExpandablePipelinePart<I>