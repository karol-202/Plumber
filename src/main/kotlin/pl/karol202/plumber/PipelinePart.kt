package pl.karol202.plumber

interface RightExpandablePipelinePart<O>
{
    val firstLayer: FirstLayer<*>?
	val middleLayers: List<MiddleLayer<*, *>>

	operator fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<*, *>
}

interface LeftExpandablePipelinePart<I>
{
	val middleLayers: List<MiddleLayer<*, *>>
	val lastLayer: LastLayer<*>?
}