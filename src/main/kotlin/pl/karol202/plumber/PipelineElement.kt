package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO,
			FE : PipelineElement<PI, *, PI, PO, FE, LE>,
			LE : PipelineElement<*, PO, PI, PO, FE, LE>>
{
	val firstElement: FE
	val lastElement: LE

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI

	fun copy(): PipelineElement<I, O, PI, PO, FE, LE>
}

interface PipelineElementWithPredecessor<I, O, PI, PO,
			FE : PipelineElement<PI, *, PI, PO, FE, LE>,
			LE : PipelineElement<*, PO, PI, PO, FE, LE>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	var previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE>

	override fun copy(): PipelineElementWithPredecessor<I, O, PI, PO, FE, LE>

	fun <CPI, CFE : PipelineElement<CPI, *, CPI, PO, CFE, CLE>, CLE : PipelineElement<*, PO, CPI, PO, CFE, CLE>>
			copyWithNewPI(): PipelineElementWithPredecessor<I, O, CPI, PO, CFE, CLE>
}

interface PipelineElementWithSuccessor<I, O, PI, PO,
			FE : PipelineElement<PI, *, PI, PO, FE, LE>,
			LE : PipelineElement<*, PO, PI, PO, FE, LE>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE>

	fun <CPO, CFE : PipelineElement<PI, *, PI, CPO, CFE, CLE>, CLE : PipelineElement<*, CPO, PI, CPO, CFE, CLE>>
			copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, PI, CPO, CFE, CLE>):
			PipelineElementWithSuccessor<I, O, PI, CPO, CFE, CLE>
}

class FirstPipelineElement<O, PO, LE : PipelineElement<*, PO, Unit, PO, FirstPipelineElement<O, PO, LE>, LE>>
		(private val layer: FirstLayer<O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO, FirstPipelineElement<O, PO, LE>, LE>) :
		PipelineElementWithSuccessor<Unit, O, Unit, PO, FirstPipelineElement<O, PO, LE>, LE>
{
	override val firstElement: FirstPipelineElement<O, PO, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun copy() = FirstPipelineElement(layer, nextElement.copy())

	override fun <CPO, CFE : FirstPipelineElement<O, CPO, CLE>, CLE : PipelineElement<*, CPO, Unit, CPO, CFE, CLE>>
			copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, Unit, CPO, CFE, CLE>):
			FirstPipelineElement<O, CPO, CLE> =
			FirstPipelineElement(layer, nextElement)
}

class MiddlePipelineElement<I, O, PI, PO>(private val layer: MiddleLayer<I, O>,
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

	override fun copy() = MiddlePipelineElement(layer, nextElement.copy())

	override fun <CPI> copyWithNewPI() = MiddlePipelineElement(layer, nextElement.copyWithNewPI<CPI>())

	override fun <CPO> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, PI, CPO>) =
		MiddlePipelineElement(layer, nextElement).also { previousElement.copyBackwardsWithNewPO(it) }
}

class LastPipelineElement<I, PI>(private val layer: LastLayer<I>) :
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
	override val lastElement: PipelineElement<*, Unit, PI, Unit>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun copy() = LastPipelineElement<I, PI>(layer)

	override fun <CPI> copyWithNewPI() = LastPipelineElement<I, CPI>(layer)
}

class StartPipelineTerminator<PI, PO>(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO>) :
		PipelineElementWithSuccessor<PI, PI, PI, PO>
{
	override val firstElement: PipelineElement<PI, *, PI, PO>
		get() = this
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun copy() = StartPipelineTerminator(nextElement.copy())

	override fun <CPO> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<PI, *, PI, CPO>) =
			StartPipelineTerminator(nextElement)

	fun <CPI> insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI>):
			PipelineElement<CPI, *, CPI, PO>
	{
		val newNext = nextElement.copyWithNewPI<CPI>()
		val newPrevious = endPipelineTerminator.previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}
}

class EndPipelineTerminator<PI, PO> :
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
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun copy() = EndPipelineTerminator<PI, PO>()

	override fun <CPI> copyWithNewPI() = EndPipelineTerminator<CPI, PO>()

	fun <CPO> insertAtEndAndReturnBeginning(startPipelineTerminator: StartPipelineTerminator<PO, CPO>):
			PipelineElement<PI, *, PI, CPO>
	{
		val newNext = startPipelineTerminator.nextElement.copyWithNewPI<PI>()
		val newPrevious = previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}
}