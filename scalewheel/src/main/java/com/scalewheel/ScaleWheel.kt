package com.scalewheel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** Visual style of a [ScaleWheel]. */
enum class ScaleWheelStyle {
    /** Clean, flat ruler. */
    Flat,

    /** Brushed-metal drum with engraved slots. Pair it with [ScaleWheelDefaults.metalColors]. */
    Metal,

    /**
     * Light ruler with ticks aligned to one edge and a tall selection line. No curving, shading or
     * fading, so every value in view stays fully visible. Pair it with
     * [ScaleWheelDefaults.rulerColors].
     */
    Ruler,
}

/** Direction in which a [ScaleWheel] scrolls. Labels always stay upright. */
enum class ScaleWheelOrientation {
    Horizontal,
    Vertical,
}

/**
 * A scrollable "ruler" wheel. The selection line always stays in the center, the scale scrolls
 * underneath it and snaps to the nearest value.
 *
 * A horizontal wheel is [ScaleWheelDefaults.Height]-tall and fills the available width. A vertical
 * wheel is [ScaleWheelDefaults.Width]-wide and fills the available height, so give it a bounded
 * height (for example `Modifier.height(280.dp)`).
 *
 * @param min smallest selectable value (inclusive).
 * @param max largest selectable value (inclusive).
 * @param defaultValue initially selected value; coerced into `min..max`.
 * @param modifier modifier for the whole wheel.
 * @param onValueChange called every time the selected value changes while scrolling.
 * @param orientation [ScaleWheelOrientation.Horizontal] (default) or [ScaleWheelOrientation.Vertical].
 * @param style [ScaleWheelStyle.Flat], [ScaleWheelStyle.Metal] or [ScaleWheelStyle.Ruler].
 * @param colors background / tick / text / selection colors, see [ScaleWheelDefaults.colors],
 * [ScaleWheelDefaults.metalColors] and [ScaleWheelDefaults.rulerColors]. Defaults to the colors
 * that belong to [style].
 * @param shape shape of the background and border; `null` picks the default of [style].
 * @param showBorder draw the outline around the wheel; `false` removes it. Its color or gradient
 * comes from [ScaleWheelColors.borderColor] / [ScaleWheelColors.borderBrush].
 * @param borderWidth thickness of the outline; unspecified picks the default of [style] (1dp, or 3dp
 * for [ScaleWheelStyle.Metal]). `0.dp` also removes it.
 * @param showSelectionLine draw the colored line in the center; hidden by default for
 * [ScaleWheelStyle.Metal], where the center highlight marks the selection.
 * @param majorTickInterval a taller, labeled tick is drawn for every value that is a multiple of this.
 * @param labelFormatter converts a value to the text shown next to a major tick, e.g. feet and inches.
 * @param textStyle style of the labels (its color is overridden by [colors]).
 * @param showMinorLabels also show a small label next to every short tick. Off by default.
 * @param minorLabelFormatter text of those small labels; `null` reuses [labelFormatter]. Pass a
 * shorter formatter when the main one is wide (e.g. only the inches of a feet/inches value).
 * @param minorTextStyle style of those small labels.
 * @param tickSpacing distance between two consecutive values along the scrolling direction.
 * @param hapticFeedback vibrate lightly each time the selected value changes.
 * @param wheelEffect curve the scale like a real cylinder: ticks away from the center get
 * closer together, thinner and fainter. Set to `false` for a flat ruler. Off by default for
 * [ScaleWheelStyle.Ruler].
 */
