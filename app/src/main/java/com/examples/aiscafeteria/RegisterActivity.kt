package com.examples.aiscafeteria

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.examples.aiscafeteria.databinding.ActivityRegisterBinding
import com.examples.aiscafeteria.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var dbHelper: UserDatabaseHelper

    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference
        dbHelper = UserDatabaseHelper(this)

        binding.registerButton.setOnClickListener {
            val username = binding.registerNameText.text.toString().trim()
            val email = binding.registerEmailText.text.toString().trim()
            val password = binding.registerPasswordText.text.toString().trim()

            if (validateInputs(username, email, password)) {
                if (isDeviceOnline(this)) {
                    registerUser(username, email, password)
                } else {
                    showToast("Internet connection is required for registration.")
                }
            }
        }

        binding.textViewRegisterTextBelowBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateInputs(username: String, email: String, password: String): Boolean {
        return when {
            username.isBlank() -> {
                showToast("Please enter a username.")
                false
            }
            email.isBlank() -> {
                showToast("Please enter an email address.")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Please enter a valid email address.")
                false
            }
            password.isBlank() -> {
                showToast("Please enter a password.")
                false
            }
            password.length < 6 -> {
                showToast("Password must be at least 6 characters long.")
                false
            }
            else -> true
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                val user = UserModel(name = username, email = email, password = password, uid = userId)

                // Save to Firebase
                database.child("user").child(userId).setValue(user).addOnCompleteListener { dbTask ->
                    if (dbTask.isSuccessful) {
                        // Save to SQLite
                        dbHelper.addUser(user)
                        showToast("Registration successful.")
                        navigateToLogin()
                    } else {
                        showToast("Failed to save user data to Firebase.")
                    }
                }
            } else {
                handleAuthError(task.exception)
            }
        }
    }

    private fun handleAuthError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthException -> showToast("Error: ${exception.message}")
            else -> showToast("Registration failed. Please try again.")
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun isDeviceOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

