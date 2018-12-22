package pl.karol202.plumber

data class ForwardCopyingData<SI, SO, PI, PO, FEO, LEI,
							  FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
							  LE : LastPipelineElement<LEI, PI, PO, FEO, FE>,
							  out ST : PipelineElement<SI, SO, PI, PO, FEO, LEI, FE, LE>,
							  BO>
		(val self: ST,
		 val lastBeforeInserted: () -> PipelineElementWithSuccessor<*, BO, PI, PO, FEO, LEI, FE, LE>)

data class BackwardCopyingData<SI, SO, PI, PO, FEO, LEI,
							   FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
							   LE : LastPipelineElement<LEI, PI, PO, FEO, FE>,
							   out ST : PipelineElement<SI, SO, PI, PO, FEO, LEI, FE, LE>,
							   BI>
		(val self: ST,
		 val lastBeforeInserted: () -> PipelineElementWithPredecessor<BI, *, PI, PO, FEO, LEI, FE, LE>)

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
		 CLE : LastPipelineElement<CLEI, PI, CPO, FEO, CFE>,
		 IO> copyWithNewPO(previousElementSupplier: () -> PipelineElementWithSuccessor<*, I, PI, CPO, FEO, CLEI, CFE, CLE>,
	                       elementToInsertAtEnd: PipelineElementWithPredecessor<PO, IO, PI, CPO, FEO, CLEI, CFE, CLE>):
			ForwardCopyingData<I, *, PI, CPO, FEO, CLEI, CFE, CLE, PipelineElementWithPredecessor<I, *, PI, CPO, FEO, CLEI, CFE, CLE>, PO>
}

interface PipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI,
									   out FE : FirstPipelineElement<FEO, PI, PO, LEI, LE>,
									   out LE : LastPipelineElement<LEI, PI, PO, FEO, FE>> :
		PipelineElement<I, O, PI, PO, FEO, LEI, FE, LE>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI, FE, LE>

	fun <CPI, CFEO,
		 CFE : FirstPipelineElement<CFEO, CPI, PO, LEI, CLE>,
		 CLE : LastPipelineElement<LEI, CPI, PO, CFEO, CFE>,
		 II>
			copyBackwardsWithNewPI(nextElement: PipelineElementWithPredecessor<O, *, CPI, PO, CFEO, LEI, CFE, CLE>,
			                       elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<II, PI, CPI, PO, CFEO, LEI, CFE, CLE>):
			BackwardCopyingData<*, O, CPI, PO, CFEO, LEI, CFE, CLE, PipelineElementWithSuccessor<*, O, CPI, PO, CFEO, LEI, CFE, CLE>, PI>
}

interface FirstPipelineElement<O, PI, PO, LEI,
							   out LE : LastPipelineElement<LEI, PI, PO, O, FirstPipelineElement<O, PI, PO, LEI, LE>>> :
		PipelineElementWithSuccessor<PI, O, PI, PO, O, LEI, FirstPipelineElement<O, PI, PO, LEI, LE>, LE>
{
	fun <CPO, CLEI,
		 CLE : LastPipelineElement<CLEI, PI, CPO, O, FirstPipelineElement<O, PI, CPO, CLEI, CLE>>,
		 IO> copyWithNewPO(elementToInsertAtEnd: PipelineElementWithPredecessor<PO, IO, PI, CPO, O, CLEI, FirstPipelineElement<O, PI, CPO, CLEI, CLE>, CLE>):
			ForwardCopyingData<PI, O, PI, CPO, O, CLEI, FirstPipelineElement<O, PI, CPO, CLEI, CLE>, CLE, FirstPipelineElement<O, PI, CPO, CLEI, CLE>, PO>
}

interface LastPipelineElement<I, PI, PO, FEO,
							  out FE : FirstPipelineElement<FEO, PI, PO, I, LastPipelineElement<I, PI, PO, FEO, FE>>> :
		PipelineElementWithPredecessor<I, PO, PI, PO, FEO, I, FE, LastPipelineElement<I, PI, PO, FEO, FE>>
{
	fun <CPI, CFEO,
		 CFE : FirstPipelineElement<CFEO, CPI, PO, I, LastPipelineElement<I, CPI, PO, CFEO, CFE>>,
		 II> copyBackwardsWithNewPI(elementToInsertAtStartSupplier: () -> PipelineElementWithSuccessor<II, PI, CPI, PO, CFEO, I, CFE, LastPipelineElement<I, CPI, PO, CFEO, CFE>>):
			BackwardCopyingData<*, PO, CPI, PO, CFEO, I, CFE, LastPipelineElement<I, CPI, PO, CFEO, CFE>, LastPipelineElement<I, CPI, PO, CFEO, CFE>, PI>
}
