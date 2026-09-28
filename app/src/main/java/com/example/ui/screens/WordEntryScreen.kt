package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomWord
import com.example.model.Player
import com.example.model.WordPresets
import com.example.ui.components.ArcadeButton
import com.example.ui.components.CircularBackButton
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DeleteRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LightIceBlue
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.PlayerColors
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDark
import com.example.ui.theme.WinGreen

@Composable
fun WordEntryScreen(
    words: List<CustomWord>,
    players: List<Player>,
    onAddWord: (word: String, authorPlayerId: Int?) -> Unit,
    onRemoveWord: (String) -> Unit,
    onQuickPackSelected: (List<String>) -> Unit,
    onStartGame: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newWordInput by remember { mutableStateOf("") }
    // Selected author ID for new word; null means Anonymous
    var selectedAuthorPlayerId by remember { mutableStateOf<Int?>(null) }
    // Set of word IDs that are temporarily unmasked by user
    val revealedWordIds = remember { mutableStateListOf<String>() }

    val canStart = words.size >= 5

    fun handleAddWord() {
        if (newWordInput.isNotBlank()) {
            onAddWord(newWordInput.trim(), selectedAuthorPlayerId)
            newWordInput = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightIceBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Navy Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(NavyBackground)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularBackButton(onClick = onBackClick)

                        Text(
                            text = "SECRET WORDS",
                            color = PureWhite,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = 1.sp
                        )

                        // Word Count Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (canStart) WinGreen else DarkPill)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${words.size}/5",
                                color = PureWhite,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Enter at least 5 words for the game. Choose who added it so they can never be the Impostor for their own word!",
                        color = PureWhite,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }

            // Word Input Row: White text input + dark circular + button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = newWordInput,
                    onValueChange = { newWordInput = it },
                    placeholder = {
                        Text(
                            text = "Type a secret word...",
                            fontFamily = FredokaFontFamily,
                            color = Color(0xFF8E99A8)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleAddWord() }),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("word_input_field")
                )

                // Circular Add Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkPill)
                        .clickable(onClick = { handleAddWord() })
                        .testTag("add_word_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = PureWhite,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }
            }

            // 1. Player Selection Slider (Directly below word input)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ADDED BY:",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF5A667A),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.testTag("player_selection_slider")
                ) {
                    // Neutral / Gray "Anonymous" pill
                    item {
                        val isAnonymous = (selectedAuthorPlayerId == null)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isAnonymous) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = DarkPill,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(if (isAnonymous) Color(0xFF6C757D) else Color(0xFFE2E8F0))
                                .clickable { selectedAuthorPlayerId = null }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("author_pill_anonymous"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isAnonymous) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Anonymous",
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isAnonymous) PureWhite else Color(0xFF4A5568)
                                )
                            }
                        }
                    }

                    // One button for each active player with matching color and name
                    items(players, key = { it.id }) { player ->
                        val isSelected = (selectedAuthorPlayerId == player.id)
                        val playerColor = PlayerColors.getOrElse(player.colorIndex) { PlayerColors[0] }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = DarkPill,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(playerColor)
                                .clickable {
                                    selectedAuthorPlayerId = if (isSelected) null else player.id
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("author_pill_${player.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = TextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = player.name,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            }

            // Quick preset pack chips for easy one-tap filling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "QUICK PACKS (TAP TO LOAD):",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF5A667A),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        PresetChip(label = "Household", onClick = { onQuickPackSelected(WordPresets.HOUSEHOLD) })
                    }
                    item {
                        PresetChip(label = "Food & Treats", onClick = { onQuickPackSelected(WordPresets.FOOD) })
                    }
                    item {
                        PresetChip(label = "Animals", onClick = { onQuickPackSelected(WordPresets.ANIMALS) })
                    }
                    item {
                        PresetChip(label = "Party Pack", onClick = { onQuickPackSelected(WordPresets.PARTY_PACK) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Secret Words Obfuscated List with color-coding
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (words.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No words added yet.\nEnter words above or choose a Quick Pack!",
                                fontFamily = FredokaFontFamily,
                                color = Color(0xFF7E8B9B),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(words, key = { _, item -> item.id }) { index, item ->
                        val isRevealed = revealedWordIds.contains(item.id)
                        val obfuscatedText = "•".repeat(item.word.length)

                        // 3. Colored Word List: Lookup author's color
                        val authorPlayer = players.firstOrNull { it.id == item.authorPlayerId }
                        val authorColor = if (authorPlayer != null) {
                            PlayerColors.getOrElse(authorPlayer.colorIndex) { PlayerColors[0] }
                        } else {
                            Color(0xFF94A3B8) // Neutral gray for Anonymous
                        }
                        val authorLabel = authorPlayer?.name ?: "Anonymous"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureWhite)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Author indicator dot matching player's exact color
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(authorColor)
                                        .border(1.5.dp, PureWhite, CircleShape)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Word ${index + 1}: ",
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark
                                )

                                Text(
                                    text = if (isRevealed) item.word else obfuscatedText,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isRevealed) PrimaryCyan else Color(0xFF6B7280),
                                    letterSpacing = if (isRevealed) 0.5.sp else 2.sp
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Small author pill indicator
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(authorColor.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = authorLabel,
                                        fontFamily = FredokaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (authorPlayer != null) TextDark else Color(0xFF475569)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Peek/Reveal toggle icon
                                Icon(
                                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle peek word",
                                    tint = Color(0xFF8E99A8),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            if (isRevealed) {
                                                revealedWordIds.remove(item.id)
                                            } else {
                                                revealedWordIds.add(item.id)
                                            }
                                        }
                                )

                                // Red X Delete Icon
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFECEE))
                                        .clickable { onRemoveWord(item.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete word",
                                        tint = DeleteRed,
                                        modifier = Modifier.size(20.dp)
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

        // Bottom Action Area: START GAME (disabled when < 5 words)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ArcadeButton(
                text = if (canStart) "START GAME" else "ADD ${5 - words.size} MORE WORDS",
                onClick = onStartGame,
                enabled = canStart,
                backgroundColor = PrimaryCyan,
                textColor = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                testTag = "start_game_button"
            )
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ $label",
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NavyBackground
        )
    }
}

