package com.example.data.service

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.repository.GeneratedScript
import com.example.data.repository.GeneratedIdeasList
import com.example.data.repository.InstagramAnalysisResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class GeminiService {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val scriptAdapter = moshi.adapter(GeneratedScript::class.java)
    private val ideasAdapter = moshi.adapter(GeneratedIdeasList::class.java)
    private val analysisAdapter = moshi.adapter(InstagramAnalysisResult::class.java)

    private fun getLanguagePromptText(language: String): String {
        return if (language.equals("Roman Telugu", ignoreCase = true)) {
            "Roman Telugu (also known as Telish or Telugu written using the English/Latin alphabet instead of Telugu script characters. CRITICAL: Use highly natural, colloquial, modern conversational words that people use in daily chats. For example: use 'eeroju', 'cheptunna', 'chala bagundi', 'ventane', 'emi jarigindante'. Use simple hybrid/English words naturally in English letters like 'save cheskondi', 'share cheyandi', 'follow avvandi', 'easy ga', 'try cheyandi', as modern Telugu reels creators do. Avoid overly formal or classical Telugu words!)"
        } else {
            language
        }
    }

    /**
     * Generates a high-converting, viral-optimized Instagram Reel script based on form inputs.
     */
    suspend fun generateScript(
        topic: String,
        niche: String,
        tone: String,
        durationSeconds: Int,
        language: String,
        targetAudience: String
    ): Result<GeneratedScript> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API Key is missing. Please set GEMINI_API_KEY in the Secrets panel."))
        }

        val languagePrompt = getLanguagePromptText(language)
        val prompt = """
            Create an extremely engaging, high-converting, and viral-optimized Instagram Reel/TikTok script.
            Topic: $topic
            Niche: $niche
            Tone/Style: $tone
            Target Duration: $durationSeconds seconds
            Language: $languagePrompt (IMPORTANT: You must write all output text, titles, hooks, bodies, visual instructions, CTAs, and hashtags in $languagePrompt!)
            Target Audience: $targetAudience

            To make the video go viral and maximize audience conversion, structure the script with:
            1. An extremely strong Hook (first 3 seconds) that acts as a pattern interrupt or psychological open loop.
            2. High-retention, snappy pacing for the Body script. Keep sentences short and conversational.
            3. A high-converting Call-to-Action (CTA) optimized to encourage saves, shares, comments, or profile visits.
            4. Detailed B-roll instructions and visual cues accompanying each section.
            5. Highly relevant viral hashtags for this topic.

            You MUST format your entire response as a valid JSON object matching this schema exactly:
            {
              "title": "A short, catchy title for the script",
              "hook": "The attention grabber (first 3 seconds of the video)",
              "body": "The main script content with natural spoken pacing, formatted with line breaks",
              "cta": "The call to action that boosts engagement (shares, saves, or comment triggers)",
              "visuals": "A description of B-roll footage, camera transitions, and text on screen",
              "hashtags": "5 high-volume viral hashtags related to this topic, starting with #",
              "timeline": [
                {
                  "timeRange": "0s - 3s",
                  "text": "What is spoken or done in this timing segment",
                  "cameraAngle": "Specific camera angle or movement (e.g., Extreme Close Up zooming in, Eye-level confidently speaking, dynamic cut-in, hand-held tracking)",
                  "expression": "Colloquial facial expression and body movement suggestions (e.g., Shocked look with raised eyebrows, knowing smirk with a wink, friendly pointing, dramatic head turn)",
                  "backgroundMusic": "Background music genre, tempo and vibe suggestions (e.g., Silence for tension, fast phonk beat, upbeat lo-fi, intense cinematic build-up)",
                  "soundEffect": "Actionable sound effects (SFX) to embed (e.g., SWOOSH transition, cash register ding, pop bubble, mouse click, record scratch, drum hit)"
                }
              ]
            }
        """.trimIndent()

        val systemInstructionText = """
            You are an elite Instagram growth marketer, copywriting expert, and viral content strategist. Your specialty is writing scripts that maximize retention, drive comments/shares, and optimize for the Instagram Reels algorithm.
            You must write all content in the requested language ($languagePrompt).
            You MUST return ONLY a valid, raw JSON object. Do not wrap the JSON in markdown code blocks like ```json ... ```. Just return the pure JSON starting with { and ending with }.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.85f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        var lastException: Exception? = null
        for (attempt in 1..3) {
            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI model on attempt $attempt.")

                val cleanedJson = cleanJsonString(jsonText)
                val parsed = scriptAdapter.fromJson(cleanedJson)
                    ?: throw Exception("Failed to parse JSON response from AI on attempt $attempt.")
                return@withContext Result.success(parsed)
            } catch (e: Exception) {
                lastException = e
                if (attempt < 3) {
                    delay(1000L * attempt) // Exponential backoff
                }
            }
        }
        Result.failure(lastException ?: Exception("Failed to generate script after 3 attempts due to a timeout or connection issue."))
    }

    /**
     * Generates 5 viral-optimized Instagram content ideas/angles.
     */
    suspend fun generateContentIdeas(
        niche: String,
        targetTopic: String,
        language: String
    ): Result<GeneratedIdeasList> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API Key is missing. Please set GEMINI_API_KEY in the Secrets panel."))
        }

        val languagePrompt = getLanguagePromptText(language)
        val prompt = """
            Generate exactly 5 viral-optimized Instagram content ideas/angles.
            Niche: $niche
            Topic area: $targetTopic
            Language: $languagePrompt (IMPORTANT: Write all fields inside the JSON in $languagePrompt)

            Ensure each idea is highly creative, unexpected, and optimized to trigger curiosity.
            
            You MUST format your entire response as a valid JSON object matching this schema exactly:
            {
              "ideas": [
                {
                  "title": "Title of the video concept",
                  "angle": "Why this goes viral (e.g., controversial, secret hack, relational humor)",
                  "hook": "The exact hook sentence to start the video",
                  "visualConcept": "Visual concept and B-roll/text overlays description"
                }
              ]
            }
        """.trimIndent()

        val systemInstructionText = """
            You are an elite creative director for Instagram influencers. Your goal is to brainstorm high-clickability video hooks and concepts.
            All responses must be strictly written in the requested language ($languagePrompt).
            You MUST return ONLY a valid, raw JSON object. Do not wrap the JSON in markdown code blocks. Just return the pure JSON starting with { and ending with }.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.9f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        var lastException: Exception? = null
        for (attempt in 1..3) {
            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI model on attempt $attempt.")

                val cleanedJson = cleanJsonString(jsonText)
                val parsed = ideasAdapter.fromJson(cleanedJson)
                    ?: throw Exception("Failed to parse JSON response from AI on attempt $attempt.")
                return@withContext Result.success(parsed)
            } catch (e: Exception) {
                lastException = e
                if (attempt < 3) {
                    delay(1000L * attempt) // Exponential backoff
                }
            }
        }
        Result.failure(lastException ?: Exception("Failed to generate ideas after 3 attempts due to a timeout or connection issue."))
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    /**
     * Performs semantic analysis of an Instagram account, determines its niche,
     * extracts its most popular content/post angles, and generates a tailored script.
     */
    suspend fun analyzeInstagramAndGenerateScript(
        username: String,
        additionalContext: String,
        language: String
    ): Result<InstagramAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API Key is missing. Please set GEMINI_API_KEY in the Secrets panel."))
        }

        val languagePrompt = getLanguagePromptText(language)
        val prompt = """
            Perform an elite, high-resolution strategic analysis of the public Instagram account: '$username'.
            Additional context provided: $additionalContext
            Language requirement: $languagePrompt (IMPORTANT: Write all outputs, titles, niches, strategies, post descriptions, scripts, hooks, and hashtags in $languagePrompt!)

            Your tasks:
            1. Discover the exact target Niche & Industry Category of this creator based on the username '$username' and any contextual clues.
            2. Extract/synthesize 3 highly realistic, top-performing posts (most viewed & liked post styles/angles) that align perfectly with this creator's handle and concept. Give them realistic view and like counts (e.g. 150K views, 12K likes), high-converting hook structures, and specific styles.
            3. Build a detailed, high-converting Target Audience Profile.
            4. Suggest a core Content Growth Strategy tailored to double their current engagement.
            5. Write a brand new, custom, ultra-viral Instagram Reel script ("tailoredScript") based directly on their most successful post styles to keep the momentum going. This script must include a title, hook, body, cta, visuals, and hashtags.

            You MUST format your entire response as a single valid JSON object matching this schema exactly:
            {
              "username": "$username",
              "discoveredNiche": "Discovered niche name",
              "audienceProfile": "Target audience persona description",
              "topPosts": [
                {
                  "title": "Topic or title of the top post",
                  "views": "Estimated or benchmark views (e.g. 240K)",
                  "likes": "Estimated or benchmark likes (e.g. 18.5K)",
                  "styleDescription": "Why this post performed so well (the visual style, audio, pacing)"
                }
              ],
              "contentStrategy": "Detailed recommendations to boost virality further",
              "tailoredScript": {
                "title": "A short, catchy title for the script",
                "hook": "The attention grabber (first 3 seconds)",
                "body": "The main script content with snappiest spoken pacing, formatted with line breaks",
                "cta": "Engagement trigger CTA (save/share trigger)",
                "visuals": "B-roll, camera movements, text overlay instructions",
                "hashtags": "5 viral hashtags starting with #",
                "timeline": [
                  {
                    "timeRange": "0s - 3s",
                    "text": "What is spoken or done in this timing segment",
                    "cameraAngle": "Specific camera angle or movement (e.g., Close-up zooming in, Eye-level confidently speaking, dynamic cut, hand-held pan)",
                    "expression": "Colloquial facial expression and body movement suggestions (e.g., Shocked look with raised eyebrows, knowing smirk, enthusiastic pointing)",
                    "backgroundMusic": "Background music genre, tempo and vibe suggestions (e.g., Fast phonk beat, upbeat lo-fi, intense cinematic build-up)",
                    "soundEffect": "Actionable sound effects to embed (e.g., SWOOSH transition, cash register ding, pop bubble, mouse click)"
                  }
                ]
              }
            }
        """.trimIndent()

        val systemInstructionText = """
            You are a world-class social media growth hacking tool and Instagram profile analyst. You analyze usernames to discover their content footprint, niche category, top visual formats, and engagement drivers.
            You must write all content in the requested language ($languagePrompt).
            You MUST return ONLY a valid, raw JSON object. Do not wrap the JSON in markdown code blocks. Just return pure JSON starting with { and ending with }.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.85f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        var lastException: Exception? = null
        for (attempt in 1..3) {
            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI model on attempt $attempt.")

                val cleanedJson = cleanJsonString(jsonText)
                val parsed = analysisAdapter.fromJson(cleanedJson)
                    ?: throw Exception("Failed to parse Instagram analysis JSON response from AI on attempt $attempt.")
                return@withContext Result.success(parsed)
            } catch (e: Exception) {
                lastException = e
                if (attempt < 3) {
                    delay(1000L * attempt) // Exponential backoff
                }
            }
        }
        Result.failure(lastException ?: Exception("Failed to perform Instagram profile analysis after 3 attempts due to a timeout or connection issue."))
    }
}
