package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp

private const val EXTERNAL_ICON_ID = "external"

/** A tappable link label followed by an inline "leaves the app" icon. */
@Composable
fun ExternalLinkText(title: String, url: String, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            withLink(LinkAnnotation.Url(url)) {
                append(title)
                append(" ")
                appendInlineContent(EXTERNAL_ICON_ID, "@external")
            }
        },
        inlineContent = mapOf(
            EXTERNAL_ICON_ID to InlineTextContent(
                Placeholder(
                    20.sp,
                    LocalTextStyle.current.fontSize,
                    PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                )
            }
        ),
        modifier = modifier
    )
}
