package pl.karol202.plumber.bidirectional

import pl.karol202.plumber.PublicApi

/**
 * Pipeline is an ordered set of operations transforming data from input to output type.
 * Pipeline consists of objects implementing BiLayer interface.
 * Each layer transforms data from type being the output type of previous layer (unless it's first layer
 * which input type is the input type of whole pipeline) to type being the input type of next layer (unless it's last layer
 * which output type is the output type of whole pipeline).
 *
 * Every pipeline is either:
 * - OpenPipeline - has a MiddleLayers both on the start and on the end of the pipeline, or
 * - LeftClosedPipeline - has a FirstBiLayer on the start and a MiddleBiLayer on the end of the pipeline, or
 * - RightClosedPipeline - has a MiddleBiLayer on the start and a LastBiLayer on the end of the pipeline, or
 * - ClosedPipeline - has a FirstBiLayer on the start and a LastBiLayer on the end of the pipeline.
 *
 * Pipelines cannot be created by using constructor but from layer (toPipeline() method)
 * or by joining pipeline with pipeline, pipe
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
abstract class BiPipeline<I, O>
{
	internal abstract val firstElement: BiPipelineElementWithSuccessor<I, *, I, O, *, *>
	internal abstract val lastElement: BiPipelineElementWithPredecessor<*, O, I, O, *, *>

	/**
	 * Transforms data from input type to output type
	 */
	@PublicApi
	fun transform(input: I): O = firstElement.transform(input)

	@PublicApi
	fun transformBack(input: O): I = lastElement.transformBack(input)
}

/**
 * OpenPipeline has MiddleLayers both on the start and on the end of the pipeline.
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
class OpenBiPipeline<I, O> internal constructor(override val firstElement: StartBiPipelineTerminator<I, O, O>,
                                                override val lastElement: EndBiPipelineTerminator<I, O, I>) :
		BiPipeline<I, O>()
{
	companion object
	{
		fun <I, O> fromLayer(layer: MiddleBiLayer<I, O>): OpenBiPipeline<I, O>
		{
			val endTerminator = EndBiPipelineTerminator<I, O, I>()
			val middleElement = MiddleBiPipelineElement(layer, endTerminator)
			val startTerminator = StartBiPipelineTerminator(middleElement)
			return OpenBiPipeline(startTerminator, endTerminator)
		}
	}

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): OpenBiPipeline<I, NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return OpenBiPipeline(leftElement.firstElement as StartBiPipelineTerminator<I, NO, NO>, rightElement.lastElement as EndBiPipelineTerminator<I, NO, I>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): RightClosedBiPipeline<I, LEI>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return RightClosedBiPipeline(leftElement.firstElement as StartBiPipelineTerminator<I, Unit, LEI>, rightElement.lastElement as LastBiPipelineElement<LEI, I, I>)
	}

	@PublicApi
	operator fun <NO> plus(rightLayer: MiddleBiLayer<O, NO>): OpenBiPipeline<I, NO> = this + OpenBiPipeline.fromLayer(rightLayer)

	@PublicApi
	operator fun plus(rightLayer: LastBiLayer<O>): RightClosedBiPipeline<I, O> = this + RightClosedBiPipeline.fromLayer(rightLayer)
}

/**
 * LeftClosedPipeline has a FirstBiLayer on the start and a MiddleBiLayer on the end of the pipeline.
 * Input type of LeftClosedPipeline is Unit.
 *
 * Generic types:
 * - O - output type of pipeline
 * - FEO - output type of first layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class LeftClosedBiPipeline<O, FEO> internal constructor(override val firstElement: FirstBiPipelineElement<FEO, O, O>,
                                                        override val lastElement: EndBiPipelineTerminator<Unit, O, FEO>) :
		BiPipeline<Unit, O>()
{
	companion object
	{
		fun <O> fromLayer(layer: FirstBiLayer<O>): LeftClosedBiPipeline<O, O>
		{
			val endTerminator = EndBiPipelineTerminator<Unit, O, O>()
			val firstElement = FirstBiPipelineElement(layer, endTerminator)
			return LeftClosedBiPipeline(firstElement, endTerminator)
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
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): LeftClosedBiPipeline<NO, FEO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return LeftClosedBiPipeline(leftElement.firstElement as FirstBiPipelineElement<FEO, NO, NO>, rightElement.lastElement as EndBiPipelineTerminator<Unit, NO, FEO>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): ClosedBiPipeline
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return ClosedBiPipeline(leftElement.firstElement as FirstBiPipelineElement<FEO, Unit, LEI>, rightElement.lastElement as LastBiPipelineElement<LEI, Unit, FEO>)
	}

	@PublicApi
	operator fun <NO> plus(rightLayer: MiddleBiLayer<O, NO>): LeftClosedBiPipeline<NO, FEO> = this + OpenBiPipeline.fromLayer(rightLayer)

	@PublicApi
	operator fun plus(rightLayer: LastBiLayer<O>): ClosedBiPipeline = this + RightClosedBiPipeline.fromLayer(rightLayer)
}


/**
 * RightClosedPipeline has a MiddleBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Output type of RightClosedPipeline is Unit.
 *
 * Generic types:
 * - I - input type of pipeline
 * - LEI - input type of last layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class RightClosedBiPipeline<I, LEI> internal constructor(override val firstElement: StartBiPipelineTerminator<I, Unit, LEI>,
                                                         override val lastElement: LastBiPipelineElement<LEI, I, I>) :
		BiPipeline<I, Unit>()
{
	companion object
	{
		fun <I> fromLayer(layer: LastBiLayer<I>): RightClosedBiPipeline<I, I>
		{
			val lastElement = LastBiPipelineElement<I, I, I>(layer)
			val startTerminator = StartBiPipelineTerminator(lastElement)
			return RightClosedBiPipeline(startTerminator, lastElement)
		}
	}

	/**
	 * Shorthand for transformBackward(Unit)
	 */
	@PublicApi
	fun transformBack(): I = transformBack(Unit)
}

/**
 * ClosedPipeline has a FirstBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Both input type and output type of ClosedPipeline is Unit.
 */
@PublicApi
class ClosedBiPipeline internal constructor(override val firstElement: FirstBiPipelineElement<*, Unit, *>,
                                            override val lastElement: LastBiPipelineElement<*, Unit, *>) :
		BiPipeline<Unit, Unit>()
