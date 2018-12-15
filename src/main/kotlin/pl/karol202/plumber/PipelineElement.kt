package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO,
						   FE : FirstPipelineElement<*, PI, PO, LE, FE>,
						   LE : LastPipelineElement<*, PI, PO, FE, LE>>
{
	val firstElement: FE
	val lastElement: LE

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

interface PipelineElementWithPredecessor<I, O, PI, PO,
										  FE : FirstPipelineElement<*, PI, PO, LE, FE>,
										  LE : LastPipelineElement<*, PI, PO, FE, LE>,
										  T : PipelineElementWithPredecessor<I, O, PI, PO, FE, LE, T>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	var previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE, *>

	fun <CPO,
		 CFE : FirstPipelineElement<*, PI, CPO, CLE, CFE>,
		 CLE : LastPipelineElement<*, PI, CPO, CFE, CLE>,
		 CT : PipelineElementWithPredecessor<I, *, PI, CPO, CFE, CLE, CT>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE, *>):
			PipelineElementWithPredecessor<I, *, PI, CPO, CFE, CLE, CT>?
}

interface PipelineElementWithSuccessor<I, O, PI, PO,
										FE : FirstPipelineElement<*, PI, PO, LE, FE>,
										LE : LastPipelineElement<*, PI, PO, FE, LE>,
										T : PipelineElementWithSuccessor<I, O, PI, PO, FE, LE, T>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE, *>

	fun <CPI,
		 CFE : FirstPipelineElement<*, CPI, PO, CLE, CFE>,
		 CLE : LastPipelineElement<*, CPI, PO, CFE, CLE>,
		 CT : PipelineElementWithSuccessor<I, O, CPI, PO, CFE, CLE, CT>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE, *>):
			PipelineElementWithSuccessor<I, O, CPI, PO, CFE, CLE, CT>?
}

interface FirstPipelineElement<O, PI, PO,
							   LE : LastPipelineElement<*, PI, PO, T, LE>,
							   T : FirstPipelineElement<O, PI, PO, LE, T>> :
		PipelineElementWithSuccessor<PI, O, PI, PO, T, LE, T>

interface LastPipelineElement<I, PI, PO,
							  FE : FirstPipelineElement<*, PI, PO, T, FE>,
							  T : LastPipelineElement<I, PI, PO, FE, T>> :
		PipelineElementWithPredecessor<I, PO, PI, PO, FE, T, T>
{
	fun <CPI,
		 CFE : FirstPipelineElement<*, CPI, PO, CT, CFE>,
		 CT : LastPipelineElement<I, CPI, PO, CFE, CT>>
			copySelfWithNewPI(): CT?
}

class StartPipelineElement<O, PO,
						   LE : LastPipelineElement<*, Unit, PO, StartPipelineElement<O, PO, LE>, LE>>
		(private val layer: FirstLayer<O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO, StartPipelineElement<O, PO, LE>, LE, *>) :
		FirstPipelineElement<O, Unit, PO, LE, StartPipelineElement<O, PO, LE>>
{
	override val firstElement: StartPipelineElement<O, PO, LE>
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
				  CFE : FirstPipelineElement<*, CPI, PO, CLE, CFE>,
				  CLE : LastPipelineElement<*, CPI, PO, CFE, CLE>,
				  CT : PipelineElementWithSuccessor<Unit, O, CPI, PO, CFE, CLE, CT>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE, *>): Nothing? = null
}

class MiddlePipelineElement<I, O, PI, PO,
							FE : PipelineElementWithSuccessor<PI, *, PI, PO, FE, LE, FE>,
							LE : PipelineElementWithPredecessor<*, PO, PI, PO, FE, LE, LE>>
		(private val layer: MiddleLayer<I, O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE, *>) :
		PipelineElementWithPredecessor<I, O, PI, PO, FE, LE, MiddlePipelineElement<I, O, PI, PO, FE, LE>>,
		PipelineElementWithSuccessor<I, O, PI, PO, FE, LE, MiddlePipelineElement<I, O, PI, PO, FE, LE>>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE, *>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE, *>
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
				  CFE : FirstPipelineElement<CPI, *, CPI, PO, CFE, CLE>,
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

class EndPipelineElement<I, PI,
						 FE : FirstPipelineElement<*, PI, Unit, EndPipelineElement<I, PI, FE>, FE>>
		(private val layer: LastLayer<I>) :
		LastPipelineElement<I, PI, Unit, FE, EndPipelineElement<I, PI, FE>>
{
	private var _previousElement: PipelineElementWithSuccessor<*, I, PI, Unit, FE, EndPipelineElement<I, PI, FE>, *>? = null
	override var previousElement: PipelineElementWithSuccessor<*, I, PI, Unit, FE, EndPipelineElement<I, PI, FE>, *>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: EndPipelineElement<I, PI, FE>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, Unit, CT, CFE>,
				  CT : LastPipelineElement<I, CPI, Unit, CFE, CT>> copySelfWithNewPI(): EndPipelineElement<I, PI, FE> =
			EndPipelineElement<I, CPI, CFE>(layer)

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
		 CFE : PipelineElementWithSuccessor<CPI, *, CPI, PO, CFE, CLE, CFE>,
		 CLE : PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE, CLE>>
			insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI, OFE>):
			PipelineElement<CPI, *, CPI, PO, CFE, CLE>
	{
		val newNext: PipelineElementWithPredecessor<*, PO, CPI, PO, CFE, CLE, CLE> = lastElement.
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