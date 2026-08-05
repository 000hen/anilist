package one.muisnowdevs.apps.anilist.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import one.muisnowdevs.apps.anilist.AnimeFavorite
import one.muisnowdevs.apps.anilist.getTodayOrder
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.AnilistSeasonYear
import one.muisnowdevs.apps.anilist.ui.component.AnimeCard
import one.muisnowdevs.apps.anilist.ui.component.AnimeDetailSheet
import one.muisnowdevs.apps.anilist.ui.component.DayHeader
import one.muisnowdevs.apps.anilist.ui.component.ErrorDetailSheet
import one.muisnowdevs.apps.anilist.ui.component.SwipeToFavoriteBox
import one.muisnowdevs.apps.anilist.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle

/**
 * The weekly airing schedule: pull to refresh the season on show, tap a title for its details,
 * swipe a row to star it.
 *
 * Favourites are collected here and handed down, so every row reads the same set instead of
 * tracking a copy of its own.
 *
 * [showFavoritesOnly] belongs to the caller, since the control that flips it lives in the app bar
 * above this screen rather than anywhere inside it. So does the season, for the same reason — which
 * is why refreshing asks the view model to reload what it already has rather than working out the
 * season again down here, where it would only ever arrive at today's.
 */
@Composable
fun AnimeScheduleScreen(
    modifier: Modifier = Modifier,
    showFavoritesOnly: Boolean = false,
    viewModel: MainViewModel = viewModel()
) {
    val schedule by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedSeason by viewModel.selected.collectAsState()

    val context = LocalContext.current
    val favorites = remember(context) { AnimeFavorite.getInstance(context) }
    val favoriteIds by favorites.favorites.collectAsState()

    // Worked out once per season rather than per row: every card asks the same question, and the
    // answer only changes when the bar above does.
    val isCurrentSeason = remember(selectedSeason) {
        selectedSeason == AnilistSeasonYear.current()
    }

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

    var detailedAnime by remember { mutableStateOf<AnilistAnime?>(null) }

    detailedAnime?.let { anime ->
        AnimeDetailSheet(anime) { detailedAnime = null }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = viewModel::reload,
        modifier = modifier
    ) {
        // Only when there is nothing to fall back on. A season that loaded once and then failed to
        // refresh keeps what it had, since a stale week reads better than an empty one.
        if (error != null && schedule.isEmpty()) {
            ScheduleLoadError(error, onRetry = viewModel::reload)
        } else {
            // Keyed on the season so the list starts again at the top when a different one is
            // picked, rather than opening at whatever offset the last season was left scrolled to.
            // Refreshing the season already showing keeps its key, and so keeps its place.
            key(selectedSeason) {
                AnimeScheduleList(
                    schedule = visibleSchedule,
                    favoriteIds = favoriteIds,
                    isCurrentSeason = isCurrentSeason,
                    onAnimeClick = { detailedAnime = it },
                    onFavoriteChange = favorites::setFavorite
                )
            }
        }
    }
}

/**
 * Stands in for the week when a season will not load.
 *
 * The reason is kept off this screen on purpose: what fails here is a walk down fixed indices in
 * someone else's page, and the exception names an offset in a script tag. That tells a reader
 * nothing they can act on, and the retry is the half of this worth offering them. It used to sit
 * below the retry as a wall of monospace, which read as the app having broken rather than as a
 * season having nothing to show — so it moves behind [ErrorDetailSheet], where it stays to hand for
 * whoever is fixing the scrape without answering someone who only wants the week back.
 */
@Composable
private fun ScheduleLoadError(
    error: Throwable?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Saved rather than remembered: the failure it describes is held by the view model and outlives
    // a rotation, so the sheet reading it should come back up with the screen instead of closing
    // under whoever was mid-way through copying it.
    var isDetailOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "無法載入這一季的時間表",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "這一季可能還沒有資料，或是連線出了問題。",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        // Side by side rather than stacked, so the pair reads as one thing to do next with the
        // filled half naming which of them that is.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onRetry) { Text("重試") }

            // Absent rather than disabled when there is nothing to open: a failure with no
            // exception behind it is not a state the reader can do anything about either.
            if (error != null) {
                TextButton(onClick = { isDetailOpen = true }) { Text("詳細資訊") }
            }
        }
    }

    if (isDetailOpen && error != null) {
        ErrorDetailSheet(error) { isDetailOpen = false }
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
 *
 * The order still opens on today even in a season that is not airing, where today means nothing to
 * the schedule — it means something to the reader, who gets the same shape of list either way.
 */
@Composable
private fun AnimeScheduleList(
    schedule: Map<DayOfWeek, List<AnilistAnime>>,
    favoriteIds: Set<String>,
    onAnimeClick: (AnilistAnime) -> Unit,
    onFavoriteChange: (id: String, favorite: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isCurrentSeason: Boolean = true
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
                    text = day.getDisplayName(TextStyle.FULL, locale),
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
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .animateItem()
                ) {
                    AnimeCard(
                        anime = anime,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onAnimeClick(anime) },
                        isFavorite = isFavorite,
                        isCurrentSeason = isCurrentSeason
                    )
                }
            }
        }
    }
}
