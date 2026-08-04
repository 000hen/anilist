package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import one.muisnowdevs.apps.anilist.ui.theme.FavoriteStar

/**
 * Switches the schedule between every title and only the starred ones.
 *
 * The icon carries the whole of the state: an outlined star while the list is showing everything,
 * a filled yellow one while it is holding titles back. [isEnabled] is the caller's to own — this
 * only reports the flip — so one flag can drive both the button and the list it filters, and the
 * two can never disagree about which way round they are.
 *
 * The description names what a press would do rather than what the icon is, since that is what a
 * screen reader is being asked for.
 */
@Composable
fun FavoriteFilterButton(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = { onEnabledChange(!isEnabled) }, modifier = modifier) {
        AnimatedContent(
            targetState = isEnabled,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 0.6f))
            },
            label = "favorite filter icon"
        ) { enabled ->
            Icon(
                imageVector = if (enabled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = if (enabled) "顯示全部" else "只顯示收藏",
                tint = if (enabled) FavoriteStar else LocalContentColor.current
            )
        }
    }
}
