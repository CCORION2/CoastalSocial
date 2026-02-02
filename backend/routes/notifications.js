const express = require('express');
const router = express.Router();
const { pool } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');
const { transformNotificationUrls } = require('../utils/urlHelper');

// Benachrichtigungen abrufen
router.get('/', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const offset = (page - 1) * limit;

        const [notifications] = await pool.query(
            `SELECT n.*, 
                    u.username, u.full_name, u.profile_picture, u.is_verified
             FROM notifications n
             LEFT JOIN users u ON n.from_user_id = u.id
             WHERE n.user_id = ?
             ORDER BY n.created_at DESC
             LIMIT ? OFFSET ?`,
            [userId, limit, offset]
        );

        // Ungelesene Anzahl
        const [unreadCount] = await pool.query(
            'SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = FALSE',
            [userId]
        );

        // URLs zu absoluten URLs transformieren
        const transformedNotifications = notifications.map(n => transformNotificationUrls(req, n));

        res.json({
            success: true,
            data: {
                notifications: transformedNotifications,
                unreadCount: unreadCount[0].count,
                page,
                hasMore: notifications.length === limit
            }
        });

    } catch (error) {
        console.error('Benachrichtigungen-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Benachrichtigungen konnten nicht abgerufen werden.'
        });
    }
});

// Benachrichtigung als gelesen markieren
router.put('/:notificationId/read', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const { notificationId } = req.params;

        await pool.query(
            'UPDATE notifications SET is_read = TRUE WHERE (id = ? OR uuid = ?) AND user_id = ?',
            [notificationId, notificationId, userId]
        );

        res.json({
            success: true,
            message: 'Benachrichtigung als gelesen markiert.'
        });

    } catch (error) {
        console.error('Markieren-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Fehler beim Markieren.'
        });
    }
});

// Alle Benachrichtigungen als gelesen markieren
router.put('/read-all', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;

        await pool.query(
            'UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE',
            [userId]
        );

        res.json({
            success: true,
            message: 'Alle Benachrichtigungen als gelesen markiert.'
        });

    } catch (error) {
        console.error('Alle-Markieren-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Fehler beim Markieren.'
        });
    }
});

// Benachrichtigung löschen
router.delete('/:notificationId', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const { notificationId } = req.params;

        await pool.query(
            'DELETE FROM notifications WHERE (id = ? OR uuid = ?) AND user_id = ?',
            [notificationId, notificationId, userId]
        );

        res.json({
            success: true,
            message: 'Benachrichtigung gelöscht.'
        });

    } catch (error) {
        console.error('Löschen-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Fehler beim Löschen.'
        });
    }
});

module.exports = router;
