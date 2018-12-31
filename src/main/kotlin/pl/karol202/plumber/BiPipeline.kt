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
}

/**
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

	@PublicApi
	operator fun <NO> plus(rightPipeline: OpenBiPipeline<O, NO>): OpenBiPipeline<I, NO> =
			fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
							rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	operator fun <LEI> plus(rightPipeline: RightClosedBiPipeline<O, LEI>): RightClosedBiPipeline<I, LEI> =
			RightClosedBiPipeline.fromUniPipelines(forwardUniPipeline + rightPipeline.forwardUniPipeline,
												  rightPipeline.backwardUniPipeline + backwardUniPipeline)

	@PublicApi
	override fun toOpenBiPipeline(): OpenBiPipeline<I, O> = this
}

/**
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
	override fun toLeftClosedBiPipeline(): LeftClosedBiPipeline<O, FEO> = this
}


/**
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

	@PublicApi
	fun transformBack(): Output<I> = transformBack(Unit)

	@PublicApi
	override fun toRightClosedBiPipeline(): RightClosedBiPipeline<I, LEI> = this
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
}
