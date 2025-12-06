package com.example.myexpirationdate.screens


import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myexpirationdate.viewmodels.MapVM
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.myexpirationdate.TAG
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import com.example.myexpirationdate.R
import com.google.android.libraries.places.api.model.Place


@Composable
fun MapScreen(mapVM: MapVM, modifier: Modifier = Modifier) {

    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val userLocation by mapVM.userLocation
    val foodBankLocations by mapVM.foodBankLocations
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context)}
    val placesClient = remember{ Places.createClient(context) }

    //handle permission requests for fine location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //Fetch user location and update camera in the case where permission is granted
            mapVM.fetchUserLocation(context, fusedLocationClient)
        }else{
            //case where permission is denied
            Log.e(TAG, "Location permission was denied by the user")
        }
    }

    //Request permission when MapScreen is launched
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED){
            //check if location permission is already granted
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                //fetch user location and update camera
                mapVM.fetchUserLocation(context, fusedLocationClient)
            }else -> {
                //request location permission
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ){
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f)

            //Fetch nearby food bank location
            mapVM.fetchFoodBankLocations(context, placesClient)

            //loop through locations and make a marker for each one
            for (location in foodBankLocations){
                if (location.name != "Placeholder"){
                    println(location.name)

//                    Marker(
//                        state = rememberMarkerState(position = location.latLng),
//                        draggable = false,
//                        title = location.name,
//                        snippet = location.address + location.openingHours,
//                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
//                    )

                    MarkerInfoWindow(
                        state = rememberMarkerState(position = location.latLng),
                        title = location.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        onInfoWindowClick = { it ->
                            it.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        },
                        onInfoWindowClose = {
                            it.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        }
                    ){
                        FoodBankWindow(location)
                    }
                }
            }
        }
    }
}


@Composable
fun FoodBankWindow(loc: Place, modifier: Modifier = Modifier){
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(35.dp, 35.dp, 35.dp, 35.dp)
            )
    ){
        Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = loc.name!!,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            Text(
                text = loc.address!!,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            for (text in loc.openingHours!!.weekdayText){
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

        }
    }
}


