package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.components.ArcadeButton
import com.example.ui.theme.DarkPill
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LightIceBlue
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.PlayerColors
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDark

@Composable
fun LobbyScreen(
    players: List<Player>,
    onPlayClick: () -> Unit,
    onAddPlayer: () -> Unit,
    onRemovePlayer: (Int) -> Unit,
    onRenamePlayer: (Int, String) -> Unit,
    onResetScores: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playerToRename by remember { mutableStateOf<Player?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val hasScores = players.any { it.score > 0 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightIceBlue)
    ) {
        // Bottom decorative cloud/pill shapes matching Screenshot 1
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cloud pills at bottom
            drawRoundRect(
                color = PureWhite,
                topLeft = Offset(w * 0.05f, h * 0.84f),
                size = Size(w * 0.25f, 32f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFD8E4F2),
                topLeft = Offset(w * 0.12f, h * 0.88f),
                size = Size(w * 0.82f, 30f),
                cornerRadius = CornerRadius(15f, 15f)
            )
            drawRoundRect(
                color = PureWhite,
                topLeft = Offset(w * 0.72f, h * 0.91f),
                size = Size(w * 0.22f, 26f),
                cornerRadius = CornerRadius(13f, 13f)
            )
            drawCircle(
                color = Color(0xFFD8E4F2),
                radius = 14f,
                center = Offset(w * 0.28f, h * 0.95f)
            )
            drawRoundRect(
                color = Color(0xFFD8E4F2),
                topLeft = Offset(w * 0.35f, h * 0.94f),
                size = Size(w * 0.52f, 26f),
                cornerRadius = CornerRadius(13f, 13f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Navy Card with Title and Instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(NavyBackground)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Bar with Title and Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Decorative Question/Info Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                                .clickable { showRulesDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                color = NavyBackground,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Text(
                            text = "IMPOSTOR",
                            color = PureWhite,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = 1.sp
                        )

                        // Settings Gear
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                                .clickable { showSettingsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = NavyBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rules Text
                    Text(
                        text = "Play with up to 10 friends! Give smart clues about your secret word without being too obvious. Discuss who the imposter could be and vote them out to win the game. Enter secret words on the next screen!",
                        color = PureWhite,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player List & Add Player Row
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    PlayerRowItem(
                        player = player,
                        hasScores = hasScores,
                        canDelete = players.size > 3,
                        onEdit = {
                            playerToRename = player
                            renameInputText = player.name
                        },
                        onDelete = { onRemovePlayer(player.id) }
                    )
                }

                // Add Player Button (Max 10)
                if (players.size < 10) {
                    item {
                        AddPlayerRowItem(onAdd = onAddPlayer)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }

        // Bottom Action Bar: Large PLAY button and Help icon
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ArcadeButton(
                text = "PLAY",
                onClick = onPlayClick,
                backgroundColor = PrimaryCyan,
                textColor = PureWhite,
                modifier = Modifier.weight(1f),
                testTag = "lobby_play_button"
            )

            // Help Question Circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(DarkPill)
                    .clickable { showRulesDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    color = PureWhite,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            }
        }
    }

    // Rename Dialog
    playerToRename?.let { player ->
        AlertDialog(
            onDismissRequest = { playerToRename = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "RENAME PLAYER",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    singleLine = true,
                    label = { Text("Player Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenamePlayer(player.id, renameInputText)
                        playerToRename = null
                    }
                ) {
                    Text(
                        "SAVE",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToRename = null }) {
                    Text("CANCEL", fontFamily = FredokaFontFamily)
                }
            }
        )
    }

    // Rules Dialog
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "HOW TO PLAY",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Enter 5 or more secret words in the custom words screen.", fontFamily = FredokaFontFamily)
                    Text("2. Pass the device around. Each player taps to view their role in secret.", fontFamily = FredokaFontFamily)
                    Text("3. Civilians see the Secret Word. The Impostor only sees 'IMPOSTOR'!", fontFamily = FredokaFontFamily)
                    Text("4. Discuss! Everyone gives a clever one-word clue related to the word.", fontFamily = FredokaFontFamily)
                    Text("5. Vote together on who you think is bluffing.", fontFamily = FredokaFontFamily)
                    Text("6. Reveal: If you catch the Impostor, civilians win (+1 pt). If the Impostor escapes, Impostor wins (+1 pt)!", fontFamily = FredokaFontFamily)
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("GOT IT!", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                }
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("GAME SETTINGS", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Active Players: ${players.size} (Min 3, Max 10)",
                        fontFamily = FredokaFontFamily
                    )
                    TextButton(
                        onClick = {
                            onResetScores()
                            showSettingsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = PrimaryCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Reset All Player Scores",
                            color = PrimaryCyan,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("CLOSE", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PlayerRowItem(
    player: Player,
    hasScores: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = PlayerColors.getOrElse(player.colorIndex) { PlayerColors[0] }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main Player Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(bgColor)
                .clickable(onClick = onEdit)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasScores) {
                        // Score Badge matching Screenshot 9
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PureWhite.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${player.score}",
                                color = TextDark,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    // Edit Pencil Icon
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit name",
                        tint = TextDark.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Delete button if > 3 players matching Screenshot 2
        if (canDelete) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(DarkPill)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove player",
                    tint = Color(0xFFFF5964),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPlayerRowItem(
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PureWhite)
                .clickable(onClick = onAdd)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Add Player",
                color = TextDark,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(PureWhite)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = TextDark,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )
        }
    }
}
