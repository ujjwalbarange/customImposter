package com.example

import com.example.model.GamePhase
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun testInitialLobbyState() {
        val state = viewModel.uiState.value
        assertEquals(GamePhase.LOBBY, state.phase)
        assertEquals(4, state.players.size)
        assertTrue(state.words.size >= 5)
    }

    @Test
    fun testAddAndRemovePlayer() {
        viewModel.addPlayer()
        assertEquals(5, viewModel.uiState.value.players.size)

        // Try removing
        val playerToRemove = viewModel.uiState.value.players.last()
        viewModel.removePlayer(playerToRemove.id)
        assertEquals(4, viewModel.uiState.value.players.size)

        // Cannot drop below 3
        val p4 = viewModel.uiState.value.players[3]
        viewModel.removePlayer(p4.id)
        assertEquals(3, viewModel.uiState.value.players.size)
        val p3 = viewModel.uiState.value.players[2]
        viewModel.removePlayer(p3.id)
        // Remains 3
        assertEquals(3, viewModel.uiState.value.players.size)
    }

    @Test
    fun testRenamePlayer() {
        val firstPlayer = viewModel.uiState.value.players[0]
        viewModel.renamePlayer(firstPlayer.id, "Sherlock")
        assertEquals("Sherlock", viewModel.uiState.value.players[0].name)
    }

    @Test
    fun testWordAttributionAndImpostorExclusion() {
        viewModel.goToWordEntry()
        viewModel.clearAllWords()

        val authorId = 2 // Player 2
        // Add 5 words all authored by Player 2
        viewModel.addWord("Guitar", authorPlayerId = authorId)
        viewModel.addWord("Piano", authorPlayerId = authorId)
        viewModel.addWord("Drums", authorPlayerId = authorId)
        viewModel.addWord("Violin", authorPlayerId = authorId)
        viewModel.addWord("Flute", authorPlayerId = authorId)

        // Run multiple rounds to ensure Player 2 is NEVER the impostor
        for (round in 1..20) {
            viewModel.startGameRound()
            val state = viewModel.uiState.value
            // The author must NEVER be the impostor
            assertTrue("Player 2 is the author and should never be the impostor", state.impostorPlayerId != authorId)
            assertTrue("Impostor should be one of the other players", state.players.any { it.id == state.impostorPlayerId && it.id != authorId })
            viewModel.returnToLobby()
            viewModel.goToWordEntry()
        }
    }

    @Test
    fun testAnonymousWordAllowsAnyImpostor() {
        viewModel.goToWordEntry()
        viewModel.clearAllWords()

        // Add 5 anonymous words
        for (i in 1..5) {
            viewModel.addWord("AnonWord$i", authorPlayerId = null)
        }

        val chosenImpostors = mutableSetOf<Int>()
        for (round in 1..40) {
            viewModel.startGameRound()
            chosenImpostors.add(viewModel.uiState.value.impostorPlayerId)
            viewModel.returnToLobby()
            viewModel.goToWordEntry()
        }
        // Over 40 rounds among 4 players, multiple players should have been selected
        assertTrue(chosenImpostors.size > 1)
    }

    @Test
    fun testWordPersistenceAcrossRounds() {
        viewModel.goToWordEntry()
        viewModel.clearAllWords()

        viewModel.addWord("Secret1")
        viewModel.addWord("Secret2")
        viewModel.addWord("Secret3")
        viewModel.addWord("Secret4")
        viewModel.addWord("Secret5")

        assertEquals(5, viewModel.uiState.value.words.size)

        // Play round
        viewModel.startGameRound()
        assertEquals(GamePhase.PASS_AND_PLAY, viewModel.uiState.value.phase)
        assertEquals(5, viewModel.uiState.value.words.size) // Persists!

        // Complete pass and play to discussion -> voting -> reveal
        for (i in 0 until viewModel.uiState.value.players.size) {
            viewModel.nextPassPlayer()
        }
        viewModel.finishDiscussion()
        viewModel.selectVotePlayer(viewModel.uiState.value.players[0].id)
        viewModel.submitVote()
        assertEquals(GamePhase.REVEAL, viewModel.uiState.value.phase)
        assertEquals(5, viewModel.uiState.value.words.size) // Persists!

        // Finish reveal back to lobby
        viewModel.finishReveal()
        assertEquals(GamePhase.LOBBY, viewModel.uiState.value.phase)
        assertEquals(5, viewModel.uiState.value.words.size) // Persists!

        // Back to word entry
        viewModel.goToWordEntry()
        assertEquals(GamePhase.WORD_ENTRY, viewModel.uiState.value.phase)
        assertEquals(5, viewModel.uiState.value.words.size) // Still persists!
    }

    @Test
    fun testPreventWordRepetitionAcrossRounds() {
        viewModel.goToWordEntry()
        viewModel.clearAllWords()

        val customWords = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry")
        customWords.forEach { viewModel.addWord(it) }

        val playedWords = mutableListOf<String>()

        // Play 5 rounds
        for (i in 1..5) {
            viewModel.startGameRound()
            val chosenWord = viewModel.uiState.value.currentSecretWord
            assertFalse("Word '$chosenWord' should not repeat within the 5 unique words cycle", playedWords.contains(chosenWord))
            playedWords.add(chosenWord)
            viewModel.returnToLobby()
            viewModel.goToWordEntry()
        }

        // All 5 words were played exactly once
        assertEquals(5, playedWords.toSet().size)

        // 6th round should automatically reset history and pick again from the full list
        viewModel.startGameRound()
        val sixthWord = viewModel.uiState.value.currentSecretWord
        assertTrue(customWords.contains(sixthWord))
        // playedWordIds should now contain 1 item (the reset cycle restarted)
        assertEquals(1, viewModel.uiState.value.playedWordIds.size)
    }

    @Test
    fun testClearAllWordsEmptiesListAndResetsPlayedHistory() {
        viewModel.goToWordEntry()
        viewModel.clearAllWords()
        assertEquals(0, viewModel.uiState.value.words.size)
        assertEquals(0, viewModel.uiState.value.playedWordIds.size)

        viewModel.quickFillPack(listOf("Cat", "Dog", "Fish", "Bird", "Mouse"))
        assertEquals(5, viewModel.uiState.value.words.size)

        viewModel.startGameRound()
        assertEquals(1, viewModel.uiState.value.playedWordIds.size)

        viewModel.returnToLobby()
        viewModel.goToWordEntry()
        viewModel.clearAllWords()

        assertEquals(0, viewModel.uiState.value.words.size)
        assertEquals(0, viewModel.uiState.value.playedWordIds.size)
    }

    @Test
    fun testWordEntryAndGameFlow() {
        viewModel.goToWordEntry()
        assertEquals(GamePhase.WORD_ENTRY, viewModel.uiState.value.phase)

        viewModel.addWord("Telescope")
        assertTrue(viewModel.uiState.value.words.any { it.word == "Telescope" })

        // Start game round
        viewModel.startGameRound()
        val inGame = viewModel.uiState.value
        assertEquals(GamePhase.PASS_AND_PLAY, inGame.phase)
        assertTrue(inGame.currentSecretWord.isNotBlank())
        assertTrue(inGame.players.any { it.id == inGame.impostorPlayerId })

        // Reveal card
        viewModel.revealCard()
        assertTrue(viewModel.uiState.value.isCardRevealed)

        // Pass to next players until discussion
        val playerCount = inGame.players.size
        for (i in 0 until playerCount) {
            viewModel.nextPassPlayer()
        }
        assertEquals(GamePhase.DISCUSSION, viewModel.uiState.value.phase)

        // Finish discussion -> voting
        viewModel.finishDiscussion()
        assertEquals(GamePhase.VOTING, viewModel.uiState.value.phase)

        // Vote for the impostor
        val impostorId = viewModel.uiState.value.impostorPlayerId
        viewModel.selectVotePlayer(impostorId)
        viewModel.submitVote()

        val revealState = viewModel.uiState.value
        assertEquals(GamePhase.REVEAL, revealState.phase)
        assertNotNull(revealState.lastRoundResult)
        assertTrue(revealState.lastRoundResult!!.civiliansWon)

        // Civilians should have score 1, Impostor should have score 0
        val impostor = revealState.players.first { it.id == impostorId }
        val civilian = revealState.players.first { it.id != impostorId }
        assertEquals(0, impostor.score)
        assertEquals(1, civilian.score)

        // Return to lobby
        viewModel.finishReveal()
        assertEquals(GamePhase.LOBBY, viewModel.uiState.value.phase)
    }
}
