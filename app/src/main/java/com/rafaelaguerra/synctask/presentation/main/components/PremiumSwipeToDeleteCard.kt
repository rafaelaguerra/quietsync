package com.rafaelaguerra.synctask.presentation.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rafaelaguerra.synctask.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun PremiumSwipeToDeleteCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    actionWidth: Dp = 92.dp,
    fullSwipeThresholdFraction: Float = 0.72f,
    showHint: Boolean = false,
    onHintShown: () -> Unit = {},
    onDelete: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val actionWidthPx = remember(actionWidth, density) { with(density) { actionWidth.toPx() } }

    var containerWidthPx by remember { mutableFloatStateOf(1f) }
    var targetOffsetPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var deleteTriggered by remember { mutableStateOf(false) }

    val revealLimitPx = actionWidthPx
    val fullSwipePx = remember(containerWidthPx, fullSwipeThresholdFraction, revealLimitPx) {
        val threshold = containerWidthPx * fullSwipeThresholdFraction
        threshold.coerceAtLeast(revealLimitPx + with(density) { 56.dp.toPx() })
    }

    val settledOffsetPx by animateFloatAsState(
        targetValue = targetOffsetPx,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = 0.82f
        ),
        label = "swipeOffset"
    )
    val offsetPx = if (isDragging) targetOffsetPx else settledOffsetPx

    val revealProgress = (abs(offsetPx) / revealLimitPx).coerceIn(0f, 1f)
    val fullProgress = (abs(offsetPx) / fullSwipePx).coerceIn(0f, 1f)
    val deleteArmed = abs(offsetPx) >= fullSwipePx * 0.96f

    var revealHapticPlayed by remember { mutableStateOf(false) }
    var deleteHapticPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(revealProgress) {
        if (revealProgress > 0.4f && !revealHapticPlayed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            revealHapticPlayed = true
        } else if (revealProgress < 0.2f) {
            revealHapticPlayed = false
        }
    }

    LaunchedEffect(deleteArmed) {
        if (deleteArmed && !deleteHapticPlayed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            deleteHapticPlayed = true
        } else if (!deleteArmed) {
            deleteHapticPlayed = false
        }
    }

    val deleteCallback by rememberUpdatedState(onDelete)
    fun triggerDelete() {
        if (deleteTriggered || !enabled) return
        deleteTriggered = true
        deleteCallback()
    }

    LaunchedEffect(showHint, revealLimitPx) {
        if (!showHint || revealLimitPx <= 0f || !enabled) return@LaunchedEffect
        // Let the screen settle before nudging the card so the gesture is noticed.
        kotlinx.coroutines.delay(450)
        onHintShown()
        targetOffsetPx = -revealLimitPx * 0.85f
        kotlinx.coroutines.delay(680)
        targetOffsetPx = 0f
    }

    val destructiveColor by animateColorAsState(
        targetValue = if (deleteArmed) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        label = "destructiveColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (deleteArmed) {
            Color.White
        } else {
            MaterialTheme.colorScheme.error
        },
        label = "iconTint"
    )
    val iconScale by animateFloatAsState(
        targetValue = (0.78f + revealProgress * 0.3f + if (deleteArmed) 0.08f else 0f).coerceAtMost(1.15f),
        label = "iconScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = -16f + (16f * revealProgress.coerceIn(0f, 1f)),
        label = "iconRotation"
    )
    val cardScale by animateFloatAsState(
        targetValue = 1f - (fullProgress * 0.03f),
        label = "cardScale"
    )
    val cardTilt by animateFloatAsState(
        targetValue = -2.1f * revealProgress,
        label = "cardTilt"
    )

    val draggableState = rememberDraggableState { delta ->
        if (!enabled || deleteTriggered) return@rememberDraggableState
        isDragging = true

        val candidate = targetOffsetPx + delta
        targetOffsetPx = dampedSwipeOffset(
            rawOffset = candidate,
            fullSwipeLimit = fullSwipePx
        )
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
            .clip(RoundedCornerShape(26.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            destructiveColor.copy(alpha = 0.12f),
                            destructiveColor.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionWidth)
                .fillMaxHeight()
                .clickable(
                    enabled = enabled && revealProgress > 0.4f && !deleteTriggered,
                    onClick = { triggerDelete() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.content_desc_delete_event),
                tint = iconTint,
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                    rotationZ = iconRotation
                    alpha = (0.25f + revealProgress * 0.75f).coerceIn(0f, 1f)
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetPx
                    scaleX = cardScale
                    scaleY = cardScale
                    rotationZ = cardTilt
                }
                .draggable(
                    orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                    enabled = enabled && !deleteTriggered,
                    state = draggableState,
                    onDragStopped = { velocity ->
                        isDragging = false
                        if (!enabled || deleteTriggered) return@draggable

                        val shouldDelete = targetOffsetPx <= -fullSwipePx ||
                            (velocity < -2500f && targetOffsetPx <= -revealLimitPx * 0.75f)

                        val shouldReveal = !shouldDelete && (
                            targetOffsetPx <= -revealLimitPx * 0.45f ||
                                (velocity < -1200f && targetOffsetPx <= -revealLimitPx * 0.25f)
                            )

                        when {
                            shouldDelete -> {
                                targetOffsetPx = -containerWidthPx
                                triggerDelete()
                            }
                            shouldReveal -> {
                                targetOffsetPx = -revealLimitPx
                            }
                            else -> {
                                targetOffsetPx = 0f
                            }
                        }
                    }
                ),
            content = content
        )
    }
}

private fun dampedSwipeOffset(
    rawOffset: Float,
    fullSwipeLimit: Float
): Float {
    if (rawOffset >= 0f) {
        // Resist right-swipe so cards naturally spring back.
        return rawOffset * 0.15f
    }
    if (rawOffset >= -fullSwipeLimit) return rawOffset

    // After crossing the destructive threshold, apply resistance.
    val extra = rawOffset + fullSwipeLimit
    return -fullSwipeLimit + (extra * 0.22f)
}
