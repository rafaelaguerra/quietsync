package com.rafaelaguerra.synctask.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.model.RepeatPeriod
import com.rafaelaguerra.synctask.domain.model.Weekday
import com.rafaelaguerra.synctask.presentation.components.CalmHeader
import com.rafaelaguerra.synctask.presentation.components.CalmPrimaryButton
import com.rafaelaguerra.synctask.presentation.localization.displayLabel
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.content_desc_back
import com.rafaelaguerra.synctask.resources.create_event_title
import com.rafaelaguerra.synctask.resources.create_header_subtitle
import com.rafaelaguerra.synctask.resources.create_header_title
import com.rafaelaguerra.synctask.resources.cta_create_event
import com.rafaelaguerra.synctask.resources.cta_creating_event
import com.rafaelaguerra.synctask.resources.date_picker_cancel
import com.rafaelaguerra.synctask.resources.date_picker_ok
import com.rafaelaguerra.synctask.resources.device_mode_description
import com.rafaelaguerra.synctask.resources.error_event_name_required
import com.rafaelaguerra.synctask.resources.helper_event_name
import com.rafaelaguerra.synctask.resources.label_days_of_week
import com.rafaelaguerra.synctask.resources.label_end
import com.rafaelaguerra.synctask.resources.label_event_name
import com.rafaelaguerra.synctask.resources.label_frequency
import com.rafaelaguerra.synctask.resources.label_start
import com.rafaelaguerra.synctask.resources.placeholder_event_description
import com.rafaelaguerra.synctask.resources.placeholder_event_location
import com.rafaelaguerra.synctask.resources.placeholder_event_title_example
import com.rafaelaguerra.synctask.resources.preparing_automation
import com.rafaelaguerra.synctask.resources.recurring_event_subtitle
import com.rafaelaguerra.synctask.resources.recurring_event_title
import com.rafaelaguerra.synctask.resources.section_details
import com.rafaelaguerra.synctask.resources.section_device_state
import com.rafaelaguerra.synctask.resources.section_recurrence
import com.rafaelaguerra.synctask.resources.section_schedule
import com.rafaelaguerra.synctask.resources.time_picker_title
import com.rafaelaguerra.synctask.resources.weekday_friday_short
import com.rafaelaguerra.synctask.resources.weekday_monday_short
import com.rafaelaguerra.synctask.resources.weekday_saturday_short
import com.rafaelaguerra.synctask.resources.weekday_sunday_short
import com.rafaelaguerra.synctask.resources.weekday_thursday_short
import com.rafaelaguerra.synctask.resources.weekday_tuesday_short
import com.rafaelaguerra.synctask.resources.weekday_wednesday_short
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEventScreen(
    uiState: MainUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onStartDateTimeChange: (Long) -> Unit,
    onEndDateTimeChange: (Long) -> Unit,
    onPhoneStateSelected: (PhoneState) -> Unit,
    onRecurringToggled: (Boolean) -> Unit,
    onRecurrenceDayToggled: (Weekday) -> Unit,
    onRecurrencePeriodSelected: (RepeatPeriod) -> Unit,
    onCreateEventRequested: () -> Unit
) {
    var hasTriedSubmit by rememberSaveable { mutableStateOf(false) }
    val trimmedTitle = uiState.title.trim()
    val titleHasError = hasTriedSubmit && trimmedTitle.isEmpty()

    var pickerTarget by remember { mutableStateOf<DateTimePickerTarget?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.create_event_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.content_desc_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalmHeader(
                    title = stringResource(Res.string.create_header_title),
                    subtitle = stringResource(Res.string.create_header_subtitle)
                )

                CalmSection(title = stringResource(Res.string.section_details)) {
                    CalmInput(
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        placeholder = stringResource(Res.string.placeholder_event_title_example),
                        label = stringResource(Res.string.label_event_name),
                        isRequired = true,
                        isError = titleHasError,
                        supportingText = if (titleHasError) {
                            stringResource(Res.string.error_event_name_required)
                        } else {
                            stringResource(Res.string.helper_event_name)
                        }
                    )
                    CalmInput(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        placeholder = stringResource(Res.string.placeholder_event_description),
                        minLines = 3
                    )
                    CalmInput(
                        value = uiState.location,
                        onValueChange = onLocationChange,
                        placeholder = stringResource(Res.string.placeholder_event_location)
                    )
                }

                CalmSection(title = stringResource(Res.string.section_schedule)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DateTimeField(
                            title = stringResource(Res.string.label_start),
                            dateTimeMillis = uiState.startDateTimeMillis,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                pickerTarget = DateTimePickerTarget(
                                    initialMillis = uiState.startDateTimeMillis,
                                    onPicked = onStartDateTimeChange
                                )
                            }
                        )
                        DateTimeField(
                            title = stringResource(Res.string.label_end),
                            dateTimeMillis = uiState.endDateTimeMillis,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                pickerTarget = DateTimePickerTarget(
                                    initialMillis = uiState.endDateTimeMillis,
                                    onPicked = onEndDateTimeChange
                                )
                            }
                        )
                    }
                }

                CalmSection(title = stringResource(Res.string.section_recurrence)) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    stringResource(Res.string.recurring_event_title),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    stringResource(Res.string.recurring_event_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.isRecurring,
                                onCheckedChange = onRecurringToggled,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    if (uiState.isRecurring) {
                        if (uiState.recurrencePeriod == RepeatPeriod.WEEKLY) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    stringResource(Res.string.label_days_of_week),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    weekDayOptions().forEach { (weekday, label) ->
                                        SoftToggleChip(
                                            label = label,
                                            selected = weekday in uiState.recurrenceDays,
                                            onClick = { onRecurrenceDayToggled(weekday) }
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(Res.string.label_frequency),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RepeatPeriod.entries.forEach { period ->
                                    SoftToggleChip(
                                        label = period.displayLabel(),
                                        selected = uiState.recurrencePeriod == period,
                                        onClick = { onRecurrencePeriodSelected(period) }
                                    )
                                }
                            }
                        }
                    }
                }

                DeviceModeSection(
                    selectedPhoneState = uiState.selectedPhoneState,
                    onPhoneStateSelected = onPhoneStateSelected
                )

                CalmPrimaryButton(
                    text = if (uiState.isLoading) {
                        stringResource(Res.string.cta_creating_event)
                    } else {
                        stringResource(Res.string.cta_create_event)
                    },
                    onClick = {
                        hasTriedSubmit = true
                        if (trimmedTitle.isEmpty()) return@CalmPrimaryButton
                        onCreateEventRequested()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(Res.string.preparing_automation),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }

    pickerTarget?.let { target ->
        BrandedDateTimePicker(
            initialMillis = target.initialMillis,
            onConfirm = { selectedMillis ->
                pickerTarget = null
                target.onPicked(selectedMillis)
            },
            onDismiss = { pickerTarget = null }
        )
    }
}

