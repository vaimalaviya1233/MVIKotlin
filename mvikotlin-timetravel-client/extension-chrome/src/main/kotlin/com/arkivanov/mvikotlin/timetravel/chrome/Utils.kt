package com.arkivanov.mvikotlin.timetravel.chrome

internal fun <T> jsObject(builder: T.() -> Unit): T =
    js("{}").unsafeCast<T>().apply(builder)
