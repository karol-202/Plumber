package pl.karol202.plumber

@PublicApi
interface Layer<I, O>
{
	/**
	 * Transforms data from input to output type.
	 */
	@PublicApi
	fun transform(input: I): O
}

/**
 * First layer is layer creating data (for example from IO operations). It can be only a start of a pipeline.
 */
@PublicApi
interface CreatorLayer<O> : Layer<Unit, O>, ConvertibleToLeftClosedUniPipeline<O, O>
{
	@PublicApi
	override fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<O, O> = LeftClosedUniPipeline.fromLayer(this)
}

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start or end part.
 */
@PublicApi
interface TransitiveLayer<I, O> : Layer<I, O>, ConvertibleToOpenUniPipeline<I, O>
{
	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = OpenUniPipeline.fromLayer(this)
}

/**
 * Last layer is layer consuming data. It can be only an end of a pipeline.
 */
@PublicApi
interface ConsumerLayer<I> : Layer<I, Unit>, ConvertibleToRightClosedUniPipeline<I, I>
{
	@PublicApi
	override fun toRightClosedUniPipeline(): RightClosedUniPipeline<I, I> = RightClosedUniPipeline.fromLayer(this)
}
