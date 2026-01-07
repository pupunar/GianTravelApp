const express = require('express');
const admin = require('firebase-admin');
const router = express.Router();

// Middleware per verificare token Firebase
const verifyToken = async (req, res, next) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing or invalid token' });
  }
  
  const token = authHeader.substring(7);
  
  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    res.status(401).json({ error: 'Invalid token', details: error.message });
  }
};

// POST /api/auth/register - Registra nuovo utente
router.post('/register', async (req, res) => {
  try {
    const { email, password, displayName } = req.body;
    
    if (!email || !password || !displayName) {
      return res.status(400).json({ error: 'Missing required fields' });
    }
    
    // Crea utente Firebase
    const userRecord = await admin.auth().createUser({
      email,
      password,
      displayName
    });
    
    // Salva profilo nel Realtime Database
    const userRef = admin.database().ref(`users/${userRecord.uid}`);
    await userRef.set({
      uid: userRecord.uid,
      email,
      displayName,
      avatar: null,
      bio: '',
      createdAt: new Date().toISOString(),
      followers: {},
      following: {}
    });
    
    // Genera custom token per login automatico
    const customToken = await admin.auth().createCustomToken(userRecord.uid);
    
    res.status(201).json({
      uid: userRecord.uid,
      email,
      displayName,
      customToken
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(400).json({ error: error.message });
  }
});

// POST /api/auth/login - Login (client-side Firebase)
router.post('/login', async (req, res) => {
  try {
    const { idToken } = req.body;
    
    if (!idToken) {
      return res.status(400).json({ error: 'Missing ID token' });
    }
    
    // Verifica token
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    
    // Ottieni dati utente
    const userRef = admin.database().ref(`users/${decodedToken.uid}`);
    const userSnapshot = await userRef.once('value');
    const userData = userSnapshot.val();
    
    res.json({
      uid: decodedToken.uid,
      email: decodedToken.email,
      displayName: decodedToken.name,
      userData
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
});

// GET /api/auth/profile - Ottieni profilo utente
router.get('/profile', verifyToken, async (req, res) => {
  try {
    const userRef = admin.database().ref(`users/${req.user.uid}`);
    const userSnapshot = await userRef.once('value');
    const userData = userSnapshot.val();
    
    if (!userData) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    res.json(userData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT /api/auth/profile - Aggiorna profilo utente
router.put('/profile', verifyToken, async (req, res) => {
  try {
    const { displayName, bio, avatar } = req.body;
    const userRef = admin.database().ref(`users/${req.user.uid}`);
    
    const updates = {};
    if (displayName) updates.displayName = displayName;
    if (bio !== undefined) updates.bio = bio;
    if (avatar) updates.avatar = avatar;
    
    await userRef.update(updates);
    
    // Aggiorna anche in Firebase Auth se necessario
    if (displayName) {
      await admin.auth().updateUser(req.user.uid, { displayName });
    }
    
    res.json({ success: true, updates });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST /api/auth/follow/:userId - Segui un utente
router.post('/follow/:userId', verifyToken, async (req, res) => {
  try {
    const followingUserId = req.params.userId;
    const currentUserId = req.user.uid;
    
    if (currentUserId === followingUserId) {
      return res.status(400).json({ error: 'Cannot follow yourself' });
    }
    
    // Aggiungi a following list dell'utente corrente
    const followingRef = admin.database()
      .ref(`users/${currentUserId}/following/${followingUserId}`);
    await followingRef.set(true);
    
    // Aggiungi a followers list dell'altro utente
    const followersRef = admin.database()
      .ref(`users/${followingUserId}/followers/${currentUserId}`);
    await followersRef.set(true);
    
    res.json({ success: true, message: 'Following user' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/auth/follow/:userId - Smetti di seguire un utente
router.delete('/follow/:userId', verifyToken, async (req, res) => {
  try {
    const followingUserId = req.params.userId;
    const currentUserId = req.user.uid;
    
    // Rimuovi da following list
    const followingRef = admin.database()
      .ref(`users/${currentUserId}/following/${followingUserId}`);
    await followingRef.remove();
    
    // Rimuovi da followers list
    const followersRef = admin.database()
      .ref(`users/${followingUserId}/followers/${currentUserId}`);
    await followersRef.remove();
    
    res.json({ success: true, message: 'Unfollowed user' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET /api/auth/users/:userId - Ottieni profilo pubblico di un utente
router.get('/users/:userId', async (req, res) => {
  try {
    const userRef = admin.database().ref(`users/${req.params.userId}`);
    const userSnapshot = await userRef.once('value');
    
    if (!userSnapshot.exists()) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    const userData = userSnapshot.val();
    
    // Ritorna solo i dati pubblici
    res.json({
      uid: userData.uid,
      displayName: userData.displayName,
      avatar: userData.avatar,
      bio: userData.bio,
      followersCount: Object.keys(userData.followers || {}).length,
      followingCount: Object.keys(userData.following || {}).length
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Esporta middleware
router.verifyToken = verifyToken;

module.exports = router;
