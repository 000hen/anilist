package one.muisnowdevs.apps.anilist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import one.muisnowdevs.apps.anilist.ui.AppLayout
import one.muisnowdevs.apps.anilist.ui.component.FavoriteFilterButton
import one.muisnowdevs.apps.anilist.ui.screen.AnimeScheduleScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Owned here because neither side can own it: the button that flips it sits in the app
            // bar and the list it filters sits under the bar, so this is the nearest thing holding
            // both. Saveable so a rotation does not quietly hand back the whole schedule.
            var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }

            AppLayout(
                navigationIcon = {
                    FavoriteFilterButton(
                        isEnabled = showFavoritesOnly,
                        onEnabledChange = { showFavoritesOnly = it }
                    )
                }
            ) { innerPadding ->
                AnimeScheduleScreen(
                    modifier = Modifier.padding(innerPadding),
                    showFavoritesOnly = showFavoritesOnly
                )
            }
        }
    }
}
