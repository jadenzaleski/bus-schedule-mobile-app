package edu.miamioh.csi.capstone.busapp.views

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.footerPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.sliderPreference
import me.zhanghai.compose.preference.switchPreference

@Composable
fun SettingsView() {
    SetTheme { ProvidePreferenceLocals { SettingScreen() } }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingScreen() {
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.weight(1f),
                color = onBackgroundColor
            )
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = onBackgroundColor
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(4.dp)) {
            switchPreference(
                key = "measurementUnit",
                defaultValue = false,
                title = { Text(text = "Unit of Measurement") },
                summary = { Text(text = if (it) "Kilometers" else "Miles") }
            )
            sliderPreference(
                key = "fontSize",
                defaultValue = 1f,
                title = { Text(text = "Font Size") },
                valueRange = 0f..2f,
                valueSteps = 1,
                //summary = { Text(text = "Slide to adjust") },
                valueText = {
                    if (it == 0f) {
                        Text(text = "Small")
                    } else if (it == 1f) {
                        Text(text = "Medium")
                    } else {
                        Text(text = "Large")
                    }
                }
            )
            preference(
                key = "updateData",
                title = { Text(text = "Update Data") },
                summary = { Text(text = "Last Update: 4:00 AM") }
            ) {}
            preference(
                key = "about",
                title = { Text(text = "About/App Info") },
                summary = { Text(text = "Learn more about this app") }
            ) {}
            preference(
                key = "help",
                title = { Text(text = "Help") },
                summary = { Text(text = "Basic Troubleshooting / Frequently-Asked Questions") }
            ) {}
            footerPreference(
                key = "footer_preference",
                summary = { Text(text = "Bus App Capstone Project - All rights reserved.") }
            )
        }
    }
}

@Composable
fun SetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowBackgroundColor = colorScheme.background.toArgb()
            window.setBackgroundDrawable(ColorDrawable(windowBackgroundColor))
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = if (darkTheme) Color.Black.toArgb() else Color.White.toArgb()
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
@Preview(showBackground = true)
fun SampleAppPreview() {
    SettingsView()
}