package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.anilist.model.AnimeSeasonYear
import uniffi.anilist.AnimeSeason
import java.time.Year

/**
 * Picks the season the schedule is showing: a year to step through, and the four seasons within it.
 *
 * Choosing a season reports it and closes, with no confirm step — every other control in the app
 * commits on a single press, and the one being replaced is a press away from coming back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonPickerSheet(
    selected: AnimeSeasonYear,
    onSelect: (AnimeSeasonYear) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    // The year being browsed, which is not yet the year being shown — stepping to 2019 loads
    // nothing until a season under it is pressed. Keyed on [selected] so reopening the sheet starts
    // from what is on screen rather than wherever the last browse wandered off to.
    var browsingYear by remember(selected) { mutableStateOf(selected.year) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            YearStepper(year = browsingYear, onYearChange = { browsingYear = it })

            SeasonGrid(
                year = browsingYear,
                selected = selected,
                onSelect = onSelect
            )
        }
    }
}

/**
 * Walks a year at a time between [AnimeSeasonYear.EARLIEST] and [AnimeSeasonYear.latest].
 *
 * The arrows disable at the ends rather than disappearing, so the range reads as a limit of what is
 * out there rather than as the control breaking.
 */
@Composable
private fun YearStepper(
    year: Year,
    onYearChange: (Year) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onYearChange(year.minusYears(1)) },
            enabled = year > AnimeSeasonYear.EARLIEST
        ) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "前一年")
        }

        Text(
            text = year.value.toString(),
            style = MaterialTheme.typography.headlineSmall
        )

        IconButton(
            onClick = { onYearChange(year.plusYears(1)) },
            enabled = year < AnimeSeasonYear.latest()
        ) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "後一年")
        }
    }
}

/**
 * The four seasons of [year], two to a row, with the one already on screen filled in.
 *
 * The filled season is compared against the whole selection rather than against its season alone,
 * so browsing to another year leaves nothing marked — which is what says the year on show is not
 * the year being read.
 */
@Composable
private fun SeasonGrid(
    year: Year,
    selected: AnimeSeasonYear,
    onSelect: (AnimeSeasonYear) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (pair in AnimeSeason.entries.chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (season in pair) {
                    val seasonYear = AnimeSeasonYear(year, season)

                    if (seasonYear == selected) {
                        FilledTonalButton(
                            onClick = { onSelect(seasonYear) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(season.label)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelect(seasonYear) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(season.label)
                        }
                    }
                }
            }
        }
    }
}
