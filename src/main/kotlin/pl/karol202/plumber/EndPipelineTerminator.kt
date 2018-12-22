package pl.karol202.plumber

class EndPipelineTerminator<PI, PO,
							out FE : FirstPipelineElement<*, PI, PO, LastPipelineElement<PO, PI, PO, FE>>>
		(previousElementSupplier: (EndPipelineTerminator<PI, PO, FE>) -> PipelineElementWithSuccessor<*, PO, PI, PO, FE, LastPipelineElement<PO, PI, PO, FE>>) :
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
				  CLE : LastPipelineElement<*, PI, CPO, CFE>,
				  IO>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, PO, PI, CPO, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<PO, IO, PI, CPO, CFE, CLE>) =
			ForwardCopyingData(elementToInsertAtEnd, previousElementSupplier)

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, PO, LastPipelineElement<PO, CPI, PO, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFE, LastPipelineElement<PO, CPI, PO, CFE>>) =
			EndPipelineTerminator<CPI, PO, CFE> { previousElement.copyBackwardsWithNewPI(it, elementToInsertAtStartSupplier) }

	/*fun <CPO> insertAtEndAndReturnBeginning(startPipelineTerminator: StartPipelineTerminator<PO, CPO>):
			PipelineElement<PI, *, PI, CPO>
	{
		val newNext = startPipelineTerminator.nextElement.copyWithNewPI<PI>()
		val newPrevious = previousElement.copyBackwardsWithNewPO(newNext)
		return newPrevious.firstElement
	}*/
}
