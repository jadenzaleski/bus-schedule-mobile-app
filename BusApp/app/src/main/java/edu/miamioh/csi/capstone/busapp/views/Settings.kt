package edu.miamioh.csi.capstone.busapp.views


import android.view.View
import android.widget.RadioGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun SettingsView() {
    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Settings", fontWeight = FontWeight.Bold, fontSize = 32.sp, textAlign = TextAlign.Center)
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Black)),
            // horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Settings Appearance:", modifier = Modifier.padding(4.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
            ThemeSelector(onThemeSelected = { /* Handle selected theme option */ })
        }

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Black)),
            // horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Units: ", modifier = Modifier.padding(4.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
            MeasurementSelector(onMeasurementSelected = { /* */ })

        }

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Black)),
            // horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Timezone: ", modifier = Modifier.padding(4.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)

        }

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Black)),
            // horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Font size:", modifier = Modifier.padding(4.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
            TextSizeChoice(onSizeSelected = { /* */ })

        }

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Black)),
            // horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "About: ", modifier = Modifier.padding(4.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)

        }

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Black)),
            // horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Help: ", modifier = Modifier.padding(4.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)

        }
    }
}


@Composable
fun ThemeSelector(
    onThemeSelected: (ThemeOption) -> Unit
) {
    // Remembering the selected theme option
    val (selectedOption, setSelectedOption) = remember { mutableStateOf(ThemeOption.DeviceDefault) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.End

    ) {
        Row(modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Light Mode", textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedOption == ThemeOption.Light,
                onSelect = { setSelectedOption(ThemeOption.Light); onThemeSelected(ThemeOption.Light) }
            )
        }
        Row(modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Dark Mode", textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedOption == ThemeOption.Dark,
                onSelect = { setSelectedOption(ThemeOption.Dark); onThemeSelected(ThemeOption.Dark) }
            )
        }
        Row (modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Default", textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedOption == ThemeOption.DeviceDefault,
                onSelect = {
                    setSelectedOption(ThemeOption.DeviceDefault); onThemeSelected(
                    ThemeOption.DeviceDefault)
                }
            )
        }
    }
}

@Composable
fun MeasurementSelector(
    onMeasurementSelected: (MeasurementType) -> Unit
) {
    // Remembering the selected theme option
    val (selectedMeasurement, setselectedMeasurement) = remember { mutableStateOf(MeasurementType.Kilometers) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.End

    ) {
        Row(modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Kilometers", textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedMeasurement == MeasurementType.Kilometers,
                onSelect = { setselectedMeasurement(MeasurementType.Kilometers); onMeasurementSelected(MeasurementType.Kilometers) }
            )
        }
        Row(modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Miles", textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedMeasurement == MeasurementType.Miles,
                onSelect = { setselectedMeasurement(MeasurementType.Miles); onMeasurementSelected(MeasurementType.Miles) }
            )
        }
    }
}

@Composable
fun TextSizeChoice(
    onSizeSelected: (TextSize) -> Unit
) {
    // Remembering the selected theme option
    val (selectedSize, setSelectedSize) = remember { mutableStateOf(TextSize.Medium) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.End

    ) {
        Row(modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Small",  fontSize = 14.sp,  textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedSize == TextSize.Small,
                onSelect = { setSelectedSize(TextSize.Small); onSizeSelected(TextSize.Small) }
            )
        }
        Row(modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Medium", textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedSize == TextSize.Medium,
                onSelect = { setSelectedSize(TextSize.Medium); onSizeSelected(TextSize.Medium) }
            )
        }
        Row (modifier = Modifier.padding(0.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Large", fontSize = 22.sp, textAlign = TextAlign.Center)
            RadioButtonOption(
                text = "",
                selected = selectedSize == TextSize.Large,
                onSelect = {
                    setSelectedSize(TextSize.Large); onSizeSelected(
                    TextSize.Large)
                }
            )
        }
    }
}

@Composable
fun RadioButtonOption(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    RadioButton(
        selected = selected,
        onClick = onSelect,
        colors = RadioButtonDefaults.colors(selectedColor = androidx.compose.ui.graphics.Color.Blue)
    )
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp)
    )
}

enum class MeasurementType {
    Miles,
    Kilometers
}

enum class ThemeOption {
    Light,
    Dark,
    DeviceDefault
}

enum class TextSize {
    Small,
    Medium,
    Large
}

@Composable
@Preview
fun SettingsViewPreview() {
    SettingsView()
}
