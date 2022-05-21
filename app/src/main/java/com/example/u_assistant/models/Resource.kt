package com.example.u_assistant.models

sealed interface Resource<out T> {
    object Loading : Resource<Nothing>
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val throwable: Throwable) : Resource<Nothing>
}

fun <T> T.toSuccess(): Resource<T> = Resource.Success(this)
fun <T> Throwable.toError(): Resource<T> = Resource.Error(this)

fun <T, R> Resource<T>.transform(
    transform: ((value: T) -> R)
): Resource<R> = when (this) {
    is Resource.Loading -> Resource.Loading
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> Resource.Error(throwable)
}

fun <T> Resource<T>.getOrNull(): T? = when (this) {
    is Resource.Loading -> null
    is Resource.Success -> data
    is Resource.Error -> null
}

fun <T> Resource<T>.getOrThrow(): T = when (this) {
    is Resource.Loading -> throw IllegalStateException("Resource is loading")
    is Resource.Success -> data
    is Resource.Error -> throw throwable
}