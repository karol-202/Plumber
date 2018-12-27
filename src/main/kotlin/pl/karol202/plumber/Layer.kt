package pl.karol202.plumber

interface Layer<I, O>
{
	fun transformForward(input: I): O

	fun transformBackward(input: O): I
}

interface FirstLayer<O> : Layer<Unit, O>
{
	fun toPipeline() = LeftClosedPipeline.fromLayer(this)

	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>) = toPipeline() + rightPipeline

	operator fun plus(rightPipeline: RightClosedPipeline<O>) = toPipeline() + rightPipeline
}

interface MiddleLayer<I, O> : Layer<I, O>
{
	fun toPipeline() = OpenPipeline.fromLayer(this)

	operator fun <NO> plus(rightPipeline: OpenPipeline<O, NO>) = toPipeline() + rightPipeline

	operator fun plus(rightPipeline: RightClosedPipeline<O>) = toPipeline() + rightPipeline
}

interface LastLayer<I> : Layer<I, Unit>
{
	fun toPipeline() = RightClosedPipeline.fromLayer(this)
}