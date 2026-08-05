package one.muisnowdevs.apps.anilist.ui.component

import android.content.ClipData
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

/**
 * The exception behind a failed load, written out for a bug report rather than for the reader.
 *
 * It is a sheet because of who each half is for. The screen underneath has to answer someone who
 * only wants the week back, and a trace naming an offset inside someone else's script tag answers
 * nothing they asked; one press away it costs that reader nothing and still hands whoever is fixing
 * the scrape the line that says which step drifted.
 *
 * Copying is the point of showing it at all — a trace that has to be retyped may as well not be
 * here — so the button carries the whole [Throwable.stackTraceToString] rather than the summary
 * above it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDetailSheet(error: Throwable, onDismissRequest: () -> Unit = {}) {
    // Walking the trace is not free and nothing about it changes while the sheet is open.
    val trace = remember(error) { error.stackTraceToString() }

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    // The confirmation sits on the button rather than in a toast: from Android 13 the system posts
    // its own copy notice, and a second one beside it reads as two copies.
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (!isCopied) return@LaunchedEffect

        delay(2000.minutes)
        isCopied = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "詳細資訊",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(ClipData.newPlainText("錯誤詳細資訊", trace))
                            )
                            isCopied = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isCopied) "已複製" else "複製",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Repeats the trace's own first line on purpose: it is the one line here worth reading,
            // and the block below is laid out to be copied rather than read down.
            Text(
                text = error.toString(),
                style = MaterialTheme.typography.bodyMedium
            )

            // Capped rather than sized to the trace. `fill = false` lets a short one stay short,
            // while a long one stops at the sheet instead of pushing the copy button off the top of
            // it — the trace scrolls inside the card, both ways, since unwrapped frame lines run
            // well past the screen and wrapping them makes the depth impossible to follow.
            Card(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = trace,
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false
                )
            }
        }
    }
}
