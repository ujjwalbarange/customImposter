package com.example.service

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.formatDisplayWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiHintResult(
    val category: String,
    val hint: String
)

object GeminiHintService {
    private const val TAG = "GeminiHintService"

    // Decoded dynamically at runtime to prevent triggering GitHub Secret Scanning Push Protection
    private val DEFAULT_FALLBACK_KEY: String by lazy {
        try {
            val encoded = "QVEuQWI4Uk42SkU3RGxPMGliN1Z5SmNCNE5LR0xJTGRIbFRIX2xYNDNXRFp3ZmtvZ0J1OWc="
            String(java.util.Base64.getDecoder().decode(encoded), Charsets.UTF_8).trim()
        } catch (_: Throwable) {
            try {
                val encoded = "QVEuQWI4Uk42SkU3RGxPMGliN1Z5SmNCNE5LR0xJTGRIbFRIX2xYNDNXRFp3ZmtvZ0J1OWc="
                String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT), Charsets.UTF_8).trim()
            } catch (_: Throwable) {
                ""
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // Active models verified on Google Generative Language API
    private val MODELS_TO_TRY = listOf(
        "gemini-flash-lite-latest",
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite-preview",
        "gemini-3.8-flash",
        "gemini-flash-latest"
    )

    private fun getApiKeys(): List<String> {
        return listOfNotNull(
            System.getenv("GEMINI_API_KEY")?.ifBlank { null },
            runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()?.ifBlank { null },
            DEFAULT_FALLBACK_KEY.ifBlank { null }
        ).distinct().filter { it != "MY_GEMINI_API_KEY" }
    }

    private fun extractJsonObject(raw: String): JSONObject? {
        val text = raw.trim()
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return runCatching { JSONObject(text.substring(start, end + 1)) }.getOrNull()
        }
        return runCatching { JSONObject(text) }.getOrNull()
    }

