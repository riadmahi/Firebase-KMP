package com.riadmahi.firebase.core

/**
 * A result wrapper for Firebase operations that can either succeed with a value
 * or fail with a FirebaseException.
 */
sealed class FirebaseResult<out T> {

    /**
     * Represents a successful result with data.
     */
    data class Success<T>(val data: T) : FirebaseResult<T>()

    /**
     * Represents a failed result with an exception.
     */
    data class Failure(val exception: FirebaseException) : FirebaseResult<Nothing>()

    /**
     * Returns true if this result is successful.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this result is a failure.
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * Maps the success value to a new value.
     */
    inline fun <R> map(transform: (T) -> R): FirebaseResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /**
     * Maps the success value to a new FirebaseResult.
     */
    inline fun <R> flatMap(transform: (T) -> FirebaseResult<R>): FirebaseResult<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    /**
     * Executes the given block if this is a success.
     */
    inline fun onSuccess(action: (T) -> Unit): FirebaseResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes the given block if this is a failure.
     */
    inline fun onFailure(action: (FirebaseException) -> Unit): FirebaseResult<T> {
        if (this is Failure) action(exception)
        return this
    }

    /**
     * Returns the success value or null if this is a failure.
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Returns the success value or throws the exception if this is a failure.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw exception
    }

    /**
     * Returns the success value or the default value if this is a failure.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> defaultValue
    }

    /**
     * Returns the success value or computes a default value if this is a failure.
     */
    inline fun getOrElse(defaultValue: (FirebaseException) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> defaultValue(exception)
    }

    /**
     * Returns the exception or null if this is a success.
     */
    fun exceptionOrNull(): FirebaseException? = (this as? Failure)?.exception

    companion object {
        /**
         * Creates a successful result.
         */
        fun <T> success(data: T): FirebaseResult<T> = Success(data)

        /**
         * Creates a failed result.
         */
        fun <T> failure(exception: FirebaseException): FirebaseResult<T> = Failure(exception)

        /**
         * Wraps a block execution in a FirebaseResult.
         */
        inline fun <T> runCatching(block: () -> T): FirebaseResult<T> {
            return try {
                Success(block())
            } catch (e: FirebaseException) {
                Failure(e)
            } catch (e: Exception) {
                Failure(FirebaseException.UnknownException(e.message ?: "Unknown error", e))
            }
        }
    }
}

/**
 * Converts a Kotlin Result to a FirebaseResult.
 */
fun <T> Result<T>.toFirebaseResult(): FirebaseResult<T> = fold(
    onSuccess = { FirebaseResult.Success(it) },
    onFailure = {
        when (it) {
            is FirebaseException -> FirebaseResult.Failure(it)
            else -> FirebaseResult.Failure(FirebaseException.UnknownException(it.message ?: "Unknown error", it))
        }
    }
)
