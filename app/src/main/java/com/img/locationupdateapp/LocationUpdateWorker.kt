package com.img.locationupdateapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationUpdateWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationArray = arrayListOf<Location>()
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,50)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5)
        .setMaxUpdateDelayMillis(10)
        .build()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                Log.d("latLong", "onLocationResult: lat $latitude ,long $longitude")
                locationArray.add(it)
                updateDataWithNewLocation(locationArray)
            }
        }
    }

    override fun doWork(): Result {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "onStartCommand: Not Granted")
        }else{
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        return Result.success()
    }

    private fun updateDataWithNewLocation(location: ArrayList<Location>) {
        val intent = Intent("LOCATION_UPDATE_ACTION")
        intent.putParcelableArrayListExtra("EXTRA_LOCATION", location)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }


}