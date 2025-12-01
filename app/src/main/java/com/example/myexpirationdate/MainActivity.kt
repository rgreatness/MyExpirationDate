package com.example.myexpirationdate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myexpirationdate.models.Photo
import com.example.myexpirationdate.screens.HomeScreen
import com.example.myexpirationdate.screens.MapScreen
import com.example.myexpirationdate.screens.PhotoGridScreen
import com.example.myexpirationdate.ui.theme.MyExpirationDateTheme
import com.example.myexpirationdate.viewmodels.CameraVM
import com.example.myexpirationdate.viewmodels.MapVM
import com.example.myexpirationdate.viewmodels.OpenAiVM
import com.example.myexpirationdate.viewmodels.OpenAiVMFactory
import com.google.android.libraries.places.api.Places
import kotlin.getValue


val TAG = "MY_TAG"

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val openAiVM: OpenAiVM by viewModels {
        OpenAiVMFactory()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }
        val geoApiKey = ManifestUtils.getAPIKey(this)
        if (!Places.isInitialized() && geoApiKey != null){
            Places.initialize(applicationContext, geoApiKey)
        }

        enableEdgeToEdge()
        setContent {
            MyExpirationDateTheme {
                val navController = rememberNavController()
                val cameraVM = viewModel<CameraVM>()
                val photos by cameraVM.photos.collectAsState()
                val mapVM = viewModel<MapVM>()

                NavHost(navController = navController, startDestination = "main_screen") {
                    composable("main_screen") {
                        MainScreen(
                            photos = photos,
                            cameraVM = cameraVM,
                            openAiVM = openAiVM,
                            mapVM = mapVM
                        )
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(photos: List<Photo>, cameraVM: CameraVM, openAiVM: OpenAiVM, mapVM: MapVM) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Photos", "Map")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.PhotoLibrary
                                    2 -> Icons.Default.Map
                                    else -> Icons.Default.Home
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(cameraVM, openAiVM)
                1 -> PhotoGridScreen(photos = photos)
                2 -> MapScreen(mapVM)
                else -> {
                }
            }
        }
    }
}

object ManifestUtils {
    //function to retrieve api key from AndroidManifest.xml
    fun getAPIKey(context: Context): String? {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            bundle.getString("com.google.android.geo.API_KEY")
        }catch(e: PackageManager.NameNotFoundException){
            e.printStackTrace()
            null
        }
    }
}

//used Gemini to generate logo

