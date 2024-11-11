package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.android.gms.common.api.ApiException

class Sign_Up : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding // ViewBinding instance
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isPasswordVisible = false // Variable to toggle password visibility
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Google Sign-In configuration
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.Client_ID)) // Your web client ID from Firebase Console
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Email Sign-Up button click
            binding.signUpButton.setOnClickListener {
            binding.UserName.text.toString()
            binding.etSignInEmail.text.toString()
            binding.etSignInPassword.text.toString()
            binding.etConfirmPassword.text.toString()

            if (binding.UserName.text.toString().isEmpty() || binding.etSignInEmail.text.toString().isEmpty() || binding.etSignInPassword.text.toString().isEmpty() || binding.etConfirmPassword.text.toString().isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (binding.etSignInPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // Proceed with Firebase email/password registration
                registerUser(binding.etSignInEmail.text.toString(), binding.etSignInPassword.text.toString())
            }
        }

        // Google Sign-In button click
        binding.googleSignUpButton.setOnClickListener {
            // Force sign out to show account picker every time
            googleSignInClient.signOut().addOnCompleteListener {
                signInWithGoogle()
            }
        }

        // Login link
        binding.tvlogin.setOnClickListener {
            binding.tvlogin.text = HtmlCompat.fromHtml("<u>Login</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Toggle password visibility
        binding.SignUpShowPassword.setOnClickListener {
            togglePasswordVisibility(binding.etSignInPassword, binding.SignUpShowPassword)
        }
    }

    // Function to register the user using Firebase Auth (email/password)
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Update the user's display name with full name input
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(binding.UserName.text.toString())
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Toast.makeText(this, "Sign-up Successful!", Toast.LENGTH_SHORT).show()
                                updateUI(auth.currentUser)
                            } else {
                                Toast.makeText(this, "Failed to set user name.", Toast.LENGTH_SHORT).show()
                                updateUI(null)
                            }
                        }
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }




    // Google Sign-In
    private fun signInWithGoogle() {
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    // Handle Google Sign-In result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                firebaseAuthWithGoogle( GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java).idToken!!)
            } catch (e: ApiException) {
                Log.w("Google Sign-In", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firebase Authentication with Google Sign-In
    private fun firebaseAuthWithGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser)
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    // Function to update the UI based on registration status
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {
            Toast.makeText(this, "Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePasswordVisibility(passwordEditText: EditText, ivShowPassword: ImageView) {
        if (isPasswordVisible) {
            // Hide Password
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivShowPassword.setImageResource(R.drawable.offpass) // Use closed-eye icon
            isPasswordVisible = false
        } else {
            // Show Password
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Use open-eye icon
            isPasswordVisible = true
        }
        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    }
