package com.example.service

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class CachedWordCombination(
    val word: String,
    val category: String,
    val hint: String
)

object LocalWordCacheManager {
    private const val PREFS_NAME = "impostor_word_cache_prefs"
    private const val KEY_CACHE = "local_word_cache"

    // Seeded rich dictionary across all 12 requested categories
    private val SEED_WORDS: Map<String, List<Pair<String, String>>> = mapOf(
        "Animals & Nature" to listOf(
            "Elephant" to "Trunk",
            "Dolphin" to "Sonar",
            "Giraffe" to "Canopy",
            "Penguin" to "Tuxedo",
            "Kangaroo" to "Pouch",
            "Eagle" to "Altitude",
            "Chameleon" to "Camouflage",
            "Octopus" to "Tentacle",
            "Koala" to "Eucalyptus",
            "Tiger" to "Stripes",
            "Forest" to "Timber",
            "Waterfall" to "Cascade"
        ),
        "Books" to listOf(
            "Novel" to "Prologue",
            "Library" to "Silence",
            "Bookmark" to "Ribbon",
            "Chapter" to "Heading",
            "Spine" to "Binding",
            "Mystery" to "Alibi",
            "Poetry" to "Stanza",
            "Dictionary" to "Alphabet",
            "Author" to "Quill",
            "Biography" to "Legacy"
        ),
        "Countries & Cities" to listOf(
            "Paris" to "Eiffel",
            "Tokyo" to "Neon",
            "London" to "Big Ben",
            "Egypt" to "Pyramid",
            "Brazil" to "Carnival",
            "Canada" to "Maple",
            "Rome" to "Colosseum",
            "Sydney" to "Harbor",
            "Cairo" to "Nile",
            "Berlin" to "Wall"
        ),
        "Famous People" to listOf(
            "Einstein" to "Relativity",
            "Cleopatra" to "Pharaoh",
            "Shakespeare" to "Playwright",
            "Mozart" to "Symphony",
            "Da Vinci" to "Canvas",
            "Newton" to "Gravity",
            "Galileo" to "Telescope",
            "Beethoven" to "Orchestra"
        ),
        "Games & Leisure" to listOf(
            "Chess" to "Checkmate",
            "Bowling" to "Strike",
            "Puzzle" to "Jigsaw",
            "Origami" to "Fold",
            "Billiards" to "Cue",
            "Camping" to "Tent",
            "Fishing" to "Bait",
            "Dominoes" to "Tile"
        ),
        "Household" to listOf(
            "Blanket" to "Warmth",
            "Microwave" to "Timer",
            "Mirror" to "Reflection",
            "Toothbrush" to "Bristle",
            "Sofa" to "Cushion",
            "Flashlight" to "Beam",
            "Toaster" to "Crumb",
            "Clock" to "Pendulum",
            "Refrigerator" to "Chiller",
            "Shower" to "Droplets"
        ),
        "Movies & TV" to listOf(
            "Cinema" to "Ticket",
            "Popcorn" to "Butter",
            "Premiere" to "Red Carpet",
            "Director" to "Megaphone",
            "Oscar" to "Statuette",
            "Projector" to "Reel",
            "Screenplay" to "Script",
            "Trailer" to "Preview"
        ),
        "Music" to listOf(
            "Guitar" to "Fretboard",
            "Piano" to "Ivory",
            "Drums" to "Cymbal",
            "Violin" to "Bow",
            "Flute" to "Woodwind",
            "Trumpet" to "Brass",
            "Saxophone" to "Jazz",
            "Conductor" to "Baton"
        ),
        "Professions" to listOf(
            "Doctor" to "Stethoscope",
            "Astronaut" to "Spacesuit",
            "Firefighter" to "Hydrant",
            "Pilot" to "Cockpit",
            "Chef" to "Apron",
            "Detective" to "Magnifier",
            "Architect" to "Blueprint",
            "Surgeon" to "Scalpel"
        ),
        "Sports" to listOf(
            "Basketball" to "Hoop",
            "Soccer" to "Whistle",
            "Tennis" to "Racket",
            "Swimming" to "Goggles",
            "Archery" to "Target",
            "Volleyball" to "Net",
            "Baseball" to "Diamond",
            "Gymnastics" to "Vault"
        ),
        "Technology" to listOf(
            "Smartphone" to "Touchscreen",
            "Laptop" to "Keyboard",
            "Satellite" to "Orbit",
            "Robot" to "Circuit",
            "Battery" to "Voltage",
            "Microchip" to "Silicon",
            "Antenna" to "Signal",
            "Drone" to "Propeller"
        ),
        "Vehicles" to listOf(
            "Helicopter" to "Rotor",
            "Submarine" to "Periscope",
            "Bicycle" to "Pedal",
            "Train" to "Locomotive",
            "Scooter" to "Handlebar",
            "Airplane" to "Runway",
            "Rocket" to "Launchpad",
            "Sailboat" to "Mast"
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun saveWordCombination(context: Context, word: String, category: String, hint: String) {
        try {
            val prefs = getPrefs(context)
            val existingJson = prefs.getString(KEY_CACHE, "[]") ?: "[]"
            val array = JSONArray(existingJson)

            // Check if already in cache
            val cleanWord = word.trim()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                if (obj.optString("word", "").equals(cleanWord, ignoreCase = true)) {
                    return
                }
            }

            val newEntry = JSONObject().apply {
                put("word", cleanWord)
                put("category", category.trim())
                put("hint", hint.trim())
            }
            array.put(newEntry)
            prefs.edit().putString(KEY_CACHE, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getFallbackWords(
        context: Context,
        category: String,
        sessionUsedWords: List<String>,
        count: Int = 6
    ): List<String> {
        val usedSet = sessionUsedWords.map { it.trim().lowercase() }.toSet()
        val candidates = mutableListOf<String>()

        // 1. Check user-saved local cache
        try {
            val prefs = getPrefs(context)
            val cachedJson = prefs.getString(KEY_CACHE, "[]") ?: "[]"
            val array = JSONArray(cachedJson)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val cachedCat = obj.optString("category", "")
                val cachedWord = obj.optString("word", "")
                if (cachedWord.isNotBlank() && cachedWord.lowercase() !in usedSet) {
                    if (category.equals("Random", ignoreCase = true) || cachedCat.equals(category, ignoreCase = true)) {
                        candidates.add(cachedWord)
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Add from seed dictionary
        if (category.equals("Random", ignoreCase = true)) {
            SEED_WORDS.values.flatten().forEach { (word, _) ->
                if (word.lowercase() !in usedSet && !candidates.contains(word)) {
                    candidates.add(word)
                }
            }
        } else {
            SEED_WORDS[category]?.forEach { (word, _) ->
                if (word.lowercase() !in usedSet && !candidates.contains(word)) {
                    candidates.add(word)
                }
            }
            // If still not enough, look in all seed words
            if (candidates.size < count) {
                SEED_WORDS.values.flatten().forEach { (word, _) ->
                    if (word.lowercase() !in usedSet && !candidates.contains(word)) {
                        candidates.add(word)
                    }
                }
            }
        }

        candidates.shuffle()
        if (candidates.size >= count) {
            return candidates.take(count)
        }

        // Emergency fallback if all words have been used in this session:
        val allSeeds = (SEED_WORDS[category]?.map { it.first } ?: SEED_WORDS.values.flatten().map { it.first }).shuffled()
        return (candidates + allSeeds).distinct().take(count)
    }

    fun getFallbackHintAndCategory(context: Context?, word: String): Pair<String, String> {
        val clean = word.trim()
        val lower = clean.lowercase()

        // 1. Check cached entries
        if (context != null) {
            try {
                val prefs = getPrefs(context)
                val cachedJson = prefs.getString(KEY_CACHE, "[]") ?: "[]"
                val array = JSONArray(cachedJson)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    if (obj.optString("word", "").equals(clean, ignoreCase = true)) {
                        val cat = obj.optString("category", "")
                        val h = obj.optString("hint", "")
                        if (cat.isNotBlank() && h.isNotBlank()) {
                            return cat to h
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Check seed words
        for ((cat, list) in SEED_WORDS) {
            for ((w, hint) in list) {
                if (w.equals(clean, ignoreCase = true)) {
                    return cat to hint
                }
            }
        }

        // 3. Rich heuristic matching for custom words
        return when {
            lower in listOf(
                "samosa", "pizza", "burger", "taco", "sushi", "pasta", "croissant", "donut", "cake",
                "cookie", "sandwich", "apple", "banana", "chocolate", "ice cream", "pancake", "biryani",
                "curry", "noodles", "popcorn", "bread", "cheese", "dumpling", "ramen", "muffin"
            ) || lower.endsWith("cake") || lower.endsWith("pie") || lower.endsWith("soup") ->
                "Food & Dining" to "Flavor"

            lower in listOf(
                "dog", "cat", "elephant", "lion", "tiger", "bear", "penguin", "dolphin", "eagle",
                "monkey", "rabbit", "giraffe", "shark", "wolf", "fox", "deer", "panda", "koala",
                "zebra", "owl", "parrot", "whale", "turtle", "frog", "snake", "horse", "cow"
            ) || lower.endsWith("fish") || lower.endsWith("bird") ->
                "Animals & Nature" to "Wild"

            lower in listOf(
                "chair", "table", "lamp", "clock", "mirror", "sofa", "television", "fridge", "microwave",
                "bed", "pillow", "blanket", "curtain", "towel", "fork", "spoon", "plate", "cup",
                "kettle", "toaster", "candle", "shelf", "carpet", "wardrobe", "desk"
            ) ->
                "Household" to "Everyday"

            lower in listOf(
                "guitar", "piano", "drums", "violin", "flute", "trumpet", "saxophone", "harp",
                "microphone", "accordion", "cello", "clarinet", "banjo", "synth"
            ) ->
                "Music" to "Melody"

            lower in listOf(
                "doctor", "astronaut", "firefighter", "pilot", "chef", "detective", "artist",
                "teacher", "dentist", "nurse", "police", "soldier", "judge", "lawyer", "farmer",
                "scientist", "engineer", "mechanic", "carpenter", "actor", "photographer"
            ) ->
                "Professions" to "Career"

            lower in listOf(
                "soccer", "football", "basketball", "tennis", "cricket", "baseball", "rugby",
                "golf", "hockey", "volleyball", "swimming", "boxing", "surfing", "skiing",
                "badminton", "bowling", "cycling", "running"
            ) ->
                "Sports" to "Athletic"

            lower in listOf(
                "car", "bus", "train", "airplane", "subway", "bicycle", "motorcycle", "helicopter",
                "boat", "ship", "submarine", "scooter", "truck", "van", "rocket", "tractor"
            ) ->
                "Vehicles" to "Transit"

            lower in listOf(
                "phone", "smartphone", "computer", "laptop", "tablet", "robot", "camera", "drone",
                "radio", "satellite", "headphones", "printer", "battery", "chip", "internet"
            ) ->
                "Technology" to "Digital"

            lower in listOf(
                "paris", "london", "tokyo", "new york", "rome", "cairo", "sydney", "berlin",
                "delhi", "mumbai", "toronto", "dubai", "beijing", "moscow", "rio", "los angeles",
                "beach", "mountain", "desert", "island", "forest", "castle", "pyramid", "bridge"
            ) ->
                "Places & Travel" to "Destination"

            lower in listOf(
                "chess", "monopoly", "poker", "cards", "dominoes", "puzzle", "scrabble", "billiards",
                "dice", "darts", "arcade", "video game"
            ) ->
                "Games & Leisure" to "Play"

            else ->
                "Mystery Theme" to "Clue"
        }
    }
}
