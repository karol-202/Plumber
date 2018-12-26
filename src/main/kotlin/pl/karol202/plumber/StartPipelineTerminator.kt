package pl.karol202.plumber

class StartPipelineTerminator<PI, PO, LEI,
							  out LE : LastPipelineElement<LEI, PI, PO, PI, FirstPipelineElement<PI, PI, PO, LEI, LE>>>
		(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO, PI, LEI, FirstPipelineElement<PI, PI, PO, LEI, LE>, LE>) :
		FirstPipelineElement<PI, PI, PO, LEI, LE>
{
	override val firstElement: StartPipelineTerminator<PI, PO, LEI, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun <CPI, CFEO,
				  CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>,
				  CLE : LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFEO, LEI, CFE, CLE>) =
		CopyingData(elementToInsertAtStartSupplier()) { nextElement }

	override fun <CPO, CLEI,
				  CLE : LastPipelineElement<CLEI, PI, CPO, PI, FirstPipelineElement<PI, PI, CPO, CLEI, CLE>>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, PI, CLEI, FirstPipelineElement<PI, PI, CPO, CLEI, CLE>, CLE>):
			CopyingData<StartPipelineTerminator<PI, CPO, CLEI, CLE>,
						PipelineElementWithSuccessor<*, PO, PI, CPO, PI, CLEI, FirstPipelineElement<PI, PI, CPO, CLEI, CLE>, CLE>>
	{
		var current: StartPipelineTerminator<PI, CPO, CLEI, CLE>? = null
		val nextData = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = StartPipelineTerminator(nextData.self)
		return CopyingData(current, nextData.lastBeforeInserted)
	}

	fun <CPI, CFEO,
		 OFE : FirstPipelineElement<CFEO, CPI, PI, PI, EndPipelineTerminator<CPI, PI, CFEO, OFE>>,
		 CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>
		 /*CLE : LastPipelineElement<LEI, CPI, PO, CFEO, FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>>*/>
			insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI, CFEO, OFE>):
			FirstPipelineElement<CFEO, CPI, PO, LEI, LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>
	{
		var newLastData: CopyingData<LastPipelineElement<LEI, CPI, PO, CFEO, CFE>,
									 PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>>? = null
		newLastData = lastElement.copyBackwardsWithNewPI<CPI, CFEO, CFE> {
			endPipelineTerminator.firstElement.copyWithNewPO(newLastData!!.lastBeforeInserted()).lastBeforeInserted()
		}
		return newLastData.self.firstElement
	}
}
