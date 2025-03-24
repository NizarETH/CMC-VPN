package com.app.cmcvpn
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner = findViewById<Spinner>(R.id.country_spinner)
        val connectButton = findViewById<Button>(R.id.connect_button)

        connectButton.setOnClickListener {
            val country = spinner.selectedItem.toString()
            val intent = Intent(this, MyVpnService::class.java).apply {
                putExtra("COUNTRY", country)
            }
            startService(intent)
        }
    }
}