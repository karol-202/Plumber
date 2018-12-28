package pl.karol202.plumber

internal class LatePreviousElement<T> : LateVal<T>(IllegalStateException("Element has no predecessor."),
												   IllegalStateException("Previous element already assigned."))

internal interface PipelineElement<I, O, PI, PO, FEO, LEI>
{
	val firstElement: PipelineElement<PI, FEO, PI, PO, FEO, LEI>
	val lastElement: PipelineElement<LEI, PO, PI, PO, FEO, LEI>

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI
}

internal interface PipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI> : PipelineElement<I, O, PI, PO, FEO, LEI>
{
	var previousElement: PipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI>

	fun <CPI, CFEO> copyWithNewPI(): PipelineElementWithPredecessor<I, O, CPI, PO, CFEO, LEI>
}

internal interface PipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI> : PipelineElement<I, O, PI, PO, FEO, LEI>
{
	val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI>

	fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, PI, CPO, FEO, CLEI>):
			PipelineElementWithSuccessor<I, O, PI, CPO, FEO, CLEI>
}

internal class FirstPipelineElement<O, PO, LEI>(private val layer: FirstLayer<O>,
                                                override val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO, O, LEI>) :
		PipelineElementWithSuccessor<Unit, O, Unit, PO, O, LEI>
{
	override val firstElement: FirstPipelineElement<O, PO, LEI>
		get() = this
	override val lastElement: PipelineElement<LEI, PO, Unit, PO, O, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, Unit, CPO, O, CLEI>) =
		FirstPipelineElement(layer, nextElement)
}

internal class MiddlePipelineElement<I, O, PI, PO, FEO, LEI>(private val layer: MiddleLayer<I, O>,
                                                             override val nextElement: PipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI>) :
		PipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI>,
		PipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI>
{
	override var previousElement by LatePreviousElement<PipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI>>()

	override val firstElement: PipelineElement<PI, FEO, PI, PO, FEO, LEI>
		get() = previousElement.firstElement
	override val lastElement: PipelineElement<LEI, PO, PI, PO, FEO, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI, CFEO> copyWithNewPI() = MiddlePipelineElement(layer, nextElement.copyWithNewPI<CPI, CFEO>())

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<O, *, PI, CPO, FEO, CLEI>) =
			MiddlePipelineElement(layer, nextElement)
}

internal class LastPipelineElement<I, PI, FEO>(private val layer: LastLayer<I>) :
		PipelineElementWithPredecessor<I, Unit, PI, Unit, FEO, I>
{
	override var previousElement by LatePreviousElement<PipelineElementWithSuccessor<*, I, PI, Unit, FEO, I>>()

	override val firstElement: PipelineElement<PI, FEO, PI, Unit, FEO, I>
		get() = previousElement.firstElement
	override val lastElement: LastPipelineElement<I, PI, FEO>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun <CPI, CFEO> copyWithNewPI() = LastPipelineElement<I, CPI, CFEO>(layer)
}

internal class StartPipelineTerminator<PI, PO, LEI>(override val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO, PI, LEI>) :
		PipelineElementWithSuccessor<PI, PI, PI, PO, PI, LEI>
{
	override val firstElement: StartPipelineTerminator<PI, PO, LEI>
		get() = this
	override val lastElement: PipelineElement<LEI, PO, PI, PO, PI, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: PipelineElementWithPredecessor<PI, *, PI, CPO, PI, CLEI>) =
			StartPipelineTerminator(nextElement)
}

internal class EndPipelineTerminator<PI, PO, FEO> : PipelineElementWithPredecessor<PO, PO, PI, PO, FEO, PO>
{
	override var previousElement by LatePreviousElement<PipelineElementWithSuccessor<*, PO, PI, PO, FEO, PO>>()

	override val firstElement: PipelineElement<PI, FEO, PI, PO, FEO, PO>
		get() = previousElement.firstElement
	override val lastElement: EndPipelineTerminator<PI, PO, FEO>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun <CPI, CFEO> copyWithNewPI() = EndPipelineTerminator<CPI, PO, CFEO>()
}
