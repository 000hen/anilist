package one.muisnowdevs.apps.anilist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.AnilistSeason
import one.muisnowdevs.apps.anilist.source.getCurrentSeason
import one.muisnowdevs.apps.anilist.source.youranimes.YourAnimesSource
import java.time.DayOfWeek
import java.time.Year

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<Map<DayOfWeek, List<AnilistAnime>>>(emptyMap())
    val uiState: StateFlow<Map<DayOfWeek, List<AnilistAnime>>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val season = getCurrentSeason()
            load(season.first, season.second)
        }
    }

    private suspend fun load(year: Year, season: AnilistSeason) {
        _isLoading.value = true
        _uiState.value = YourAnimesSource.list(year, season)
        _isLoading.value = false
    }

    fun reload(year: Year, season: AnilistSeason) = viewModelScope.launch {
        load(year, season)
    }
}