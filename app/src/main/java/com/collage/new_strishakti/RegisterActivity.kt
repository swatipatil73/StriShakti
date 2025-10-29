package com.collage.new_strishakti

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.R
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
import com.collage.new_strishakti.ui.RegisterViewModel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*



import java.util.*

class RegisterActivity : BaseActivity() {
 val viewModel: RegisterViewModel by viewModels()

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
    private lateinit var roleSpinner: Spinner

    private var selectedRole: String = ""


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupSwayamsiddhaSpinner()
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
        setupRoleSpinner()


// optionally set default placeholder till data arrives
        setDefaultSpinner(departmentSpinner, "Please select department")
        setDefaultSpinner(streamSpinner, "Please select stream")

        setDefaultSpinner(collegeSpinner, "Please select college")
        setDefaultSpinner(schoolSpinner, "Please select school")
        setDefaultSpinner(studyCentreSpinner, "Please select study centre")

    }

    private fun setupRoleSpinner() {
        val roles = listOf("STUDENT", "FACULTY", "STAFF", "OTHER") // No default
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRole = roles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRole = "" // Optional fallback
            }
        }
    }



    private fun setDefaultSpinner(spinner: Spinner, defaultText: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(defaultText))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }



    private fun setupSwayamsiddhaSpinner() {
        val options = arrayOf("Are you a Swayamsiddha user?","No", "Yes")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        swayamSpinner.adapter = adapter

        swayamSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 2) {
                    showExtendedForm()
                } else {
                    hideExtendedForm()
                }
            }

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


            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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
            val defaultUniversity = University(0, "Please select university" )
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

                    // ✅ Log selected college
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


//        viewModel.error.observe(this) { msg ->
//            showRetryDialog("Something went wrong while loading data.\nPlease try again after some time. ")
//        }

        // 📦 Observe registration-specific errors
        viewModel.registrationError.observe(this) { errorMessage ->
            errorMessage?.let {
                showErrorDialog(it)  // Shows "Email already exists" etc.
                viewModel.registrationError.postValue(null)
            }
        }

// 📦 Observe data loading errors (for spinners, etc.)
        viewModel.dataLoadError.observe(this) { errorMessage ->
            errorMessage?.let {
                showRetryDialog(it)  // Can pass detailed error or a generic message
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

        // show default "Please select district"
        setDefaultSpinner(districtSpinner, "Please select district")
        // show default "Please select taluka"
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
        val swayamYes = swayamSpinner.selectedItem.toString() == "Yes"

        // Basic validation
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

        // ✅ Validate role ONLY if Swayamsiddha is Yes
        if (swayamYes && selectedRole.isBlank()) {
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_SHORT).show()
            return
        }

        // 🧪 Log values for debugging
        Log.d("RegisterDebug", "UniversityId: $universityId, CollegeUserId: $selectedCollegeUserId, SchoolId: $selectedSchoolId, StudyCentreId: $selectedStudyCentreId, DeptId: $selectedDepartmentId, StreamId: $selectedStreamId")

        // ✅ Only assign subRole if Swayamsiddha is Yes
        val safeSubRole = if (swayamYes) selectedRole else null

        val request = RegisterRequest(
            userFirstName = firstName.text.toString().trim(),
            userLastName = lastName.text.toString().trim(),
            userEmail = email.text.toString().trim(),
            userGender = "FEMALE",
            userDateOfBirth = dob.text.toString().trim(),
            userAddress = address.text.toString().trim(),
            userPassword = password.text.toString().trim(),
            stateId = stateId,
            districtId = districtId,
            talukaId = talukaId,
            userMobileNumber = phone.text.toString().trim(),
            userRole = "ROLE_USER",

            // ✅ Only send subRole if Swayamsiddha is YES
            subRole = if (swayamYes) selectedRole else "",

            isSwayamsiddha = swayamYes,
            termsAndConditionsAccepted = agreeCheckBox.isChecked,

            // ✅ Only send IDs if Swayamsiddha is YES, otherwise empty string
            universityId = if (swayamYes) universityId.toString() else "",
            collegeId = if (swayamYes) selectedCollegeUserId.toString() else "",
            schoolId = if (swayamYes) selectedSchoolId.toString() else "",
            studyCentreId = if (swayamYes) selectedStudyCentreId.toString() else "",
            departmentId = if (swayamYes) selectedDepartmentId.toString() else "",
            streamId = if (swayamYes) selectedStreamId.toString() else "",

            // Optional: image URLs (dummy for now)
            userProfileImagePath = "",
            userCoverProfileImagePath = ""
        )

        Log.d("RegisterRequest", "Sending registration request: $request")
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
                retryLoadingAllData() // Retry your API calls here
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
        // Add any other APIs you want to retry
    }


}











