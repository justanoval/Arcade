package com.justanoval.command

fun interface ArgumentInterpreter<T, U> {
    fun interpret(input: T): U
}