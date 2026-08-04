package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * How far the row has to travel before letting go of it toggles anything, as a fraction of its own
 * width. A fraction rather than a dp so the gesture asks for the same share of the row on a phone
 * and on a tablet; a quarter is clear of the horizontal drift that comes with scrolling a list,
 * and still short enough that the thumb never has to carry the row across the screen.
 */
private const val ArmFraction = 0.25f

/** Sends the row home from wherever the finger left it, carrying the release velocity into it. */
private val ReturnSpec =
    spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

/** Which side of the row a swipe has uncovered, and so the side the icon belongs on. */
private enum class RevealedEdge { None, Left, Right }

/**
 * Wraps [content] in a swipe that toggles whether it is starred. Either direction does the same
 * thing: [FavoriteSwipeBackground] uncovers the star the swipe is about to change, and turns it
 * into a tick — with a haptic to match — once the row has come far enough that letting go will
 * record the change. Releasing there reports through [onFavoriteChange] and springs the row home
 * from wherever it was; the row is never carried off and never dismissed, so [isFavorite] has to
 * be state the caller owns rather than something this box remembers.
 */
@Composable
fun SwipeToFavoriteBox(
    isFavorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentIsFavorite by rememberUpdatedState(isFavorite)
    val currentOnFavoriteChange by rememberUpdatedState(onFavoriteChange)

    // Every route to a change goes through here, so the buzz can never drift away from the change
    // it is reporting. ToggleOn/ToggleOff fall back to older constants below API 34 on their own,
    // and route through the view, so they need no permission and honour the system haptic setting.
    val haptics = LocalHapticFeedback.current
    val changeFavorite = { favorite: Boolean ->
        currentOnFavoriteChange(favorite)
        haptics.performHapticFeedback(
            if (favorite) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
        )
    }

    // Deliberately remember rather than rememberSaveable, which would save the position: a row
    // scrolled out of the list mid-swipe would come back still held open. The row always comes home
    // anyway, so there is no position here worth carrying across a trip out of the list.
    val offsetX = remember { Animatable(0f) }
    var rowWidth by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var isCommitted by remember { mutableStateOf(false) }

    // Deltas arrive faster than the coroutines that apply them, so accumulate the target here
    // first. Launching with offsetX.value + delta instead would read the offset when the coroutine
    // runs rather than when the delta arrived, and two deltas queued within a frame would both read
    // the same offset and collapse into one.
    //
    // The guard is what sends the row home at all. onDragStopped runs undispatched, so the last
    // delta's coroutine is still queued when the release starts the animation back to zero; left
    // alone it would run a moment later, take the Animatable's mutex off that animation and snap
    // the row back to where the finger let go, parking it there for good. Once the drag is over
    // the offset is the animation's to own, so the leftover delta is dropped instead.
    val dragTarget = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val dragState = rememberDraggableState { delta ->
        dragTarget.floatValue += delta
        scope.launch { if (isDragging) offsetX.snapTo(dragTarget.floatValue) }
    }

    // Both of these are read off the raw offset, so take them through derivedStateOf: they change
    // on the few frames the swipe crosses a boundary rather than on every frame of the drag.
    // Leaving the offset itself to Modifier.offset's lambda keeps content() out of the drag too.
    val revealedEdge by remember {
        derivedStateOf {
            val offset = offsetX.value
            when {
                offset > 0f -> RevealedEdge.Left
                offset < 0f -> RevealedEdge.Right
                else -> RevealedEdge.None
            }
        }
    }
    val isArmed by remember {
        derivedStateOf { rowWidth > 0 && abs(offsetX.value) >= rowWidth * ArmFraction }
    }

    // The flag flips while the row is still out, so colouring the background from the live one
    // would swap it under the returning row. Latch it instead and stop tracking for as long as the
    // row is away from home: the star and its colours then stand for the state the swipe set out to
    // change for the whole gesture, and the tick — not a recoloured row — is what reports the flip.
    // Tracking while home also picks up changes made outside a swipe, such as the action below.
    var favoriteAtRest by remember { mutableStateOf(isFavorite) }
    LaunchedEffect(revealedEdge, isFavorite) {
        if (revealedEdge == RevealedEdge.None) favoriteAtRest = isFavorite
    }

    // Crossing the threshold is what makes a partial swipe worth having: it is the only thing that
    // tells the thumb letting go will now do something, and that it need not carry the row further.
    // Gated on the drag so the trip home, which decays back past the same threshold, cannot buzz.
    LaunchedEffect(isArmed) {
        if (!isDragging) return@LaunchedEffect
        haptics.performHapticFeedback(
            if (isArmed) HapticFeedbackType.GestureThresholdActivate
            else HapticFeedbackType.SegmentTick
        )
    }

    val actionLabel = if (isFavorite) "取消收藏" else "加入收藏"

    Box(
        modifier = modifier
            .onSizeChanged { rowWidth = it.width }
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    isDragging = true
                    // Catching a row still on its way home has to carry on from where it is.
                    dragTarget.floatValue = offsetX.value
                },
                onDragStopped = { velocity ->
                    isDragging = false
                    // isArmed is read off the offset rather than the accumulated target, so this
                    // goes by what the row actually reached on screen — the dropped last delta was
                    // never drawn either. Tying the change to what the tick showed is the point of
                    // arming at all: better to miss a crossing the thumb never saw than to report
                    // one it never saw coming.
                    if (isArmed) {
                        // Latched for the trip home: isArmed decays back through the threshold
                        // partway through the animation, and the tick reporting the change must
                        // not turn back into a star on the way.
                        isCommitted = true
                        changeFavorite(!currentIsFavorite)
                    }
                    // A cancelled drag arrives here too, at zero velocity, so the row comes home
                    // even when the list steals the gesture. finally rather than a plain trailing
                    // statement because catching the row mid-return cancels this animation, and a
                    // stale isCommitted would open the next swipe already showing a tick.
                    try {
                        offsetX.animateTo(0f, ReturnSpec, initialVelocity = velocity)
                    } finally {
                        isCommitted = false
                    }
                }
            )
            // A swipe is unreachable without one, so offer the same toggle as an explicit action.
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(actionLabel) {
                        changeFavorite(!isFavorite)
                        true
                    }
                )
            }
    ) {
        FavoriteSwipeBackground(
            edge = revealedEdge,
            isFavorite = favoriteAtRest,
            isConfirmed = isArmed || isCommitted,
            modifier = Modifier.matchParentSize()
        )
        Box(modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }) {
            content()
        }
    }
}

