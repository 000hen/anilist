package one.muisnowdevs.apps.anilist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import one.muisnowdevs.apps.anilist.getCurrentSession
import one.muisnowdevs.apps.anilist.source.AnimeInformation
import one.muisnowdevs.apps.anilist.source.Season
import one.muisnowdevs.apps.anilist.source.Week
import one.muisnowdevs.apps.anilist.source.YourAnimesSource

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<Map<Week, List<AnimeInformation>>>(emptyMap())
    val uiState: StateFlow<Map<Week, List<AnimeInformation>>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val season = getCurrentSession()
            load(season.first, season.second)
        }
    }

    private suspend fun load(year: Int, season: Season) {
        _isLoading.value = true
        _uiState.value = YourAnimesSource.load(year, season)
        _isLoading.value = false
    }

    fun reload(year: Int, season: Season) = viewModelScope.launch {
        load(year, season)
    }
}