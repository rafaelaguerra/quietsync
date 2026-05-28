package com.rafaelaguerra.synctask.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rafaelaguerra.synctask.R
import kotlinx.coroutines.launch

private const val ONBOARDING_PAGE_COUNT = 2

// Shared vertical rhythm so both pages line up perfectly while swiping.
private val HeroHeight: Dp = 300.dp
private val PageContentSpacing: Dp = 18.dp
private val PageHorizontalPadding: Dp = 4.dp

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> PermissionsPage()
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PageIndicator(
                    pageCount = ONBOARDING_PAGE_COUNT,
                    currentPage = pagerState.currentPage
                )

                val isLastPage = pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLastPage) {
                            stringResource(R.string.onboarding_cta_start)
                        } else {
                            stringResource(R.string.onboarding_cta_next)
                        },
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // The "Skip intro" shortcut only makes sense on page 1, but we keep the
                // TextButton mounted on every page so the primary CTA above never shifts
                // a pixel between pages — page 2 just renders it invisible & disabled.
                val isFirstPage = pagerState.currentPage == 0
                TextButton(
                    onClick = onSkip,
                    enabled = isFirstPage,
                    modifier = Modifier
                        .alpha(if (isFirstPage) 1f else 0f)
                        .semantics { if (!isFirstPage) hideFromAccessibility() }
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_cta_skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Shared scaffold for onboarding pages so both pages line up identically.
 * Hero block has a fixed height; below it sits the title and content.
 * Using the same structure on every page keeps the swipe transition smooth.
 */
@Composable
private fun OnboardingPageScaffold(
    hero: @Composable () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PageHorizontalPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PageContentSpacing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroHeight),
            contentAlignment = Alignment.Center
        ) {
            hero()
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        )

        content()

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPageScaffold(
        hero = { OnboardingIllustration() },
        title = stringResource(R.string.onboarding_title)
    ) {
        Text(
            text = stringResource(R.string.onboarding_subtitle_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_subtitle_promise),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.onboarding_subtitle_action))
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        )
    }
}

@Composable
private fun PermissionsPage() {
    OnboardingPageScaffold(
        hero = { PermissionsHero() },
        title = stringResource(R.string.onboarding_perm_title)
    ) {
        Text(
            text = stringResource(R.string.onboarding_perm_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        )

        PermissionItem(
            icon = { iconColor ->
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = iconColor
                )
            },
            title = stringResource(R.string.onboarding_perm_calendar_title),
            body = stringResource(R.string.onboarding_perm_calendar_body),
            tintContainer = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.primary
        )

        PermissionItem(
            icon = { iconColor ->
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = iconColor
                )
            },
            title = stringResource(R.string.onboarding_perm_mode_title),
            body = stringResource(R.string.onboarding_perm_mode_body),
            tintContainer = MaterialTheme.colorScheme.tertiaryContainer,
            iconColor = MaterialTheme.colorScheme.tertiary
        )

        Text(
            text = stringResource(R.string.onboarding_perm_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionItem(
    icon: @Composable (iconColor: Color) -> Unit,
    title: String,
    body: String,
    tintContainer: Color,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tintContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon(iconColor)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .let { if (isActive) it.size(width = 22.dp, height = 8.dp) else it.size(8.dp) }
                    .background(
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

@Composable
private fun OnboardingIllustration() {
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.Url(YOGA_DEVELOPER_LOTTIE_JSON_URL)
    )
    val progress = animateLottieCompositionAsState(
        composition = compositionResult.value,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = compositionResult.value,
        progress = { progress.value },
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroHeight)
    )
}

@Composable
private fun PermissionsHero() {
    // Two overlapping pastel circles with the permission icons. Sized to fill the same
    // hero block as the Lottie on page 1, so titles align across pages while swiping.
    val ringSize = 140.dp
    val iconSize = 56.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .offset(x = (-44).dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize)
            )
        }
        Box(
            modifier = Modifier
                .size(ringSize)
                .offset(x = 44.dp)
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

private const val YOGA_DEVELOPER_LOTTIE_JSON_URL =
    "https://assets-v2.lottiefiles.com/a/12bac05e-116d-11ee-916d-d342c074ee37/9bGfbYmMFX.json"
