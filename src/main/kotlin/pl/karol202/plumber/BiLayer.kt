package pl.karol202.plumber

@PublicApi
interface BiLayer<I, O>
{
	@PublicApi
	fun transform(input: I): O

    @PublicApi
	fun transformBack(input: O): I
}

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
