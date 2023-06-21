package com.ninidze.chesscomposekmm.data.helper

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Failure(val message: String) : Resource<Nothing>()
}

suspend fun <R> safeCall(function: suspend () -> R): Resource<R> {
    return try {
        val result = function.invoke()
        Resource.Success(result)
    } catch (e: Throwable) {
        Resource.Failure(e.stackTraceToString())
    }
}