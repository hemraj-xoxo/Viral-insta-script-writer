package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.SavedScript
import com.example.data.db.ScriptDao
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class ScriptTimelineItem(
    val timeRange: String,
    val text: String,
    val cameraAngle: String,
    val expression: String,
    val backgroundMusic: String,
    val soundEffect: String
)

@JsonClass(generateAdapter = true)
data class GeneratedScript(
    val title: String,
    val hook: String,
    val body: String,
    val cta: String,
    val visuals: String,
    val hashtags: String,
    val timeline: List<ScriptTimelineItem>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedIdea(
    val title: String,
    val angle: String,
    val hook: String,
    val visualConcept: String
)

@JsonClass(generateAdapter = true)
data class GeneratedIdeasList(
    val ideas: List<GeneratedIdea>
)

@JsonClass(generateAdapter = true)
data class AnalyzedPost(
    val title: String,
    val views: String,
    val likes: String,
    val styleDescription: String
)

@JsonClass(generateAdapter = true)
data class InstagramAnalysisResult(
    val username: String,
    val discoveredNiche: String,
    val audienceProfile: String,
    val topPosts: List<AnalyzedPost>,
    val contentStrategy: String,
    val tailoredScript: GeneratedScript
)

class ScriptRepository(
    private val scriptDao: ScriptDao,
    private val geminiService: com.example.data.service.GeminiService = com.example.data.service.GeminiService()
) {

    val savedScripts: Flow<List<SavedScript>> = scriptDao.getAllSavedScripts()

    // Generate script using Gemini API via the GeminiService service layer
    suspend fun generateScript(
        topic: String,
        niche: String,
        tone: String,
        durationSeconds: Int,
        language: String,
        targetAudience: String
    ): Result<GeneratedScript> = geminiService.generateScript(
        topic = topic,
        niche = niche,
        tone = tone,
        durationSeconds = durationSeconds,
        language = language,
        targetAudience = targetAudience
    )

    // Generate content ideas list via the GeminiService service layer
    suspend fun generateContentIdeas(
        niche: String,
        targetTopic: String,
        language: String
    ): Result<GeneratedIdeasList> = geminiService.generateContentIdeas(
        niche = niche,
        targetTopic = targetTopic,
        language = language
    )

    // Perform Instagram analysis and generate tailored script via the GeminiService service layer
    suspend fun analyzeInstagramAndGenerateScript(
        username: String,
        additionalContext: String,
        language: String
    ): Result<InstagramAnalysisResult> = geminiService.analyzeInstagramAndGenerateScript(
        username = username,
        additionalContext = additionalContext,
        language = language
    )

    // Save script locally
    suspend fun saveScript(savedScript: SavedScript): Long = withContext(Dispatchers.IO) {
        scriptDao.insertScript(savedScript)
    }

    // Delete script locally
    suspend fun deleteScript(id: Int) = withContext(Dispatchers.IO) {
        scriptDao.deleteScriptById(id)
    }
}
