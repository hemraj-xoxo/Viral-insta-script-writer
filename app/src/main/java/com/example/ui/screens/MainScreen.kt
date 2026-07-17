package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.data.db.SavedScript
import com.example.data.repository.GeneratedIdea
import com.example.data.repository.GeneratedScript
import com.example.data.repository.ScriptTimelineItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScriptViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScriptViewModel) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val savedScripts by viewModel.savedScripts.collectAsStateWithLifecycle()
    val isHistoryExpanded by viewModel.isHistoryExpanded.collectAsStateWithLifecycle()

    // Key configuration check
    val apiKey = BuildConfig.GEMINI_API_KEY
    val isApiKeyConfigured = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    // Main Header Row with Bento Grid theme aesthetic (Light, pastel purple and dark typography)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateBg)
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "POWERED BY AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = InstaPink, // #65558F
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Viral Creator",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary, // #1D1B20
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // API Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isApiKeyConfigured) AccentGreen.copy(alpha = 0.12f)
                                        else Color(0xFFFCAF45).copy(alpha = 0.12f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isApiKeyConfigured) AccentGreen else Color(0xFFFCAF45),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isApiKeyConfigured) AccentGreen else Color(0xFFFCAF45))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isApiKeyConfigured) "AI Active" else "Needs Key",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isApiKeyConfigured) AccentGreen else Color(0xFFFCAF45)
                                    )
                                }
                            }

                            // Elegant Bento History Sidebar Toggle Button
                            IconButton(
                                onClick = { viewModel.setHistoryExpanded(!isHistoryExpanded) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(BentoContainerHot, CircleShape) // Light pastel lavender background
                                    .testTag("history_sidebar_toggle_button")
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = "History Sidebar",
                                    tint = BentoDeepViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Elegant User Profile Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BentoContainerHot) // #EADDFF
                                    .clickable {
                                        Toast.makeText(context, "Viral Creator Account Screen", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Profile icon",
                                    tint = BentoDeepViolet, // #21005D
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // If API Key is missing, show a beautiful dismissal alert
                    if (!isApiKeyConfigured) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE5A93C).copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "API Key not configured. Please set GEMINI_API_KEY in the AI Studio Secrets panel.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFD180)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Standard M3 Bottom Navigation
                NavigationBar(
                    containerColor = SlateSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(
                                if (activeTab == 0) Icons.Default.Description else Icons.Outlined.Description,
                                contentDescription = "Script icon"
                            )
                        },
                        label = { Text("Script", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = InstaPink,
                            indicatorColor = InstaPink.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_script_tab")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            Icon(
                                if (activeTab == 1) Icons.Default.Lightbulb else Icons.Outlined.Lightbulb,
                                contentDescription = "Ideas icon"
                            )
                        },
                        label = { Text("Ideas", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = InstaPink,
                            indicatorColor = InstaPink.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_ideas_tab")
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            Icon(
                                if (activeTab == 2) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Library icon"
                            )
                        },
                        label = { Text("Library", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = InstaPink,
                            indicatorColor = InstaPink.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_library_tab")
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = {
                            Icon(
                                if (activeTab == 3) Icons.Default.Analytics else Icons.Outlined.Analytics,
                                contentDescription = "Formula icon"
                            )
                        },
                        label = { Text("Hub", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = InstaPink,
                            indicatorColor = InstaPink.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_formula_tab")
                    )
                }
            },
            containerColor = SlateBg
        ) { innerPadding ->
            // Render content based on selected tab with smooth cross-fade animation
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier.padding(innerPadding),
                label = "tab_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> ScriptGeneratorTab(viewModel)
                    1 -> ContentIdeasTab(viewModel)
                    2 -> SavedLibraryTab(viewModel, savedScripts)
                    3 -> ViralityHubTab()
                }
            }
        }

        // Beautiful Bento-style History Sidebar/Drawer
        AnimatedVisibility(
            visible = isHistoryExpanded,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .zIndex(10f)
        ) {
            BentoHistorySidebar(
                viewModel = viewModel,
                savedScripts = savedScripts,
                onDismiss = { viewModel.setHistoryExpanded(false) }
            )
        }
    }
}

