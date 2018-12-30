package pl.karol202.plumber

/**
 * Pipeline is an ordered set of operations transforming data from input to output type.
 * Pipeline consists of objects implementing BiLayer interface.
 * Each layer transforms data from type being the output type of previous layer (unless it's first layer
 * which input type is the input type of whole pipeline) to type being the input type of next layer (unless it's last layer
 * which output type is the output type of whole pipeline).
 *
 * Every pipeline is either:
 * - OpenPipeline - has a MiddleLayers both on the start and on the end of the pipeline, or
 * - LeftClosedPipeline - has a FirstBiLayer on the start and a TransitiveBiLayer on the end of the pipeline, or
 * - RightClosedPipeline - has a TransitiveBiLayer on the start and a LastBiLayer on the end of the pipeline, or
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
sealed class BiPipeline<I, O>
{
	internal abstract val forwardUniPipeline: UniPipeline<I, O>
	internal abstract val backwardUniPipeline: UniPipeline<O, I>

	/**
	 * Transforms data from input type to output type
	 */
	@PublicApi
	fun transform(input: I): O = forwardUniPipeline.transform(input)

	@PublicApi
	fun transformBack(input: O): I = backwardUniPipeline.transform(input)
}

/**
 * OpenPipeline has MiddleLayers both on the start and on the end of the pipeline.
 *
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
class OpenBiPipeline<I, O> internal constructor(override val forwardUniPipeline: OpenUniPipeline<I, O>,
                                                override val backwardUniPipeline: OpenUniPipeline<O, I>) :
		BiPipeline<I, O>(), ConvertibleToOpenBiPipeline<I, O>
{
	companion object
	{
		@PublicApi
		fun <I, O> fromUniPipelines(forwardUniPipeline: OpenUniPipeline<I, O>,
		                            backwardUniPipeline: OpenUniPipeline<O, I>): OpenBiPipeline<I, O> =
				OpenBiPipeline(forwardUniPipeline, backwardUniPipeline)

		@PublicApi
		fun <I, O> fromLayer(layer: TransitiveBiLayer<I, O>): OpenBiPipeline<I, O>
		{
			val forwardUniPipeline = OpenUniPipeline.fromLayer(object : TransitiveLayer<I, O> {
				override fun transform(input: I) = layer.transform(input)
			})
			val backwardUniPipeline = OpenUniPipeline.fromLayer(object : TransitiveLayer<O, I> {
				override fun transform(input: O) = layer.transformBack(input)
			})
			return fromUniPipelines(forwardUniPipeline, backwardUniPipeline)
		}
	}

	/**
	 * Following operator methods join two pipelines into a new one
	 */
	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): OpenBiPipeline<I, NO> = fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline, rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): RightClosedBiPipeline<I, LEI> = RightClosedBiPipeline.fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline, rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	override fun toOpenBiPipeline(): OpenBiPipeline<I, O> = this
}

/**
 * LeftClosedPipeline has a FirstBiLayer on the start and a TransitiveBiLayer on the end of the pipeline.
 * Input type of LeftClosedPipeline is Unit.
 *
 * Generic types:
 * - O - output type of pipeline
 * - FEO - output type of first layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class LeftClosedBiPipeline<O, FEO> internal constructor(override val forwardUniPipeline: LeftClosedUniPipeline<O, FEO>,
                                                        override val backwardUniPipeline: RightClosedUniPipeline<O, FEO>) :
		BiPipeline<Unit, O>(), ConvertibleToLeftClosedBiPipeline<O, FEO>
{
	companion object
	{
		@PublicApi
		fun <O, FEO> fromUniPipelines(forwardUniPipeline: LeftClosedUniPipeline<O, FEO>,
		                              backwardUniPipeline: RightClosedUniPipeline<O, FEO>): LeftClosedBiPipeline<O, FEO> =
				LeftClosedBiPipeline(forwardUniPipeline, backwardUniPipeline)

		@PublicApi
		fun <T> fromLayer(layer: TerminalBiLayer<T>): LeftClosedBiPipeline<T, T>
		{
			val forwardUniPipeline = LeftClosedUniPipeline.fromLayer(object : CreatorLayer<T> {
				override fun transform(input: Unit) = layer.transformBack(input)
			})
			val backwardUniPipeline = RightClosedUniPipeline.fromLayer(object : ConsumerLayer<T> {
				override fun transform(input: T) = layer.transform(input)
			})
			return fromUniPipelines(forwardUniPipeline, backwardUniPipeline)
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
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): LeftClosedBiPipeline<NO, FEO> =
			fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
							rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): ClosedBiPipeline =
			ClosedBiPipeline.fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
											 rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	override fun toLeftClosedBiPipeline(): LeftClosedBiPipeline<O, FEO> = this
}


/**
 * RightClosedPipeline has a TransitiveBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Output type of RightClosedPipeline is Unit.
 *
 * Generic types:
 * - I - input type of pipeline
 * - LEI - input type of last layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class RightClosedBiPipeline<I, LEI> internal constructor(override val forwardUniPipeline: RightClosedUniPipeline<I, LEI>,
                                                         override val backwardUniPipeline: LeftClosedUniPipeline<I, LEI>) :
		BiPipeline<I, Unit>(), ConvertibleToRightClosedBiPipeline<I, LEI>
{
	companion object
	{
		@PublicApi
		fun <I, LEI> fromUniPipelines(forwardUniPipeline: RightClosedUniPipeline<I, LEI>,
		                              backwardUniPipeline: LeftClosedUniPipeline<I, LEI>): RightClosedBiPipeline<I, LEI> =
				RightClosedBiPipeline(forwardUniPipeline, backwardUniPipeline)

		@PublicApi
		fun <T> fromLayer(layer: TerminalBiLayer<T>): RightClosedBiPipeline<T, T>
		{
			val forwardUniPipeline = RightClosedUniPipeline.fromLayer(object : ConsumerLayer<T> {
				override fun transform(input: T) = layer.transform(input)
			})
			val backwardUniPipeline = LeftClosedUniPipeline.fromLayer(object : CreatorLayer<T> {
				override fun transform(input: Unit) = layer.transformBack(input)
			})
			return fromUniPipelines(forwardUniPipeline, backwardUniPipeline)
		}
	}

	/**
	 * Shorthand for transformBackward(Unit)
	 */
	@PublicApi
	fun transformBack(): I = transformBack(Unit)

	@PublicApi
	override fun toRightClosedBiPipeline(): RightClosedBiPipeline<I, LEI> = this
}

/**
 * ClosedPipeline has a FirstBiLayer on the start and a LastBiLayer on the end of the pipeline.
 * Both input type and output type of ClosedPipeline is Unit.
 */
@PublicApi
class ClosedBiPipeline internal constructor(override val forwardUniPipeline: ClosedUniPipeline,
                                            override val backwardUniPipeline: ClosedUniPipeline) :
		BiPipeline<Unit, Unit>()
{
	companion object
	{
		@PublicApi
		fun fromUniPipelines(forwardUniPipeline: ClosedUniPipeline,
		                     backwardUniPipeline: ClosedUniPipeline): ClosedBiPipeline = ClosedBiPipeline(forwardUniPipeline, backwardUniPipeline)
	}
}
