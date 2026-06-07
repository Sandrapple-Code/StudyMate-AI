package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalXp: Int = 0,
    val completedBadgeIds: String = "", // e.g. "first_login,first_ai"
    val studyGoalsHours: Float = 1.0f,
    val dailyStudySeconds: Int = 0,
    val lastStudyDate: String = "" // "YYYY-MM-DD" style
)

@Entity(tableName = "conversations")
data class ChatConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val toolType: String, // e.g. "Concept", "Summarize", "Quiz", "ELI5", "Flashcard", "StudyPlan", "Mindmap" ...
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val content: String,
    val isImageAttached: Boolean = false,
    val attachedImagePath: String = ""
)

@Entity(tableName = "pdfs")
data class PdfEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val title: String,
    val localUri: String,
    val bookmarks: String = "", // comma-separated page numbers
    val annotationData: String = "", // JSON or simple notes string
    val subject: String = "General"
)

@Entity(tableName = "pdf_folders")
data class PdfFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val folderName: String
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val date: String, // "YYYY-MM-DD"
    val durationSeconds: Int,
    val xpEarned: Int
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val title: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val dayOfWeek: String, // e.g. "Monday"
    val subject: String,
    val time: String, // "HH:MM" format
    val isReminderEnabled: Boolean = true,
    val notificationTriggered: Boolean = false
)

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val title: String,
    val strokeData: String, // Raw points coordinates serialized as text string
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "long_term_goals")
data class LongTermGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val title: String,
    val type: String, // "Weekly" or "Monthly"
    val targetHours: Float,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAO (Data Access Object) ---

@Dao
interface StudyMateDao {
    // User functions
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Chat history functions
    @Query("SELECT * FROM conversations WHERE username = :username ORDER BY timestamp DESC")
    fun getConversations(username: String): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM conversations WHERE username = :username AND (prompt LIKE '%' || :query || '%' OR response LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchConversations(username: String, query: String): Flow<List<ChatConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ChatConversationEntity)

    @Query("DELETE FROM conversations WHERE username = :username")
    suspend fun clearConversations(username: String)

    @Query("DELETE FROM conversations WHERE username = :username AND toolType = :toolType")
    suspend fun deleteConversationsByType(username: String, toolType: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Int)

    // Notes functions
    @Query("SELECT * FROM notes WHERE username = :username ORDER BY id DESC")
    fun getNotes(username: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE username = :username AND (subject LIKE '%' || :query || '%' OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%') ORDER BY id DESC")
    fun searchNotes(username: String, query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Int)

    // PDFs functions
    @Query("SELECT * FROM pdfs WHERE username = :username ORDER BY id DESC")
    fun getPdfs(username: String): Flow<List<PdfEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfEntity)

    @Query("DELETE FROM pdfs WHERE id = :id")
    suspend fun deletePdf(id: Int)

    @Update
    suspend fun updatePdf(pdf: PdfEntity)

    // PDF Folders functions
    @Query("SELECT * FROM pdf_folders WHERE username = :username ORDER BY folderName ASC")
    fun getPdfFolders(username: String): Flow<List<PdfFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfFolder(folder: PdfFolderEntity)

    @Query("DELETE FROM pdf_folders WHERE id = :id")
    suspend fun deletePdfFolder(id: Int)

    @Query("DELETE FROM pdf_folders WHERE username = :username AND folderName = :folderName")
    suspend fun deletePdfFolderByName(username: String, folderName: String)

    // Focus Sessions functions
    @Query("SELECT * FROM focus_sessions WHERE username = :username ORDER BY id DESC")
    fun getFocusSessions(username: String): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity)

    // Tasks queries
    @Query("SELECT * FROM tasks WHERE username = :username ORDER BY id DESC")
    fun getTasks(username: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Int)

    // Timetable queries
    @Query("SELECT * FROM timetable WHERE username = :username ORDER BY dayOfWeek, time ASC")
    fun getTimetable(username: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(item: TimetableEntity)

    @Update
    suspend fun updateTimetable(item: TimetableEntity)

    @Query("DELETE FROM timetable WHERE id = :id")
    suspend fun deleteTimetable(id: Int)

    // Drawings queries
    @Query("SELECT * FROM drawings WHERE username = :username ORDER BY timestamp DESC")
    fun getDrawings(username: String): Flow<List<DrawingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity)

    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteDrawing(id: Int)

    // Long term goals queries
    @Query("SELECT * FROM long_term_goals WHERE username = :username ORDER BY timestamp DESC")
    fun getLongTermGoals(username: String): Flow<List<LongTermGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLongTermGoal(goal: LongTermGoalEntity)

    @Update
    suspend fun updateLongTermGoal(goal: LongTermGoalEntity)

    @Query("DELETE FROM long_term_goals WHERE id = :id")
    suspend fun deleteLongTermGoal(id: Int)
}

// --- AppDatabase ---

@Database(
    entities = [
        UserEntity::class,
        ChatConversationEntity::class,
        NoteEntity::class,
        PdfEntity::class,
        PdfFolderEntity::class,
        FocusSessionEntity::class,
        TaskEntity::class,
        TimetableEntity::class,
        DrawingEntity::class,
        LongTermGoalEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class StudyMateDatabase : RoomDatabase() {
    abstract fun dao(): StudyMateDao

    companion object {
        @Volatile
        private var INSTANCE: StudyMateDatabase? = null

        fun getDatabase(context: android.content.Context): StudyMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyMateDatabase::class.java,
                    "studymate_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
