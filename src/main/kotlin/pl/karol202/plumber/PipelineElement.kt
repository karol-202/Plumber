package pl.karol202.plumber

data class CopyingData<out S, L>(val self: S,
								 val lastBeforeInserted: () -> L)

interface PipelineElement<I, O, PI, PO, FEO, LEI,
						  out FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
						  out LE : LastPipelineElement<LEI, PI, PO, FEO, FE>>
{
	val firstElement: FE
	val lastElement: LE

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

interface PipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI,
										 out FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
										 out LE : LastPipelineElement<LEI, PI, PO, FEO, FE>> :
		PipelineElement<I, O, PI, PO, FEO, LEI, FE, LE>
{
	val previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI, FE, LE>

	fun <CPO, CLEI,
		 CFE : FirstPipelineElement<FEO, PI, CPO, CLEI, CLE>,
		 CLE : LastPipelineElement<CLEI, PI, CPO, FEO, CFE>>
			copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, FEO, CLEI, CFE, CLE>,
	                       elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, FEO, CLEI, CFE, CLE>):
			CopyingData<PipelineElementWithPredecessor<I, *, PI, CPO, FEO, CLEI, CFE, CLE>, PipelineElementWithSuccessor<*, PO, PI, CPO, FEO, CLEI, CFE, CLE>>
}

interface PipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI,
									   out FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
									   out LE : LastPipelineElement<LEI, PI, PO, FEO, FE>> :
		PipelineElement<I, O, PI, PO, FEO, LEI, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI, FE, LE>

	fun <CPI, CFEO,
		 CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>,
		 CLE : LastPipelineElement<LEI, CPI, PO, CFEO, CFE>>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFEO, LEI, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFEO, LEI, CFE, CLE>):
			CopyingData<PipelineElementWithSuccessor<*, O, CPI, PO, CFEO, LEI, CFE, CLE>,
						PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, LEI, CFE, CLE>>
}

interface FirstPipelineElement<O, PI, PO, LEI,
							   out LE : LastPipelineElement<LEI, PI, PO, O, FirstPipelineElement<O, PI, PO, LEI, LE>>> :
		PipelineElementWithSuccessor<PI, O, PI, PO, O, LEI, FirstPipelineElement<O, PI, PO, LEI, LE>, LE>
{
	fun <CPO, CLEI,
		 CLE : LastPipelineElement<CLEI, PI, CPO, O, FirstPipelineElement<O, PI, CPO, CLEI, CLE>>>
			copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, *, PI, CPO, O, CLEI, FirstPipelineElement<O, PI, CPO, CLEI, CLE>, CLE>):
			CopyingData<FirstPipelineElement<O, PI, CPO, CLEI, CLE>,
						PipelineElementWithSuccessor<*, PO, PI, CPO, O, CLEI, FirstPipelineElement<O, PI, CPO, CLEI, CLE>, CLE>>
}

interface LastPipelineElement<I, PI, PO, FEO,
							  out FE : FirstPipelineElement<FEO, PI, PO, I, LastPipelineElement<I, PI, PO, FEO, FE>>> :
		PipelineElementWithPredecessor<I, PO, PI, PO, FEO, I, FE, LastPipelineElement<I, PI, PO, FEO, FE>>
{
	fun <CPI, CFEO,
		 CFE : FirstPipelineElement<CFEO, CPI, PO, I, LastPipelineElement<I, CPI, PO, CFEO, CFE>>>
			copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<*, PI, CPI, PO, CFEO, I, CFE, LastPipelineElement<I, CPI, PO, CFEO, CFE>>):
			CopyingData<LastPipelineElement<I, CPI, PO, CFEO, CFE>,
						PipelineElementWithPredecessor<PI, *, CPI, PO, CFEO, I, CFE, LastPipelineElement<I, CPI, PO, CFEO, CFE>>>
}
