package pl.karol202.plumber

class EndPipelineElement<I, PI, FEO,
					     out FE : FirstPipelineElement<FEO, PI, Unit, I, LastPipelineElement<I, PI, Unit, FEO, FE>>>
		(private val layer: LastLayer<I>,
		 previousElementSupplier: (EndPipelineElement<I, PI, FEO, FE>) -> PipelineElementWithSuccessor<*, I, PI, Unit, FEO, I, FE, LastPipelineElement<I, PI, Unit, FEO, FE>>) :
		LastPipelineElement<I, PI, Unit, FEO, FE>
{
	override val previousElement by lazy { previousElementSupplier(this) }

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: EndPipelineElement<I, PI, FEO, FE>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI, CFEO,
				  CFE : FirstPipelineElement<CFEO, CPI, Unit, I, LastPipelineElement<I, CPI, Unit, CFEO, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, Unit, CFEO, I, CFE, LastPipelineElement<I, CPI, Unit, CFEO, CFE>>):
			CopyingData<EndPipelineElement<I, CPI, CFEO, CFE>,
						PipelineElementWithPredecessor<PI, *, CPI, Unit, CFEO, I, CFE, LastPipelineElement<I, CPI, Unit, CFEO, CFE>>>
	{
		var previousData: CopyingData<PipelineElementWithSuccessor<*, I, CPI, Unit, CFEO, I, CFE, LastPipelineElement<I, CPI, Unit, CFEO, CFE>>,
									  PipelineElementWithPredecessor<PI, *, CPI, Unit, CFEO, I, CFE, LastPipelineElement<I, CPI, Unit, CFEO, CFE>>>? = null
		val current = EndPipelineElement<I, CPI, CFEO, CFE>(layer) { endPipelineElement ->
			previousElement.copyBackwardsWithNewPI(endPipelineElement, elementToInsertAtStartSupplier).also { previousData = it }.self
		}
		return CopyingData(current) { previousData?.lastBeforeInserted?.invoke() ?: throw PipelineException("Not available.") }
	}

	override fun <CPO, CLEI,
				  CFE : FirstPipelineElement<FEO, PI, CPO, CLEI, CLE>,
				  CLE : LastPipelineElement<CLEI, PI, CPO, FEO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, FEO, CLEI, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<Unit, *, PI, CPO, FEO, CLEI, CFE, CLE>) =
			throw PipelineException("Cannot change PO of last element.")
}
