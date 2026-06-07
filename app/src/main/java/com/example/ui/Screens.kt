package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.provider.OpenableColumns
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.app.Activity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.data.*
import com.example.ui.components.*
import com.example.viewmodel.MainActivityViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import org.json.JSONArray
import org.json.JSONObject
import com.example.viewmodel.QuizQuestion
import com.example.viewmodel.StudyFlashcard
import com.example.viewmodel.MindmapNode
import com.example.ui.theme.LocalIsDarkMode
import androidx.compose.material3.LocalTextStyle

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current
) {
    val isDark = LocalIsDarkMode.current
    val finalColor = if (isDark) {
        when (color) {
            Color(0xFF333333), Color(0xFF3C3C3C), Color(0xFF141C24), Color(0xFF1E293B), Color(0xFF475569) -> Color(0xFFF1F5F9)
            Color(0xFF555555), Color(0xFF777777), Color(0xFF888888), Color(0xFF64748B) -> Color(0xFF94A3B8)
            Color.Unspecified -> {
                if (style.color == Color.Unspecified || style.color == Color(0xFF3C3C3C) || style.color == Color(0xFF333333) || style.color == Color(0xFF141C24)) {
                    Color(0xFFF1F5F9)
                } else {
                    style.color
                }
            }
            else -> color
        }
    } else {
        color
    }
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = finalColor,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

// Enums for AI chat tools tabs
enum class AIChatTool(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Chat("Interactive Chat 💬", Icons.Default.Forum),
    Explain("Explain Concept", Icons.Default.School),
    Summarize("Summarize", Icons.Default.Menu),
    Quiz("Generate Quiz", Icons.Default.QuestionAnswer),
    ELI5("ELI5", Icons.Default.ChildCare),
    Flashcard("Flashcards", Icons.Default.ContentCopy),
    StudyPlan("Study Plan", Icons.Default.CalendarMonth),
    Rewrite("Rewrite Pro", Icons.Default.Edit),
    Bullets("Bullets Converter", Icons.Default.List),
    Interview("Interview prep", Icons.Default.ContactPage),
    Translate("Translate", Icons.Default.Translate),
    ShortNotes("Short Notes", Icons.Default.NoteAlt),
    MindMap("Mind Map", Icons.Default.Hub)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMateAppContent(viewModel: MainActivityViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val unlockedBadge by viewModel.recentUnlockedBadge.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF7F9FB)
    val surfaceColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textMainColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3C3C3C)
    val indicatorColor = if (isDark) Color(0xFF334155) else Color(0xFFF1FDF0)

    if (currentScreen != StudyMateScreen.Splash && 
        currentScreen != StudyMateScreen.Login && 
        currentScreen != StudyMateScreen.Register &&
        currentScreen != StudyMateScreen.Dashboard) {
        androidx.activity.compose.BackHandler {
            when (currentScreen) {
                StudyMateScreen.Pdfs -> {
                    val selectedPdf = viewModel.selectedPdf.value
                    val activeFolder = viewModel.activePdfFolder.value
                    if (selectedPdf != null) {
                        viewModel.selectPdf(null)
                    } else if (activeFolder != null) {
                        viewModel.setActivePdfFolder(null)
                    } else {
                        viewModel.navigateTo(StudyMateScreen.Dashboard)
                    }
                }
                StudyMateScreen.Notes -> {
                    val selectedNote = viewModel.selectedNote.value
                    if (selectedNote != null) {
                        viewModel.selectNote(null)
                    } else {
                        viewModel.navigateTo(StudyMateScreen.Dashboard)
                    }
                }
                else -> {
                    viewModel.navigateTo(StudyMateScreen.Dashboard)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != StudyMateScreen.Splash && 
                currentScreen != StudyMateScreen.Login && 
                currentScreen != StudyMateScreen.Register) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.School, contentDescription = "School Logo", tint = Color(0xFF58CC02), modifier = Modifier.size(24.dp))
                            Text("StudyMate AI", fontWeight = FontWeight.Black, color = textMainColor, fontSize = 20.sp)
                        }
                    },
                    navigationIcon = {
                        if (currentScreen != StudyMateScreen.Dashboard) {
                            IconButton(onClick = {
                                when (currentScreen) {
                                    StudyMateScreen.Pdfs -> {
                                        val selectedPdf = viewModel.selectedPdf.value
                                        val activeFolder = viewModel.activePdfFolder.value
                                        if (selectedPdf != null) {
                                            viewModel.selectPdf(null)
                                        } else if (activeFolder != null) {
                                            viewModel.setActivePdfFolder(null)
                                        } else {
                                            viewModel.navigateTo(StudyMateScreen.Dashboard)
                                        }
                                    }
                                    StudyMateScreen.Notes -> {
                                        val selectedNote = viewModel.selectedNote.value
                                        if (selectedNote != null) {
                                            viewModel.selectNote(null)
                                        } else {
                                            viewModel.navigateTo(StudyMateScreen.Dashboard)
                                        }
                                    }
                                    else -> {
                                        viewModel.navigateTo(StudyMateScreen.Dashboard)
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to Dashboard", tint = if (isDark) Color.White else Color(0xFF777777))
                            }
                        }
                    },
                    actions = {
                        currentUser?.let { user ->
                            StreakFireCounter(days = user.currentStreak)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = surfaceColor,
                        titleContentColor = textMainColor
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != StudyMateScreen.Splash && 
                currentScreen != StudyMateScreen.Login && 
                currentScreen != StudyMateScreen.Register) {
                NavigationBar(containerColor = surfaceColor) {
                    val screens = listOf(
                        Triple(StudyMateScreen.Dashboard, "Home", Icons.Default.Home),
                        Triple(StudyMateScreen.AIChat, "AI Tools", Icons.Default.Psychology),
                        Triple(StudyMateScreen.Notes, "Notebook", Icons.Default.NoteAlt),
                        Triple(StudyMateScreen.Pdfs, "PDF Vault", Icons.Default.PictureAsPdf),
                        Triple(StudyMateScreen.Achievements, "Badges", Icons.Default.EmojiEvents)
                    )
                    screens.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF58CC02),
                                selectedTextColor = Color(0xFF58CC02),
                                indicatorColor = indicatorColor,
                                unselectedIconColor = if (isDark) Color(0xFF64748B) else Color(0xFF888888),
                                unselectedTextColor = if (isDark) Color(0xFF64748B) else Color(0xFF888888)
                             )
                        )
                    }
                }
            }
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                StudyMateScreen.Splash -> SplashScreen(viewModel)
                StudyMateScreen.Login -> LoginScreen(viewModel)
                StudyMateScreen.Register -> RegisterScreen(viewModel)
                StudyMateScreen.QuickUnlock -> QuickUnlockScreen(viewModel)
                StudyMateScreen.Dashboard -> DashboardScreen(viewModel)
                StudyMateScreen.AIChat -> AIChatScreen(viewModel)
                StudyMateScreen.Notes -> NotesScreen(viewModel)
                StudyMateScreen.Pdfs -> PdfsScreen(viewModel)
                StudyMateScreen.MindMaps -> MindMapsScreen(viewModel)
                StudyMateScreen.Achievements -> AchievementsScreen(viewModel)
                StudyMateScreen.FocusMode -> FocusModeScreen(viewModel)
                StudyMateScreen.History -> HistoryScreen(viewModel)
                StudyMateScreen.Settings -> SettingsScreen(viewModel)
                StudyMateScreen.Planner -> StudyPlannerScreen(viewModel)
                StudyMateScreen.CanvasDraw -> CanvasDrawScreen(viewModel)
            }

            // Timetable Reminder Alarms dialogue overlay
            val activeReminder by viewModel.activeReminder.collectAsState()
            activeReminder?.let { alarm ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissActiveReminder() },
                    confirmButton = {
                        DuolingoButton(
                            text = "START STUDY SESSION ✍️",
                            onClick = {
                                viewModel.dismissActiveReminder()
                                viewModel.navigateTo(StudyMateScreen.FocusMode)
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissActiveReminder() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text("CLOSE ALARM", fontWeight = FontWeight.Bold, color = Color(0xFFFF4B4B))
                        }
                    },
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "TIMETABLE REMINDER! 🦉",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color(0xFFFF4B4B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StudyMateOwl(expression = MascotExpression.Celebrating, modifier = Modifier.size(110.dp))
                            Text(
                                text = "Your scheduled study/lecture hour is here!",
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = alarm.subject.uppercase(),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF141C24),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Weekly Schedule: ${alarm.dayOfWeek} at ${alarm.time}",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF777777),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // 13. Congratulations / Badge Unlocked Celebrate Dialog popup
            unlockedBadge?.let { badgeId ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissBadgeCelebration() },
                    confirmButton = {
                        DuolingoButton(
                            text = "AWESOME!",
                            onClick = { viewModel.dismissBadgeCelebration() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    },
                    title = {
                        Text(
                            "NEW ACHIEVEMENT UNLOCKED!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFFFF9600),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StudyMateOwl(expression = MascotExpression.Celebrating, modifier = Modifier.size(130.dp))
                            Text(
                                "You earned the \"${badgeId.replace("_", " ").uppercase()}\" badge!",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF3C3C3C),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Your dedication to study gets rewarded. Keep the streak fire burning high! 🔥",
                                fontSize = 13.sp,
                                color = Color(0xFF777777),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

// 1. Splash Screen
@Composable
fun SplashScreen(viewModel: MainActivityViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StudyMateOwl(expression = MascotExpression.Happy, modifier = Modifier.size(180.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "StudyMate AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF58CC02)
        )
        Text(
            text = "Your playful AI Study Companion",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF777777),
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        DuolingoButton(
            text = "GET STARTED",
            onClick = { viewModel.navigateTo(StudyMateScreen.Login) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )
    }
}

// 2. Login Screen
@Composable
fun LoginScreen(viewModel: MainActivityViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lastUser = viewModel.lastUsername

    // Simulated lock dialogs
    var showSimulatedFingerprint by remember { mutableStateOf(false) }
    var showPinPad by remember { mutableStateOf(false) }
    var interactivePin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("Enter 4-digit Screen Lock PIN or Passcode") }

    val keyguardManager = remember { context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    val isDeviceSecure = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        keyguardManager.isDeviceSecure
    } else {
        keyguardManager.isKeyguardSecure
    }

    val systemLockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loginByPhoneLock { success, msg ->
                if (!success) {
                    errorMsg = msg
                }
            }
        } else {
            errorMsg = "Authentication canceled."
        }
    }

    fun triggerSystemLock() {
        if (lastUser == null) {
            errorMsg = "No previous user session found. Please login normally first!"
            return
        }
        if (isDeviceSecure && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "Unlock StudyMate",
                "Please verify your pattern, pin or biometric lock screen."
            )
            if (intent != null) {
                systemLockLauncher.launch(intent)
            } else {
                showSimulatedFingerprint = true
            }
        } else {
            showSimulatedFingerprint = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StudyMateOwl(expression = MascotExpression.Encouraging, modifier = Modifier.size(130.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OwlSpeechBubble(text = "Welcome back! Login to save your streak and XP progress! 🦉")

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = Color(0xFFFF4B4B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DuolingoButton(
            text = "LOGIN",
            onClick = {
                viewModel.login(username, password) { success, msg ->
                    if (!success) {
                        errorMsg = msg
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone Lock / Forgot Lock Alternative Option
        if (lastUser != null) {
            DuolingoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { triggerSystemLock() },
                color = Color(0xFFF0FDF4),
                borderColor = Color(0xFF22C55E)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Forgot Password?",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "FORGOT PASSWORD?",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            "Tap to bypass via Phone System Lock / PIN 🔓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        TextButton(onClick = { viewModel.navigateTo(StudyMateScreen.Register) }) {
            Text(
                "Don't have an account? REGISTER",
                color = Color(0xFF1CB0F6),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }

    // --- DIALOGS FOR SECURITY GRAPHICS FINGERPRINT SCANNING ---
    if (showSimulatedFingerprint) {
        var isScanning by remember { mutableStateOf(false) }
        var scanStatus by remember { mutableStateOf("Touch and hold scanner below to begin fingerprint match") }
        var scanProgress by remember { mutableStateOf(0f) }

        AlertDialog(
            onDismissRequest = { showSimulatedFingerprint = false },
            title = {
                Text(
                    "Simulated Phone Security Scanner",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        scanStatus,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )

                    if (isScanning) {
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF1CB0F6),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    // Tactile interactive button simulating biometric scanner
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                color = if (isScanning) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                                shape = CircleShape
                            )
                            .border(width = 3.dp, color = if (isScanning) Color(0xFF0EA5E9) else Color(0xFF94A3B8), shape = CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        try {
                                            isScanning = true
                                            scanStatus = "Scanning Fingerprint... Keep holding!"
                                            for (i in 1..20) {
                                                kotlinx.coroutines.delay(100)
                                                scanProgress = i / 20f
                                            }
                                            scanStatus = "Identity Matched! 🎉 Access Granted."
                                            kotlinx.coroutines.delay(500)
                                            viewModel.loginByPhoneLock { success, msg ->
                                                if (success) {
                                                    showSimulatedFingerprint = false
                                                } else {
                                                    scanStatus = "Error: $msg"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Handle cancellation
                                        } finally {
                                            isScanning = false
                                            scanProgress = 0f
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Simulated Biometrics Scanner",
                            tint = if (isScanning) Color(0xFF0EA5E9) else Color(0xFF64748B),
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Text(
                        "HOLD SCANNER FOR 2 SECONDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimulatedFingerprint = false }) {
                    Text("USE PASSWORD INSTEAD", fontWeight = FontWeight.Bold, color = Color(0xFF1CB0F6))
                }
            }
        )
    }

    if (showPinPad) {
        AlertDialog(
            onDismissRequest = { showPinPad = false },
            title = {
                Text(
                    "Simulated Device Lock PIN bypass",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        pinMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    // Display Dots for current entered length
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 1..4) {
                            val active = interactivePin.length >= i
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (active) Color(0xFF9333EA) else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    )
                                    .border(width = 1.dp, color = Color(0xFFCBD5E1), shape = CircleShape)
                            )
                        }
                    }

                    // Keypad grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Clear", "0", "Back")
                        ).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 44.dp)
                                            .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                                            .clickable {
                                                when (key) {
                                                    "Clear" -> interactivePin = ""
                                                    "Back" -> if (interactivePin.isNotEmpty()) interactivePin = interactivePin.dropLast(1)
                                                    else -> {
                                                        if (interactivePin.length < 4) {
                                                            interactivePin += key
                                                            if (interactivePin.length == 4) {
                                                                pinMessage = "PIN Verified! Access Granted."
                                                                viewModel.loginByPhoneLock { success, msg ->
                                                                    if (success) {
                                                                        showPinPad = false
                                                                    } else {
                                                                        pinMessage = "Error: $msg"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(key, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// 3. Register Screen
@Composable
fun RegisterScreen(viewModel: MainActivityViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StudyMateOwl(expression = MascotExpression.Happy, modifier = Modifier.size(135.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OwlSpeechBubble(text = "Hello! Join the team to begin unlocking badges! Let's register! 🦉")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = Color(0xFFFF4B4B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DuolingoButton(
            text = "REGISTER NOW",
            color = Color(0xFF1CB0F6),
            shadowColor = Color(0xFF108CBF),
            onClick = {
                if (password != confirmPassword) {
                    errorMsg = "Passwords do not match!"
                    return@DuolingoButton
                }
                viewModel.register(username, password) { success, msg ->
                    if (!success) {
                        errorMsg = msg
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.navigateTo(StudyMateScreen.Login) }) {
            Text(
                "Already registered? LOGIN",
                color = Color(0xFF58CC02),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// 4. Dashboard Screen
@Composable
fun DashboardScreen(viewModel: MainActivityViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val mascotMsg by viewModel.mascotMessage.collectAsState()
    val mascotExp by viewModel.mascotExpression.collectAsState()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val levelMax = 100

    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF7F9FB)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textMain = if (isDark) Color(0xFFF1F5F9) else Color(0xFF141C24)
    val textSecond = if (isDark) Color(0xFF94A3B8) else Color(0xFF777777)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor) // Cozy Bento soft background slate
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown menu button and chat history shortcut at the top left corner of Home/Dashboard Page
        item {
            var homeDropdownExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    DuolingoButton(
                        text = "AI TOOLBOX ▾",
                        onClick = { homeDropdownExpanded = true },
                        color = Color(0xFFF1FDF0),
                        shadowColor = Color(0xFFD2E8CE),
                        contentColor = Color(0xFF58CC02)
                    )

                    DropdownMenu(
                        expanded = homeDropdownExpanded,
                        onDismissRequest = { homeDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        AIChatTool.values().forEach { tool ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = null,
                                            tint = Color(0xFF58CC02),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = tool.label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setSelectedChatTool(tool)
                                    viewModel.navigateTo(StudyMateScreen.AIChat)
                                    homeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                DuolingoButton(
                    text = "CHAT HISTORY ⏳",
                    onClick = { viewModel.navigateTo(StudyMateScreen.History) },
                    color = Color(0xFFE0F2FE),
                    shadowColor = Color(0xFFBAE6FD),
                    contentColor = Color(0xFF0284C7)
                )
            }
        }

        // Welcoming Card Bento Block
        item {
            DuolingoCard(
                color = cardBg,
                borderColor = borderCol,
                shadowColor = if (isDark) Color(0x30000000) else Color(0x10000000),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "WELCOME BACK,",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8E79FF), // Purple accent text
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            user?.username?.uppercase() ?: "STUDENT",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = textMain
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StreakProgressBar(
                            xp = user?.totalXp ?: 10,
                            levelMax = levelMax
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(if (isDark) Color(0xFF334155) else Color(0xFFFFF4E5), shape = RoundedCornerShape(20.dp))
                            .border(2.dp, Color(0xFFFF9600), shape = RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Level", tint = Color(0xFFFF9600), modifier = Modifier.size(32.dp))
                            Text("LVL ${((user?.totalXp ?: 10) / 100) + 1}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF9600))
                        }
                    }
                }
            }
        }

        // 1. NIGHT OWL APPRECIATION SPECIAL BLOCK
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val isNightTime = currentHour >= 20 || currentHour < 5 // 8 PM to 5 AM
        if (isNightTime) {
            item {
                val lastClaim = viewModel.lastNightOwlClaimDate
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val todayStr = dateFormat.format(java.util.Date())
                val isClaimed = lastClaim == todayStr

                DuolingoCard(
                    color = Color(0xFF0F172A),
                    borderColor = Color(0xFFFEF3C7),
                    shadowColor = Color(0xFFFDE68A),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star Night Owl",
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "MOONLIGHT CO-PILOT APPRECIATION 🌕✨",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFEF3C7)
                            )
                        }

                        Text(
                            "Thank you for committing to late night study sessions! Quiet night sprints build ultimate concentration. Stay safe and nourished! 🦉🌌",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
                        )

                        DuolingoButton(
                            text = if (isClaimed) "NIGHT OWL REWARD CLAIMED (+20 XP) ⭐" else "CLAIM STARLIGHT SUPPORTER XP (+20 XP) 🎁",
                            onClick = { viewModel.claimNightOwlXpBoost() },
                            color = if (isClaimed) Color(0xFF334155) else Color(0xFFFEF3C7),
                            shadowColor = if (isClaimed) Color(0xFF1E293B) else Color(0xFFFDE68A),
                            contentColor = if (isClaimed) Color(0xFF94A3B8) else Color(0xFF78350F),
                            enabled = !isClaimed,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 2. CHOOSE COZY DARK MODE CONFIGURATION CARD
        item {
            DuolingoCard(
                color = cardBg,
                borderColor = borderCol,
                shadowColor = if (isDark) Color(0x30000000) else Color(0x06000000),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isDark) Color(0xFF334155) else Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Theme Mode Icon",
                                tint = if (isDark) Color(0xFFFCD34D) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "OPTIONAL DARK MODE 🌌",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = textMain
                            )
                            Text(
                                "Study comfortably in low light environments.",
                                fontSize = 11.sp,
                                color = textSecond
                            )
                        }
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF58CC02),
                            checkedTrackColor = Color(0xFFE2FDE0)
                        )
                    )
                }
            }
        }

        // Active AI Buddy Companion dialog advice visual
        item {
            DuolingoCard(
                color = Color.White,
                borderColor = Color(0xFFE2E8F0),
                shadowColor = Color(0x06000000),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudyMateOwl(expression = mascotExp, modifier = Modifier.size(90.dp)) {
                        // Tap on buddy companion owl triggers next advice dialog and awards casual XP
                        viewModel.addXp(1)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "STUDYBUDDY ADVISORY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF58CC02),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mascotMsg,
                            color = Color(0xFF333333),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Core Bento Grid Section Label
        item {
            Text(
                "AI CORE TOOLS & ROOMS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF64748B), // elegant slate text
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 2.dp)
            )
        }

        // 2x2 Bento Grid: Row 1
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Explain Concept Bento
                DuolingoCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.setSelectedChatTool(AIChatTool.Explain)
                            viewModel.navigateTo(StudyMateScreen.AIChat)
                        },
                    color = Color(0xFFE5F6FF),
                    borderColor = Color(0xFF1CB0F6),
                    shadowColor = Color(0xFF1899D6),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1CB0F6), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.School, contentDescription = "Explain", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "EXPLAIN\nCONCEPT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1899D6),
                            lineHeight = 18.sp
                        )
                    }
                }

                // Summarize Text Bento
                DuolingoCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.setSelectedChatTool(AIChatTool.Summarize)
                            viewModel.navigateTo(StudyMateScreen.AIChat)
                        },
                    color = Color(0xFFF2F0FF),
                    borderColor = Color(0xFF8E79FF),
                    shadowColor = Color(0xFF745CFA),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF8E79FF), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Summarize", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "SUMMARIZE\nACADEMIC",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF745CFA),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 2x2 Bento Grid: Row 2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Flashcards Bento
                DuolingoCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.setSelectedChatTool(AIChatTool.Flashcard)
                            viewModel.navigateTo(StudyMateScreen.AIChat)
                        },
                    color = Color(0xFFFFF4E5),
                    borderColor = Color(0xFFFF9600),
                    shadowColor = Color(0xFFE97600),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFF9600), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Flashcards", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "ACTIVE\nFLASHCARDS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE97600),
                            lineHeight = 18.sp
                        )
                    }
                }

                // Mindmaps Bento
                DuolingoCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.setSelectedChatTool(AIChatTool.MindMap)
                            viewModel.navigateTo(StudyMateScreen.AIChat)
                        },
                    color = Color(0xFFE5FFE9),
                    borderColor = Color(0xFF2EB040),
                    shadowColor = Color(0xFF209431),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF2EB040), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Hub, contentDescription = "Mindmaps", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "MIND MAP\nCAGES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF209431),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Interactive AI Chat Buddy option and Chat History dropdown
        item {
            val conversations by viewModel.conversations.collectAsState()
            var homeHistoryDropdownExpanded by remember { mutableStateOf(false) }
            var selectedHomeHistoryLog by remember { mutableStateOf<com.example.data.ChatConversationEntity?>(null) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DuolingoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setSelectedChatTool(AIChatTool.Chat)
                            viewModel.navigateTo(StudyMateScreen.AIChat)
                        },
                    color = Color(0xFFF1FDF0), // Delightful soft light green
                    borderColor = Color(0xFF58CC02), // Duolingo green border
                    shadowColor = Color(0xFF46A302), // Duolingo green shadow
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF58CC02), shape = RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = "Interactive chat",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INTERACTIVE AI CHAT BUDDY 💬",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF3B9B01)
                            )
                            Text(
                                text = "Engage in intelligent back-and-forth academic discussions with your AI companion.",
                                fontSize = 12.sp,
                                color = Color(0xFF46A302)
                            )
                        }
                    }
                }

                // Chat history opening button with dropdown to show previous chats
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DuolingoButton(
                        text = "CHAT HISTORY ⏳ ▾",
                        onClick = { homeHistoryDropdownExpanded = true },
                        color = Color(0xFFE0F2FE),
                        shadowColor = Color(0xFFBAE6FD),
                        contentColor = Color(0xFF0284C7),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = homeHistoryDropdownExpanded,
                        onDismissRequest = { homeHistoryDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        val chatLogs = remember(conversations) {
                            conversations.filter { it.toolType == "Chat" }
                        }

                        if (chatLogs.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "(No past conversations recorded)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                },
                                onClick = { homeHistoryDropdownExpanded = false }
                            )
                        } else {
                            chatLogs.forEach { log ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "💬 " + if (log.prompt.length > 32) log.prompt.take(32) + "..." else log.prompt,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF334155),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteConversationById(log.id)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Log",
                                                    tint = Color(0xFFFF4B4B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedHomeHistoryLog = log
                                        homeHistoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Beautiful Modal Popup Dialog for selected historical log on Home Screen
            selectedHomeHistoryLog?.let { log ->
                val scrollState = rememberScrollState()
                AlertDialog(
                    onDismissRequest = { selectedHomeHistoryLog = null },
                    title = {
                        Text(
                            text = "HISTORICAL CHAT LOG #${log.id}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .heightIn(max = 350.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROMPT:",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(log.prompt))
                                        viewModel.notifyMascot("Copied prompt text to clipboard! 🦉📝", MascotExpression.Celebrating)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Prompt",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text("COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    }
                                }
                            }
                            Text(
                                text = log.prompt,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0F9FF), shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI STUDYBUDDY RESPONSE:",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFF58CC02)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(log.response))
                                        viewModel.notifyMascot("Copied response text to clipboard! 🦉📝", MascotExpression.Celebrating)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Response",
                                            tint = Color(0xFF58CC02),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text("COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF58CC02))
                                    }
                                }
                            }
                            Text(
                                text = log.response,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1FDF0), shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            DuolingoButton(
                                text = "COPY Q&A 📋",
                                onClick = {
                                    val fullText = "PROMPT:\n${log.prompt}\n\nRESPONSE:\n${log.response}"
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(fullText))
                                    viewModel.notifyMascot("Successfully copied entire conversation Q&A to clipboard! 🦉📋", MascotExpression.Celebrating)
                                },
                                color = Color(0xFFF3E8FF),
                                shadowColor = Color(0xFFD8B4FE),
                                contentColor = Color(0xFF9333EA),
                                modifier = Modifier.weight(1.3f)
                            )
                            DuolingoButton(
                                text = "CLOSE LOG",
                                onClick = { selectedHomeHistoryLog = null },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(26.dp)
                )
            }
        }

        // Productivity & Vault Grids (Third Row of Bento)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Focus Mode Bento Box
                DuolingoCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(StudyMateScreen.FocusMode) },
                    color = Color(0xFFFFF2F2),
                    borderColor = Color(0xFFFF4B4B),
                    shadowColor = Color(0xFFD03B3B),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFF4B4B), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Focus", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "POMODORO\nROOMS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD03B3B),
                            lineHeight = 18.sp
                        )
                    }
                }

                // Notebook Notes Bento Box
                DuolingoCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(StudyMateScreen.Notes) },
                    color = Color(0xFFE5FBF8),
                    borderColor = Color(0xFF00C3A5),
                    shadowColor = Color(0xFF009C84),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF00C3A5), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.NoteAlt, contentDescription = "Notes", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "NOTEBOOK\nVAULTS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF009C84),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Daily Planner & Timetable Bento Row
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DuolingoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(StudyMateScreen.Planner) },
                color = Color(0xFFFFFBEB), // Soft dynamic amber
                borderColor = Color(0xFFFF9600), // Duolingo Orange border
                shadowColor = Color(0xFFE97600), // Duolingo Orange shadow
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFF9600), shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Daily Planner", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "DAILY PLANNER & TIMETABLE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            "Create checklists with checkboxes and design full study timetables with real-time owl alarm notifications and system reminders.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        // Dedicated Canvas Drawing Sandbox Bento Row
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DuolingoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(StudyMateScreen.CanvasDraw) },
                color = Color(0xFFF0FDF4), // Soft green background
                borderColor = Color(0xFF22C55E), // Green border
                shadowColor = Color(0xFF16A34A), // Green shadow
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF22C55E), shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Canvas Drawing Sandbox", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "CANVAS SKETCH SANDBOX",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            "Draw chemistry structures, physics graphs, calculus waves on a clean blackboard sketcher and save drawings to portfolio gallery.",
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }
        }

        // Interactive Scientific Formulas & Symbols Bento Box
        item {
            var showReferenceDialog by remember { mutableStateOf(false) }
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            var toastMessage by remember { mutableStateOf<String?>(null) }

            Spacer(modifier = Modifier.height(14.dp))
            
            DuolingoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        toastMessage = null
                        showReferenceDialog = true 
                    },
                color = Color(0xFFF3E8FF),
                borderColor = Color(0xFFC084FC),
                shadowColor = Color(0xFFA855F7),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF9333EA), shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Functions, contentDescription = "Formulas", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "FORMULA & SYMBOLS VAULT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7E22CE)
                        )
                        Text(
                            "Quickly copy physics, chemistry, and math formulas/equations to clipboards.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B21A8)
                        )
                    }
                }
            }

            if (showReferenceDialog) {
                AlertDialog(
                    onDismissRequest = { showReferenceDialog = false },
                    confirmButton = {
                        DuolingoButton(
                            text = "DONE",
                            onClick = { showReferenceDialog = false },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    },
                    title = {
                        Text(
                            "SCIENTIFIC FORMULA DIRECTORY",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF7E22CE)
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Tap any symbol or equation to copy it instantly to your clipboard, ready to paste in your notes or chat!",
                                fontSize = 12.sp,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Center
                            )
                            
                            ScientificSymbolTray(
                                onSymbolSelected = { char ->
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(char))
                                    toastMessage = "Copied to clipboard: $char"
                                }
                            )

                            toastMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF58CC02),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }

        // Secondary Settings Link & Action Row
        item {
            Spacer(modifier = Modifier.height(14.dp))
            DuolingoButton(
                text = "STUDY PARAMETERS & GOALS",
                color = Color.White,
                shadowColor = Color(0xFFCBD5E1),
                contentColor = Color(0xFF1CB0F6),
                onClick = { viewModel.navigateTo(StudyMateScreen.Settings) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            DuolingoButton(
                text = "LOGOUT ACCOUNT",
                color = Color.White,
                shadowColor = Color(0xFFCBD5E1),
                contentColor = Color(0xFFFF4B4B),
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 5. AI Study Tools Chat Screen with different Tabs
@Composable
fun AIChatScreen(viewModel: MainActivityViewModel) {
    val selectedTool by viewModel.selectedChatTool.collectAsState()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var textInput by remember { mutableStateOf("") }
    val aiResponse by viewModel.aiResponseText.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()

    // Specific sub-tool states
    val activeQuiz by viewModel.activeQuiz.collectAsState()
    val activeCards by viewModel.activeFlashcards.collectAsState()
    val activeMindmap by viewModel.activeMindmap.collectAsState()

    var targetLanguage by remember { mutableStateOf("Spanish") }

    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF7F9FB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor) // Clean slate Bento background
            .padding(12.dp)
    ) {
        var dropdownExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown selection button placed precisely at the top left corner of the AI Tools view
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                DuolingoButton(
                    text = "${selectedTool.label.uppercase()} ▾",
                    onClick = { dropdownExpanded = true },
                    color = Color(0xFFF1FDF0),
                    shadowColor = Color(0xFFD2E8CE),
                    contentColor = Color(0xFF58CC02)
                )

                 val menuBgColor = Color.White
                val menuBorderColor = Color(0xFFE2E8F0)

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(menuBgColor)
                        .border(1.5.dp, menuBorderColor, RoundedCornerShape(16.dp))
                        .padding(vertical = 4.dp)
                ) {
                    AIChatTool.values().forEach { tool ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selectedTool == tool) {
                                                Color(0xFFF1FDF0)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = null,
                                        tint = if (selectedTool == tool) Color(0xFF58CC02) else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = tool.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTool == tool) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTool == tool) Color(0xFF58CC02) else Color(0xFF334155)
                                    )
                                }
                            },
                            onClick = {
                                viewModel.setSelectedChatTool(tool)
                                textInput = ""
                                dropdownExpanded = false
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick access chat history button
            DuolingoButton(
                text = "CHAT HISTORY ⏳",
                onClick = { viewModel.navigateTo(StudyMateScreen.History) },
                color = Color(0xFFE0F2FE),
                shadowColor = Color(0xFFBAE6FD),
                contentColor = Color(0xFF0284C7)
            )
        }

        if (selectedTool == AIChatTool.Chat) {
            val conversations by viewModel.conversations.collectAsState()
            var historyDropdownExpanded by remember { mutableStateOf(false) }
            var selectedHistoryLog by remember { mutableStateOf<com.example.data.ChatConversationEntity?>(null) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InteractiveChatCompanion(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )

                // Dropdown menu button to view previous chats
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DuolingoButton(
                        text = "VIEW PAST CONVERSATIONS 💬 ▾",
                        onClick = { historyDropdownExpanded = true },
                        color = Color(0xFFF1FDF0),
                        shadowColor = Color(0xFFD2E8CE),
                        contentColor = Color(0xFF58CC02),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = historyDropdownExpanded,
                        onDismissRequest = { historyDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        val chatLogs = remember(conversations) {
                            conversations.filter { it.toolType == "Chat" }
                        }

                        if (chatLogs.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "(No past conversations recorded)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                },
                                onClick = { historyDropdownExpanded = false }
                            )
                        } else {
                            chatLogs.forEach { log ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "💬 " + if (log.prompt.length > 32) log.prompt.take(32) + "..." else log.prompt,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF334155),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteConversationById(log.id)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Log",
                                                    tint = Color(0xFFFF4B4B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedHistoryLog = log
                                        historyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Beautiful Modal Popup Dialog for selected historical log
            selectedHistoryLog?.let { log ->
                val scrollState = rememberScrollState()
                AlertDialog(
                    onDismissRequest = { selectedHistoryLog = null },
                    title = {
                        Text(
                            text = "HISTORICAL CHAT LOG #${log.id}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .heightIn(max = 350.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROMPT:",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(log.prompt))
                                        viewModel.notifyMascot("Copied prompt text to clipboard! 🦉📝", MascotExpression.Celebrating)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Prompt",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text("COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    }
                                }
                            }
                            Text(
                                text = log.prompt,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0F9FF), shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI STUDYBUDDY RESPONSE:",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFF58CC02)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(log.response))
                                        viewModel.notifyMascot("Copied response text to clipboard! 🦉📝", MascotExpression.Celebrating)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Response",
                                            tint = Color(0xFF58CC02),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text("COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF58CC02))
                                    }
                                }
                            }
                            Text(
                                text = log.response,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1FDF0), shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            DuolingoButton(
                                text = "COPY Q&A 📋",
                                onClick = {
                                    val fullText = "PROMPT:\n${log.prompt}\n\nRESPONSE:\n${log.response}"
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(fullText))
                                    viewModel.notifyMascot("Successfully copied entire conversation Q&A to clipboard! 🦉📋", MascotExpression.Celebrating)
                                },
                                color = Color(0xFFF3E8FF),
                                shadowColor = Color(0xFFD8B4FE),
                                contentColor = Color(0xFF9333EA),
                                modifier = Modifier.weight(1.3f)
                            )
                            DuolingoButton(
                                text = "CLOSE LOG",
                                onClick = { selectedHistoryLog = null },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(26.dp)
                )
            }
        } else {
            // Scrollable Workspace area
            LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, shape = RoundedCornerShape(26.dp))
                .border(2.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(26.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Header / instructions of Active Tool
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = selectedTool.icon, contentDescription = selectedTool.label, tint = Color(0xFF58CC02), modifier = Modifier.size(28.dp))
                    Text(text = selectedTool.label.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                }
            }

            // Input panel triggers
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (selectedTool) {
                        AIChatTool.Translate -> {
                            // Target Language Selector Input
                            OutlinedTextField(
                                value = targetLanguage,
                                onValueChange = { targetLanguage = it },
                                label = { Text("Target Language (e.g. French, Japanese, Spanish)", color = Color.Black) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFF475569),
                                    unfocusedPlaceholderColor = Color(0xFF475569),
                                    focusedLabelColor = Color.Black,
                                    unfocusedLabelColor = Color(0xFF475569),
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                        }
                        else -> {}
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = when (selectedTool) {
                                    AIChatTool.Explain -> "Type a concept, e.g. Quantum Computing..."
                                    AIChatTool.Summarize -> "Paste long article text to condense..."
                                    AIChatTool.Quiz -> "Enter topic for flash quizzes, e.g. Mitochondria..."
                                    AIChatTool.ELI5 -> "Type tough concept to explain simply..."
                                    AIChatTool.Flashcard -> "Revision topic for automated cards..."
                                    AIChatTool.StudyPlan -> "What are you preparing for? (exam subject)..."
                                    else -> "Enter your instruction details..."
                                },
                                color = Color(0xFF475569)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color(0xFF475569),
                            unfocusedPlaceholderColor = Color(0xFF475569),
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            cursorColor = Color.Black
                        )
                    )

                    ScientificSymbolTray(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onSymbolSelected = { symbol ->
                            textInput += symbol
                        }
                    )

                    DuolingoButton(
                        text = "GENERATE RESPONSE",
                        onClick = {
                            when (selectedTool) {
                                AIChatTool.Chat -> {}
                                AIChatTool.Explain -> viewModel.explainConcept(textInput)
                                AIChatTool.Summarize -> viewModel.summarizeText(textInput)
                                AIChatTool.Quiz -> viewModel.generateQuiz(textInput)
                                AIChatTool.ELI5 -> viewModel.explainEli5(textInput)
                                AIChatTool.Flashcard -> viewModel.generateFlashcards(textInput)
                                AIChatTool.StudyPlan -> viewModel.generateStudyPlan(textInput)
                                AIChatTool.Rewrite -> viewModel.rewriteProfessionally(textInput)
                                AIChatTool.Bullets -> viewModel.convertToBullets(textInput)
                                AIChatTool.Interview -> viewModel.generateInterviewPrep(textInput)
                                AIChatTool.Translate -> viewModel.translateText(textInput, targetLanguage)
                                AIChatTool.ShortNotes -> viewModel.generateShortNotes(textInput)
                                AIChatTool.MindMap -> viewModel.generateMindmap(textInput)
                            }
                        },
                        enabled = textInput.isNotBlank() && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Load indicator typing
            if (isLoading) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(color = Color(0xFF58CC02))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("StudyMate buddy is studying your query...", fontSize = 12.sp, color = Color(0xFF777777), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results Output displays
            if (!isLoading) {
                // Flashcards Display
                if (selectedTool == AIChatTool.Flashcard && activeCards.isNotEmpty()) {
                    item {
                        Text("REVISION DECKS (Tap Card to FLIP 🔄)", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF555555), modifier = Modifier.padding(vertical = 4.dp))
                    }
                    itemsIndexed(activeCards) { id, card ->
                        val cardScrollState = rememberScrollState()
                        
                        DuolingoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 320.dp),
                            color = if (card.isFlipped) Color(0xFFF1FDF0) else Color(0xFFFFF4E5), // Playful amber/apricot front and lime-green back
                            borderColor = if (card.isFlipped) Color(0xFF58CC02) else Color(0xFFFF9600),
                            shadowColor = if (card.isFlipped) Color(0xFF46A302) else Color(0xFFE97600),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.flipFlashcard(id) }
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Side Badge Identifier Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (card.isFlipped) Color(0xFF58CC02) else Color(0xFFFF9600),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (card.isFlipped) "REVISION ANSWER / BACK 💡" else "REVISION QUESTION / FRONT ❓",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                    
                                    Text(
                                        text = "Card ${id + 1} of ${activeCards.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF888888)
                                    )
                                }

                                // Interactive scrollable area for long text
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(cardScrollState)
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (card.isFlipped) card.back else card.front,
                                        color = if (card.isFlipped) Color(0xFF1F2937) else Color(0xFF1E293B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                
                                // Decorative Tip Footer
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Flip Card",
                                        tint = if (card.isFlipped) Color(0xFF58CC02) else Color(0xFFFF9600),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TAP CARD TO FLIP OVER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (card.isFlipped) Color(0xFF58CC02) else Color(0xFFFF9600)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quiz Sheet Board Displays
                else if (selectedTool == AIChatTool.Quiz && activeQuiz.isNotEmpty()) {
                    item {
                        Text("TEST YOUR BRAIN:", fontWeight = FontWeight.Black, color = Color(0xFF777777))
                    }
                    itemsIndexed(activeQuiz) { qIdx, question ->
                        DuolingoCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${qIdx + 1}. ${question.question}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3C3C3C)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            question.options.forEachIndexed { oIdx, opt ->
                                val isSelected = question.selectedAnswerIndex == oIdx
                                val isCorrect = question.correctAnswerIndex == oIdx
                                val cardBg = when {
                                    isSelected && isCorrect -> Color(0xFFE2FDE0) // Correct selection
                                    isSelected && !isCorrect -> Color(0xFFFEE6E7) // Wrong
                                    question.selectedAnswerIndex != null && isCorrect -> Color(0xFFE2FDE0) // Show correct
                                    else -> Color.White
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cardBg)
                                        .border(1.dp, Color(0xFFE5E5E5), shape = RoundedCornerShape(8.dp))
                                        .clickable(enabled = question.selectedAnswerIndex == null) {
                                            viewModel.submitQuizAnswer(qIdx, oIdx)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(opt, fontWeight = FontWeight.Medium, color = Color(0xFF3C3C3C))
                                }
                            }

                            // Show explanations after user selects
                            if (question.selectedAnswerIndex != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Explanation: ${question.explanation}",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                    }
                }

                // Expandable Mindmap branch structures Rendering
                else if (selectedTool == AIChatTool.MindMap && activeMindmap != null) {
                    item {
                        Text("STRUCTURE TRUNKS:", fontWeight = FontWeight.Bold, color = Color(0xFF777777))
                    }
                    item {
                        activeMindmap?.let { root ->
                            MindmapTreeView(node = root)
                        }
                    }
                }

                // Standard Markdown Text Output Rendering
                else if (aiResponse.isNotEmpty()) {
                    item {
                        MarkdownText(
                            markdownString = aiResponse,
                            isOverLightBackground = true
                        )
                    }
                }
            }
        }
        }
    }
}