/**
 * The icon uncovered behind a row mid-swipe, pinned to whichever [edge] the swipe has uncovered so
 * it stays under the thumb. The edge is the physical one the row has moved off, which is what the
 * eye is following, so it needs no reading direction to place it.
 *
 * [isFavorite] is the state the row is swiping *out of* — a filled star to add it, an outlined one
 * on the error colours to drop it — and callers are expected to hold it still for the length of a
 * gesture. Once [isConfirmed] the star gives way to a tick, which is how the swipe reports that it
 * has come far enough to count; the colours stay put so that nothing but the icon moves.
 */
@Composable
private fun FavoriteSwipeBackground(
    edge: RevealedEdge,
    isFavorite: Boolean,
    isConfirmed: Boolean,
    modifier: Modifier = Modifier
) {
    // Nothing is uncovered while the row sits at home, so there is nothing to draw.
    if (edge == RevealedEdge.None) return

    Box(
        modifier = modifier
            .background(
                color = if (isFavorite) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = if (edge == RevealedEdge.Left) AbsoluteAlignment.CenterLeft
        else AbsoluteAlignment.CenterRight
    ) {
        AnimatedContent(
            targetState = isConfirmed,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 0.6f))
            },
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "favorite swipe icon"
        ) { confirmed ->
            Icon(
                imageVector = when {
                    confirmed -> Icons.Rounded.Check
                    isFavorite -> Icons.Rounded.StarBorder
                    else -> Icons.Rounded.Star
                },
                contentDescription = null,
                tint =
                    if (isFavorite) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
