package com.rafaelaguerra.synctask.presentation.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rafaelaguerra.synctask.presentation.components.CalmPrimaryButton
import com.rafaelaguerra.synctask.presentation.components.CalmScreenBackground
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.force_update_cta
import com.rafaelaguerra.synctask.resources.force_update_subtitle
import com.rafaelaguerra.synctask.resources.force_update_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ForceUpdateScreen(
    minSupportedVersionCode: Long,
    onUpdateClick: () -> Unit
) {
    CalmScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.force_update_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = stringResource(
                    Res.string.force_update_subtitle,
                    minSupportedVersionCode
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            CalmPrimaryButton(
                text = stringResource(Res.string.force_update_cta),
                onClick = onUpdateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            )
        }
    }
}
