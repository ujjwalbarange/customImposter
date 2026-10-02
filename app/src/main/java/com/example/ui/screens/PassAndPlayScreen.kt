package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.model.formatDisplayWord
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.components.ArcadeButton
import com.example.ui.components.CircularBackButton
import com.example.ui.components.DiagonalStripedCard
import com.example.ui.components.SpyFedoraIllustration
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DeleteRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.PlayerColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDark

@Composable
fun PassAndPlayScreen(
    currentPlayer: Player,
    nextPlayer: Player?,
    isLastPlayer: Boolean,
    isCardRevealed: Boolean,
    secretWord: String,
    isImpostor: Boolean,
    showCategory: Boolean = true,
    showAiHint: Boolean = false,
    aiCategory: String? = null,
    aiHint: String? = null,
    onRevealCard: () -> Unit,
    onGotItClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuitDialog by remember { mutableStateOf(false) }

    val playerBgColor = PlayerColors.getOrElse(currentPlayer.colorIndex) { PlayerColors[0] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(playerBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Back button and "The word for PLAYER X"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CircularBackButton(
                    onClick = { showQuitDialog = true },
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "The word for",
                        color = TextDark.copy(alpha = 0.85f),
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentPlayer.name.uppercase(),
                        color = TextDark,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        lineHeight = 42.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Middle Section: Center Card (Striped or Revealed)
            DiagonalStripedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clickable {
                        if (!isCardRevealed) {
                            onRevealCard()
                        }
                    }
                    .testTag("role_reveal_card")
            ) {
                if (!isCardRevealed) {
                    // State A: Hidden - "?" and "TAP to reveal"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "?",
                            color = Color(0xFF6B7280),
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "TAP to reveal",
                            color = PureWhite,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    // State B: Revealed (Civilian Word vs Impostor)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        if (isImpostor) {
                            // Impostor view: Fedora + Sunglasses + IMPOSTOR
                            SpyFedoraIllustration(
                                modifier = Modifier.size(width = 96.dp, height = 66.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "IMPOSTOR",
                                color = DeleteRed,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                letterSpacing = 2.sp
                            )

                            // Show Category if enabled
                            if (showCategory && !aiCategory.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "CATEGORY: ${aiCategory.uppercase()}",
                                    color = PureWhite,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("impostor_category_text")
                                )
                            }

                            // Show Hint pill if enabled
                            if (showAiHint && !aiHint.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E232A).copy(alpha = 0.92f))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .testTag("impostor_hint_pill"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "Hint",
                                            tint = Color(0xFFFFD166),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "HINT: ${aiHint.uppercase()}",
                                            color = PureWhite,
                                            fontFamily = FredokaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Civilian view: Secret Word with proper line spacing and adaptive font size
                            val formattedWord = formatDisplayWord(secretWord).uppercase()
                            val (wordFontSize, wordLineHeight) = when {
                                formattedWord.length > 16 -> 26.sp to 36.sp
                                formattedWord.length > 10 -> 30.sp to 42.sp
                                else -> 36.sp to 48.sp
                            }
                            Text(
                                text = formattedWord,
                                color = PureWhite,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = wordFontSize,
                                lineHeight = wordLineHeight,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Keep it secret! Give subtle clues.",
                                color = PureWhite.copy(alpha = 0.7f),
                                fontFamily = FredokaFontFamily,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom Section: Advance info & GOT IT button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isCardRevealed) {
                    val passText = if (isLastPlayer) {
                        "Advance to Discussion"
                    } else {
                        "Pass to ${nextPlayer?.name ?: "Next Player"}"
                    }

                    Text(
                        text = passText,
                        color = TextDark,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ArcadeButton(
                        text = "GOT IT",
                        onClick = onGotItClick,
                        backgroundColor = DarkPill,
                        textColor = PureWhite,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "got_it_button"
                    )
                } else {
                    // Placeholder spacing when not revealed
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("CANCEL ROUND?", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to return to the lobby? Current round progress will be lost.",
                    fontFamily = FredokaFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuitDialog = false
                        onBackClick()
                    }
                ) {
                    Text("YES, QUIT", color = DeleteRed, fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("CONTINUE PLAYING", fontFamily = FredokaFontFamily)
                }
            }
        )
    }
}
