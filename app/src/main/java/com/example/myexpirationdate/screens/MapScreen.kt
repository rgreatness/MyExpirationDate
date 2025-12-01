package com.example.myexpirationdate.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myexpirationdate.viewmodels.CameraVM
import com.example.myexpirationdate.viewmodels.MapVM
import com.example.myexpirationdate.viewmodels.OpenAiVM
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.myexpirationdate.TAG



@Composable
fun MapScreen(mapVM: MapVM, modifier: Modifier = Modifier) {

    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val userLocation by mapVM.userLocation
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context)}

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
        }
    }

//    Column(
//        modifier = modifier.fillMaxSize(),//.padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ){


//        val uri = Uri.parse("https://www.google.com/maps/search/food+bank/@${latitude},${longitude},188000m") //""https://www.google.com/maps/search/food+bank/@${latitude},${longitude},188000m/?api=1")
//
//        Intent(Intent.ACTION_VIEW, uri).also {
//            context.startActivity(it)
//        }

//        Text("Donate or volunteer at a food bank near you!")
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        Button(
//            onClick = {
//                val uri = Uri.parse("https://www.google.com/maps/search/food+bank/@41.1630423,-80.229205,34143m")
//
//                Intent(Intent.ACTION_VIEW, uri).also {
//                    context.startActivity(it)
//                }
//            }
//        ) {
//            Text("Find Food Banks")
//        }

//    }
}
