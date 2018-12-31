package pl.karol202.plumber

@PublicApi
sealed class Output<O>
{
	@PublicApi
	data class Value<O>(val value: O) : Output<O>()

	@PublicApi
	class NoValue<O> : Output<O>()

	@PublicApi
	fun <T> fold(ifValue: (O) -> T, ifNoValue: () -> T): T = when(this)
	{
		is Value<O> -> ifValue(value)
		is NoValue<O> -> ifNoValue()
	}
}
