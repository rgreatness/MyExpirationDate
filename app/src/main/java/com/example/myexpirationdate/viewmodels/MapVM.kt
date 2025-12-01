package com.example.myexpirationdate.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.myexpirationdate.TAG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest

class MapVM: ViewModel() {

    //save user location as latitude and longitude
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation = _userLocation

    //save food bank locations as latitude and longitude
    private val _foodBankLocations = listOf(mutableStateOf<LatLng?>(null))
    val foodBankLocations = _foodBankLocations

    fun fetchFoodBankLocations(context: Context, placesClient: PlacesClient){
//        val request = SearchNearbyRequest.builder()
//            .
//            .setMaxResultCount(50)
//            .build()

        //placesClient.searchNearby()

    }



    // fetch user location and update state
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient){
        // check if location permission is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try{
                // fetch last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let{
                        //update user's location to state
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                    }
                }

            }catch(e: SecurityException){
                Log.e(TAG, "Permission for location access was revoked: ${e.localizedMessage}", e)
            }
        }else{
            Log.e(TAG, "Permission for location access was not granted")
        }
    }
}