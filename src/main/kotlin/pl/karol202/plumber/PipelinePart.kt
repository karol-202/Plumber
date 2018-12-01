package pl.karol202.plumber

interface RightExpandablePipelinePart<O>
{
	operator fun <I> plus(nextPart: LeftExpandablePipelinePart<I>): Pipeline<I, O>
}

interface LeftExpandablePipelinePart<I>