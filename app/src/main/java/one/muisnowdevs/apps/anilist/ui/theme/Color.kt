package one.muisnowdevs.apps.anilist.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * The yellow a star is filled with once it stands for something switched on.
 *
 * Held outside the colour scheme deliberately: [AnilistTheme] takes dynamic colour from Android 12
 * up, so anything read off the scheme is whatever the wallpaper happens to be that day. A star has
 * to keep reading as a star, so this is a fixed amber — dark enough to hold on a light bar, bright
 * enough to hold on a dark one.
 */
val FavoriteStar = Color(0xFFFFC107)