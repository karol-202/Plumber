package pl.karol202.plumber

@PublicApi
sealed class Output<O>
{
	@PublicApi
	data class Value<O>(val value: O) : Output<O>()

	@PublicApi
	class NoValue<O> : Output<O>()
	{
		override fun equals(other: Any?): Boolean
		{
			if(this === other) return true
			if(other !is NoValue<*>) return false
			return true
		}

		override fun hashCode(): Int
		{
			return this::class.hashCode()
		}
	}

	@PublicApi
	fun getOrNull(): O? = (this as? Value<O>)?.value

	@PublicApi
	fun <T> fold(ifValue: (O) -> T, ifNoValue: () -> T): T = when(this)
	{
		is Value<O> -> ifValue(value)
		is NoValue<O> -> ifNoValue()
	}
}
