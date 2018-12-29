package pl.karol202.plumber.unidirectional

import pl.karol202.plumber.LatePreviousElement

/**
 * I - input of element
 * O - output of element
 * PI - input of pipeline
 * PO - output of pipeline
 * FEO - output of first element
 * LEI - input of last element
*/
internal interface UniPipelineElement<I, O, PI, PO, FEO, LEI>
{
	val firstElement: UniPipelineElement<PI, FEO, PI, PO, FEO, LEI>
	val lastElement: UniPipelineElement<LEI, PO, PI, PO, FEO, LEI>

	fun transform(input: I): PO
}

internal interface UniPipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI> : UniPipelineElement<I, O, PI, PO, FEO, LEI>
{
	var previousElement: UniPipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI>

	fun <CPI, CFEO> copyWithNewPI(): UniPipelineElementWithPredecessor<I, O, CPI, PO, CFEO, LEI>
}

internal interface UniPipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI> : UniPipelineElement<I, O, PI, PO, FEO, LEI>
{
	val nextElement: UniPipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI>

	fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: UniPipelineElementWithPredecessor<O, *, PI, CPO, FEO, CLEI>):
			UniPipelineElementWithSuccessor<I, O, PI, CPO, FEO, CLEI>
}

internal class FirstUniPipelineElement<O, PO, LEI>(private val layer: FirstLayer<O>,
                                                   override val nextElement: UniPipelineElementWithPredecessor<O, *, Unit, PO, O, LEI>) :
		UniPipelineElementWithSuccessor<Unit, O, Unit, PO, O, LEI>
{
	override val firstElement: FirstUniPipelineElement<O, PO, LEI>
		get() = this
	override val lastElement: UniPipelineElement<LEI, PO, Unit, PO, O, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transform(input: Unit) = nextElement.transform(layer.transform(input))

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: UniPipelineElementWithPredecessor<O, *, Unit, CPO, O, CLEI>) =
			FirstUniPipelineElement(layer, nextElement)
}

internal class MiddleUniPipelineElement<I, O, PI, PO, FEO, LEI>(private val layer: MiddleLayer<I, O>,
                                                                override val nextElement: UniPipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI>) :
		UniPipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI>,
		UniPipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI>
{
	override var previousElement by LatePreviousElement<UniPipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI>>()

	override val firstElement: UniPipelineElement<PI, FEO, PI, PO, FEO, LEI>
		get() = previousElement.firstElement
	override val lastElement: UniPipelineElement<LEI, PO, PI, PO, FEO, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transform(input: I) = nextElement.transform(layer.transform(input))

	override fun <CPI, CFEO> copyWithNewPI() = MiddleUniPipelineElement(layer, nextElement.copyWithNewPI<CPI, CFEO>())

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: UniPipelineElementWithPredecessor<O, *, PI, CPO, FEO, CLEI>) =
			MiddleUniPipelineElement(layer, nextElement)
}

internal class LastUniPipelineElement<I, PI, FEO>(private val layer: LastLayer<I>) :
		UniPipelineElementWithPredecessor<I, Unit, PI, Unit, FEO, I>
{
	override var previousElement by LatePreviousElement<UniPipelineElementWithSuccessor<*, I, PI, Unit, FEO, I>>()

	override val firstElement: UniPipelineElement<PI, FEO, PI, Unit, FEO, I>
		get() = previousElement.firstElement
	override val lastElement: LastUniPipelineElement<I, PI, FEO>
		get() = this

	override fun transform(input: I) = layer.transform(input)

	override fun <CPI, CFEO> copyWithNewPI() = LastUniPipelineElement<I, CPI, CFEO>(layer)
}

internal class StartUniPipelineTerminator<PI, PO, LEI>(override val nextElement: UniPipelineElementWithPredecessor<PI, *, PI, PO, PI, LEI>) :
		UniPipelineElementWithSuccessor<PI, PI, PI, PO, PI, LEI>
{
	override val firstElement: StartUniPipelineTerminator<PI, PO, LEI>
		get() = this
	override val lastElement: UniPipelineElement<LEI, PO, PI, PO, PI, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transform(input: PI) = nextElement.transform(input)

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: UniPipelineElementWithPredecessor<PI, *, PI, CPO, PI, CLEI>) =
			StartUniPipelineTerminator(nextElement)
}

internal class EndUniPipelineTerminator<PI, PO, FEO> : UniPipelineElementWithPredecessor<PO, PO, PI, PO, FEO, PO>
{
	override var previousElement by LatePreviousElement<UniPipelineElementWithSuccessor<*, PO, PI, PO, FEO, PO>>()

	override val firstElement: UniPipelineElement<PI, FEO, PI, PO, FEO, PO>
		get() = previousElement.firstElement
	override val lastElement: EndUniPipelineTerminator<PI, PO, FEO>
		get() = this

	override fun transform(input: PO) = input

	override fun <CPI, CFEO> copyWithNewPI() = EndUniPipelineTerminator<CPI, PO, CFEO>()
}
