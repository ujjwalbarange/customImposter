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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomWord
import com.example.model.Player
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

private val CATEGORIES = listOf(
    "Random",
    "Animals & Nature",
    "Books",
    "Countries & Cities",
    "Famous People",
    "Games & Leisure",
    "Household",
    "Movies & TV",
    "Music",
    "Professions",
    "Sports",
    "Technology",
    "Vehicles"
)

@Composable
fun WordEntryScreen(
    words: List<CustomWord>,
    players: List<Player>,
    playedWordIds: Set<String> = emptySet(),
    showCategoryToImpostor: Boolean = true,
    showAiHintToImpostor: Boolean = true,
    isGeneratingCategoryWords: Boolean = false,
    activeGeneratingCategory: String? = null,
    onToggleCategory: (Boolean) -> Unit = {},
    onToggleAiHint: (Boolean) -> Unit = {},
    onAddWord: (word: String, authorPlayerId: Int?) -> Unit,
    onRemoveWord: (String) -> Unit,
    onClearAll: () -> Unit,
    onCategorySelected: (String) -> Unit = {},
    onOpenCacheView: () -> Unit = {},
    onStartGame: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newWordInput by remember { mutableStateOf("") }
    // Selected author ID for new word; default to first player, null means Anonymous
    var selectedAuthorPlayerId by remember { mutableStateOf<Int?>(players.firstOrNull()?.id) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val canStart = words.size >= 5

    fun handleAddWord() {
        if (newWordInput.isNotBlank()) {
            val currentAuthorId = selectedAuthorPlayerId
            onAddWord(newWordInput.trim(), currentAuthorId)
            newWordInput = ""

            // 1. Player Auto-Advance Loop & 2. End of List Wrapping & 3. Anonymous Exception:
            // When a user adds a word while a specific player is selected (e.g., Player 1),
            // immediately update the active selection to the next player in sequence.
            // If at the end of the roster, wrap around back to Player 1.
            // If Anonymous/None is selected, do NOT advance (remain on Anonymous).
            if (currentAuthorId != null && players.isNotEmpty()) {
                val currentIndex = players.indexOfFirst { it.id == currentAuthorId }
                if (currentIndex != -1) {
                    val nextIndex = (currentIndex + 1) % players.size
                    selectedAuthorPlayerId = players[nextIndex].id
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "CLEAR ALL WORDS?",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "This will remove all ${words.size} custom words from the game.",
                    fontFamily = FredokaFontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "CLEAR ALL",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeleteRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("GAME SETTINGS", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Toggle 1: Show Category to Impostor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Show Category to Impostor",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Provides broad category clue (e.g. Food, Location)",
                                fontFamily = FredokaFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Switch(
                            checked = showCategoryToImpostor,
                            onCheckedChange = onToggleCategory,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PrimaryCyan
                            )
                        )
                    }

                    // Toggle 2: Show Hint to Impostor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Show Hint to Impostor",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Provides a subtle lateral hint to the Impostor",
                                fontFamily = FredokaFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Switch(
                            checked = showAiHintToImpostor,
                            onCheckedChange = onToggleAiHint,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PrimaryCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chunky 'View Cache' Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF8B5CF6))
                            .clickable {
                                showSettingsDialog = false
                                onOpenCacheView()
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .testTag("word_entry_settings_view_cache_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "View Cache",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "View Cache",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite
                            )
                        }
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
                            fontSize = 24.sp,
                            letterSpacing = 1.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Settings Button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DarkPill)
                                    .clickable { showSettingsDialog = true }
                                    .testTag("word_entry_settings_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Game Settings",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

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

            // Word Input Row: White text input + dark circular + button + visible Clear All button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        .size(54.dp)
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
                        fontSize = 30.sp
                    )
                }

                // Visible 'Clear All' Button
                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (words.isNotEmpty()) Color(0xFFFFECEE) else Color(0xFFE2E8F0))
                        .clickable(enabled = words.isNotEmpty()) {
                            showClearDialog = true
                        }
                        .padding(horizontal = 12.dp)
                        .testTag("clear_all_words_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear All Words",
                            tint = if (words.isNotEmpty()) DeleteRed else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Clear All",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (words.isNotEmpty()) DeleteRed else Color(0xFF94A3B8)
                        )
                    }
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

            // Category Quick-Add Slider: Generate 6 words
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ADD 6 WORDS (BY CATEGORY):",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF5A667A)
                    )

                    if (isGeneratingCategoryWords) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryCyan,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Loading...",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.testTag("category_quick_add_slider")
                ) {
                    items(CATEGORIES) { category ->
                        val isThisLoading = isGeneratingCategoryWords && activeGeneratingCategory == category
                        CategoryChip(
                            label = category,
                            isLoading = isThisLoading,
                            enabled = !isGeneratingCategoryWords,
                            onClick = { onCategorySelected(category) }
                        )
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
                        // Completely obfuscate length to fixed string of bullets so players cannot guess the word length
                        val obfuscatedText = "••••••••"

                        // Word Attribution: GAME vs Human Player vs Anonymous
                        val isGame = item.isGameGenerated
                        val authorPlayer = if (!isGame) players.firstOrNull { it.id == item.authorPlayerId } else null

                        val authorColor = when {
                            isGame -> Color(0xFF8B5CF6) // Distinct vibrant purple for GAME
                            authorPlayer != null -> PlayerColors.getOrElse(authorPlayer.colorIndex) { PlayerColors[0] }
                            else -> Color(0xFF94A3B8) // Neutral gray for Anonymous
                        }
                        val authorLabel = when {
                            isGame -> "GAME"
                            authorPlayer != null -> authorPlayer.name
                            else -> "Anonymous"
                        }

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
                                // Author indicator dot (Purple for GAME)
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
                                    text = obfuscatedText,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF6B7280),
                                    letterSpacing = 2.sp
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Author pill indicator (GAME gets a distinct purple badge)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isGame) Color(0xFF8B5CF6).copy(alpha = 0.2f) else authorColor.copy(alpha = 0.25f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = authorLabel,
                                        fontFamily = FredokaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isGame -> Color(0xFF7C3AED)
                                            authorPlayer != null -> TextDark
                                            else -> Color(0xFF475569)
                                        }
                                    )
                                }

                                if (item.id in playedWordIds) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE2E8F0))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Played",
                                            fontFamily = FredokaFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

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
private fun CategoryChip(
    label: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val isRandom = label.equals("Random", ignoreCase = true)
    val bgColor = if (isRandom) Color(0xFF8B5CF6) else DarkPill
    val textColor = PureWhite

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("category_chip_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = PureWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Loading...",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
            } else {
                Text(
                    text = if (isRandom) "🎲 Random" else label,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
            }
        }
    }
}

