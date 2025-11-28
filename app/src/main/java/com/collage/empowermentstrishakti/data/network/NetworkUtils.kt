package com.collage.empowermentstrishakti.data.network



import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response

/**
 * Generic safe API caller that prevents long hangs, exceptions, or null bodies.
 */
suspend fun <T> safeApiCall(
    maxRetries: Int = 1,
    timeoutMillis: Long = 15000,
    call: suspend () -> Response<T>
): Result<T> {
    var attempt = 0
    var lastError: Throwable? = null

    while (attempt <= maxRetries) {
        try {
            // Timeout + IO context
            val response = withTimeoutOrNull(timeoutMillis) {
                withContext(Dispatchers.IO) { call() }
            } ?: return Result.failure(Exception("⏱️ Server took too long to respond."))

            // Success
            if (response.isSuccessful) {
                response.body()?.let { return Result.success(it) }
                return Result.failure(Exception("Empty response body"))
            } else {
                return Result.failure(Exception("HTTP ${response.code()} ${response.message()}"))
            }

        } catch (e: Exception) {
            lastError = e
        }
        attempt++
    }

    return Result.failure(lastError ?: Exception("Unknown network error"))
}
