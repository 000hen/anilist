package one.muisnowdevs.apps.anilist.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.anilist.R
import one.muisnowdevs.apps.anilist.ui.theme.AnilistTheme

/**
 * App-wide chrome — theme and top app bar — wrapped around whatever screen is showing.
 * The scaffold padding is handed to [content] so each screen decides how to consume it.
 *
 * [navigationIcon] and [subtitle] are slots rather than anything this layout builds itself: the
 * controls worth putting in the bar belong to the screen underneath it, and are hoisted past here
 * to whoever owns both. That keeps the chrome ignorant of any one screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLayout(
    navigationIcon: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    AnilistTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    // Taller than the stock 64dp, which fits two lines of text but leaves nothing
                    // for a subtitle that is pressable — the season selector needs a touch target,
                    // not just a baseline.
                    expandedHeight = 76.dp,
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.app_name))
                            subtitle()
                        }
                    },
                    navigationIcon = navigationIcon
                )
            },
            content = content
        )
    }
}
