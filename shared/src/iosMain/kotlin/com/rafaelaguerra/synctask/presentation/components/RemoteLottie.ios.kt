package com.rafaelaguerra.synctask.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun RemoteLottie(
    url: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    // Neutral placeholder until a Compose-native Lottie renderer is wired up on iOS.
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            shape = CircleShape
        )
    )
}