private data class DateTimePickerTarget(
    val initialMillis: Long,
    val onPicked: (Long) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandedDateTimePicker(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var stage by remember { mutableStateOf(DateTimePickerStage.Date) }
    var pickedDateUtcMillis by remember { mutableStateOf(initialMillis) }

    when (stage) {
        DateTimePickerStage.Date -> {
            val utcInitial = remember(initialMillis) { toUtcStartOfDayMillis(initialMillis) }
            val dateState = rememberDatePickerState(initialSelectedDateMillis = utcInitial)

            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        val selected = dateState.selectedDateMillis ?: utcInitial
                        pickedDateUtcMillis = selected
                        stage = DateTimePickerStage.Time
                    }) {
                        Text(stringResource(Res.string.date_picker_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.date_picker_cancel))
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            ) {
                DatePicker(
                    state = dateState,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = MaterialTheme.colorScheme.primary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        DateTimePickerStage.Time -> {
            val timeState = rememberTimePickerState(
                initialHour = hourOf(initialMillis),
                initialMinute = minuteOf(initialMillis),
                is24Hour = true
            )

            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.time_picker_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TimePicker(
                            state = timeState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectorColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                                clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(Res.string.date_picker_cancel))
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            TextButton(onClick = {
                                onConfirm(
                                    combineUtcDateWithLocalTime(
                                        pickedDateUtcMillis,
                                        timeState.hour,
                                        timeState.minute
                                    )
                                )
                            }) {
                                Text(stringResource(Res.string.date_picker_ok))
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class DateTimePickerStage { Date, Time }

@Composable
private fun DeviceModeSection(
    selectedPhoneState: PhoneState,
    onPhoneStateSelected: (PhoneState) -> Unit
) {
    var modeMenuExpanded by remember { mutableStateOf(false) }
    val sectionShape = RoundedCornerShape(24.dp)
    val selectorShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                shape = sectionShape
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                shape = sectionShape
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(999.dp)
                    )
            )
            Text(
                text = stringResource(Res.string.section_device_state),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = stringResource(Res.string.device_mode_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = selectorShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = selectorShape
                    )
                    .clickable { modeMenuExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPhoneState.displayLabel(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = modeMenuExpanded,
                onDismissRequest = { modeMenuExpanded = false }
            ) {
                PhoneState.entries.forEach { state ->
                    DropdownMenuItem(
                        text = { Text(state.displayLabel()) },
                        onClick = {
                            modeMenuExpanded = false
                            onPhoneStateSelected(state)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalmSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalmInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    label: String? = null,
    isRequired: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let {
            {
                Text(
                    text = if (isRequired) "$it *" else it,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        },
        supportingText = supportingText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        isError = isError,
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun DateTimeField(
    title: String,
    dateTimeMillis: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatShortDate(dateTimeMillis),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatShortTime(dateTimeMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SoftToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun weekDayOptions(): List<Pair<Weekday, String>> = listOf(
    Weekday.MONDAY to stringResource(Res.string.weekday_monday_short),
    Weekday.TUESDAY to stringResource(Res.string.weekday_tuesday_short),
    Weekday.WEDNESDAY to stringResource(Res.string.weekday_wednesday_short),
    Weekday.THURSDAY to stringResource(Res.string.weekday_thursday_short),
    Weekday.FRIDAY to stringResource(Res.string.weekday_friday_short),
    Weekday.SATURDAY to stringResource(Res.string.weekday_saturday_short),
    Weekday.SUNDAY to stringResource(Res.string.weekday_sunday_short)
)
