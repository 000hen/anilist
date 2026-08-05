package one.muisnowdevs.apps.anilist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.AnilistSeasonYear
import one.muisnowdevs.apps.anilist.source.ScheduleDay
import one.muisnowdevs.apps.anilist.source.youranimes.YourAnimesSource
import kotlin.coroutines.coroutineContext

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<Map<ScheduleDay, List<AnilistAnime>>>(emptyMap())
    val uiState: StateFlow<Map<ScheduleDay, List<AnilistAnime>>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /**
     * The season everything on screen is describing.
     *
     * It lives here rather than beside the control that changes it because it is the input to the
     * load: a second copy held up in the app bar could name one season while the list underneath
     * still held another.
     */
    private val _selected = MutableStateFlow(AnilistSeasonYear.current())
    val selected: StateFlow<AnilistSeasonYear> = _selected.asStateFlow()

    private var loadJob: Job? = null

    init {
        reload()
    }

    fun select(seasonYear: AnilistSeasonYear) {
        if (seasonYear == _selected.value) return

        _selected.value = seasonYear
        reload()
    }

    /**
     * Drops whatever is in flight first. Stepping through the picker starts a load per season, and
     * without this a slow earlier response could arrive last and leave the list showing a season
     * the bar had already moved on from.
     */
    fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { load(_selected.value) }
    }

    private suspend fun load(seasonYear: AnilistSeasonYear) {
        _isLoading.value = true
        _error.value = null

        try {
            _uiState.value = YourAnimesSource.list(seasonYear.year, seasonYear.season)
        } catch (cancellation: CancellationException) {
            // Rethrown rather than reported: this is the load above being replaced, not a failure,
            // and swallowing it would break the cancellation it was asked for.
            throw cancellation
        } catch (failure: Exception) {
            // A season the site never published and a page whose shape has drifted both arrive
            // here, and browsing years puts the first of those one tap away. This used to escape
            // the scope and take the app down with it.
            _error.value = failure
            _uiState.value = emptyMap()
        } finally {
            // A cancelled load leaves the flag to its replacement, which has already raised it.
            // Clearing it unconditionally would race that and drop the spinner mid-load.
            if (currentCoroutineContext().isActive) _isLoading.value = false
        }
    }
}