// Interactive chat buddy view screen supporting continuous back-and-forth threads
@Composable
fun InteractiveChatCompanion(viewModel: MainActivityViewModel, modifier: Modifier = Modifier) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    val mascotMessage by viewModel.mascotMessage.collectAsState()
    val mascotExpression by viewModel.mascotExpression.collectAsState()

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var chatInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Filter conversations of toolType == "Chat" and reverse to place oldest first
    val chatMessages = remember(conversations) {
        conversations
            .filter { it.toolType == "Chat" }
            .reversed()
    }

    // Auto-scroll to the bottom when new messages arrive
    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(26.dp))
            .border(2.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(26.dp))
            .padding(16.dp)
    ) {
        // Chat Header with status & clear button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudyMateOwl(expression = mascotExpression, modifier = Modifier.size(36.dp))
                Column {
                    Text(
                        text = "STUDYMATE CHAT BUDDY",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Text(
                        text = if (isLoading) "Thinking..." else "Online",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLoading) Color(0xFFFF9600) else Color(0xFF58CC02)
                    )
                }
            }

            if (chatMessages.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearChatHistory() }
                ) {
                    Text(
                        "RESET CHAT",
                        color = Color(0xFFFF4B4B),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(bottom = 12.dp))

        // Chats lists / message history
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (chatMessages.isEmpty()) {
                // Welcoming Empty State card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StudyMateOwl(expression = MascotExpression.Happy, modifier = Modifier.size(90.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "YOUR INTERACTIVE CHAT BUDDY! 💬🎓",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Ask any academic questions, formulate calculations, or practice foreign languages step-by-step. Your chat history is saved securely to refer in future!",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "TAP QUICK SUGGESTIONS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val suggestions = listOf(
                        "Explain Photosynthesis like I'm 5 🍃",
                        "Give me a 5-minute study tip ⏱️",
                        "How do quantum mechanics work? 🌌"
                    )
                    suggestions.forEach { suggestion ->
                        DuolingoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    chatInputText = suggestion
                                    viewModel.sendChatMessage(suggestion)
                                    chatInputText = ""
                                }
                                .padding(vertical = 4.dp),
                            color = Color(0xFFF8FAFC),
                            borderColor = Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { chat ->
                        // User Prompt bubble card
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(chat.prompt))
                                            viewModel.notifyMascot("Copied prompt text to clipboard! Paste it into your Note Vault! 🦉📝", MascotExpression.Celebrating)
                                        }
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "YOU",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Prompt",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "COPY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE2FDE0), shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                                    .border(1.5.dp, Color(0xFF58CC02), shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = chat.prompt,
                                    color = Color(0xFF1E293B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Assistant Response bubble card
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(end = 24.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = null,
                                        tint = Color(0xFF1CB0F6),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "STUDYMATE BUDDY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1CB0F6)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(chat.response))
                                            viewModel.notifyMascot("Copied study assistance text to clipboard! Paste it into your Note Vault! 🦉📝", MascotExpression.Celebrating)
                                        }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Response",
                                        tint = Color(0xFF1CB0F6),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "COPY CHAT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1CB0F6)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                                    .border(1.5.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    MarkdownText(
                                        markdownString = chat.response,
                                        isOverLightBackground = true
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF58CC02))
                                Text(
                                    "Writing response...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF777777)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Tray Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInputText,
                onValueChange = { chatInputText = it },
                placeholder = { Text("Ask anything...", color = Color(0xFF475569)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color(0xFF475569),
                    unfocusedPlaceholderColor = Color(0xFF475569),
                    focusedBorderColor = Color(0xFF58CC02),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    cursorColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (chatInputText.isNotBlank() && !isLoading) {
                        viewModel.sendChatMessage(chatInputText)
                        chatInputText = ""
                    }
                }),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (chatInputText.isNotBlank() && !isLoading) {
                                viewModel.sendChatMessage(chatInputText)
                                chatInputText = ""
                            }
                        },
                        enabled = chatInputText.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            tint = if (chatInputText.isNotBlank()) Color(0xFF58CC02) else Color(0xFF94A3B8)
                        )
                    }
                }
            )
        }
    }
}