@Composable
fun ScaleWheel(
    min: Int,
    max: Int,
    defaultValue: Int,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit = {},
    orientation: ScaleWheelOrientation = ScaleWheelOrientation.Horizontal,
    style: ScaleWheelStyle = ScaleWheelStyle.Flat,
    colors: ScaleWheelColors = ScaleWheelDefaults.colorsFor(style),
    shape: Shape? = null,
    showBorder: Boolean = true,
    borderWidth: Dp = Dp.Unspecified,
    showSelectionLine: Boolean = style != ScaleWheelStyle.Metal,
    majorTickInterval: Int = 10,
    labelFormatter: (Int) -> String = { it.toString() },
    textStyle: TextStyle = ScaleWheelDefaults.TextStyle,
    showMinorLabels: Boolean = false,
    minorLabelFormatter: ((Int) -> String)? = null,
    minorTextStyle: TextStyle = ScaleWheelDefaults.MinorTextStyle,
    tickSpacing: Dp = ScaleWheelDefaults.TickSpacing,
    hapticFeedback: Boolean = true,
    wheelEffect: Boolean = style != ScaleWheelStyle.Ruler,
) {
    require(min <= max) { "min ($min) must be <= max ($max)" }
    require(majorTickInterval > 0) { "majorTickInterval must be > 0" }

    val vertical = orientation == ScaleWheelOrientation.Vertical
    val metal = style == ScaleWheelStyle.Metal
    val metrics = metricsFor(style, vertical)
    val frameShape = shape ?: if (metal) ScaleWheelDefaults.MetalShape else ScaleWheelDefaults.Shape

    val count = max - min + 1
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = defaultValue.coerceIn(min, max) - min,
    )
    val tickSpacingPx = with(LocalDensity.current) { tickSpacing.toPx() }

    // The item sitting under the centered selection line. Only ever read in the draw / semantics
    // phases or in effects, never during composition, so scrolling does not recompose the wheel.
    val selectedIndex = remember(count, tickSpacingPx) {
        derivedStateOf {
            val passedHalf = listState.firstVisibleItemScrollOffset > tickSpacingPx / 2
            (listState.firstVisibleItemIndex + if (passedHalf) 1 else 0).coerceIn(0, count - 1)
        }
    }

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(min, count, hapticFeedback) {
        var first = true
        snapshotFlow { selectedIndex.value }
            .distinctUntilChanged()
            .collect { index ->
                if (first) {
                    first = false
                } else {
                    if (hapticFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentOnValueChange(min + index)
                }
            }
    }

    val metalShadeBrush = remember(colors.metalShadowColor, vertical) {
        metalShade(vertical, colors.metalShadowColor)
    }
    val labelStyle = remember(textStyle) { textStyle.copy(textAlign = TextAlign.Center) }
    val minorLabelStyle = remember(minorTextStyle) { minorTextStyle.copy(textAlign = TextAlign.Center) }
    val borderBrush = remember(colors.borderColor, colors.borderBrush, vertical, metal) {
        colors.borderBrush ?: if (metal) {
            stopsBrush(
                // The bezel shades across the wheel, i.e. perpendicular to the scrolling direction.
                vertical = !vertical,
                0f to lerp(colors.borderColor, Color.White, 0.55f),
                0.5f to colors.borderColor,
                1f to lerp(colors.borderColor, Color.Black, 0.6f),
            )
        } else {
            SolidColor(colors.borderColor)
        }
    }
    val strokeWidth = when {
        !showBorder -> 0.dp
        borderWidth != Dp.Unspecified -> borderWidth
        metal -> 3.dp
        else -> 1.dp
    }

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (vertical) {
                    Modifier.fillMaxHeight().width(metrics.thickness)
                } else {
                    Modifier.fillMaxWidth().height(metrics.thickness)
                },
            )
            .clip(frameShape)
            .then(
                if (metal) {
                    Modifier.metalSurface(colors.backgroundColor, colors.metalShadowColor, vertical)
                } else {
                    Modifier.background(colors.backgroundColor)
                },
            )
            .then(if (strokeWidth > 0.dp) Modifier.border(strokeWidth, borderBrush, frameShape) else Modifier)
            .semantics { stateDescription = labelFormatter(min + selectedIndex.value) },
    ) {
        val sidePadding = (((if (vertical) maxHeight else maxWidth) - tickSpacing) / 2).coerceAtLeast(0.dp)
        val flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Center)

        val itemContent: @Composable (Int) -> Unit = { index ->
            val value = min + index
            val isMajor = Math.floorMod(value, majorTickInterval) == 0
            Tick(
                modifier = if (wheelEffect) Modifier.wheelPosition(listState, index, vertical) else Modifier,
                scaleModifier = if (wheelEffect) Modifier.wheelScale(listState, index, vertical) else Modifier,
                spacing = tickSpacing,
                vertical = vertical,
                metal = metal,
                metrics = metrics,
                isMajor = isMajor,
                label = when {
                    isMajor -> labelFormatter(value)
                    showMinorLabels -> (minorLabelFormatter ?: labelFormatter)(value)
                    else -> null
                },
                labelColor = {
                    val selected = selectedIndex.value
                    val highlighted = if (isMajor) abs(index - selected) < majorTickInterval else index == selected
                    when {
                        isMajor && highlighted -> colors.selectedTextColor
                        isMajor -> colors.textColor
                        highlighted -> colors.selectedMinorTextColor
                        else -> colors.minorTextColor
                    }
                },
                tickColor = if (isMajor) colors.majorTickColor else colors.minorTickColor,
                textStyle = if (isMajor) labelStyle else minorLabelStyle,
            )
        }

        if (vertical) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = sidePadding),
            ) {
                items(count) { itemContent(it) }
            }
        } else {
            LazyRow(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(horizontal = sidePadding),
            ) {
                items(count) { itemContent(it) }
            }
        }

        if (wheelEffect) {
            if (metal) {
                // Cylinder lighting: darkness grows smoothly from the middle towards both ends,
                // plus a soft highlight exactly behind the selection line.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(metalShadeBrush)
                        .background(if (vertical) MetalHighlightVertical else MetalHighlightHorizontal),
                )
            } else {
                // Edge shading: the drum fades into the background where it turns away.
                val shade = lerp(colors.backgroundColor, Color.Black, 0.05f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            stopsBrush(
                                vertical,
                                0f to shade.copy(alpha = 0.9f),
                                0.25f to shade.copy(alpha = 0f),
                                0.75f to shade.copy(alpha = 0f),
                                1f to shade.copy(alpha = 0.9f),
                            ),
                        ),
                )
            }
        }

        if (showSelectionLine) {
            Box(
                modifier = (
                    if (vertical) {
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = metrics.startPadding)
                            .height(metrics.selectionLineWidth)
                            .width(metrics.tickAreaSize)
                    } else {
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = metrics.startPadding)
                            .width(metrics.selectionLineWidth)
                            .height(metrics.tickAreaSize)
                    }
                    )
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = colors.selectionLineColor,
                        spotColor = colors.selectionLineColor,
                    )
                    .clip(RoundedCornerShape(50))
                    .background(colors.selectionLineColor),
            )
        }
    }
}

