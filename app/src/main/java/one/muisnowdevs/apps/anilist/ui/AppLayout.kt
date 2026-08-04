package one.muisnowdevs.apps.anilist.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import one.muisnowdevs.apps.anilist.R
import one.muisnowdevs.apps.anilist.ui.theme.AnilistTheme

/**
 * App-wide chrome — theme and top app bar — wrapped around whatever screen is showing.
 * The scaffold padding is handed to [content] so each screen decides how to consume it.
 *
 * [navigationIcon] is a slot rather than anything this layout builds itself: the actions worth
 * putting at the leading edge of the bar belong to the screen underneath it, and are hoisted past
 * here to whoever owns both. That keeps the chrome ignorant of any one screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLayout(
    navigationIcon: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    AnilistTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = navigationIcon
                )
            },
            content = content
        )
    }
}
