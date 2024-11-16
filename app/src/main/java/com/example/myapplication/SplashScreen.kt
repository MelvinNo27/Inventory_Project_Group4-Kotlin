package com.example.myapplication
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val handler = Handler()
    private var progressStatus = 0
    private var loadingText = "LOADING"
    private val dots = arrayOf(".", "..", "...","....") // Array to animate dots
    private var currentDotIndex = 0
    private val dotDelay = 500L // Delay to switch dots (in milliseconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start rotating dots animation on "LOADING..." text
        animateLoadingText()

        // Check if there's an internet connection before starting the loading
        if (!isConnectedToInternet()) {
            // Show an alert if there's no internet connection
            showNoInternetAlert()
        } else {
            // Continue loading if there is internet connection
            progressBar()
        }
    }

    private fun animateLoadingText() {
        // Runnable to update the "LOADING..." text with dots
        val runnable = object : Runnable {
            override fun run() {
                // Set the text with the current dots
                binding.loadingText.text = "$loadingText${dots[currentDotIndex]}"

                // Update the dot index to create the dot animation
                currentDotIndex = (currentDotIndex + 1) % dots.size

                // Re-run this method every 500 milliseconds
                handler.postDelayed(this, dotDelay)
            }
        }

        // Start the animation
        handler.post(runnable)
    }

    private fun progressBar() {
        // Start the progress simulation
        Thread {
            // Now that we know there's internet, start the loading process
            while (progressStatus < 100) {
                // Increment the progress
                progressStatus += 1

                // Update the progress bar UI
                handler.post {
                    binding.progressBarr.progress = progressStatus
                }

                // Wait for 50ms before updating the progress again
                Thread.sleep(50)

                // If the progress reaches 100%, stop the loading process
                if (progressStatus == 100) {
                    handler.post {
                        // After loading reaches 100%, check user status
                        checkInternetAndUserStatus()
                    }
                }
            }
        }.start()
    }

    private fun checkInternetAndUserStatus() {
        if (isConnectedToInternet()) {
            // Proceed to check if the user is logged in or not
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User is signed in, navigate to Dashboard
                goToDashboard()
            } else {
                // User is not signed in, navigate to Login
                goToLogin()
            }
        } else {
            // No internet connection
            showNoInternetAlert()
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun goToDashboard() {
        // Start the Dashboard activity
        val intent = Intent(this, Userdashboard::class.java)
        startActivity(intent)
        finish() // Finish this activity to remove it from the back stack
    }

    private fun goToLogin() {
        // Start the Login activity
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
    private fun showNoInternetAlert() {
        // Display a toast message
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        // Show an alert dialog when there's no internet connection
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                // Launch Wi-Fi settings to allow the user to enable the internet
                openWifiSettings()
            }
            .create()

        alertDialog.show()
    }

    private fun openWifiSettings() {
        // Intent to open Wi-Fi settings
        val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    }
}


