package pl.karol202.plumber

interface PipelineElement<I, O, PI, PO,
						  out FE : FirstPipelineElement<*, PI, PO, LE>,
						  out LE : LastPipelineElement<*, PI, PO, FE>>
{
	val firstElement: FE
	val lastElement: LE

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

interface PipelineElementWithPredecessor<I, O, PI, PO,
										 out FE : FirstPipelineElement<*, PI, PO, LE>,
										 out LE : LastPipelineElement<*, PI, PO, FE>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FE, LE>

	fun <CPO,
		 CFE : FirstPipelineElement<*, PI, CPO, CLE>,
		 CLE : LastPipelineElement<*, PI, CPO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>):
			PipelineElementWithPredecessor<I, *, PI, CPO, CFE, CLE>
}

interface PipelineElementWithSuccessor<I, O, PI, PO,
									   out FE : FirstPipelineElement<*, PI, PO, LE>,
									   out LE : LastPipelineElement<*, PI, PO, FE>> :
		PipelineElement<I, O, PI, PO, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FE, LE>

	fun <CPI,
		 CFE : FirstPipelineElement<*, CPI, PO, CLE>,
		 CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFE, CLE>):
			PipelineElementWithSuccessor<*, O, CPI, PO, CFE, CLE>
}

interface FirstPipelineElement<O, PI, PO,
							   out LE : LastPipelineElement<*, PI, PO, FirstPipelineElement<*, PI, PO, LE>>> :
		PipelineElementWithSuccessor<PI, O, PI, PO, FirstPipelineElement<*, PI, PO, LE>, LE>
{
	fun <CPO,
		 CLE : LastPipelineElement<*, PI, CPO, FirstPipelineElement<*, PI, CPO, CLE>>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, FirstPipelineElement<*, PI, CPO, CLE>, CLE>):
			FirstPipelineElement<*, PI, CPO, CLE>
}

interface LastPipelineElement<I, PI, PO,
							  out FE : FirstPipelineElement<*, PI, PO, LastPipelineElement<*, PI, PO, FE>>> :
		PipelineElementWithPredecessor<I, PO, PI, PO, FE, LastPipelineElement<*, PI, PO, FE>>
{
	fun <CPI,
		 CFE : FirstPipelineElement<*, CPI, PO, LastPipelineElement<*, CPI, PO, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFE, LastPipelineElement<*, CPI, PO, CFE>>):
			LastPipelineElement<*, CPI, PO, CFE>
}

class StartPipelineElement<O, PO,
						   out LE : LastPipelineElement<*, Unit, PO, FirstPipelineElement<*, Unit, PO, LE>>>
		(private val layer: FirstLayer<O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO, FirstPipelineElement<*, Unit, PO, LE>, LE>) :
		FirstPipelineElement<O, Unit, PO, LE>
{
	override val firstElement: StartPipelineElement<O, PO, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, CLE>,
				  CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, Unit, CPI, PO, CFE, CLE>) =
			throw PipelineException("Cannot change PI of first element.")

	override fun <CPO,
				  CLE : LastPipelineElement<*, Unit, CPO, FirstPipelineElement<*, Unit, CPO, CLE>>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, Unit, CPO, FirstPipelineElement<*, Unit, CPO, CLE>, CLE>):
			StartPipelineElement<O, CPO, CLE>
	{
		var current: StartPipelineElement<O, CPO, CLE>? = null
		val next = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = StartPipelineElement(layer, next)
		return current
	}
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
	override val previousElement by lazy { previousElementSupplier(this) }

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, CLE>,
				  CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFE, CLE>) =
			MiddlePipelineElement(layer, { previousElement.copyBackwardsWithNewPI(it, elementToInsertAtStartSupplier) }, nextElement)

	override fun <CPO,
				  CFE : FirstPipelineElement<*, PI, CPO, CLE>,
				  CLE : LastPipelineElement<*, PI, CPO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>):
			MiddlePipelineElement<I, O, PI, CPO, CFE, CLE>
	{
		var current: MiddlePipelineElement<I, O, PI, CPO, CFE, CLE>? = null
		val next = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = MiddlePipelineElement(layer, { previousElementSupplier() }, next)
		return current
	}
}

