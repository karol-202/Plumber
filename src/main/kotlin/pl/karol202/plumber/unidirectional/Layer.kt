package pl.karol202.plumber.unidirectional

import pl.karol202.plumber.PublicApi

@PublicApi
interface Layer<I, O>
{
	/**
	 * Transforms data from input to output type.
	 */
	@PublicApi
	fun transform(input: I): O
}

/**
 * First layer is layer creating data (for example from IO operations). It can be only a start of a pipeline.
 */
@PublicApi
interface FirstLayer<O> : Layer<Unit, O>

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start or end part.
 */
@PublicApi
interface MiddleLayer<I, O> : Layer<I, O>

/**
 * Last layer is layer consuming data. It can be only an end of a pipeline.
 */
@PublicApi
interface LastLayer<I> : Layer<I, Unit>

/**
 * Following methods are '+' operator overloads and are used to join layers with other layers
 * or pipelines in order to create new pipelines.
 */
operator fun <O, NO> FirstLayer<O>.plus(rightPipeline: OpenUniPipeline<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + rightPipeline

operator fun <O, LEI> FirstLayer<O>.plus(rightPipeline: RightClosedUniPipeline<O, LEI>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + rightPipeline

operator fun <O, NO> FirstLayer<O>.plus(rightLayer: MiddleLayer<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

operator fun <O> FirstLayer<O>.plus(rightLayer: LastLayer<O>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)

operator fun <I, O, NO> MiddleLayer<I, O>.plus(rightPipeline: OpenUniPipeline<O, NO>): OpenUniPipeline<I, NO> =
		OpenUniPipeline.fromLayer(this) + rightPipeline

operator fun <I, O, LEI> MiddleLayer<I, O>.plus(rightPipeline: RightClosedUniPipeline<O, LEI>): RightClosedUniPipeline<I, LEI> =
		OpenUniPipeline.fromLayer(this) + rightPipeline

operator fun <I, O, NO> MiddleLayer<I, O>.plus(rightLayer: MiddleLayer<O, NO>): OpenUniPipeline<I, NO> =
		OpenUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

operator fun <I, O> MiddleLayer<I, O>.plus(rightLayer: LastLayer<O>): RightClosedUniPipeline<I, O> =
		OpenUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)
