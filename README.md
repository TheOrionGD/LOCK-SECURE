# LOCK - AI Placement & Mock Interview Coach 🎯

An intelligent Android application built with **Jetpack Compose**, **Material 3**, and **Gemini AI** to help students and job seekers prepare for campus recruitment and technical interviews.

---

## 🌟 Key Features

- **🤖 AI-Powered Interview Simulation**: Generates contextual interview questions across technical domains (Java, DBMS, OS, Computer Networks, Python) and HR/soft skills.
- **🎙️ Real-time Speech-to-Text & Text-to-Speech**: Voice-driven conversational interview practice with realistic interviewer responses.
- **📊 Comprehensive Feedback Reports**: Instant evaluation breakdown including overall score, technical score, communication score, confidence score, strengths, areas for improvement, and ideal answer samples.
- **📂 Session History & Analytics**: Tracks user performance over time with average scoring and category analytics.
- **🎨 Modern Material 3 UI**: Clean, responsive interface featuring dynamic theming, smooth navigation, and dark mode support.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture
- **State Management:** Kotlin Coroutines & `StateFlow`
- **Networking:** [OkHttp 4](https://square.github.io/okhttp/) & REST API integration with Google Gemini
- **Serialization:** Google Gson
- **Voice & Audio:** Android `TextToSpeech` & `SpeechRecognizer`
- **Navigation:** Jetpack Navigation Compose

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- Android SDK 35+
- JDK 17 or 21

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/TheOrionGD/LOCK-SECURE.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📦 Releases

Download the latest signed `.apk` or Google Play `.aab` bundle directly from the [Releases](https://github.com/TheOrionGD/LOCK-SECURE/releases) tab.

---

## 📄 License
This project is licensed under the Apache 2.0 License.
