package pl.karol202.plumber

abstract class Pipeline<I, O>(val firstLayer: FirstLayer<*>?,
							  val middleLayers: List<MiddleLayer<*, *>>,
							  val lastLayer: LastLayer<*>?)
{
	companion object
	{
		fun create(firstLayer: FirstLayer<*>?, middleLayers: List<MiddleLayer<*, *>>, lastLayer: LastLayer<*>?) =
				if(firstLayer == null && lastLayer == null) OpenPipeline(middleLayers)
	}

	init
	{
		TODO("check integrity")
	}
}

class OpenPipeline<I, O>(middleLayers: List<MiddleLayer<*, *>>) :
		Pipeline<I, O>(null, middleLayers, null),
	LeftExpandablePipelinePart<I>,
	RightExpandablePipelinePart<O>
{
	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<*, *>
	{
		TODO("not implemented")
	}
}

class LeftClosedPipeline<O>(firstLayer: FirstLayer<*>,
                            middleLayers: List<MiddleLayer<*, *>>) :
		Pipeline<Unit, O>(firstLayer, middleLayers, null),
		RightExpandablePipelinePart<O>
{
	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<*, *>
	{
		TODO("not implemented")
	}
}

class RightClosedPipeline<I>(middleLayers: List<MiddleLayer<*, *>>,
                             lastLayer: LastLayer<*>) :
		Pipeline<I, Unit>(null, middleLayers, lastLayer),
		LeftExpandablePipelinePart<I>

class ClosedPipeline(firstLayer: FirstLayer<*>,
                     middleLayers: List<MiddleLayer<*, *>>,
                     lastLayer: LastLayer<*>) :
		Pipeline<Unit, Unit>(firstLayer, middleLayers, lastLayer)