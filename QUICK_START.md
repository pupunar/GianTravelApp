# 🚀 GianTravelApp - Guida Veloce alla Compilazione

## 1️⃣ Prerequisiti (5 min)

- Scarica **Android Studio** da [developer.android.com](https://developer.android.com/studio)
- Installa Java 17+
- Abilita USB Debugging sul tuo telefono Android

## 2️⃣ Clona e Apri il Progetto (2 min)

```bash
git clone https://github.com/pupunar/GianTravelApp.git
cd GianTravelApp
```

Apri Android Studio → File → Open → Seleziona la cartella

## 3️⃣ Configura Google Maps API (10 min)

**A. Ottieni la chiave API:**
1. Vai a [console.cloud.google.com](https://console.cloud.google.com/)
2. Crea progetto "GianTravelApp"
3. Abilita "Maps SDK for Android"
4. Credentials → Create API Key (Android)

**B. Configura nel progetto:**

Apri `app/src/main/AndroidManifest.xml` e sostituisci:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

## 4️⃣ Compila l'APK (15 min)

### Opzione A: Debug APK (Consigliato)

Terminale in Android Studio (View → Tool Windows → Terminal):

```bash
./gradlew assembleDebug
```

L'APK sarà in:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Opzione B: Usa Android Studio

1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Attendi che finisca
3. Clicca "Locate" per trovare l'APK

## 5️⃣ Installa sul Telefono (2 min)

**Metodo 1: Android Studio**
1. Connetti il telefono con USB
2. Clicca Run → Run 'app'

**Metodo 2: ADB (Riga di comando)**

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Metodo 3: Drag & Drop**

Trascinare `app-debug.apk` sul telefono e aprire

## ✅ Fatto!

L'app è installata e pronta da usare! 🌟

---

## 🔥 Tip Veloci

**Per test veloce senza compilare nuovamente:**
```bash
./gradlew installDebug
```

**Pulisci build e ricompila:**
```bash
./gradlew clean assembleDebug
```

**Vedi log in tempo reale:**
```bash
adb logcat
```

---

## 🗘️ Build Release per Google Play

```bash
# Crea keystore (una sola volta)
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias giantravelapp

# Compila release
./gradlew bundleRelease -Pandroid.injected.signing.store.file=release.keystore -Pandroid.injected.signing.store.password=PASSWORD -Pandroid.injected.signing.key.alias=giantravelapp -Pandroid.injected.signing.key.password=PASSWORD
```

---

**Hai problemi? Vedi README.md per il troubleshooting completo!**
