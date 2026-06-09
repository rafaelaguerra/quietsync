package com.rafaelaguerra.synctask.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rafaelaguerra.synctask.presentation.components.CalmPrimaryButton
import com.rafaelaguerra.synctask.presentation.components.CalmSecondaryButton
import com.rafaelaguerra.synctask.presentation.components.RemoteLottie
import com.rafaelaguerra.synctask.presentation.localization.displayLabel
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.cta_create_another_event
import com.rafaelaguerra.synctask.resources.cta_view_events
import com.rafaelaguerra.synctask.resources.label_end
import com.rafaelaguerra.synctask.resources.label_location
import com.rafaelaguerra.synctask.resources.label_start
import com.rafaelaguerra.synctask.resources.success_event_window_message
import com.rafaelaguerra.synctask.resources.success_subtitle
import com.rafaelaguerra.synctask.resources.success_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSuccessScreen(
    preview: CreatedEventPreview,
    onGoToList: () -> Unit,
    onCreateAnotherEvent: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RemoteLottie(
                url = SUCCESS_CHECK_LOTTIE_URL,
                modifier = Modifier.size(150.dp)
            )

            Text(
                text = stringResource(Res.string.success_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(Res.string.success_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(
                    Res.string.success_event_window_message,
                    preview.title,
                    preview.phoneState.displayLabel()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = preview.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preview.phoneState.displayLabel(),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    SuccessRow(
                        label = stringResource(Res.string.label_start),
                        value = formatShortDateTime(preview.startDateTimeMillis)
                    )
                    SuccessRow(
                        label = stringResource(Res.string.label_end),
                        value = formatShortDateTime(preview.endDateTimeMillis)
                    )
                    if (preview.location.isNotBlank()) {
                        SuccessRow(
                            label = stringResource(Res.string.label_location),
                            value = preview.location
                        )
                    }
                }
            }

            CalmPrimaryButton(
                text = stringResource(Res.string.cta_view_events),
                onClick = onGoToList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            CalmSecondaryButton(
                text = stringResource(Res.string.cta_create_another_event),
                onClick = onCreateAnotherEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SuccessRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private const val SUCCESS_CHECK_LOTTIE_URL =
    "https://assets6.lottiefiles.com/packages/lf20_jbrw3hcz.json"
