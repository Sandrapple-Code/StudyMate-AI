package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response Models ---

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @Json(name = "text") val text: String
)

data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

// --- Retrofit Interface ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Gemini Service Wrapper ---

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * Calls Gemini generateContent API and returns response text.
     */
    suspend fun getStudyResponse(
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Gemini API Key is missing. Please configure GEMINI_API_KEY inside the Secrets panel of AI Studio!"
        }

        val baseInstruction = systemInstruction ?: "You are study companion StudyMate AI."
        val mathRules = """
            
            [STRICT RULES FOR MATHEMATICAL & SCIENTIFIC FORMATTING]:
            - NEVER use LaTeX formatting or math markup.
            - NEVER use dollar signs (${'$'}) for math, equations, or any other purpose.
            - NEVER use ${'$'}${'$'}, \(, \), \[, or \] delimiters.
            - Output must be plain natural text utilizing inline Unicode symbols only (e.g. π, √, ×, ÷, ±, ≈, ≤, ≥, ∞, ², ³, ⁿ, ₀, ₁, ₂).
            - Write all formulas and equations in standard, natural textbook form using these symbols instead of LaTeX.
            - Examples:
              Correct: Area = πr²
              Correct: 2x + 5 = 15
              Correct: Speed = distance ÷ time
              Incorrect: ${'$'}2x + 5 = 15${'$'}, ${'$'}${'$'}πr²${'$'}${'$'}, \(\pi r²\), \frac{a}{b}
            - Convert any mathematical/scientific notation directly into readable plain text with standard Unicode characters.
        """.trimIndent()

        val fullSystemInstruction = baseInstruction + "\n" + mathRules

        val contents = listOf(
            GeminiContent(parts = listOf(GeminiPart(text = prompt)))
        )

        val sysInstructionContent = GeminiContent(parts = listOf(GeminiPart(text = fullSystemInstruction)))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = sysInstructionContent,
            generationConfig = GeminiGenerationConfig(temperature = temperature)
        )

        return try {
            val response = api.generateContent(apiKey, request)
            var text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                // Post-process to clean and strip LaTeX/math syntax leaks
                text = text.replace("$$", "")
                text = text.replace("$", "")
                text = text.replace("\\(", "")
                text = text.replace("\\)", "")
                text = text.replace("\\[", "")
                text = text.replace("\\]", "")
                text = formatScientificTextLeaked(text)
            }
            text ?: "Oh no, StudyMate couldn't generate a response. Please try again!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error generated on query request: ${e.localizedMessage ?: "Unknown network error. Please verify network connectivity."}"
        }
    }

    /**
     * Calls Gemini generateContent API for continuous multi-turn conversations.
     */
    suspend fun getChatResponse(
        chatHistory: List<Pair<String, String>>, // list of role and message text pairs
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Gemini API Key is missing. Please configure GEMINI_API_KEY inside the Secrets panel of AI Studio!"
        }

        val baseInstruction = systemInstruction ?: "You are study companion StudyMate AI. Practice continuous interactive learning conversation."
        val mathRules = """
            
            [STRICT RULES FOR MATHEMATICAL & SCIENTIFIC FORMATTING]:
            - NEVER use LaTeX formatting or math markup.
            - NEVER use dollar signs ($) for math, equations, or any other purpose.
            - NEVER use $$, \(, \), \[, or \] delimiters.
            - Output must be plain natural text utilizing inline Unicode symbols only (e.g. π, √, ×, ÷, ±, ≈, ≤, ≥, ∞, ², ³, ⁿ, ₀, ₁, ₂).
            - Write all formulas and equations in standard, natural textbook form using these symbols instead of LaTeX.
            - Convert any mathematical/scientific notation directly into readable plain text with standard Unicode characters.
        """.trimIndent()

        val fullSystemInstruction = baseInstruction + "\n" + mathRules

        val contents = chatHistory.map { (role, text) ->
            GeminiContent(
                role = if (role.lowercase() == "user") "user" else "model",
                parts = listOf(GeminiPart(text = text))
            )
        }

        val sysInstructionContent = GeminiContent(parts = listOf(GeminiPart(text = fullSystemInstruction)))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = sysInstructionContent,
            generationConfig = GeminiGenerationConfig(temperature = temperature)
        )

        return try {
            val response = api.generateContent(apiKey, request)
            var text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                // Post-process to clean and strip LaTeX/math syntax leaks
                text = text.replace("$$", "")
                text = text.replace("$", "")
                text = text.replace("\\(", "")
                text = text.replace("\\)", "")
                text = text.replace("\\[", "")
                text = text.replace("\\]", "")
                text = formatScientificTextLeaked(text)
            }
            text ?: "Oh no, StudyMate couldn't generate a response. Please try again!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error generated on query request: ${e.localizedMessage ?: "Unknown network error. Please verify network connectivity."}"
        }
    }

    private fun formatScientificTextLeaked(input: String): String {
        var text = input

        // Translate LaTeX fractions: \frac{num}{den} -> (num) / (den)
        val fracRegex = Regex("""\\frac\{([^}]+)\}\{([^}]+)\}""")
        text = fracRegex.replace(text) { match ->
            "(${match.groupValues[1]})/(${match.groupValues[2]})"
        }

        // Translate LaTeX square root: \sqrt{content} -> √(content)
        val sqrtRegex = Regex("""\\sqrt\{([^}]+)\}""")
        text = sqrtRegex.replace(text) { match ->
            "√(${match.groupValues[1]})"
        }

        val sqrtSimpleRegex = Regex("""\\sqrt\s+(\w+)""")
        text = sqrtSimpleRegex.replace(text) { match ->
            "√(${match.groupValues[1]})"
        }

        // Standard scientific translations
        val latexReplacements = mapOf(
            "\\alpha" to "α",
            "\\beta" to "β",
            "\\gamma" to "γ",
            "\\theta" to "θ",
            "\\pi" to "π",
            "\\sum" to "Σ",
            "\\int" to "∫",
            "\\Delta" to "Δ",
            "\\sigma" to "σ",
            "\\mu" to "μ",
            "\\lambda" to "λ",
            "\\omega" to "ω",
            "\\phi" to "φ",
            "\\infty" to "∞",
            "\\approx" to "≈",
            "\\neq" to "≠",
            "\\pm" to "±",
            "\\rightarrow" to "→",
            "\\partial" to "∂",
            "\\nabla" to "∇",
            "\\hbar" to "ℏ",
            "\\Psi" to "Ψ",
            "\\nu" to "ν"
        )

        for ((latex, symbol) in latexReplacements) {
            text = text.replace(latex, symbol)
        }

        // Parse curly-braced exponents/superscripts: x^{2}
        val superCurly = Regex("""\^\{([^}]+)\}""")
        text = superCurly.replace(text) { match ->
            convertToSuperscript(match.groupValues[1])
        }

        // Parse individual character exponent/superscripts: x^2
        val superSingle = Regex("""\^([0-9a-zA-Z+-])""")
        text = superSingle.replace(text) { match ->
            convertToSuperscript(match.groupValues[1])
        }

        // Parse curly-braced subscripts: H_{12}
        val subCurly = Regex("""_\{([^}]+)\}""")
        text = subCurly.replace(text) { match ->
            convertToSubscript(match.groupValues[1])
        }

        // Parse individual character subscripts: H_2
        val subSingle = Regex("""_([0-9a-zA-Z+-])""")
        text = subSingle.replace(text) { match ->
            convertToSubscript(match.groupValues[1])
        }

        text = text.replace("\\*", "×")

        return text
    }

    private fun convertToSuperscript(input: String): String {
        val map = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '+' to '⁺', '-' to '⁻', '=' to '⁼', 'n' to 'ⁿ', 'x' to 'ˣ',
            'y' to 'ʸ', 'i' to 'ⁱ', 'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ',
            'd' to 'ᵈ', 'e' to 'ᵉ', 'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ',
            'j' to 'ʲ', 'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'o' to 'ᵒ',
            'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
            'v' to 'ᵛ', 'w' to 'ʷ', 'z' to 'ᶻ'
        )
        return input.map { map[it] ?: it }.joinToString("")
    }

    private fun convertToSubscript(input: String): String {
        val map = mapOf(
            '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
            '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
            '+' to '₊', '-' to '₋', '=' to '₌', 'a' to 'ₐ', 'e' to 'ₑ',
            'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ', 'l' to 'ₗ',
            'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ', 'p' to 'ₚ', 'r' to 'ᵣ',
            's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ', 'v' to 'ᵥ', 'x' to 'ₓ',
            'y' to 'ᵧ'
        )
        return input.map { map[it] ?: it }.joinToString("")
    }
}
