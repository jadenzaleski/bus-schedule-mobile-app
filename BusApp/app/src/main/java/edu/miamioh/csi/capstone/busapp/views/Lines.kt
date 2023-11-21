package edu.miamioh.csi.capstone.busapp.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

// ***** INACTIVE *****
@Composable
fun LinesView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LINES",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
@Preview
fun LinesViewPreview() {
    LinesView()
}