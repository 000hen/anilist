package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import one.muisnowdevs.apps.anilist.formatTimeInDay
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.getCurrentMinute
import one.muisnowdevs.apps.anilist.source.getMinimalMinute
import kotlin.time.Duration.Companion.minutes

/**
 * Schedule row: cover, airing time, title and the tags describing the title.
 *
 * [isFavorite] is drawn as a [FavoriteTag] beside the airing time; the card only reports it, the
 * caller owns it.
 */
@Composable
fun AnimeCard(
    anime: AnilistAnime,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onClick: () -> Unit = {}
) {
    val weeklyTime by remember(anime) { mutableIntStateOf(anime.onAirTime.minuteOfWeek) }
    var isOver by remember(weeklyTime) {
        mutableStateOf(weeklyTime <= getCurrentMinute() && weeklyTime >= getMinimalMinute())
    }

    LaunchedEffect(Unit) {
        if (isOver) return@LaunchedEffect
        if (weeklyTime <= getMinimalMinute()) return@LaunchedEffect

        delay((weeklyTime - getCurrentMinute()).minutes)
        isOver = true
    }

    val color by animateColorAsState(
        if (isOver) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainer
    )

    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CoverImage(
                url = anime.image,
                modifier = Modifier.fillMaxWidth(0.2f),
                description = "Image of ${anime.name}"
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimeInDay(anime.onAirTime.minute),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    AnimatedVisibility(visible = isFavorite) { FavoriteTag() }
                }

                Text(
                    text = anime.name,
                    style = MaterialTheme.typography.titleLarge
                )

                if (anime.isAdult) AdultTag(modifier = Modifier.padding(top = 4.dp))

                RecommendationTagRow(
                    tags = anime.genres,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}
