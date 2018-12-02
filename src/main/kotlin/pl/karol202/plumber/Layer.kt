package pl.karol202.plumber

interface Layer<I, O>
{
	fun transformForward(input: I): O

	fun transformBackward(input: O): I
}

interface FirstLayer<O> : Layer<Unit, O>, RightExpandablePipelinePart<O>
{
	override val firstLayer: FirstLayer<*>?
		get() = this
	override val middleLayers: List<MiddleLayer<*, *>>
		get() = listOf()

	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>) =
			nextPart.lastLayer.let {
				if(it != null) ClosedPipeline(this, middleLayers + nextPart.middleLayers, it)
				else LeftClosedPipeline<>(this, middleLayers + nextPart.middleLayers)
			}
}

interface MiddleLayer<I, O> : Layer<I, O>, LeftExpandablePipelinePart<I>, RightExpandablePipelinePart<O>
{
	override val firstLayer: FirstLayer<*>?
		get() = null
	override val middleLayers: List<MiddleLayer<*, *>>
		get() = listOf(this)
	override val lastLayer: LastLayer<*>?
		get() = null

	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<*, *>
	{
		TODO("not implemented")
	}
}

interface LastLayer<I> : Layer<I, Unit>, LeftExpandablePipelinePart<I>
{
	override val middleLayers: List<MiddleLayer<*, *>>
		get() = listOf()
	override val lastLayer: LastLayer<*>?
		get() = this
}