package pl.karol202.plumber.bidirectional

import pl.karol202.plumber.PublicApi

/**
 * Layer representing any operation which can have input and/or output.
 * Every layer can transform data forwards (data of input type to output type) and backwards (data of output type to input type).
 * BiLayer should not be implemented directly but through one of interfaces: FirstBiLayer, TransitiveBiLayer, LastBiLayer.
 *
 * Generic types:
 * - I - input type
 * - O - output type
*/
@PublicApi
interface BiLayer<I, O>
{
	/**
	 * Transforms data from input to output type.
	 */
	@PublicApi
	fun transform(input: I): O

    /**
     * Transforms data from output to input type.
     */
    @PublicApi
	fun transformBack(input: O): I
}

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start part or end part.
 */
@PublicApi
interface TransitiveBiLayer<I, O> : BiLayer<I, O>

/**
 * First layer is layer creating data (for example from IO operations). It can be only a start of a pipeline.
 */
@PublicApi
interface TerminalBiLayer<T> : BiLayer<T, Unit>

/**
 * Following methods are '+' operator overloads and are used to join layers with other layers
 * or pipelines in order to create new pipelines.
 */
@PublicApi
operator fun <O, NO> TerminalBiLayer<O>.plus(rightPipeline: OpenBiPipeline<O, NO>): LeftClosedBiPipeline<NO, O> =
		LeftClosedBiPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <O, LEI> TerminalBiLayer<O>.plus(rightPipeline: RightClosedBiPipeline<O, LEI>): ClosedBiPipeline =
		LeftClosedBiPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <O, NO> TerminalBiLayer<O>.plus(rightLayer: TransitiveBiLayer<O, NO>): LeftClosedBiPipeline<NO, O> =
		LeftClosedBiPipeline.fromLayer(this) + OpenBiPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <O> TerminalBiLayer<O>.plus(rightLayer: TerminalBiLayer<O>): ClosedBiPipeline =
		LeftClosedBiPipeline.fromLayer(this) + RightClosedBiPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <I, O, NO> TransitiveBiLayer<I, O>.plus(rightPipeline: OpenBiPipeline<O, NO>): OpenBiPipeline<I, NO> =
		OpenBiPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <I, O, LEI> TransitiveBiLayer<I, O>.plus(rightPipeline: RightClosedBiPipeline<O, LEI>): RightClosedBiPipeline<I, LEI> =
		OpenBiPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <I, O, NO> TransitiveBiLayer<I, O>.plus(rightLayer: TransitiveBiLayer<O, NO>): OpenBiPipeline<I, NO> =
		OpenBiPipeline.fromLayer(this) + OpenBiPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <I, O> TransitiveBiLayer<I, O>.plus(rightLayer: TerminalBiLayer<O>): RightClosedBiPipeline<I, O> =
		OpenBiPipeline.fromLayer(this) + RightClosedBiPipeline.fromLayer(rightLayer)
