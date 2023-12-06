package edu.miamioh.csi.capstone.busapp.views

import android.widget.RadioGroup
import android.widget.Switch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import edu.miamioh.csi.capstone.busapp.CSVHandler
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation

@OptIn(ExperimentalUnitApi::class)
@Composable
fun SettingsView() {

    // Array of names of each settings section
    val names = arrayOf(
        "System Appearance",
        "Units",
        "Timezone Adjustments",
        "Update Data",
        "Font Size",
        "Info/About",
        "Help",
        "Feedback"
    )

    // Details of the settings - can be/needs to be better customized
    val details = arrayOf(
        "System Appearance - details",
        "Units - details",
        "Timezone Adjustments - details",
        "Update Data - details",
        "Font Size - details",
        "Info/About - details",
        "Help - details",
        "Feedback - details"
    )

    // creates the LazyColumn based on the names array as titles
    MySettings(names, details)
}


@Composable
fun MySettings(names: Array<String>, details: Array<String>, modifier: Modifier = Modifier) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        val itemcount = names.size
        items(itemcount) {item ->
            // calls ColumnItem to create a card and then populate the card
            // based on RowContent and the names array.
            ColumnItem (
                itemIndex = item,
                title = names,
                details = details,
                modifier
            )
        }
    }
}

@Composable
fun ColumnItem(itemIndex: Any,
               title: Array<String>,
               details: Array<String>,
               modifier: Modifier) {
    Card (
        modifier
            .padding(2.dp)
            .wrapContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(10.dp)
    ) {
        //Inside of the card: has a row and then in each is a column of
        //the text and calls RowContent to add the correct contents to the card
        Row (modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            Column (modifier.padding(12.dp)) {
                Text(text = title[itemIndex as Int],
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp)
                Text(text = details[itemIndex],
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp)
                RowContent(index = itemIndex)
            }
        }
    }
}

@Composable
fun RowContent(index: Int) {

    // checks the index and calls the needed function to populate the card
    if (index == 0) {
        SystemApperanceContent()
    } else if (index == 1) {
        UnitsContent()
    } else if (index == 2) {
        TimeZoneContent()
    } else if (index == 3) {
        UpdateDataContent()
    } else if (index == 4) {
        FontSizeContent()
    } else if (index == 5) {
        AboutInfoContent()
    } else if (index == 6) {
        HelpContent()
    } else if (index == 7) {
        FeedbackContent()
    }
}

@Composable
fun SystemApperanceContent() {
    // Populates the radio button with light mode, dark mode, and
    // device settings options
    Text(text = "System appearance (Light / Dark / System or Default).", fontSize = 16.sp)
    AppearanceRadioButton()
}

@Composable
fun UnitsContent() {
    // Populates a switch with the option to pick the measurements on the
    // app to be Kilometers or Miles
    Text(text = "Units (km/mi)", fontSize = 16.sp)
    SwitchMeasurementChoice()
}

@Composable
fun TimeZoneContent() {
    // Populates a dropdown that gives the options for a timezone to
    // be selected from 00:00 to 23:00.
    Text(text = "Timezone adjustment", fontSize = 16.sp)
    TimeZoneDropDown()

}

@Composable
fun UpdateDataContent() {
    // creates a button that is not implemented to update the data pulled
    // from the CSVHander.kt if needed
    Text(text = "Update data", fontSize = 16.sp)
    UpdateButton()
}

@Composable
fun FontSizeContent() {
    // creates radio button for the user to pick if they want
    // regular, large or extra large font.
    Text(text = "Font size / type", fontSize = 16.sp)
    FontSizeRadioButtons()
}


@Composable
fun AboutInfoContent() {
    // basic card for Info/About the app
    // NEEDS TO BE IMPLEMENTED
    Text(text = "Info/about", fontSize = 16.sp)
}

@Composable
fun HelpContent() {
    // basic card for help the app
    // NEEDS TO BE IMPLEMENTED
    Text(text = "Get Help at: ", fontSize = 20.sp)
    Text(text = "borrorkn@miamioh.edu", fontSize = 12.sp, color = Color.Blue)

}

@Composable
fun FeedbackContent() {
    // basic card for Info the app
    // NEEDS TO BE IMPLEMENTED
    Text(text = "Send any feedback to: ", fontSize = 20.sp)
    Text(text = "ferrenam@miamioh.edu", fontSize = 12.sp, color = Color.Blue)
}

@Composable
@Preview
fun SettingsViewPreview() {
    SettingsView()
}

@Composable
fun AppearanceRadioButton() {
    val radioOptions = listOf("Light", "Dark", "Device Settings")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1] ) }
    Column {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = {
                            onOptionSelected(text)
                        }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = { onOptionSelected(text) }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 16.dp, top = 14.dp)
                )
            }
        }
    }
}

@Composable
fun SwitchMeasurementChoice() {
    val checkedValue = remember { mutableStateOf(true) }
    Column {
        Switch(checked = checkedValue.value, onCheckedChange = {
            checkedValue.value = it
        })

        if (checkedValue.value) {
            Text(text = "Kilometers")
        } else {
            Text(text = "Miles")
        }
        //Text(text = "${checkedValue.value}")
    }

}

@Composable
fun TimeZoneDropDown() {
    val timeZoneList = listOf("00:00", "01:00", "02:00", "03:00",
                              "04:00", "05:00", "06:00", "07:00",
                              "08:00", "09:00", "10:00", "11:00",
                              "12:00", "13:00", "14:00", "15:00",
                              "16:00", "17:00", "18:00", "19:00",
                              "20:00", "21:00", "22:00", "23:00")
    val expanded = remember { mutableStateOf(false) }
    val currentValue = remember { mutableStateOf(timeZoneList[0]) }

    Surface (modifier = Modifier.fillMaxSize()) {
        Box (modifier = Modifier.fillMaxWidth()) {
            Row (modifier = Modifier
                .clickable {
                    expanded.value = !expanded.value
                }
                .align(Alignment.CenterStart)) {
                Text(text = currentValue.value)
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)

            }
        }
    }
    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        timeZoneList.forEach {
            DropdownMenuItem(onClick = {
                currentValue.value = it
                expanded.value = false
            }) {
                Text(text = it)
            }
        }
    }
}

@Composable
fun FontSizeRadioButtons() {
    val radioOptions = listOf("Regular", "Large", "Extra Large")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1] ) }
    Column {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = {
                            onOptionSelected(text)
                        }
                    )
                    .padding(horizontal = 16.dp)

            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = { onOptionSelected(text) }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 16.dp, top = 14.dp)
                )
            }
        }
    }
}

@Composable
fun UpdateButton() {
    FilledTonalButton(onClick = {
        //your onclick code here
    },

        ) {
        Text(text = "Data Button")
    }
}