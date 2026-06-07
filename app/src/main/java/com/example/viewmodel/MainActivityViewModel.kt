package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.MascotExpression
import com.example.ui.StudyMateScreen
import com.example.ui.AIChatTool
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// Dynamic quiz model
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    var selectedAnswerIndex: Int? = null,
    val explanation: String
)

// Dynamic Flashcard model
data class StudyFlashcard(
    val front: String,
    val back: String,
    var isFlipped: Boolean = false
)

// Dynamic MindMap node model
data class MindmapNode(
    val label: String,
    val children: List<MindmapNode> = emptyList(),
    var isExpanded: Boolean = true
)

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StudyMateDatabase.getDatabase(application)
    private val dao = db.dao()

    private val prefs = application.getSharedPreferences("studymate_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    val lastNightOwlClaimDate: String get() = prefs.getString("last_night_owl_claim", "") ?: ""

    fun claimNightOwlXpBoost() {
        val user = _currentUser.value ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        
        if (lastNightOwlClaimDate != todayStr) {
            prefs.edit().putString("last_night_owl_claim", todayStr).apply()
            viewModelScope.launch {
                val updated = user.copy(totalXp = user.totalXp + 20)
                dao.updateUser(updated)
                _currentUser.value = updated
                awardBadge("night_owl")
                _mascotExpression.value = MascotExpression.Celebrating
                _mascotMessage.value = "🦉 Starry Flight Award! Added +20 XP for your midnight study grind! Unlocked Night Owl badge! Keep going! 🌌✨"
            }
        }
    }

    val lastUsername: String? get() = prefs.getString("last_username", null)
    val isSessionActive: Boolean get() = prefs.getBoolean("is_session_active", false)

    fun saveSession(username: String) {
        prefs.edit()
            .putString("last_username", username)
            .putBoolean("is_session_active", true)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean("is_session_active", false)
            .apply()
    }

    // --- Screen State ---
    private val _currentScreen = MutableStateFlow(StudyMateScreen.Splash)
    val currentScreen: StateFlow<StudyMateScreen> = _currentScreen.asStateFlow()

    // --- Active Selected AI Chat Tool ---
    private val _selectedChatTool = MutableStateFlow(AIChatTool.Explain)
    val selectedChatTool: StateFlow<AIChatTool> = _selectedChatTool.asStateFlow()

    fun setSelectedChatTool(tool: AIChatTool) {
        _selectedChatTool.value = tool
    }

    // --- Authentication & Active User State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- Flows observed from database ---
    val conversations: StateFlow<List<ChatConversationEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getConversations(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getNotes(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drawings: StateFlow<List<DrawingEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getDrawings(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pdfs: StateFlow<List<PdfEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getPdfs(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pdfFolders: StateFlow<List<com.example.data.PdfFolderEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getPdfFolders(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getFocusSessions(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getTasks(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetable: StateFlow<List<TimetableEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getTimetable(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val longTermGoals: StateFlow<List<LongTermGoalEntity>> = _currentUser
        .flatMapLatest { user ->
            user?.let { dao.getLongTermGoals(it.username) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeReminder = MutableStateFlow<TimetableEntity?>(null)
    val activeReminder: StateFlow<TimetableEntity?> = _activeReminder.asStateFlow()

    private val triggeredStamps = mutableSetOf<String>()

    // --- Interactive Tool States ---
    private val _aiResponseText = MutableStateFlow("")
    val aiResponseText: StateFlow<String> = _aiResponseText.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _mascotExpression = MutableStateFlow(MascotExpression.Happy)
    val mascotExpression: StateFlow<MascotExpression> = _mascotExpression.asStateFlow()

    private val _mascotMessage = MutableStateFlow("Hi! I'm your owl study buddy. Let's learn something amazing today! 🦉")
    val mascotMessage: StateFlow<String> = _mascotMessage.asStateFlow()

    // --- Study Mode / Pomodoro State ---
    private val _pomodoroSecondsLeft = MutableStateFlow(1500) // 25 mins standard
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    val initialPomodoroSeconds = 1500 // tracking base

    // --- Dynamic Content Containers generated by AI ---
    private val _activeQuiz = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val activeQuiz: StateFlow<List<QuizQuestion>> = _activeQuiz.asStateFlow()

    private val _activeFlashcards = MutableStateFlow<List<StudyFlashcard>>(emptyList())
    val activeFlashcards: StateFlow<List<StudyFlashcard>> = _activeFlashcards.asStateFlow()

    private val _activeMindmap = MutableStateFlow<MindmapNode?>(null)
    val activeMindmap: StateFlow<MindmapNode?> = _activeMindmap.asStateFlow()

    // --- Selected Item State for view details ---
    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    private val _selectedPdf = MutableStateFlow<PdfEntity?>(null)
    val selectedPdf: StateFlow<PdfEntity?> = _selectedPdf.asStateFlow()

    private val _activePdfFolder = MutableStateFlow<String?>(null)
    val activePdfFolder: StateFlow<String?> = _activePdfFolder.asStateFlow()

    // --- Congratulatory Dialog State ---
    private val _recentUnlockedBadge = MutableStateFlow<String?>(null)
    val recentUnlockedBadge: StateFlow<String?> = _recentUnlockedBadge.asStateFlow()

    init {
        // Run timer tick in background
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_isTimerRunning.value) {
                    if (_pomodoroSecondsLeft.value > 0) {
                        _pomodoroSecondsLeft.value -= 1
                    } else {
                        // Timer completed!
                        _isTimerRunning.value = false
                        completeFocusSession()
                    }
                }
            }
        }

        // Start periodic timetable reminder check
        viewModelScope.launch {
            while (true) {
                checkTimetableReminders()
                kotlinx.coroutines.delay(8000) // check every 8 seconds
            }
        }

        // Session routing check on startup
        viewModelScope.launch {
            val lastUser = lastUsername
            val sessionActive = isSessionActive
            if (sessionActive && lastUser != null) {
                val user = dao.getUserByUsername(lastUser)
                if (user != null) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    _currentScreen.value = StudyMateScreen.Dashboard
                    updateStudyStreakAndXP(lastUser)
                    awardBadge("first_login")
                } else {
                    _currentScreen.value = StudyMateScreen.Splash
                }
            } else {
                _currentScreen.value = StudyMateScreen.Splash
            }
        }
    }

    private fun checkTimetableReminders() {
        val user = _currentUser.value ?: return
        val currentList = timetable.value
        if (currentList.isEmpty()) return

        val sdfDay = java.text.SimpleDateFormat("EEEE", java.util.Locale.US)
        val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        val now = java.util.Date()
        val dayName = sdfDay.format(now)
        val timeStr = sdfTime.format(now)

        for (item in currentList) {
            if (item.isReminderEnabled && item.dayOfWeek.equals(dayName, ignoreCase = true) && item.time == timeStr) {
                val stamp = "${item.id}-$dayName-$timeStr"
                if (stamp !in triggeredStamps) {
                    triggeredStamps.add(stamp)
                    _activeReminder.value = item
                    _mascotMessage.value = "🦉 BEEP BEEP! It's study time for: ${item.subject.uppercase()}! You scheduled this session on $dayName at $timeStr. Let's learn! 🔥"
                    _mascotExpression.value = MascotExpression.Celebrating
                    
                    // Show actual native Android notification
                    sendSystemNotification(item.subject, "Reminder: Scheduled class/study session started!")
                }
            }
        }
    }

    private fun sendSystemNotification(title: String, message: String) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "studymate_reminders"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "StudyMate Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Class and Study Timetable Reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("StudyMate Class Schedule: $title")
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // --- Navigation Functions ---
    fun navigateTo(screen: StudyMateScreen) {
        _currentScreen.value = screen
        updateCompanionTip()
    }

    // --- Login & Registration ---
    fun register(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        if (username.isBlank() || pass.length < 4) {
            onResult(false, "Username cannot be empty, and password must be at least 4 characters!")
            return
        }
        viewModelScope.launch {
            val existing = dao.getUserByUsername(username)
            if (existing != null) {
                onResult(false, "Username already exists! Tap Login.")
                return@launch
            }

            // Create new user
            val newUser = UserEntity(
                username = username,
                passwordHash = pass, // plaintext/simple for prototype
                completedBadgeIds = "",
                lastStudyDate = ""
            )
            dao.insertUser(newUser)
            onResult(true, "Successfully registered! Logging you in.")
            // Log in right away
            login(username, pass) { _, _ -> }
        }
    }

    fun login(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = dao.getUserByUsername(username)
            if (user == null || user.passwordHash != pass) {
                onResult(false, "Invalid username or password!")
                return@launch
            }

            _currentUser.value = user
            _isLoggedIn.value = true
            _currentScreen.value = StudyMateScreen.Dashboard
            onResult(true, "Welcome back, $username!")

            // Verify and update study streak
            updateStudyStreakAndXP(username)
            awardBadge("first_login")

            // Persist session
            saveSession(username)
        }
    }

    fun loginByPhoneLock(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val lastUser = lastUsername ?: return
        viewModelScope.launch {
            val user = dao.getUserByUsername(lastUser)
            if (user != null) {
                _currentUser.value = user
                _isLoggedIn.value = true
                _currentScreen.value = StudyMateScreen.Dashboard
                onResult(true, "Welcome back, $lastUser!")

                // Verify and update study streak
                updateStudyStreakAndXP(lastUser)
                awardBadge("first_login")

                // Keep session active
                saveSession(lastUser)
            } else {
                onResult(false, "No record found for $lastUser.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _currentScreen.value = StudyMateScreen.Login
        clearSession()
    }

    // --- Study Streak & XP Mechanics ---
    private suspend fun updateStudyStreakAndXP(username: String) {
        val user = dao.getUserByUsername(username) ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        var streak = user.currentStreak
        var longest = user.longestStreak
        val lastDateStr = user.lastStudyDate

        if (lastDateStr.isEmpty()) {
            streak = 1
        } else {
            val today = dateFormat.parse(todayStr)
            val lastDate = dateFormat.parse(lastDateStr)
            if (today != null && lastDate != null) {
                val diffMs = today.time - lastDate.time
                val diffDays = diffMs / (1000 * 60 * 60 * 24)

                if (diffDays == 1L) {
                    streak += 1
                } else if (diffDays > 1L) {
                    streak = 0 // reset
                }
            }
        }

        if (streak > longest) {
            longest = streak
        }

        val updated = user.copy(
            currentStreak = streak,
            longestStreak = longest,
            lastStudyDate = todayStr
        )
        dao.updateUser(updated)
        _currentUser.value = updated

        // Check badge awards based on streak
        if (streak >= 3) awardBadge("streak_3_day")
        if (streak >= 7) awardBadge("streak_7_day")
        if (streak >= 30) awardBadge("streak_30_day")
    }

    fun addXp(amount: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val currentXp = user.totalXp + amount
            val updated = user.copy(totalXp = currentXp)
            dao.updateUser(updated)
            _currentUser.value = updated

            // Badge rewards for XP milestones
            if (currentXp >= 100) awardBadge("xp_100")
            if (currentXp >= 500) awardBadge("xp_500")
            if (currentXp >= 1500) awardBadge("xp_1500")
        }
    }

    fun studyAndLogHabit() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            addXp(15)
            _mascotExpression.value = MascotExpression.Celebrating
            _mascotMessage.value = "Hooray! Interactive learning block logged for today! +15 XP rewarded to ${user.username}! 🦉🏆"
        }
    }

    // Badge triggers
    fun awardBadge(badgeId: String) {
        val user = _currentUser.value ?: return
        val currentBadges = user.completedBadgeIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (badgeId !in currentBadges) {
            currentBadges.add(badgeId)
            viewModelScope.launch {
                val updated = user.copy(completedBadgeIds = currentBadges.joinToString(","))
                dao.updateUser(updated)
                _currentUser.value = updated
                // Show celebration trigger
                _recentUnlockedBadge.value = badgeId
                _mascotExpression.value = MascotExpression.Celebrating
                _mascotMessage.value = "WHOO! You unlocked the \"${formatBadgeName(badgeId)}\" badge! You're outstanding! 🥳🦉"
            }
        }
    }

    fun dismissBadgeCelebration() {
        _recentUnlockedBadge.value = null
        _mascotExpression.value = MascotExpression.Happy
    }

    private fun formatBadgeName(id: String): String {
        return id.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    // --- Study Actions ---
    // 1. Concept Explainer AI Tool
    fun explainConcept(concept: String) {
        if (concept.isBlank()) return
        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Studious
        _mascotMessage.value = "Reading about $concept now... Giving a professional breakdown! 📖✏️"

        viewModelScope.launch {
            val prompt = "Explain the concept \"$concept\" using clear headings, definitions, visual examples, and a quick summary. Make it easy to read."
            val response = GeminiService.getStudyResponse(prompt, "You are study companion StudyMate AI. Use Markdown format.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            _mascotExpression.value = MascotExpression.Happy

            // Add conversation to DB
            insertAiConversation("Concept", concept, response)
            addXp(15)
            awardBadge("first_ai_query")
        }
    }

    // 2. Summarize long Text
    fun summarizeText(text: String) {
        if (text.isBlank()) return
        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Studious
        _mascotMessage.value = "Reading through that paragraph... Creating a beautiful brief summarize! 🔍📝"

        viewModelScope.launch {
            val prompt = "Provide a concise summary of the following text under key headers, and outline 3 major takeaways:\n$text"
            val response = GeminiService.getStudyResponse(prompt, "You are StudyMate AI text summarizer. Use Markdown formatting.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            _mascotExpression.value = MascotExpression.Happy

            insertAiConversation("Summarize", text.take(60) + "...", response)
            addXp(15)
        }
    }

    // 3. Quiz Questions Generator
    fun generateQuiz(topic: String) {
        if (topic.isBlank()) return
        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Studious
        _mascotMessage.value = "Creating exam quiz sheets for $topic! Prepare your brain... 🧠⚡"

        viewModelScope.launch {
            val prompt = """
                Generate exactly 3 Multiple Choice Questions (MCQs) on the topic: "$topic".
                Return ONLY a JSON array in the exact format:
                [
                  {
                    "question": "What is ...?",
                    "options": ["A", "B", "C", "D"],
                    "correctIndex": 0,
                    "explanation": "Why correct..."
                  }
                ]
                Do not include any markdown comments or enclosing ```json tags.
            """.trimIndent()
            val response = GeminiService.getStudyResponse(prompt, "You are StudyMate AI quiz engine. Return raw JSON array text.")
            
            _isAiLoading.value = false
            try {
                // Stripping potential markdown text wraps to prevent parser crash
                val cleaned = response.substringAfter("[").substringBeforeLast("]")
                val jsonArr = JSONArray("[$cleaned]")
                val list = mutableListOf<QuizQuestion>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val optsJson = obj.getJSONArray("options")
                    val opts = List(optsJson.length()) { optsJson.getString(it) }
                    list.add(
                        QuizQuestion(
                            question = obj.getString("question"),
                            options = opts,
                            correctAnswerIndex = obj.getInt("correctIndex"),
                            explanation = obj.getString("explanation")
                        )
                    )
                }
                _activeQuiz.value = list
                _aiResponseText.value = "Successfully generated a ${list.size}-question quiz! Take it below."
                _mascotMessage.value = "The quiz is ready! Can you score 100%?"
                awardBadge("ai_explorer")
            } catch (e: Exception) {
                e.printStackTrace()
                _aiResponseText.value = "Failed to parse generated quiz structure. Raw generated text:\n\n$response"
                _mascotMessage.value = "Wait, my spell failed! Try asking the quiz generator again."
            }
        }
    }

    // Submit Quiz responses and award XP
    fun submitQuizAnswer(questionIndex: Int, selectedOptionIndex: Int) {
        val list = _activeQuiz.value.toMutableList()
        if (questionIndex in list.indices) {
            list[questionIndex] = list[questionIndex].copy(selectedAnswerIndex = selectedOptionIndex)
            _activeQuiz.value = list

            // Check if user answered all correct and award extra XP
            val allAnswered = list.all { it.selectedAnswerIndex != null }
            if (allAnswered) {
                val correctCount = list.count { it.selectedAnswerIndex == it.correctAnswerIndex }
                val scoreXp = correctCount * 15
                addXp(scoreXp)
                if (correctCount == list.size) {
                    awardBadge("quiz_master")
                    _mascotExpression.value = MascotExpression.Celebrating
                    _mascotMessage.value = "PERFECT quiz! You scored full marks and got +$scoreXp XP! Outstanding job! 🦉🎉"
                } else {
                    _mascotMessage.value = "Quiz submitted! You got $correctCount/${list.size} correct. Review explanations below!"
                }
            }
        }
    }

    // 4. Explain like I'm 5 (ELI5)
    fun explainEli5(concept: String) {
        if (concept.isBlank()) return
        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Encouraging
        _mascotMessage.value = "Simplifying $concept so even a cozy hedgehog could understand! 🦔🍃"

        viewModelScope.launch {
            val prompt = "Explain the complex concept of \"$concept\" using analogies and direct language suitable for a 5-year-old child."
            val response = GeminiService.getStudyResponse(prompt, "You are StudyMate AI. Speak like a friendly primary school coach.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("ELI5", concept, response)
            addXp(15)
        }
    }

    // 5. Generate Flashcards tool
    fun generateFlashcards(topic: String) {
        if (topic.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Folding flashcard templates for $topic... 📑✨"

        viewModelScope.launch {
            val prompt = """
                Generate exactly 4 card decks (front and back) representing revision flashcards for: "$topic".
                Return ONLY a JSON array in the exact format:
                [
                  {
                    "front": "Question/Term",
                    "back": "Answer/Definition"
                  }
                ]
                Do not include markdown tags.
            """.trimIndent()
            val response = GeminiService.getStudyResponse(prompt, "Return raw JSON text only.")
            _isAiLoading.value = false
            try {
                val cleaned = response.substringAfter("[").substringBeforeLast("]")
                val arr = JSONArray("[$cleaned]")
                val cards = mutableListOf<StudyFlashcard>()
                for (it in 0 until arr.length()) {
                    val obj = arr.getJSONObject(it)
                    cards.add(StudyFlashcard(obj.getString("front"), obj.getString("back")))
                }
                _activeFlashcards.value = cards
                _aiResponseText.value = "Generated ${cards.size} Revision Flashcards! Tap to flip them."
            } catch (e: Exception) {
                e.printStackTrace()
                _aiResponseText.value = "Flashcards parsed incorrectly. Raw output:\n\n$response"
            }
        }
    }

    fun flipFlashcard(index: Int) {
        val list = _activeFlashcards.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(isFlipped = !list[index].isFlipped)
            _activeFlashcards.value = list
            addXp(2) // flashcard studies
        }
    }

    // 6. Create Study Plan
    fun generateStudyPlan(topic: String) {
        if (topic.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Plotting a success schedule for $topic! Lets map it out... 🗺️⚡"

        viewModelScope.launch {
            val prompt = "Generate a weekly study schedule plan for learning \"$topic\" effectively. Divide into Day 1-7, including milestones and visual checklist."
            val response = GeminiService.getStudyResponse(prompt, "You are StudyMate AI Schedule Plan generator. Use markdown bullet checklists.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("StudyPlan", topic, response)
            addXp(20)
        }
    }

    // 7. Rewrite Professionally
    fun rewriteProfessionally(text: String) {
        if (text.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Polishing and sharpening your writing! 👔✍️"

        viewModelScope.launch {
            val prompt = "Rewrite the following paragraphs to be highly professional, structured, and clear. Suggest improvements:\n\n$text"
            val response = GeminiService.getStudyResponse(prompt, "You are StudyMate AI writing assistant.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("Rewrite", text.take(65) + "...", response)
            addXp(15)
        }
    }

    // 8. Convert into Bullet Points
    fun convertToBullets(text: String) {
        if (text.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Chop chop! Shredding that article into bullet points! ✂️🗒️"

        viewModelScope.launch {
            val prompt = "Translate this long narrative into a clear bulleted, indent-based summary with bold headings:\n\n$text"
            val response = GeminiService.getStudyResponse(prompt, "Convert into quick revisions points.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("Bullets", text.take(60) + "...", response)
            addXp(15)
        }
    }

    // 9. Generate Interview Prep
    fun generateInterviewPrep(topic: String) {
        if (topic.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Mocking an technical interview panel about $topic... 🤵📊"

        viewModelScope.launch {
            val prompt = "Generate 4 likely technical interview questions about \"$topic\" along with expert answer guidelines."
            val response = GeminiService.getStudyResponse(prompt, "StudyMate AI Interview Board.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("Interview", topic, response)
            addXp(15)
        }
    }

    // 10. Translate text
    fun translateText(text: String, targetLang: String) {
        if (text.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Translating text into beautiful $targetLang! 🌍💬"

        viewModelScope.launch {
            val prompt = "Translate the following text into $targetLang: \n\n$text"
            val response = GeminiService.getStudyResponse(prompt, "Output only translation as clean text.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("Translate", "To $targetLang", response)
            addXp(15)
        }
    }

    // 11. Generate short notes (Revision cards)
    fun generateShortNotes(topic: String) {
        if (topic.isBlank()) return
        _isAiLoading.value = true
        _mascotMessage.value = "Assembling concise summary cards on $topic! 📝🃏"

        viewModelScope.launch {
            val prompt = "Create short, high-density revision crib sheets for \"$topic\". Focus on formulas, key terms, definitions, and quick references."
            val response = GeminiService.getStudyResponse(prompt, "Keep it very concise with bold bullet lists.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            insertAiConversation("ShortNotes", topic, response)
            addXp(15)
        }
    }

    // 12. Mind Map Generation System
    fun generateMindmap(topic: String) {
        if (topic.isBlank()) return
        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Studious
        _mascotMessage.value = "Weaving an interactive visual mind map for $topic... 🕸️🧠"

        viewModelScope.launch {
            val prompt = """
                Generate a dynamic hierarchical mind map centered on the concept: "$topic".
                Return ONLY a JSON tree structure representing the mind map in the exact format:
                {
                  "label": "Topic Name",
                  "children": [
                    {
                      "label": "Subtopic A",
                      "children": [
                        { "label": "Detail A1" },
                        { "label": "Detail A2" }
                      ]
                    },
                    {
                      "label": "Subtopic B",
                      "children": [
                        { "label": "Detail B1" }
                      ]
                    }
                  ]
                }
                Verify there are no markdown quotes, enclosed in ```json symbols, or outer strings.
            """.trimIndent()
            val response = GeminiService.getStudyResponse(prompt, "Output raw JSON payload matching structural outline.")
            _isAiLoading.value = false
            try {
                val cleaned = response.substringAfter("{").substringBeforeLast("}")
                val tree = buildMindmapFromJson(JSONObject("{$cleaned}"))
                _activeMindmap.value = tree
                _aiResponseText.value = "Structured Mind Map generated! Explore branches below."
                _mascotMessage.value = "Mind map is fully customizable!"
            } catch (e: Exception) {
                e.printStackTrace()
                _aiResponseText.value = "Parsed raw JSON tree failed. Full text:\n\n$response"
                _mascotMessage.value = "My mind map grid disconnected. Try regenerating!"
            }
        }
    }

    private fun buildMindmapFromJson(json: JSONObject): MindmapNode {
        val label = json.getString("label")
        val children = mutableListOf<MindmapNode>()
        if (json.has("children")) {
            val arr = json.getJSONArray("children")
            for (i in 0 until arr.length()) {
                children.add(buildMindmapFromJson(arr.getJSONObject(i)))
            }
        }
        return MindmapNode(label, children)
    }

    // Helper to insert logs to history DB
    private suspend fun insertAiConversation(tool: String, prompt: String, response: String) {
        val user = _currentUser.value ?: return
        dao.insertConversation(
            ChatConversationEntity(
                username = user.username,
                toolType = tool,
                prompt = prompt,
                response = response
            )
        )
    }

    // Delete feedback history item
    fun deleteConversationById(id: Int) {
        viewModelScope.launch {
            dao.deleteConversation(id)
        }
    }

    fun clearAllConversations() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.clearConversations(user.username)
        }
    }

    // --- Interactive Chat Buddy ---
    fun sendChatMessage(messageText: String) {
        if (messageText.isBlank()) return
        val user = _currentUser.value ?: return

        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Studious
        _mascotMessage.value = "Let me think about that... 🧠💭"

        viewModelScope.launch {
            try {
                // Get last 20 messages of this toolType to keep the context window balanced
                val pastLogs = conversations.value
                    .filter { it.toolType == "Chat" }
                    .take(20)
                    .reversed()
                    .flatMap { listOf(Pair("user", it.prompt), Pair("model", it.response)) }

                val fullHistory = pastLogs + Pair("user", messageText)

                val response = GeminiService.getChatResponse(
                    chatHistory = fullHistory,
                    systemInstruction = "You are StudyMate AI buddy, a friendly, super intelligent interactive study partner. Give clean, readable, encouraging answers using appropriate bullet points or simplified examples.",
                    temperature = 0.7f
                )

                _isAiLoading.value = false
                _mascotExpression.value = MascotExpression.Happy
                _mascotMessage.value = "I've responded! Read it below or ask another question."

                insertAiConversation("Chat", messageText, response)
                addXp(12)
            } catch (e: Exception) {
                e.printStackTrace()
                _isAiLoading.value = false
                _mascotExpression.value = MascotExpression.Studious
                _mascotMessage.value = "My connection with Gemini lapsed. Let's try again!"
            }
        }
    }

    fun clearChatHistory() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.deleteConversationsByType(user.username, "Chat")
        }
    }

    // --- Notes Board Module Management ---
    fun saveNote(subject: String, chapter: String, topic: String, content: String, isImageAttached: Boolean = false, imagePath: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.insertNote(
                NoteEntity(
                    username = user.username,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    content = content,
                    isImageAttached = isImageAttached,
                    attachedImagePath = imagePath
                )
            )
            addXp(20)
            awardBadge("first_note")
            _mascotExpression.value = MascotExpression.Celebrating
            _mascotMessage.value = "Brilliant! Your note about \"$topic\" is securely saved in your study vault! Keep it up! 📓🌟"
        }
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            dao.deleteNote(id)
        }
    }

    // --- Drawings Canvas Sandboxes ---
    fun saveDrawing(title: String, strokeData: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.insertDrawing(
                DrawingEntity(
                    username = user.username,
                    title = title,
                    strokeData = strokeData
                )
            )
            addXp(20)
            _mascotExpression.value = MascotExpression.Celebrating
            _mascotMessage.value = "Magnificent sketch! Your blueprint drawing \"$title\" is securely saved in your local gallery! 🎨🖌️"
        }
    }

    fun deleteDrawingById(id: Int) {
        viewModelScope.launch {
            dao.deleteDrawing(id)
        }
    }

    fun selectNote(note: NoteEntity?) {
        _selectedNote.value = note
    }

    // --- PDF Notebook Organizer ---
    fun selectPdf(pdf: PdfEntity?) {
        _selectedPdf.value = pdf
    }

    fun setActivePdfFolder(folder: String?) {
        _activePdfFolder.value = folder
    }

    fun deletePdf(id: Int) {
        viewModelScope.launch {
            dao.deletePdf(id)
        }
    }

    fun createPdfFolder(folderName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.insertPdfFolder(
                com.example.data.PdfFolderEntity(
                    username = user.username,
                    folderName = folderName
                )
            )
        }
    }

    fun deletePdfFolder(id: Int) {
        viewModelScope.launch {
            dao.deletePdfFolder(id)
        }
    }

    fun deletePdfFolderByName(folderName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.deletePdfFolderByName(user.username, folderName)
        }
    }

    fun uploadSimulatedPdf(title: String, sampleFileName: String, subject: String = "General") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            dao.insertPdf(
                PdfEntity(
                    username = user.username,
                    title = title,
                    localUri = sampleFileName,
                    bookmarks = "",
                    subject = subject
                )
            )
            // also ensure folder exists
            dao.insertPdfFolder(
                com.example.data.PdfFolderEntity(
                    username = user.username,
                    folderName = subject
                )
            )
            addXp(25)
            awardBadge("first_pdf_upload")
            _mascotExpression.value = MascotExpression.Celebrating
            _mascotMessage.value = "Hooray! Attached simulated document: $title. Ready to bookmark and summarize! 📑🦉"
        }
    }

    fun togglePdfBookmark(pdfId: Int, pageNum: Int) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            // Find in current db
            _selectedPdf.value?.let { pdf ->
                val bookmarksSet = pdf.bookmarks.split(",")
                    .filter { it.isNotBlank() }
                    .map { it.toInt() }
                    .toMutableSet()

                if (pageNum in bookmarksSet) {
                    bookmarksSet.remove(pageNum)
                } else {
                    bookmarksSet.add(pageNum)
                }

                val updatedStr = bookmarksSet.joinToString(",")
                val updatedPdf = pdf.copy(bookmarks = updatedStr)
                dao.updatePdf(updatedPdf)
                _selectedPdf.value = updatedPdf
            }
        }
    }

    fun savePdfAnnotation(annotation: String) {
        viewModelScope.launch {
            _selectedPdf.value?.let { pdf ->
                val updatedPdf = pdf.copy(annotationData = annotation)
                dao.updatePdf(updatedPdf)
                _selectedPdf.value = updatedPdf
                addXp(10)
            }
        }
    }

    // PDF AI Integrations
    fun generatePdfSummary(pdfTitle: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val prompt = "Create a summary sheet of the study material titled \"$pdfTitle\" covering key points, definitions, formulas, and 3 review flashcard questions."
            val response = GeminiService.getStudyResponse(prompt, "You are StudyMate AI Document Analyst.")
            _aiResponseText.value = response
            _isAiLoading.value = false
            addXp(25)
            _mascotMessage.value = "Done! I analyzed the PDF structure perfectly."
        }
    }

    fun generateMindmapFromPdf(pdfTitle: String, annotation: String = "") {
        _isAiLoading.value = true
        _mascotExpression.value = MascotExpression.Studious
        _mascotMessage.value = "Analyzing Document \"$pdfTitle\" and weaving an interactive visual mind map graph... 🕸️🧠"

        viewModelScope.launch {
            val prompt = """
                You are a expert visual learning assistant.
                Generate a dynamic hierarchical mind map centered on the syllabus or study document: "$pdfTitle".
                Additional context notes or highlights: "$annotation"
                
                Return ONLY a JSON tree structure representing the mind map in the exact format:
                {
                  "label": "Document Name/Goal",
                  "children": [
                    {
                      "label": "Main Topic A",
                      "children": [
                        { "label": "Detail A1" },
                        { "label": "Detail A2" }
                      ]
                    },
                    {
                      "label": "Main Topic B",
                      "children": [
                        { "label": "Detail B1" }
                      ]
                    }
                  ]
                }
                Verify there are no markdown quotes, enclosed in ```json symbols, or outer strings.
            """.trimIndent()
            val response = GeminiService.getStudyResponse(prompt, "Output raw JSON payload matching structural outline.")
            _isAiLoading.value = false
            try {
                val cleaned = response.substringAfter("{").substringBeforeLast("}")
                val tree = buildMindmapFromJson(JSONObject("{$cleaned}"))
                _activeMindmap.value = tree
                _aiResponseText.value = "Document Mind Map generated! Explore branches below."
                _mascotMessage.value = "Great! Document Mind Map is fully loaded."
            } catch (e: Exception) {
                e.printStackTrace()
                _aiResponseText.value = "Parsed raw JSON tree failed. Full text:\n\n$response"
                _mascotMessage.value = "Document mind map generation disconnected or timed out. Try again!"
            }
        }
    }

    // --- Study Mode / Pomodoro Timer Helpers ---
    fun toggleTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
        _mascotExpression.value = if (_isTimerRunning.value) MascotExpression.Studious else MascotExpression.Happy
        _mascotMessage.value = if (_isTimerRunning.value) {
            "Focus mode ON! No notifications, just pure genius power! Let's work! ⏱️🔥"
        } else {
            "Breather session? Don't worry, I will hold the stopwatch."
        }
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        _pomodoroSecondsLeft.value = 1500
        _mascotExpression.value = MascotExpression.Happy
        _mascotMessage.value = "Timer reset. Ready to go when you are!"
    }

    private fun completeFocusSession() {
        val user = _currentUser.value ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        viewModelScope.launch {
            dao.insertFocusSession(
                FocusSessionEntity(
                    username = user.username,
                    date = todayStr,
                    durationSeconds = initialPomodoroSeconds,
                    xpEarned = 50
                )
            )

            // Update daily user goals metrics
            val currentDailySeconds = user.dailyStudySeconds + initialPomodoroSeconds
            val updated = user.copy(
                dailyStudySeconds = currentDailySeconds,
                totalXp = user.totalXp + 50
            )
            dao.updateUser(updated)
            _currentUser.value = updated

            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 20 || currentHour < 5

            awardBadge("productivity_hero")
            if (isNightTime) {
                awardBadge("night_owl")
                _mascotExpression.value = MascotExpression.Celebrating
                _mascotMessage.value = "WOW! Complete 25 Night Focus Minutes! Awarded +50 XP study hero & unlocked Night Owl badge! 🌕🦉🌌 Keep up the incredible night study!"
            } else {
                _mascotExpression.value = MascotExpression.Celebrating
                _mascotMessage.value = "WOW! Complete 25 Focus Minutes! Awarded +50 XP study hero! Streak maintained! 🦉🏆"
            }
        }
    }

    // Update study goals configuration
    fun updateStudyGoal(hours: Float) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(studyGoalsHours = hours)
            dao.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun addLongTermGoal(title: String, type: String, targetHours: Float) {
        val username = currentUser.value?.username ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insertLongTermGoal(
                com.example.data.LongTermGoalEntity(
                    username = username,
                    title = title,
                    type = type,
                    targetHours = targetHours,
                    isCompleted = false
                )
            )
            _mascotExpression.value = MascotExpression.Celebrating
            _mascotMessage.value = "Awesome! Created a new $type Study Goal: \"$title\"! Aim high, study smart! 🦉🚀"
        }
    }

    fun toggleLongTermGoal(goal: com.example.data.LongTermGoalEntity) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val nextCompleted = !goal.isCompleted
            dao.updateLongTermGoal(goal.copy(isCompleted = nextCompleted))
            if (nextCompleted) {
                addXp(100)
                awardBadge("long_term_badge")
                _mascotExpression.value = MascotExpression.Celebrating
                _mascotMessage.value = "SPECTACULAR! Completed long-term study goal \"${goal.title}\"! Awarded +100 XP & the LONG-TERM WARRIOR badge! 🦉🔥🏆"
            }
        }
    }

    fun deleteLongTermGoal(id: Int) {
        viewModelScope.launch {
            dao.deleteLongTermGoal(id)
        }
    }

    // --- Checklists & Task Actions ---
    fun addTask(title: String) {
        val username = currentUser.value?.username ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insertTask(TaskEntity(username = username, title = title, isCompleted = false))
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            dao.deleteTask(taskId)
        }
    }

    // --- Timetable & Reminders Actions ---
    fun addTimetableEntry(dayOfWeek: String, subject: String, time: String) {
        val username = currentUser.value?.username ?: return
        if (subject.isBlank() || time.isBlank()) return
        viewModelScope.launch {
            dao.insertTimetable(
                TimetableEntity(
                    username = username,
                    dayOfWeek = dayOfWeek,
                    subject = subject,
                    time = time,
                    isReminderEnabled = true
                )
            )
        }
    }

    fun deleteTimetableEntry(id: Int) {
        viewModelScope.launch {
            dao.deleteTimetable(id)
        }
    }

    fun toggleTimetableReminder(entry: TimetableEntity) {
        viewModelScope.launch {
            dao.updateTimetable(entry.copy(isReminderEnabled = !entry.isReminderEnabled))
        }
    }

    fun dismissActiveReminder() {
        _activeReminder.value = null
    }

    fun notifyMascot(message: String, expression: MascotExpression) {
        _mascotMessage.value = message
        _mascotExpression.value = expression
    }

    // Update messages when switching screens
    private fun updateCompanionTip() {
        val listTips = listOf(
            "Write a note about what you learned today! Rewriting embeds knowledge +20XP. 📝",
            "Try generating MCQs on Recursion to test your speed! ⚡",
            "Focus for 25 minutes! Silences notification noise. Let's work! ⏱️",
            "Bookmark difficult PDF pages so we can summarize them with AI later! 📑",
            "Maintain your daily study streak! Missing days resets fire multiplier. 🔥"
        )
        _mascotMessage.value = listTips.random()
        _mascotExpression.value = MascotExpression.Happy
    }
}
