package pl.karol202.plumber

@PublicApi
interface ConvertibleToOpenUniPipeline<I, O>
{
	@PublicApi
	fun toOpenUniPipeline(): OpenUniPipeline<I, O>
}

@PublicApi
interface ConvertibleToLeftClosedUniPipeline<O, FEO>
{
	@PublicApi
	fun toLeftClosedUniPipeline(): LeftClosedUniPipeline<O, FEO>
}

@PublicApi
interface ConvertibleToRightClosedUniPipeline<I, LEI>
{
	@PublicApi
	fun toRightClosedUniPipeline(): RightClosedUniPipeline<I, LEI>
}

@PublicApi
interface ConvertibleToOpenBiPipeline<I, O>
{
	@PublicApi
	fun toOpenBiPipeline(): OpenBiPipeline<I, O>
}

@PublicApi
interface ConvertibleToLeftClosedBiPipeline<O, FEO>
{
	@PublicApi
	fun toLeftClosedBiPipeline(): LeftClosedBiPipeline<O, FEO>
}

@PublicApi
interface ConvertibleToRightClosedBiPipeline<I, LEI>
{
	@PublicApi
	fun toRightClosedBiPipeline(): RightClosedBiPipeline<I, LEI>
}

@PublicApi
operator fun <O, NO, FEO> ConvertibleToLeftClosedUniPipeline<O, FEO>.plus(right: ConvertibleToOpenUniPipeline<O, NO>):
		LeftClosedUniPipeline<NO, FEO> = toLeftClosedUniPipeline() + right.toOpenUniPipeline()

@PublicApi
operator fun <O, NO> ConvertibleToLeftClosedUniPipeline<O, *>.plus(right: ConvertibleToRightClosedUniPipeline<O, NO>):
		ClosedUniPipeline = toLeftClosedUniPipeline() + right.toRightClosedUniPipeline()

@PublicApi
operator fun <I, O, NO> ConvertibleToOpenUniPipeline<I, O>.plus(right: ConvertibleToOpenUniPipeline<O, NO>):
		OpenUniPipeline<I, NO> = toOpenUniPipeline() + right.toOpenUniPipeline()

@PublicApi
operator fun <I, O, LEI> ConvertibleToOpenUniPipeline<I, O>.plus(right: ConvertibleToRightClosedUniPipeline<O, LEI>):
		RightClosedUniPipeline<I, LEI> = toOpenUniPipeline() + right.toRightClosedUniPipeline()

@PublicApi
operator fun <O, NO, FEO> ConvertibleToLeftClosedBiPipeline<O, FEO>.plus(right: ConvertibleToOpenBiPipeline<O, NO>):
		LeftClosedBiPipeline<NO, FEO> = toLeftClosedBiPipeline() + right.toOpenBiPipeline()

@PublicApi
operator fun <O, NO> ConvertibleToLeftClosedBiPipeline<O, *>.plus(right: ConvertibleToRightClosedBiPipeline<O, NO>):
		ClosedBiPipeline = toLeftClosedBiPipeline() + right.toRightClosedBiPipeline()

@PublicApi
operator fun <I, O, NO> ConvertibleToOpenBiPipeline<I, O>.plus(right: ConvertibleToOpenBiPipeline<O, NO>):
		OpenBiPipeline<I, NO> = toOpenBiPipeline() + right.toOpenBiPipeline()

@PublicApi
operator fun <I, O, LEI> ConvertibleToOpenBiPipeline<I, O>.plus(right: ConvertibleToRightClosedBiPipeline<O, LEI>):
		RightClosedBiPipeline<I, LEI> = toOpenBiPipeline() + right.toRightClosedBiPipeline()
