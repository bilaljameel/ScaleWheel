package com.scalewheel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colors used by [ScaleWheel]. Create with [ScaleWheelDefaults.colors]. */
@Immutable
class ScaleWheelColors(
    val backgroundColor: Color,
    val borderColor: Color,
    val majorTickColor: Color,
    val minorTickColor: Color,
    val textColor: Color,
    val selectedTextColor: Color,
    val selectionLineColor: Color,
    /** Color of the small labels under the short ticks; defaults to [textColor]. */
    val minorTextColor: Color = textColor,
    /** Color of the small label of the selected value; defaults to [selectedTextColor]. */
    val selectedMinorTextColor: Color = selectedTextColor,
    /** Color of the dark bands and edges of [ScaleWheelStyle.Metal]; black by default. */
    val metalShadowColor: Color = Color.Black,
    /**
     * Gradient (or any brush) for the outline; replaces [borderColor] when set. For
     * [ScaleWheelStyle.Metal] it replaces the automatic bezel shading.
     */
    val borderBrush: Brush? = null,
)

object ScaleWheelDefaults {
    val Shape: Shape = RoundedCornerShape(20.dp)
    val TextStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
    val MinorTextStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Normal)
    val TickSpacing = 14.dp
    val MetalShape: Shape = RoundedCornerShape(12.dp)

    internal val TickAreaHeight = 36.dp

    /** Free space above the lines; the labels get exactly the same amount below them. */
    internal val TopPadding = 26.dp
    internal val LabelAreaHeight = 26.dp


    /** Thickness of a horizontal wheel. */
    val Height = TopPadding + TickAreaHeight + LabelAreaHeight

    /** Thickness of a vertical wheel; wider than [Height] because upright labels need the room. */
    val Width = 100.dp
    internal val MajorTickHeight = 30.dp
    internal val MinorTickHeight = 16.dp
    internal val TickWidth = 1.5.dp
    internal val SelectionLineWidth = 5.dp
    internal val SlotWidth = 4.dp

    /**
     * Every color is optional. Defaults follow the system light/dark theme.
     *
     * @param backgroundColor wheel background.
     * @param borderColor thin outline around the wheel.
     * @param borderBrush gradient for the outline; replaces [borderColor] when set.
     * @param majorTickColor color of the tall (labeled) ticks.
     * @param minorTickColor color of the short ticks.
     * @param textColor color of the labels.
     * @param selectedTextColor color of the labels close to the selected value.
     * @param selectionLineColor color of the center selection line.
     * @param minorTextColor color of the small labels under the short ticks (default: [textColor]).
     * @param selectedMinorTextColor color of the small label of the selected value
     * (default: [selectedTextColor]).
     */
    @Composable
    fun colors(
        backgroundColor: Color = if (isSystemInDarkTheme()) Color(0xFF1E1F1E) else Color(0xFFF7F7F7),
        borderColor: Color = if (isSystemInDarkTheme()) Color(0xFF353735) else Color(0xFFE6E6E6),
        majorTickColor: Color = if (isSystemInDarkTheme()) Color(0xFF7A9A5A) else Color(0xFF8DB35F),
        minorTickColor: Color = if (isSystemInDarkTheme()) Color(0xFF5C7345) else Color(0xFFB3CE95),
        textColor: Color = if (isSystemInDarkTheme()) Color(0xFF8D8F8D) else Color(0xFF9A9A9A),
        selectedTextColor: Color = Color(0xFF7DBB2F),
        selectionLineColor: Color = Color(0xFF7DBB2F),
        minorTextColor: Color = textColor,
        selectedMinorTextColor: Color = selectedTextColor,
        borderBrush: Brush? = null,
    ): ScaleWheelColors = ScaleWheelColors(
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        majorTickColor = majorTickColor,
        minorTickColor = minorTickColor,
        textColor = textColor,
        selectedTextColor = selectedTextColor,
        selectionLineColor = selectionLineColor,
        minorTextColor = minorTextColor,
        selectedMinorTextColor = selectedMinorTextColor,
        borderBrush = borderBrush,
    )

    /**
     * Colors for [ScaleWheelStyle.Metal]. [backgroundColor] is the base metal tone (the chrome
     * gradient is derived from it, so a gold tone gives a gold drum), [borderColor] the bezel,
     * [majorTickColor] / [minorTickColor] the engraved slots. [shadowColor] is the color of the
     * dark bands above/below and at the ends of the drum (black by default, e.g. a deep blue gives
     * a blue-tinted drum).
     */
    fun metalColors(
        backgroundColor: Color = Color(0xFFC4C6C8),
        borderColor: Color = Color(0xFF6E7072),
        majorTickColor: Color = Color(0xFF1C1C1C),
        minorTickColor: Color = Color(0xFF3A3A3A),
        textColor: Color = Color(0xFFB8B8B8),
        selectedTextColor: Color = Color(0xFF8BD13A),
        selectionLineColor: Color = Color(0xFF7DBB2F),
        minorTextColor: Color = textColor,
        selectedMinorTextColor: Color = selectedTextColor,
        shadowColor: Color = Color.Black,
        borderBrush: Brush? = null,
    ): ScaleWheelColors = ScaleWheelColors(
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        majorTickColor = majorTickColor,
        minorTickColor = minorTickColor,
        textColor = textColor,
        selectedTextColor = selectedTextColor,
        selectionLineColor = selectionLineColor,
        minorTextColor = minorTextColor,
        selectedMinorTextColor = selectedMinorTextColor,
        metalShadowColor = shadowColor,
        borderBrush = borderBrush,
    )

    /**
     * Colors for [ScaleWheelStyle.Ruler]: light gray card, soft green ticks, gray labels and a
     * green selection line. Defaults follow the system light/dark theme.
     */
    @Composable
    fun rulerColors(
        backgroundColor: Color = if (isSystemInDarkTheme()) Color(0xFF1E1F1E) else Color(0xFFF7F7F8),
        borderColor: Color = if (isSystemInDarkTheme()) Color(0xFF353735) else Color(0xFFE4E4E8),
        majorTickColor: Color = if (isSystemInDarkTheme()) Color(0xFF6F8C52) else Color(0xFFA9D67C),
        minorTickColor: Color = if (isSystemInDarkTheme()) Color(0xFF55683F) else Color(0xFFC9E5AE),
        textColor: Color = if (isSystemInDarkTheme()) Color(0xFF8D8F8D) else Color(0xFF9C9CAA),
        selectedTextColor: Color = Color(0xFF86C93A),
        selectionLineColor: Color = Color(0xFF86C93A),
        minorTextColor: Color = textColor,
        selectedMinorTextColor: Color = selectedTextColor,
        borderBrush: Brush? = null,
    ): ScaleWheelColors = ScaleWheelColors(
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        majorTickColor = majorTickColor,
        minorTickColor = minorTickColor,
        textColor = textColor,
        selectedTextColor = selectedTextColor,
        selectionLineColor = selectionLineColor,
        minorTextColor = minorTextColor,
        selectedMinorTextColor = selectedMinorTextColor,
        borderBrush = borderBrush,
    )

    /** The default colors that belong to [style]. */
    @Composable
    fun colorsFor(style: ScaleWheelStyle): ScaleWheelColors = when (style) {
        ScaleWheelStyle.Flat -> colors()
        ScaleWheelStyle.Metal -> metalColors()
        ScaleWheelStyle.Ruler -> rulerColors()
    }
}
