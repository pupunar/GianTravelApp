const express = require('express');
const admin = require('firebase-admin');
const router = express.Router();
const { verifyToken } = require('./auth');

// POST /api/notifications/subscribe - Registra device token
router.post('/subscribe', verifyToken, async (req, res) => {
  try {
    const { deviceToken, platform } = req.body;
    const userId = req.user.uid;
    
    if (!deviceToken) {
      return res.status(400).json({ error: 'Missing device token' });
    }
    
    // Salva token nel database
    const tokenRef = admin.database()
      .ref(`users/${userId}/deviceTokens/${deviceToken}`);
    
    await tokenRef.set({
      token: deviceToken,
      platform: platform || 'unknown',
      registeredAt: new Date().toISOString(),
      lastActive: new Date().toISOString()
    });
    
    res.json({ success: true, message: 'Device registered for notifications' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST /api/notifications/unsubscribe - Deregistra device token
router.post('/unsubscribe', verifyToken, async (req, res) => {
  try {
    const { deviceToken } = req.body;
    const userId = req.user.uid;
    
    if (!deviceToken) {
      return res.status(400).json({ error: 'Missing device token' });
    }
    
    const tokenRef = admin.database()
      .ref(`users/${userId}/deviceTokens/${deviceToken}`);
    
    await tokenRef.remove();
    
    res.json({ success: true, message: 'Device unregistered' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Funzione interna per inviare notifiche
async function sendNotificationToUser(userId, notification) {
  try {
    const tokenSnapshot = await admin.database()
      .ref(`users/${userId}/deviceTokens`)
      .once('value');
    
    if (!tokenSnapshot.exists()) {
      console.log(`No tokens found for user ${userId}`);
      return;
    }
    
    const tokens = Object.values(tokenSnapshot.val() || {})
      .map(tokenData => tokenData.token)
      .filter(token => token);
    
    if (tokens.length === 0) {
      return;
    }
    
    // Invia notifiche via FCM
    const message = {
      notification: {
        title: notification.title,
        body: notification.body,
        click_action: notification.action || 'FLUTTER_NOTIFICATION_CLICK'
      },
      data: notification.data || {},
      android: {
        priority: 'high',
        notification: {
          channelId: 'default',
          icon: 'ic_launcher'
        }
      },
      webpush: {
        notification: {
          icon: '/logo.png',
          badge: '/badge.png'
        }
      }
    };
    
    // Invia a tutti i token
    for (const token of tokens) {
      try {
        await admin.messaging().send({
          ...message,
          token
        });
      } catch (err) {
        if (err.code === 'messaging/invalid-registration-token' ||
            err.code === 'messaging/registration-token-not-registered') {
          // Rimuovi token invalido
          const invalidTokenRef = admin.database()
            .ref(`users/${userId}/deviceTokens`);
          const snapshot = await invalidTokenRef.once('value');
          for (const key in snapshot.val() || {}) {
            if (snapshot.val()[key].token === token) {
              await invalidTokenRef.child(key).remove();
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('Error sending notification:', error);
  }
}

// POST /api/notifications/comment - Notifica nuovo commento
router.post('/comment', verifyToken, async (req, res) => {
  try {
    const { tripId, tripUserId, friendName, message } = req.body;
    
    if (!tripId || !tripUserId) {
      return res.status(400).json({ error: 'Missing required fields' });
    }
    
    // Salva notifica nel database
    const notificationRef = admin.database()
      .ref(`notifications/${tripUserId}`);
    
    await notificationRef.push({
      type: 'comment',
      tripId,
      friendName,
      message,
      createdAt: new Date().toISOString(),
      read: false
    });
    
    // Invia notifica push
    await sendNotificationToUser(tripUserId, {
      title: 'Nuovo commento! 💬',
      body: `${friendName}: ${message.substring(0, 50)}...`,
      action: 'OPEN_TRIP',
      data: {
        tripId,
        type: 'comment'
      }
    });
    
    res.json({ success: true, message: 'Notification sent' });
  } catch (error) {
    console.error('Error sending comment notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// POST /api/notifications/follow - Notifica nuovo follower
router.post('/follow', verifyToken, async (req, res) => {
  try {
    const { followerUserId, followerName } = req.body;
    const userId = req.user.uid;
    
    if (!followerUserId) {
      return res.status(400).json({ error: 'Missing follower user ID' });
    }
    
    // Salva notifica
    const notificationRef = admin.database()
      .ref(`notifications/${userId}`);
    
    await notificationRef.push({
      type: 'follow',
      followerUserId,
      followerName,
      createdAt: new Date().toISOString(),
      read: false
    });
    
    // Invia notifica push
    await sendNotificationToUser(userId, {
      title: 'Nuovo follower! 👥',
      body: `${followerName} ti sta seguendo`,
      action: 'OPEN_PROFILE',
      data: {
        followerUserId,
        type: 'follow'
      }
    });
    
    res.json({ success: true });
  } catch (error) {
    console.error('Error sending follow notification:', error);
    res.status(500).json({ error: error.message });
  }
});

// GET /api/notifications - Leggi notifiche utente
router.get('/', verifyToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const limit = parseInt(req.query.limit) || 20;
    
    const notificationRef = admin.database()
      .ref(`notifications/${userId}`);
    
    const snapshot = await notificationRef
      .orderByChild('createdAt')
      .limitToLast(limit)
      .once('value');
    
    const notifications = [];
    snapshot.forEach(child => {
      notifications.push({
        id: child.key,
        ...child.val()
      });
    });
    
    // Ordina dal più recente
    notifications.reverse();
    
    res.json(notifications);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT /api/notifications/:notificationId/read - Marca notifica come letta
router.put('/:notificationId/read', verifyToken, async (req, res) => {
  try {
    const { notificationId } = req.params;
    const userId = req.user.uid;
    
    const notificationRef = admin.database()
      .ref(`notifications/${userId}/${notificationId}`);
    
    await notificationRef.update({
      read: true,
      readAt: new Date().toISOString()
    });
    
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/notifications/:notificationId - Cancella notifica
router.delete('/:notificationId', verifyToken, async (req, res) => {
  try {
    const { notificationId } = req.params;
    const userId = req.user.uid;
    
    const notificationRef = admin.database()
      .ref(`notifications/${userId}/${notificationId}`);
    
    await notificationRef.remove();
    
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
module.exports.sendNotificationToUser = sendNotificationToUser;
