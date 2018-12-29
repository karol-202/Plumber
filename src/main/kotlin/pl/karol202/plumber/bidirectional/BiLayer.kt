package pl.karol202.plumber.bidirectional

import pl.karol202.plumber.PublicApi
import pl.karol202.plumber.unidirectional.*

/**
 * Layer representing any operation which can have input and/or output.
 * Every layer can transform data forwards (data of input type to output type) and backwards (data of output type to input type).
 * BiLayer should not be implemented directly but through one of interfaces: FirstBiLayer, MiddleBiLayer, LastBiLayer.
 *
 * Generic types:
 * - I - input type
 * - O - output type
*/
@PublicApi
interface BiLayer<I, O> : Layer<I, O>
{
    /**
     * Transforms data from output to input type.
     */
    @PublicApi
	fun transformBack(input: O): I
}

/**
 * First layer is layer creating data (for example from IO operations). It can be only a start of a pipeline.
 */
@PublicApi
interface FirstBiLayer<O> : BiLayer<Unit, O>, FirstLayer<O>

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start part or end part.
 */
@PublicApi
interface MiddleBiLayer<I, O> : BiLayer<I, O>, MiddleLayer<I, O>

/**
 * Last layer is layer consuming data. It can be only an end of a pipeline.
 */
@PublicApi
interface LastBiLayer<I> : BiLayer<I, Unit>, LastLayer<I>

/**
 * Following methods are '+' operator overloads and are used to join layers with other layers
 * or pipelines in order to create new pipelines.
 */
operator fun <O, NO> FirstBiLayer<O>.plus(rightPipeline: OpenBiPipeline<O, NO>): LeftClosedBiPipeline<NO, O> =
		LeftClosedBiPipeline.fromLayer(this) + rightPipeline

operator fun <O, LEI> FirstBiLayer<O>.plus(rightPipeline: RightClosedBiPipeline<O, LEI>): ClosedBiPipeline =
		LeftClosedBiPipeline.fromLayer(this) + rightPipeline

operator fun <O, NO> FirstBiLayer<O>.plus(rightLayer: MiddleBiLayer<O, NO>): LeftClosedBiPipeline<NO, O> =
		LeftClosedBiPipeline.fromLayer(this) + OpenBiPipeline.fromLayer(rightLayer)

operator fun <O> FirstBiLayer<O>.plus(rightLayer: LastBiLayer<O>): ClosedBiPipeline =
		LeftClosedBiPipeline.fromLayer(this) + RightClosedBiPipeline.fromLayer(rightLayer)

operator fun <I, O, NO> MiddleBiLayer<I, O>.plus(rightPipeline: OpenBiPipeline<O, NO>): OpenBiPipeline<I, NO> =
		OpenBiPipeline.fromLayer(this) + rightPipeline

operator fun <I, O, LEI> MiddleBiLayer<I, O>.plus(rightPipeline: RightClosedBiPipeline<O, LEI>): RightClosedBiPipeline<I, LEI> =
		OpenBiPipeline.fromLayer(this) + rightPipeline

operator fun <I, O, NO> MiddleBiLayer<I, O>.plus(rightLayer: MiddleBiLayer<O, NO>): OpenBiPipeline<I, NO> =
		OpenBiPipeline.fromLayer(this) + OpenBiPipeline.fromLayer(rightLayer)

operator fun <I, O> MiddleBiLayer<I, O>.plus(rightLayer: LastBiLayer<O>): RightClosedBiPipeline<I, O> =
		OpenBiPipeline.fromLayer(this) + RightClosedBiPipeline.fromLayer(rightLayer)

operator fun <O, NO> FirstBiLayer<O>.plus(rightLayer: MiddleLayer<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

operator fun <O> FirstBiLayer<O>.plus(rightLayer: LastLayer<O>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)
