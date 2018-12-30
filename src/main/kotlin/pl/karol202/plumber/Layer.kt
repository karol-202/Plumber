package pl.karol202.plumber

@PublicApi
interface Layer<I, O>
{
	@PublicApi
	fun transform(input: I): O
}

@PublicApi
interface CreatorLayer<O> : Layer<Unit, O>, ConvertibleToLeftClosedUniPipeline<O, O>
{
	@PublicApi
	override fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<O, O> = LeftClosedUniPipeline.fromLayer(this)
}

@PublicApi
interface TransitiveLayer<I, O> : Layer<I, O>, ConvertibleToOpenUniPipeline<I, O>
{
	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = OpenUniPipeline.fromLayer(this)
}

@PublicApi
interface ConsumerLayer<I> : Layer<I, Unit>, ConvertibleToRightClosedUniPipeline<I, I>
{
	@PublicApi
	override fun toRightClosedUniPipeline(): RightClosedUniPipeline<I, I> = RightClosedUniPipeline.fromLayer(this)
}
