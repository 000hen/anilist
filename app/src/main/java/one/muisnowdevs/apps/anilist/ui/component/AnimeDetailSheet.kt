package one.muisnowdevs.apps.anilist.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.anilist.source.AnimeInformation

/** Full detail of a single title: artwork, where to watch it, synopsis and official links. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailSheet(anime: AnimeInformation, onDismissRequest: () -> Unit = {}) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(true)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CoverImage(
                        url = anime.cover,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                StreamingIconRow(
                    streamings = anime.streaming + anime.adultstreaming,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = anime.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            item { Text(text = anime.description, style = MaterialTheme.typography.bodyMedium) }
            item { Text(text = anime.episode, style = MaterialTheme.typography.bodyMedium) }

            item { OfficialLinksCard(links = anime.olinks) }
        }
    }
}
