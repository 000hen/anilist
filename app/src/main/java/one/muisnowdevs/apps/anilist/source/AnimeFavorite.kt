package one.muisnowdevs.apps.anilist.source

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val STORAGE_NAME = "favorites"
private const val STORAGE_KEY = "favorites"

/**
 * The ids of the titles the user has starred, backed by shared preferences.
 *
 * [favorites] is a [StateFlow] so the UI can observe one source of truth rather than each row
 * holding its own copy of the flag: an edit made anywhere reaches every collector immediately.
 */
class AnimeFavorite private constructor(context: Context) {
    private val storage = context.applicationContext
        .getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)

    private val _favorites =
        MutableStateFlow(storage.getStringSet(STORAGE_KEY, emptySet())?.toSet() ?: emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    companion object {
        @Volatile
        private var instance: AnimeFavorite? = null

        fun getInstance(context: Context): AnimeFavorite {
            return instance ?: synchronized(this) {
                instance ?: AnimeFavorite(context).also { instance = it }
            }
        }
    }

    fun addFavorite(id: String) = setFavorite(id, true)

    fun removeFavorite(id: String) = setFavorite(id, false)

    /**
     * Stars or unstars [id]. Writing the value it already holds is a no-op, so callers driven by a
     * gesture may fire more than once without churning storage.
     */
    fun setFavorite(id: String, favorite: Boolean) {
        val updated = if (favorite) _favorites.value + id else _favorites.value - id
        if (updated == _favorites.value) return

        _favorites.value = updated
        storage.edit { putStringSet(STORAGE_KEY, updated) }
    }
}
