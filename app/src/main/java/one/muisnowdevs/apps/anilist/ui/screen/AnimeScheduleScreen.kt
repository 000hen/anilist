package one.muisnowdevs.apps.anilist.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import one.muisnowdevs.apps.anilist.getCurrentSession
import one.muisnowdevs.apps.anilist.getTodayOrder
import one.muisnowdevs.apps.anilist.source.AnimeFavorite
import one.muisnowdevs.apps.anilist.source.AnimeInformation
import one.muisnowdevs.apps.anilist.source.Week
import one.muisnowdevs.apps.anilist.ui.component.AnimeCard
import one.muisnowdevs.apps.anilist.ui.component.AnimeDetailSheet
import one.muisnowdevs.apps.anilist.ui.component.DayHeader
import one.muisnowdevs.apps.anilist.ui.component.SwipeToFavoriteBox
import one.muisnowdevs.apps.anilist.viewmodel.MainViewModel
import java.time.format.TextStyle

/**
 * The weekly airing schedule: pull to refresh the current season, tap a title for its details,
 * swipe a row to star it.
 *
 * Favourites are collected here and handed down, so every row reads the same set instead of
 * tracking a copy of its own.
 *
 * [showFavoritesOnly] belongs to the caller, since the control that flips it lives in the app bar
 * above this screen rather than anywhere inside it.
 */
@Composable
fun AnimeScheduleScreen(
    modifier: Modifier = Modifier,
    showFavoritesOnly: Boolean = false,
    viewModel: MainViewModel = viewModel()
) {
    val schedule by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val favorites = remember(context) { AnimeFavorite.getInstance(context) }
    val favoriteIds by favorites.favorites.collectAsState()

    // A day left with nothing to show is dropped outright rather than kept as a bare header, so
    // switching the filter on visibly shortens the week rather than just thinning it.
    //
    // Keyed on the ids as well as the flag, which is what makes unstarring a row take it out of a
    // filtered list on the spot — the list doing exactly what the star in the bar says it does.
    val visibleSchedule = remember(schedule, favoriteIds, showFavoritesOnly) {
        if (!showFavoritesOnly) schedule
        else schedule
            .mapValues { (_, animes) -> animes.filter { it.id in favoriteIds } }
            .filterValues { it.isNotEmpty() }
    }

    var detailedAnime by remember { mutableStateOf<AnimeInformation?>(null) }

    detailedAnime?.let { anime ->
        AnimeDetailSheet(anime) { detailedAnime = null }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = {
            val (year, season) = getCurrentSession()
            viewModel.reload(year, season)
        },
        modifier = modifier
    ) {
        AnimeScheduleList(
            schedule = visibleSchedule,
            favoriteIds = favoriteIds,
            onAnimeClick = { detailedAnime = it },
            onFavoriteChange = favorites::setFavorite
        )
    }
}

/**
 * Lists every day of the week starting from today, each behind a sticky [DayHeader] that scrolls
 * back to its own section when tapped.
 *
 * Rows are keyed by anime id so that a refresh reshuffling the schedule cannot leave a row's state
 * pointing at the title that used to occupy its slot.
 *
 * A day absent from [schedule] is skipped entirely, header and all. The source groups by day, so a
 * week with nothing airing on a Tuesday simply has no Tuesday in it, and filtering empties days the
 * same way; either way a header with nothing beneath it is just noise between two real days.
 */
@Composable
private fun AnimeScheduleList(
    schedule: Map<Week, List<AnimeInformation>>,
    favoriteIds: Set<String>,
    onAnimeClick: (AnimeInformation) -> Unit,
    onFavoriteChange: (id: String, favorite: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val days = remember { getTodayOrder() }
    val locale = LocalLocale.current.platformLocale

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = state
    ) {
        for (day in days) {
            val animes = schedule[day].orEmpty()
            if (animes.isEmpty()) continue

            stickyHeader { headerIndex ->
                val isFloating by remember(headerIndex) {
                    derivedStateOf {
                        state.firstVisibleItemIndex > headerIndex ||
                                (state.firstVisibleItemIndex == headerIndex && state.firstVisibleItemScrollOffset > 0)
                    }
                }

                DayHeader(
                    text = day.week.getDisplayName(TextStyle.FULL, locale),
                    isFloating = isFloating,
                    onClick = { scope.launch { state.animateScrollToItem(headerIndex) } },
                    modifier = Modifier.animateItem()
                )
            }

            items(animes, key = { it.id }) { anime ->
                val isFavorite = anime.id in favoriteIds

                SwipeToFavoriteBox(
                    isFavorite = isFavorite,
                    onFavoriteChange = { onFavoriteChange(anime.id, it) },
                    modifier = Modifier.padding(horizontal = 8.dp).animateItem()
                ) {
                    AnimeCard(
                        anime = anime,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onAnimeClick(anime) },
                        isFavorite = isFavorite
                    )
                }
            }
        }
    }
}
