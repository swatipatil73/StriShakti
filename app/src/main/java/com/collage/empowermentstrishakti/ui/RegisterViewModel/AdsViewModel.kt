package com.collage.empowermentstrishakti.ui.RegisterViewModel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.post.AdsResponse
import com.collage.empowermentstrishakti.data.model.Ads.Resource
import com.collage.empowermentstrishakti.data.repository.AdsRepository
import kotlinx.coroutines.launch

class AdsViewModel(
    private val repository: AdsRepository = AdsRepository()
) : ViewModel() {

    private val _ads = MutableLiveData<Resource<AdsResponse>>()
    val ads: LiveData<Resource<AdsResponse>> = _ads

    /**
     * Load ads. Pass the Authorization header exactly as backend expects.
     * Example: viewModel.loadAds("Bearer <token>")
     */
    fun loadAds(authorization: String) {
        _ads.postValue(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getAds(authorization)
            _ads.postValue(result)
        }
    }
}