// 6. Rich Note Creation Screen with Drawing canvas and attachment options
@Composable
fun NotesScreen(viewModel: MainActivityViewModel) {
    val notesList by viewModel.notes.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var isCreatingNew by remember { mutableStateOf(false) }

    // Forms fields
    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var drawingState by remember { mutableStateOf("") }

    if (selectedNote != null) {
        // Detailed single note display view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectNote(null) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close description")
                }
                IconButton(onClick = { 
                    selectedNote?.let { viewModel.deleteNoteById(it.id) }
                    viewModel.selectNote(null)
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete note", tint = Color(0xFFFF4B4B))
                }
            }

            Text(
                "SUBJECT: ${selectedNote!!.subject.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF58CC02)
            )
            Text(
                "${selectedNote!!.chapter} - ${selectedNote!!.topic}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3C3C3C)
            )

            Divider(color = Color(0xFFE5E5E5))

            MarkdownText(
                markdownString = selectedNote!!.content,
                textColor = Color(0xFF4C4C4C)
            )

            if (selectedNote!!.isImageAttached) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Image attached", tint = Color(0xFF888888), modifier = Modifier.size(48.dp))
                        Text("Handdrawn Ink Blueprint Attached", fontSize = 13.sp, color = Color(0xFF777777))
                    }
                }
            }
        }
    } else if (isCreatingNew) {
        // Form field inputs notes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CREATE NEW STUDY NOTE", fontWeight = FontWeight.Black, fontSize = 16.sp)
                IconButton(onClick = { isCreatingNew = false }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                }
            }

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g. Physics)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("Chapter (e.g. Chapter 4: Thermodynamics)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic (e.g. Entropy)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                label = { Text("Typed rich detailed study notes details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            ScientificSymbolTray(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onSymbolSelected = { symbol ->
                    textContent += symbol
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DuolingoButton(
                text = "SAVE STUDY NOTE (+20 XP)",
                onClick = {
                    viewModel.saveNote(
                        subject, chapter, topic, textContent,
                        isImageAttached = false,
                        imagePath = ""
                    )
                    // Reset form
                    subject = ""
                    chapter = ""
                    topic = ""
                    textContent = ""
                    drawingState = ""
                    isCreatingNew = false
                },
                enabled = subject.isNotBlank() && topic.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        // Vault items lists
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NOTEBOOK VAULT", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
                IconButton(
                    onClick = { isCreatingNew = true },
                    modifier = Modifier.background(Color(0xFF58CC02), shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            if (notesList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.NoteAlt, contentDescription = "empty", tint = Color(0xFFDCDCDC), modifier = Modifier.size(64.dp))
                    Text("Your notebook vault is empty! Tap the green + button to write your first note.", fontSize = 13.sp, color = Color(0xFF888888), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notesList) { note ->
                        DuolingoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectNote(note) }
                        ) {
                            Text(note.subject.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF58CC02))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(note.topic, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3C3C3C))
                            Text(note.chapter, fontSize = 13.sp, color = Color(0xFF777777))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.content.take(80) + "...", fontSize = 13.sp, color = Color(0xFF555555))
                            if (note.isImageAttached) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Drawing", tint = Color(0xFF1CB0F6), modifier = Modifier.size(16.dp))
                                    Text("Contains design diagram blueprints", fontSize = 11.sp, color = Color(0xFF1CB0F6), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. PDF VAULT Screen
@Composable
fun PdfsScreen(viewModel: MainActivityViewModel) {
    val pdfsList by viewModel.pdfs.collectAsState()
    val pdfFoldersList by viewModel.pdfFolders.collectAsState()
    val selectedPdf by viewModel.selectedPdf.collectAsState()

    var isAddingPdf by remember { mutableStateOf(false) }
    var pdfTitleInput by remember { mutableStateOf("") }
    var pdfSubjectInput by remember { mutableStateOf("General") }
    var pickedTempFile by remember { mutableStateOf("Calculus_Module_1.pdf") }

    val activeFolderSubject by viewModel.activePdfFolder.collectAsState()
    var isCreatingSubjectFolder by remember { mutableStateOf(false) }
    var newSubjectFolderName by remember { mutableStateOf("") }

    // Aggregate folders list dynamically to include custom folders & folders with PDFs in them
    val displayFolders = remember(pdfFoldersList, pdfsList) {
        val set = linkedSetOf("General", "Mathematics", "Science")
        pdfFoldersList.forEach { set.add(it.folderName) }
        pdfsList.forEach { if (it.subject.isNotBlank()) set.add(it.subject) }
        set.toList()
    }

    if (selectedPdf != null) {
        val pdf = selectedPdf!!
        var drawAnnotation by remember { mutableStateOf(pdf.annotationData) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectPdf(null) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close detailed")
                }
                IconButton(onClick = { 
                    viewModel.deletePdf(pdf.id)
                    viewModel.selectPdf(null)
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete PDF", tint = Color(0xFFFF4B4B))
                }
            }

            Text("PDF DOCUMENT MANAGER • ${pdf.subject.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF58CC02))
            Text(pdf.title, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color(0xFF3C3C3C))

            // 1. System Opener block
            DuolingoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            if (pdf.localUri.startsWith("content://") || pdf.localUri.startsWith("file://")) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(android.net.Uri.parse(pdf.localUri), "application/pdf")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Open study PDF with:"))
                            } else {
                                // Simulated asset, open chooser with text details
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TITLE, "Study PDF Attachment")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Simulated Study Document title: ${pdf.title}\nTemplate asset: ${pdf.localUri}\nSubject category: ${pdf.subject}\n\nStudy Commentary & Annotation: ${pdf.annotationData}")
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Choose helper app to read notes containing:"))
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Cannot open default system PDF viewer: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                color = Color(0xFFFFF1F2),
                borderColor = Color(0xFFF43F5E),
                shadowColor = Color(0xFFE11D48),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Launch, contentDescription = "Launch", tint = Color(0xFFF43F5E), modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OPEN IN DEVICE DEFAULT READER 📱", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFBE123C))
                        Text("Open this document inside your device's native PDF viewing application or send to other tools.", fontSize = 11.sp, color = Color(0xFF9F1239))
                    }
                }
            }

            // 2. NotebookLM Co-Pilot Features Block
            Text("NOTEBOOK LM CO-PILOT AI INTEGRATIONS 🧠⚡", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DuolingoButton(
                    text = "AI SUMMARIZE 📝",
                    onClick = {
                        viewModel.setSelectedChatTool(com.example.ui.AIChatTool.Summarize)
                        viewModel.generatePdfSummary(pdf.title)
                        viewModel.navigateTo(com.example.ui.StudyMateScreen.AIChat)
                    },
                    color = Color(0xFFE0F2FE),
                    shadowColor = Color(0xFFBAE6FD),
                    contentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )

                DuolingoButton(
                    text = "MIND MAP 🕸️",
                    onClick = {
                        viewModel.generateMindmapFromPdf(pdf.title, drawAnnotation)
                        viewModel.navigateTo(com.example.ui.StudyMateScreen.MindMaps)
                    },
                    color = Color(0xFFF3E8FF),
                    shadowColor = Color(0xFFD8B4FE),
                    contentColor = Color(0xFF9333EA),
                    modifier = Modifier.weight(1.1f)
                )
            }

            DuolingoButton(
                text = if (pdf.bookmarks.isNotEmpty()) "BOOKMARKED ⭐ (REMOVE)" else "ADD BOOKMARK ⭐",
                onClick = { viewModel.togglePdfBookmark(pdf.id, 1) },
                color = Color(0xFFFFFBEB),
                shadowColor = Color(0xFFFDE68A),
                contentColor = Color(0xFFD97706),
                modifier = Modifier.fillMaxWidth()
            )

            // Commentary notes notepad section
            Text("DOCUMENT HIGHLIGHT COMMENTARY & HIGHLIGHT KEYNOTES:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))

            OutlinedTextField(
                value = drawAnnotation,
                onValueChange = { 
                    drawAnnotation = it 
                    viewModel.savePdfAnnotation(it)
                },
                label = { Text("Study text commentary or notes details...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    } else if (isAddingPdf) {
        val context = LocalContext.current
        val pdfPageList = listOf("Biochem_Lecture.pdf", "Calculus_Limits.pdf", "Chemistry_Atomic.pdf", "World_History.pdf", "AI_DesignPatterns.pdf")
        val pdfPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                var displayName = "Picked_Document.pdf"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameCol = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameCol != -1 && cursor.moveToFirst()) {
                            displayName = cursor.getString(nameCol)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (pdfTitleInput.isBlank()) {
                    pdfTitleInput = displayName.replace(".pdf", "", ignoreCase = true)
                }
                pickedTempFile = uri.toString()
                android.widget.Toast.makeText(context, "Selected: $displayName", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("ATTACH STUDY DOCUMENT", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pdfPickerLauncher.launch("application/pdf") }
                    .border(2.dp, Color(0xFFD9D9D9), shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Upload PDF",
                        tint = Color(0xFF58CC02),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        "CHOOSE PDF FILE FROM MY DEVICE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF58CC02)
                    )
                    Text(
                        "Tap here to browse and select any actual PDF file directly from your storage.",
                        fontSize = 11.sp,
                        color = Color(0xFF777777),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (pickedTempFile.startsWith("content://") || pickedTempFile.startsWith("file://")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF4CAF50))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Local Device PDF Attached", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text(pickedTempFile, fontSize = 10.sp, color = Color(0xFF43A047), maxLines = 1)
                    }
                }
            }

            OutlinedTextField(
                value = pdfTitleInput,
                onValueChange = { pdfTitleInput = it },
                label = { Text("Document Class Title (e.g. Biochem Lecture Guide)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("ASSIGN SUBJECT FOLDER:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3C3C3C))
            OutlinedTextField(
                value = pdfSubjectInput,
                onValueChange = { pdfSubjectInput = it },
                label = { Text("Subject Folder Name (e.g. Biology, Mathematics, Computer Science)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("OR: Choose simulated study template if you have no PDF on device:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF777777))
            pdfPageList.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickedTempFile = item }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = pickedTempFile == item, onClick = { pickedTempFile = item })
                    Text(item, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { isAddingPdf = false }, modifier = Modifier.weight(1f)) {
                    Text("CANCEL")
                }
                DuolingoButton(
                    text = "ATTACH EXAM PDF",
                    onClick = {
                        viewModel.uploadSimulatedPdf(pdfTitleInput, pickedTempFile, pdfSubjectInput)
                        // Trigger navigating into this newly populated folder automatically
                        viewModel.setActivePdfFolder(pdfSubjectInput)
                        isAddingPdf = false
                        pdfTitleInput = ""
                    },
                    enabled = pdfTitleInput.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Check if user is drilling down inside a subject folder
            if (activeFolderSubject != null) {
                val currentFolder = activeFolderSubject!!
                val filteredPdfs = pdfsList.filter { it.subject == currentFolder }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setActivePdfFolder(null) }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF58CC02))
                        Text(
                            text = "BACK TO SHELVES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF58CC02)
                        )
                    }

                    // Optional deletion button when folder has no materials inside
                    if (filteredPdfs.isEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.deletePdfFolderByName(currentFolder)
                                viewModel.setActivePdfFolder(null)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Empty Folder", tint = Color(0xFFFF4B4B))
                        }
                    }
                }

                Text(
                    text = "📁 CABINET FOLDER: ${currentFolder.uppercase()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF3C3C3C),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (filteredPdfs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "empty pdfs list",
                            tint = Color(0xFFDCDCDC),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No syllabi or document PDFs available in the \"$currentFolder\" folder yet.",
                            fontSize = 13.sp,
                            color = Color(0xFF888888),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPdfs) { pdf ->
                            DuolingoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPdf(pdf) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "pdf", tint = Color(0xFFFF4B4B), modifier = Modifier.size(36.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pdf.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3C3C3C))
                                        Text("File source: ${pdf.localUri}", fontSize = 11.sp, color = Color(0xFF777777))
                                        if (pdf.bookmarks.isNotEmpty()) {
                                            Text("Bookmarks toggled count: ${pdf.bookmarks.split(",").size}", fontSize = 10.sp, color = Color(0xFF58CC02), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Attach Action button locked to this folder
                Spacer(modifier = Modifier.height(12.dp))
                DuolingoButton(
                    text = "ATTACH STUDY DOCUMENT ➕",
                    onClick = {
                        pdfSubjectInput = currentFolder
                        isAddingPdf = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

            } else {
                // By default display the collection folders as interactive button blocks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PDF SUBJECT FOLDER CABINETS 🗄️", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
                    IconButton(
                        onClick = { isCreatingSubjectFolder = true },
                        modifier = Modifier.background(Color(0xFF58CC02), shape = CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Empty Cabinet Folder", tint = Color.White)
                    }
                }

                Text(
                    text = "Select a cabinet subject shelf folder below to review documents or attach new learning syllabi.",
                    fontSize = 12.sp,
                    color = Color(0xFF777777),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayFolders) { pathFolder ->
                        val count = pdfsList.count { it.subject == pathFolder }
                        DuolingoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActivePdfFolder(pathFolder) },
                            color = Color(0xFFFFFDF5),
                            borderColor = Color(0xFFFEF3C7),
                            shadowColor = Color(0xFFFDE68A),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFFEF3C7), shape = RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Folder",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pathFolder.uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "$count study syllabus materials stored inside",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Navigate in",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Bottom create button helper
                    item {
                        DuolingoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCreatingSubjectFolder = true },
                            color = Color(0xFFF8FAFC),
                            borderColor = Color(0xFFE2E8F0),
                            shadowColor = Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Folder icon",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CREATE NEW EMPTY CABINET SHELF",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = "Add folder classification before importing PDF content guides",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isCreatingSubjectFolder) {
                AlertDialog(
                    onDismissRequest = { isCreatingSubjectFolder = false },
                    title = { Text("CREATE NEW SUBJECT CABINET FOLDER", fontWeight = FontWeight.Black, fontSize = 15.sp) },
                    text = {
                        OutlinedTextField(
                            value = newSubjectFolderName,
                            onValueChange = { newSubjectFolderName = it },
                            placeholder = { Text("e.g. Physics, Biochemistry") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newSubjectFolderName.isNotBlank()) {
                                    val cleanedFolderName = newSubjectFolderName.trim()
                                    viewModel.createPdfFolder(cleanedFolderName)
                                    viewModel.setActivePdfFolder(cleanedFolderName)
                                    pdfSubjectInput = cleanedFolderName
                                    isCreatingSubjectFolder = false
                                    newSubjectFolderName = ""
                                }
                            }
                        ) {
                            Text("CREATE & OPEN 📁", color = Color(0xFF58CC02), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isCreatingSubjectFolder = false }) {
                            Text("CANCEL")
                        }
                    }
                )
            }
        }
    }
}

