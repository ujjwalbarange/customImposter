package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.formatDisplayWord
import com.example.service.CachedWordCombination
import com.example.ui.components.CircularBackButton
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DeleteRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LightIceBlue
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CacheViewScreen(
    cachedWords: List<CachedWordCombination>,
    onDeleteWord: (String) -> Unit,
    onDeleteWords: (Set<String>) -> Unit,
    onClearAllCache: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedWords by remember { mutableStateOf(setOf<String>()) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    // Distinct categories in the current cache
    val existingCategories by remember(cachedWords) {
        derivedStateOf {
            listOf("All") + cachedWords.map { it.category.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
    }

    // Filtered words based on search query and selected category
    val filteredWords by remember(cachedWords, searchQuery, selectedCategory) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase().replace('_', ' ')
            cachedWords.filter { item ->
                val matchesCategory = (selectedCategory == "All") || item.category.equals(selectedCategory, ignoreCase = true)
                val formattedWord = formatDisplayWord(item.word).lowercase()
                val rawWord = item.word.lowercase()
                val matchesQuery = query.isEmpty() ||
                        formattedWord.contains(query) ||
                        rawWord.contains(query) ||
                        item.category.lowercase().contains(query) ||
                        item.hint.lowercase().contains(query)
                matchesCategory && matchesQuery
            }
        }
    }

    // Confirmation dialog for clearing all cache
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "CLEAR ALL CACHE?",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all ${cachedWords.size} words from your offline local storage.",
                    fontFamily = FredokaFontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllCache()
                        showClearAllDialog = false
                        isSelectionMode = false
                        selectedWords = emptySet()
                    }
                ) {
                    Text(
                        text = "CLEAR EVERYTHING",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeleteRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
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

    // Confirmation dialog for deleting selected items
    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "DELETE SELECTED WORDS?",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${selectedWords.size} selected word(s) from the cache?",
                    fontFamily = FredokaFontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteWords(selectedWords)
                        showDeleteSelectedDialog = false
                        selectedWords = emptySet()
                        isSelectionMode = false
                    }
                ) {
                    Text(
                        text = "DELETE",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeleteRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
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
                            text = "OFFLINE CACHE",
                            color = PureWhite,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Total count badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkPill)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cachedWords.size}",
                                    color = PureWhite,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // Clear All Button (Red trash icon)
                            if (cachedWords.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B1E2B))
                                        .clickable { showClearAllDialog = true }
                                        .testTag("clear_all_cache_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Clear Cache",
                                        tint = DeleteRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Words, categories, and hints stored on this device for offline play. Long-press any card for bulk actions.",
                        color = PureWhite.copy(alpha = 0.85f),
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search words or categories...",
                            fontFamily = FredokaFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFF8E99A8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF8E99A8),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF8E99A8),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    },
                    singleLine = true,
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
                        .fillMaxWidth()
                        .testTag("cache_search_field")
                )
            }

            // Category Filter Slider
            if (existingCategories.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cache_category_slider")
                ) {
                    items(existingCategories) { cat ->
                        val isSelected = (selectedCategory == cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryCyan else PureWhite)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("cache_category_chip_${cat.lowercase().replace(" ", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) PureWhite else TextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cached Word Items List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = if (isSelectionMode) 100.dp else 24.dp)
            ) {
                if (filteredWords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No matching words found" else "No cached words available",
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Try searching for a different word or category" else "Words generated in the game will automatically be stored here!",
                                    fontFamily = FredokaFontFamily,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredWords, key = { it.word }) { item ->
                        val isSelected = selectedWords.contains(item.word)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, Color(0xFF8B5CF6), RoundedCornerShape(20.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(if (isSelected) Color(0xFFF3E8FF) else PureWhite)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedWords = if (isSelected) {
                                                selectedWords - item.word
                                            } else {
                                                selectedWords + item.word
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedWords = setOf(item.word)
                                        } else {
                                            selectedWords = if (isSelected) {
                                                selectedWords - item.word
                                            } else {
                                                selectedWords + item.word
                                            }
                                        }
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("cached_word_item_${item.word.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Selection checkbox indicator if in selection mode
                                    if (isSelectionMode) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formatDisplayWord(item.word),
                                            fontFamily = FredokaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = TextDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Category Pill
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE2E8F0))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    fontFamily = FredokaFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }

                                            // Hint Pill
                                            if (item.hint.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFFEF3C7))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Lightbulb,
                                                            contentDescription = "Hint",
                                                            tint = Color(0xFFD97706),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = item.hint,
                                                            fontFamily = FredokaFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF92400E)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Individual Delete Button (X)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFECEE))
                                        .clickable { onDeleteWord(item.word) }
                                        .testTag("delete_cached_word_${item.word.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete word",
                                        tint = DeleteRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selection Action Bar (Floating at Bottom)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NavyBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Select All / Deselect All Button
                    val allFilteredSelected = filteredWords.isNotEmpty() && filteredWords.all { it.word in selectedWords }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkPill)
                            .clickable {
                                selectedWords = if (allFilteredSelected) {
                                    selectedWords - filteredWords.map { it.word }.toSet()
                                } else {
                                    selectedWords + filteredWords.map { it.word }.toSet()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("select_all_toggle_button")
                    ) {
                        Text(
                            text = if (allFilteredSelected) "Deselect All" else "Select All",
                            color = PureWhite,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Count Badge
                    Text(
                        text = "${selectedWords.size} selected",
                        color = PureWhite,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Delete Selected Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedWords.isNotEmpty()) DeleteRed else Color(0xFF6B7280))
                                .clickable(enabled = selectedWords.isNotEmpty()) {
                                    showDeleteSelectedDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("delete_selected_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Delete",
                                    color = PureWhite,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Close Selection Mode
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkPill)
                                .clickable {
                                    isSelectionMode = false
                                    selectedWords = emptySet()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Selection",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
