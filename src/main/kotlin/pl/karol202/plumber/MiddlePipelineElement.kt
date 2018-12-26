package pl.karol202.plumber

class MiddlePipelineElement<I, O, PI, PO, FEO, LEI,
							out FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
							out LE : LastPipelineElement<LEI, PI, PO, FEO, FE>>
	    (private val layer: MiddleLayer<I, O>,
	     previousElementSupplier: (MiddlePipelineElement<I, O, PI, PO, FEO, LEI, FE, LE>) -> PipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI, FE, LE>,
	     override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI, FE, LE>) :
		PipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI, FE, LE>,
		PipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI, FE, LE>
{
	override val previousElement by lazy { previousElementSupplier(this) }

	override val firstElement: FE
		get() = previousElement.firstElement
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI, CFEO,
				  CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>,
				  CLE : LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFEO, LEI, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFEO, LEI, CFE, CLE>):
			CopyingData<MiddlePipelineElement<I, O, CPI, PO, CFEO, LEI, CFE, CLE>,
						PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, CLE>>
	{
		var previousData: CopyingData<PipelineElementWithSuccessor<*, I, CPI, PO, CFEO, LEI, CFE, CLE>,
									  PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, CLE>>? = null
		val current = MiddlePipelineElement<I, O, CPI, PO, CFEO, LEI, CFE, CLE>(layer, {
			previousElement.copyBackwardsWithNewPI(it, elementToInsertAtStartSupplier).also { previousData = it }.self
		}, nextElement)
		return CopyingData(current) { previousData?.lastBeforeInserted?.invoke() ?: throw PipelineException("Not available.") }
	}

	override fun <CPO, CLEI,
				  CFE : FirstPipelineElement<FEO, PI, CPO, CLEI, CLE>,
				  CLE : LastPipelineElement<CLEI, PI, CPO, FEO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, FEO, CLEI, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, FEO, CLEI, CFE, CLE>):
			CopyingData<MiddlePipelineElement<I, O, PI, CPO, FEO, CLEI, CFE, CLE>,
						PipelineElementWithSuccessor<*, PO, PI, CPO, FEO, CLEI, CFE, CLE>>
	{
		var current: MiddlePipelineElement<I, O, PI, CPO, FEO, CLEI, CFE, CLE>? = null
		val nextData = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = MiddlePipelineElement(layer, { previousElementSupplier() }, nextData.self)
		return CopyingData(current, nextData.lastBeforeInserted)
	}
}
