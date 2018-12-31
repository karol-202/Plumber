package pl.karol202.plumber

@PublicApi
interface Layer<I, O>
{
	@PublicApi
	fun transform(input: I): Output<O>
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

@PublicApi
interface SimpleLayer<I, O> : Layer<I, O>
{
	@PublicApi
	override fun transform(input: I): Output<O> = Output.Value(transformSimply(input))

	@PublicApi
	fun transformSimply(input: I): O
}

@PublicApi
interface SimpleCreatorLayer<O> : SimpleLayer<Unit, O>, CreatorLayer<O>

@PublicApi
interface SimpleTransitiveLayer<I, O> : SimpleLayer<I, O>, TransitiveLayer<I, O>

@PublicApi
interface SimpleConsumerLayer<I> : SimpleLayer<I, Unit>, ConsumerLayer<I>
