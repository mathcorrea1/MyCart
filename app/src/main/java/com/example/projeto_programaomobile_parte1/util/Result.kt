package com.example.projeto_programaomobile_parte1.util

/**
 * Sealed class para representar o resultado de operações assíncronas
 * Facilita o tratamento de sucesso, erro e loading states
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Exception? = when (this) {
        is Error -> exception
        else -> null
    }
}

/**
 * Extensão para executar código apenas se Result for Success
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Extensão para executar código apenas se Result for Error
 */
inline fun <T> Result<T>.onError(action: (Exception, String?) -> Unit): Result<T> {
    if (this is Result.Error) action(exception, message)
    return this
}

/**
 * Extensão para executar código apenas se Result for Loading
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}

