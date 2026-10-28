package com.example.scalewheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scalewheel.ScaleWheel
import com.scalewheel.ScaleWheelDefaults
import com.scalewheel.ScaleWheelOrientation
import com.scalewheel.ScaleWheelStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.scalewheel.ui.theme.ScaleWheelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScaleWheelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Demo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Demo(modifier: Modifier = Modifier) {
    var inches by remember { mutableIntStateOf(69) }
    var showMinor by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Small numbers", modifier = Modifier.weight(1f))
            Switch(checked = showMinor, onCheckedChange = { showMinor = it })
        }
        Text("Height: ${inches / 12}'${inches % 12}\"")
        ScaleWheel(
            min = 48,
            max = 84,
            defaultValue = 69,
            majorTickInterval = 6,
            labelFormatter = { "${it / 12}'${it % 12}\"" },
            showMinorLabels = showMinor,
            minorLabelFormatter = { "${it % 12}" },
            onValueChange = { inches = it },
        )
        Text("Ruler")
        ScaleWheel(
            min = 36,
            max = 96,
            defaultValue = 68,
            style = ScaleWheelStyle.Ruler,
            majorTickInterval = 6,
            labelFormatter = { "${it / 12}'${it % 12}\"" },
            tickSpacing = 10.dp,
        )
        Text("Default look (0..100)")
        ScaleWheel(min = 0, max = 100, defaultValue = 40, showMinorLabels = showMinor)
        Text("Metal")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Metal,
            colors = ScaleWheelDefaults.metalColors(),
            showMinorLabels = showMinor,
        )
        Text("Metal (gold), own small-number colors")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Metal,
            colors = ScaleWheelDefaults.metalColors(
                backgroundColor = Color(0xFFD9B45A),
                borderColor = Color(0xFF8A6B1F),
                textColor = Color(0xFFFFE9A8),
                minorTextColor = Color(0xFFB59A5C),
                selectedTextColor = Color(0xFFFFFFFF),
                selectedMinorTextColor = Color(0xFFFFFFFF),
            ),
            showMinorLabels = showMinor,
        )
        Text("Metal (blue shadow)")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Metal,
            colors = ScaleWheelDefaults.metalColors(
                backgroundColor = Color(0xFFB8C7E0),
                borderColor = Color(0xFF4B5C80),
                shadowColor = Color(0xFF0A1633),
            ),
            showMinorLabels = showMinor,
        )
        Text("Ruler, gradient outline")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Ruler,
            colors = ScaleWheelDefaults.rulerColors(
                borderBrush = Brush.horizontalGradient(listOf(Color(0xFF86C93A), Color(0xFF2AA9E0), Color(0xFFE05CC4))),
            ),
            borderWidth = 3.dp,
            showMinorLabels = showMinor,
        )
        Text("Ruler, no outline")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Ruler,
            showBorder = false,
            showMinorLabels = showMinor,
        )
        Text("Metal, own outline")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Metal,
            colors = ScaleWheelDefaults.metalColors(
                borderBrush = Brush.verticalGradient(listOf(Color(0xFFFF8A3D), Color(0xFFB8003C))),
            ),
            borderWidth = 4.dp,
            showMinorLabels = showMinor,
        )
        Text("Metal, no outline")
        ScaleWheel(
            min = 0,
            max = 100,
            defaultValue = 40,
            style = ScaleWheelStyle.Metal,
            showBorder = false,
            showMinorLabels = showMinor,
        )
        Text("Custom colors")
        ScaleWheel(
            min = 30,
            max = 200,
            defaultValue = 70,
            colors = ScaleWheelDefaults.colors(
                backgroundColor = Color(0xFF1B1B3A),
                borderColor = Color.Transparent,
                majorTickColor = Color(0xFF8C8CE0),
                minorTickColor = Color(0xFF6464AA),
                textColor = Color(0xFFB0B0F0),
                minorTextColor = Color(0xFF7878B8),
                selectedTextColor = Color(0xFFFFC857),
                selectedMinorTextColor = Color(0xFFFFC857),
                selectionLineColor = Color(0xFFFFC857),
            ),
            showMinorLabels = showMinor,
        )

        Text("Vertical")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScaleWheel(
                min = 0,
                max = 100,
                defaultValue = 40,
                orientation = ScaleWheelOrientation.Vertical,
                showMinorLabels = showMinor,
                modifier = Modifier.height(260.dp),
            )
            ScaleWheel(
                min = 36,
                max = 96,
                defaultValue = 68,
                orientation = ScaleWheelOrientation.Vertical,
                style = ScaleWheelStyle.Ruler,
                majorTickInterval = 6,
                labelFormatter = { "${it / 12}'${it % 12}\"" },
                tickSpacing = 10.dp,
                modifier = Modifier.height(260.dp),
            )
            ScaleWheel(
                min = 0,
                max = 100,
                defaultValue = 40,
                orientation = ScaleWheelOrientation.Vertical,
                style = ScaleWheelStyle.Metal,
                showMinorLabels = showMinor,
                modifier = Modifier.height(260.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScaleWheel(
                min = 30,
                max = 200,
                defaultValue = 70,
                orientation = ScaleWheelOrientation.Vertical,
                style = ScaleWheelStyle.Metal,
                colors = ScaleWheelDefaults.metalColors(
                    backgroundColor = Color(0xFFD9B45A),
                    borderColor = Color(0xFF8A6B1F),
                ),
                modifier = Modifier.height(260.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DemoPreview() {
    ScaleWheelTheme {
        Demo()
    }
}
