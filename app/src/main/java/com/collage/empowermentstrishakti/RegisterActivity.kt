package com.collage.empowermentstrishakti

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.PixelCopy.request
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Common.UserType
import com.collage.empowermentstrishakti.Factory.RegisterViewModelFactory
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.regi.College
import com.collage.empowermentstrishakti.data.model.regi.Department
import com.collage.empowermentstrishakti.data.model.regi.District
import com.collage.empowermentstrishakti.data.model.regi.RegisterRequest
import com.collage.empowermentstrishakti.data.model.regi.School
import com.collage.empowermentstrishakti.data.model.regi.State
import com.collage.empowermentstrishakti.data.model.regi.StreamItem
import com.collage.empowermentstrishakti.data.model.regi.StudyCentre
import com.collage.empowermentstrishakti.data.model.regi.Taluka
import com.collage.empowermentstrishakti.data.model.regi.University
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.UserProfileRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class RegisterActivity : BaseActivity() {
  //  val viewModel: RegisterViewModel by viewModels()
  private val viewModel: RegisterViewModel by viewModels {
      val apiService = ApiClient.apiService
      val sessionManager = SessionManager(applicationContext)
      val repository = UserProfileRepository(
          api = apiService,
          sessionManager = sessionManager,
          appContext = applicationContext
      )
      RegisterViewModelFactory(repository)
  }


    private lateinit var userType: UserType

    // UI elements
    private lateinit var stateSpinner: Spinner
    private lateinit var districtSpinner: Spinner
    private lateinit var talukaSpinner: Spinner
    private lateinit var swayamSpinner: Spinner
    private lateinit var universitySpinner: Spinner
    private lateinit var collegeSpinner: Spinner
    private lateinit var schoolSpinner: Spinner
    private lateinit var studyCentreSpinner: Spinner
    private lateinit var departmentSpinner: Spinner
    private lateinit var streamSpinner: Spinner

    // Swayamsiddha sub-role spinner (STUDENT/FACULTY/etc)
    private lateinit var roleSpinner: Spinner



    // Selected role values
    private var selectedSubRole: String? = null    // for Swayamsiddha (STUDENT/FACULTY/...)
    private var selectedAdiRole: String = ""       // backend ROLE_... for AdiShakti

    private lateinit var extendedForm: LinearLayout

    private lateinit var registerButton: AppCompatButton

    private lateinit var firstName: TextInputEditText
    private lateinit var lastName: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var phone: TextInputEditText
    private lateinit var address: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var dob: TextInputEditText
    private lateinit var agreeCheckBox: CheckBox

    // Selected IDs
    private var stateId: Int = 0
    private var districtId: Int = 0
    private var talukaId: Int = 0

    private var universityId: Int = 0
    private var selectedCollegeUserId: Int = 0
    private var selectedSchoolId: Int = 0
    private var selectedStudyCentreId: Int = 0
    private var selectedDepartmentId: Int = 0
    private var selectedStreamId: Int = 0
    private lateinit var spinnerLocalBodyType: Spinner
    private lateinit var etLocalBodyName: TextInputEditText
    private lateinit var etWardNo: TextInputEditText

    // UI display list
    private val localBodyDisplayList = listOf(
        "Select Local Body Type",
        "Gram Panchayat",
        "Nagar Palika",
        "Mahanagar Palika"
    )

    // Backend mapping
    private val localBodyTypeMap: Map<String, String> = mapOf(
        "Gram Panchayat" to "GRAM_PANCHAYAT",
        "Nagar Palika" to "NAGAR_PALIKA",
        "Mahanagar Palika" to "MAHANAGAR_PALIKA"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)



        userType = UserType.valueOf(
            intent.getStringExtra("USER_TYPE") ?: UserType.NORMAL.name
        )

        initViews()          // ✅ FIRST
        setupUIByUserType()  // ✅ THEN



        setupDatePicker()
        setupStateDropdown()
        setupObservers()

        // API Calls
        viewModel.fetchUniversities()
        viewModel.fetchDepartments()
        viewModel.fetchStreams()

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun initViews() {
        stateSpinner = findViewById(R.id.spinner_state)
        districtSpinner = findViewById(R.id.spinner_district)
        talukaSpinner = findViewById(R.id.spinner_taluka)
        swayamSpinner = findViewById(R.id.spinner_swayamsiddha)
        extendedForm = findViewById(R.id.extended_form_layout)
        registerButton = findViewById(R.id.btn_register)
        universitySpinner = findViewById(R.id.spinner_university)
        collegeSpinner = findViewById(R.id.spinner_college)
        schoolSpinner = findViewById(R.id.spinner_schools)

        spinnerLocalBodyType = findViewById(R.id.spinner_local_body_type)
        etLocalBodyName = findViewById(R.id.et_local_body_name)
        etWardNo = findViewById(R.id.et_ward_no)





        studyCentreSpinner = findViewById(R.id.spinner_studycentre)
        firstName = findViewById(R.id.et_first_name)
        lastName = findViewById(R.id.et_last_name)
        email = findViewById(R.id.et_email)
        phone = findViewById(R.id.et_phone)
        address = findViewById(R.id.et_address)
        password = findViewById(R.id.et_password)
        dob = findViewById(R.id.et_dob)
        agreeCheckBox = findViewById(R.id.cb_agree)
        departmentSpinner = findViewById(R.id.spinner_departments)
        streamSpinner = findViewById(R.id.spinner_stream)

        roleSpinner = findViewById(R.id.spinner_role)
        setupSwayamRoleSpinner()

        // optionally set default placeholder till data arrives
        setDefaultSpinner(departmentSpinner, "Please select department")
        setDefaultSpinner(streamSpinner, "Please select stream")

        setDefaultSpinner(collegeSpinner, "Please select college")
        setDefaultSpinner(schoolSpinner, "Please select school")
        setDefaultSpinner(studyCentreSpinner, "Please select study centre")
    }

    private fun setupSwayamRoleSpinner() {
        val roles = listOf("Please select sub-role", "STUDENT", "FACULTY", "STAFF", "OTHER")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSubRole = if (position == 0) null else roles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSubRole = null
            }
        }
    }



    private fun setDefaultSpinner(spinner: Spinner, defaultText: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(defaultText))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }



    // Activity-level show/hide animated methods (used by both spinners)
    private fun showExtendedForm() {
        extendedForm.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = 100f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }
        }
    }

    private fun hideExtendedForm() {
        extendedForm.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(300)
            .withEndAction {
                extendedForm.visibility = View.GONE
            }
            .start()
    }

    private fun setupStateDropdown() {
        viewModel.fetchStates()
        viewModel.stateList.observe(this) { states ->
            val fullList = mutableListOf(State(0, "Please select state"))
            fullList.addAll(states)

            val names = fullList.map { it.stateName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            stateSpinner.adapter = adapter

            stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        stateId = 0
                        clearDistrictTaluka()
                    } else {
                        stateId = fullList[position].stateId
                        viewModel.fetchDistricts(stateId)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun setupObservers() {
        // ✅ District Spinner
        viewModel.districtList.observe(this) { districts ->
            val fullList = mutableListOf(District(0, "Please select district"))
            fullList.addAll(districts)

            val names = fullList.map { it.districtName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            districtSpinner.adapter = adapter

            districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        districtId = 0
                        clearTaluka()
                        Log.d("DistrictSelection", "No district selected")
                    } else {
                        districtId = fullList[position].districtId
                        Log.d("DistrictSelection", "Selected District: ID=${districtId}, Name=${fullList[position].districtName}")
                        viewModel.fetchTalukas(districtId)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        // ✅ Taluka Spinner
        viewModel.talukaList.observe(this) { talukas ->
            val fullList = mutableListOf(Taluka(0, "Please select taluka"))
            fullList.addAll(talukas)

            val names = fullList.map { it.talukaName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            talukaSpinner.adapter = adapter

            talukaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        talukaId = 0
                        Log.d("TalukaSelection", "No taluka selected")
                    } else {
                        talukaId = fullList[position].talukaId
                        Log.d("TalukaSelection", "Selected Taluka: ID=${talukaId}, Name=${fullList[position].talukaName}")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.universityList.observe(this) { universityList ->
            val defaultUniversity = University(0, "Please select university")
            val fullList = listOf(defaultUniversity) + universityList

            val names = fullList.map { it.institutionName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            universitySpinner.adapter = adapter

            universitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    universityId = fullList[position].universityId

                    if (universityId != 0) {
                        viewModel.fetchColleges(universityId)
                        viewModel.fetchSchools(universityId)
                        viewModel.fetchStudyCentres(universityId)
                    } else {
                        setDefaultSpinner(collegeSpinner, "Please select college")
                        setDefaultSpinner(schoolSpinner, "Please select school")
                        setDefaultSpinner(studyCentreSpinner, "Please select study centre")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.collegeList.observe(this) { collegeList ->
            val fullList = mutableListOf(College(0, "Please select college", ""))
            fullList.addAll(collegeList)

            val names = fullList.map { it.institutionName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            collegeSpinner.adapter = adapter

            collegeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedCollege = fullList[position]
                    selectedCollegeUserId = selectedCollege.userId
                    Log.d("CollegeSelection", "Selected College: ID=${selectedCollege.userId}, Name=${selectedCollege.institutionName}")
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.schoolList.observe(this) { schoolList ->
            val fullList = mutableListOf(School(0, "Please select school", "", null))
            fullList.addAll(schoolList)

            val names = fullList.map { it.schoolName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            schoolSpinner.adapter = adapter

            schoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedSchoolId = fullList[position].schoolId
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.studyCentreList.observe(this) { centreList ->
            val fullList = mutableListOf(StudyCentre(0, "Please select study centre", "", null))
            fullList.addAll(centreList)

            val names = fullList.map { it.studyCentreName }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            studyCentreSpinner.adapter = adapter

            studyCentreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedStudyCentreId = fullList[position].studyCentreId
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.departmentList.observe(this) { deptList ->
            val fullList = mutableListOf(Department(0, "", "Please select department", null))
            fullList.addAll(deptList)

            val names = fullList.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            departmentSpinner.adapter = adapter

            departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedDepartmentId = fullList[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedDepartmentId = 0
                }
            }
        }

        viewModel.streamList.observe(this) { streamList ->
            val defaultItem = StreamItem(0, "", "Please select stream", null, null)
            val fullList = listOf(defaultItem) + streamList

            val names = fullList.map { it.streamName }

            val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names) {
                override fun isEnabled(position: Int): Boolean {
                    return position != 0 // Disable selection of default item
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val tv = view as TextView
                    tv.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                    return view
                }
            }

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            streamSpinner.adapter = adapter

            streamSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedStreamId = fullList[position].streamId
                    Log.d("StreamSelection", "Selected Stream ID: $selectedStreamId")
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedStreamId = 0
                }
            }
        }

        // Registration & data error observers
        viewModel.registrationError.observe(this) { errorMessage ->
            errorMessage?.let {
                showErrorDialog(it)
                viewModel.registrationError.postValue(null)
            }
        }

        viewModel.dataLoadError.observe(this) { errorMessage ->
            errorMessage?.let {
                showRetryDialog(it)
                viewModel.dataLoadError.postValue(null)
            }
        }

        viewModel.registrationResult.observe(this, Observer { success ->
            if (success) {
                Snackbar.make(registerButton, "Registration successful!", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(registerButton, "Registration failed!", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun clearDistrictTaluka() {
        districtId = 0
        talukaId = 0
        setDefaultSpinner(districtSpinner, "Please select district")
        setDefaultSpinner(talukaSpinner, "Please select taluka")
    }

    private fun clearTaluka() {
        talukaId = 0
        setDefaultSpinner(talukaSpinner, "Please select taluka")
    }

    private fun setupDatePicker() {
        dob.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, y, m, d ->
                val sel = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
                dob.setText(sel)
            }, year, month, day).show()
        }
    }

    private fun registerUser() {
        val backendLocalBodyType: String? =
            if (userType == UserType.ADISHAKTI &&
                spinnerLocalBodyType.selectedItem != null
            ) {
                localBodyTypeMap[spinnerLocalBodyType.selectedItem.toString()]
            } else {
                null
            }


        // ================= COMMON VALIDATION =================
        if (firstName.text.isNullOrBlank() ||
            lastName.text.isNullOrBlank() ||
            email.text.isNullOrBlank() ||
            phone.text.isNullOrBlank() ||
            address.text.isNullOrBlank() ||
            password.text.isNullOrBlank() ||
            dob.text.isNullOrBlank() ||
            stateId == 0 ||
            districtId == 0 ||
            talukaId == 0 ||
            !agreeCheckBox.isChecked
        ) {
            Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // ================= SWAYAMSIDHA VALIDATION =================
        if (userType == UserType.SWAYAMSIDHA && selectedSubRole.isNullOrBlank()) {
            Toast.makeText(this, "Please select Swayamsiddha sub-role", Toast.LENGTH_SHORT).show()
            return
        }

        // ================= ADISHAKTI VALIDATION =================
        if (userType == UserType.ADISHAKTI) {
            if (backendLocalBodyType == null ||
                spinnerLocalBodyType.selectedItemPosition == 0 ||
                etLocalBodyName.text.isNullOrBlank() ||
                etWardNo.text.isNullOrBlank()
            ) {
                Toast.makeText(this, "Please fill all AdiShakti details", Toast.LENGTH_SHORT).show()
                return
            }
        }




        if (userType == UserType.SWAYAMSIDHA) {
            if (universityId == 0 ||
                selectedDepartmentId == 0 ||
                selectedStreamId == 0
            ) {
                Toast.makeText(
                    this,
                    "Please complete all Swayamsiddha details",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }


        // ================= CREATE REQUEST (THIS WAS MISSING) =================

        val request = RegisterRequest(
            userFirstName = firstName.text.toString().trim(),
            userLastName = lastName.text.toString().trim(),
            userEmail = email.text.toString().trim(),
            userGender = "FEMALE",
            userDateOfBirth = dob.text.toString().trim(),
            userAddress = address.text.toString().trim(),
            userPassword = password.text.toString().trim(),
            userMobileNumber = phone.text.toString().trim(),

            stateId = stateId,
            districtId = districtId,
            talukaId = talukaId,

            // ✅ STATIC ROLE
            userRole = "ROLE_USER",

            // ================= SWAYAMSIDHA =================
            subRole = if (userType == UserType.SWAYAMSIDHA) selectedSubRole else null,

            universityId = if (userType == UserType.SWAYAMSIDHA && universityId != 0)
                universityId.toString() else null,

            collegeId = if (userType == UserType.SWAYAMSIDHA && selectedCollegeUserId != 0)
                selectedCollegeUserId.toString() else null,

            schoolId = if (userType == UserType.SWAYAMSIDHA && selectedSchoolId != 0)
                selectedSchoolId.toString() else null,

            studyCentreId = if (userType == UserType.SWAYAMSIDHA && selectedStudyCentreId != 0)
                selectedStudyCentreId.toString() else null,

            departmentId = if (userType == UserType.SWAYAMSIDHA && selectedDepartmentId != 0)
                selectedDepartmentId.toString() else null,

            streamId = if (userType == UserType.SWAYAMSIDHA && selectedStreamId != 0)
                selectedStreamId.toString() else null,

            // ================= ADISHAKTI =================
            localBodyType = if (userType == UserType.ADISHAKTI)
                backendLocalBodyType
            else null,


                    localBodyName = if (userType == UserType.ADISHAKTI)
                etLocalBodyName.text.toString().trim() else null,

            wardNo = if (userType == UserType.ADISHAKTI)
                etWardNo.text.toString().trim() else null,



            isSwayamsiddha = userType == UserType.SWAYAMSIDHA,
            isAdiShakti = userType == UserType.ADISHAKTI,
            termsAndConditionsAccepted = agreeCheckBox.isChecked
        )



        Log.d("RegisterRequest", request.toString())
        viewModel.registerUser(request)
    }



    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Registration Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun showRetryDialog(message: String) {
        Log.e("RetryDialog", "Error occurred: $message")
        AlertDialog.Builder(this)
            .setTitle("Data Load Failed")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                retryLoadingAllData()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun retryLoadingAllData() {
        viewModel.fetchStates()
        viewModel.fetchUniversities()
        viewModel.fetchDepartments()
        viewModel.fetchStreams()
    }


    private fun setupUIByUserType() {
        when (userType) {

            UserType.NORMAL -> {
                hideExtendedForm()
                hideAdiShaktiFields()
            }

            UserType.SWAYAMSIDHA -> {
                hideAdiShaktiFields()
                showExtendedForm() // only swayamsiddha layout
            }

            UserType.ADISHAKTI -> {
                hideExtendedForm()
                selectedAdiRole = "ROLE_USER" // static backend role
                showAdiShaktiFields()
            }
        }
    }

    private fun hideAdiShaktiFields() {
        spinnerLocalBodyType.visibility = View.GONE
        etLocalBodyName.visibility = View.GONE
        etWardNo.visibility = View.GONE

    }


    private fun showAdiShaktiFields() {
        spinnerLocalBodyType.visibility = View.VISIBLE
        etLocalBodyName.visibility = View.VISIBLE
        etWardNo.visibility = View.VISIBLE

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            localBodyDisplayList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocalBodyType.adapter = adapter
    }





}