@Composable
private fun Tick(
    modifier: Modifier,
    scaleModifier: Modifier,
    spacing: Dp,
    vertical: Boolean,
    metal: Boolean,
    metrics: WheelMetrics,
    isMajor: Boolean,
    label: String?,
    labelColor: () -> Color,
    tickColor: Color,
    textStyle: TextStyle,
) {
    val tickLength = if (isMajor) ScaleWheelDefaults.MajorTickHeight else ScaleWheelDefaults.MinorTickHeight
    Box(
        modifier = modifier
            .then(
                if (vertical) {
                    Modifier.height(spacing).width(metrics.thickness).padding(start = metrics.startPadding)
                } else {
                    Modifier.width(spacing).height(metrics.thickness).padding(top = metrics.startPadding)
                },
            ),
    ) {
        Box(
            modifier = scaleModifier
                .align(if (vertical) Alignment.CenterStart else Alignment.TopCenter)
                .then(
                    if (vertical) {
                        Modifier.height(spacing).width(metrics.tickAreaSize)
                    } else {
                        Modifier.width(spacing).height(metrics.tickAreaSize)
                    },
                )
                .drawBehind {
                    val length = tickLength.toPx()
                    // `along` runs in the scrolling direction, `across` perpendicular to it.
                    val along = if (vertical) size.height else size.width
                    val across = if (vertical) size.width else size.height
                    val inset = metrics.tickBottomInset
                    val start = if (inset != null) across - inset.toPx() - length else (across - length) / 2f
                    val mid = along / 2f
                    if (metal) {
                        // Engraved slot: a light lip underneath, the dark recess on top of it.
                        val w = ScaleWheelDefaults.SlotWidth.toPx()
                        val corner = CornerRadius(w / 2f)
                        val slotSize = if (vertical) Size(length, w) else Size(w, length)
                        val slotTopLeft = if (vertical) Offset(start, mid - w / 2f) else Offset(mid - w / 2f, start)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.7f),
                            topLeft = slotTopLeft + Offset(0f, 1.dp.toPx()),
                            size = slotSize,
                            cornerRadius = corner,
                        )
                        drawRoundRect(
                            color = tickColor,
                            topLeft = slotTopLeft,
                            size = slotSize,
                            cornerRadius = corner,
                        )
                    } else {
                        drawLine(
                            color = tickColor,
                            start = if (vertical) Offset(start, mid) else Offset(mid, start),
                            end = if (vertical) Offset(start + length, mid) else Offset(mid, start + length),
                            strokeWidth = ScaleWheelDefaults.TickWidth.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                },
        )
        if (label != null) {
            Box(
                modifier = Modifier
                    .align(if (vertical) Alignment.CenterEnd else Alignment.BottomCenter)
                    .then(
                        if (vertical) {
                            Modifier.height(spacing).width(metrics.labelAreaSize)
                        } else {
                            Modifier.width(spacing).height(metrics.labelAreaSize)
                        },
                    ),
                contentAlignment = metrics.labelAlignment,
            ) {
                BasicText(
                    text = label,
                    style = textStyle,
                    maxLines = 1,
                    softWrap = false,
                    color = labelColor,
                    modifier = scaleModifier.wrapContentSize(unbounded = true),
                )
            }
        }
    }
}

