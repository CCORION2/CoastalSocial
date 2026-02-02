const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

// Nachricht senden
router.post('/:receiverId', authenticateToken, async (req, res) => {
    try {
        const senderId = req.user.userId;
        const receiverId = parseInt(req.params.receiverId);
        const { content } = req.body;

        if (!content || content.trim() === '') {
            return res.status(400).json({
                success: false,
                message: 'Nachricht darf nicht leer sein.'
            });
        }

        const uuid = uuidv4();

        await pool.query(
            `INSERT INTO messages (uuid, sender_id, receiver_id, content) 
             VALUES (?, ?, ?, ?)`,
            [uuid, senderId, receiverId, content]
        );

        // Benachrichtigung erstellen
        await pool.query(
            `INSERT INTO notifications (uuid, user_id, type, from_user_id, content) 
             VALUES (?, ?, 'message', ?, ?)`,
            [uuidv4(), receiverId, senderId, content.substring(0, 50)]
        );

        res.status(201).json({
            success: true,
            message: 'Nachricht gesendet!',
            data: { uuid }
        });

    } catch (error) {
        console.error('Nachrichten-Senden-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Nachricht konnte nicht gesendet werden.'
        });
    }
});

// Konversationen abrufen
router.get('/conversations', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;

        const [conversations] = await pool.query(
            `SELECT 
                u.id, u.uuid, u.username, u.full_name, u.profile_picture, u.is_verified,
                m.content as last_message,
                m.created_at as last_message_time,
                m.sender_id = ? as is_own_message,
                (SELECT COUNT(*) FROM messages 
                 WHERE sender_id = u.id AND receiver_id = ? AND is_read = FALSE) as unread_count
             FROM users u
             JOIN messages m ON (
                 (m.sender_id = u.id AND m.receiver_id = ?) OR
                 (m.sender_id = ? AND m.receiver_id = u.id)
             )
             WHERE m.id = (
                 SELECT MAX(m2.id) FROM messages m2
                 WHERE (m2.sender_id = u.id AND m2.receiver_id = ?)
                    OR (m2.sender_id = ? AND m2.receiver_id = u.id)
             )
             ORDER BY m.created_at DESC`,
            [userId, userId, userId, userId, userId, userId]
        );

        res.json({
            success: true,
            data: { conversations }
        });

    } catch (error) {
        console.error('Konversationen-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Konversationen konnten nicht abgerufen werden.'
        });
    }
});

// Nachrichten einer Konversation abrufen
router.get('/:userId', authenticateToken, async (req, res) => {
    try {
        const currentUserId = req.user.userId;
        const otherUserId = parseInt(req.params.userId);
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 50;
        const offset = (page - 1) * limit;

        const [messages] = await pool.query(
            `SELECT m.*, 
                    u.username, u.full_name, u.profile_picture
             FROM messages m
             JOIN users u ON m.sender_id = u.id
             WHERE (m.sender_id = ? AND m.receiver_id = ?)
                OR (m.sender_id = ? AND m.receiver_id = ?)
             ORDER BY m.created_at DESC
             LIMIT ? OFFSET ?`,
            [currentUserId, otherUserId, otherUserId, currentUserId, limit, offset]
        );

        // Nachrichten als gelesen markieren
        await pool.query(
            `UPDATE messages SET is_read = TRUE 
             WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE`,
            [otherUserId, currentUserId]
        );

        res.json({
            success: true,
            data: {
                messages: messages.reverse(),
                page,
                hasMore: messages.length === limit
            }
        });

    } catch (error) {
        console.error('Nachrichten-Abruf-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Nachrichten konnten nicht abgerufen werden.'
        });
    }
});

// Nachricht löschen
router.delete('/:messageId', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const { messageId } = req.params;

        const [result] = await pool.query(
            'DELETE FROM messages WHERE (id = ? OR uuid = ?) AND sender_id = ?',
            [messageId, messageId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Nachricht nicht gefunden oder keine Berechtigung.'
            });
        }

        res.json({
            success: true,
            message: 'Nachricht gelöscht.'
        });

    } catch (error) {
        console.error('Nachrichten-Lösch-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Nachricht konnte nicht gelöscht werden.'
        });
    }
});

module.exports = router;
