package pl.karol202.plumber

/**
 * UniPipeline is an ordered set of operations transforming data from input to output type.
 * UniPipeline consists of objects implementing BiLayer interface.
 * Each layer transforms data from type being the output type of previous layer (unless it's first layer
 * which input type is the input type of whole pipeline) to type being the input type of next layer (unless it's last layer
 * which output type is the output type of whole pipeline).
 *
 * Every pipeline is either:
 * - OpenUniPipeline - has a MiddleLayers both on the start and on the end of the pipeline, or
 * - LeftClosedUniPipeline - has a FirstBiLayer on the start and a TransitiveBiLayer on the end of the pipeline, or
 * - RightClosedUniPipeline - has a TransitiveBiLayer on the start and a LastBiLayer on the end of the pipeline, or
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
sealed class UniPipeline<I, O>
{
	internal abstract val firstElement: PipelineElementWithSuccessor<I, *, I, O, *, *>
	internal abstract val lastElement: PipelineElementWithPredecessor<*, O, I, O, *, *>

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
class OpenUniPipeline<I, O> internal constructor(override val firstElement: StartPipelineTerminator<I, O, O>,
                                                 override val lastElement: EndPipelineTerminator<I, O, I>) :
		UniPipeline<I, O>(), ConvertibleToOpenUniPipeline<I, O>
{
	companion object
	{
		@PublicApi
		fun <I, O> fromLayer(layer: TransitiveLayer<I, O>): OpenUniPipeline<I, O>
		{
			val endTerminator = EndPipelineTerminator<I, O, I>()
			val middleElement = MiddlePipelineElement(layer, endTerminator)
			val startTerminator = StartPipelineTerminator(middleElement)
			return OpenUniPipeline(startTerminator, endTerminator)
		}

		@PublicApi
		fun <I, O> fromLayer(layer: TransitiveBiLayer<I, O>): OpenUniPipeline<I, O> = fromLayer(object : TransitiveLayer<I, O> {
			override fun transform(input: I) = layer.transform(input)
		})
	}

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenUniPipeline<O, NO>): OpenUniPipeline<I, NO>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return OpenUniPipeline(leftElement.firstElement as StartPipelineTerminator<I, NO, NO>,
							   rightElement.lastElement as EndPipelineTerminator<I, NO, I>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedUniPipeline<O, LEI>): RightClosedUniPipeline<I, LEI>
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<I, I>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return RightClosedUniPipeline(leftElement.firstElement as StartPipelineTerminator<I, Unit, LEI>,
									  rightElement.lastElement as LastPipelineElement<LEI, I, I>)
	}

	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = this
}

/**
 * LeftClosedUniPipeline has a FirstBiLayer on the start and a TransitiveBiLayer on the end of the pipeline.
 * Input type of LeftClosedUniPipeline is Unit.
 *
 * Generic types:
 * - O - output type of pipeline
 * - FEO - output type of first layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class LeftClosedUniPipeline<O, FEO> internal constructor(override val firstElement: FirstPipelineElement<FEO, O, O>,
                                                         override val lastElement: EndPipelineTerminator<Unit, O, FEO>) :
		UniPipeline<Unit, O>(), ConvertibleToLeftClosedUniPipeline<O, FEO>
{
	companion object
	{
		@PublicApi
		fun <O> fromLayer(layer: CreatorLayer<O>): LeftClosedUniPipeline<O, O>
		{
			val endTerminator = EndPipelineTerminator<Unit, O, O>()
			val firstElement = FirstPipelineElement(layer, endTerminator)
			return LeftClosedUniPipeline(firstElement, endTerminator)
		}

		@PublicApi
		fun <T> fromLayer(layer: TerminalBiLayer<T>): LeftClosedUniPipeline<T, T> = fromLayer(object : CreatorLayer<T> {
			override fun transform(input: Unit) = layer.transformBack(input)
		})
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
		return LeftClosedUniPipeline(leftElement.firstElement as FirstPipelineElement<FEO, NO, NO>,
									 rightElement.lastElement as EndPipelineTerminator<Unit, NO, FEO>)
	}

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedUniPipeline<O, LEI>): ClosedUniPipeline
	{
		val rightElement = rightPipeline.firstElement.nextElement.copyWithNewPI<Unit, FEO>()
		val leftElement = lastElement.previousElement.copyBackwardsWithNewPO(rightElement)
		return ClosedUniPipeline(leftElement.firstElement as FirstPipelineElement<FEO, Unit, LEI>,
								 rightElement.lastElement as LastPipelineElement<LEI, Unit, FEO>)
	}

	@PublicApi
	override fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<O, FEO> = this
}


/**
 * RightClosedUniPipeline has a TransitiveBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Output type of RightClosedUniPipeline is Unit.
 *
 * Generic types:
 * - I - input type of pipeline
 * - LEI - input type of last layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class RightClosedUniPipeline<I, LEI> internal constructor(override val firstElement: StartPipelineTerminator<I, Unit, LEI>,
                                                          override val lastElement: LastPipelineElement<LEI, I, I>) :
		UniPipeline<I, Unit>(), ConvertibleToRightClosedUniPipeline<I, LEI>
{
	companion object
	{
		@PublicApi
		fun <I> fromLayer(layer: ConsumerLayer<I>): RightClosedUniPipeline<I, I>
		{
			val lastElement = LastPipelineElement<I, I, I>(layer)
			val startTerminator = StartPipelineTerminator(lastElement)
			return RightClosedUniPipeline(startTerminator, lastElement)
		}

		@PublicApi
		fun <T> fromLayer(layer: TerminalBiLayer<T>): RightClosedUniPipeline<T, T> = fromLayer(object : ConsumerLayer<T> {
			override fun transform(input: T) = layer.transform(input)
		})
	}

	override fun toRightClosedUniPipeline(): RightClosedUniPipeline<I, LEI> = this
}

/**
 * ClosedUniPipeline has a FirstBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Both input type and output type of ClosedUniPipeline is Unit.
 */
@PublicApi
class ClosedUniPipeline internal constructor(override val firstElement: FirstPipelineElement<*, Unit, *>,
                                             override val lastElement: LastPipelineElement<*, Unit, *>) :
		UniPipeline<Unit, Unit>()
