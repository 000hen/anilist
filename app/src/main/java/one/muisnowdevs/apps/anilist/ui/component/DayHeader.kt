package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Centred day label for the schedule list.
 *
 * While [isFloating] it fades to the primary container colours, so a header pinned over the list
 * stays readable against the rows it covers.
 */
@Composable
fun DayHeader(
    text: String,
    isFloating: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val transition = updateTransition(isFloating)

    val containerColor by transition.animateColor { floating ->
        if (!floating) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor by transition.animateColor { floating ->
        if (!floating) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(containerColor)
                .clickable(onClick = onClick)
        ) {
            Text(
                text = text,
                color = contentColor,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