// 8. Visual Mindmaps Expandable Trees List Screens
@Composable
fun MindMapsScreen(viewModel: MainActivityViewModel) {
    val activeMindmap by viewModel.activeMindmap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("STUDYMATE MIND MAP SHEETS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
        Spacer(modifier = Modifier.height(12.dp))

        if (activeMindmap == null) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Hub, contentDescription = "empty mindmap", tint = Color(0xFFDCDCDC), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Woven mind map graphs generated by StudyMate. Navigate to AI Tools -> Mind Map to weave one! 🕸️🦉", fontSize = 13.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFFCFCFC), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE5E5E5), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("ROOT CONCEPTS CLUSTERS:", fontWeight = FontWeight.Black, color = Color(0xFF888888), fontSize = 12.sp)
                }
                item {
                    activeMindmap?.let { root ->
                        MindmapTreeView(node = root)
                    }
                }
            }
        }
    }
}

// Recursive visual design layout for tree structures
@Composable
fun MindmapTreeView(node: MindmapNode, depth: Int = 0) {
    var isExpanded by remember { mutableStateOf(node.isExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (depth == 0) Color(0xFFF1FDF0) else Color(0xFFFFFBFE))
                .border(1.dp, if (depth == 0) Color(0xFF58CC02) else Color(0xFFE5E5E5), shape = RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (node.children.isEmpty()) Icons.Default.Adjust else if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = if (depth == 0) Color(0xFF58CC02) else Color(0xFF1CB0F6),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = node.label,
                fontWeight = if (depth == 0) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = if (depth == 0) 15.sp else 13.sp,
                color = if (depth == 0) Color(0xFF46A302) else Color(0xFF3C3C3C)
            )
        }

        if (isExpanded && node.children.isNotEmpty()) {
            node.children.forEach { child ->
                MindmapTreeView(node = child, depth = depth + 1)
            }
        }
    }
}

