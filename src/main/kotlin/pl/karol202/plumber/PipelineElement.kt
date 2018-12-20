package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO,
						  out FE : FirstPipelineElement<*, PI, PO, LE>,
						  out LE : LastPipelineElement<*, PI, PO, FE>>
{
	val firstElement: FE
	val lastElement: LE

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI

	fun <CPO,
		 CFE : FirstPipelineElement<*, PI, CPO, CLE>,
		 CLE : LastPipelineElement<*, PI, CPO, CFE>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>):
			PipelineElementWithPredecessor<I, *, PI, CPO, CFE, CLE>

	fun <CPI,
		 CFE : FirstPipelineElement<*, CPI, PO, CLE>,
		 CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, I, CPI, PO, CFE, CLE>,
			                       nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>):
			PipelineElementWithSuccessor<I, O, CPI, PO, CFE, CLE>
}

interface PipelineElementWithPredecessor<I, O, PI, PO,
										 out FE : FirstPipelineElement<*, PI, PO, LE>,
										 out LE : LastPipelineElement<*, PI, PO, FE>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE>
}

interface PipelineElementWithSuccessor<I, O, PI, PO,
									   out FE : FirstPipelineElement<*, PI, PO, LE>,
									   out LE : LastPipelineElement<*, PI, PO, FE>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE>
}

interface FirstPipelineElement<O, PI, PO,
							   out LE : LastPipelineElement<*, PI, PO, FirstPipelineElement<O, PI, PO, LE>>> :
		PipelineElementWithSuccessor<PI, O, PI, PO, FirstPipelineElement<O, PI, PO, LE>, LE>

interface LastPipelineElement<I, PI, PO,
							  out FE : FirstPipelineElement<*, PI, PO, LastPipelineElement<I, PI, PO, FE>>> :
		PipelineElementWithPredecessor<I, PO, PI, PO, FE, LastPipelineElement<I, PI, PO, FE>>

class StartPipelineElement<O, PO,
						   out LE : LastPipelineElement<*, Unit, PO, StartPipelineElement<O, PO, LE>>>
		(private val layer: FirstLayer<O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO, StartPipelineElement<O, PO, LE>, LE>) :
		FirstPipelineElement<O, Unit, PO, LE>
{
	override val firstElement: StartPipelineElement<O, PO, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun <CPO,
				  CFE : FirstPipelineElement<*, Unit, CPO, CLE>,
				  CLE : LastPipelineElement<*, Unit, CPO, CFE>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, Unit, CPO, CFE, CLE>) =
			StartPipelineElement<O, CPO, CLE>(layer, nextElement.copyWithNewPO(elementToInsertAtEnd))

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, CLE>,
				  CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, Unit, CPI, PO, CFE, CLE>,
			                       nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>) =
			throw PipelineException("Cannot change PI of StartPipelineElement.")
}

class MiddlePipelineElement<I, O, PI, PO,
							out FE : FirstPipelineElement<*, PI, PO, LE>,
							out LE : LastPipelineElement<*, PI, PO, FE>>
		(private val layer: MiddleLayer<I, O>,
		 previousElementSupplier: (MiddlePipelineElement<I, O, PI, PO, FE, LE>) -> PipelineElementWithSuccessor<*, I, PI, PO, FE, LE>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE>) :
		PipelineElementWithPredecessor<I, O, PI, PO, FE, LE>,
		PipelineElementWithSuccessor<I, O, PI, PO, FE, LE>
{
	override val previousElement = previousElementSupplier(this)

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, CLE>,
				  CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, I, CPI, PO, CFE, CLE>,
			                       nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>) =
			MiddlePipelineElement(layer, { previousElement.copyBackwardsWithNewPI<CPI, CFE, CLE>(null, it) ?: elementToInsertAtStartSupplier() }, nextElement)

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

/*fun <I, O, CPI, PO,
		CFE : FirstPipelineElement<*, CPI, PO, CLE>,
		CLE : LastPipelineElement<*, CPI, PO, CFE>>
		copyBackwardsWithNewPI(element: MiddlePipelineElement<>
		elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, I, CPI, PO, CFE, CLE>,
		                       nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>) =
		MiddlePipelineElement(layer, { previousElement.copyBackwardsWithNewPI<CPI, CFE, CLE>(null, it) ?: elementToInsertAtStartSupplier() }, nextElement)*/

class EndPipelineElement<I, PI,
						 out FE : FirstPipelineElement<*, PI, Unit, LastPipelineElement<I, PI, Unit, FE>>>
		(private val layer: LastLayer<I>) :
		LastPipelineElement<I, PI, Unit, FE>
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

	override fun <CPI, CFE : FirstPipelineElement<*, CPI, Unit, LastPipelineElement<I, CPI, Unit, CFE>>>
			copySelfWithNewPI(): LastPipelineElement<I, CPI, Unit, CFE> = EndPipelineElement(layer)

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