// --- TAB 1: SCRIPT GENERATOR ---
@Composable
fun ScriptGeneratorTab(viewModel: ScriptViewModel) {
    val context = LocalContext.current

    val isGenerating by viewModel.isGeneratingScript.collectAsStateWithLifecycle()
    val isAnalyzingInstagram by viewModel.isAnalyzingInstagram.collectAsStateWithLifecycle()
    val scriptResult by viewModel.generatedScript.collectAsStateWithLifecycle()
    val errorMsg by viewModel.scriptError.collectAsStateWithLifecycle()
    val instagramError by viewModel.instagramError.collectAsStateWithLifecycle()

    val topic by viewModel.scriptTopic.collectAsStateWithLifecycle()
    val selectedNiche by viewModel.scriptNiche.collectAsStateWithLifecycle()
    val selectedTone by viewModel.scriptTone.collectAsStateWithLifecycle()
    val selectedDuration by viewModel.scriptDuration.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.scriptLanguage.collectAsStateWithLifecycle()
    val audience by viewModel.scriptAudience.collectAsStateWithLifecycle()

    val instagramUsername by viewModel.instagramUsername.collectAsStateWithLifecycle()
    val instagramContext by viewModel.instagramContext.collectAsStateWithLifecycle()

    var inputMode by remember { mutableStateOf("TOPIC") } // "TOPIC" or "INSTAGRAM"

    // Predefined popular niches and tones
    val niches = listOf(
        "Tech & Gadgets", "AI & Software", "Comedy & Memes", "Business & Startups",
        "Storytelling & History", "Finance & Crypto", "Self Improvement", "Fitness & Diet", "Fashion & Lifestyle"
    )
    val tones = listOf(
        "Informative & Snappy", "Funny & Sarcastic", "Dramatic & Suspenseful",
        "Inspiring & Motivational", "Curiosity Driven", "Direct & Bold", "Chill & Narrative"
    )
    val languages = listOf(
        "English", "Spanish", "Hindi", "French", "German", "Japanese", "Portuguese", "Arabic", "Italian", "Russian", "Roman Telugu"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Hero Image banner loaded from drawable (generated asset)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BentoContainerHot),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, SlateCardSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Light background Bolt Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = "Bolt Icon",
                                tint = BentoDeepViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // HOT NOW Capsule
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(BentoDeepViolet)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "HOT NOW",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "The \"Tech Secret\" Hook",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepViolet,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = "Viral script for tech storytelling. 85% virality forecast.",
                        fontSize = 13.sp,
                        color = BentoTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Only show input form if no script is generated yet OR we want to edit/regenerate
        if (scriptResult == null && !isGenerating && !isAnalyzingInstagram) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, SlateCardSecondary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        
                        // Input Mode Selector Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateBg)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "TOPIC" to "Topic Mode",
                                "INSTAGRAM" to "Instagram Scan"
                            ).forEach { (mode, label) ->
                                val isSel = inputMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) SlateCardSecondary else Color.Transparent)
                                        .clickable { inputMode = mode }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (mode == "TOPIC") Icons.Default.Edit else Icons.Default.AlternateEmail,
                                            contentDescription = null,
                                            tint = if (isSel) InstaPink else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (inputMode == "TOPIC") {
                            // --- TOPIC MODE FORM ---
                            Text(
                                text = "Video Topic or Key Idea",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = topic,
                                onValueChange = { viewModel.scriptTopic.value = it },
                                placeholder = { Text("What is your video about? e.g. 3 AI tools that feel illegal to know", color = TextSecondary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("script_topic_input"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateBg,
                                    unfocusedContainerColor = SlateBg,
                                    focusedIndicatorColor = InstaPink,
                                    unfocusedIndicatorColor = SlateCardSecondary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Niche Selector (Dropdown Component)
                            Text(
                                text = "Select Content Niche",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            var nicheExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { nicheExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SlateCardSecondary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("niche_dropdown_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Category,
                                                contentDescription = "Category Icon",
                                                tint = InstaPink,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = selectedNiche,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown icon",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = nicheExpanded,
                                    onDismissRequest = { nicheExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .background(SlateSurface)
                                ) {
                                    niches.forEach { item ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Category,
                                                        contentDescription = null,
                                                        tint = if (selectedNiche == item) InstaPink else TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item,
                                                        color = TextPrimary,
                                                        fontWeight = if (selectedNiche == item) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.scriptNiche.value = item
                                                nicheExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tone selection row
                            Text(
                                text = "Select Video Tone",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(tones) { item ->
                                    FilterChip(
                                        selected = selectedTone == item,
                                        onClick = { viewModel.scriptTone.value = item },
                                        label = { Text(item, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = InstaPurple,
                                            selectedLabelColor = Color.White,
                                            containerColor = SlateBg,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = SlateCardSecondary,
                                            selectedBorderColor = InstaPurple,
                                            enabled = true,
                                            selected = selectedTone == item
                                        )
                                    )
                                }
                            }
                        } else {
                            // --- INSTAGRAM AUDIT & TAILOR MODE FORM ---
                            Text(
                                text = "Instagram Username",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = instagramUsername,
                                onValueChange = { viewModel.instagramUsername.value = it },
                                placeholder = { Text("e.g. zuck or leomessi", color = TextSecondary) },
                                leadingIcon = {
                                    Icon(Icons.Default.AlternateEmail, contentDescription = "Username symbol", tint = InstaPink, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("instagram_username_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateBg,
                                    unfocusedContainerColor = SlateBg,
                                    focusedIndicatorColor = InstaPink,
                                    unfocusedIndicatorColor = SlateCardSecondary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Additional Channel Context (Optional)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = instagramContext,
                                onValueChange = { viewModel.instagramContext.value = it },
                                placeholder = { Text("e.g. focused on software engineering tutorials or cooking healthy desserts", color = TextSecondary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("instagram_context_input"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateBg,
                                    unfocusedContainerColor = SlateBg,
                                    focusedIndicatorColor = InstaPink,
                                    unfocusedIndicatorColor = SlateCardSecondary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Language Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Language",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    Button(
                                        onClick = { expanded = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateBg),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(selectedLanguage, color = TextPrimary, fontSize = 13.sp)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = TextSecondary)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(SlateSurface)
                                    ) {
                                        languages.forEach { lang ->
                                            DropdownMenuItem(
                                                text = { Text(lang, color = TextPrimary) },
                                                onClick = {
                                                    viewModel.scriptLanguage.value = lang
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Duration selection
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Target Duration",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(15, 30, 60).forEach { dur ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (selectedDuration == dur) InstaOrange else SlateBg)
                                                .clickable { viewModel.scriptDuration.value = dur }
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${dur}s",
                                                color = if (selectedDuration == dur) Color.White else TextSecondary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (inputMode == "TOPIC") {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Target Audience field
                            Text(
                                text = "Target Audience (Optional)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = audience,
                                onValueChange = { viewModel.scriptAudience.value = it },
                                placeholder = { Text("e.g. Aspiring student developers, creators", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateBg,
                                    unfocusedContainerColor = SlateBg,
                                    focusedIndicatorColor = InstaPink,
                                    unfocusedIndicatorColor = SlateCardSecondary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Generate Button with glowing Instagram gradient
                        Button(
                            onClick = {
                                if (inputMode == "TOPIC") {
                                    viewModel.generateScript()
                                } else {
                                    viewModel.analyzeInstagramAccount()
                                }
                            },
                            enabled = !isGenerating && !isAnalyzingInstagram,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_script_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(InstaPink, InstaPurple, InstaOrange)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGenerating || isAnalyzingInstagram) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (inputMode == "TOPIC") Icons.Default.AutoAwesome else Icons.Default.QueryStats,
                                            contentDescription = "Spark",
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (inputMode == "TOPIC") "Generate Viral Script" else "Audit Account & Tailor",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Render Generation Error State
        if (errorMsg != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFE57373))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generation Error", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg!!, color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.clearScriptError() },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dismiss", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Render Instagram Error State
        if (instagramError != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFE57373))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Instagram Audit Error", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(instagramError!!, color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.clearInstagramError() },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dismiss", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Render Loading State Placeholder
        if (isGenerating) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = InstaPink)
                        Text(
                            text = "AI is brainstorming viral Hooks...",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Analyzing audience pattern interrupts, pacing, and CTAs in $selectedLanguage",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Render Instagram Audit Loading State
        if (isAnalyzingInstagram) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = InstaPink)
                        Text(
                            text = "Auditing public Instagram metrics...",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Analyzing niche popularity, extracting high-engagement views/likes patterns, and tailoring viral hooks in $selectedLanguage...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Render Generated Script Result
        if (scriptResult != null && !isGenerating) {
            item {
                RenderGeneratedScriptCard(scriptResult!!, viewModel, context)
            }
        }
    }
}

@Composable
fun TtsScriptPlayer(
    hook: String,
    body: String,
    cta: String,
    language: String
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(1.0f) }
    
    // Selection state: "ALL", "HOOK", "BODY", "CTA"
    var selectedSection by remember { mutableStateOf("ALL") }

    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Initialize TTS
    DisposableEffect(context) {
        val obj = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsInstance = obj
        onDispose {
            obj.stop()
            obj.shutdown()
        }
    }

    // Set voice speed whenever speed changes or when tts becomes ready
    LaunchedEffect(speed, ttsInstance, isTtsReady, isPlaying) {
        if (isTtsReady) {
            ttsInstance?.setSpeechRate(speed)
        }
    }

    // Equalizer animation heights
    val barCount = 12
    val barHeights = remember { mutableStateListOf<Float>().apply { addAll(List(barCount) { 4f }) } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                for (i in 0 until barCount) {
                    barHeights[i] = (10..40).random().toFloat()
                }
                kotlinx.coroutines.delay(100)
            }
        } else {
            for (i in 0 until barCount) {
                barHeights[i] = 4f
            }
        }
    }

    // Helper to trigger speaking
    fun startSpeaking() {
        if (!isTtsReady || ttsInstance == null) {
            Toast.makeText(context, "Text-to-Speech engine is preparing...", Toast.LENGTH_SHORT).show()
            return
        }

        val textToSpeak = when (selectedSection) {
            "HOOK" -> hook
            "BODY" -> body
            "CTA" -> cta
            else -> "Title. ${hook}. Script Body. ${body}. Call to Action. ${cta}"
        }

        // Set Language
        val locale = when (language.trim().lowercase()) {
            "spanish" -> Locale("es", "ES")
            "hindi" -> Locale("hi", "IN")
            "french" -> Locale.FRENCH
            "german" -> Locale.GERMAN
            "japanese" -> Locale.JAPANESE
            "portuguese" -> Locale("pt", "PT")
            "arabic" -> Locale("ar")
            "italian" -> Locale.ITALIAN
            "russian" -> Locale("ru", "RU")
            else -> Locale.ENGLISH
        }
        
        val langResult = ttsInstance?.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to default
            ttsInstance?.setLanguage(Locale.ENGLISH)
        }

        ttsInstance?.setSpeechRate(speed)

        // Set complete listener to reset play state when reading finishes
        ttsInstance?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                isPlaying = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isPlaying = false
            }
        })

        // Speak
        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "reels_tts")
        }
        ttsInstance?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "reels_tts")
        isPlaying = true
    }

    fun stopSpeaking() {
        ttsInstance?.stop()
        isPlaying = false
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SlateCardSecondary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("tts_audio_player_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Player Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Voice Reader",
                        tint = InstaPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Voice Script Reader (TTS)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Sound Wave/Equalizer Graphic when playing
                Row(
                    modifier = Modifier.height(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (i in 0 until barCount) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(barHeights[i].dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(if (isPlaying) InstaPink else TextSecondary.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector tabs: HOOK / BODY / CTA / FULL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "Whole Script",
                    "HOOK" to "Hook Only",
                    "BODY" to "Body",
                    "CTA" to "CTA Only"
                ).forEach { (code, label) ->
                    val isSel = selectedSection == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) InstaPink.copy(alpha = 0.15f) else SlateSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSel) InstaPink else SlateCardSecondary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedSection = code
                                if (isPlaying) {
                                    stopSpeaking()
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) InstaPink else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Button
                    Button(
                        onClick = {
                            if (isPlaying) {
                                stopSpeaking()
                            } else {
                                startSpeaking()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) InstaPurple else InstaPink),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("tts_play_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Listen",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "Stop" else "Listen",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Speed Toggles: 0.8x, 1.0x, 1.25x, 1.5x
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateSurface)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.8f, 1.0f, 1.2f, 1.5f).forEach { s ->
                            val isCurrentSpeed = speed == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrentSpeed) SlateCardSecondary else Color.Transparent)
                                    .clickable { speed = s }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${s}x",
                                    fontSize = 9.sp,
                                    fontWeight = if (isCurrentSpeed) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrentSpeed) InstaPink else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Voice Language Indicator Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Voice: $language",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ScriptTimelineView(timeline: List<ScriptTimelineItem>?) {
    if (timeline.isNullOrEmpty()) return

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "🎬 TIMELINE & AUDIO-VISUAL PRODUCTION GUIDE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = InstaOrange,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateBg),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, InstaOrange.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Track your reel's pacing, dynamic camera movements, sound effects, facial expressions, and music cues scene-by-scene.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            timeline.forEachIndexed { index, item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SlateCardSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header with Time Slot indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(InstaPink)
                                        .size(8.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scene #${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(InstaOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = item.timeRange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = InstaOrange
                                )
                            }
                        }

                        if (item.text.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${item.text}\"",
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = SlateCardSecondary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Production specs list
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Camera Angle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Camera Angle",
                                    tint = InstaPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("CAMERA ANGLE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaPurple)
                                    Text(item.cameraAngle, fontSize = 11.sp, color = TextPrimary)
                                }
                            }

                            // Expressions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEmotions,
                                    contentDescription = "Expression",
                                    tint = InstaPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("EXPRESSION SUGGESTION", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaPink)
                                    Text(item.expression, fontSize = 11.sp, color = TextPrimary)
                                }
                            }

                            // Background Music
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Background Music",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("BACKGROUND MUSIC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                                    Text(item.backgroundMusic, fontSize = 11.sp, color = TextPrimary)
                                }
                            }

                            // Sound Effects
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Sound Effect",
                                    tint = InstaOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("SOUND EFFECT (SFX)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaOrange)
                                    Text(item.soundEffect, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderGeneratedScriptCard(script: GeneratedScript, viewModel: ScriptViewModel, context: Context) {
    val targetDuration by viewModel.scriptDuration.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.scriptLanguage.collectAsStateWithLifecycle()
    val instagramResult by viewModel.instagramAnalysisResult.collectAsStateWithLifecycle()
    
    var isSaved by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    var hookText by remember(script) { mutableStateOf(script.hook) }
    var bodyText by remember(script) { mutableStateOf(script.body) }
    var visualsText by remember(script) { mutableStateOf(script.visuals) }
    var ctaText by remember(script) { mutableStateOf(script.cta) }
    var hashtagsText by remember(script) { mutableStateOf(script.hashtags) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, InstaPink.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentPink)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "EDITING SCRIPT" else "READY SCRIPT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentPink,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Edit Mode Toggle Button
                    IconButton(
                        onClick = { isEditMode = !isEditMode },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isEditMode) InstaPink.copy(alpha = 0.2f) else SlateCard, CircleShape)
                            .testTag("toggle_edit_mode_button")
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Toggle Edit Mode",
                            tint = if (isEditMode) InstaPink else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Action
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Viral Reels Script",
                                "Title: ${script.title}\n\nHook: ${hookText}\n\nBody: ${bodyText}\n\nCTA: ${ctaText}\n\nVisuals: ${visualsText}\n\nHashtags: ${hashtagsText}"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SlateCard, CircleShape)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy script", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }

                    // Share Action
                    IconButton(
                        onClick = {
                            val shareText = "🔥 NEW VIRAL REEL SCRIPT 🔥\n\n🎯 TITLE: ${script.title}\n\n🚀 HOOK: ${hookText}\n\n📝 SCRIPT BODY:\n${bodyText}\n\n🎯 CTA: ${ctaText}\n\n🎬 VISUAL CUES:\n${visualsText}\n\n🏷️ HASHTAGS:\n${hashtagsText}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Reel Script"))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SlateCard, CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share script", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }

                    // Save Action
                    IconButton(
                        onClick = {
                            viewModel.saveCustomScript(
                                title = script.title,
                                hook = hookText,
                                body = bodyText,
                                cta = ctaText,
                                visuals = visualsText,
                                hashtags = hashtagsText
                            )
                            isSaved = true
                            Toast.makeText(context, "Saved to library!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isSaved) AccentGreen.copy(alpha = 0.2f) else SlateCard, CircleShape)
                    ) {
                        Icon(
                            if (isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                            contentDescription = "Save script",
                            tint = if (isSaved) AccentGreen else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Script Title
            Text(
                text = script.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Render Instagram Analysis Results directly if they are active
            if (instagramResult != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateBg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, InstaPink.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = "Analysis",
                                tint = InstaPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instagram Account Audit: @${instagramResult!!.username}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DISCOVERED NICHE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaPink)
                                Text(instagramResult!!.discoveredNiche, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AUDIENCE DISCOVERY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaPurple)
                                Text(instagramResult!!.audienceProfile, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text("VIRAL GROWTH STRATEGY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                        Text(instagramResult!!.contentStrategy, fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("MOST VIEWED & LIKED POSTS FORMULA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaOrange)
                        Spacer(modifier = Modifier.height(6.dp))
                        instagramResult!!.topPosts.forEach { post ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = post.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(InstaPink.copy(alpha = 0.15f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("👁️ ${post.views}", fontSize = 8.sp, color = InstaPink, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(InstaPurple.copy(alpha = 0.15f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("❤️ ${post.likes}", fontSize = 8.sp, color = InstaPurple, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = post.styleDescription,
                                        fontSize = 9.sp,
                                        color = TextSecondary,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = SlateCardSecondary)

            // Voice Script Audio Player
            TtsScriptPlayer(
                hook = hookText,
                body = bodyText,
                cta = ctaText,
                language = selectedLanguage
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hook block (Vibrant, high-contrast outline)
            Text(
                text = "🚀 HOOK (FIRST 3 SECONDS - CRITICAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InstaOrange,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (isEditMode) {
                OutlinedTextField(
                    value = hookText,
                    onValueChange = { 
                        hookText = it
                        isSaved = false
                    },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_script_hook"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = InstaOrange.copy(alpha = 0.08f),
                        unfocusedContainerColor = InstaOrange.copy(alpha = 0.08f),
                        focusedBorderColor = InstaOrange,
                        unfocusedBorderColor = InstaOrange.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(InstaOrange.copy(alpha = 0.08f))
                        .border(1.dp, InstaOrange.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = hookText,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body script block
            Text(
                text = "📝 SPOKEN BODY SCRIPT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InstaPink,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (isEditMode) {
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { 
                        bodyText = it
                        isSaved = false
                    },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_script_body"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SlateBg,
                        unfocusedContainerColor = SlateBg,
                        focusedBorderColor = InstaPink,
                        unfocusedBorderColor = SlateCardSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = bodyText,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual layout instructions
            Text(
                text = "🎬 VISUAL CUES / B-ROLL INSTRUCTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InstaPurple,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (isEditMode) {
                OutlinedTextField(
                    value = visualsText,
                    onValueChange = { 
                        visualsText = it
                        isSaved = false
                    },
                    textStyle = TextStyle(color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_script_visuals"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = InstaPurple.copy(alpha = 0.05f),
                        unfocusedContainerColor = InstaPurple.copy(alpha = 0.05f),
                        focusedBorderColor = InstaPurple,
                        unfocusedBorderColor = InstaPurple.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(InstaPurple.copy(alpha = 0.05f))
                        .border(1.dp, InstaPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = visualsText,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Layout
            Text(
                text = "🎯 CALL TO ACTION (CTA)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (isEditMode) {
                OutlinedTextField(
                    value = ctaText,
                    onValueChange = { 
                        ctaText = it
                        isSaved = false
                    },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth().testTag("edit_script_cta"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = AccentGreen.copy(alpha = 0.05f),
                        unfocusedContainerColor = AccentGreen.copy(alpha = 0.05f),
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = AccentGreen.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentGreen.copy(alpha = 0.05f))
                        .border(1.dp, AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = ctaText,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hashtags line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tags: ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                if (isEditMode) {
                    OutlinedTextField(
                        value = hashtagsText,
                        onValueChange = { 
                            hashtagsText = it
                            isSaved = false
                        },
                        textStyle = TextStyle(color = InstaPink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth().testTag("edit_script_hashtags"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SlateBg,
                            unfocusedContainerColor = SlateBg,
                            focusedBorderColor = InstaPink,
                            unfocusedBorderColor = SlateCardSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Text(
                        text = hashtagsText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InstaPink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- REAL-TIME CHARACTER & LENGTH ANALYTICS DASHBOARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateBg),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SlateCardSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("script_analytics_dashboard")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Analytics",
                            tint = InstaPink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-Time Instagram Length Guard",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Instagram Caption Limit (2,200 characters)
                    val captionLength = hookText.length + bodyText.length + ctaText.length + hashtagsText.length
                    val captionLimit = 2200
                    val captionProgress = (captionLength.toFloat() / captionLimit).coerceIn(0f, 1f)
                    
                    val (captionColor, captionFeedback) = when {
                        captionLength < 125 -> Pair(
                            AccentGreen,
                            "Optimal: Micro-caption. No truncation on feed! Fits standard display perfectly."
                        )
                        captionLength in 125..1000 -> Pair(
                            InstaPink,
                            "Engaging: Perfect balance for details, though text will truncate with a 'more' button."
                        )
                        captionLength in 1001..2100 -> Pair(
                            InstaOrange,
                            "SEO Rich: High keyword density. Great for search discovery, but keep formatting readable."
                        )
                        else -> Pair(
                            Color.Red,
                            "⚠️ EXCEEDED LIMIT: Instagram will reject any caption exceeding 2,200 characters!"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Instagram Caption Length",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "$captionLength / $captionLimit chars",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = captionColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SlateCardSecondary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(captionProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(captionColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = captionFeedback,
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Video Speaking Pace Tracker (Reel Length Guard)
                    val spokenWords = (hookText + " " + bodyText + " " + ctaText)
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                        .size
                    val estimatedSecs = Math.max(1, Math.round(spokenWords / (135.0 / 60.0)).toInt())
                    val paceProgress = (estimatedSecs.toFloat() / targetDuration).coerceIn(0f, 1.2f)

                    val (paceColor, paceFeedback) = when {
                        estimatedSecs <= targetDuration -> Pair(
                            AccentGreen,
                            "Optimal Pacing! Your script spoken words ($spokenWords) fit comfortably within the ${targetDuration}s Reel window."
                        )
                        estimatedSecs <= targetDuration * 1.1f -> Pair(
                            InstaOrange,
                            "Tight Fit: Requires fast, continuous talking (~150 WPM) to squeeze into ${targetDuration}s."
                        )
                        else -> Pair(
                            Color.Red,
                            "⚠️ Script Too Long: Estimated at ${estimatedSecs}s. Delete some body sentences to keep under your target duration!"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Estimated Speaking Time",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(InstaPink.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Target: ${targetDuration}s",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InstaPink
                                    )
                                }
                            }
                            Text(
                                text = "~${estimatedSecs}s (${spokenWords} words)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = paceColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom Progress Bar (Supports overflow beyond 1.0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SlateCardSecondary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(Math.min(1.0f, paceProgress))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(paceColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = paceFeedback,
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Compact Section Metrics Bento Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hook Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("HOOK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaOrange)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${hookText.length} Chars", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${hookText.split(Regex("\\s+")).filter { it.isNotBlank() }.size} Words", fontSize = 9.sp, color = TextSecondary)
                            }
                        }

                        // Body Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("BODY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = InstaPink)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${bodyText.length} Chars", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${bodyText.split(Regex("\\s+")).filter { it.isNotBlank() }.size} Words", fontSize = 9.sp, color = TextSecondary)
                            }
                        }

                        // CTA Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("CTA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${ctaText.length} Chars", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${ctaText.split(Regex("\\s+")).filter { it.isNotBlank() }.size} Words", fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Interactive Chronological Timeline & Audio-Visual Production Guide
            ScriptTimelineView(script.timeline)

            Spacer(modifier = Modifier.height(18.dp))

            // Button to reset and write a new script
            OutlinedButton(
                onClick = { viewModel.resetScriptResult() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SlateCardSecondary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "New Script", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Another Script", fontSize = 13.sp)
            }
        }
    }
}


// --- TAB 2: CONTENT IDEAS GENERATOR ---
@Composable
fun ContentIdeasTab(viewModel: ScriptViewModel) {
    val context = LocalContext.current

    val isGenerating by viewModel.isGeneratingIdeas.collectAsStateWithLifecycle()
    val ideasResult by viewModel.generatedIdeas.collectAsStateWithLifecycle()
    val errorMsg by viewModel.ideasError.collectAsStateWithLifecycle()

    val topic by viewModel.ideasTopic.collectAsStateWithLifecycle()
    val selectedNiche by viewModel.ideasNiche.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.ideasLanguage.collectAsStateWithLifecycle()

    val niches = listOf("AI & Automation", "Funny & Humor", "Tech & Gadgets", "Finance & Investing", "Education", "Travel & Exploration", "Healthy Eating", "Fitness")
    val languages = listOf("English", "Spanish", "Hindi", "French", "German", "Japanese", "Portuguese", "Arabic", "Italian", "Russian", "Roman Telugu")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Explanatory Intro Card
        if (ideasResult == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, SlateCardSecondary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Idea Icon", tint = InstaYellow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Reel Idea Brainstormer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Brainstorm 5 high-converting, viral-ready video angles with catchy titles, specific visual concepts, and ready-to-use spoken hooks.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Form Content
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, SlateCardSecondary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Topic area
                        Text(
                            text = "Core Topic or Theme",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { viewModel.ideasTopic.value = it },
                            placeholder = { Text("e.g. Daily life hacks, python programming, vegan cooking", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ideas_topic_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SlateBg,
                                unfocusedContainerColor = SlateBg,
                                focusedIndicatorColor = InstaPink,
                                unfocusedIndicatorColor = SlateCardSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Niche Selector (Dropdown Component)
                        Text(
                            text = "Select Content Niche",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        var nicheExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { nicheExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateBg),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SlateCardSecondary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ideas_niche_dropdown_button")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Category,
                                            contentDescription = "Category Icon",
                                            tint = InstaPink,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = selectedNiche,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown icon",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = nicheExpanded,
                                onDismissRequest = { nicheExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(SlateSurface)
                            ) {
                                niches.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Category,
                                                    contentDescription = null,
                                                    tint = if (selectedNiche == item) InstaPink else TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item,
                                                    color = TextPrimary,
                                                    fontWeight = if (selectedNiche == item) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.ideasNiche.value = item
                                            nicheExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Language Selection
                        Text(
                            text = "Language",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { expanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateBg),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedLanguage, color = TextPrimary, fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = TextSecondary)
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(SlateSurface)
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, color = TextPrimary) },
                                        onClick = {
                                            viewModel.ideasLanguage.value = lang
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = { viewModel.generateContentIdeas() },
                            enabled = !isGenerating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_ideas_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(InstaPurple, InstaPink, InstaOrange)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "Spark icon", tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generate 5 Viral Ideas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Render Error
        if (errorMsg != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFE57373))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generation Error", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg!!, color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.clearIdeasError() },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dismiss", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Render loading state placeholder
        if (isGenerating) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = InstaPurple)
                        Text(
                            text = "AI is generating trending angles...",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Matching virality algorithms and creating catchy titles in $selectedLanguage",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Render Generated Content Ideas list
        if (ideasResult != null && !isGenerating) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5 VIRAL IDEAS FOUND",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InstaYellow,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.saveCurrentIdeas()
                                Toast.makeText(context, "Saved entire strategy to library!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Save library", tint = InstaYellow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Strategy", color = TextPrimary, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { viewModel.resetIdeasResult() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(SlateCard, CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset ideas", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            items(ideasResult!!) { idea ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SlateCardSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .clickable { expanded = !expanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(InstaOrange.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = idea.angle.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InstaOrange
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = idea.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = TextSecondary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                HorizontalDivider(color = SlateCardSecondary, modifier = Modifier.padding(bottom = 12.dp))

                                Text(
                                    text = "🎤 VIRAL SPOKEN HOOK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPink
                                )
                                Text(
                                    text = "\"${idea.hook}\"",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                Text(
                                    text = "🎬 B-ROLL & VISUAL CONCEPT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InstaPurple
                                )
                                Text(
                                    text = idea.visualConcept,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText(
                                                "Video Idea",
                                                "Title: ${idea.title}\nAngle: ${idea.angle}\nHook: ${idea.hook}\nVisuals: ${idea.visualConcept}"
                                            )
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Idea copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Idea", color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- TAB 3: SAVED LIBRARY ---
@Composable
fun SavedLibraryTab(viewModel: ScriptViewModel, scripts: List<SavedScript>) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf("ALL") } // ALL, SCRIPT, IDEAS

    val filteredScripts = scripts.filter {
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) ||
                it.topic.contains(searchQuery, ignoreCase = true) ||
                it.body.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilterType == "ALL" || it.type == selectedFilterType
        matchesSearch && matchesType
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Search & Filter Card
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search saved scripts...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SlateSurface,
                        unfocusedContainerColor = SlateSurface,
                        focusedIndicatorColor = InstaPink,
                        unfocusedIndicatorColor = SlateCardSecondary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Filters
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All Saved", "SCRIPT" to "Scripts", "IDEAS" to "Ideas Sets").forEach { (key, label) ->
                        val isSelected = selectedFilterType == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) InstaPink else SlateSurface)
                                .clickable { selectedFilterType = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Empty State Check
        if (filteredScripts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = "Empty Bookmarks",
                        tint = SlateCardSecondary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No saved scripts found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try adjusting your search query." else "Generate scripts or ideas and save them to build your viral catalog.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            items(filteredScripts, key = { it.id }) { item ->
                SavedScriptItem(item, viewModel, context)
            }
        }
    }
}

@Composable
fun SavedScriptItem(script: SavedScript, viewModel: ScriptViewModel, context: Context) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (script.type == "SCRIPT") InstaPink.copy(alpha = 0.25f) else InstaYellow.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            // Top Meta row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (script.type == "SCRIPT") InstaPink.copy(alpha = 0.15f)
                                else InstaYellow.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = script.type,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (script.type == "SCRIPT") InstaPink else InstaYellow
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${script.niche} • ${script.language}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Expand indicator icon
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Script Title
            Text(
                text = script.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Preview subtitle
            if (!isExpanded) {
                Text(
                    text = if (script.type == "SCRIPT") script.hook else "Content strategy list with 5 viral ideas.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Expanded detail contents
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = SlateCardSecondary, modifier = Modifier.padding(bottom = 12.dp))

                    if (script.type == "SCRIPT") {
                        // SCRIPT TYPE DETAILS
                        Text(
                            text = "🚀 HOOK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = InstaOrange
                        )
                        Text(
                            text = script.hook,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        Text(
                            text = "📝 BODY SCRIPT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = InstaPink
                        )
                        Text(
                            text = script.body,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        if (script.visuals.isNotEmpty()) {
                            Text(
                                text = "🎬 VISUAL CUES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = InstaPurple
                            )
                            Text(
                                text = script.visuals,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )
                        }

                        Text(
                            text = "🎯 CTA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                        Text(
                            text = script.cta,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        if (script.hashtags.isNotEmpty()) {
                            Text(
                                text = "🏷️ HASHTAGS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = script.hashtags,
                                fontSize = 12.sp,
                                color = InstaPink,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Deserialize and render timeline if available
                        val timelineItems = remember(script.timelineJson) {
                            if (script.timelineJson.isBlank()) null
                            else {
                                try {
                                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                                    val adapter = moshi.adapter<List<ScriptTimelineItem>>(
                                        com.squareup.moshi.Types.newParameterizedType(List::class.java, ScriptTimelineItem::class.java)
                                    )
                                    adapter.fromJson(script.timelineJson)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                            }
                        }
                        if (timelineItems != null) {
                            ScriptTimelineView(timelineItems)
                        }
                    } else {
                        // CONTENT IDEAS TYPE DETAILS (Parse local JSON)
                        val parsedIdeas = remember(script.ideasJson) {
                            try {
                                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Map::class.java)
                                val adapter = moshi.adapter<List<Map<String, String>>>(listType)
                                val rawList = adapter.fromJson(script.ideasJson) ?: emptyList()
                                rawList.map {
                                    GeneratedIdea(
                                        title = it["title"] ?: "",
                                        angle = it["angle"] ?: "",
                                        hook = it["hook"] ?: "",
                                        visualConcept = it["visualConcept"] ?: ""
                                    )
                                }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }

                        parsedIdeas.forEachIndexed { idx, idea ->
                            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                Text(
                                    text = "IDEA ${idx + 1}: ${idea.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = InstaYellow
                                )
                                Text(
                                    text = "Angle: ${idea.angle.uppercase()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    color = InstaOrange,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Hook: \"${idea.hook}\"",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Visuals: ${idea.visualConcept}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (idx < parsedIdeas.size - 1) {
                                    HorizontalDivider(color = SlateCardSecondary.copy(alpha = 0.5f), modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action tools row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete Button
                        Button(
                            onClick = {
                                viewModel.deleteSavedScript(script.id)
                                Toast.makeText(context, "Deleted from library", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = Color(0xFFEF5350), fontSize = 11.sp)
                        }

                        // Copy Button
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val content = if (script.type == "SCRIPT") {
                                    "Title: ${script.title}\nHook: ${script.hook}\nBody: ${script.body}\nCTA: ${script.cta}\nVisuals: ${script.visuals}\nHashtags: ${script.hashtags}"
                                } else {
                                    script.ideasJson
                                }
                                val clip = ClipData.newPlainText("Library script", content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied content to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy All", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}


// --- TAB 4: VIRALITY HUB ---
@Composable
fun ViralityHubTab() {
    val context = LocalContext.current

    val rules = listOf(
        ViralRule(
            title = "The 3-Second Rule (Pattern Interrupt)",
            desc = "To stop a user from scrolling, introduce a sharp pattern interrupt. A physical move towards the camera, a contrasting word flash, or sound effect inside 1.5 seconds is key.",
            color = InstaPink
        ),
        ViralRule(
            title = "Open Curiosity Loops",
            desc = "Never start with 'Hi my name is'. Start directly with the core problem: 'This simple website does the work of 5 employees... but there's a catch.' The 'catch' is an open loop they must stay to close.",
            color = InstaPurple
        ),
        ViralRule(
            title = "Dynamic Text on Screen",
            desc = "Over 70% of viewers watch Instagram Reels with audio completely off. Bold, centered, animated text highlighting emotional power keywords keeps eyes locked to the screen.",
            color = InstaOrange
        ),
        ViralRule(
            title = "CTA Optimization (Shares/Saves > Likes)",
            desc = "The Instagram Reels algorithm heavily prioritizes SAVES and SHARES. Direct your CTAs to offer highly reusable value. E.g. 'Save this so you can use this template tomorrow.'",
            color = AccentGreen
        )
    )

    val templates = listOf(
        "\"Do NOT do [Mistake] if you want to achieve [Goal]...\"",
        "\"This 10-second hack will save you hundreds of hours in [Topic]...\"",
        "\"The secret website that feels illegal to know for [Niche] creators...\"",
        "\"I tried [Action/Trend] for 30 days and here is what happened...\"",
        "\"99% of people do [Action] wrong. Here is the actual way to do it...\""
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Feature Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, SlateCardSecondary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🚀 IG Virality Playbook",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Proven biological and behavioral triggers that satisfy the social media algorithm.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Section: Hook templates
        item {
            Text(
                text = "🔥 HIGH-CONVERTING HOOK TEMPLATES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = InstaOrange,
                letterSpacing = 1.sp
            )
        }

        items(templates) { template ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SlateCardSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = template,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Hook template", template)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Hook template copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(SlateBg, CircleShape)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hook", tint = TextPrimary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // Section: Algorithmic triggers
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "📊 THE VIRAL ALGORITHM CHEATSHEET",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = InstaPink,
                letterSpacing = 1.sp
            )
        }

        items(rules) { rule ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SlateCardSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(rule.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rule.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rule.desc,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

data class ViralRule(
    val title: String,
    val desc: String,
    val color: Color
)

// Helper standard vector icon to draw an Instagram Reels logo
fun imageOfReel(): androidx.compose.ui.graphics.vector.ImageVector {
    return Icons.Default.PlayCircle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoHistorySidebar(
    viewModel: ScriptViewModel,
    savedScripts: List<SavedScript>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredHistory = savedScripts.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.topic.contains(searchQuery, ignoreCase = true) ||
                it.niche.contains(searchQuery, ignoreCase = true)
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.82f)
            .widthIn(max = 380.dp)
            .testTag("history_sidebar"),
        color = SlateSurface,
        tonalElevation = 16.dp,
        border = BorderStroke(1.dp, SlateCardSecondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History Icon",
                        tint = InstaPink,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "History Catalog",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(SlateBg, CircleShape)
                        .testTag("history_sidebar_close_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Sidebar",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search history box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search history...", color = TextSecondary, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SlateBg,
                    unfocusedContainerColor = SlateBg,
                    focusedIndicatorColor = InstaPink,
                    unfocusedIndicatorColor = SlateCardSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List of History items
            if (filteredHistory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = "No history",
                        tint = SlateCardSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching results" else "No generation history yet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try other keywords." else "Your generated content will automatically save here.",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateBg),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, SlateCardSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Meta tags row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (item.type == "SCRIPT") InstaPink.copy(alpha = 0.15f)
                                                else InstaYellow.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.type,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (item.type == "SCRIPT") InstaPink else InstaYellow
                                        )
                                    }

                                    Text(
                                        text = item.niche,
                                        fontSize = 9.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Title
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Topic
                                Text(
                                    text = "Topic: ${item.topic}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Actions row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Load Button
                                    Button(
                                        onClick = {
                                            if (item.type == "SCRIPT") {
                                                viewModel.loadScriptIntoGenerator(item)
                                                Toast.makeText(context, "Restored script into generator!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.loadIdeasIntoGenerator(item)
                                                Toast.makeText(context, "Restored ideas strategy!", Toast.LENGTH_SHORT).show()
                                            }
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CloudDownload,
                                            contentDescription = "Restore",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Restore", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteSavedScript(item.id)
                                            Toast.makeText(context, "Removed from history", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