// 9. Achievements & Badges Screen
data class BadgeVisuals(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color,
    val cardBg: Color,
    val borderCol: Color
)

fun getBadgeVisuals(id: String, desc: String, unlocked: Boolean): BadgeVisuals {
    return when(id) {
        "first_login" -> BadgeVisuals(
            title = "FIRST ACCESS KEY",
            description = desc,
            icon = Icons.Default.AccountCircle,
            accentColor = if (unlocked) Color(0xFF1D4ED8) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
        )
        "first_ai_query" -> BadgeVisuals(
            title = "EXPLAIN MASTER",
            description = desc,
            icon = Icons.Default.School,
            accentColor = if (unlocked) Color(0xFF7E22CE) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFF3E8FF) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFC084FC) else Color(0xFFE2E8F0)
        )
        "first_note" -> BadgeVisuals(
            title = "PRISTINE NOTE VAULT",
            description = desc,
            icon = Icons.Default.Edit,
            accentColor = if (unlocked) Color(0xFF0F766E) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
        )
        "first_pdf_upload" -> BadgeVisuals(
            title = "VAULT FILE PORTER",
            description = desc,
            icon = Icons.Default.Folder,
            accentColor = if (unlocked) Color(0xFFBE123C) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFFECDD3) else Color(0xFFE2E8F0)
        )
        "streak_3_day" -> BadgeVisuals(
            title = "3-DAY FLAME SAGE",
            description = desc,
            icon = Icons.Default.LocalFireDepartment,
            accentColor = if (unlocked) Color(0xFFD97706) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFFFF7ED) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFFFEDD5) else Color(0xFFE2E8F0)
        )
        "streak_7_day" -> BadgeVisuals(
            title = "7-DAY MASTER SAGE",
            description = desc,
            icon = Icons.Default.Star,
            accentColor = if (unlocked) Color(0xFFC2410C) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
        )
        "quiz_master" -> BadgeVisuals(
            title = "A-GRADE TRIVIA PRO",
            description = desc,
            icon = Icons.Default.EmojiEvents,
            accentColor = if (unlocked) Color(0xFF15803D) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)
        )
        "ai_explorer" -> BadgeVisuals(
            title = "DEEP INDEX COGNITION",
            description = desc,
            icon = Icons.Default.Hub,
            accentColor = if (unlocked) Color(0xFF4338CA) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFC7D2FE) else Color(0xFFE2E8F0)
        )
        "productivity_hero" -> BadgeVisuals(
            title = "SILENT TIMER HERO",
            description = desc,
            icon = Icons.Default.Timer,
            accentColor = if (unlocked) Color(0xFFE11D48) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFFFE4E6) else Color(0xFFE2E8F0)
        )
        "long_term_badge" -> BadgeVisuals(
            title = "LONG-TERM WARRIOR",
            description = desc,
            icon = Icons.Default.EmojiEvents,
            accentColor = if (unlocked) Color(0xFFFF9600) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFFCD34D) else Color(0xFFE2E8F0)
        )
        "night_owl" -> BadgeVisuals(
            title = "STARLIGHT NIGHT OWL 🌕",
            description = desc,
            icon = Icons.Default.Star,
            accentColor = if (unlocked) Color(0xFFFCD34D) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFFEF3C7) else Color(0xFFE2E8F0)
        )
        else -> BadgeVisuals(
            title = id.replace("_", " ").uppercase(),
            description = desc,
            icon = Icons.Default.EmojiEvents,
            accentColor = if (unlocked) Color(0xFF58CC02) else Color(0xFF94A3B8),
            cardBg = if (unlocked) Color(0xFFF1FDF0) else Color(0xFFF8FAFC),
            borderCol = if (unlocked) Color(0xFFE5E5E5) else Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun StreakCalendar(currentStreak: Int, onStudyLogClicked: () -> Unit) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    DuolingoCard(
        color = Color(0xFFFFFDF9),
        borderColor = Color(0xFFFF9600),
        shadowColor = Color(0x15FF9600)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = Color(0xFFFF9600),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "STREAK HABIT CALENDAR",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFFBD5E00)
                    )
                }
                Text(
                    text = "${currentStreak}d consecutive",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF4B00)
                )
            }

            Text(
                text = "Tap on any day in your calendar queue to study and log your streak fire progress! 🔥",
                fontSize = 11.sp,
                color = Color(0xFF78350F),
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    // Mon-Sun corresponding to active states.
                    // If currentStreak is 3, make the last 3 days checked, or index-based highlights.
                    val isActive = index < currentStreak || (currentStreak == 0 && index == 0)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable { onStudyLogClicked() }
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isActive) Color(0xFFFF9600) else Color(0xFF94A3B8)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) Color(0xFFFFEFE3) else Color(0xFFF1F5F9)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isActive) Color(0xFFFF9600) else Color(0xFFCBD5E1),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak logged",
                                    tint = Color(0xFFFF4B00),
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsScreen(viewModel: MainActivityViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val completedBadges = user?.completedBadgeIds?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    val badgesList = listOf(
        Pair("first_login", "Log in and start learning on StudyMate companion."),
        Pair("first_ai_query", "Launch your very first Gemini AI academic query."),
        Pair("first_note", "Draft a class study note inside Note Vault."),
        Pair("first_pdf_upload", "Attach a syllabus PDF guide to the catalog."),
        Pair("streak_3_day", "Study and ask AI 3 days consecutively."),
        Pair("streak_7_day", "Lock down your study routines for a week!"),
        Pair("quiz_master", "Finish all dynamic quiz queries perfectly!"),
        Pair("ai_explorer", "Try translating or mapping outline codes!"),
        Pair("productivity_hero", "Complete 25 minutes Pomodoro tracking."),
        Pair("long_term_badge", "Achieved by completing a long term study goal (weekly or monthly basis)!"),
        Pair("night_owl", "Unlocked by focus tracking or claiming starlight rewards during night study hours (8 PM - 5 AM).")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("YOUR STREAK SHIELD", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
        }

        item {
            DuolingoCard(shadowColor = Color(0x33FF9600), borderColor = Color(0xFFFF9600)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = "Fire", tint = Color(0xFFFF4B00), modifier = Modifier.size(48.dp))
                    Column {
                        Text("STREAK MULTIPLIER: ${user?.currentStreak ?: 0} DAYS!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFFF9600))
                        Text("Record Longest Flame: ${user?.longestStreak ?: 0} Days", fontSize = 13.sp, color = Color(0xFF777777))
                    }
                }
            }
        }

        item {
            StreakCalendar(
                currentStreak = user?.currentStreak ?: 0,
                onStudyLogClicked = {
                    viewModel.studyAndLogHabit()
                }
            )
        }

        item {
            Text("COLLECTABLE BADGES GRID", fontWeight = FontWeight.Bold, color = Color(0xFF777777), fontSize = 14.sp)
        }

        items(badgesList) { (id, desc) ->
            val unlocked = id in completedBadges
            val visuals = getBadgeVisuals(id, desc, unlocked)
            
            DuolingoCard(
                color = visuals.cardBg,
                borderColor = visuals.borderCol
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = if (unlocked) visuals.accentColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unlocked) visuals.icon else Icons.Default.Lock,
                            contentDescription = visuals.title,
                            tint = if (unlocked) visuals.accentColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = visuals.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (unlocked) visuals.accentColor else Color(0xFF64748B)
                            )
                            if (unlocked) {
                                Box(
                                    modifier = Modifier
                                        .background(visuals.accentColor, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("EARNED", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                        Text(visuals.description, fontSize = 12.sp, color = Color(0xFF475569))
                    }
                }
            }
        }
    }
}

