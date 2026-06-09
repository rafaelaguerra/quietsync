package com.rafaelaguerra.synctask.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rafaelaguerra.synctask.presentation.components.RemoteLottie
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.onboarding_cta_next
import com.rafaelaguerra.synctask.resources.onboarding_cta_skip
import com.rafaelaguerra.synctask.resources.onboarding_cta_start
import com.rafaelaguerra.synctask.resources.onboarding_perm_calendar_body
import com.rafaelaguerra.synctask.resources.onboarding_perm_calendar_body_ios
import com.rafaelaguerra.synctask.resources.onboarding_perm_footer_ios
import com.rafaelaguerra.synctask.resources.onboarding_perm_subtitle_ios
import com.rafaelaguerra.synctask.resources.onboarding_perm_title_ios
import com.rafaelaguerra.synctask.resources.onboarding_perm_calendar_title
import com.rafaelaguerra.synctask.resources.onboarding_perm_footer
import com.rafaelaguerra.synctask.resources.onboarding_perm_mode_body
import com.rafaelaguerra.synctask.resources.onboarding_perm_mode_title
import com.rafaelaguerra.synctask.resources.onboarding_perm_subtitle
import com.rafaelaguerra.synctask.resources.onboarding_perm_title
import com.rafaelaguerra.synctask.resources.onboarding_subtitle_action
import com.rafaelaguerra.synctask.resources.onboarding_subtitle_intro
import com.rafaelaguerra.synctask.resources.onboarding_subtitle_promise
import com.rafaelaguerra.synctask.resources.onboarding_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val ONBOARDING_PAGE_COUNT = 2

/** Share of the pager height used by the Lottie hero on every onboarding step. */
private const val HeroHeightFraction = 0.46f
/** Per-asset scale tweaks so both Lotties read at a similar visual size (Fit + shared box). */
private const val WelcomeLottieScale = 1.22f
private const val PermissionsLottieScale = 0.92f
private val HeroTopMargin: Dp = 32.dp
private val PageContentSpacing: Dp = 18.dp
private val CompactPageContentSpacing: Dp = 10.dp
private val PageHorizontalPadding: Dp = 4.dp
private val CtaTopSpacing: Dp = 24.dp
private val CtaBlockSpacing: Dp = 20.dp
private val CtaSecondarySpacing: Dp = 4.dp

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    permissionMode: OnboardingPermissionMode = OnboardingPermissionMode.FULL
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
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> PermissionsPage(permissionMode = permissionMode)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = CtaTopSpacing, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CtaBlockSpacing)
            ) {
                PageIndicator(
                    pageCount = ONBOARDING_PAGE_COUNT,
                    currentPage = pagerState.currentPage
                )

                val isLastPage = pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CtaSecondarySpacing)
                ) {
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
                                stringResource(Res.string.onboarding_cta_start)
                            } else {
                                stringResource(Res.string.onboarding_cta_next)
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
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .alpha(if (isFirstPage) 1f else 0f)
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_cta_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shared scaffold for onboarding pages.
 * Hero sits below the status bar with consistent top margin; text sits just above the CTAs.
 */
@Composable
private fun OnboardingPageScaffold(
    lottieUrl: String,
    title: String,
    lottieScale: Float = 1f,
    titleStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    contentSpacing: Dp = PageContentSpacing,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PageHorizontalPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(HeroHeightFraction)
                .statusBarsPadding()
                .padding(top = HeroTopMargin),
            contentAlignment = Alignment.Center
        ) {
            RemoteLottie(
                url = lottieUrl,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(lottieScale)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            OnboardingTextBlock(
                title = title,
                titleStyle = titleStyle,
                contentSpacing = contentSpacing,
                content = content
            )
        }
    }
}

@Composable
private fun OnboardingTextBlock(
    title: String,
    titleStyle: TextStyle,
    contentSpacing: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(contentSpacing)
    ) {
        Text(
            text = title,
            style = titleStyle,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        content()
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPageScaffold(
        lottieUrl = YOGA_DEVELOPER_LOTTIE_JSON_URL,
        lottieScale = WelcomeLottieScale,
        title = stringResource(Res.string.onboarding_title)
    ) {
        Text(
            text = stringResource(Res.string.onboarding_subtitle_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        )

        Text(
            text = stringResource(Res.string.onboarding_subtitle_promise),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(Res.string.onboarding_subtitle_action))
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
private fun PermissionsPage(permissionMode: OnboardingPermissionMode) {
    val calendarOnly = permissionMode == OnboardingPermissionMode.CALENDAR_ONLY

    OnboardingPageScaffold(
        lottieUrl = PERMISSION_LOTTIE_JSON_URL,
        lottieScale = PermissionsLottieScale,
        title = stringResource(
            if (calendarOnly) Res.string.onboarding_perm_title_ios else Res.string.onboarding_perm_title
        ),
        contentSpacing = CompactPageContentSpacing
    ) {
        Text(
            text = stringResource(
                if (calendarOnly) Res.string.onboarding_perm_subtitle_ios else Res.string.onboarding_perm_subtitle
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        PermissionItem(
            compact = true,
            icon = { iconColor ->
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = iconColor
                )
            },
            title = stringResource(Res.string.onboarding_perm_calendar_title),
            body = stringResource(
                if (calendarOnly) {
                    Res.string.onboarding_perm_calendar_body_ios
                } else {
                    Res.string.onboarding_perm_calendar_body
                }
            ),
            tintContainer = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.primary
        )

        if (!calendarOnly) {
            PermissionItem(
                compact = true,
                icon = { iconColor ->
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = iconColor
                    )
                },
                title = stringResource(Res.string.onboarding_perm_mode_title),
                body = stringResource(Res.string.onboarding_perm_mode_body),
                tintContainer = MaterialTheme.colorScheme.tertiaryContainer,
                iconColor = MaterialTheme.colorScheme.tertiary
            )
        }

        Text(
            text = stringResource(
                if (calendarOnly) Res.string.onboarding_perm_footer_ios else Res.string.onboarding_perm_footer
            ),
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
    iconColor: Color,
    compact: Boolean = false
) {
    val cardPadding = if (compact) 12.dp else 16.dp
    val iconBoxSize = if (compact) 32.dp else 40.dp
    val cornerRadius = if (compact) 16.dp else 20.dp
    val rowSpacing = if (compact) 12.dp else 14.dp
    val titleStyle = if (compact) {
        MaterialTheme.typography.titleSmall
    } else {
        MaterialTheme.typography.titleMedium
    }
    val bodyStyle = if (compact) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.bodyMedium
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(cardPadding),
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
        verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .background(tintContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon(iconColor)
        }
        Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 4.dp)) {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = bodyStyle,
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

private const val YOGA_DEVELOPER_LOTTIE_JSON_URL =
    "https://assets-v2.lottiefiles.com/a/12bac05e-116d-11ee-916d-d342c074ee37/9bGfbYmMFX.json"

private const val PERMISSION_LOTTIE_JSON_URL =
    "https://assets-v2.lottiefiles.com/a/59316726-117d-11ee-a0d1-8fe186a07308/NzmiE1kLJk.json"
