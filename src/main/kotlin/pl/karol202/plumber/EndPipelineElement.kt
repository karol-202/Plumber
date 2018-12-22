package pl.karol202.plumber

class EndPipelineElement<I, PI,
					     out FE : FirstPipelineElement<*, PI, Unit, LastPipelineElement<I, PI, Unit, FE>>>
		(private val layer: LastLayer<I>,
		 previousElementSupplier: (EndPipelineElement<I, PI, FE>) -> PipelineElementWithSuccessor<*, I, PI, Unit, FE, LastPipelineElement<I, PI, Unit, FE>>) :
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
				  CLE : LastPipelineElement<*, PI, CPO, CFE>,
				  IO>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, CFE, CLE>,
			              elementToInsertAtEnd: PipelineElementWithPredecessor<Unit, IO, PI, CPO, CFE, CLE>) =
			throw PipelineException("Cannot change PO of last element.")

	override fun <CPI,
				  CFE : FirstPipelineElement<*, CPI, Unit, LastPipelineElement<I, CPI, Unit, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, Unit, CFE, LastPipelineElement<I, CPI, Unit, CFE>>) =
			EndPipelineElement<I, CPI, CFE>(layer) { previousElement.copyBackwardsWithNewPI(it, elementToInsertAtStartSupplier) }
}
