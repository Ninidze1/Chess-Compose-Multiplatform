package com.ninidze.chesscomposekmm.domain.result

import kotlinx.coroutines.CancellationException

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * @property message A short, human-readable description of the failure.
     * @property cause The exception that caused the failure, if any.
     */
    data class Failure(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}

/**
 * Runs [function] and wraps its outcome in a [Resource]. Cancellation is rethrown so
 * structured concurrency keeps working, and only [Exception]s are turned into a
 * [Resource.Failure]; [Error]s propagate.
 */
suspend fun <R> safeCall(function: suspend () -> R): Resource<R> = try {
    Resource.Success(function())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Resource.Failure(message = e.message ?: e::class.simpleName.orEmpty(), cause = e)
}
