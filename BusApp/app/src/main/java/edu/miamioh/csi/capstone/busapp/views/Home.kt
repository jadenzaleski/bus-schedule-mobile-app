package edu.miamioh.csi.capstone.busapp.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.miamioh.csi.capstone.busapp.CSVHandler
import edu.miamioh.csi.capstone.busapp.*

@Composable
fun HomeView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(CSVHandler.getAgencies().size) { item ->
                // Inside this lambda create a composable for each item in the list
                ObjectListItem(item)
            }
        }
    }
}

/**
 * Displays the text of the desired item from a list.
 * @property item is the current index of the list.
 */
@Composable
fun ObjectListItem(item: Int) {
    val a: Agency = CSVHandler.getAgencies()[item]
    Text(
        text = a.toString(),
        color = Color.White,
        modifier = Modifier.padding(10.dp)
    )
}

@Composable
@Preview
fun HomeViewPreview() {
    HomeView()
}