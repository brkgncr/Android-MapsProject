package com.burak.mapsproject


import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.burak.mapsproject.databinding.ActivityMapsBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    var followBoolean : Boolean? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
        sharedPreferences = getSharedPreferences("com.burak.mapsproject", MODE_PRIVATE)
        followBoolean = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                followBoolean = sharedPreferences.getBoolean("followBoolean",false)
                if(!followBoolean!!) {
                    mMap.clear()
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Your location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,14f))
                    sharedPreferences.edit().putBoolean("followBoolean",true).apply()
                }
            }
        }

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.root,"Permission needed to get the location",Snackbar.LENGTH_INDEFINITE).setAction(
                    "Allow!"
                ) {
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            val userLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(userLastLocation != null) {
                val lastLocationLatLng = LatLng(userLastLocation.latitude,userLastLocation.longitude)
                mMap.addMarker(MarkerOptions().position(lastLocationLatLng).title("Your location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng,14f))
            }
        }
    }

    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result) {
                if(ContextCompat.checkSelfPermission(this@MapsActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val userLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(userLastLocation != null) {
                        val lastLocationLatLng = LatLng(userLastLocation.latitude,userLastLocation.longitude)
                        mMap.addMarker(MarkerOptions().position(lastLocationLatLng).title("Your location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng,14f))
                    }
                }
            } else  {
                Toast.makeText(this@MapsActivity,"Permission needed to get the location", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()

        // geocoder

        val geocoder = Geocoder(this, Locale.getDefault())
        var address = ""

        try {
            val list = geocoder.getFromLocation(p0.latitude,p0.longitude,1,Geocoder.GeocodeListener { addressList ->
                val firstAddress = addressList.first()

                val countryName = firstAddress.countryName
                val street = firstAddress.thoroughfare
                val alley = firstAddress.subThoroughfare
                address += street
                address += alley
                println(address)

            })
        } catch (e : Exception) {
            e.printStackTrace()
        }

        mMap.addMarker(MarkerOptions().position(p0))
    }
}