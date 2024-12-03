package com.examples.aiscafeteria

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.examples.aiscafeteria.databinding.ActivityStartScreenBinding

class StartScreen : AppCompatActivity() {
private val binding: ActivityStartScreenBinding by lazy {
    ActivityStartScreenBinding.inflate(layoutInflater)
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()
        binding.nextButton.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }
}