package com.collage.empowermentstrishakti

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.collage.empowermentstrishakti.data.model.regi.LoginRequest
import com.collage.empowermentstrishakti.data.model.regi.LoginResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.R

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Common.UserType
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity:  AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    private lateinit var etEmailOrPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        etEmailOrPhone = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        val fullText = "Don’t have an account? Register"
        val spannableString = SpannableString(fullText)

// Find the word "Register"
        val startIndex = fullText.indexOf("Register")
        val endIndex = startIndex + "Register".length

// Create a clickable span
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open RegisterActivity
                //val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
//startActivity(intent)
                showUserTypeBottomSheet()
            }




            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE  // Optional: make it red
                ds.isUnderlineText = false // Optional: remove underline
            }
        }


        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvRegister.text = spannableString
        tvRegister.movementMethod = LinkMovementMethod.getInstance()
        tvRegister.highlightColor = Color.TRANSPARENT


        btnLogin.setOnClickListener {
            val emailOrPhone = etEmailOrPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (emailOrPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmailOrPhone(emailOrPhone)) {
                Toast.makeText(
                    this,
                    "Enter a valid email or 10-digit mobile number",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loginUser(emailOrPhone, password)
            }
        }

    }

    private fun loginUser(emailOrPhone: String, password: String) {
        progressBar.visibility = View.VISIBLE

        val request = LoginRequest(
            userEmailOrMobileNumber = emailOrPhone,
            userPassword = password
        )

        val call = ApiClient.apiService.loginUser(request)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // extract fields from your LoginResponse model
                    val userId = body.userId
                    val userFirstName = body.userFirstName ?: ""
                    val userLastName = body.userLastName ?: ""
                    val userName = if (userFirstName.isNotBlank()) "$userFirstName $userLastName".trim() else (body.userFirstName ?: "")
                    val token = body.token ?: ""

                    val uuid = body.userUUID
                    val isSwayamsiddha = body.isSwyamsiddha
                    val isAdiShakti = body.isAdiShakti

                    Log.d("ssswwe1", "Mapped roles -> isSwyamsiddha=$isSwayamsiddha, isAdiShakti=$isAdiShakti")

// Save to SharedPreferences
                    sessionManager.saveUserUuid(uuid)
                    sessionManager.setIsSwayamsiddha(isSwayamsiddha)
                    sessionManager.setIsAdiShakti(isAdiShakti)

// Log after saving
                    Log.d("ssswwe2", "Saved flags -> isSwayamsiddha=${sessionManager.isSwayamsiddha()}, isAdiShakti=${sessionManager.isAdiShakti()}")

                    // Save in SharedPreferences (SessionManager)
//                    sessionManager.saveUserData(
//                        userId = userId,
//                        userName = userName,
//                        token = token
//
//
//                    )


                    sessionManager.saveUserData(userId, userName, token)

                    sessionManager.saveUserUuid(uuid)
                    // Save UUID separately (you added this helper)



                    Log.d("LOGIN", "Saved -> userId=$userId, uuid=$uuid, tokenExists=${token.isNotEmpty()}")

                    // Navigate to main screen
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }


                else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody).getString("message")
                    } catch (e: Exception) {
                        "Login failed"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun isValidEmailOrPhone(input: String): Boolean {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        val phonePattern = Regex("^\\d{10}$")
        return emailPattern.matcher(input).matches() || phonePattern.matches(input)
    }


    private fun showUserTypeBottomSheet() {
        val sheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(
            com.collage.empowermentstrishakti.R.layout.bottomsheet_user_type,
            null
        )

        val tvNormal = view.findViewById<TextView>(
            com.collage.empowermentstrishakti.R.id.tvNormal
        )
        val tvSwayam = view.findViewById<TextView>(
            com.collage.empowermentstrishakti.R.id.tvSwayam
        )
        val tvAdi = view.findViewById<TextView>(
            com.collage.empowermentstrishakti.R.id.tvAdi
        )

        tvNormal.setOnClickListener {
            openRegister(UserType.NORMAL)
            sheet.dismiss()
        }

        tvSwayam.setOnClickListener {
            openRegister(UserType.SWAYAMSIDHA)
            sheet.dismiss()
        }

        tvAdi.setOnClickListener {
            openRegister(UserType.ADISHAKTI)
            sheet.dismiss()
        }

        sheet.setContentView(view)
        sheet.show()
    }


    private fun openRegister(userType: UserType) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("USER_TYPE", userType.name)
        startActivity(intent)
    }





}