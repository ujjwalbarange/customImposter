package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoundResult
import com.example.ui.components.ArcadeButton
import com.example.ui.components.CircularBackButton
import com.example.ui.components.ExposedStampGraphic
import com.example.ui.components.QuestionMarkWatermarkBackground
import com.example.ui.components.SpySilhouetteWithCollar
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DeleteRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WinGreen

@Composable
fun RevealScreen(
    result: RoundResult?,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (result == null) {
        onNextClick()
        return
    }

    val civiliansWon = result.civiliansWon

    QuestionMarkWatermarkBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Back button & Verdict banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                CircularBackButton(onClick = onBackClick)

                Spacer(modifier = Modifier.height(10.dp))

                // Win/Loss Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (civiliansWon) WinGreen else DeleteRed)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (civiliansWon) "🎉 CIVILIANS WIN! (+1 PT)" else "🕵️ IMPOSTOR WINS! (+1 PT)",
                        color = PureWhite,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Middle Section: Silhouette + EXPOSED stamp + Info cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Spy silhouette with tilted EXPOSED stamp layered on top
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 200.dp)
                        .testTag("exposed_graphic_container"),
                    contentAlignment = Alignment.Center
                ) {
                    SpySilhouetteWithCollar(
                        modifier = Modifier.size(200.dp)
                    )

                    ExposedStampGraphic(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Secret Word Card matching Screenshot 8
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF222744))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Secret word",
                            color = Color(0xFF7E8A9E),
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.secretWord,
                            color = PureWhite,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Impostor Card matching Screenshot 8
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF222744))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Impostor",
                            color = Color(0xFF7E8A9E),
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.impostor.name,
                            color = DeleteRed,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    }
                }

                if (!civiliansWon) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Group voted for: ${result.votedPlayer.name}",
                        color = Color(0xFF9EABB9),
                        fontFamily = FredokaFontFamily,
                        fontSize = 14.sp
                    )
                }
            }

            // Bottom Section: NEXT button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                ArcadeButton(
                    text = "NEXT",
                    onClick = onNextClick,
                    backgroundColor = DarkPill,
                    textColor = PureWhite,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "reveal_next_button"
                )
            }
        }
    }
}