// 10. Focus Mode screen (Pomodoro countdown, silence notifications quotes)
@Composable
fun FocusModeScreen(viewModel: MainActivityViewModel) {
    val secLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()

    val mins = secLeft / 60
    val secs = secLeft % 60
    val timeLabel = String.format("%02d:%02d", mins, secs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DND PRO PRODUCTIVITY ROOM", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFFFF4B4B))
        Spacer(modifier = Modifier.height(10.dp))
        Text("Focus mode silences external application notifications.", fontSize = 13.sp, color = Color(0xFF777777), textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(48.dp))

        // Visual dynamic timer boundary circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFFFF2F2), shape = CircleShape)
                .border(8.dp, Color(0xFFFF4B4B), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeLabel, fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF4B4B))
                Text("POMODORO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8B8B))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DuolingoButton(
                text = if (isRunning) "PAUSE TIMERS" else "START RUNNING",
                color = if (isRunning) Color(0xFFFF9600) else Color(0xFFFF4B4B),
                shadowColor = if (isRunning) Color(0xFFE97600) else Color(0xFFD03B3B),
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier.weight(1f)
            )

            DuolingoButton(
                text = "RESET STOPWATCH",
                color = Color.White,
                shadowColor = Color(0xFFE5E5E5),
                contentColor = Color(0xFF777777),
                onClick = { viewModel.resetTimer() },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Motivation quote banner
        if (isRunning) {
            OwlSpeechBubble(text = "\"Genius is 1% inspiration, 99% distraction-free study sessions. I have silenced your app notifications... Run deep academic code operations!\" 🧪🦉")
        }
    }
}

// 11. Search History Logs View screen
@Composable
fun HistoryScreen(viewModel: MainActivityViewModel) {
    val conversations by viewModel.conversations.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("AI CONVERSATION HISTORY LOGS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3C3C3C))
        Spacer(modifier = Modifier.height(4.dp))
        Text("Track, review and delete past prompts generated by Gemini.", fontSize = 12.sp, color = Color(0xFF777777))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs description/queries...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search log") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RECORDED ITEMS count: ${conversations.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF777777))
            TextButton(onClick = { viewModel.clearAllConversations() }) {
                Text("CLEAR ALL HISTORY", color = Color(0xFFFF4B4B), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }

        val filteredLogs = if (searchQuery.isBlank()) conversations else conversations.filter {
            it.prompt.contains(searchQuery, ignoreCase = true) || it.response.contains(searchQuery, ignoreCase = true)
        }

        if (filteredLogs.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = "none", tint = Color(0xFFDCDCDC), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No matching logs found in StudyMate history database.", fontSize = 13.sp, color = Color(0xFF888888))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLogs) { log ->
                    DuolingoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.toolType.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF58CC02))
                            IconButton(onClick = { viewModel.deleteConversationById(log.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF4B4B), modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("PROMPT: \"${log.prompt}\"", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3C3C3C))
                        Spacer(modifier = Modifier.height(4.dp))
                        MarkdownText(markdownString = log.response)
                    }
                }
            }
        }
    }
}

