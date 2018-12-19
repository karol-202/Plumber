package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO>
{
	val firstElement: FirstPipelineElement<*, PI, PO>
	val lastElement: LastPipelineElement<*, PI, PO>

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

interface PipelineElementWithPredecessor<I, O, PI, PO> : PipelineElement<I, O, PI, PO>
{
	var previousElement: PipelineElementWithSuccessor<*, I, PI, PO>

	fun <CPO> copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO>):
			PipelineElementWithPredecessor<I, *, PI, CPO>?
}

interface PipelineElementWithSuccessor<I, O, PI, PO> : PipelineElement<I, O, PI, PO>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO>

	fun <CPI> copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO>):
			PipelineElementWithSuccessor<I, O, CPI, PO>?
}

interface FirstPipelineElement<O, PI, PO> : PipelineElementWithSuccessor<PI, O, PI, PO>

interface LastPipelineElement<I, PI, PO> : PipelineElementWithPredecessor<I, PO, PI, PO>
{
	fun <CPI> copySelfWithNewPI(): LastPipelineElement<I, CPI, PO>?
}

class StartPipelineElement<O, PO>(private val layer: FirstLayer<O>,
                                  override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO>) :
		FirstPipelineElement<O, Unit, PO>
{
	override val firstElement: StartPipelineElement<O, PO>
		get() = this
	override val lastElement: LastPipelineElement<*, Unit, PO>
		get() = nextElement.lastElement

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun <CPI> copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO>): Nothing? = null
}

class MiddlePipelineElement<I, O, PI, PO>(private val layer: MiddleLayer<I, O>,
                                          override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO>) :
		PipelineElementWithPredecessor<I, O, PI, PO>,
		PipelineElementWithSuccessor<I, O, PI, PO>
{
	override var previousElement = previousElementSupplier(this)

	override val firstElement: FirstPipelineElement<*, PI, PO>
		get() = previousElement.firstElement
	override val lastElement: LastPipelineElement<*, PI, PO>
		get() = nextElement.lastElement

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI> copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO>) =
			MiddlePipelineElement(layer, nextElement)

	override fun <CPO> copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO>):
			MiddlePipelineElement<I, O, PI, CPO>?
	{
		val next = nextElement.copyWithNewPO(elementToInsertAtEnd) ?: return null
		return MiddlePipelineElement(layer, next)
	}
}

/*fun <I, O, CPI, PO,
		CFE : FirstPipelineElement<*, CPI, PO, CLE>,
		CLE : LastPipelineElement<*, CPI, PO, CFE>>
		copyBackwardsWithNewPI(element: MiddlePipelineElement<>
		elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, I, CPI, PO, CFE, CLE>,
		                       nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>) =
		MiddlePipelineElement(layer, { previousElement.copyBackwardsWithNewPI<CPI, CFE, CLE>(null, it) ?: elementToInsertAtStartSupplier() }, nextElement)*/

class EndPipelineElement<I, PI>(private val layer: LastLayer<I>) : LastPipelineElement<I, PI, Unit>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, Unit, FE, EndPipelineElement<I, PI, FE>, *>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, Unit, FE, EndPipelineElement<I, PI, FE>, *>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: FirstPipelineElement<*, PI, Unit>
		get() = previousElement.firstElement
	override val lastElement: EndPipelineElement<I, PI>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI> copySelfWithNewPI(): LastPipelineElement<I, CPI, Unit> = EndPipelineElement(layer)

	override fun <CPO> copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<Unit, *, PI, CPO>): Nothing? = null
}

class StartPipelineTerminator<PI, PO>(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO>) :
		FirstPipelineElement<PI, PI, PO>
{
	override val firstElement: StartPipelineTerminator<PI, PO>
		get() = this
	override val lastElement: LastPipelineElement<*, PI, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun <CPI> copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<PI, *, CPI, PO>): Nothing? = null

	fun <CPI> insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI>):
			PipelineElement<CPI, *, CPI, PO>
	{
		//val newNext: PipelineElementWithPredecessor<*, PO, CPI, PO> = lastElement.
		val newPrevious = endPipelineTerminator.previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}
}

class EndPipelineTerminator<PI, PO> : LastPipelineElement<PO, PI, PO>
{
	private var _previousElement: PipelineElementWithSuccessor<*, PO, PI, PO>? = null
	override var previousElement: PipelineElementWithSuccessor<*, PO, PI, PO>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: FirstPipelineElement<*, PI, PO>
		get() = previousElement.firstElement
	override val lastElement: EndPipelineTerminator<PI, PO>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun <CPO> copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO>) = elementToInsertAtEnd

	fun <CPO> insertAtEndAndReturnBeginning(startPipelineTerminator: StartPipelineTerminator<PO, CPO>):
			PipelineElement<PI, *, PI, CPO>
	{
		val newNext = startPipelineTerminator.nextElement.copyWithNewPI<PI>()
		val newPrevious = previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}
}