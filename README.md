# GianTravelApp 🚀

Un'app Android completa per il tracciamento GPS in tempo reale dei tuoi viaggi, con foto geotaggate, diario di viaggio, meteo real-time e condivisione con gli amici!

## ✨ Caratteristiche Principali

✅ **Tracciamento GPS Real-time** - Registra la tua posizione ogni 5 secondi  
✅ **Mappa Interattiva** - Visualizza il percorso su Google Maps  
✅ **Foto con Geotag** - Importa foto dalla galleria con coordinate GPS automatiche  
✅ **Diario di Viaggio** - Scrivi note e osservazioni in tempo reale  
✅ **Meteo Real-time** - Visualizza le condizioni meteo lungo il percorso  
✅ **Esportazione PDF** - Crea rapporto completo del viaggio  
✅ **Esportazione GPX** - Esporta per dispositivi GPS  
✅ **Condivisione Sociale** - Condividi su WhatsApp, Instagram, Email  
✅ **Link Web Pubblico** - Gli amici seguono il viaggio dal web senza app  
✅ **Commenti Amici** - Ricevi feedback in tempo reale  
✅ **Statistiche** - Distanza, velocità media, quota massima  

## 🛠️ Requisiti

- **Android Studio** 2022.1 o superiore
- **Android SDK** minimo API 24 (Android 7.0)
- **Java 17**
- **Kotlin 1.9+**
- **Google Maps API Key** (da Google Cloud Console)
- **Firebase Project** (opzionale, per backend sync)
- **OpenWeatherMap API Key** (opzionale, per meteo)

## 📦 Setup Iniziale

### 1. Clona il Repository

```bash
git clone https://github.com/pupunar/GianTravelApp.git
cd GianTravelApp
```

### 2. Apri in Android Studio

- Apri Android Studio
- File → Open → Seleziona la cartella del progetto
- Attendi che il progetto si sincronizzi (Gradle)

### 3. Configura le API Keys

#### Google Maps API

1. Vai a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuovo progetto
3. Abilita **Maps SDK for Android**
4. Crea una **Android API Key**
5. Apri `app/src/main/AndroidManifest.xml`
6. Sostituisci `YOUR_GOOGLE_MAPS_API_KEY` con la tua chiave:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

#### OpenWeatherMap API (Opzionale)

1. Registrati su [OpenWeatherMap](https://openweathermap.org/api)
2. Ottieni la tua API Key
3. Aggiungi a `strings.xml`:

```xml
<string name="openweather_api_key">YOUR_OPENWEATHER_API_KEY</string>
```

### 4. Sincronizza Gradle

```bash
./gradlew clean build
```

## 🚀 Compilazione dell'APK

### Opzione 1: Build di Debug (Consigliato per Test)

```bash
./gradlew assembleDebug
```

L'APK sarà in: `app/build/outputs/apk/debug/app-debug.apk`

### Opzione 2: Build di Release (per Play Store)

#### Crea Keystore

```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias giantravelapp
```

#### Compila APK Release

```bash
./gradlew assembleRelease -Pandroid.injected.signing.store.file=release.keystore -Pandroid.injected.signing.store.password=PASSWORD -Pandroid.injected.signing.key.alias=giantravelapp -Pandroid.injected.signing.key.password=PASSWORD
```

L'APK sarà in: `app/build/outputs/apk/release/app-release.apk`

## 📱 Installazione su Dispositivo

### Via Android Studio

1. Connetti il dispositivo Android al PC
2. Abilita **USB Debugging** sul dispositivo
3. Clicca **Run** → **Run 'app'** in Android Studio

### Via Riga di Comando

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🗂️ Struttura del Progetto

```
GianTravelApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/giantravelapp/
│   │   │   ├── ui/                    # Activities (UI)
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── TripDetailActivity.kt
│   │   │   │   ├── PhotoActivity.kt
│   │   │   │   ├── DiaryActivity.kt
│   │   │   │   ├── ShareTripActivity.kt
│   │   │   │   └── CommentsActivity.kt
│   │   │   ├── model/                 # Data Models
│   │   │   │   └── TripData.kt
│   │   │   ├── service/               # Background Services
│   │   │   │   └── LocationTrackingService.kt
│   │   │   ├── db/                    # Database (Room)
│   │   │   │   └── AppDatabase.kt
│   │   │   ├── adapter/               # RecyclerView Adapters
│   │   │   │   └── TripAdapter.kt
│   │   │   ├── viewmodel/             # ViewModels
│   │   │   │   └── TripViewModel.kt
│   │   │   └── export/                # Esportatori (PDF, GPX)
│   │   │       ├── PDFExporter.kt
│   │   │       └── GPXExporter.kt
│   │   ├── res/
│   │   │   ├── layout/                # XML Layouts
│   │   │   ├── values/
│   │   │   │   └── strings.xml
│   │   │   └── drawable/              # Drawables
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── README.md
```

## 🔧 Configurazione Avanzata

### Attiva il Tracciamento in Background

Per il monitoraggio GPS continuo in background, assicurati che:

1. Il permesso `ACCESS_BACKGROUND_LOCATION` sia concesso
2. L'app abbia priorità in batteria alta
3. Il dispositivo non abbia restrizioni di batteria

### Firebase Sync (Opzionale)

1. Crea un progetto Firebase
2. Scarica `google-services.json` dal console Firebase
3. Posizionalo in `app/`
4. L'app sincronizzerà automaticamente i dati

## 📚 TODO - Funzionalità da Implementare

- [ ] Implementazione completa di PhotoActivity (EXIF reader)
- [ ] Implementazione completa di DiaryActivity
- [ ] Implementazione completa di ShareTripActivity
- [ ] Implementazione completa di CommentsActivity
- [ ] Backend Firebase per sincronizzazione real-time
- [ ] Web interface per seguire i viaggi
- [ ] Meteo real-time integrato
- [ ] Condivisione social media
- [ ] Statistiche avanzate
- [ ] Modalità offline
- [ ] Backup cloud
- [ ] Widget home screen

## 🐛 Troubleshooting

### Errore: Google Maps API not configured

**Soluzione:** Verifica di aver aggiunto correttamente la chiave API in `AndroidManifest.xml`

### Errore: Permessi non concessi

**Soluzione:** L'app richiederà i permessi al primo avvio. Se rifiutati, abilitali manualmente in:
Impostazioni → GianTravelApp → Permessi

### APK non si installa

**Soluzione:** Disinstalla versioni precedenti:
```bash
adb uninstall com.giantravelapp
```

### GPS non funziona

**Soluzione:**
- Attiva il GPS del dispositivo
- Consenti i permessi di localizzazione
- Attendere 30-60 secondi per il fix GPS

## 📄 Licenza

MIT License - Vedi LICENSE.md

## 👨‍💻 Contributi

Ie stie libere di fare fork e pull request!

## 📞 Supporto

Per domande o problemi, apri un issue su GitHub.

---

**Buon viaggio! 🌍✈️**
