package com.img.locationupdateapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {


    private lateinit var startBtn:Button
    private lateinit var stopBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBtn = findViewById(R.id.startBtn)
        stopBtn = findViewById(R.id.stopBtn)

        val filter = IntentFilter("LOCATION_UPDATE_ACTION")
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, filter)

        startBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),1001)
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val serviceIntent = Intent(this,ActivityTrackingService::class.java)
                    startForegroundService(serviceIntent)
                } else {
                    val serviceIntent = Intent(this,ActivityTrackingService::class.java)
                    startService(serviceIntent)
                }
            }
        }

        stopBtn.setOnClickListener {
            val serviceIntent = Intent(this,ActivityTrackingService::class.java)
            stopService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001){
            Log.d("TAG", "onRequestPermissionsResult: Granted")
        }else{
            Log.d("TAG", "onRequestPermissionsResult: Not Granted")
        }
    }


    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "LOCATION_UPDATE_ACTION") {
                val locationArray: ArrayList<Location>? = intent.getParcelableArrayListExtra("EXTRA_LOCATION")
                if (locationArray!=null&&locationArray.size >0) {
                   locationArray.forEach {location->
                       val latitude = location.latitude
                       val longitude = location.longitude
                       Log.d("latLong", "onReceive: lat $latitude ,long $longitude")
                   }
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver)
        super.onDestroy()

    }
}