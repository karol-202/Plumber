package pl.karol202.plumber

/**
 * Layer representing any operation which can have input and/or output.
 * Every layer can transform data forwards (data of input type to output type) and backwards (data of output type to input type).
 * Layer should not be implemented directly but through one of interfaces: FirstLayer, MiddleLayer, LastLayer.
 *
 * Generic types:
 * - I - input type
 * - O - output type
*/
@PublicApi
interface Layer<I, O>
{
    /**
     * Transforms data from input to output type.
     */
    @PublicApi
	fun transformForward(input: I): O

    /**
     * Transforms data from output to input type.
     */
    @PublicApi
	fun transformBackward(input: O): I
}

/**
 * First layer is layer creating data (for example from IO operations). It can be only a start of a pipeline.
 */
@PublicApi
interface FirstLayer<O> : Layer<Unit, O>

/**
 * Middle layer is layer transforming data between input and output types.
 * It can middle part of a pipeline as well as start part or end part.
 */
@PublicApi
interface MiddleLayer<I, O> : Layer<I, O>

/**
 * Last layer is layer consuming data. It can be only an end of a pipeline.
 */
@PublicApi
interface LastLayer<I> : Layer<I, Unit>


/**
 * Following toPipeline() methods create new pipeline from a layer.
 * There is no need to use them directly.
 */
fun <O> FirstLayer<O>.toPipeline(): LeftClosedPipeline<O, O> = LeftClosedPipeline.fromLayer(this)

fun <I, O> MiddleLayer<I, O>.toPipeline(): OpenPipeline<I, O> = OpenPipeline.fromLayer(this)

fun <I> LastLayer<I>.toPipeline(): RightClosedPipeline<I, I> = RightClosedPipeline.fromLayer(this)

/**
 * Following methods are '+' operator overloads and are used to join layers with other layers
 * or pipelines in order to create new pipelines.
 */
operator fun <O, NO> FirstLayer<O>.plus(rightPipeline: OpenPipeline<O, NO>): LeftClosedPipeline<NO, O> =
    toPipeline() + rightPipeline

operator fun <O, LEI> FirstLayer<O>.plus(rightPipeline: RightClosedPipeline<O, LEI>): ClosedPipeline =
    toPipeline() + rightPipeline

operator fun <O, NO> FirstLayer<O>.plus(rightLayer: MiddleLayer<O, NO>): LeftClosedPipeline<NO, O> =
    toPipeline() + rightLayer.toPipeline()

operator fun <O> FirstLayer<O>.plus(rightLayer: LastLayer<O>): ClosedPipeline =
    toPipeline() + rightLayer.toPipeline()

operator fun <I, O, NO> MiddleLayer<I, O>.plus(rightPipeline: OpenPipeline<O, NO>): OpenPipeline<I, NO> =
    toPipeline() + rightPipeline

operator fun <I, O, LEI> MiddleLayer<I, O>.plus(rightPipeline: RightClosedPipeline<O, LEI>): RightClosedPipeline<I, LEI> =
    toPipeline() + rightPipeline

operator fun <I, O, NO> MiddleLayer<I, O>.plus(rightLayer: MiddleLayer<O, NO>): OpenPipeline<I, NO> =
    toPipeline() + rightLayer.toPipeline()

operator fun <I, O> MiddleLayer<I, O>.plus(rightLayer: LastLayer<O>): RightClosedPipeline<I, O> =
    toPipeline() + rightLayer.toPipeline()