/**
 * Layout across the wheel (perpendicular to the scrolling direction), in the order
 * start padding, tick area, label area; every variant adds up to [thickness].
 */
private class WheelMetrics(
    val thickness: Dp,
    val startPadding: Dp,
    val tickAreaSize: Dp,
    /** Distance between the far end of the ticks and the tick area; `null` centers the ticks. */
    val tickBottomInset: Dp?,
    val selectionLineWidth: Dp,
    val labelAlignment: Alignment,
) {
    val labelAreaSize: Dp get() = thickness - startPadding - tickAreaSize
}

private val FlatMetricsHorizontal = WheelMetrics(
    thickness = ScaleWheelDefaults.Height,
    startPadding = ScaleWheelDefaults.TopPadding,
    tickAreaSize = ScaleWheelDefaults.TickAreaHeight,
    tickBottomInset = null,
    selectionLineWidth = ScaleWheelDefaults.SelectionLineWidth,
    labelAlignment = Alignment.Center,
)

private val FlatMetricsVertical = WheelMetrics(
    thickness = ScaleWheelDefaults.Width,
    startPadding = 22.dp,
    tickAreaSize = ScaleWheelDefaults.TickAreaHeight,
    tickBottomInset = null,
    selectionLineWidth = ScaleWheelDefaults.SelectionLineWidth,
    labelAlignment = Alignment.Center,
)

private val RulerMetricsHorizontal = WheelMetrics(
    thickness = ScaleWheelDefaults.Height,
    startPadding = 12.dp,
    tickAreaSize = 52.dp,
    tickBottomInset = 12.dp,
    selectionLineWidth = 6.dp,
    labelAlignment = Alignment.TopCenter,
)

private val RulerMetricsVertical = WheelMetrics(
    thickness = ScaleWheelDefaults.Width,
    startPadding = 12.dp,
    tickAreaSize = 52.dp,
    tickBottomInset = 12.dp,
    selectionLineWidth = 6.dp,
    labelAlignment = Alignment.CenterStart,
)

private fun metricsFor(style: ScaleWheelStyle, vertical: Boolean): WheelMetrics = when {
    style == ScaleWheelStyle.Ruler && vertical -> RulerMetricsVertical
    style == ScaleWheelStyle.Ruler -> RulerMetricsHorizontal
    vertical -> FlatMetricsVertical
    else -> FlatMetricsHorizontal
}

/** A gradient that runs top to bottom when [vertical], otherwise left to right. */
private fun stopsBrush(vertical: Boolean, vararg stops: Pair<Float, Color>): Brush =
    if (vertical) Brush.verticalGradient(*stops) else Brush.horizontalGradient(*stops)

/**
 * Brushed metal: a chrome gradient across the wheel (equally dark bands on both sides, bright
 * specular band in the middle, where the slots sit) with faint brush streaks along the scrolling
 * direction.
 */
