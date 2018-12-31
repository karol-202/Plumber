package pl.karol202.plumber

/**
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
sealed class BiPipeline<I, O>
{
	internal abstract val forwardUniPipeline: UniPipeline<I, O>
	internal abstract val backwardUniPipeline: UniPipeline<O, I>

	@PublicApi
	fun transform(input: I): Output<O> = forwardUniPipeline.transform(input)

	@PublicApi
	fun transformBack(input: O): Output<I> = backwardUniPipeline.transform(input)

	@PublicApi
	abstract fun invert(): BiPipeline<O, I>
}

/**
 * Generic types:
 * - I - input type of pipeline
 * - O - output type of pipeline
 */
@PublicApi
class OpenBiPipeline<I, O> internal constructor(override val forwardUniPipeline: OpenUniPipeline<I, O>,
                                                override val backwardUniPipeline: OpenUniPipeline<O, I>) :
		BiPipeline<I, O>(), ConvertibleToOpenUniPipeline<I, O>, ConvertibleToOpenBiPipeline<I, O>
{
	companion object
	{
		@PublicApi
		fun <I, O> fromUniPipelines(forwardUniPipeline: OpenUniPipeline<I, O>,
		                            backwardUniPipeline: OpenUniPipeline<O, I>): OpenBiPipeline<I, O> =
				OpenBiPipeline(forwardUniPipeline, backwardUniPipeline)

		@PublicApi
		fun <I, O> fromLayer(layer: TransitiveBiLayerWithFlowControl<I, O>): OpenBiPipeline<I, O>
		{
			val forwardUniPipeline = OpenUniPipeline.fromLayer(object : TransitiveLayerWithFlowControl<I, O> {
				override fun transformWithFlowControl(input: I) = layer.transformWithFlowControl(input)
			})
			val backwardUniPipeline = OpenUniPipeline.fromLayer(object : TransitiveLayerWithFlowControl<O, I> {
				override fun transformWithFlowControl(input: O) = layer.transformBackWithFlowControl(input)
			})
			return fromUniPipelines(forwardUniPipeline, backwardUniPipeline)
		}
	}

	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): OpenBiPipeline<I, NO> =
			fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
							rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): RightClosedBiPipeline<I, LEI> =
			RightClosedBiPipeline.fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
												  rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <NO> plus(rightLayer: TransitiveBiLayer<O, NO>): OpenBiPipeline<I, NO> = this.plus(biPipeline = rightLayer)

	@PublicApi
	operator fun plus(rightLayer: TerminalBiLayer<O>): RightClosedBiPipeline<I, O> = this.plus(biPipeline = rightLayer)

	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = forwardUniPipeline

	@PublicApi
	override fun toOpenBiPipeline(): OpenBiPipeline<I, O> = this

	@PublicApi
	override fun invert() = fromUniPipelines(backwardUniPipeline, forwardUniPipeline)
}

/**
 * Generic types:
 * - O - output type of pipeline
 * - FEO - output type of first layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class LeftClosedBiPipeline<O, FEO> internal constructor(override val forwardUniPipeline: LeftClosedUniPipeline<O, FEO>,
                                                        override val backwardUniPipeline: RightClosedUniPipeline<O, FEO>) :
		BiPipeline<Unit, O>(), ConvertibleToLeftClosedBiPipeline<O, FEO>, ConvertibleToLeftClosedUniPipeline<O, FEO>
{
	companion object
	{
		@PublicApi
		fun <O, FEO> fromUniPipelines(forwardUniPipeline: LeftClosedUniPipeline<O, FEO>,
		                              backwardUniPipeline: RightClosedUniPipeline<O, FEO>): LeftClosedBiPipeline<O, FEO> =
				LeftClosedBiPipeline(forwardUniPipeline, backwardUniPipeline)

		@PublicApi
		fun <T> fromLayer(layer: TerminalBiLayerWithFlowControl<T>): LeftClosedBiPipeline<T, T>
		{
			val forwardUniPipeline = LeftClosedUniPipeline.fromLayer(object : CreatorLayerWithFlowControl<T> {
				override fun transformWithFlowControl(input: Unit) = layer.transformBackWithFlowControl(input)
			})
			val backwardUniPipeline = RightClosedUniPipeline.fromLayer(object : ConsumerLayerWithFlowControl<T> {
				override fun transformWithFlowControl(input: T) = layer.transformWithFlowControl(input)
			})
			return fromUniPipelines(forwardUniPipeline, backwardUniPipeline)
		}
	}

	@PublicApi
	fun transform(): Output<O> = transform(Unit)

	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): LeftClosedBiPipeline<NO, FEO> =
			fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
							rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): ClosedBiPipeline =
			ClosedBiPipeline.fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
											 rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <NO> plus(rightLayer: TransitiveBiLayer<O, NO>): LeftClosedBiPipeline<NO, FEO> =
			this.plus(biPipeline = rightLayer)

	@PublicApi
	operator fun plus(rightLayer: TerminalBiLayer<O>): ClosedBiPipeline = this.plus(biPipeline = rightLayer)

	@PublicApi
	override fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<O, FEO> = forwardUniPipeline

	@PublicApi
	override fun toLeftClosedBiPipeline(): LeftClosedBiPipeline<O, FEO> = this

	@PublicApi
	override fun invert() = RightClosedBiPipeline.fromUniPipelines(backwardUniPipeline, forwardUniPipeline)
}


/**
 * Generic types:
 * - I - input type of pipeline
 * - LEI - input type of last layer, it is not important for using of pipeline, so can be ignored
 */
@PublicApi
class RightClosedBiPipeline<I, LEI> internal constructor(override val forwardUniPipeline: RightClosedUniPipeline<I, LEI>,
                                                         override val backwardUniPipeline: LeftClosedUniPipeline<I, LEI>) :
		BiPipeline<I, Unit>(), ConvertibleToRightClosedBiPipeline<I, LEI>, ConvertibleToRightClosedUniPipeline<I, LEI>
{
	companion object
	{
		@PublicApi
		fun <I, LEI> fromUniPipelines(forwardUniPipeline: RightClosedUniPipeline<I, LEI>,
		                              backwardUniPipeline: LeftClosedUniPipeline<I, LEI>): RightClosedBiPipeline<I, LEI> =
				RightClosedBiPipeline(forwardUniPipeline, backwardUniPipeline)

		@PublicApi
		fun <T> fromLayer(layer: TerminalBiLayerWithFlowControl<T>): RightClosedBiPipeline<T, T>
		{
			val forwardUniPipeline = RightClosedUniPipeline.fromLayer(object : ConsumerLayerWithFlowControl<T> {
				override fun transformWithFlowControl(input: T) = layer.transformWithFlowControl(input)
			})
			val backwardUniPipeline = LeftClosedUniPipeline.fromLayer(object : CreatorLayerWithFlowControl<T> {
				override fun transformWithFlowControl(input: Unit) = layer.transformBackWithFlowControl(input)
			})
			return fromUniPipelines(forwardUniPipeline, backwardUniPipeline)
		}
	}

	@PublicApi
	fun transformBack(): Output<I> = transformBack(Unit)

	override fun toRightClosedUniPipeline(): RightClosedUniPipeline<I, LEI> = forwardUniPipeline

	@PublicApi
	override fun toRightClosedBiPipeline(): RightClosedBiPipeline<I, LEI> = this

	@PublicApi
	override fun invert() = LeftClosedBiPipeline.fromUniPipelines(backwardUniPipeline, forwardUniPipeline)
}

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

	@PublicApi
	override fun invert() = fromUniPipelines(backwardUniPipeline, forwardUniPipeline)
}
