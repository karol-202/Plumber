package pl.karol202.plumber

interface Layer<I, O>
{
	fun transformForward(input: I): O

	fun transformBackward(input: O): I
}

interface FirstLayer<O> : Layer<Unit, O>, RightExpandablePipeline<Unit, O>
{
	override fun <PO> plus(next: LeftExpandablePipeline<O, PO>): Pipeline<Unit, PO>
	{

	}

	override fun toPipeline() = LeftClosedPipeline.fromLayer(this)
}

interface MiddleLayer<I, O> : Layer<I, O>, LeftExpandablePipeline<I, O>, RightExpandablePipeline<I, O>
{
	override fun <PO> plus(next: LeftExpandablePipeline<O, PO>): Pipeline<I, PO>
	{

	}

	override fun toPipeline() = OpenPipeline.fromLayer(this)
}

interface LastLayer<I> : Layer<I, Unit>, LeftExpandablePipeline<I, Unit>
{
	override fun toPipeline() = RightClosedPipeline.fromLayer(this)
}