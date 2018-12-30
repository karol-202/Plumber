package pl.karol202.plumber

/**
 * Layer representing any operation which can have input and/or output.
 * Every layer can transform data forwards (data of input type to output type) and backwards (data of output type to input type).
 * BiLayer should not be implemented directly but through one of interfaces: FirstBiLayer, TransitiveBiLayer, LastBiLayer.
 *
 * Generic types:
 * - I - input type
 * - O - output type
*/
@PublicApi
interface BiLayer<I, O>
{
	/**
	 * Transforms data from input to output type.
	 */
	@PublicApi
	fun transform(input: I): O

    /**
     * Transforms data from output to input type.
     */
    @PublicApi
	fun transformBack(input: O): I
}

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start part or end part.
 */
@PublicApi
interface TransitiveBiLayer<I, O> : BiLayer<I, O>, ConvertibleToOpenUniPipeline<I, O>, ConvertibleToOpenBiPipeline<I, O>
{
	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = OpenUniPipeline.fromLayer(this)

	@PublicApi
	override fun toOpenBiPipeline(): OpenBiPipeline<I, O> = OpenBiPipeline.fromLayer(this)

	@PublicApi
	fun invert(): TransitiveBiLayer<O, I> = object : TransitiveBiLayer<O, I> {
		override fun transform(input: O) = this@TransitiveBiLayer.transformBack(input)

		override fun transformBack(input: I) = this@TransitiveBiLayer.transform(input)
	}
}

/**
 * First layer is layer creating data (for example from IO operations). It can be only a start of a pipeline.
 */
@PublicApi
interface TerminalBiLayer<T> : BiLayer<T, Unit>,
                               ConvertibleToLeftClosedUniPipeline<T, T>,
                               ConvertibleToRightClosedUniPipeline<T, T>,
                               ConvertibleToLeftClosedBiPipeline<T, T>,
                               ConvertibleToRightClosedBiPipeline<T, T>
{
	@PublicApi
	override fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<T, T> = LeftClosedUniPipeline.fromLayer(this)

	@PublicApi
	override fun toRightClosedUniPipeline(): RightClosedUniPipeline<T, T> = RightClosedUniPipeline.fromLayer(this)

	@PublicApi
	override fun toLeftClosedBiPipeline(): LeftClosedBiPipeline<T, T> = LeftClosedBiPipeline.fromLayer(this)

	@PublicApi
	override fun toRightClosedBiPipeline(): RightClosedBiPipeline<T, T> = RightClosedBiPipeline.fromLayer(this)
}
