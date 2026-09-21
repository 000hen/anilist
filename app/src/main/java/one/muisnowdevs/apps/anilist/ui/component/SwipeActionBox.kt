package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val ArmFraction = 0.25f

private val ReturnSpec =
    spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

private enum class RevealedEdge { None, Left, Right }

enum class SwipeActionEdge { Left, Right }

/**
 * Wraps [content] in a horizontal swipe that performs one action from either direction.
 *
 * This owns only the gesture: threshold feedback, commit state, accessibility action, and returning
 * the row to rest. What the action means and what is drawn behind the row stay with the caller via
 * [onAction] and [background].
 *
 * [background] is composed only while an edge is visible. That lets action-specific backgrounds
 * remember values for exactly one reveal without the gesture component knowing anything about the
 * caller's state.
 */
@Composable
fun SwipeActionBox(
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    background: @Composable (edge: SwipeActionEdge, isConfirmed: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val currentOnAction by rememberUpdatedState(onAction)
    val haptics = LocalHapticFeedback.current

    // This is deliberately transient. Restoring a saved offset could bring a recycled row back
    // already open even though every completed or cancelled gesture is meant to return home.
    val offsetX = remember { Animatable(0f) }
    var rowWidth by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var isCommitted by remember { mutableStateOf(false) }

    // Keep the target separate from Animatable.value because drag deltas can arrive faster than the
    // coroutines applying them. Once the drag stops, queued deltas are intentionally ignored so one
    // cannot cancel the return animation and leave the row stranded away from zero.
    val dragTarget = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val dragState = rememberDraggableState { delta ->
        dragTarget.floatValue += delta
        scope.launch {
            if (isDragging) offsetX.snapTo(dragTarget.floatValue)
        }
    }

    val revealedEdge by remember {
        derivedStateOf {
            when {
                offsetX.value > 0f -> RevealedEdge.Left
                offsetX.value < 0f -> RevealedEdge.Right
                else -> RevealedEdge.None
            }
        }
    }
    val isArmed by remember {
        derivedStateOf {
            rowWidth > 0 && abs(offsetX.value) >= rowWidth * ArmFraction
        }
    }

    // Only the outward crossing reports threshold feedback. The spring home crosses the same
    // boundary in reverse and must stay silent.
    androidx.compose.runtime.LaunchedEffect(isArmed) {
        if (!isDragging) return@LaunchedEffect
        haptics.performHapticFeedback(
            if (isArmed) HapticFeedbackType.GestureThresholdActivate
            else HapticFeedbackType.SegmentTick
        )
    }

    Box(
        modifier = modifier
            .onSizeChanged { rowWidth = it.width }
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    isDragging = true
                    // Catching the row during its return continues from the visible position.
                    dragTarget.floatValue = offsetX.value
                },
                onDragStopped = { velocity ->
                    isDragging = false

                    // Commit from the rendered offset rather than the accumulated target. A queued
                    // delta that never reached the screen must not trigger an action the user never
                    // saw become armed.
                    if (isArmed) {
                        isCommitted = true
                        currentOnAction()
                    }

                    // A new drag can cancel this animation. The commit latch still has to clear or
                    // the next reveal would begin in the confirmed state.
                    try {
                        offsetX.animateTo(0f, ReturnSpec, initialVelocity = velocity)
                    } finally {
                        isCommitted = false
                    }
                }
            )
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(actionLabel) {
                        currentOnAction()
                        true
                    }
                )
            }
    ) {
        when (revealedEdge) {
            RevealedEdge.Left ->
                background(SwipeActionEdge.Left, isArmed || isCommitted)

            RevealedEdge.Right ->
                background(SwipeActionEdge.Right, isArmed || isCommitted)

            RevealedEdge.None -> Unit
        }

        Box(
            modifier = Modifier.offset {
                IntOffset(offsetX.value.roundToInt(), 0)
            }
        ) {
            content()
        }
    }
}
