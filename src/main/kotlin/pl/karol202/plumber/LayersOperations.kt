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
operator fun <O, NO, FEO> ConvertibleToLeftClosedUniPipeline<O, FEO>.plus(uniPipeline: ConvertibleToOpenUniPipeline<O, NO>):
		LeftClosedUniPipeline<NO, FEO> = toLeftClosedUniPipeline() + uniPipeline.toOpenUniPipeline()

@PublicApi
operator fun <O, NO> ConvertibleToLeftClosedUniPipeline<O, *>.plus(uniPipeline: ConvertibleToRightClosedUniPipeline<O, NO>):
		ClosedUniPipeline = toLeftClosedUniPipeline() + uniPipeline.toRightClosedUniPipeline()

@PublicApi
operator fun <I, O, NO> ConvertibleToOpenUniPipeline<I, O>.plus(uniPipeline: ConvertibleToOpenUniPipeline<O, NO>):
		OpenUniPipeline<I, NO> = toOpenUniPipeline() + uniPipeline.toOpenUniPipeline()

@PublicApi
operator fun <I, O, LEI> ConvertibleToOpenUniPipeline<I, O>.plus(uniPipeline: ConvertibleToRightClosedUniPipeline<O, LEI>):
		RightClosedUniPipeline<I, LEI> = toOpenUniPipeline() + uniPipeline.toRightClosedUniPipeline()

@PublicApi
operator fun <O, NO, FEO> ConvertibleToLeftClosedBiPipeline<O, FEO>.plus(biPipeline: ConvertibleToOpenBiPipeline<O, NO>):
		LeftClosedBiPipeline<NO, FEO> = toLeftClosedBiPipeline() + biPipeline.toOpenBiPipeline()

@PublicApi
operator fun <O, NO> ConvertibleToLeftClosedBiPipeline<O, *>.plus(biPipeline: ConvertibleToRightClosedBiPipeline<O, NO>):
		ClosedBiPipeline = toLeftClosedBiPipeline() + biPipeline.toRightClosedBiPipeline()

@PublicApi
operator fun <I, O, NO> ConvertibleToOpenBiPipeline<I, O>.plus(biPipeline: ConvertibleToOpenBiPipeline<O, NO>):
		OpenBiPipeline<I, NO> = toOpenBiPipeline() + biPipeline.toOpenBiPipeline()

@PublicApi
operator fun <I, O, LEI> ConvertibleToOpenBiPipeline<I, O>.plus(biPipeline: ConvertibleToRightClosedBiPipeline<O, LEI>):
		RightClosedBiPipeline<I, LEI> = toOpenBiPipeline() + biPipeline.toRightClosedBiPipeline()
