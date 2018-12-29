package pl.karol202.plumber.unidirectional

import pl.karol202.plumber.PublicApi

/**
 * UniPipeline is an ordered set of operations transforming data from input to output type.
 * UniPipeline consists of objects implementing BiLayer interface.
 * Each layer transforms data from type being the output type of previous layer (unless it's first layer
 * which input type is the input type of whole pipeline) to type being the input type of next layer (unless it's last layer
 * which output type is the output type of whole pipeline).
 *
 * Every pipeline is either:
 * - OpenUniPipeline - has a MiddleLayers both on the start and on the end of the pipeline, or
 * - LeftClosedUniPipeline - has a FirstBiLayer on the start and a MiddleBiLayer on the end of the pipeline, or
 * - RightClosedUniPipeline - has a MiddleBiLayer on the start and a LastBiLayer on the end of the pipeline, or
 * - ClosedUniPipeline - has a FirstBiLayer on the start and a LastBiLayer on the end of the pipeline.
 *
 * Pipelines cannot be created by using constructor but from layer (toPipeline() method)
 * or by joining pipeline with pipeline, pipe
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
abstract class UniPipeline<I, O>
{
	internal abstract val firstElement: UniPipelineElementWithSuccessor<I, *, I, O, *, *>
	internal abstract val lastElement: UniPipelineElementWithPredecessor<*, O, I, O, *, *>

	/**
	 * Transforms data from input type to output type
	 */
    @PublicApi
	fun transform(input: I): O = firstElement.transform(input)
}

/**
 * OpenUniPipeline has MiddleLayers both on the start and on the end of the pipeline.
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
class OpenUniPipeline<I, O> internal constructor(override val firstElement: StartUniPipelineTerminator<I, O, O>,
                                                 override val lastElement: EndUniPipelineTerminator<I, O, I>) :
		UniPipeline<I, O>()
{
	companion object
	{
		fun <I, O> fromLayer(layer: MiddleLayer<I, O>): OpenUniPipeline<I, O>
		{
			val endTerminator = EndUniPipelineTerminator<I, O, I>()
			val middleElement = MiddleUniPipelineElement(layer, endTerminator)
			val startTerminator = StartUniPipelineTerminator(middleElement)
			return OpenUniPipeline(startTerminator, endTerminator)
		}
	}

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenUniPipeline<O, NO>): OpenUniPipeline<I, NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return OpenUniPipeline(leftElement.firstElement as StartUniPipelineTerminator<I, NO, NO>, rightElement.lastElement as EndUniPipelineTerminator<I, NO, I>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedUniPipeline<O, LEI>): RightClosedUniPipeline<I, LEI>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return RightClosedUniPipeline(leftElement.firstElement as StartUniPipelineTerminator<I, Unit, LEI>, rightElement.lastElement as LastUniPipelineElement<LEI, I, I>)
	}

	@PublicApi
	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>): OpenUniPipeline<I, NO> = this + OpenUniPipeline.fromLayer(rightLayer)

	@PublicApi
	operator fun plus(rightLayer: LastLayer<O>): RightClosedUniPipeline<I, O> = this + RightClosedUniPipeline.fromLayer(rightLayer)
}

/**
 * LeftClosedUniPipeline has a FirstBiLayer on the start and a MiddleBiLayer on the end of the pipeline.
 * Input type of LeftClosedUniPipeline is Unit.
 *
 * Generic types:
 * - O - output type of pipeline
 * - FEO - output type of first layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class LeftClosedUniPipeline<O, FEO> internal constructor(override val firstElement: FirstUniPipelineElement<FEO, O, O>,
                                                         override val lastElement: EndUniPipelineTerminator<Unit, O, FEO>) :
		UniPipeline<Unit, O>()
{
	companion object
	{
		fun <O> fromLayer(layer: FirstLayer<O>): LeftClosedUniPipeline<O, O>
		{
			val endTerminator = EndUniPipelineTerminator<Unit, O, O>()
			val firstElement = FirstUniPipelineElement(layer, endTerminator)
			return LeftClosedUniPipeline(firstElement, endTerminator)
		}
	}

	/**
	 * Shorthand for transformForward(Unit)
	 */
	@PublicApi
	fun transform(): O = transform(Unit)

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenUniPipeline<O, NO>): LeftClosedUniPipeline<NO, FEO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return LeftClosedUniPipeline(leftElement.firstElement as FirstUniPipelineElement<FEO, NO, NO>, rightElement.lastElement as EndUniPipelineTerminator<Unit, NO, FEO>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedUniPipeline<O, LEI>): ClosedUniPipeline
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return ClosedUniPipeline(leftElement.firstElement as FirstUniPipelineElement<FEO, Unit, LEI>, rightElement.lastElement as LastUniPipelineElement<LEI, Unit, FEO>)
	}

	@PublicApi
	operator fun <NO> plus(rightLayer: MiddleLayer<O, NO>): LeftClosedUniPipeline<NO, FEO> = this + OpenUniPipeline.fromLayer(rightLayer)

	@PublicApi
	operator fun plus(rightLayer: LastLayer<O>): ClosedUniPipeline = this + RightClosedUniPipeline.fromLayer(rightLayer)
}


/**
 * RightClosedUniPipeline has a MiddleBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Output type of RightClosedUniPipeline is Unit.
 *
 * Generic types:
 * - I - input type of pipeline
 * - LEI - input type of last layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class RightClosedUniPipeline<I, LEI> internal constructor(override val firstElement: StartUniPipelineTerminator<I, Unit, LEI>,
                                                          override val lastElement: LastUniPipelineElement<LEI, I, I>) :
		UniPipeline<I, Unit>()
{
	companion object
	{
		fun <I> fromLayer(layer: LastLayer<I>): RightClosedUniPipeline<I, I>
		{
			val lastElement = LastUniPipelineElement<I, I, I>(layer)
			val startTerminator = StartUniPipelineTerminator(lastElement)
			return RightClosedUniPipeline(startTerminator, lastElement)
		}
	}
}

/**
 * ClosedUniPipeline has a FirstBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Both input type and output type of ClosedUniPipeline is Unit.
 */
@PublicApi
class ClosedUniPipeline internal constructor(override val firstElement: FirstUniPipelineElement<*, Unit, *>,
                                             override val lastElement: LastUniPipelineElement<*, Unit, *>) :
		UniPipeline<Unit, Unit>()
