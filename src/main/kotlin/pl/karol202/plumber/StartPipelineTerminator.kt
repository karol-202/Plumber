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
				 CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>,
				 CLE : LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>
			insertAtBeginningAndReturnBeginning(endPipelineTerminator: EndPipelineTerminator<CPI, PI, CFEO, OFE>): CFE
	{
		var newLastData: CopyingData<LastPipelineElement<LEI, CPI, PO, CFEO, CFE>,
				PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, CLE>>? = null
		newLastData = lastElement.copyBackwardsWithNewPI {
			val lastBeforeInserted = newLastData?.lastBeforeInserted?.invoke() ?: throw PipelineException("Not created yet.")
			endPipelineTerminator.firstElement.copyWithNewPO(lastBeforeInserted).lastBeforeInserted()
					as PipelineElementWithSuccessor<*, PI, CPI, PO, CFEO, LEI, CFE, CLE>
		} as CopyingData<LastPipelineElement<LEI, CPI, PO, CFEO, CFE>, PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, CLE>>
		return newLastData.self.firstElement
	}
}
