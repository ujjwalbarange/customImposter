package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CustomWord
import com.example.model.GamePhase
import com.example.model.Player
import com.example.model.RoundResult
import com.example.model.WordPresets
import com.example.service.GeminiHintService
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val phase: GamePhase = GamePhase.LOBBY,
    val players: List<Player> = listOf(
        Player(id = 1, name = "Player 1", colorIndex = 0),
        Player(id = 2, name = "Player 2", colorIndex = 1),
        Player(id = 3, name = "Player 3", colorIndex = 2),
        Player(id = 4, name = "Player 4", colorIndex = 3)
    ),
    val words: List<CustomWord> = WordPresets.HOUSEHOLD.take(5).map { CustomWord(word = it) },
    val playedWordIds: Set<String> = emptySet(),
    val showCategoryToImpostor: Boolean = true,
    val showAiHintToImpostor: Boolean = true,
    val isSettingUpRound: Boolean = false,
    val aiCategory: String? = null,
    val aiHint: String? = null,
    val currentPassIndex: Int = 0,
    val isCardRevealed: Boolean = false,
    val currentSecretWord: String = "",
    val impostorPlayerId: Int = -1,
    val selectedVotePlayerId: Int? = null,
    val lastRoundResult: RoundResult? = null
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private val secureRandom = SecureRandom()

    // --- Settings ---
    fun setShowCategoryToImpostor(enabled: Boolean) {
        _uiState.update { it.copy(showCategoryToImpostor = enabled) }
    }

    fun setShowAiHintToImpostor(enabled: Boolean) {
        _uiState.update { it.copy(showAiHintToImpostor = enabled) }
    }

    // --- Player Management ---
    fun addPlayer() {
        _uiState.update { current ->
            if (current.players.size >= 10) return@update current
            val newId = (current.players.maxOfOrNull { it.id } ?: 0) + 1
            val nextColorIndex = current.players.size % 10
            val newPlayer = Player(
                id = newId,
                name = "Player ${current.players.size + 1}",
                colorIndex = nextColorIndex
            )
            current.copy(players = current.players + newPlayer)
        }
    }

    fun removePlayer(playerId: Int) {
        _uiState.update { current ->
            if (current.players.size <= 3) return@update current
            current.copy(players = current.players.filterNot { it.id == playerId })
        }
    }

    fun renamePlayer(playerId: Int, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { current ->
            current.copy(
                players = current.players.map { player ->
                    if (player.id == playerId) player.copy(name = trimmed) else player
                }
            )
        }
    }

    // --- Word Management ---
    fun addWord(wordText: String, authorPlayerId: Int? = null) {
        val trimmed = wordText.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { current ->
            current.copy(words = current.words + CustomWord(word = trimmed, authorPlayerId = authorPlayerId))
        }
    }

    fun removeWord(wordId: String) {
        _uiState.update { current ->
            current.copy(
                words = current.words.filterNot { it.id == wordId },
                playedWordIds = current.playedWordIds - wordId
            )
        }
    }

    fun quickFillPack(pack: List<String>) {
        _uiState.update { current ->
            val newWords = pack.map { CustomWord(word = it, authorPlayerId = null) }
            current.copy(words = newWords, playedWordIds = emptySet())
        }
    }

    fun clearAllWords() {
        _uiState.update { current ->
            current.copy(words = emptyList(), playedWordIds = emptySet())
        }
    }

    // --- State Transitions ---
    fun goToWordEntry() {
        _uiState.update { it.copy(phase = GamePhase.WORD_ENTRY) }
    }

    fun returnToLobby() {
        _uiState.update {
            it.copy(
                phase = GamePhase.LOBBY,
                currentPassIndex = 0,
                isCardRevealed = false,
                selectedVotePlayerId = null
            )
        }
    }

    fun startGameRound() {
        val state = _uiState.value
        if (state.words.size < 5 || state.players.size < 3) return

        // 1. Prevent Word Repetition:
        // Filter out words that have already been played across rounds.
        val unplayedWords = state.words.filter { it.id !in state.playedWordIds }
        val (poolToPickFrom, basePlayedIds) = if (unplayedWords.isNotEmpty()) {
            unplayedWords to state.playedWordIds
        } else {
            // If all words have been played, automatically reset played history and use full list
            state.words to emptySet<String>()
        }

        // Strict unbiased randomization using SecureRandom
        val randomWordIndex = secureRandom.nextInt(poolToPickFrom.size)
        val selectedWord = poolToPickFrom[randomWordIndex]
        val updatedPlayedIds = basePlayedIds + selectedWord.id

        // 2. Strict Impostor Selection Logic:
        // The randomly selected Impostor CANNOT be the author of the chosen Secret Word.
        // Dynamically filter out the author before picking the Impostor.
        val eligibleImpostors = if (selectedWord.authorPlayerId != null) {
            val nonAuthors = state.players.filter { it.id != selectedWord.authorPlayerId }
            if (nonAuthors.isNotEmpty()) nonAuthors else state.players
        } else {
            state.players
        }

        val randomImpostorIndex = secureRandom.nextInt(eligibleImpostors.size)
        val randomImpostor = eligibleImpostors[randomImpostorIndex]

        val needsAiData = state.showCategoryToImpostor || state.showAiHintToImpostor

        if (needsAiData) {
            _uiState.update { current ->
                current.copy(
                    isSettingUpRound = true,
                    currentSecretWord = selectedWord.word,
                    impostorPlayerId = randomImpostor.id,
                    playedWordIds = updatedPlayedIds,
                    aiCategory = null,
                    aiHint = null,
                    currentPassIndex = 0,
                    isCardRevealed = false,
                    selectedVotePlayerId = null
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                val result = GeminiHintService.fetchGeminiData(selectedWord.word)
                _uiState.update { current ->
                    current.copy(
                        isSettingUpRound = false,
                        phase = GamePhase.PASS_AND_PLAY,
                        aiCategory = result.category ?: "Secret Words",
                        aiHint = result.hint
                    )
                }
            }
        } else {
            _uiState.update { current ->
                current.copy(
                    phase = GamePhase.PASS_AND_PLAY,
                    isSettingUpRound = false,
                    currentSecretWord = selectedWord.word,
                    impostorPlayerId = randomImpostor.id,
                    playedWordIds = updatedPlayedIds,
                    aiCategory = null,
                    aiHint = null,
                    currentPassIndex = 0,
                    isCardRevealed = false,
                    selectedVotePlayerId = null
                )
            }
        }
    }

    fun revealCard() {
        _uiState.update { it.copy(isCardRevealed = true) }
    }

    fun nextPassPlayer() {
        val state = _uiState.value
        if (state.currentPassIndex < state.players.size - 1) {
            _uiState.update {
                it.copy(
                    currentPassIndex = it.currentPassIndex + 1,
                    isCardRevealed = false
                )
            }
        } else {
            // All players saw their roles -> move to discussion
            _uiState.update {
                it.copy(
                    phase = GamePhase.DISCUSSION,
                    isCardRevealed = false
                )
            }
        }
    }

    fun finishDiscussion() {
        _uiState.update {
            it.copy(
                phase = GamePhase.VOTING,
                selectedVotePlayerId = null
            )
        }
    }

    fun selectVotePlayer(playerId: Int) {
        _uiState.update { it.copy(selectedVotePlayerId = playerId) }
    }

    fun submitVote() {
        val state = _uiState.value
        val votedId = state.selectedVotePlayerId ?: return
        val impostorId = state.impostorPlayerId

        val impostor = state.players.firstOrNull { it.id == impostorId } ?: state.players.first()
        val votedPlayer = state.players.firstOrNull { it.id == votedId } ?: state.players.first()

        val civiliansWon = (votedId == impostorId)

        // Update scores:
        // If civilians won: each civilian gets +1 point, impostor gets 0
        // If impostor won: impostor gets +1 point, civilians get 0
        val updatedPlayers = state.players.map { player ->
            when {
                civiliansWon && player.id != impostorId -> player.copy(score = player.score + 1)
                !civiliansWon && player.id == impostorId -> player.copy(score = player.score + 1)
                else -> player
            }
        }

        val result = RoundResult(
            secretWord = state.currentSecretWord,
            impostor = impostor,
            votedPlayer = votedPlayer,
            civiliansWon = civiliansWon
        )

        _uiState.update {
            it.copy(
                phase = GamePhase.REVEAL,
                players = updatedPlayers,
                lastRoundResult = result
            )
        }
    }

    fun finishReveal() {
        _uiState.update {
            it.copy(
                phase = GamePhase.LOBBY,
                currentPassIndex = 0,
                isCardRevealed = false,
                selectedVotePlayerId = null
            )
        }
    }

    fun resetScores() {
        _uiState.update { current ->
            current.copy(
                players = current.players.map { it.copy(score = 0) }
            )
        }
    }
}
