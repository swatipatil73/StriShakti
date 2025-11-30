package com.collage.empowermentstrishakti.data.repository



import com.collage.empowermentstrishakti.data.model.post.AdsResponse

import com.collage.empowermentstrishakti.data.model.Ads.Resource
import com.collage.empowermentstrishakti.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AdsRepository {

    // use the ApiClient singleton you provided
    private val api = ApiClient.apiService

    /**
     * Fetch ads. Pass the Authorization header from caller.
     * Example: "Bearer <token>" or raw token depending on backend.
     */
    suspend fun getAds(authorization: String): Resource<AdsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAds(authorization)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Empty response body")
                    }
                } else {
                    // Optionally parse errorBody for a more detailed message
                    val code = response.code()
                    val serverMsg = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    val errMsg = if (!serverMsg.isNullOrBlank()) {
                        "Server error $code: $serverMsg"
                    } else {
                        "Server error: $code"
                    }
                    Resource.Error(errMsg)
                }
            } catch (e: IOException) {
                // network or conversion error
                Resource.Error("Network failure: ${e.message ?: "IO error"}")
            } catch (e: HttpException) {
                Resource.Error("HTTP error: ${e.message ?: e.code().toString()}")
            } catch (e: Exception) {
                Resource.Error("Unexpected error: ${e.message ?: "Unknown"}")
            }
        }
    }
}

