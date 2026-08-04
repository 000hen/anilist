package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Warns that a title carries adult content. */
@Composable
fun AdultTag(modifier: Modifier = Modifier) {
    LabelChip(
        text = "成人內容",
        icon = Icons.Rounded.Warning,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier,
        textStyle = MaterialTheme.typography.labelMedium
    )
}

/** Marks a title the user has starred. */
@Composable
fun FavoriteTag(modifier: Modifier = Modifier) {
    LabelChip(
        text = "已收藏",
        icon = Icons.Rounded.Star,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier,
        textStyle = MaterialTheme.typography.labelMedium
    )
}

/** A single genre / recommendation keyword. */
@Composable
fun RecommendationTag(name: String, modifier: Modifier = Modifier) {
    LabelChip(
        text = name,
        icon = Icons.Rounded.Tag,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    )
}

/** Wrapping row of [RecommendationTag]s. */
@Composable
fun RecommendationTagRow(tags: Collection<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (tag in tags) {
            RecommendationTag(tag)
        }
    }
}
