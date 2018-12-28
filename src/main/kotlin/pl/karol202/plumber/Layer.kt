package pl.karol202.plumber

interface Layer<I, O>
{
	fun transformForward(input: I): O

	fun transformBackward(input: O): I
}

interface FirstLayer<O> : Layer<Unit, O>

interface MiddleLayer<I, O> : Layer<I, O>

interface LastLayer<I> : Layer<I, Unit>


fun <O> FirstLayer<O>.toPipeline() = LeftClosedPipeline.fromLayer(this)

fun <I, O> MiddleLayer<I, O>.toPipeline() = OpenPipeline.fromLayer(this)

fun <I> LastLayer<I>.toPipeline() = RightClosedPipeline.fromLayer(this)

operator fun <O, NO> FirstLayer<O>.plus(rightPipeline: OpenPipeline<O, NO>) = toPipeline() + rightPipeline

operator fun <O, LEI> FirstLayer<O>.plus(rightPipeline: RightClosedPipeline<O, LEI>) = toPipeline() + rightPipeline

operator fun <O, NO> FirstLayer<O>.plus(rightLayer: MiddleLayer<O, NO>) = toPipeline() + rightLayer.toPipeline()

operator fun <O> FirstLayer<O>.plus(rightLayer: LastLayer<O>) = toPipeline() + rightLayer.toPipeline()

operator fun <I, O, NO> MiddleLayer<I, O>.plus(rightPipeline: OpenPipeline<O, NO>) = toPipeline() + rightPipeline

operator fun <I, O, LEI> MiddleLayer<I, O>.plus(rightPipeline: RightClosedPipeline<O, LEI>) = toPipeline() + rightPipeline

operator fun <I, O, NO> MiddleLayer<I, O>.plus(rightLayer: MiddleLayer<O, NO>) = toPipeline() + rightLayer.toPipeline()

operator fun <I, O> MiddleLayer<I, O>.plus(rightLayer: LastLayer<O>) = toPipeline() + rightLayer.toPipeline()
