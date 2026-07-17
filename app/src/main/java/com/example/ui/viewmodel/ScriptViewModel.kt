package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.SavedScript
import com.example.data.repository.GeneratedIdea
import com.example.data.repository.GeneratedScript
import com.example.data.repository.InstagramAnalysisResult
import com.example.data.repository.ScriptRepository
import com.example.data.repository.ScriptTimelineItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScriptViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ScriptRepository(db.scriptDao())

    val savedScripts: StateFlow<List<SavedScript>> = repository.savedScripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Script Generation State ---
    private val _isGeneratingScript = MutableStateFlow(false)
    val isGeneratingScript: StateFlow<Boolean> = _isGeneratingScript.asStateFlow()

    private val _generatedScript = MutableStateFlow<GeneratedScript?>(null)
    val generatedScript: StateFlow<GeneratedScript?> = _generatedScript.asStateFlow()

    private val _scriptError = MutableStateFlow<String?>(null)
    val scriptError: StateFlow<String?> = _scriptError.asStateFlow()

    // Script Input Parameters
    val scriptTopic = MutableStateFlow("")
    val scriptNiche = MutableStateFlow("Tech & Gadgets")
    val scriptTone = MutableStateFlow("Informative & Snappy")
    val scriptDuration = MutableStateFlow(30)
    val scriptLanguage = MutableStateFlow("English")
    val scriptAudience = MutableStateFlow("Young Professionals")

    // --- Instagram Profile Analyzer State ---
    private val _isAnalyzingInstagram = MutableStateFlow(false)
    val isAnalyzingInstagram: StateFlow<Boolean> = _isAnalyzingInstagram.asStateFlow()

    private val _instagramAnalysisResult = MutableStateFlow<InstagramAnalysisResult?>(null)
    val instagramAnalysisResult: StateFlow<InstagramAnalysisResult?> = _instagramAnalysisResult.asStateFlow()

    private val _instagramError = MutableStateFlow<String?>(null)
    val instagramError: StateFlow<String?> = _instagramError.asStateFlow()

    val instagramUsername = MutableStateFlow("")
    val instagramContext = MutableStateFlow("")

    // --- Ideas Generation State ---
    private val _isGeneratingIdeas = MutableStateFlow(false)
    val isGeneratingIdeas: StateFlow<Boolean> = _isGeneratingIdeas.asStateFlow()

    private val _generatedIdeas = MutableStateFlow<List<GeneratedIdea>?>(null)
    val generatedIdeas: StateFlow<List<GeneratedIdea>?> = _generatedIdeas.asStateFlow()

    private val _ideasError = MutableStateFlow<String?>(null)
    val ideasError: StateFlow<String?> = _ideasError.asStateFlow()

    // Ideas Input Parameters
    val ideasNiche = MutableStateFlow("AI & Automation")
    val ideasTopic = MutableStateFlow("Daily Life Hacks")
    val ideasLanguage = MutableStateFlow("English")

    // --- Active Tab State ---
    private val _activeTab = MutableStateFlow(0) // 0: Generate Script, 1: Generate Ideas, 2: Library, 3: Viral Secret Formulas
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _isHistoryExpanded = MutableStateFlow(false)
    val isHistoryExpanded: StateFlow<Boolean> = _isHistoryExpanded.asStateFlow()

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun setHistoryExpanded(expanded: Boolean) {
        _isHistoryExpanded.value = expanded
    }

    // Generate Script
    fun generateScript() {
        if (scriptTopic.value.isBlank()) {
            _scriptError.value = "Please enter a topic or keyword."
            return
        }

        viewModelScope.launch {
            _isGeneratingScript.value = true
            _scriptError.value = null
            _generatedScript.value = null

            val result = repository.generateScript(
                topic = topicValueCleaned(scriptTopic.value),
                niche = scriptNiche.value,
                tone = scriptTone.value,
                durationSeconds = scriptDuration.value,
                language = scriptLanguage.value,
                targetAudience = scriptAudience.value
            )

            result.fold(
                onSuccess = { script ->
                    _generatedScript.value = script
                    // Auto-save to History/Library (Equivalent to localStorage)
                    saveCurrentScript()
                },
                onFailure = { error ->
                    _scriptError.value = error.localizedMessage ?: "Failed to generate script. Try again."
                }
            )
            _isGeneratingScript.value = false
        }
    }

    private fun topicValueCleaned(topic: String): String {
        return topic.trim()
    }

    // Generate Content Ideas
    fun generateContentIdeas() {
        if (ideasTopic.value.isBlank()) {
            _ideasError.value = "Please enter a core theme or topic area."
            return
        }

        viewModelScope.launch {
            _isGeneratingIdeas.value = true
            _ideasError.value = null
            _generatedIdeas.value = null

            val result = repository.generateContentIdeas(
                niche = ideasNiche.value,
                targetTopic = ideasTopic.value,
                language = ideasLanguage.value
            )

            result.fold(
                onSuccess = { ideasList ->
                    _generatedIdeas.value = ideasList.ideas
                    // Auto-save to History/Library (Equivalent to localStorage)
                    saveCurrentIdeas()
                },
                onFailure = { error ->
                    _ideasError.value = error.localizedMessage ?: "Failed to generate ideas. Try again."
                }
            )
            _isGeneratingIdeas.value = false
        }
    }

    private fun serializeTimeline(timeline: List<ScriptTimelineItem>?): String {
        if (timeline == null) return ""
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter<List<ScriptTimelineItem>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, ScriptTimelineItem::class.java)
            )
            adapter.toJson(timeline)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun deserializeTimeline(json: String?): List<ScriptTimelineItem>? {
        if (json.isNullOrBlank()) return null
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter<List<ScriptTimelineItem>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, ScriptTimelineItem::class.java)
            )
            adapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Load script into active generator states
    fun loadScriptIntoGenerator(saved: SavedScript) {
        _generatedScript.value = GeneratedScript(
            title = saved.title,
            hook = saved.hook,
            body = saved.body,
            cta = saved.cta,
            visuals = saved.visuals,
            hashtags = saved.hashtags,
            timeline = deserializeTimeline(saved.timelineJson)
        )
        scriptTopic.value = saved.topic
        scriptNiche.value = saved.niche
        scriptLanguage.value = saved.language
        setActiveTab(0) // Switch to Script tab
    }

    // Load ideas list into active generator states
    @Suppress("UNCHECKED_CAST")
    fun loadIdeasIntoGenerator(saved: SavedScript) {
        if (saved.ideasJson.isBlank()) return
        try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listAdapter = moshi.adapter(List::class.java)
            val rawList = listAdapter.fromJson(saved.ideasJson) as? List<Map<String, String>> ?: return
            
            val mappedIdeas = rawList.map {
                GeneratedIdea(
                    title = it["title"] ?: "",
                    angle = it["angle"] ?: "",
                    hook = it["hook"] ?: "",
                    visualConcept = it["visualConcept"] ?: ""
                )
            }
            _generatedIdeas.value = mappedIdeas
            ideasTopic.value = saved.topic
            ideasNiche.value = saved.niche
            ideasLanguage.value = saved.language
            setActiveTab(1) // Switch to Ideas tab
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Save current generated script to Room local DB
    fun saveCurrentScript(customTitle: String? = null) {
        val currentScript = _generatedScript.value ?: return
        viewModelScope.launch {
            val scriptToSave = SavedScript(
                title = customTitle ?: currentScript.title,
                type = "SCRIPT",
                topic = scriptTopic.value,
                niche = scriptNiche.value,
                language = scriptLanguage.value,
                hook = currentScript.hook,
                body = currentScript.body,
                cta = currentScript.cta,
                visuals = currentScript.visuals,
                hashtags = currentScript.hashtags,
                timelineJson = serializeTimeline(currentScript.timeline)
            )
            repository.saveScript(scriptToSave)
        }
    }

    // Save a custom edited script to Room local DB
    fun saveCustomScript(
        title: String,
        hook: String,
        body: String,
        cta: String,
        visuals: String,
        hashtags: String
    ) {
        val activeTimeline = _generatedScript.value?.timeline
        viewModelScope.launch {
            val scriptToSave = SavedScript(
                title = title,
                type = "SCRIPT",
                topic = scriptTopic.value,
                niche = scriptNiche.value,
                language = scriptLanguage.value,
                hook = hook,
                body = body,
                cta = cta,
                visuals = visuals,
                hashtags = hashtags,
                timelineJson = serializeTimeline(activeTimeline)
            )
            repository.saveScript(scriptToSave)
        }
    }

    // Save a generated ideas set to Room local DB
    fun saveCurrentIdeas(customTitle: String? = null) {
        val currentIdeas = _generatedIdeas.value ?: return
        viewModelScope.launch {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listAdapter = moshi.adapter(List::class.java) // General raw map representation or serialization
            
            // Re-serialize ideas list so it can be saved in DB
            val ideasMapList = currentIdeas.map { 
                mapOf(
                    "title" to it.title,
                    "angle" to it.angle,
                    "hook" to it.hook,
                    "visualConcept" to it.visualConcept
                )
            }
            val jsonText = listAdapter.toJson(ideasMapList)

            val scriptToSave = SavedScript(
                title = customTitle ?: "Content Strategy: ${ideasTopic.value}",
                type = "IDEAS",
                topic = ideasTopic.value,
                niche = ideasNiche.value,
                language = ideasLanguage.value,
                ideasJson = jsonText
            )
            repository.saveScript(scriptToSave)
        }
    }

    // Delete saved script from Room local DB
    fun deleteSavedScript(id: Int) {
        viewModelScope.launch {
            repository.deleteScript(id)
        }
    }

    // Clear state messages
    fun clearScriptError() {
        _scriptError.value = null
    }

    fun clearIdeasError() {
        _ideasError.value = null
    }

    fun resetScriptResult() {
        _generatedScript.value = null
    }

    fun resetIdeasResult() {
        _generatedIdeas.value = null
    }

    // --- Instagram Profile Analyzer Method ---
    fun analyzeInstagramAccount() {
        val username = instagramUsername.value.trim()
        if (username.isBlank()) {
            _instagramError.value = "Please enter an Instagram username."
            return
        }
        val cleanUsername = if (username.startsWith("@")) username.substring(1) else username

        viewModelScope.launch {
            _isAnalyzingInstagram.value = true
            _instagramError.value = null
            _instagramAnalysisResult.value = null

            val result = repository.analyzeInstagramAndGenerateScript(
                username = cleanUsername,
                additionalContext = instagramContext.value,
                language = scriptLanguage.value
            )

            result.fold(
                onSuccess = { analysis ->
                    _instagramAnalysisResult.value = analysis
                    // Also populate the active generated script in the UI so the user can immediately play/listen, copy, save, or edit it!
                    _generatedScript.value = analysis.tailoredScript
                    // Update input parameters for consistency
                    scriptTopic.value = "Custom tailored Script for @$cleanUsername"
                    scriptNiche.value = analysis.discoveredNiche
                    scriptAudience.value = analysis.audienceProfile
                    
                    // Auto-save script to library/history (equivalent to localStorage)
                    saveCurrentScript(customTitle = "Tailored for @$cleanUsername: ${analysis.tailoredScript.title}")
                },
                onFailure = { error ->
                    _instagramError.value = error.localizedMessage ?: "Failed to analyze Instagram account. Try again."
                }
            )
            _isAnalyzingInstagram.value = false
        }
    }

    fun clearInstagramError() {
        _instagramError.value = null
    }

    fun resetInstagramResult() {
        _instagramAnalysisResult.value = null
    }
}

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScriptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScriptViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
