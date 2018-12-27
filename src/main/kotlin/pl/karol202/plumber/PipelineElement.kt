package pl.karol202.plumber

internal interface PipelineElement<I, O, PI, PO>
{
	val firstElement: PipelineElement<PI, *, PI, PO>
	val lastElement: PipelineElement<*, PO, PI, PO>

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

internal interface PipelineElementWithPredecessor<I, O, PI, PO> :
		PipelineElement<I, O, PI, PO>
{
	var previousElement: PipelineElementWithSuccessor<*, I, PI, PO>

	fun <CPI> copyWithNewPI(): PipelineElementWithPredecessor<I, O, CPI, PO>
}

internal interface PipelineElementWithSuccessor<I, O, PI, PO> :
		PipelineElement<I, O, PI, PO>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO>

	fun <CPO> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, PI, CPO>):
			PipelineElementWithSuccessor<I, O, PI, CPO>
}

internal class FirstPipelineElement<O, PO>(private val layer: FirstLayer<O>,
                                           override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO>) :
		PipelineElementWithSuccessor<Unit, O, Unit, PO>
{
	override val firstElement: FirstPipelineElement<O, PO>
		get() = this
	override val lastElement: PipelineElement<*, PO, Unit, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun <CPO> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, Unit, CPO>) =
		FirstPipelineElement(layer, nextElement)
}

internal class MiddlePipelineElement<I, O, PI, PO>(private val layer: MiddleLayer<I, O>,
                                                   override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO>) :
		PipelineElementWithPredecessor<I, O, PI, PO>,
		PipelineElementWithSuccessor<I, O, PI, PO>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, PO>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, PO>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: PipelineElement<PI, *, PI, PO>
		get() = previousElement.firstElement
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI> copyWithNewPI() = MiddlePipelineElement(layer, nextElement.copyWithNewPI<CPI>())

	override fun <CPO> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, PI, CPO>) =
			MiddlePipelineElement(layer, nextElement)
}

internal class LastPipelineElement<I, PI>(private val layer: LastLayer<I>) :
		PipelineElementWithPredecessor<I, Unit, PI, Unit>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, Unit>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, Unit>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: PipelineElement<PI, *, PI, Unit>
		get() = previousElement.firstElement
	override val lastElement: LastPipelineElement<I, PI>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI> copyWithNewPI() = LastPipelineElement<I, CPI>(layer)
}

internal class StartPipelineTerminator<PI, PO>(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO>) :
		PipelineElementWithSuccessor<PI, PI, PI, PO>
{
	override val firstElement: StartPipelineTerminator<PI, PO>
		get() = this
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun <CPO> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<PI, *, PI, CPO>) =
			StartPipelineTerminator(nextElement)
}

internal class EndPipelineTerminator<PI, PO> :
		PipelineElementWithPredecessor<PO, PO, PI, PO>
{
	private var _previousElement: PipelineElementWithSuccessor<*, PO, PI, PO>? = null
	override var previousElement: PipelineElementWithSuccessor<*, PO, PI, PO>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: PipelineElement<PI, *, PI, PO>
		get() = previousElement.firstElement
	override val lastElement: EndPipelineTerminator<PI, PO>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun <CPI> copyWithNewPI() = EndPipelineTerminator<CPI, PO>()
}
