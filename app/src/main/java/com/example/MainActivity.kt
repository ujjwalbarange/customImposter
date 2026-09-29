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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.GamePhase
import com.example.ui.screens.DiscussionScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.PassAndPlayScreen
import com.example.ui.screens.RevealScreen
import com.example.ui.screens.VotingScreen
import com.example.ui.screens.WordEntryScreen
import com.example.ui.theme.ImpostorTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

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
    BackHandler(enabled = uiState.phase != GamePhase.LOBBY) {
        when (uiState.phase) {
            GamePhase.LOBBY -> { /* Default system exit */ }
            GamePhase.WORD_ENTRY -> viewModel.returnToLobby()
            GamePhase.PASS_AND_PLAY -> viewModel.returnToLobby()
            GamePhase.DISCUSSION -> viewModel.returnToLobby()
            GamePhase.VOTING -> viewModel.finishDiscussion() // or return to lobby
            GamePhase.REVEAL -> viewModel.finishReveal()
        }
    }

    AnimatedContent(
        targetState = uiState.phase,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "phase_transition"
    ) { phase ->
        when (phase) {
            GamePhase.LOBBY -> {
                LobbyScreen(
                    players = uiState.players,
                    onPlayClick = { viewModel.goToWordEntry() },
                    onAddPlayer = { viewModel.addPlayer() },
                    onRemovePlayer = { viewModel.removePlayer(it) },
                    onRenamePlayer = { id, name -> viewModel.renamePlayer(id, name) },
                    onResetScores = { viewModel.resetScores() }
                )
            }

            GamePhase.WORD_ENTRY -> {
                WordEntryScreen(
                    words = uiState.words,
                    players = uiState.players,
                    playedWordIds = uiState.playedWordIds,
                    onAddWord = { word, authorId -> viewModel.addWord(word, authorId) },
                    onRemoveWord = { viewModel.removeWord(it) },
                    onClearAll = { viewModel.clearAllWords() },
                    onQuickPackSelected = { viewModel.quickFillPack(it) },
                    onStartGame = { viewModel.startGameRound() },
                    onBackClick = { viewModel.returnToLobby() }
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
}
