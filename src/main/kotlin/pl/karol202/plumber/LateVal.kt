package pl.karol202.plumber

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class LateVal<T>(val nullException: Exception,
					  val assignedException: Exception) : ReadWriteProperty<Any?, T>
{
	private var variable: T? = null

	override operator fun getValue(thisRef: Any?, property: KProperty<*>) = variable ?: throw nullException

	override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
	{
		if(variable != null) throw assignedException
		variable = value
	}
}
