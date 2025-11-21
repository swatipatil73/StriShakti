package com.collage.new_strishakti

// Replace package above with your actual package if different.

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Factory.EventViewModelFactory
import com.collage.new_strishakti.data.model.Event.EventCategory
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.repository.EventRepository
import com.collage.new_strishakti.databinding.ActivityCreateEventBinding
import com.collage.new_strishakti.ui.RegisterViewModel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.Calendar

/**
 * Full CreateEventActivity — copy/paste into your project.
 * - Initializes binding first
 * - Uses ApiClient.apiService + SessionManager to construct EventRepository
 * - Observes viewModel.districts to populate district spinner
 * - DatePicker & TimePicker wired to non-editable EditTexts
 * - Image picker via ActivityResultContracts.OpenDocument (persist permission)
 * - Builds multipart and calls viewModel.createEvent(...)
 *
 * Make sure:
 *  - ApiClient, EventRepository (constructor with ApiService, SessionManager),
 *    EventViewModel, EventViewModelFactory, SessionManager, District, EventCategory,
 *    CreateEventResponse etc. exist in your project.
 *  - Your ApiService.createEvent() signature matches the multipart keys used below.
 */
class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var repo: EventRepository
    private lateinit var factory: EventViewModelFactory
    private lateinit var session: SessionManager

    private var selectedImageUri: Uri? = null
    private var selectedCategory: EventCategory? = null
    private var selectedDistrict: District? = null

    // paging/defaults (if you want to load events later)
    private val defaultStateId = 14
    private val initialPage = 0
    private val pageSize = 20

    // ActivityResult launcher for picking image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                // persist permission so we can read later
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedImageUri = it
                binding.ivPreview.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize binding first
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // edge-to-edge padding using binding.root
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        enableEdgeToEdge() // if you use this helper (keep or remove as needed)

        // init session, repo, viewmodel
        session = SessionManager(this)

        // Recommended: repository that accepts ApiService + SessionManager
        repo = EventRepository()
        factory = EventViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory).get(EventViewModel::class.java)

        // Observe LiveData (districts, categories, create result)
        observeViewModel()

        // Setup UI spinners and clicks (date/time wiring below)
        setupStaticSpinners()
        setupClicks()

        // Load categories and districts
        // viewModel.loadCategories() -> repository will use session token internally
        viewModel.loadCategories(session.getToken()?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" } ?: "")
        viewModel.loadDistricts(defaultStateId)
    }

    private fun observeViewModel() {
        // categories -> populate category spinner
        viewModel.categories.observe(this) { list ->
            populateCategorySpinner(list)
        }

        // districts -> populate district spinner
        viewModel.districts.observe(this) { districts ->
            populateDistrictSpinner(districts)
        }

        // create result
        viewModel.createResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    val resp = it.getOrNull()
                    Toast.makeText(this, resp?.message ?: "Event created", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val err = it.exceptionOrNull()?.message ?: "Create failed"
                    Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                }
                viewModel.clearCreateResult()
            }
        }

        // error handling (if you have viewModel.error)
        viewModel.error.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun populateCategorySpinner(list: List<EventCategory>) {
        val spinnerList = ArrayList<EventCategory>()
        spinnerList.add(EventCategory(0, "Select Event Category", null, null))
        spinnerList.addAll(list)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = spinnerList[position].takeIf { it.catId != 0 }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }
    }

    private fun populateDistrictSpinner(districts: List<District>?) {
        val list = ArrayList<District>()
        list.add(District(0, "Select District"))
        if (!districts.isNullOrEmpty()) list.addAll(districts)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistrict.adapter = spinnerAdapter

        // auto-select districtId 26 if present (optional)
        for (i in list.indices) {
            if (list[i].districtId == 26) {
                binding.spinnerDistrict.setSelection(i)
                break
            }
        }

        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val d = parent?.getItemAtPosition(position) as? District
                selectedDistrict = d?.takeIf { it.districtId != 0 }
                // optionally load events for this district:
                // val districtId = selectedDistrict?.districtId ?: 0
                // viewModel.loadEvents(session.getUserId(), districtId, initialPage, pageSize, session.getToken())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDistrict = null
            }
        }
    }

    private fun setupStaticSpinners() {
        // Who can see events (PRIVATE, PUBLIC)
        val visibilityOptions = listOf("PRIVATE_EVENT", "PUBLIC_EVENT")
        binding.spinnerVisibility.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, visibilityOptions)

        // Event mode (Hybrid, Online, Offline)
        val modeOptions = listOf("Hybrid", "Online", "Offline")
        binding.spinnerMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modeOptions)
    }

    private fun setupClicks() {
        // Image picker
        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        // Create button
        binding.btnCreateEvent.setOnClickListener {
            createEventClicked()
        }

        // Date/time fields: open pickers
        binding.etStartDate.setOnClickListener { showDatePicker(binding.etStartDate) }
        binding.etEndDate.setOnClickListener { showDatePicker(binding.etEndDate) }

        binding.etStartTime.setOnClickListener { showTimePicker(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { showTimePicker(binding.etEndTime) }

        // prevent keyboard for these fields
        binding.etStartDate.inputType = InputType.TYPE_NULL
        binding.etEndDate.inputType = InputType.TYPE_NULL
        binding.etStartTime.inputType = InputType.TYPE_NULL
        binding.etEndTime.inputType = InputType.TYPE_NULL
    }

    private fun showDatePicker(targetEditText: EditText) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            val mm = (m + 1).toString().padStart(2, '0')
            val dd = d.toString().padStart(2, '0')
            val formatted = "$dd-$mm-$y"
            targetEditText.setText(formatted)
        }, year, month, day).show()
    }

    private fun showTimePicker(targetEditText: EditText) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, h, m ->
            val hh = h.toString().padStart(2, '0')
            val mm = m.toString().padStart(2, '0')
            val formatted = "$hh:$mm"
            targetEditText.setText(formatted)
        }, hour, minute, true).show()
    }

    // Build multipart and call viewModel
    private fun createEventClicked() {
        val token = session.getToken() ?: run {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val hostUserId = session.getUserId()
        val districtId = selectedDistrict?.districtId ?: 26 // fallback
        val eventCatgId = selectedCategory?.catId ?: 0
        if (eventCatgId == 0) {
            Toast.makeText(this, "Select category", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.etEventName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val startDate = binding.etStartDate.text.toString().trim()
        val startTime = binding.etStartTime.text.toString().trim()
        val endDate = binding.etEndDate.text.toString().trim()
        val endTime = binding.etEndTime.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val eventMode = binding.spinnerMode.selectedItem?.toString() ?: "Online"
        val postOwnerType = binding.spinnerVisibility.selectedItem?.toString() ?: "PUBLIC_EVENT"
        val virtualLink = binding.etEventLink.text.toString().trim()

        // basic validation
        if (name.isEmpty()) { Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show(); return }
        if (startDate.isEmpty() || startTime.isEmpty()) { Toast.makeText(this, "Select start date/time", Toast.LENGTH_SHORT).show(); return }

        val map = mutableMapOf<String, RequestBody>()
        fun strPart(key: String, value: String) {
            map[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        strPart("eventName", name)
        strPart("eventDescription", description)
        strPart("startDate", startDate)
        strPart("startTime", startTime)
        strPart("endDate", endDate)
        strPart("endTime", endTime)
        strPart("eventAddress", address)
        strPart("eventPostType", "event")
        strPart("eventVideoThumbnailUrl", "")
        strPart("postOwnerType", postOwnerType)
        strPart("eventMode", eventMode)
        strPart("eventNotify", "true")
        strPart("virtualEventLink", virtualLink)

        // prepare image part if exists
        val imagePart = selectedImageUri?.let { uri ->
            try {
                createImagePartFromUri(uri, "eventImageUrl")
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        // call ViewModel — it will call repository
        viewModel.createEvent(authHeader, hostUserId, districtId, eventCatgId, map, imagePart)
    }

    // helper: create MultipartBody.Part from Uri
    @Throws(IOException::class)
    private fun createImagePartFromUri(uri: Uri, partName: String): MultipartBody.Part? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val fileName = queryFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)
        inputStream.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val requestFile = tempFile.asRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(0)
            }
        }
        return name
    }
}
