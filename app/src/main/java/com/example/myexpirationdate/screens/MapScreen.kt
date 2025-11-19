package com.example.myexpirationdate.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myexpirationdate.viewmodels.CameraVM
import com.example.myexpirationdate.viewmodels.OpenAiVM

@Composable
fun MapScreen(
    latitude: Double, longitude: Double, context: Context = LocalContext.current, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
//        val uri = Uri.parse("https://www.google.com/maps/search/food+bank/@${latitude},${longitude},188000mm") //""https://www.google.com/maps/search/food+bank/@${latitude},${longitude},188000m/?api=1")
//
//        Intent(Intent.ACTION_VIEW, uri).also {
//            context.startActivity(it)
//        }

        Text("Donate or volunteer at a food bank near you!")

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val uri = Uri.parse("https://www.google.com/maps/search/food+bank/@${latitude},${longitude},188000mm") //""https://www.google.com/maps/search/food+bank/@${latitude},${longitude},188000m/?api=1")

                Intent(Intent.ACTION_VIEW, uri).also {
                    context.startActivity(it)
                }
            }
        ) {
            Text("Find Food Banks")
        }

    }
}