package pl.karol202.plumber

class StartPipelineElement<O, PO, LEI,
						   out LE : LastPipelineElement<LEI, Unit, PO, O, FirstPipelineElement<O, Unit, PO, LEI, LE>>>
		(private val layer: FirstLayer<O>,
		 override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO, O, LEI, FirstPipelineElement<O, Unit, PO, LEI, LE>, LE>) :
		FirstPipelineElement<O, Unit, PO, LEI, LE>
{
	override val firstElement: StartPipelineElement<O, PO, LEI, LE>
		get() = this
	override val lastElement: LE
		get() = nextElement.lastElement

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun <CPI, CFEO,
				  CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>,
				  CLE : LastPipelineElement<LEI, CPI, PO, CFEO, CFE>,
				  II>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFEO, LEI, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<II, Unit, CPI, PO, CFEO, LEI, CFE, CLE>) =
			throw PipelineException("Cannot change PI of first element.")

	override fun <CPO, CLEI,
				  CLE : LastPipelineElement<CLEI, Unit, CPO, O, FirstPipelineElement<O, Unit, CPO, CLEI, CLE>>,
				  IO>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, IO, Unit, CPO, O, CLEI, FirstPipelineElement<O, Unit, CPO, CLEI, CLE>, CLE>):
			ForwardCopyingData<Unit, O, Unit, CPO, FirstPipelineElement<O, Unit, CPO, CLEI, CLE>, CLE, StartPipelineElement<O, CPO, CLEI, CLE>, PO>
	{
		var current: StartPipelineElement<O, CPO, CLEI, CLE>? = null
		val nextData = nextElement.copyWithNewPO({ current ?: throw PipelineException("Not created yet.") }, elementToInsertAtEnd)
		current = StartPipelineElement(layer, nextData.self)
		return ForwardCopyingData(current, nextData.lastBeforeInserted)
	}
}
