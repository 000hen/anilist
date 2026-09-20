package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.anilist.model.AnimeSeasonYear
import uniffi.anilist.AnimeSeason

/**
 * What a season is called in the app.
 *
 * Kept here rather than in `:source` because it is copy: the source module describes when a title
 * airs, not how a reader is told about it.
 */
internal val AnimeSeason.label: String
    get() = when (this) {
        AnimeSeason.WINTER -> "冬季"
        AnimeSeason.SPRING -> "春季"
        AnimeSeason.SUMMER -> "夏季"
        AnimeSeason.FALL -> "秋季"
    }

/**
 * The season the schedule is showing, sitting under the app name and opening the picker on a press.
 *
 * Reports the press and holds nothing: which season this is, and whether the picker is up, both
 * belong to the caller, since the list below the bar answers to the same season this names.
 *
 * The description says what pressing does rather than reading out the label, which the screen
 * reader has already spoken by that point.
 */
@Composable
fun SeasonSelectorButton(
    seasonYear: AnimeSeasonYear,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick, onClickLabel = "選擇年份與季度")
            .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${seasonYear.year.value} ${seasonYear.season.label}",
            style = MaterialTheme.typography.labelLarge
        )

        Icon(
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}
