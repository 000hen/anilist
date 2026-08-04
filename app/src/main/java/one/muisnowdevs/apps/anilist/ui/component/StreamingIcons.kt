package one.muisnowdevs.apps.anilist.ui.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import one.muisnowdevs.apps.anilist.source.Streaming

private const val VENDOR_ICON_BASE_URL = "https://d28s5ztqvkii64.cloudfront.net/images"

/** Vendor badge: names the vendor on long press, opens its watch page on tap. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingIcon(streaming: Streaming, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = { PlainTooltip { Text(streaming.vendor) } },
        state = rememberTooltipState(),
        modifier = modifier
    ) {
        SubcomposeAsyncImage(
            model = "$VENDOR_ICON_BASE_URL/${streaming.vendor}_icon.webp",
            contentDescription = streaming.title,
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, streaming.url.toUri()))
                },
            error = {
                Icon(
                    Icons.Rounded.PlayCircleFilled,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.14f)
                )
            }
        )
    }
}

/** Centred, wrapping row of [StreamingIcon]s. */
@Composable
fun StreamingIconRow(streamings: List<Streaming>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        for (streaming in streamings) {
            StreamingIcon(streaming)
        }
    }
}
