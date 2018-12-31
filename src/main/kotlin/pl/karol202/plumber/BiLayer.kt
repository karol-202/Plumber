package pl.karol202.plumber

@PublicApi
interface BiLayerWithFlowControl<I, O>
{
	@PublicApi
	fun transformWithFlowControl(input: I): Output<O>

    @PublicApi
	fun transformBackWithFlowControl(input: O): Output<I>
}

@PublicApi
interface TransitiveBiLayerWithFlowControl<I, O> : BiLayerWithFlowControl<I, O>, ConvertibleToOpenUniPipeline<I, O>, ConvertibleToOpenBiPipeline<I, O>
{
	@PublicApi
	override fun toOpenUniPipeline(): OpenUniPipeline<I, O> = OpenUniPipeline.fromLayer(this)

	@PublicApi
	override fun toOpenBiPipeline(): OpenBiPipeline<I, O> = OpenBiPipeline.fromLayer(this)

	@PublicApi
	fun invert(): TransitiveBiLayerWithFlowControl<O, I> = object : TransitiveBiLayerWithFlowControl<O, I> {
		override fun transformWithFlowControl(input: O) = this@TransitiveBiLayerWithFlowControl.transformBackWithFlowControl(input)

		override fun transformBackWithFlowControl(input: I) = this@TransitiveBiLayerWithFlowControl.transformWithFlowControl(input)
	}
}

@PublicApi
interface TerminalBiLayerWithFlowControl<T> : BiLayerWithFlowControl<T, Unit>,
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

@PublicApi
interface BiLayer<I, O> : BiLayerWithFlowControl<I, O>
{
	@PublicApi
	override fun transformWithFlowControl(input: I): Output<O> = Output.Value(transform(input))

	@PublicApi
	override fun transformBackWithFlowControl(input: O): Output<I> = Output.Value(transformBack(input))

	@PublicApi
	fun transform(input: I): O

	@PublicApi
	fun transformBack(input: O): I
}

@PublicApi
interface TransitiveBiLayer<I, O> : BiLayer<I, O>, TransitiveBiLayerWithFlowControl<I, O>

@PublicApi
interface TerminalBiLayer<T> : BiLayer<T, Unit>, TerminalBiLayerWithFlowControl<T>
