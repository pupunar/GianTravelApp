# GianTravelApp - Backend + Weather + Web Viewer Setup 🚀

Guida completa per configurare il backend Node.js, l'integrazione meteo e il web viewer per seguire i viaggi.

## 📋 Prerequisiti

- **Node.js 18+** - [Scarica](https://nodejs.org/)
- **Firebase Project** - [Crea progetto](https://console.firebase.google.com/)
- **OpenWeatherMap API Key** - [Registrati](https://openweathermap.org/api)

---

## 1️⃣ Configurare OpenWeatherMap (Per Meteo Real-time)

### Ottieni API Key

1. Vai su [openweathermap.org](https://openweathermap.org/api)
2. Crea account (gratuito)
3. Accedi e vai a **API keys**
4. Copia la **default key**

### Configura nell'App Android

1. Apri `app/src/main/res/values/strings.xml`
2. Aggiungi:

```xml
<string name="openweather_api_key">YOUR_API_KEY_HERE</string>
```

✅ **FATTO!** L'app raccoglierà automaticamente meteo durante il tracciamento.

---

## 2️⃣ Configurare Firebase

### Crea Progetto Firebase

1. Vai a [console.firebase.google.com](https://console.firebase.google.com/)
2. Clicca **Create Project**
3. Nome: `GianTravelApp`
4. Disabilita Google Analytics (opzionale)
5. Clicca **Create**

### Abilita Realtime Database

1. Nel progetto Firebase, vai a **Realtime Database**
2. Clicca **Create Database**
3. Seleziona locazione (Europe/Naples è buona)
4. Scegli **Start in test mode** (per sviluppo)
5. Clicca **Enable**

### Abilita Authentication

1. Vai a **Authentication**
2. Clicca **Get Started**
3. Abilita **Email/Password**

### Scarica Service Account Key

1. Vai a **Project Settings** (⚙️ in alto a destra)
2. Vai a **Service Accounts**
3. Seleziona **Node.js**
4. Clicca **Generate new private key**
5. Salva il file `serviceAccountKey.json`

---

## 3️⃣ Configura Backend Node.js

### Installa Dipendenze

```bash
cd backend
npm install
```

### Configura File .env

1. Copia `.env.example` in `.env`:

```bash
cp .env.example .env
```

2. Modifica `.env` con i tuoi valori:

```env
NODE_ENV=development
PORT=5000

# Da Firebase Console
FIREBASE_PROJECT_ID=giantravelapp-xxxxx
FIREBASE_DATABASE_URL=https://giantravelapp-xxxxx.firebaseio.com
FIREBASE_STORAGE_BUCKET=giantravelapp-xxxxx.appspot.com

# Contenuto di serviceAccountKey.json (stringificato come JSON)
FIREBASE_SERVICE_ACCOUNT_KEY={"type":"service_account","project_id":"...","private_key_id":"...","private_key":"-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n","client_email":"...","client_id":"...","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"..."}

# OpenWeatherMap
OPENWEATHER_API_KEY=tua_api_key_qui

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:5000,http://localhost:8000
```

⚠️ **NOTA:** Per il campo `FIREBASE_SERVICE_ACCOUNT_KEY`, stringifica il JSON del file:

```bash
# Converti serviceAccountKey.json in stringa JSON
jq -c . serviceAccountKey.json
```

Poi incolla il risultato nel `.env`.

### Avvia Backend

```bash
cd backend
npm start
```

Dovrebbe vedere:
```
🚀 GianTravelApp Backend running on port 5000
📋 Environment: development
```

✅ Backend è online!

---

## 4️⃣ Configura Web Viewer

### Servi Web Viewer Localmente

**Opzione 1: Con Python** (Più semplice)

```bash
cd web-viewer
python -m http.server 8000
```

Opoi visita: **http://localhost:8000**

**Opzione 2: Con Node.js**

```bash
cd web-viewer
npx http-server
```

**Opzione 3: Con VS Code Live Server**

1. Installa estensione "Live Server"
2. Clicca destro su `index.html` → "Open with Live Server"

### Genera Share Link

Quando avvii un viaggio dal app Android:
1. L'app genera **Share Code** (es: `SHARE_abc123xy`)
2. Condividi link con amici:

```
http://localhost:8000?code=SHARE_abc123xy
```

Oppure, per pubblico online (dopo deploy):

```
https://yourdomain.com/viewer?code=SHARE_abc123xy
```

---

## 5️⃣ Collegare Android App al Backend

### Aggiorna Backend URL nell'App

1. Apri `app/src/main/java/com/giantravelapp/api/WeatherService.kt`
2. Verifica URL base Retrofit (già impostato su `https://api.openweathermap.org/data/2.5/`)

3. Crea nuovo file `FirebaseService.kt`:

```kotlin
// app/src/main/java/com/giantravelapp/service/FirebaseService.kt
package com.giantravelapp.service

import com.google.firebase.database.FirebaseDatabase
import com.giantravelapp.model.LocationPoint

class FirebaseService {
    private val database = FirebaseDatabase.getInstance()
    
    fun uploadLocation(tripId: Long, location: LocationPoint) {
        val ref = database.reference.child("trips/$tripId/locations")
        ref.child(location.id.toString()).setValue(location)
    }
}
```

4. Aggiungi al `LocationTrackingService.kt` (già predisposto con TODO)

---

## 6️⃣ Prova Tutto

### Checklist Finale

✅ Backend Node.js in esecuzione
```bash
cd backend && npm start
# Deve dire: 🚀 GianTravelApp Backend running on port 5000
```

✅ Web Viewer accessibile
```bash
cd web-viewer && python -m http.server 8000
# Visita: http://localhost:8000
```

✅ App Android compilata e installata
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

✅ Google Maps API key configurata
✅ OpenWeatherMap API key aggiunta
✅ Firebase progetto creato

### Test Flow

1. Apri app Android
2. Crea nuovo viaggio
3. Inizia tracciamento (GPS attivo)
4. Apri web viewer nel browser: `http://localhost:8000?code=SHARE_xxxxx`
5. Dovresti vedere la mappa aggiornare in real-time
6. Scrivi un commento nel web viewer

---

## 🔧 Troubleshooting

### Backend non connette a Firebase

```
Error: Invalid Firebase Service Account Key
```

**Soluzione:** Verifica che `FIREBASE_SERVICE_ACCOUNT_KEY` in `.env` sia valido JSON.

### Web Viewer non si aggiorna

**Problema:** Browser console mostra CORS error

**Soluzione:**
1. Backend deve avere CORS abilitato (già nel `server.js`)
2. Verifica che `backendUrl` in `web-viewer/app.js` sia corretto
3. Backend deve essere in esecuzione

### Posizioni non sincronizzate da Android

**Problema:** App invia ubicazioni ma web viewer non le vede

**Soluzione:**
1. Abilita Firebase nella app (decommentare TODO in `LocationTrackingService.kt`)
2. Configurà Firebase Auth nel `MainActivity.kt`
3. Testa con `curl`:

```bash
curl -X POST http://localhost:5000/api/trips/test-trip-id/locations \
  -H "Content-Type: application/json" \
  -d '{"latitude": 40.7128, "longitude": -74.0060}'
```

### Meteo non appare

**Problema:** Weather card vuota nel web viewer

**Soluzione:**
1. Verifica OpenWeatherMap API key sia valida
2. Controlla rate limit API (gratuito: 60 richieste/minuto)
3. Aumenta intervallo di richieste meteo in `WeatherRepository.kt`

---

## 🚀 Deployment (Production)

### Deploy Backend su Heroku

```bash
heroku login
heroku create giantravelapp-backend
git push heroku main
```

### Deploy Web Viewer su GitHub Pages

```bash
# Crea repo 'giantravelapp-web'
git subtree push --prefix web-viewer origin gh-pages
```

Disponibile su: `https://username.github.io/giantravelapp-web?code=SHARE_xxxxx`

---

## 📚 API Reference

### Trips

- `POST /api/trips` - Crea viaggio
- `GET /api/trips/:tripId` - Ottieni dettagli
- `POST /api/trips/:tripId/locations` - Aggiungi posizione
- `GET /api/trips/:tripId/locations` - Ottieni posizioni
- `POST /api/trips/:tripId/comments` - Aggiungi commento
- `GET /api/trips/:tripId/comments` - Ottieni commenti
- `GET /api/share/:shareCode` - Accedi tramite share code

---

**Hai finito! 🎉 Ora tutti possono seguire i tuoi viaggi dal web!**
