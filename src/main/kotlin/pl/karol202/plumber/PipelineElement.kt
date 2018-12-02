package pl.karol202.plumber

internal interface PipelineElement<I, O, PI, PO>
{
	val firstElement: PipelineElement<PI, *, PI, PO>
	val lastElement: PipelineElement<*, PO, PI, PO>

	fun transformForward(input: I): PO

	fun transformBackward(input: O): PI

	fun copy(): PipelineElement<I, O, PI, PO>
}

internal interface PipelineElementWithPredecessor<I, O, PI, PO> :
		PipelineElement<I, O, PI, PO>
{
	var previousElement: PipelineElement<*, I, PI, PO>

	override fun copy(): PipelineElementWithPredecessor<I, O, PI, PO>

	fun <CPI> copyWithNewPI(): PipelineElementWithPredecessor<I, O, CPI, PO>
}

internal class FirstPipelineElement<O, PO>(private val layer: FirstLayer<O>,
                                           private val nextElement: PipelineElementWithPredecessor<O, *, Unit, PO>) :
		PipelineElement<Unit, O, Unit, PO>
{
	override val firstElement: PipelineElement<Unit, *, Unit, PO>
		get() = this
	override val lastElement: PipelineElement<*, PO, Unit, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: Unit) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = layer.transformBackward(input)

	override fun copy() = FirstPipelineElement(layer, nextElement.copy())
}

internal class MiddlePipelineElement<I, O, PI, PO>(private val layer: MiddleLayer<I, O>,
                                                   private val nextElement: PipelineElementWithPredecessor<O, *, PI, PO>) :
		PipelineElementWithPredecessor<I, O, PI, PO>
{
	private var _previousElement: PipelineElement<*, I, PI, PO>? = null
	override var previousElement: PipelineElement<*, I, PI, PO>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: PipelineElement<PI, *, PI, PO>
		get() = previousElement.firstElement
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: I) = nextElement.transformForward(layer.transformForward(input))

	override fun transformBackward(input: O) = previousElement.transformBackward(layer.transformBackward(input))

	override fun copy() = MiddlePipelineElement(layer, nextElement.copy())

	override fun <CPI> copyWithNewPI() = MiddlePipelineElement(layer, nextElement.copyWithNewPI<CPI>())
}

internal class LastPipelineElement<I, PI>(private val layer: LastLayer<I>) :
		PipelineElementWithPredecessor<I, Unit, PI, Unit>
{
	private var _previousElement: PipelineElement<*, I, PI, Unit>? = null
	override var previousElement: PipelineElement<*, I, PI, Unit>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: PipelineElement<PI, *, PI, Unit>
		get() = previousElement.firstElement
	override val lastElement: PipelineElement<*, Unit, PI, Unit>
		get() = this

	override fun transformForward(input: I) = layer.transformForward(input)

	override fun transformBackward(input: Unit) = previousElement.transformBackward(layer.transformBackward(input))

	override fun copy() = LastPipelineElement<I, PI>(layer)

	override fun <CPI> copyWithNewPI() = LastPipelineElement<I, CPI>(layer)
}

internal class StartPipelineTerminator<PI, PO>(private val nextElement: PipelineElementWithPredecessor<PI, *, PI, PO>) :
		PipelineElement<PI, PI, PI, PO>
{
	override val firstElement: PipelineElement<PI, *, PI, PO>
		get() = this
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = nextElement.lastElement

	init
	{
		nextElement.previousElement = this
	}

	override fun transformForward(input: PI) = nextElement.transformForward(input)

	override fun transformBackward(input: PI) = input

	override fun copy() = StartPipelineTerminator(nextElement.copy())

	//fun copyAndInsertAtBeginning()
}

internal class EndPipelineTerminator<PI, PO> :
		PipelineElementWithPredecessor<PO, PO, PI, PO>
{
	private var _previousElement: PipelineElement<*, PO, PI, PO>? = null
	override var previousElement: PipelineElement<*, PO, PI, PO>
		get() = _previousElement ?: throw IllegalStateException("Element has no predecessor.")
		set(value)
		{
			if(_previousElement != null) throw IllegalStateException("Previous element already assigned.")
			_previousElement = value
		}

	override val firstElement: PipelineElement<PI, *, PI, PO>
		get() = previousElement.firstElement
	override val lastElement: PipelineElement<*, PO, PI, PO>
		get() = this

	override fun transformForward(input: PO) = input

	override fun transformBackward(input: PO) = previousElement.transformBackward(input)

	override fun copy() = EndPipelineTerminator<PI, PO>()

	override fun <CPI> copyWithNewPI() = EndPipelineTerminator<CPI, PO>()
}