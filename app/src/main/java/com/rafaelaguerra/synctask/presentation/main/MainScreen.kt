package com.rafaelaguerra.synctask.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rafaelaguerra.synctask.R
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.presentation.common.resolve
import com.rafaelaguerra.synctask.presentation.components.CalmHeader
import com.rafaelaguerra.synctask.presentation.components.CalmModePill
import com.rafaelaguerra.synctask.presentation.components.CalmPrimaryButton
import com.rafaelaguerra.synctask.presentation.localization.displayLabel
import com.rafaelaguerra.synctask.presentation.main.components.PremiumSwipeToDeleteCard
import com.rafaelaguerra.synctask.ui.theme.Ink50
import com.rafaelaguerra.synctask.ui.theme.ModeAirplaneDark
import com.rafaelaguerra.synctask.ui.theme.ModeAirplaneLight
import com.rafaelaguerra.synctask.ui.theme.ModeDndDark
import com.rafaelaguerra.synctask.ui.theme.ModeDndLight
import com.rafaelaguerra.synctask.ui.theme.ModeNormalDark
import com.rafaelaguerra.synctask.ui.theme.ModeNormalLight
import com.rafaelaguerra.synctask.ui.theme.ModeSilentDark
import com.rafaelaguerra.synctask.ui.theme.ModeSilentLight
import com.rafaelaguerra.synctask.ui.theme.ModeVibrateDark
import com.rafaelaguerra.synctask.ui.theme.ModeVibrateLight
import java.text.DateFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    canShowSwipeHint: Boolean,
    onSwipeHintShown: () -> Unit,
    onRemoveEventRequested: (Long) -> Unit,
    onFilterSelected: (PhoneState?) -> Unit,
    onCreateEventRequested: () -> Unit,
    onReportIssueRequested: () -> Unit,
    onRefreshRequested: () -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pendingDeleteEvents = remember { mutableStateMapOf<Long, AppManagedEvent>() }
    val pullToRefreshState = rememberPullToRefreshState()

    val requestDelete: (AppManagedEvent) -> Unit = { event ->
        if (!pendingDeleteEvents.containsKey(event.eventId)) {
            pendingDeleteEvents[event.eventId] = event
            // Delete immediately and confirm with a short snackbar (no Undo).
            onRemoveEventRequested(event.eventId)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.snackbar_event_deleted, event.title),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(uiState.appManagedEvents) {
        val validIds = uiState.appManagedEvents.map { it.eventId }.toSet()
        pendingDeleteEvents.keys.toList().forEach { id ->
            if (id !in validIds) pendingDeleteEvents.remove(id)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message.resolve(context))
            onMessageShown()
        }
    }

    val filteredEvents = if (uiState.selectedFilter == null) {
        uiState.appManagedEvents
    } else {
        uiState.appManagedEvents.filter { it.phoneState == uiState.selectedFilter }
    }
    val visibleEvents = filteredEvents.filterNot { pendingDeleteEvents.containsKey(it.eventId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.top_bar_app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onReportIssueRequested) {
                        Icon(
                            imageVector = Icons.Rounded.ReportProblem,
                            contentDescription = stringResource(R.string.content_desc_report_issue)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventRequested,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.content_desc_create_event)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isEventsLoading,
            onRefresh = onRefreshRequested,
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                CalmHeader(
                    title = stringResource(R.string.main_header_title),
                    subtitle = stringResource(R.string.main_header_subtitle)
                )

                if (uiState.appManagedEvents.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModeFilterChip(
                            label = stringResource(R.string.filter_all),
                            selected = uiState.selectedFilter == null,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { onFilterSelected(null) }
                        )
                        PhoneState.entries.forEach { state ->
                            ModeFilterChip(
                                label = state.displayLabel(),
                                selected = uiState.selectedFilter == state,
                                accentColor = state.cardColor(),
                                onClick = { onFilterSelected(state) }
                            )
                        }
                    }
                }

                when {
                    uiState.isEventsLoading && visibleEvents.isEmpty() -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(3) { SkeletonEventCard() }
                        }
                    }

                    visibleEvents.isEmpty() -> {
                        CalmEmptyState(
                            isFiltered = uiState.selectedFilter != null,
                            onCreateEvent = onCreateEventRequested
                        )
                    }

                    else -> {
                        var shouldShowHintOnFirst by remember(
                            visibleEvents.firstOrNull()?.eventId,
                            canShowSwipeHint
                        ) {
                            mutableStateOf(canShowSwipeHint)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            visibleEvents.forEachIndexed { index, event ->
                                key(event.eventId) {
                                    SwipeableEventCard(
                                        event = event,
                                        cardColor = event.phoneState.cardColor(),
                                        textColor = event.phoneState.textColor(),
                                        showSwipeHint = index == 0 && shouldShowHintOnFirst,
                                        onSwipeHintShown = {
                                            shouldShowHintOnFirst = false
                                            onSwipeHintShown()
                                        },
                                        onDeleteConfirmed = requestDelete
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeFilterChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accentColor.copy(alpha = 0.22f),
            selectedLabelColor = accentColor,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = accentColor.copy(alpha = 0.4f),
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(999.dp)
    )
}

@Composable
private fun SkeletonEventCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
    )
}

@Composable
private fun CalmEmptyState(
    isFiltered: Boolean,
    onCreateEvent: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isFiltered) {
                    Text("🔎")
                } else {
                    CalmEmptyStateLottie()
                }
            }
            Text(
                text = if (isFiltered) {
                    stringResource(R.string.empty_filtered_title)
                } else {
                    stringResource(R.string.empty_clear_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isFiltered) {
                    stringResource(R.string.empty_filtered_subtitle)
                } else {
                    stringResource(R.string.empty_clear_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            CalmPrimaryButton(
                text = stringResource(R.string.cta_create_event),
                onClick = onCreateEvent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalmEmptyStateLottie() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(ZEN_BLUE_LOTUS_LOTTIE_JSON_URL)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(72.dp)
    )
}

private const val ZEN_BLUE_LOTUS_LOTTIE_JSON_URL =
    "https://assets-v2.lottiefiles.com/a/2bd270a4-7a5d-460f-bc20-7b7741855367/zqm17j5MMX.json"

@Composable
private fun SwipeableEventCard(
    event: AppManagedEvent,
    cardColor: Color,
    textColor: Color,
    showSwipeHint: Boolean,
    onSwipeHintShown: () -> Unit,
    onDeleteConfirmed: (AppManagedEvent) -> Unit
) {
    PremiumSwipeToDeleteCard(
        modifier = Modifier.fillMaxWidth(),
        showHint = showSwipeHint,
        onHintShown = onSwipeHintShown,
        onDelete = { onDeleteConfirmed(event) }
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatEventWindow(event),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                CalmModePill(
                    label = event.phoneState.displayLabel(),
                    background = Color.Black.copy(alpha = 0.09f),
                    contentColor = textColor
                )
            }
        }
    }
}

@Composable
private fun formatEventWindow(event: AppManagedEvent): String {
    if (event.startDateTimeMillis <= 0L || event.endDateTimeMillis <= 0L) {
        return stringResource(R.string.schedule_undefined)
    }
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
    return "${dateFormat.format(event.startDateTimeMillis)} · " +
        "${timeFormat.format(event.startDateTimeMillis)} - ${timeFormat.format(event.endDateTimeMillis)}"
}

@Composable
fun PhoneState.cardColor(): Color {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    return when (this) {
        PhoneState.NORMAL -> if (isDark) ModeNormalDark else ModeNormalLight
        PhoneState.VIBRATE -> if (isDark) ModeVibrateDark else ModeVibrateLight
        PhoneState.SILENT -> if (isDark) ModeSilentDark else ModeSilentLight
        PhoneState.DO_NOT_DISTURB -> if (isDark) ModeDndDark else ModeDndLight
        PhoneState.AIRPLANE_MODE -> if (isDark) ModeAirplaneDark else ModeAirplaneLight
    }
}

@Composable
fun PhoneState.textColor(): Color {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    return when (this) {
        PhoneState.SILENT -> Ink50
        else -> if (isDark) Color(0xFFE7ECEE) else Color(0xFF2A2825)
    }
}
