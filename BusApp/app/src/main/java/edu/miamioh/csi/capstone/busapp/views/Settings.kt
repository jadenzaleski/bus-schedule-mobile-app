package edu.miamioh.csi.capstone.busapp.views

import android.widget.ListView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.miamioh.csi.capstone.busapp.CSVHandler
import edu.miamioh.csi.capstone.busapp.Trip

@Composable
fun SettingsView() {
    val items = listOf("First Setting", "Units", "Timezone", "Refresh Data",
                        "Privacy", "Info / about", "help", "feedback")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.CenterStart
    ) {
        PopulateSettings1()
    }
}

fun PopulateSettings2(
    items: List<String>
) {

}

@Composable
fun PopulateSettings1() {
    LazyColumn {
        item {Text(text = "First Setting", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "Units", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "Timezone", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "Refresh Data", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "Privacy", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "info / about", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "help", color = Color.White, modifier = Modifier.padding(16.dp))}
        item {Text(text = "feedback", color = Color.White, modifier = Modifier.padding(16.dp))}

    }

}

@Composable
@Preview
fun SettingsViewPreview() {
    SettingsView()
}