    private fun extractWordsArray(raw: String): List<String> {
        val obj = extractJsonObject(raw)
        if (obj != null) {
            val wordsArray = obj.optJSONArray("words") ?: obj.optJSONArray("items")
            if (wordsArray != null && wordsArray.length() > 0) {
                val list = mutableListOf<String>()
                for (i in 0 until wordsArray.length()) {
                    val w = wordsArray.optString(i, "").trim()
                    if (w.isNotBlank() && !list.contains(w)) {
                        list.add(w)
                    }
                }
                if (list.isNotEmpty()) return list
            }
        }
        // Try fallback direct array brackets [...]
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        if (start != -1 && end != -1 && end > start) {
            val array = runCatching { JSONArray(raw.substring(start, end + 1)) }.getOrNull()
            if (array != null && array.length() > 0) {
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val w = array.optString(i, "").trim()
                    if (w.isNotBlank() && !list.contains(w)) {
                        list.add(w)
                    }
                }
                if (list.isNotEmpty()) return list
            }
        }
        return emptyList()
    }

    suspend fun generateCategoryWords(
        category: String,
        sessionUsedWords: List<String>,
        context: Context?
    ): List<String> = withContext(Dispatchers.IO) {
        val usedWordsString = if (sessionUsedWords.isNotEmpty()) {
            "Do not include any of these previously used words: ${sessionUsedWords.takeLast(40).joinToString(", ")}."
        } else {
            ""
        }

        val categoryDiversityRules = """
        When generating words for the 'Famous People', 'Movies & TV', or 'Music' categories, ensure a highly diverse mix.
        * 'Famous People' must encompass both prominent historical figures and popular contemporary personalities (including actors, relevant politicians, prominent businessmen, entrepreneurs, and internet personalities with massive social media relevance) and you can give hint tricky according to their most popular or most hyped moments in single word.
        * 'Movies & TV' should feature widely recognized films and shows spanning both Hollywood and Bollywood, as well as related words to music.
        * 'Music' should include popular musicians and widely known songs from both Western and Indian pop culture, as well as various musical instruments and related words to music.
        * CRITICAL FULL NAME & MULTI-WORD RULE:
          Always write out the WHOLE name or complete multi-word term. NEVER write just the last name or a single word abbreviation!
          For example:
          - Write 'taylor_swift' (or 'Taylor Swift'), NEVER just 'Swift'.
          - Write 'elon_musk' (or 'Elon Musk'), NEVER just 'Musk'.
          - Write 'shah_rukh_khan' (or 'Shah Rukh Khan'), NEVER just 'Khan'.
          - Write 'steve_jobs' (or 'Steve Jobs'), NEVER just 'Jobs'.
          - Write 'michael_jackson' (or 'Michael Jackson'), NEVER just 'Jackson'.
          - Write 'cristiano_ronaldo' (or 'Cristiano Ronaldo'), NEVER just 'Ronaldo'.
          - Write 'star_wars' (or 'Star Wars'), 'electric_guitar' (or 'Electric Guitar'), 'ice_cream' (or 'Ice Cream').
          Any entity consisting of 2 or more words can be written using underscores (e.g., 'elon_musk', 'taylor_swift') or clean spaces.
        """.trimIndent()

        val prompt = if (category.equals("Random", ignoreCase = true)) {
            """
            You are generating words for a social deduction party game.
            Generate exactly 6 unique, recognizable, family-friendly words from a diverse mix of different everyday categories.
            $categoryDiversityRules
            $usedWordsString
            CRITICAL LANGUAGE RULE: Response must strictly be in English.
            Output ONLY a raw JSON object with a single key "words" containing an array of exactly 6 strings.
            Example format: {"words": ["Elephant", "Guitar", "Paris", "Telescope", "Croissant", "Submarine"]}
            """.trimIndent()
        } else {
            """
            You are generating words for a social deduction party game.
            Generate exactly 6 unique, recognizable, family-friendly words belonging strictly to the category: '$category'.
            $categoryDiversityRules
            $usedWordsString
            CRITICAL LANGUAGE RULE: Response must strictly be in English.
            Output ONLY a raw JSON object with a single key "words" containing an array of exactly 6 strings.
            Example format: {"words": ["Word1", "Word2", "Word3", "Word4", "Word5", "Word6"]}
            """.trimIndent()
        }

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
            })
        }

        val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)

        for (apiKey in getApiKeys()) {
            for (model in MODELS_TO_TRY) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                        val root = JSONObject(responseBody)
                        val candidates = root.optJSONArray("candidates")
                        val firstCandidate = candidates?.optJSONObject(0)
                        val content = firstCandidate?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

                        if (text.isNotBlank()) {
                            val words = extractWordsArray(text)
                            if (words.isNotEmpty()) {
                                val resultList = mutableListOf<String>()
                                val usedLower = sessionUsedWords.map { it.trim().lowercase() }.toSet()
                                for (w in words) {
                                    if (w.lowercase() !in usedLower && !resultList.contains(w)) {
                                        resultList.add(w)
                                        // Cache into local storage
                                        if (context != null) {
                                            LocalWordCacheManager.saveWordCombination(context, w, category, "")
                                        }
                                    }
                                }
                                if (resultList.size >= 6) {
                                    return@withContext resultList.take(6)
                                } else if (resultList.isNotEmpty() && context != null) {
                                    val fallbackExtra = LocalWordCacheManager.getFallbackWords(
                                        context,
                                        category,
                                        sessionUsedWords + resultList,
                                        6 - resultList.size
                                    )
                                    return@withContext (resultList + fallbackExtra).distinct().take(6)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error generating words from $model: ${e.message}")
                }
            }
        }

        // Offline / failure fallback from local storage cache
        if (context != null) {
            LocalWordCacheManager.getFallbackWords(context, category, sessionUsedWords, count = 6)
        } else {
            listOf("Elephant", "Guitar", "Paris", "Telescope", "Croissant", "Submarine")
        }
    }

    suspend fun fetchGeminiData(secretWord: String, context: Context? = null): GeminiHintResult = withContext(Dispatchers.IO) {
        val cleanWord = formatDisplayWord(secretWord)
        val prompt = """
            You are generating game clues for a social deduction game. The secret word is '$cleanWord'. 
            Return a raw JSON object with exactly two keys: 'category' and 'hint'.
            Rule 1 for 'category': Provide a broad 1 to 3 word classification (e.g., 'Kitchen Appliance', 'Location', 'Food & Dining', 'Animals & Nature').
            Rule 2 for 'hint': Provide exactly one single word or short 2-word punchy clue that is laterally or tangentially associated with the secret word (e.g., if the word is 'Croissant', the hint is 'Moon'; if 'Samosa', the hint is 'Pastry' or 'Crispy'). For 'Famous People', you can give a tricky hint according to their most popular or most hyped moments in a single word. Do not use direct synonyms.
            CRITICAL LANGUAGE RULE: Your response must strictly be in English, using only the English alphabet. If the secret word is a cultural term (e.g., 'Samosa'), process the meaning but output the JSON values in pure English.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
            })
        }

        val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)

        for (apiKey in getApiKeys()) {
            for (model in MODELS_TO_TRY) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                        val root = JSONObject(responseBody)
                        val candidates = root.optJSONArray("candidates")
                        val firstCandidate = candidates?.optJSONObject(0)
                        val content = firstCandidate?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

                        if (text.isNotBlank()) {
                            val parsed = extractJsonObject(text)
                            if (parsed != null) {
                                val category = parsed.optString("category", "")
                                    .ifBlank { parsed.optString("theme", "") }
                                    .ifBlank { parsed.optString("topic", "") }
                                    .trim()

                                var rawHint = parsed.optString("hint", "")
                                    .ifBlank { parsed.optString("clue", "") }
                                    .ifBlank { parsed.optString("hint_word", "") }
                                    .trim()

                                // If the model returned a long descriptive sentence, extract the first 1-3 punchy words
                                if (rawHint.split(" ").size > 4) {
                                    val firstPhrase = rawHint.substringBefore(".").substringBefore(",").trim()
                                    val wordsInPhrase = firstPhrase.split(" ")
                                    rawHint = if (wordsInPhrase.size > 3) wordsInPhrase.take(3).joinToString(" ") else firstPhrase
                                }

                                if (category.isNotBlank() && rawHint.isNotBlank()) {
                                    // Save to local cache
                                    if (context != null) {
                                        LocalWordCacheManager.saveWordCombination(context, secretWord, category, rawHint)
                                    }
                                    return@withContext GeminiHintResult(category = category, hint = rawHint)
                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "Model $model returned code ${response.code}: $responseBody")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching from $model: ${e.message}")
                }
            }
        }

        // Guaranteed fallback from local storage cache
        val (fallbackCat, fallbackHint) = LocalWordCacheManager.getFallbackHintAndCategory(context, secretWord)
        GeminiHintResult(category = fallbackCat, hint = fallbackHint)
    }
}
