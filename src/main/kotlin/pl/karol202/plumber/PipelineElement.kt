package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO>
{
	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

class FirstPipelineElement<O, PO>(private val layer: FirstLayer<O>,
                                  private val nextElement: PipelineElement<O, *, Unit, PO>) :
		PipelineElement<Unit, O, Unit, PO>
{
	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)
}

class MiddlePipelineElement<I, O, PI, PO>(private val layer: MiddleLayer<I, O>,
                                          private val previousElement: PipelineElement<*, I, PI, PO>,
                                          private val nextElement: PipelineElement<O, *, PI, PO>) :
		PipelineElement<I, O, PI, PO>
{
	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))
}

class LastPipelineElement<I, PI>(private val layer: LastLayer<I>,
                                 private val previousElement: PipelineElement<*, I, PI, Unit>) :
		PipelineElement<I, Unit, PI, Unit>
{
	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))
}

class StartPipelineTerminator<PI, PO>(private val nextElement: PipelineElement<PI, *, PI, PO>) :
		PipelineElement<PI, PI, PI, PO>
{
	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input
}

class EndPipelineTerminator<PI, PO>(private val previousElement: PipelineElement<*, PO, PI, PO>) :
		PipelineElement<PO, PO, PI, PO>
{
	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)
}