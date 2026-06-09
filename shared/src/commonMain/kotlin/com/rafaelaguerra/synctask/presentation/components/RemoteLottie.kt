package com.rafaelaguerra.synctask.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Renders a looping Lottie animation loaded from a remote URL.
 * Android uses Airbnb's lottie-compose; iOS currently renders a neutral placeholder
 * (a Compose-native Lottie renderer such as Compottie can be dropped in later).
 */
@Composable
expect fun RemoteLottie(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
)
