# CoastalSocial

Eine Social Media App für Android und iOS, ähnlich wie Facebook.

## Projektstruktur

```
SocialMediaApp/
├── backend/           # Node.js + Express API
│   ├── config/        # Datenbankkonfiguration
│   ├── database/      # SQL Schema
│   ├── middleware/    # Auth & Upload Middleware
│   ├── routes/        # API Endpoints
│   └── server.js      # Express Server
│
├── android/           # Android App (Kotlin Multiplatform)
│   ├── app/           # Android-spezifischer Code
│   │   └── src/main/java/com/coastalsocial/app/
│   └── shared/        # Gemeinsamer Code (Android + iOS)
│       └── src/
│           ├── commonMain/     # Plattformunabhängiger Code
│           ├── androidMain/    # Android-spezifisch
│           └── iosMain/        # iOS-spezifisch
│
└── iosApp/            # iOS App (SwiftUI)
    └── iosApp/
        ├── ContentView.swift
        └── Info.plist
```

## Backend Setup

### Voraussetzungen
- Node.js 18+
- MySQL 8.0+

### Installation

1. **Datenbank erstellen:**
```bash
mysql -u root -p < backend/database/schema.sql
```

2. **.env konfigurieren:**
```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=dein_passwort
DB_NAME=coastalsocial
JWT_SECRET=dein_geheimer_schluessel
PORT=3000
```

3. **Abhängigkeiten installieren:**
```bash
cd backend
npm install
```

4. **Server starten:**
```bash
npm start
```

Der Server läuft unter `http://localhost:3000`

## Android Setup

### Voraussetzungen
- Android Studio Hedgehog (2023.1.1) oder neuer
- JDK 17
- Android SDK 34

### Installation

1. **Projekt in Android Studio öffnen:**
   - File → Open → `SocialMediaApp/android`

2. **BASE_URL anpassen:**
   In `app/build.gradle.kts` die Server-URL ändern:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://DEINE_IP:3000/api/\"")
```

3. **App bauen und ausführen:**
   - Run → Run 'app'

## iOS Setup

### Voraussetzungen
- macOS mit Xcode 15+
- CocoaPods (optional)
- JDK 17 (für Kotlin-Framework-Kompilierung)

### Installation

1. **Kotlin Shared Framework bauen:**
```bash
cd android
./gradlew :shared:assembleXCFramework
```

2. **iOS-Projekt in Xcode öffnen:**
```bash
cd iosApp
open iosApp.xcodeproj
```

3. **Server-URL anpassen:**
   In `android/shared/src/commonMain/kotlin/.../data/api/ApiConfig.kt`:
```kotlin
const val BASE_URL = "http://DEINE_IP:3000/api/"
```

4. **App bauen und ausführen:**
   - Wähle ein iPhone-Simulator oder echtes Gerät
   - Drücke ⌘+R oder klicke auf "Run"

### Hinweise für iOS-Entwicklung

- Das Shared-Framework wird automatisch bei jedem Build kompiliert
- Bei Änderungen am Shared-Code: Xcode-Projekt neu bauen
- Für echte Geräte: Apple Developer Account erforderlich

## API Endpoints

### Auth
- `POST /api/auth/register` - Registrierung
- `POST /api/auth/login` - Anmeldung
- `GET /api/auth/verify` - Token verifizieren

### Posts
- `GET /api/posts` - Feed laden
- `POST /api/posts` - Post erstellen
- `POST /api/posts/:id/like` - Like/Unlike
- `POST /api/posts/:id/comments` - Kommentar hinzufügen

### Users
- `GET /api/users/profile/:username` - Profil laden
- `PUT /api/users/profile` - Profil bearbeiten
- `GET /api/users/search?q=` - Benutzer suchen

### Friends
- `GET /api/friends` - Freundesliste
- `POST /api/friends/request/:userId` - Anfrage senden
- `PUT /api/friends/accept/:userId` - Anfrage akzeptieren

### Messages
- `GET /api/messages/conversations` - Unterhaltungen
- `GET /api/messages/:userId` - Nachrichten mit Benutzer
- `POST /api/messages/:userId` - Nachricht senden

### Notifications
- `GET /api/notifications` - Benachrichtigungen
- `PUT /api/notifications/:id/read` - Als gelesen markieren

## Features

- ✅ Benutzerregistrierung & Anmeldung
- ✅ Profil mit Bild und Bio
- ✅ Posts mit Bildern erstellen
- ✅ Likes & Kommentare
- ✅ Freundschaftssystem
- ✅ Private Nachrichten
- ✅ Push-Benachrichtigungen
- ✅ Stories (24h sichtbar)
- ✅ Benutzersuche
- ✅ Dark Mode

## Technologien

### Backend
- Node.js + Express
- MySQL
- JWT Authentication
- Multer (File Uploads)

### Android
- Kotlin
- Jetpack Compose
- Material 3
- Hilt (Dependency Injection)
- Retrofit (Networking)
- Coil (Image Loading)
- DataStore (Local Storage)
- Navigation Compose

### iOS
- Swift
- SwiftUI
- UIKit (für Integration)
- Combine

### Shared (Kotlin Multiplatform)
- Ktor (Networking)
- Kotlinx Serialization
- Kotlinx Coroutines
- Compose Multiplatform (optional)

## Lizenz

MIT License
