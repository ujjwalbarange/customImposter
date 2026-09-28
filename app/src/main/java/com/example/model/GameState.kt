package com.example.model

enum class GamePhase {
    LOBBY,
    WORD_ENTRY,
    PASS_AND_PLAY,
    DISCUSSION,
    VOTING,
    REVEAL
}

data class Player(
    val id: Int,
    val name: String,
    val colorIndex: Int,
    val score: Int = 0
)

data class CustomWord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val word: String,
    val authorPlayerId: Int? = null
)

data class RoundResult(
    val secretWord: String,
    val impostor: Player,
    val votedPlayer: Player,
    val civiliansWon: Boolean
)

object WordPresets {
    val PARTY_PACK = listOf(
        "Blanket", "Popcorn", "Backpack", "Headphones", "Campfire",
        "Rollercoaster", "Watermelon", "Guitar", "Sunglasses", "Passport"
    )

    val HOUSEHOLD = listOf(
        "Blanket", "Microwave", "Mirror", "Toothbrush", "Sofa",
        "Flashlight", "Toaster", "Clock", "Refrigerator", "Shower"
    )

    val FOOD = listOf(
        "Pizza", "Ice Cream", "Sushi", "Pancake", "Chocolate",
        "Hamburger", "Taco", "Spaghetti", "Donut", "Avocado"
    )

    val ANIMALS = listOf(
        "Penguin", "Elephant", "Kangaroo", "Dolphin", "Giraffe",
        "Chameleon", "Gorilla", "Octopus", "Koala", "Tiger"
    )
}