private fun Modifier.metalSurface(base: Color, shadow: Color, vertical: Boolean): Modifier = drawWithCache {
    val edge = lerp(base, shadow, 0.92f)
    val shade = lerp(base, shadow, 0.72f)
    val mid = lerp(base, Color.White, 0.15f)
    val gloss = lerp(base, Color.White, 0.65f)
    val brush = if (vertical) {
        // The lines sit left of center, with the dark label column on the right.
        Brush.horizontalGradient(
            0f to edge,
            0.05f to edge,
            0.14f to shade,
            0.2f to mid,
            0.27f to gloss,
            0.5f to gloss,
            0.58f to mid,
            0.66f to shade,
            0.8f to edge,
            1f to edge,
        )
    } else {
        Brush.verticalGradient(
            0f to edge,
            0.1f to edge,
            0.24f to shade,
            0.32f to mid,
            0.4f to gloss,
            0.6f to gloss,
            0.68f to mid,
            0.76f to shade,
            0.9f to edge,
            1f to edge,
        )
    }
    val step = 2.dp.toPx()
    val light = Color.White.copy(alpha = 0.06f)
    val dark = Color.Black.copy(alpha = 0.06f)
    onDrawBehind {
        drawRect(brush)
        val extent = if (vertical) size.width else size.height
        var p = 0f
        var i = 0
        while (p < extent) {
            val color = if (i % 2 == 0) light else dark
            if (vertical) {
                drawLine(color, Offset(p, 0f), Offset(p, size.height), strokeWidth = 1f)
            } else {
                drawLine(color, Offset(0f, p), Offset(size.width, p), strokeWidth = 1f)
            }
            p += step
            i++
        }
    }
}

/** Rotation (in radians) of the ticks at the very edge of the wheel. */
private const val MaxWheelAngle = 1.4f

/** Shadow-colored overlay that follows the cosine falloff of a cylinder lit from the front. */
private fun metalShade(vertical: Boolean, shadow: Color): Brush = stopsBrush(
    vertical,
    *Array(11) { i ->
        val t = i / 10f
        val angle = (2f * t - 1f) * MaxWheelAngle
        t to shadow.copy(alpha = 0.9f * (1f - cos(angle).pow(1.5f)))
    },
)

private fun metalHighlight(vertical: Boolean): Brush = stopsBrush(
    vertical,
    0.42f to Color.Transparent,
    0.5f to Color.White.copy(alpha = 0.28f),
    0.58f to Color.Transparent,
)

private val MetalHighlightHorizontal = metalHighlight(vertical = false)
private val MetalHighlightVertical = metalHighlight(vertical = true)

/**
 * Angle of the item on a virtual cylinder, 0 at the center of the wheel. Returns the item's flat
 * distance from the center in pixels, its angle, and half of the wheel's length in pixels.
 */
private fun GraphicsLayerScope.wheelAngle(
    listState: LazyListState,
    index: Int,
    vertical: Boolean,
): Triple<Float, Float, Float>? {
    val info = listState.layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return null
    val half = (if (vertical) info.viewportSize.height else info.viewportSize.width) / 2f
    if (half <= 0f) return null
    val dx = item.offset + item.size / 2f + info.beforeContentPadding - half
    val t = (dx / half).coerceIn(-1f, 1f)
    return Triple(dx, t * MaxWheelAngle, half)
}

/**
 * Moves the item from its flat position to where it sits on the cylinder and fades it.
 * Computed in the draw phase (graphicsLayer) so scrolling never triggers recomposition.
 */
private fun Modifier.wheelPosition(listState: LazyListState, index: Int, vertical: Boolean): Modifier =
    graphicsLayer {
        val (dx, angle, half) = wheelAngle(listState, index, vertical) ?: return@graphicsLayer
        val radius = half / sin(MaxWheelAngle)
        val shift = radius * sin(angle) - dx
        if (vertical) translationY = shift else translationX = shift
        alpha = (cos(angle).pow(2.2f)).coerceAtLeast(0.12f)
        compositingStrategy = CompositingStrategy.ModulateAlpha
    }

/**
 * Shrinks the content around its own center, so the lines stay on the selection line's axis
 * and the labels stay on the same line.
 */
private fun Modifier.wheelScale(listState: LazyListState, index: Int, vertical: Boolean): Modifier =
    graphicsLayer {
        val (_, angle, _) = wheelAngle(listState, index, vertical) ?: return@graphicsLayer
        val c = cos(angle)
        // Along the scrolling direction the drum turns away; across it, lines in the middle bulge
        // out slightly and get shorter towards the edges.
        val along = c
        val across = 1.12f * c.pow(0.8f)
        if (vertical) {
            scaleY = along
            scaleX = across
        } else {
            scaleX = along
            scaleY = across
        }
    }
