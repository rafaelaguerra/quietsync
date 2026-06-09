package com.rafaelaguerra.synctask.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rafaelaguerra.synctask.presentation.components.CalmPrimaryButton
import com.rafaelaguerra.synctask.presentation.components.CalmSecondaryButton
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.paywall_benefits_summary
import com.rafaelaguerra.synctask.resources.paywall_cta_continue_free
import com.rafaelaguerra.synctask.resources.paywall_cta_processing
import com.rafaelaguerra.synctask.resources.paywall_cta_restore
import com.rafaelaguerra.synctask.resources.paywall_cta_unlock
import com.rafaelaguerra.synctask.resources.paywall_icon
import com.rafaelaguerra.synctask.resources.paywall_one_time_payment
import com.rafaelaguerra.synctask.resources.paywall_reason_default
import com.rafaelaguerra.synctask.resources.paywall_reason_edit_existing
import com.rafaelaguerra.synctask.resources.paywall_reason_weekly_limit
import com.rafaelaguerra.synctask.resources.paywall_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PremiumPaywallScreen(
    reason: PaywallReason?,
    premiumPriceLabel: String,
    isPurchaseInProgress: Boolean,
    onPurchaseRequested: () -> Unit,
    onRestoreRequested: () -> Unit,
    onCloseRequested: () -> Unit
) {
    val reasonText = when (reason) {
        PaywallReason.WEEKLY_LIMIT -> stringResource(Res.string.paywall_reason_weekly_limit)
        PaywallReason.EDIT_EXISTING_EVENT -> stringResource(Res.string.paywall_reason_edit_existing)
        null -> stringResource(Res.string.paywall_reason_default)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(Res.string.paywall_icon))
                }

                Text(
                    text = stringResource(Res.string.paywall_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = reasonText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.paywall_one_time_payment),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = premiumPriceLabel,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(Res.string.paywall_benefits_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                CalmPrimaryButton(
                    text = if (isPurchaseInProgress) {
                        stringResource(Res.string.paywall_cta_processing)
                    } else {
                        stringResource(Res.string.paywall_cta_unlock)
                    },
                    onClick = onPurchaseRequested,
                    enabled = !isPurchaseInProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )
                CalmSecondaryButton(
                    text = stringResource(Res.string.paywall_cta_restore),
                    onClick = onRestoreRequested,
                    enabled = !isPurchaseInProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
                CalmSecondaryButton(
                    text = stringResource(Res.string.paywall_cta_continue_free),
                    onClick = onCloseRequested,
                    enabled = !isPurchaseInProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }
    }
}
