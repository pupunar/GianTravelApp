const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const admin = require('firebase-admin');
const rateLimit = require('express-rate-limit');
const { v4: uuidv4 } = require('uuid');

require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minuti
  max: 100 // limita 100 richieste per IP
});
app.use(limiter);

// Firebase Admin Setup
const serviceAccountKey = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
admin.initializeApp({
  credential: admin.credential.cert(serviceAccountKey),
  databaseURL: process.env.FIREBASE_DATABASE_URL,
  storageBucket: process.env.FIREBASE_STORAGE_BUCKET
});

const db = admin.database();
const storage = admin.storage();

// ========================
// TRIP ENDPOINTS
// ========================

// Crea nuovo viaggio
app.post('/api/trips', async (req, res) => {
  try {
    const { userId, name, description } = req.body;
    
    if (!userId || !name) {
      return res.status(400).json({ error: 'userId e name sono obbligatori' });
    }
    
    const tripId = uuidv4();
    const shareCode = 'SHARE_' + Math.random().toString(36).substr(2, 8).toUpperCase();
    
    const tripData = {
      tripId,
      userId,
      name,
      description: description || '',
      startDate: Date.now(),
      endDate: null,
      isActive: true,
      shareCode,
      locations: {},
      photos: {},
      diaryEntries: {},
      comments: {},
      followers: {},
      createdAt: Date.now()
    };
    
    await db.ref(`trips/${tripId}`).set(tripData);
    
    res.json({ success: true, tripId, shareCode });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// Ottieni dettagli viaggio
app.get('/api/trips/:tripId', async (req, res) => {
  try {
    const { tripId } = req.params;
    const snapshot = await db.ref(`trips/${tripId}`).once('value');
    const trip = snapshot.val();
    
    if (!trip) {
      return res.status(404).json({ error: 'Viaggio non trovato' });
    }
    
    res.json(trip);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// ========================
// LOCATION ENDPOINTS
// ========================

// Aggiungi posizione al viaggio
app.post('/api/trips/:tripId/locations', async (req, res) => {
  try {
    const { tripId } = req.params;
    const { latitude, longitude, altitude, speed, temperature, humidity, weatherCondition } = req.body;
    
    const locationId = uuidv4();
    const locationData = {
      locationId,
      latitude,
      longitude,
      altitude: altitude || 0,
      speed: speed || 0,
      temperature,
      humidity,
      weatherCondition,
      timestamp: Date.now()
    };
    
    // Salva in Firebase
    await db.ref(`trips/${tripId}/locations/${locationId}`).set(locationData);
    
    // Notifica followers in real-time via WebSocket (vedi web-viewer)
    res.json({ success: true, locationId });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// Ottieni tutte le posizioni di un viaggio
app.get('/api/trips/:tripId/locations', async (req, res) => {
  try {
    const { tripId } = req.params;
    const snapshot = await db.ref(`trips/${tripId}/locations`).once('value');
    const locations = snapshot.val() || {};
    
    // Converti in array e ordina per timestamp
    const locationArray = Object.values(locations).sort((a, b) => a.timestamp - b.timestamp);
    
    res.json(locationArray);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// ========================
// SHARING ENDPOINTS
// ========================

// Condividi viaggio con codice
app.post('/api/trips/:tripId/followers', async (req, res) => {
  try {
    const { tripId } = req.params;
    const { followerName, followerEmail } = req.body;
    
    const followerId = uuidv4();
    const followerData = {
      followerId,
      name: followerName,
      email: followerEmail,
      joinedAt: Date.now()
    };
    
    await db.ref(`trips/${tripId}/followers/${followerId}`).set(followerData);
    
    res.json({ success: true, followerId });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// Ottieni viaggio tramite share code
app.get('/api/share/:shareCode', async (req, res) => {
  try {
    const { shareCode } = req.params;
    
    // Cerca il viaggio con questo codice
    const snapshot = await db.ref('trips').orderByChild('shareCode').equalTo(shareCode).once('value');
    const trips = snapshot.val();
    
    if (!trips || Object.keys(trips).length === 0) {
      return res.status(404).json({ error: 'Viaggio non trovato' });
    }
    
    const trip = Object.values(trips)[0];
    res.json(trip);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// ========================
// COMMENTS ENDPOINTS
// ========================

// Aggiungi commento
app.post('/api/trips/:tripId/comments', async (req, res) => {
  try {
    const { tripId } = req.params;
    const { friendName, message, latitude, longitude } = req.body;
    
    const commentId = uuidv4();
    const commentData = {
      commentId,
      friendName,
      message,
      latitude,
      longitude,
      timestamp: Date.now()
    };
    
    await db.ref(`trips/${tripId}/comments/${commentId}`).set(commentData);
    
    res.json({ success: true, commentId });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// Ottieni commenti
app.get('/api/trips/:tripId/comments', async (req, res) => {
  try {
    const { tripId } = req.params;
    const snapshot = await db.ref(`trips/${tripId}/comments`).once('value');
    const comments = snapshot.val() || {};
    
    const commentArray = Object.values(comments).sort((a, b) => b.timestamp - a.timestamp);
    res.json(commentArray);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message });
  }
});

// ========================
// HEALTH CHECK
// ========================

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: Date.now() });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 GianTravelApp Backend running on port ${PORT}`);
  console.log(`📍 Environment: ${process.env.NODE_ENV || 'development'}`);
});

module.exports = app;