class EndPipelineElement<I, PI,
						 out FE : FirstPipelineElement<*, PI, Unit, LastPipelineElement<*, PI, Unit, FE>>>
		(private val layer: LastLayer<I>,
		 previousElementSupplier: (EndPipelineElement<I, PI, FE>) -> PipelineElementWithSuccessor<*, I, PI, Unit, FE, LastPipelineElement<*, PI, Unit, FE>>) :
		LastPipelineElement<I, PI, Unit, FE>
{
	override val previousElement by lazy { previousElementSupplier(this) }

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: EndPipelineElement<I, PI, FE>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPO,
			CFE : FirstPipelineElement<*, PI, CPO, CLE>,
			CLE : LastPipelineElement<*, PI, CPO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<Unit, *, PI, CPO, CFE, CLE>) =
			throw PipelineException("Cannot change PO of last element.")

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, Unit, LastPipelineElement<*, CPI, Unit, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, Unit, CFE, LastPipelineElement<*, CPI, Unit, CFE>>) =
			EndPipelineElement<I, CPI, CFE>(layer) { previousElement.copyBackwardsWithNewPI(it, elementToInsertAtStartSupplier) }
}

class StartPipelineTerminator<PI, PO,
							  out LE : LastPipelineElement<*, PI, PO, FirstPipelineElement<*, PI, PO, LE>>>
		(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO, FirstPipelineElement<*, PI, PO, LE>, LE>) :
		FirstPipelineElement<PI, PI, PO, LE>
{
	override val firstElement: StartPipelineTerminator<PI, PO, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, CLE>,
				  CLE : LastPipelineElement<*, CPI, PO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<PI, *, CPI, PO, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFE, CLE>) =
			elementToInsertAtStartSupplier()

	override fun <CPO,
				  CLE : LastPipelineElement<*, PI, CPO, FirstPipelineElement<*, PI, CPO, CLE>>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, FirstPipelineElement<*, PI, CPO, CLE>, CLE>):
			FirstPipelineElement<PI, PI, CPO, CLE>
	{
		var current: StartPipelineTerminator<PI, CPO, CLE>? = null
		val next = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = StartPipelineTerminator(next)
		return current
	}

	fun <CPI,
		 OFE : FirstPipelineElement<*, CPI, PI, EndPipelineTerminator<CPI, PI, OFE>>,
		 CFE : FirstPipelineElement<*, CPI, PO, CLE>,
		 CLE : LastPipelineElement<*, CPI, PO, CFE>>
			insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI, OFE>):
			FirstPipelineElement<*, CPI, PO, CLE>
	{
		var newLast: LastPipelineElement<*, CPI, PO, CFE>? = null
		newLast = lastElement.copyBackwardsWithNewPI<CPI, CFE> {			endPipelineTerminator.firstElement.copyWithNewPO<PO, CLE>(newLast ?: throw PipelineException("Not created yet."))
		}
		return newLast.firstElement
	}
}

class EndPipelineTerminator<PI, PO,
							out FE : FirstPipelineElement<*, PI, PO, LastPipelineElement<*, PI, PO, FE>>>
	   (previousElementSupplier: (EndPipelineTerminator<PI, PO, FE>) -> PipelineElementWithSuccessor<*, PO, PI, PO, FE, LastPipelineElement<*, PI, PO, FE>>) :
		LastPipelineElement<PO, PI, PO, FE>
{
	override val previousElement by lazy { previousElementSupplier(this) }

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: EndPipelineTerminator<PI, PO, FE>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun <CPO,
				  CFE : FirstPipelineElement<*, PI, CPO, CLE>,
				  CLE : LastPipelineElement<*, PI, CPO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, PO, PI, CPO, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, CFE, CLE>) =
			elementToInsertAtEnd

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, LastPipelineElement<*, CPI, PO, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFE, LastPipelineElement<*, CPI, PO, CFE>>) =
			EndPipelineTerminator<CPI, PO, CFE> { previousElement.copyBackwardsWithNewPI(it, elementToInsertAtStartSupplier) }

	/*fun <CPO> insertAtEndAndReturnBeginning(startPipelineTerminator: StartPipelineTerminator<PO, CPO>):
			PipelineElement<PI, *, PI, CPO>
	{
		val newNext = startPipelineTerminator.nextElement.copyWithNewPI<PI>()
		val newPrevious = previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}*/
}