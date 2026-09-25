package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.example.model.Player
import com.example.ui.components.ArcadeButton
import com.example.ui.components.CircularBackButton
import com.example.ui.components.QuestionMarkWatermarkBackground
import com.example.ui.theme.DarkPill
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.PlayerColors
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDark

@Composable
fun VotingScreen(
    players: List<Player>,
    selectedPlayerId: Int?,
    onPlayerSelected: (Int) -> Unit,
    onSubmitVote: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuestionMarkWatermarkBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CircularBackButton(onClick = onBackClick)
            }

            // Title & Instructions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VOTING",
                    color = PrimaryCyan,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Decide as a group who is the impostor",
                    color = PureWhite,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Player Cards List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    val isSelected = (selectedPlayerId == player.id)
                    val bgColor = PlayerColors.getOrElse(player.colorIndex) { PlayerColors[0] }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 4.dp,
                                        color = PureWhite,
                                        shape = RoundedCornerShape(26.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .padding(if (isSelected) 3.dp else 0.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(bgColor)
                            .clickable { onPlayerSelected(player.id) }
                            .padding(horizontal = 24.dp)
                            .testTag("voting_player_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = player.name,
                                color = TextDark,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PureWhite),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = TextDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }

        // Bottom Fixed Area: NEXT button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            ArcadeButton(
                text = "NEXT",
                onClick = onSubmitVote,
                enabled = selectedPlayerId != null,
                backgroundColor = DarkPill,
                textColor = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_vote_button"
            )
        }
    }
}
