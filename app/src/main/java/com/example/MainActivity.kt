package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GamePhase
import com.example.ui.screens.CacheViewScreen
import com.example.ui.screens.DiscussionScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.PassAndPlayScreen
import com.example.ui.screens.RevealScreen
import com.example.ui.screens.VotingScreen
import com.example.ui.screens.WordEntryScreen
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.ImpostorTheme
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(application) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpostorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ImpostorApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ImpostorApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle system back navigation according to game phase
    BackHandler(enabled = uiState.phase != GamePhase.LOBBY && !uiState.isSettingUpRound) {
        when (uiState.phase) {
            GamePhase.LOBBY -> { /* Default system exit */ }
            GamePhase.CACHE_VIEW -> viewModel.closeCacheView()
            GamePhase.WORD_ENTRY -> viewModel.returnToLobby()
            GamePhase.PASS_AND_PLAY -> viewModel.returnToLobby()
            GamePhase.DISCUSSION -> viewModel.returnToLobby()
            GamePhase.VOTING -> viewModel.finishDiscussion() // or return to lobby
            GamePhase.REVEAL -> viewModel.finishReveal()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "phase_transition"
        ) { phase ->
            when (phase) {
                GamePhase.LOBBY -> {
                    LobbyScreen(
                        players = uiState.players,
                        showCategoryToImpostor = uiState.showCategoryToImpostor,
                        showAiHintToImpostor = uiState.showAiHintToImpostor,
                        onToggleCategory = { viewModel.setShowCategoryToImpostor(it) },
                        onToggleAiHint = { viewModel.setShowAiHintToImpostor(it) },
                        onPlayClick = { viewModel.goToWordEntry() },
                        onAddPlayer = { viewModel.addPlayer() },
                        onRemovePlayer = { viewModel.removePlayer(it) },
                        onRenamePlayer = { id, name -> viewModel.renamePlayer(id, name) },
                        onResetScores = { viewModel.resetScores() },
                        onOpenCacheView = { viewModel.openCacheView() }
                    )
                }

                GamePhase.WORD_ENTRY -> {
                    WordEntryScreen(
                        words = uiState.words,
                        players = uiState.players,
                        playedWordIds = uiState.playedWordIds,
                        showCategoryToImpostor = uiState.showCategoryToImpostor,
                        showAiHintToImpostor = uiState.showAiHintToImpostor,
                        isGeneratingCategoryWords = uiState.isGeneratingCategoryWords,
                        activeGeneratingCategory = uiState.activeGeneratingCategory,
                        onToggleCategory = { viewModel.setShowCategoryToImpostor(it) },
                        onToggleAiHint = { viewModel.setShowAiHintToImpostor(it) },
                        onAddWord = { word, authorId -> viewModel.addWord(word, authorId) },
                        onRemoveWord = { viewModel.removeWord(it) },
                        onClearAll = { viewModel.clearAllWords() },
                        onCategorySelected = { viewModel.generateWordsForCategory(it) },
                        onOpenCacheView = { viewModel.openCacheView() },
                        onStartGame = { viewModel.startGameRound() },
                        onBackClick = { viewModel.returnToLobby() }
                    )
                }

                GamePhase.CACHE_VIEW -> {
                    CacheViewScreen(
                        cachedWords = uiState.cachedWords,
                        onDeleteWord = { viewModel.deleteCachedWord(it) },
                        onDeleteWords = { viewModel.deleteCachedWords(it) },
                        onClearAllCache = { viewModel.clearAllCache() },
                        onBackClick = { viewModel.closeCacheView() }
                    )
                }

                GamePhase.PASS_AND_PLAY -> {
                    val currentPlayer = uiState.players.getOrElse(uiState.currentPassIndex) { uiState.players[0] }
                    val nextPlayer = uiState.players.getOrNull(uiState.currentPassIndex + 1)
                    val isLast = uiState.currentPassIndex == uiState.players.size - 1
                    val isImpostor = (currentPlayer.id == uiState.impostorPlayerId)

                    PassAndPlayScreen(
                        currentPlayer = currentPlayer,
                        nextPlayer = nextPlayer,
                        isLastPlayer = isLast,
                        isCardRevealed = uiState.isCardRevealed,
                        secretWord = uiState.currentSecretWord,
                        isImpostor = isImpostor,
                        showCategory = uiState.showCategoryToImpostor,
                        showAiHint = uiState.showAiHintToImpostor,
                        aiCategory = uiState.aiCategory,
                        aiHint = uiState.aiHint,
                        onRevealCard = { viewModel.revealCard() },
                        onGotItClick = { viewModel.nextPassPlayer() },
                        onBackClick = { viewModel.returnToLobby() }
                    )
                }

                GamePhase.DISCUSSION -> {
                    DiscussionScreen(
                        players = uiState.players,
                        onDoneClick = { viewModel.finishDiscussion() },
                        onBackClick = { viewModel.returnToLobby() }
                    )
                }

                GamePhase.VOTING -> {
                    VotingScreen(
                        players = uiState.players,
                        selectedPlayerId = uiState.selectedVotePlayerId,
                        onPlayerSelected = { viewModel.selectVotePlayer(it) },
                        onSubmitVote = { viewModel.submitVote() },
                        onBackClick = { viewModel.returnToLobby() }
                    )
                }

                GamePhase.REVEAL -> {
                    RevealScreen(
                        result = uiState.lastRoundResult,
                        onNextClick = { viewModel.finishReveal() },
                        onBackClick = { viewModel.returnToLobby() }
                    )
                }
            }
        }

        // Global Loading Overlay: Clean "Loading..." text without internal AI branding
        if (uiState.isSettingUpRound) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC08121E))
                    .clickable(enabled = false) {}
                    .testTag("round_setup_loading_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(NavyBackground)
                        .padding(horizontal = 28.dp, vertical = 32.dp)
                ) {
                    CircularProgressIndicator(
                        color = PrimaryCyan,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Loading...",
                        color = PureWhite,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
