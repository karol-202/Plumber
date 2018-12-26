package pl.karol202.plumber

class EndPipelineTerminator<PI, PO, FEO,
							out FE : FirstPipelineElement<FEO, PI, PO, PO, LastPipelineElement<PO, PI, PO, FEO, FE>>>
		(previousElementSupplier: (EndPipelineTerminator<PI, PO, FEO, FE>) -> PipelineElementWithSuccessor<*, PO, PI, PO, FEO, PO, FE, LastPipelineElement<PO, PI, PO, FEO, FE>>) :
		LastPipelineElement<PO, PI, PO, FEO, FE>
{
	override val previousElement by lazy { previousElementSupplier(this) }

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: EndPipelineTerminator<PI, PO, FEO, FE>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun <CPI, CFEO,
				  CFE : FirstPipelineElement<CFEO, CPI, PO, PO, LastPipelineElement<PO, CPI, PO, CFEO, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFEO, PO, CFE, LastPipelineElement<PO, CPI, PO, CFEO, CFE>>):
			CopyingData<EndPipelineTerminator<CPI, PO, CFEO, CFE>,
						PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, PO, CFE, LastPipelineElement<PO, CPI, PO, CFEO, CFE>>>
	{
		var previousData: CopyingData<PipelineElementWithSuccessor<*, PO, CPI, PO, CFEO, PO, CFE, LastPipelineElement<PO, CPI, PO, CFEO, CFE>>,
									  PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, PO, CFE, LastPipelineElement<PO, CPI, PO, CFEO, CFE>>>? = null
		val current = EndPipelineTerminator<CPI, PO, CFEO, CFE> { endPipelineElement ->
			previousElement.copyBackwardsWithNewPI(endPipelineElement, elementToInsertAtStartSupplier).also { previousData = it }.self
		}
		return CopyingData(current) { previousData?.lastBeforeInserted?.invoke() ?: throw PipelineException("Not available.") }
	}

	override fun <CPO, CLEI,
				  CFE : FirstPipelineElement<FEO, PI, CPO, CLEI, CLE>,
				  CLE : LastPipelineElement<CLEI, PI, CPO, FEO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, PO, PI, CPO, FEO, CLEI, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, FEO, CLEI, CFE, CLE>) =
			CopyingData(elementToInsertAtEnd, previousElementSupplier)

	/*fun <CPO> insertAtEndAndReturnBeginning(startPipelineTerminator: StartPipelineTerminator<PO, CPO>):
			PipelineElement<PI, *, PI, CPO>
	{
		val newNext = startPipelineTerminator.nextElement.copyWithNewPI<PI>()
		val newPrevious = previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}*/
}
