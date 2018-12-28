package pl.karol202.plumber

/**
 * Pipeline is an ordered set of operations transforming data from input to output type.
 * Pipeline consists of objects implementing Layer interface.
 * Each layer transforms data from type being the output type of previous layer (unless it's first layer
 * which input type is the input type of whole pipeline) to type being the input type of next layer (unless it's last layer
 * which output type is the output type of whole pipeline).
 *
 * Every pipeline is either:
 * - OpenPipeline - has a MiddleLayers both on the start and on the end of the pipeline, or
 * - LeftClosedPipeline - has a FirstLayer on the start and a MiddleLayer on the end of the pipeline, or
 * - RightClosedPipeline - has a MiddleLayer on the start and a LastLayer on the end of the pipeline, or
 * - ClosedPipeline - has a FirstLayer on the start and a LastLayer on the end of the pipeline.
 *
 * Pipelines cannot be created by using constructor but from layer (toPipeline() method)
 * or by joining pipeline with pipeline, pipe
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
sealed class Pipeline<I, O>
{
	internal abstract val firstElement: PipelineElementWithSuccessor<I, *, I, O, *, *>
	internal abstract val lastElement: PipelineElementWithPredecessor<*, O, I, O, *, *>

	/**
	 * Transforms data from input type to output type
	 */
    @PublicApi
	fun transformForward(input: I) = firstElement.transformForward(input)

	/**
	 * Transforms data from output type to input type
	 */
    @PublicApi
	fun transformBackward(input: O) = lastElement.transformBackward(input)
}

/**
 * OpenPipeline has MiddleLayers both on the start and on the end of the pipeline.
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
class OpenPipeline<I, O> internal constructor(override val firstElement: StartPipelineTerminator<I, O, O>,
											  override val lastElement: EndPipelineTerminator<I, O, I>) :
		Pipeline<I, O>()
{
	companion object
	{
		internal fun <I, O> fromLayer(layer: MiddleLayer<I, O>): OpenPipeline<I, O>
		{
			val endTerminator = EndPipelineTerminator<I, O, I>()
			val middleElement = MiddlePipelineElement(layer, endTerminator)
			val startTerminator = StartPipelineTerminator(middleElement)
			return OpenPipeline(startTerminator, endTerminator)
		}
	}

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>): OpenPipeline<I, NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return OpenPipeline(leftElement.firstElement as StartPipelineTerminator<I, NO, NO>,
							rightElement.lastElement as EndPipelineTerminator<I, NO, I>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedPipeline<O, LEI>): RightClosedPipeline<I, LEI>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return RightClosedPipeline(leftElement.firstElement as StartPipelineTerminator<I, Unit, LEI>,
								   rightElement.lastElement as LastPipelineElement<LEI, I, I>)
	}

	@PublicApi
	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>) = this + rightLayer.toPipeline()

	@PublicApi
	operator fun plus(rightLayer: LastLayer<O>) = this + rightLayer.toPipeline()
}

/**
 * LeftClosedPipeline has a FirstLayer on the start and a MiddleLayer on the end of the pipeline.
 * Input type of LeftClosedPipeline is Unit.
 *
 * Generic types:
 * - O - output type of pipeline
 * - FEO - output type of first layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class LeftClosedPipeline<O, FEO> internal constructor(override val firstElement: FirstPipelineElement<FEO, O, O>,
                                                      override val lastElement: EndPipelineTerminator<Unit, O, FEO>) :
		Pipeline<Unit, O>()
{
	companion object
	{
		internal fun <O> fromLayer(layer: FirstLayer<O>): LeftClosedPipeline<O, O>
		{
			val endTerminator = EndPipelineTerminator<Unit, O, O>()
			val firstElement = FirstPipelineElement(layer, endTerminator)
			return LeftClosedPipeline(firstElement, endTerminator)
		}
	}

	/**
	 * Shorthand for transformForward(Unit)
	 */
	@PublicApi
	fun transformForward(): O = transformForward(Unit)

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>): LeftClosedPipeline<NO, FEO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return LeftClosedPipeline(leftElement.firstElement as FirstPipelineElement<FEO, NO, NO>,
								  rightElement.lastElement as EndPipelineTerminator<Unit, NO, FEO>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedPipeline<O, LEI>): ClosedPipeline
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return ClosedPipeline(leftElement.firstElement as FirstPipelineElement<FEO, Unit, LEI>,
							  rightElement.lastElement as LastPipelineElement<LEI, Unit, FEO>)
	}

	@PublicApi
	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>) = this + rightLayer.toPipeline()

	@PublicApi
	operator fun plus(rightLayer: LastLayer<O>) = this + rightLayer.toPipeline()
}


/**
 * RightClosedPipeline has a MiddleLayer on the start and a LastLayer on the end of the pipeline.
 * Output type of RightClosedPipeline is Unit.
 *
 * Generic types:
 * - I - input type of pipeline
 * - LEI - input type of last layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class RightClosedPipeline<I, LEI> internal constructor(override val firstElement: StartPipelineTerminator<I, Unit, LEI>,
                                                       override val lastElement: LastPipelineElement<LEI, I, I>) :
		Pipeline<I, Unit>()
{
	companion object
	{
		internal fun <I> fromLayer(layer: LastLayer<I>): RightClosedPipeline<I, I>
		{
			val lastElement = LastPipelineElement<I, I, I>(layer)
			val startTerminator = StartPipelineTerminator(lastElement)
			return RightClosedPipeline(startTerminator, lastElement)
		}
	}

	/**
	 * Shorthand for transformBackward(Unit)
	 */
	@PublicApi
	fun transformBackward(): I = transformBackward(Unit)
}

/**
 * ClosedPipeline has a FirstLayer on the start and a LastLayer on the end of the pipeline.
 * Both input type and output type of ClosedPipeline is Unit.
 */
@PublicApi
class ClosedPipeline internal constructor(override val firstElement: FirstPipelineElement<*, Unit, *>,
										  override val lastElement: LastPipelineElement<*, Unit, *>) :
		Pipeline<Unit, Unit>()
