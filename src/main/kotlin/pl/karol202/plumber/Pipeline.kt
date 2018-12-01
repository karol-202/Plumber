package pl.karol202.plumber

abstract class Pipeline<I, O>(private val firstLayer: FirstLayer<Any>?,
							  private val middleLayers: List<MiddleLayer<Any, Any>>,
							  private val lastLayer: LastLayer<Any>?)

class OpenPipeline<I, O>(middleLayers: List<MiddleLayer<Any, Any>>) :
		Pipeline<I, O>(null, middleLayers, null),
	LeftExpandablePipelinePart<I>,
	RightExpandablePipelinePart<O>
{
	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<I, O>
	{
		TODO("not implemented")
	}
}

class LeftClosedPipeline<O>(firstLayer: FirstLayer<Any>,
                            middleLayers: List<MiddleLayer<Any, Any>>) :
		Pipeline<Unit, O>(firstLayer, middleLayers, null),
		RightExpandablePipelinePart<O>
{
	override fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<I, O>
	{
		TODO("not implemented")
	}
}

class RightClosedPipeline<I>(middleLayers: List<MiddleLayer<Any, Any>>,
                             lastLayer: LastLayer<Any>) :
		Pipeline<I, Unit>(null, middleLayers, lastLayer),
		LeftExpandablePipelinePart<I>

class ClosedPipeline(firstLayer: FirstLayer<Any>,
                     middleLayers: List<MiddleLayer<Any, Any>>,
                     lastLayer: LastLayer<Any>) :
		Pipeline<Unit, Unit>(firstLayer, middleLayers, lastLayer)