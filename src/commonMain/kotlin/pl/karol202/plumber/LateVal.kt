package pl.karol202.plumber

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LateVal<T>(private val nullException: () -> Exception,
				 private val assignedException: () -> Exception) : ReadWriteProperty<Any?, T>
{
	private var variable: T? = null

	override operator fun getValue(thisRef: Any?, property: KProperty<*>) = variable ?: throw nullException()

	override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
	{
		if(variable != null) throw assignedException()
		variable = value
	}
}

fun <T> latePreviousElement() = LateVal<T>({ PipelineException("Element has no predecessor.") },
											{ PipelineException("Previous element already assigned.") })
