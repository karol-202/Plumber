package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO,
						   FE : PipelineElementWithSuccessor<PI, *, PI, PO, FE, LE, FE>,
						   LE : PipelineElementWithPredecessor<*, PO, PI, PO, FE, LE, LE>>
{
	val firstElement: FE
	val lastElement: LE

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

interface PipelineElementWithPredecessor<I, O, PI, PO,
										  FE : PipelineElementWithSuccessor<PI, *, PI, PO, FE, LE, FE>,
										  LE : PipelineElementWithPredecessor<*, PO, PI, PO, FE, LE, LE>,
										  T : PipelineElementWithPredecessor<I, O, PI, PO, FE, LE, T>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	var previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE, *>

	fun <CPI,
		 CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE, CFE>,
		 CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE, CLE>,
		 CT : PipelineElementWithPredecessor<I, O, CPI, PO, CFE, CLE, CT>>
			copySelfWithNewPI(): PipelineElementWithPredecessor<I, O, CPI, PO, CFE, CLE, CT>?

	fun <CPO,
		 CFE : PipelineElementWithSuccessor<PI, *, PI, CPO, CFE, CLE, CFE>,
		 CLE : PipelineElementWithPredecessor<*, CPO, PI, CPO, CFE, CLE, CLE>,
		 CT : PipelineElementWithPredecessor<I, *, PI, CPO, CFE, CLE, CT>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>):
			PipelineElementWithPredecessor<I, *, PI, CPO, CFE, CLE>?
}

interface PipelineElementWithSuccessor<I, O, PI, PO,
										FE : PipelineElementWithSuccessor<PI, *, PI, PO, FE, LE, FE>,
										LE : PipelineElementWithPredecessor<*, PO, PI, PO, FE, LE, LE>,
										T : PipelineElementWithSuccessor<I, O, PI, PO, FE, LE, T>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE>

	fun <CPI,
		 CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE>,
		 CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>):
			PipelineElementWithSuccessor<I, O, CPI, PO, CFE, CLE>?
}

class FirstPipelineElement<O, PO,
							LE : PipelineElementWithPredecessor<*, PO, Unit, PO, FirstPipelineElement<O, PO, LE>, LE>>
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

	override fun <CPI,
				  CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>): Nothing? = null
}

class MiddlePipelineElement<I, O, PI, PO,
							 FE : PipelineElementWithSuccessor<PI, *, PI, PO, FE, LE>,
							 LE : PipelineElementWithPredecessor<*, PO, PI, PO, FE, LE>>
		(private val layer: MiddleLayer<I, O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE>) :
		PipelineElementWithPredecessor<I, O, PI, PO, FE, LE>,
		PipelineElementWithSuccessor<I, O, PI, PO, FE, LE>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: LE
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI,
				  CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE>>
			copySelfWithNewPI(): Nothing? = null

	override fun <CPI,
				  CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>) =
			MiddlePipelineElement(layer, nextElement).also { previousElement.copyBackwardsWithNewPI(it) }

	override fun <CPO,
				  CFE : PipelineElementWithSuccessor<PI, *, PI, CPO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, CPO, PI, CPO, CFE, CLE>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>):
			MiddlePipelineElement<I, O, PI, CPO, CFE, CLE>?
	{
		val next = nextElement.copyWithNewPO(elementToInsertAtEnd) ?: return null
		return MiddlePipelineElement(layer, next)
	}
}

class LastPipelineElement<I, PI,
						   FE : PipelineElementWithSuccessor<PI, *, PI, Unit, FE, LastPipelineElement<I, PI, FE>>>
		(private val layer: LastLayer<I>) :
		PipelineElementWithPredecessor<I, Unit, PI, Unit, FE, LastPipelineElement<I, PI, FE>>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, Unit, FE, LastPipelineElement<I, PI, FE>>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, Unit, FE, LastPipelineElement<I, PI, FE>>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: LastPipelineElement<I, PI, FE>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI,
				  CFE : PipelineElementWithSuccessor<CPI, *, CPI, Unit, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, Unit, CPI, Unit, CFE, CLE>>
			copySelfWithNewPI(): PipelineElementWithPredecessor<I, Unit, CPI, Unit, CFE, CLE> =
			LastPipelineElement<I, CPI, CFE>(layer)

	override fun <CPO,
				  CFE : PipelineElementWithSuccessor<PI, *, PI, CPO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, CPO, PI, CPO, CFE, CLE>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<Unit, *, PI, CPO, CFE, CLE>): Nothing? = null
}

class StartPipelineTerminator<PI, PO,
							   LE : PipelineElementWithPredecessor<*, PO, PI, PO, StartPipelineTerminator<PI, PO, LE>, LE>>
		(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO, StartPipelineTerminator<PI, PO, LE>, LE>) :
		PipelineElementWithSuccessor<PI, PI, PI, PO, StartPipelineTerminator<PI, PO, LE>, LE>
{
	override val firstElement: StartPipelineTerminator<PI, PO, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun <CPI,
				  CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<PI, *, CPI, PO, CFE, CLE>): Nothing? = null

	fun <CPI,
		 OFE : PipelineElementWithSuccessor<CPI, *, CPI, PI, OFE, EndPipelineTerminator<CPI, PI, OFE>>,
		 CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE>,
		 CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE>>
			insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI, OFE>):
			PipelineElement<CPI, *, CPI, PO, CFE, CLE>
	{
		val newNext = lastElement.
		val newPrevious = endPipelineTerminator.previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}
}

class EndPipelineTerminator<PI, PO,
							 FE : PipelineElementWithSuccessor<PI, *, PI, PO, FE, EndPipelineTerminator<PI, PO, FE>>> :
		PipelineElementWithPredecessor<PO, PO, PI, PO, FE, EndPipelineTerminator<PI, PO, FE>>
{
	private var _previousElement: PipelineElementWithSuccessor<*, PO, PI, PO, FE, EndPipelineTerminator<PI, PO, FE>>? = null
	override var previousElement: PipelineElementWithSuccessor<*, PO, PI, PO, FE, EndPipelineTerminator<PI, PO, FE>>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: EndPipelineTerminator<PI, PO, FE>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun <CPO,
				  CFE : PipelineElementWithSuccessor<PI, *, PI, CPO, CFE, CLE>,
				  CLE : PipelineElementWithPredecessor<*, CPO, PI, CPO, CFE, CLE>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>) = elementToInsertAtEnd

	fun <CPO> insertAtEndAndReturnBeginning(startPipelineTerminator: StartPipelineTerminator<PO, CPO>):
			PipelineElement<PI, *, PI, CPO>
	{
		val newNext = startPipelineTerminator.nextElement.copyWithNewPI<PI>()
		val newPrevious = previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}
}