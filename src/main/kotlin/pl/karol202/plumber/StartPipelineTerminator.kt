package pl.karol202.plumber

class StartPipelineTerminator<PI, PO,
							  out LE : LastPipelineElement<LEI, PI, PO, FirstPipelineElement<PI, PI, PO, LE>>,
							  LEI>
		(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO, FirstPipelineElement<PI, PI, PO, LE>, LE>) :
		FirstPipelineElement<PI, PI, PO, LE>
{
	override val firstElement: StartPipelineTerminator<PI, PO, LE, LEI>
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
				  CLE : LastPipelineElement<*, PI, CPO, FirstPipelineElement<PI, PI, CPO, CLE>>,
				  IO>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, IO, PI, CPO, FirstPipelineElement<PI, PI, CPO, CLE>, CLE>):
			ForwardCopyingData<PI, PI, PI, CPO, FirstPipelineElement<PI, PI, CPO, CLE>, CLE, StartPipelineTerminator<PI, CPO, CLE, LEI>, PO>
	{
		var current: StartPipelineTerminator<PI, CPO, CLE>? = null
		val next = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = StartPipelineTerminator(next)
		return current
	}

	fun <CPI,
		 OFE : FirstPipelineElement<*, CPI, PI, EndPipelineTerminator<CPI, PI, OFE>>,
		 CFE : FirstPipelineElement<*, CPI, PO, CLE>,
		 CLE : LastPipelineElement<LEI, CPI, PO, CFE>>
			insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI, OFE>):
			FirstPipelineElement<*, CPI, PO, CLE>
	{
		var newLast: LastPipelineElement<LEI, CPI, PO, CFE>? = null
		newLast = lastElement.copyBackwardsWithNewPI<CPI, CFE> {
			endPipelineTerminator.firstElement.copyWithNewPO<PO, CLE, >(newLast ?: throw PipelineException("Not created yet.")).lastBeforeInserted()
		}
		return newLast.firstElement
	}
}
