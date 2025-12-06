package com.example.myexpirationdate.viewmodels

import android.Manifest
import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.myexpirationdate.TAG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.LocationRestriction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import java.util.Arrays

class MapVM: ViewModel() {

    //save user location as latitude and longitude
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation = _userLocation

    //save food bank locations as latitude and longitude
    val place: Place = Place.builder().setName("Placeholder").build()
    private var _foodBankLocations = mutableStateOf(List<Place>(
        20,
        init = { num -> place },
    ))
    val foodBankLocations = _foodBankLocations



    fun fetchFoodBankLocations(context: Context, placesClient: PlacesClient){

        val placeFields: List<Place.Field> = listOf<Place.Field>(
            Place.Field.LAT_LNG,
            Place.Field.ID,
            Place.Field.OPENING_HOURS,
            Place.Field.NAME,
            Place.Field.ADDRESS
        )

        val swLat = userLocation.value!!.latitude - 0.8
        val swLon = userLocation.value!!.longitude - 0.8
        val swBound = LatLng(swLat, swLon)

        val neLat = userLocation.value!!.latitude + 0.8
        val neLon = userLocation.value!!.longitude + 0.8
        val neBound = LatLng(neLat, neLon)

        val bounds = RectangularBounds.newInstance(swBound, neBound)
        val request = SearchByTextRequest.builder("food bank", placeFields)
            .setMaxResultCount(40)
            .setLocationRestriction(bounds)
            .build()

        placesClient.searchByText(request)
            .addOnSuccessListener { response ->
                val places: List<Place> = response.places
                _foodBankLocations.value = places
                Log.e(TAG, "Places worked.")
            }

    }



    // fetch user location and update state
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient){
        // check if location permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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