package com.collage.empowermentstrishakti

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Common.UserType
import com.collage.empowermentstrishakti.data.model.regi.*
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.UserProfileRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.RegisterViewModel

class RoleFormActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel
    private lateinit var sessionManager: SessionManager

    // Layouts
    private lateinit var layoutSwayam: LinearLayout
    private lateinit var layoutAdi: LinearLayout

    // SWAYAMSIDHA
    private lateinit var roleSpinner: Spinner
    private lateinit var departmentSpinner: Spinner
    private lateinit var streamSpinner: Spinner
    private lateinit var universitySpinner: Spinner
    private lateinit var collegeSpinner: Spinner
    private lateinit var schoolSpinner: Spinner
    private lateinit var studyCentreSpinner: Spinner

    // ADISHAKTI
    private lateinit var spinnerLocalBodyType: Spinner
    private lateinit var etLocalBodyName: EditText
    private lateinit var etWardNo: EditText

    private lateinit var btnContinue: Button

    // Selected IDs
    private var universityId = 0
    private var selectedCollegeUserId = 0
    private var selectedSchoolId = 0
    private var selectedStudyCentreId = 0
    private var selectedDepartmentId = 0
    private var selectedStreamId = 0
    private var selectedSubRole: String? = null

    private val localBodyDisplayList = listOf("Select Local Body Type", "Gram Panchayat", "Nagar Palika", "Mahanagar Palika")
    private val localBodyTypeMap = mapOf(
        "Gram Panchayat" to "GRAM_PANCHAYAT",
        "Nagar Palika" to "NAGAR_PALIKA",
        "Mahanagar Palika" to "MAHANAGAR_PALIKA"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_role_form)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        viewModel = RegisterViewModel(UserProfileRepository(ApiClient.apiService, sessionManager, this))

        initViews()

        val userType = UserType.valueOf(intent.getStringExtra("USER_TYPE") ?: "SWAYAMSIDHA")
        setupUIByRole(userType)
        setupObservers()

        if (userType == UserType.SWAYAMSIDHA) {
            viewModel.fetchUniversities()
            viewModel.fetchDepartments()
            viewModel.fetchStreams()
        }
    }

    private fun initViews() {
        layoutSwayam = findViewById(R.id.layoutSwayam)
        layoutAdi = findViewById(R.id.layoutAdi)

        roleSpinner = findViewById(R.id.spinner_role)
        departmentSpinner = findViewById(R.id.spinner_departments)
        streamSpinner = findViewById(R.id.spinner_stream)
        universitySpinner = findViewById(R.id.spinner_university)
        collegeSpinner = findViewById(R.id.spinner_college)
        schoolSpinner = findViewById(R.id.spinner_schools)
        studyCentreSpinner = findViewById(R.id.spinner_studycentre)

        spinnerLocalBodyType = findViewById(R.id.spinner_local_body_type)
        etLocalBodyName = findViewById(R.id.et_local_body_name)
        etWardNo = findViewById(R.id.et_ward_no)

        btnContinue = findViewById(R.id.btnContinue)
        btnContinue.setOnClickListener { onContinueClicked() }

        // Set default spinners
        listOf(departmentSpinner, streamSpinner, collegeSpinner, schoolSpinner, studyCentreSpinner)
            .forEach { setDefaultSpinner(it, "Please select") }
    }

    private fun onContinueClicked() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userType = UserType.valueOf(intent.getStringExtra("USER_TYPE") ?: "SWAYAMSIDHA")

        when (userType) {
            UserType.SWAYAMSIDHA -> {
                if (selectedSubRole == null || universityId == 0) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return
                }

                viewModel.updateUserRole(
                    userId = userId,
                    isSwayamsiddha = true,
                    isAdiShakti = false,
                    subRole = selectedSubRole,
                    universityId = universityId,
                    collegeId = selectedCollegeUserId.takeIf { it != 0 },
                    departmentId = selectedDepartmentId.takeIf { it != 0 },
                    streamId = selectedStreamId.takeIf { it != 0 },
                    studyCentreId = selectedStudyCentreId.takeIf { it != 0 },
                    schoolId = selectedSchoolId.takeIf { it != 0 }
                )
            }

            UserType.ADISHAKTI -> {
                val selectedLocalBody = spinnerLocalBodyType.selectedItem.toString()
                if (selectedLocalBody == "Select Local Body Type" ||
                    etLocalBodyName.text.isNullOrBlank() ||
                    etWardNo.text.isNullOrBlank()
                ) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return
                }

                viewModel.updateUserRole(
                    userId = userId,
                    isSwayamsiddha = false,
                    isAdiShakti = true,
                    localBodyType = localBodyTypeMap[selectedLocalBody],
                    localBodyName = etLocalBodyName.text.toString(),
                    wardNo = etWardNo.text.toString()
                )
            }

            else -> Unit
        }
    }

    private fun setupUIByRole(userType: UserType) {
        layoutSwayam.visibility = if (userType == UserType.SWAYAMSIDHA) View.VISIBLE else View.GONE
        layoutAdi.visibility = if (userType == UserType.ADISHAKTI) View.VISIBLE else View.GONE

        if (userType == UserType.SWAYAMSIDHA) setupSwayamRoleSpinner()
        if (userType == UserType.ADISHAKTI) setupLocalBodySpinner()
    }

    private fun setupObservers() {
        viewModel.updateSuccess.observe(this) {
            Toast.makeText(this, "Role updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.universityList.observe(this) { list ->
            val fullList = listOf(University(0, "Please select university")) + list
            universitySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fullList.map { it.institutionName })
            universitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    universityId = fullList[position].universityId
                    if (universityId != 0) {
                        viewModel.fetchColleges(universityId)
                        viewModel.fetchSchools(universityId)
                        viewModel.fetchStudyCentres(universityId)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.collegeList.observe(this) { list ->
            val fullList = listOf(College(0, "Please select college", "")) + list
            collegeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fullList.map { it.institutionName })
            collegeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedCollegeUserId = fullList[position].userId
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.schoolList.observe(this) { list ->
            val fullList = listOf(School(0, "Please select school", "", null)) + list
            schoolSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fullList.map { it.schoolName })
            schoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedSchoolId = fullList[position].schoolId
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.studyCentreList.observe(this) { list ->
            val fullList = listOf(StudyCentre(0, "Please select study centre", "", null)) + list
            studyCentreSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fullList.map { it.studyCentreName })
            studyCentreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedStudyCentreId = fullList[position].studyCentreId
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.departmentList.observe(this) { list ->
            val fullList = listOf(Department(0, "", "Please select department", null)) + list
            departmentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fullList.map { it.name })
            departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedDepartmentId = fullList[position].id
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        viewModel.streamList.observe(this) { list ->
            val fullList = listOf(StreamItem(0, "", "Please select stream", null, null)) + list
            val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fullList.map { it.streamName }) {
                override fun isEnabled(position: Int) = position != 0
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent) as TextView
                    view.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                    return view
                }
            }
            streamSpinner.adapter = adapter
            streamSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedStreamId = fullList[position].streamId
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun setDefaultSpinner(spinner: Spinner, text: String) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(text))
    }

    private fun setupSwayamRoleSpinner() {
        val roles = listOf("Please select sub-role", "STUDENT", "FACULTY", "STAFF", "OTHER")
        roleSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSubRole = if (position == 0) null else roles[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupLocalBodySpinner() {
        spinnerLocalBodyType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, localBodyDisplayList)
    }
}
