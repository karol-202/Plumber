package pl.karol202.plumber.bidirectional

import pl.karol202.plumber.LatePreviousElement

/**
 * I - input of element
 * O - output of element
 * PI - input of pipeline
 * PO - output of pipeline
 * FEO - output of first element
 * LEI - input of last element
*/
internal interface BiPipelineElement<I, O, PI, PO, FEO, LEI>
{
	val firstElement: BiPipelineElement<PI, FEO, PI, PO, FEO, LEI>
	val lastElement: BiPipelineElement<LEI, PO, PI, PO, FEO, LEI>

	fun transform(input: I): PO

	fun transformBack(input: O): PI
}

internal interface BiPipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI> : BiPipelineElement<I, O, PI, PO, FEO, LEI>
{
	var previousElement: BiPipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI>

	fun <CPI, CFEO> copyWithNewPI(): BiPipelineElementWithPredecessor<I, O, CPI, PO, CFEO, LEI>
}

internal interface BiPipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI> : BiPipelineElement<I, O, PI, PO, FEO, LEI>
{
	val nextElement: BiPipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI>

	fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: BiPipelineElementWithPredecessor<O, *, PI, CPO, FEO, CLEI>):
			BiPipelineElementWithSuccessor<I, O, PI, CPO, FEO, CLEI>
}

internal class FirstBiPipelineElement<O, PO, LEI>(private val layer: FirstBiLayer<O>,
                                                  override val nextElement: BiPipelineElementWithPredecessor<O, *, Unit, PO, O, LEI>) :
		BiPipelineElementWithSuccessor<Unit, O, Unit, PO, O, LEI>
{
	override val firstElement: FirstBiPipelineElement<O, PO, LEI>
		get() = this
	override val lastElement: BiPipelineElement<LEI, PO, Unit, PO, O, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transform(input: Unit) = nextElement.transform(layer.transform(input))

	override fun transformBack(input: O) = layer.transformBack(input)

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: BiPipelineElementWithPredecessor<O, *, Unit, CPO, O, CLEI>) =
			FirstBiPipelineElement(layer, nextElement)
}

internal class MiddleBiPipelineElement<I, O, PI, PO, FEO, LEI>(private val layer: MiddleBiLayer<I, O>,
                                                               override val nextElement: BiPipelineElementWithPredecessor<O, *, PI, PO, FEO, LEI>) :
		BiPipelineElementWithPredecessor<I, O, PI, PO, FEO, LEI>,
		BiPipelineElementWithSuccessor<I, O, PI, PO, FEO, LEI>
{
	override var previousElement by LatePreviousElement<BiPipelineElementWithSuccessor<*, I, PI, PO, FEO, LEI>>()

	override val firstElement: BiPipelineElement<PI, FEO, PI, PO, FEO, LEI>
		get() = previousElement.firstElement
	override val lastElement: BiPipelineElement<LEI, PO, PI, PO, FEO, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transform(input: I) = nextElement.transform(layer.transform(input))

	override fun transformBack(input: O) = previousElement.transformBack(layer.transformBack(input))

	override fun <CPI, CFEO> copyWithNewPI() = MiddleBiPipelineElement(layer, nextElement.copyWithNewPI<CPI, CFEO>())

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: BiPipelineElementWithPredecessor<O, *, PI, CPO, FEO, CLEI>) =
			MiddleBiPipelineElement(layer, nextElement)
}

internal class LastBiPipelineElement<I, PI, FEO>(private val layer: LastBiLayer<I>) :
		BiPipelineElementWithPredecessor<I, Unit, PI, Unit, FEO, I>
{
	override var previousElement by LatePreviousElement<BiPipelineElementWithSuccessor<*, I, PI, Unit, FEO, I>>()

	override val firstElement: BiPipelineElement<PI, FEO, PI, Unit, FEO, I>
		get() = previousElement.firstElement
	override val lastElement: LastBiPipelineElement<I, PI, FEO>
		get() = this

	override fun transform(input: I) = layer.transform(input)

	override fun transformBack(input: Unit) = previousElement.transformBack(layer.transformBack(input))

	override fun <CPI, CFEO> copyWithNewPI() = LastBiPipelineElement<I, CPI, CFEO>(layer)
}

internal class StartBiPipelineTerminator<PI, PO, LEI>(override val nextElement: BiPipelineElementWithPredecessor<PI, *, PI, PO, PI, LEI>) :
		BiPipelineElementWithSuccessor<PI, PI, PI, PO, PI, LEI>
{
	override val firstElement: StartBiPipelineTerminator<PI, PO, LEI>
		get() = this
	override val lastElement: BiPipelineElement<LEI, PO, PI, PO, PI, LEI>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transform(input: PI) = nextElement.transform(input)

	override fun transformBack(input: PI) = input

	override fun <CPO, CLEI> copyBackwardsWithNewPO(nextElement: BiPipelineElementWithPredecessor<PI, *, PI, CPO, PI, CLEI>) =
			StartBiPipelineTerminator(nextElement)
}

internal class EndBiPipelineTerminator<PI, PO, FEO> : BiPipelineElementWithPredecessor<PO, PO, PI, PO, FEO, PO>
{
	override var previousElement by LatePreviousElement<BiPipelineElementWithSuccessor<*, PO, PI, PO, FEO, PO>>()

	override val firstElement: BiPipelineElement<PI, FEO, PI, PO, FEO, PO>
		get() = previousElement.firstElement
	override val lastElement: EndBiPipelineTerminator<PI, PO, FEO>
		get() = this

	override fun transform(input: PO) = input

	override fun transformBack(input: PO) = previousElement.transformBack(input)

	override fun <CPI, CFEO> copyWithNewPI() = EndBiPipelineTerminator<CPI, PO, CFEO>()
}
