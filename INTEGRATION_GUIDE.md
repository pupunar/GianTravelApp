# 🌍 GianTravelApp - Integrazione Completa (5 min setup)

Guida veloce per collegare **Meteo Real-time + Web Viewer**.

---

## Step 1: OpenWeatherMap (2 min)

```bash
# 1. Vai su https://openweathermap.org/api
# 2. Crea account (gratuito)
# 3. Copia la tua API Key

# 4. Apri app/src/main/res/values/strings.xml
# 5. Aggiungi:
<string name="openweather_api_key">INCOLLA_QUI_LA_CHIAVE</string>

# 6. Compila app
./gradlew assembleDebug
```

✅ **Meteo è attivo!** L'app raccoglierà meteo durante il tracciamento.

---

## Step 2: Firebase (2 min)

```bash
# 1. Vai su https://console.firebase.google.com/
# 2. Crea progetto: "GianTravelApp"
# 3. Abilita "Realtime Database"
# 4. Abilita "Authentication" (Email/Password)
# 5. Scarica Service Account Key:
#    Project Settings → Service Accounts → Generate Key (Node.js)
#    Salva come backend/serviceAccountKey.json
```

---

## Step 3: Backend Node.js (1 min)

```bash
cd backend

# 1. Copia .env
cp .env.example .env

# 2. Modifica .env con i tuoi dati:
# - FIREBASE_PROJECT_ID (da Firebase Console)
# - FIREBASE_DATABASE_URL (da Realtime Database)
# - FIREBASE_SERVICE_ACCOUNT_KEY (da serviceAccountKey.json)
# - OPENWEATHER_API_KEY (da passo 1)

# 3. Installa dipendenze
npm install

# 4. Avvia backend
npm start

# Dovresti vedere:
# 🚀 GianTravelApp Backend running on port 5000
```

---

## Step 4: Web Viewer (1 min)

```bash
# In nuovo terminale:
cd web-viewer

# Opzione A: Python
python -m http.server 8000

# Opzione B: Node.js
npx http-server

# Opzione C: VS Code
# Destro su index.html → "Open with Live Server"

# Accedi a: http://localhost:8000
```

---

## Step 5: Testa Tutto

### Checklist Finale

- [ ] Backend Node.js running on port 5000 (🚀)
- [ ] Web Viewer accessibile su http://localhost:8000
- [ ] App Android compilata e installata
- [ ] GPS abilitato sul dispositivo
- [ ] OpenWeatherMap API key nel app
- [ ] Firebase configurato

### Test Flow

1. **App Android:**
   - Apri app
   - Crea "New Trip"
   - Inizia tracciamento
   - Vedrai il codice share (es: `SHARE_abc123`)

2. **Web Viewer:**
   - Apri browser: `http://localhost:8000?code=SHARE_abc123`
   - Dovrebbe mostrare mappa con polyline
   - Meteo deve apparire nell'overlay
   - Aggiungi commento

3. **Aggiornamenti:**
   - Muoviti con il telefono
   - La mappa si aggiorna ogni 5 secondi
   - I commenti appaiono istantaneamente

---

## 🌈 Architettura

```
📋 Android App
    │ GPS + Weather tracking
    │
    🔗 Firebase Realtime DB
    │
    👏 Backend Node.js (Port 5000)
    │
    🌐 Web Viewer (Localhost:8000)
       │
       💭 Friends can comment
```

---

## API Principali

```bash
# Creare viaggio
POST http://localhost:5000/api/trips
{"userId": "gian", "name": "Roma Trip"}

# Aggiungere posizione con meteo
POST http://localhost:5000/api/trips/{tripId}/locations
{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "temperature": 22,
  "humidity": 65,
  "weatherCondition": "Sunny"
}

# Ottenere viaggio tramite share code
GET http://localhost:5000/api/share/SHARE_abc123xy

# Aggiungere commento
POST http://localhost:5000/api/trips/{tripId}/comments
{"friendName": "Marco", "message": "Divertimento!", "latitude": 40.7128, "longitude": -74.0060}
```

---

## 🚫 Common Issues

### "CORS error in web viewer"
```
SOLUTION: Backend non è in esecuzione
- Verificare che npm start sia lanciato in backend/
- Controllare che backendUrl in web-viewer/app.js sia corretto
```

### "Weather not showing"
```
SOLUTION: API Key non configurata
- Verifica che strings.xml abbia la corretta OpenWeatherMap key
- Controlla rate limit (gratuito: 60 req/min)
```

### "Web viewer not updating"
```
SOLUTION: Controllo Firebase/Backend
- Assicurati che Firebase Realtime DB sia abilitato
- Verifica che FIREBASE_SERVICE_ACCOUNT_KEY in .env sia valido
- Testa backend con: curl http://localhost:5000/api/health
```

---

## 🚀 Prossimi Step

1. **Deploy Backend** su Heroku/Railway
2. **Deploy Web Viewer** su GitHub Pages/Netlify
3. **Aggiungere Autenticazione** Firebase Auth
4. **Foto Upload** con EXIF extraction
5. **Notifiche** per commenti

---

**Fatto! 🌟 Ora il tuo viaggio è condivisibile e visibile dal web!**

Per dettagli completi, vedi: [BACKEND_SETUP.md](./BACKEND_SETUP.md)
