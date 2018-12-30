package pl.karol202.plumber.unidirectional

import pl.karol202.plumber.PublicApi
import pl.karol202.plumber.bidirectional.TerminalBiLayer
import pl.karol202.plumber.bidirectional.TransitiveBiLayer

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
interface CreatorLayer<O> : Layer<Unit, O>

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start or end part.
 */
@PublicApi
interface TransitiveLayer<I, O> : Layer<I, O>

/**
 * Last layer is layer consuming data. It can be only an end of a pipeline.
 */
@PublicApi
interface ConsumerLayer<I> : Layer<I, Unit>

/**
 * Following methods are '+' operator overloads and are used to join layers with other layers
 * or pipelines in order to create new pipelines.
 */
@PublicApi
operator fun <O, NO> CreatorLayer<O>.plus(rightPipeline: OpenUniPipeline<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <O, LEI> CreatorLayer<O>.plus(rightPipeline: RightClosedUniPipeline<O, LEI>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <O, NO> CreatorLayer<O>.plus(rightLayer: TransitiveLayer<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <O> CreatorLayer<O>.plus(rightLayer: ConsumerLayer<O>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <O, NO> TerminalBiLayer<O>.plus(rightPipeline: OpenUniPipeline<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <O, LEI> TerminalBiLayer<O>.plus(rightPipeline: RightClosedUniPipeline<O, LEI>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <O, NO> TerminalBiLayer<O>.plus(rightLayer: TransitiveLayer<O, NO>): LeftClosedUniPipeline<NO, O> =
		LeftClosedUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <O> TerminalBiLayer<O>.plus(rightLayer: ConsumerLayer<O>): ClosedUniPipeline =
		LeftClosedUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <I, O, NO> TransitiveLayer<I, O>.plus(rightPipeline: OpenUniPipeline<O, NO>): OpenUniPipeline<I, NO> =
		OpenUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <I, O, LEI> TransitiveLayer<I, O>.plus(rightPipeline: RightClosedUniPipeline<O, LEI>): RightClosedUniPipeline<I, LEI> =
		OpenUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <I, O, NO> TransitiveLayer<I, O>.plus(rightLayer: TransitiveLayer<O, NO>): OpenUniPipeline<I, NO> =
		OpenUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <I, O> TransitiveLayer<I, O>.plus(rightLayer: ConsumerLayer<O>): RightClosedUniPipeline<I, O> =
		OpenUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <I, O, NO> TransitiveBiLayer<I, O>.plus(rightPipeline: OpenUniPipeline<O, NO>): OpenUniPipeline<I, NO> =
		OpenUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <I, O, LEI> TransitiveBiLayer<I, O>.plus(rightPipeline: RightClosedUniPipeline<O, LEI>): RightClosedUniPipeline<I, LEI> =
		OpenUniPipeline.fromLayer(this) + rightPipeline

@PublicApi
operator fun <I, O, NO> TransitiveBiLayer<I, O>.plus(rightLayer: TransitiveLayer<O, NO>): OpenUniPipeline<I, NO> =
		OpenUniPipeline.fromLayer(this) + OpenUniPipeline.fromLayer(rightLayer)

@PublicApi
operator fun <I, O> TransitiveBiLayer<I, O>.plus(rightLayer: ConsumerLayer<O>): RightClosedUniPipeline<I, O> =
		OpenUniPipeline.fromLayer(this) + RightClosedUniPipeline.fromLayer(rightLayer)