// 12. Goal Settings & profile summary exporter screen
@Composable
fun SettingsScreen(viewModel: MainActivityViewModel) {
    val user by viewModel.currentUser.collectAsState()
    var studyHourGoal by remember { mutableStateOf(user?.studyGoalsHours ?: 1.0f) }

    var exportNotifier by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("SET STUDY GOALS TIME", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))

        Text("Set your active daily companion hours goal: ${String.format("%.1f", studyHourGoal)} Hours")
        Slider(
            value = studyHourGoal,
            onValueChange = { 
                studyHourGoal = it
                viewModel.updateStudyGoal(it)
            },
            valueRange = 0.5f..8.0f
        )

        val longTermGoals by viewModel.longTermGoals.collectAsState()
        var newGoalTitle by remember { mutableStateOf("") }
        var newGoalType by remember { mutableStateOf("Weekly") } // "Weekly" or "Monthly"
        var newGoalTargetHours by remember { mutableStateOf(5.0f) }

        Divider(color = Color(0xFFE5E5E5))

        Text("LONG-TERM STUDY GOALS & REWARDS 🏆", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
        Text(
            "Configure long-term academic targets on a weekly or monthly basis structure. Complete them to collect a bonus +100 XP and the elite LONG-TERM WARRIOR badge! 🔥",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            lineHeight = 16.sp
        )

        DuolingoCard(
            color = Color(0xFFF8FAFC),
            borderColor = Color(0xFFE2E8F0)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "CREATE NEW LONG-TERM GOAL",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = newGoalTitle,
                    onValueChange = { newGoalTitle = it },
                    placeholder = { Text("e.g. Study biochemistry 15 hrs, finish all reading", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF58CC02),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Text(
                    "Schedule Cycle Period:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Weekly", "Monthly").forEach { basis ->
                        val isSelected = newGoalType == basis
                        val selectedCol = if (basis == "Weekly") Color(0xFFF1FDF0) else Color(0xFFF5F3FF)
                        val selectedBorder = if (basis == "Weekly") Color(0xFF58CC02) else Color(0xFF8B5CF6)
                        val selectedText = if (basis == "Weekly") Color(0xFF58CC02) else Color(0xFF8B5CF6)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedCol else Color.White)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) selectedBorder else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { newGoalType = basis }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = basis.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = if (isSelected) selectedText else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Text(
                    "Target Study Hour Volume: ${newGoalTargetHours.toInt()} Hours",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )

                Slider(
                    value = newGoalTargetHours,
                    onValueChange = { newGoalTargetHours = it },
                    valueRange = 1.0f..100.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF58CC02),
                        activeTrackColor = Color(0xFF8CEF55)
                    )
                )

                DuolingoButton(
                    text = "ACTIVATE TARGET $newGoalType GOAL 🚀",
                    onClick = {
                        if (newGoalTitle.isNotBlank()) {
                            viewModel.addLongTermGoal(newGoalTitle, newGoalType, newGoalTargetHours)
                            newGoalTitle = ""
                        }
                    },
                    color = Color(0xFF58CC02),
                    shadowColor = Color(0xFF46A302),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (longTermGoals.isNotEmpty()) {
            Text(
                "ACTIVE LONG-TERM GOALS",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )

            longTermGoals.forEach { goal ->
                DuolingoCard(
                    color = if (goal.isCompleted) Color(0xFFF0FDF4) else Color.White,
                    borderColor = if (goal.isCompleted) Color(0xFF86EFAC) else Color(0xFFE2E8F0),
                    shadowColor = if (goal.isCompleted) Color(0x1022C55E) else Color(0x06000000)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val labelColor = if (goal.type == "Weekly") Color(0xFF16A34A) else Color(0xFF7C3AED)
                                val labelBg = if (goal.type == "Weekly") Color(0xFFDCFCE7) else Color(0xFFF3E8FF)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(labelBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = goal.type.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = labelColor
                                    )
                                }

                                if (goal.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "REWARDED 🎉",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = goal.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (goal.isCompleted) Color(0xFF15803D) else Color(0xFF1E293B),
                                textDecoration = if (goal.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )

                            Text(
                                text = "Target study volume: ${goal.targetHours.toInt()} Hours",
                                fontSize = 11.sp,
                                color = if (goal.isCompleted) Color(0xFF166534) else Color(0xFF64748B)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Checkbox(
                                checked = goal.isCompleted,
                                onCheckedChange = { viewModel.toggleLongTermGoal(goal) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF58CC02))
                            )

                            IconButton(
                                onClick = { viewModel.deleteLongTermGoal(goal.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Goal",
                                    tint = Color(0xFFFF4B4B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFFE5E5E5))

        Text("EXPORT PROFILE DATA ARCHIVE", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
        Text(
            "Press the button to simulate a local SQLite JSON data exporter to back up your notes, streaks and completed badges.",
            fontSize = 13.sp,
            color = Color(0xFF777777)
        )

        DuolingoButton(
            text = "SIMULATE DATABASE BACKUP EXPORT",
            onClick = {
                exportNotifier = """
                    Success! Exported JSON Payload:
                    {
                      "profile": "${user?.username ?: "Anonymous"}",
                      "totalXp": ${user?.totalXp ?: 0},
                      "streak": ${user?.currentStreak ?: 0},
                      "badges": "${user?.completedBadgeIds ?: "none"}"
                    }
                """.trimIndent()
            },
            color = Color(0xFF1CB0F6),
            shadowColor = Color(0xFF108CBF),
            modifier = Modifier.fillMaxWidth()
        )

        if (exportNotifier.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F0F5), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(text = exportNotifier, color = Color(0xFF555555), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color(0xFFE5E5E5))

        Text("SECURITY & ACCESSIBILITY SETTINGS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF3C3C3C))
        
        DuolingoCard(
            color = Color(0xFFF8FAFC),
            borderColor = Color(0xFFE2E8F0)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Settings",
                        tint = Color(0xFF9333EA)
                    )
                    Text(
                        "Device Credentials Session Lock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                Text(
                    "• Auto-login session is active to avoid asking for authentication again and again.\n" +
                    "• You will only be asked to unlock when the app is restarted from cold storage or background termination.\n" +
                    "• Alternative PIN/Scanner security is available if you ever forget your companion password.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )

                Box(
                    modifier = Modifier
                        .background(Color(0xFFF3E8FF), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Status: SECURE DEVICE PASSWORD RECOVERY ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7E22CE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        DuolingoButton(
            text = "LOGOUT & LOCK COMPANION",
            onClick = { viewModel.logout() },
            color = Color(0xFFFF4B4B),
            shadowColor = Color(0xFFC73636),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 12. Study planner room Composable with beautiful timetable and checklists
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyPlannerScreen(viewModel: MainActivityViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val timetable by viewModel.timetable.collectAsState()
    
    var newTaskTitle by remember { mutableStateOf("") }
    
    // Timetable inputs
    var selectedDay by remember { mutableStateOf("Monday") }
    var subjectTitle by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("09:00") } // HH:mm format
    
    val daysList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF7F9FB)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Owl header section encouraging study planning
        item {
            DuolingoCard(
                color = Color.White,
                borderColor = Color(0xFFE2E8F0)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudyMateOwl(expression = MascotExpression.Studious, modifier = Modifier.size(80.dp)) {
                        viewModel.addXp(1)
                    }
                    Column {
                        Text(
                            "STUDY PLANNER 🦉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF9600)
                        )
                        Text(
                            "Organize study targets! Tick tasks and set lesson timetable alarms matching your device clock.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3C3C3C)
                        )
                    }
                }
            }
        }
        
        // SECTION 1: TASK CHECKLIST WITH CHECKBOXES
        item {
            Text(
                "STUDY CHECKLIST",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                letterSpacing = 0.8.sp
            )
        }
        
        item {
            DuolingoCard(color = Color.White, borderColor = Color(0xFFE2E8F0)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "ADD NEW TASK",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFF1CB0F6)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("e.g. Read Physics Chapter 3", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("task_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        IconButton(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    viewModel.addTask(newTaskTitle)
                                    newTaskTitle = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF58CC02), shape = RoundedCornerShape(12.dp))
                                .testTag("add_task_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
                        }
                    }
                }
            }
        }
        
        // Checklist List tasks inline
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Todo list is empty. Add a task to start checking them off!",
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        } else {
            items(tasks) { task ->
                DuolingoCard(
                    color = if (task.isCompleted) Color(0xFFF1FDF0) else Color.White,
                    borderColor = if (task.isCompleted) Color(0xFF58CC02) else Color(0xFFE5E5E5)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF58CC02))
                            )
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) Color(0xFF555555) else Color(0xFF1F2937),
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                            )
                        }
                        IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = Color(0xFFFF4B4B)
                            )
                        }
                    }
                }
            }
        }
        
        // SECTION 2: STUDY TIMETABLE
        item {
            Text(
                "TIMETABLE SCHEDULER",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            DuolingoCard(color = Color.White, borderColor = Color(0xFFE2E8F0)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "SCHEDULE NEW LECTURE / LESSON",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFFFF9600)
                    )
                    
                    // Day Picker horizontal scroll
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Day of week:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(daysList) { day ->
                                val isSelected = day == selectedDay
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelected) Color(0xFFFF9600) else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedDay = day }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.White else Color(0xFF4B5563)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Subject and Time inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = subjectTitle,
                            onValueChange = { subjectTitle = it },
                            placeholder = { Text("Subject / Hour Book", fontSize = 13.sp) },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = scheduleTime,
                            onValueChange = { scheduleTime = it },
                            placeholder = { Text("HH:mm", fontSize = 13.sp) },
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    
                    Text("Trigger alarms automatically at class time! Uses standard 24h format.", fontSize = 11.sp, color = Color(0xFF888888), fontStyle = FontStyle.Italic)
                    
                    DuolingoButton(
                        text = "ADD TO TIMETABLE 📖",
                        onClick = {
                            if (subjectTitle.isNotBlank() && scheduleTime.isNotBlank()) {
                                viewModel.addTimetableEntry(selectedDay, subjectTitle, scheduleTime)
                                subjectTitle = ""
                            }
                        },
                        color = Color(0xFFFF9600),
                        shadowColor = Color(0xFFE97600),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Timetable elements list
        if (timetable.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Timetable is empty. Log your lectures or revision hours!",
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        } else {
            items(timetable) { entry ->
                DuolingoCard(
                    color = Color.White,
                    borderColor = if (entry.isReminderEnabled) Color(0xFFFF9600) else Color(0xFFE5E5E5)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (entry.isReminderEnabled) Color(0xFFFFF4E5) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (entry.isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = "Notification",
                                    tint = if (entry.isReminderEnabled) Color(0xFFFF9600) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = entry.subject.uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${entry.dayOfWeek} at ${entry.time}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = entry.isReminderEnabled,
                                onCheckedChange = { viewModel.toggleTimetableReminder(entry) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFF9600),
                                    checkedTrackColor = Color(0xFFFFF4E5)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.deleteTimetableEntry(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete timetable item",
                                    tint = Color(0xFFFF4B4B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 12. DEDICATED CANVAS DRAWING SANDBOX SCREEN
@Composable
fun CanvasDrawScreen(viewModel: MainActivityViewModel) {
    val drawingsList by viewModel.drawings.collectAsState()
    
    var drawingTitle by remember { mutableStateOf("") }
    
    // Board parameters
    val strokesList = remember { mutableStateListOf<com.example.ui.components.StrokePath>() }
    val currentPoints = remember { mutableStateListOf<androidx.compose.ui.geometry.Offset>() }
    
    var selectedColor by remember { mutableStateOf(Color(0xFF3C3C3C)) } // standard charcoal
    var selectedWidth by remember { mutableStateOf(6f) }
    var isEraserActive by remember { mutableStateOf(false) }

    val colors = listOf(
        Color(0xFF3C3C3C), // Charcoal
        Color(0xFFFF4B4B), // Red
        Color(0xFF1CB0F6), // Blue
        Color(0xFF58CC02), // Green
        Color(0xFFFF9600), // Orange
        Color(0xFF9333EA)  // Purple
    )

    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF7F9FB)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Screen Indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "CANVAS SANDBOX",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF3C3C3C)
                    )
                    Text(
                        "Sketch formulas, graphs, and diagrams",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF777777)
                    )
                }
                
                IconButton(
                    onClick = { viewModel.navigateTo(StudyMateScreen.Dashboard) },
                    modifier = Modifier.background(Color(0xFFE2E8F0), shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF475569))
                }
            }
        }

        // Active sketching board
        item {
            DuolingoCard(
                color = Color.White,
                borderColor = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title input field
                    OutlinedTextField(
                        value = drawingTitle,
                        onValueChange = { drawingTitle = it },
                        placeholder = { Text("Give your blueprint a title (e.g. Krebs Cycle)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF58CC02),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    // Toolkit bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Color pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(color, shape = CircleShape)
                                        .border(
                                            width = if (selectedColor == color && !isEraserActive) 3.dp else 1.dp,
                                            color = if (selectedColor == color && !isEraserActive) Color.White else Color(0x22000000),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedColor = color
                                            isEraserActive = false
                                        }
                                )
                            }
                        }

                        // Toolbar settings
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Eraser Mode Button
                            IconButton(
                                onClick = { isEraserActive = !isEraserActive },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = if (isEraserActive) Color(0xFFFFEBF0) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isEraserActive) Icons.Default.BorderColor else Icons.Default.Palette,
                                    contentDescription = "Eraser Toggle",
                                    tint = if (isEraserActive) Color(0xFFFF4B4B) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Width trigger
                            TextButton(
                                onClick = {
                                    selectedWidth = when (selectedWidth) {
                                        6f -> 12f
                                        12f -> 24f
                                        else -> 6f
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF64748B))
                            ) {
                                val brushLabel = when (selectedWidth) {
                                    6f -> "Thin"
                                    12f -> "Med"
                                    else -> "Thick"
                                }
                                Text(brushLabel, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }

                            // Clear Board Button
                            IconButton(
                                onClick = {
                                    strokesList.clear()
                                    currentPoints.clear()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Canvas Board",
                                    tint = Color(0xFFFF4B4B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Raw canvas touch container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFFFBFBFB), shape = RoundedCornerShape(16.dp))
                            .border(1.5.dp, Color(0xFFEDF2F7), shape = RoundedCornerShape(16.dp))
                            .pointerInput(isEraserActive, selectedColor, selectedWidth) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints.add(offset)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        currentPoints.add(change.position)
                                    },
                                    onDragEnd = {
                                        val colorToUse = if (isEraserActive) Color(0xFFFBFBFB) else selectedColor
                                        strokesList.add(com.example.ui.components.StrokePath(currentPoints.toList(), colorToUse, selectedWidth))
                                        currentPoints.clear()
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Render stable strokes
                            strokesList.forEach { stroke ->
                                val pathObj = androidx.compose.ui.graphics.Path().apply {
                                    val pts = stroke.points
                                    if (pts.isNotEmpty()) {
                                        moveTo(pts[0].x, pts[0].y)
                                        for (i in 1 until pts.size) {
                                            val p = pts[i]
                                            lineTo(p.x, p.y)
                                        }
                                    }
                                }
                                drawPath(
                                    path = pathObj,
                                    color = stroke.color,
                                    style = Stroke(
                                        width = stroke.width,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            // Render active sketch path
                            if (currentPoints.isNotEmpty()) {
                                val activePathObj = androidx.compose.ui.graphics.Path().apply {
                                    val firstPt = currentPoints[0]
                                    moveTo(firstPt.x, firstPt.y)
                                    for (i in 1 until currentPoints.size) {
                                        val p = currentPoints[i]
                                        lineTo(p.x, p.y)
                                    }
                                }
                                val activeColor = if (isEraserActive) Color(0xFFFBFBFB) else selectedColor
                                drawPath(
                                    path = activePathObj,
                                    color = activeColor,
                                    style = Stroke(
                                        width = selectedWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons save
                    DuolingoButton(
                        text = "SAVE SKETCH DESIGN (+20 XP)",
                        onClick = {
                            val finalTitle = if (drawingTitle.isNotBlank()) drawingTitle else "My Sketch ${System.currentTimeMillis() % 1000}"
                            val stringifiedData = serializeStrokes(strokesList.toList())
                            viewModel.saveDrawing(finalTitle, stringifiedData)
                            
                            // Reset state
                            drawingTitle = ""
                            strokesList.clear()
                            currentPoints.clear()
                        },
                        color = Color(0xFF58CC02),
                        shadowColor = Color(0xFF46A302),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Portfolio section lists
        item {
            Text(
                "SAVED SKETCHES GALLERY",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (drawingsList.isEmpty()) {
            item {
                DuolingoCard(
                    color = Color.White,
                    borderColor = Color(0xFFE2E8F0)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Draw", tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                        Text(
                            "No blueprints saved yet. Sketch above to build your study collection!",
                            fontSize = 12.sp,
                            color = Color(0xFF777777),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(drawingsList) { item ->
                DuolingoCard(
                    color = Color.White,
                    borderColor = Color(0xFFE2E8F0)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    // LOAD SKETCH ON BOARD TO VIEW
                                    drawingTitle = item.title
                                    strokesList.clear()
                                    val loaded = deserializeStrokes(item.strokeData)
                                    strokesList.addAll(loaded)
                                    
                                    // Give user feedback via mascot message
                                    viewModel.saveDrawing("", "") // triggers mascot update in lightweight manner or manually
                                }
                        ) {
                            Text(
                                text = item.title.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Blueprint sketch • Click to LOAD & VIEW 🔄",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF58CC02)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteDrawingById(item.id) }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Sketch", tint = Color(0xFFFF4B4B))
                        }
                    }
                }
            }
        }
    }
}

// Helper serializations for drawing sandbox
fun serializeStrokes(strokes: List<com.example.ui.components.StrokePath>): String {
    val arr = JSONArray()
    for (stroke in strokes) {
        val obj = JSONObject()
        val pArr = JSONArray()
        for (p in stroke.points) {
            val pObj = JSONArray()
            pObj.put(p.x.toDouble())
            pObj.put(p.y.toDouble())
            pArr.put(pObj)
        }
        obj.put("points", pArr)
        obj.put("r", stroke.color.red.toDouble())
        obj.put("g", stroke.color.green.toDouble())
        obj.put("b", stroke.color.blue.toDouble())
        obj.put("a", stroke.color.alpha.toDouble())
        obj.put("width", stroke.width.toDouble())
        arr.put(obj)
    }
    return arr.toString()
}

fun deserializeStrokes(jsonStr: String): List<com.example.ui.components.StrokePath> {
    val list = mutableListOf<com.example.ui.components.StrokePath>()
    try {
        if (jsonStr.isBlank()) return list
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val r = obj.getDouble("r").toFloat()
            val g = obj.getDouble("g").toFloat()
            val b = obj.getDouble("b").toFloat()
            val a = obj.getDouble("a").toFloat()
            val width = obj.getDouble("width").toFloat()
            val pointsArr = obj.getJSONArray("points")
            val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
            for (j in 0 until pointsArr.length()) {
                val p = pointsArr.getJSONArray(j)
                points.add(androidx.compose.ui.geometry.Offset(p.getDouble(0).toFloat(), p.getDouble(1).toFloat()))
            }
            list.add(com.example.ui.components.StrokePath(points, Color(red = r, green = g, blue = b, alpha = a), width))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

@Composable
fun QuickUnlockScreen(viewModel: MainActivityViewModel) {
    val context = LocalContext.current
    val lastUser = viewModel.lastUsername ?: "User"
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    
    // Simulated padlock dialogs
    var showSimulatedFingerprint by remember { mutableStateOf(false) }
    var showPinPad by remember { mutableStateOf(false) }
    var interactivePin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("Enter 4-digit Screen Lock PIN or Passcode") }

    // KeyguardManager setup
    val keyguardManager = remember { context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    val isDeviceSecure = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        keyguardManager.isDeviceSecure
    } else {
        keyguardManager.isKeyguardSecure
    }

    val systemLockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loginByPhoneLock { success, msg ->
                if (!success) {
                    errorMsg = msg
                }
            }
        } else {
            errorMsg = "Authentication canceled. Try again."
        }
    }

    fun triggerSystemLock() {
        if (isDeviceSecure && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "StudyMate Quick Unlock",
                "Please verify your pattern, pin or biometric lock screen."
            )
            if (intent != null) {
                systemLockLauncher.launch(intent)
            } else {
                showSimulatedFingerprint = true
            }
        } else {
            showSimulatedFingerprint = true
        }
    }

    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF7F9FB)

    // Auto-prompt on launch to make it super frictionless
    LaunchedEffect(Unit) {
        triggerSystemLock()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StudyMateOwl(expression = MascotExpression.Studious, modifier = Modifier.size(130.dp))
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "SESSION IS LOCKED",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Welcome back, ${lastUser.uppercase()}! 🦉",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        DuolingoCard(
            color = Color.White,
            borderColor = Color(0xFFE2E8F0)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Please verify your credentials or use your Phone system locks to instantly access the dashboard.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                // Password Lock input fallback
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password for $lastUser") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFFF4B4B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DuolingoButton(
                        text = "UNLOCK WITH PASSWORD",
                        onClick = {
                            viewModel.login(lastUser, password) { success, msg ->
                                if (!success) {
                                    errorMsg = msg
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF58CC02),
                        shadowColor = Color(0xFF46A302)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Quick Unlock buttons
        Text(
            "QUICK DEVICE LOCK ACCESS METHODS:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DuolingoButton(
                    text = "🔓 USE SYSTEM LOCK",
                    onClick = { triggerSystemLock() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1CB0F6),
                    shadowColor = Color(0xFF1899D6)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                DuolingoButton(
                    text = "🖐️ PHONE SCANNER",
                    onClick = { showSimulatedFingerprint = true },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF9600),
                    shadowColor = Color(0xFFE08000)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DuolingoButton(
            text = "⌨️ ENTER PIN SECURITY",
            onClick = { showPinPad = true },
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF9333EA),
            shadowColor = Color(0xFF7E22CE)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reset option / logout / switch account
        TextButton(
            onClick = { viewModel.logout() }
        ) {
            Text(
                "❌ SIGN IN WITH ANOTHER ACCOUNT",
                color = Color(0xFFFF4B4B),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }

    // --- DIALOGS FOR SECURITY GRAPHICS FINGERPRINT SCANNING ---
    if (showSimulatedFingerprint) {
        var isScanning by remember { mutableStateOf(false) }
        var scanStatus by remember { mutableStateOf("Touch and hold scanner below to begin fingerprint match") }
        var scanProgress by remember { mutableStateOf(0f) }

        AlertDialog(
            onDismissRequest = { showSimulatedFingerprint = false },
            title = {
                Text(
                    "Simulated Phone Security Scanner",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        scanStatus,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )

                    if (isScanning) {
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF1CB0F6),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    // Tactile interactive button simulating biometric scanner
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                color = if (isScanning) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                                shape = CircleShape
                            )
                            .border(width = 3.dp, color = if (isScanning) Color(0xFF0EA5E9) else Color(0xFF94A3B8), shape = CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        try {
                                            isScanning = true
                                            scanStatus = "Scanning Fingerprint... Keep holding!"
                                            for (i in 1..20) {
                                                kotlinx.coroutines.delay(100)
                                                scanProgress = i / 20f
                                            }
                                            scanStatus = "Identity Matched! 🎉 Access Granted."
                                            kotlinx.coroutines.delay(500)
                                            viewModel.loginByPhoneLock { success, msg ->
                                                if (success) {
                                                    showSimulatedFingerprint = false
                                                } else {
                                                    scanStatus = "Error: $msg"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Handle cancellation
                                        } finally {
                                            isScanning = false
                                            scanProgress = 0f
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Simulated Biometrics Scanner",
                            tint = if (isScanning) Color(0xFF0EA5E9) else Color(0xFF64748B),
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Text(
                        "HOLD SCANNER FOR 2 SECONDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimulatedFingerprint = false }) {
                    Text("USE PASSWORD INSTEAD", fontWeight = FontWeight.Bold, color = Color(0xFF1CB0F6))
                }
            }
        )
    }

    if (showPinPad) {
        AlertDialog(
            onDismissRequest = { showPinPad = false },
            title = {
                Text(
                    "Simulated Device Lock PIN bypass",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        pinMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    // Display Dots for current entered length
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 1..4) {
                            val active = interactivePin.length >= i
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (active) Color(0xFF9333EA) else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    )
                                    .border(width = 1.dp, color = Color(0xFFCBD5E1), shape = CircleShape)
                            )
                        }
                    }

                    // Keypad grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Clear", "0", "Back")
                        ).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 44.dp)
                                            .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                                            .clickable {
                                                when (key) {
                                                    "Clear" -> interactivePin = ""
                                                    "Back" -> if (interactivePin.isNotEmpty()) interactivePin = interactivePin.dropLast(1)
                                                    else -> {
                                                        if (interactivePin.length < 4) {
                                                            interactivePin += key
                                                            if (interactivePin.length == 4) {
                                                                pinMessage = "PIN Verified! Access Granted."
                                                                viewModel.loginByPhoneLock { success, msg ->
                                                                    if (success) {
                                                                        showPinPad = false
                                                                    } else {
                                                                        pinMessage = "Error: $msg"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(key, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

