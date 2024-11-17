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
import com.google.firebase.database.FirebaseDatabase

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
            PasswordVisibility()
        }
        binding.ivShowConfirmPassword.setOnClickListener {
            ConfirmPasswordVisibility()
        }
    }

    // Function to register the user using Firebase Auth (email/password)
    private fun registerUser(email: String, password: String) {
        // Temporary store the user's information in the "pending_users" node
        val userName = binding.UserName.text.toString()
        val userEmail = binding.etSignInEmail.text.toString()

        // Create a pending user object
        val pendingUser = PendingUser(
            email = userEmail,
            name = userName,
            password = password,
            status = "pending" // Mark as pending for admin approval
        )

        // Get a reference to the "pending_users" node
        val database = FirebaseDatabase.getInstance().getReference("pending_users")

        // Add the user data under the "pending_users" node
        val newUserRef = database.push() // Generate a unique key for the new user
        newUserRef.setValue(pendingUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Your sign-up request has been sent please wait for admin approval", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                } else {
                    Toast.makeText(this, "Failed to submit your request", Toast.LENGTH_SHORT).show()
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

    private fun PasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            binding.etSignInPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.SignUpShowPassword.setImageResource(R.drawable.offpass) // Use closed-eye icon
            isPasswordVisible = false
        } else {
            // Show Password
            binding.etSignInPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.SignUpShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Use open-eye icon
            isPasswordVisible = true
        }

        // Move the cursor to the end of the text
        binding.etSignInPassword.setSelection(binding.etSignInPassword.text.length)
    }

    private fun ConfirmPasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivShowConfirmPassword.setImageResource(R.drawable.offpass) // Use closed-eye icon
            isPasswordVisible = false
        } else {
            // Show Password
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivShowConfirmPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Use open-eye icon
            isPasswordVisible = true
        }

        // Move the cursor to the end of the text
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    }
