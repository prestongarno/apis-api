package com.prestongarno.apis.logging

import org.slf4j.LoggerFactory
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


class LoggingProvider<T : Any>(val clazz: KClass<T>) {

  operator fun provideDelegate(inst: Any?, property: KProperty<*>) =
      lazy { LoggerFactory.getLogger(clazz.java) }
}

inline fun <reified T : Any> KCallable<T>.logger() =
    LoggingProvider(T::class)

inline fun <reified T : Any> T.logger() =
    LoggingProvider(T::class)

