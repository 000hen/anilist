package one.muisnowdevs.apps.anilist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import one.muisnowdevs.apps.anilist.ui.AppLayout
import one.muisnowdevs.apps.anilist.ui.component.FavoriteFilterButton
import one.muisnowdevs.apps.anilist.ui.component.SeasonPickerSheet
import one.muisnowdevs.apps.anilist.ui.component.SeasonSelectorButton
import one.muisnowdevs.apps.anilist.ui.screen.AnimeScheduleScreen
import one.muisnowdevs.apps.anilist.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Owned here because neither side can own it: the button that flips it sits in the app
            // bar and the list it filters sits under the bar, so this is the nearest thing holding
            // both. Saveable so a rotation does not quietly hand back the whole schedule.
            var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }

            // Same shape, same reason: the chip that opens the picker is in the bar, the schedule
            // it changes is below it. The season itself is the view model's, since it is what the
            // load is addressed by — only whether the sheet is up is decided here.
            var isPickerOpen by rememberSaveable { mutableStateOf(false) }

            // Held at this level rather than left to the screen's default so the bar and the list
            // read the same season out of the same place.
            val viewModel: MainViewModel = viewModel()
            val selectedSeason by viewModel.selected.collectAsState()

            AppLayout(
                navigationIcon = {
                    FavoriteFilterButton(
                        isEnabled = showFavoritesOnly,
                        onEnabledChange = { showFavoritesOnly = it }
                    )
                },
                subtitle = {
                    SeasonSelectorButton(
                        seasonYear = selectedSeason,
                        onClick = { isPickerOpen = true }
                    )
                }
            ) { innerPadding ->
                AnimeScheduleScreen(
                    modifier = Modifier.padding(innerPadding),
                    showFavoritesOnly = showFavoritesOnly,
                    viewModel = viewModel
                )

                // Inside the layout rather than beside it, so the sheet is drawn under the app's
                // theme; AppLayout is what applies it.
                if (isPickerOpen) {
                    SeasonPickerSheet(
                        selected = selectedSeason,
                        onSelect = {
                            viewModel.select(it)
                            isPickerOpen = false
                        },
                        onDismissRequest = { isPickerOpen = false }
                    )
                }
            }
        }
    }
}
