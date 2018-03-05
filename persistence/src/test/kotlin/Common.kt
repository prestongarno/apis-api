package com.prestongarno.apis.persistence

import com.prestongarno.apis.core.entities.ApiInfo
import java.time.Instant

fun noInfo() = ApiInfo(emptyMap())

fun Long.toJodaDate() = org.joda.time.Instant(this).toDateTime()!!

fun Long.toDate() = java.util.Date.from(java.time.Instant.ofEpochMilli(this))

fun Long.toInstant() = Instant.ofEpochMilli(this)

fun <T> use(instance: T, block: T.() -> Unit) = instance.block()