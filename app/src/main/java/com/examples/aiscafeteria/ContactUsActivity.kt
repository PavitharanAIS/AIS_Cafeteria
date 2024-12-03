package com.examples.aiscafeteria

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.examples.aiscafeteria.databinding.ActivityContactUsBinding
import android.Manifest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ContactUsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding: ActivityContactUsBinding by lazy {
        ActivityContactUsBinding.inflate(layoutInflater)
    }

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Get the SupportMapFragment and request to load the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.contactUsBackButton.setOnClickListener {
            finish()
        }

        binding.contactEmailButton.setOnClickListener {
            sendEmail()
        }

        binding.contactCallButton.setOnClickListener {
            makePhoneCall()
        }

        binding.contactWebsiteButton.setOnClickListener {
            val websiteUrl = "https://www.ais.ac.nz/"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(websiteUrl)
            }

            // Forcing intent to use a specific browser package (Google Chrome)
            intent.setPackage("com.android.chrome")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Chrome browser not found.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.facebookLogo.setOnClickListener {
            val websiteUrl = "https://www.facebook.com/"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(websiteUrl)
            }

            // Forcing intent to use a specific browser package (Google Chrome)
            intent.setPackage("com.android.chrome")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Chrome browser not found.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.instagramLogo.setOnClickListener {
            val websiteUrl = "https://www.instagram.com/?hl=en"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(websiteUrl)
            }

            // Forcing intent to use a specific browser package (Google Chrome)
            intent.setPackage("com.android.chrome")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Chrome browser not found.", Toast.LENGTH_SHORT).show()
            }
        }

        // Request necessary permissions
        requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun makePhoneCall() {
        val phoneIntent = Intent(Intent.ACTION_CALL)
        phoneIntent.data = Uri.parse("tel:+64123456789")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            startActivity(phoneIntent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        }
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@aiscafeteria.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Type Your Subject Here")
            putExtra(Intent.EXTRA_TEXT, "Type Your Email Here")
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    // Google Maps callback when the map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(-36.873166599611636, 174.72077259282335)
        googleMap.addMarker(MarkerOptions().position(location).title("AIS Cafeteria"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        // Enable location if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // Handling lifecycle events for MapView
    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}
