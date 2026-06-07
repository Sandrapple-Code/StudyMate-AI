# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/e2409216-baa8-44e6-9098-c8a997921459

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

# StudyMate AI

StudyMate AI is an AI-powered study companion built with Flutter that helps students learn smarter, stay productive, and organize their academic work in one place.

Features:

AI-powered learning assistant using Gemini API
Explain concepts in simple language
Generate summaries, flashcards, quizzes, and study plans
Create short notes and mind maps
PDF management and note organization
Focus Mode for distraction-free studying
Daily streaks, XP system, and achievement badges
Chat history and personalized learning experience
Secure user authentication
Clean, Duolingo-inspired light-themed UI

Designed to make studying engaging, interactive, and rewarding while leveraging AI to improve learning outcomes.

Built with Flutter, Gemini API, and modern full-stack architecture.
