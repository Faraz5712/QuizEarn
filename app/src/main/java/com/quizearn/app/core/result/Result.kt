package com.quizearn.app.core.result

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable, val message: String? = exception.message) : Result<Nothing>
    data object Loading : Result<Nothing>
}

val Result<*>.isSuccess: Boolean
    get() = this is Result.Success

val Result<*>.isFailure: Boolean
    get() = this is Result.Error

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

fun Result<*>.exceptionOrNull(): Throwable? = when (this) {
    is Result.Error -> exception
    else -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw exception
    is Result.Loading -> throw IllegalStateException("Result is still Loading")
}
