package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/** Remote artwork clipped to the theme's small shape. Sizing is left to the caller. */
@Composable
fun CoverImage(
    url: String?,
    modifier: Modifier = Modifier,
    description: String? = null,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    if (url != null) AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.clip(MaterialTheme.shapes.small),
    )
    else CoverImagePlaceholder(modifier = modifier, description = description)
}

@Composable
private fun CoverImagePlaceholder(
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth(0.2f),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.QuestionMark, contentDescription = description)
    }
}