package pl.karol202.plumber

@PublicApi
interface LayerWithFlowControl<I, O>
{
	@PublicApi
	fun transformWithFlowControl(input: I): Output<O>
}

@PublicApi
interface CreatorLayerWithFlowControl<O> : LayerWithFlowControl<Unit, O>, ConvertibleToLeftClosedUniPipeline<O, O>
{
	@PublicApi
	override fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<O, O> = LeftClosedUniPipeline.fromLayer(this)
}

@PublicApi
interface TransitiveLayerWithFlowControl<I, O> : LayerWithFlowControl<I, O>, ConvertibleToOpenUniPipeline<I, O>
{
	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = OpenUniPipeline.fromLayer(this)
}

@PublicApi
interface ConsumerLayerWithFlowControl<I> : LayerWithFlowControl<I, Unit>, ConvertibleToRightClosedUniPipeline<I, I>
{
	@PublicApi
	override fun toRightClosedUniPipeline(): RightClosedUniPipeline<I, I> = RightClosedUniPipeline.fromLayer(this)
}

@PublicApi
interface Layer<I, O> : LayerWithFlowControl<I, O>
{
	@PublicApi
	override fun transformWithFlowControl(input: I): Output<O> = Output.Value(transform(input))

	@PublicApi
	fun transform(input: I): O
}

@PublicApi
interface CreatorLayer<O> : Layer<Unit, O>, CreatorLayerWithFlowControl<O>

@PublicApi
interface TransitiveLayer<I, O> : Layer<I, O>, TransitiveLayerWithFlowControl<I, O>

@PublicApi
interface ConsumerLayer<I> : Layer<I, Unit>, ConsumerLayerWithFlowControl<I>
