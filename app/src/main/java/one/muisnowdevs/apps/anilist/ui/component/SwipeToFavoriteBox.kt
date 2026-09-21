package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Favourite-specific presentation for [SwipeActionBox].
 *
 * The shared component owns the swipe state machine. This wrapper only translates that action into
 * a favourite toggle and supplies the star/tick background, so other swipe actions can reuse the
 * gesture without inheriting favourite state or styling.
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
    val haptics = LocalHapticFeedback.current

    val changeFavorite = {
        val favorite = !currentIsFavorite
        currentOnFavoriteChange(favorite)
        haptics.performHapticFeedback(
            if (favorite) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
        )
    }

    SwipeActionBox(
        actionLabel = if (isFavorite) "取消收藏" else "加入收藏",
        onAction = changeFavorite,
        modifier = modifier,
        background = { edge, isConfirmed ->
            FavoriteSwipeBackground(
                edge = edge,
                isFavorite = isFavorite,
                isConfirmed = isConfirmed,
                modifier = Modifier.fillMaxSize()
            )
        },
        content = content
    )
}

/**
 * The favourite icon uncovered behind a row. [SwipeActionBox] removes this composition when the row
 * reaches rest, so remembering [isFavorite] here naturally latches the value for one reveal. A
 * favourite change therefore cannot recolour the background while the committed row is returning.
 */
@Composable
private fun FavoriteSwipeBackground(
    edge: SwipeActionEdge,
    isFavorite: Boolean,
    isConfirmed: Boolean,
    modifier: Modifier = Modifier
) {
    val favoriteAtSwipeStart = remember { isFavorite }

    Box(
        modifier = modifier.background(
            color = if (favoriteAtSwipeStart) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium
        ),
        contentAlignment = if (edge == SwipeActionEdge.Left) {
            AbsoluteAlignment.CenterLeft
        } else {
            AbsoluteAlignment.CenterRight
        }
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
                    favoriteAtSwipeStart -> Icons.Rounded.StarBorder
                    else -> Icons.Rounded.Star
                },
                contentDescription = null,
                tint = if (favoriteAtSwipeStart) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
