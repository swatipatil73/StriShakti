package com.collage.new_strishakti.ui.RegisterViewModel


import android.util.Log
import android.widget.Spinner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.regi.College
import com.collage.new_strishakti.data.model.regi.Department
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.model.regi.RegisterRequest
import com.collage.new_strishakti.data.model.regi.School
import com.collage.new_strishakti.data.model.regi.State
import com.collage.new_strishakti.data.model.regi.StreamItem
import com.collage.new_strishakti.data.model.regi.StudyCentre
import com.collage.new_strishakti.data.model.regi.Taluka
import com.collage.new_strishakti.data.model.regi.University
import com.collage.new_strishakti.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    val stateList = MutableLiveData<List<State>>()
    val districtList = MutableLiveData<List<District>>()
    val talukaList = MutableLiveData<List<Taluka>>()
    val universityList = MutableLiveData<List<University>>() // ⬅️
    val registrationResult = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    val departmentList = MutableLiveData<List<Department>>()
    val streamList = MutableLiveData<List<StreamItem>>()
    // new:
    val collegeList = MutableLiveData<List<College>>()
    val schoolList = MutableLiveData<List<School>>()
    val studyCentreList = MutableLiveData<List<StudyCentre>>()
    val registrationError = MutableLiveData<String?>()

    val dataLoadError = MutableLiveData<String?>()


    private lateinit var roleSpinner: Spinner
    private var selectedRole: String = ""

    fun fetchStates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getAllStates()
                if (response.isSuccessful && response.body() != null) {
                    stateList.postValue(response.body())
                } else {
                    error.postValue("Failed to load states: ${response.code()}")
                }
            } catch (e: Exception) {
              //  error.postValue(e.message)
                dataLoadError.postValue("Failed to load data. Please check your connection.")

            }
        }
    }

    fun fetchDistricts(stateId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getDistricts(stateId)
                if (response.isSuccessful && response.body() != null) {
                    districtList.postValue(response.body())
                } else {
                    error.postValue("Failed to load districts: ${response.code()}")
                }
            } catch (e: Exception) {
                error.postValue(e.message)
            }
        }
    }

    fun fetchTalukas(districtId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getTalukas(districtId)
                if (response.isSuccessful && response.body() != null) {
                    talukaList.postValue(response.body())
                } else {
                    error.postValue("Failed to load talukas: ${response.code()}")
                }
            } catch (e: Exception) {
                error.postValue(e.message)
            }
        }
    }


    fun fetchUniversities() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getUniversities()
                if (response.isSuccessful && response.body() != null) {
                    // assuming `body()` gives an object that has a `.universities` list
                    universityList.postValue(response.body()!!.universities)
                } else {
                    error.postValue("Failed to load universities: ${response.code()}")
                }
            } catch (e: Exception) {
                error.postValue("Error fetching universities: ${e.message}")
            }
        }
    }

    fun fetchColleges(universityId: Int) {
        Log.d("FetchColleges", "University ID: $universityId") // 👈 This logs the university ID
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getCollegesByUniversity(universityId)
                if (resp.isSuccessful && resp.body() != null) {
                    collegeList.postValue(resp.body())
                } else {
                    // you could post empty list so observer gets notified
                    collegeList.postValue(emptyList())
                    error.postValue("Failed to load colleges: ${resp.code()}")
                }
            } catch (e: Exception) {
                collegeList.postValue(emptyList())
                error.postValue(e.message)
            }
        }
    }

    fun fetchSchools(universityId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getSchoolsByUniversity(universityId)
                if (resp.isSuccessful && resp.body() != null) {
                    schoolList.postValue(resp.body())
                } else {
                    schoolList.postValue(emptyList())
                    error.postValue("Failed to load schools: ${resp.code()}")
                }
            } catch (e: Exception) {
                schoolList.postValue(emptyList())
                error.postValue(e.message)
            }
        }
    }

    fun fetchStudyCentres(universityId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getStudyCentresByUniversity(universityId)
                if (resp.isSuccessful && resp.body() != null) {
                    studyCentreList.postValue(resp.body())
                } else {
                    studyCentreList.postValue(emptyList())
                    error.postValue("Failed to load study centres: ${resp.code()}")
                }
            } catch (e: Exception) {
                studyCentreList.postValue(emptyList())
                error.postValue(e.message)
            }
        }
    }


    // fetch departments
    fun fetchDepartments() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getAllDepartments()
                if (resp.isSuccessful && resp.body() != null) {
                    departmentList.postValue(resp.body())
                } else {
                    departmentList.postValue(emptyList())
                }
            } catch (e: Exception) {
                departmentList.postValue(emptyList())
            }
        }
    }

    // fetch streams
    fun fetchStreams() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getAllStreams()
                if (resp.isSuccessful && resp.body() != null) {
                    streamList.postValue(resp.body())
                } else {
                    streamList.postValue(emptyList())
                }
            } catch (e: Exception) {
                streamList.postValue(emptyList())
            }
        }
    }

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.registerUser(request)

                if (response.isSuccessful) {
                    registrationResult.postValue(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val json = JSONObject(errorBody ?: "")
                        json.getString("message")
                    } catch (e: Exception) {
                        "Registration failed. Please try again."
                    }
                    registrationError.postValue(errorMessage)
                }
            } catch (e: Exception) {
                registrationError.postValue(e.message ?: "Something went wrong during registration.")
            }
        }
    }


}


