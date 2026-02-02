const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

// Freundschaftsanfrage senden
router.post('/request/:userId', authenticateToken, async (req, res) => {
    try {
        const requesterId = req.user.userId;
        const addresseeId = parseInt(req.params.userId);

        if (requesterId === addresseeId) {
            return res.status(400).json({
                success: false,
                message: 'Du kannst dir selbst keine Anfrage senden.'
            });
        }

        // Prüfen ob bereits eine Freundschaft existiert
        const [existing] = await pool.query(
            `SELECT * FROM friendships 
             WHERE (requester_id = ? AND addressee_id = ?) 
                OR (requester_id = ? AND addressee_id = ?)`,
            [requesterId, addresseeId, addresseeId, requesterId]
        );

        if (existing.length > 0) {
            return res.status(400).json({
                success: false,
                message: 'Freundschaftsanfrage existiert bereits.'
            });
        }

        await pool.query(
            'INSERT INTO friendships (requester_id, addressee_id, status) VALUES (?, ?, ?)',
            [requesterId, addresseeId, 'pending']
        );

        // Benachrichtigung erstellen
        await pool.query(
            `INSERT INTO notifications (uuid, user_id, type, from_user_id) 
             VALUES (?, ?, 'friend_request', ?)`,
            [uuidv4(), addresseeId, requesterId]
        );

        res.status(201).json({
            success: true,
            message: 'Freundschaftsanfrage gesendet!'
        });

    } catch (error) {
        console.error('Freundschaftsanfrage-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Anfrage konnte nicht gesendet werden.'
        });
    }
});

// Freundschaftsanfrage akzeptieren
router.put('/accept/:userId', authenticateToken, async (req, res) => {
    try {
        const addresseeId = req.user.userId;
        const requesterId = parseInt(req.params.userId);

        const [result] = await pool.query(
            `UPDATE friendships SET status = 'accepted' 
             WHERE requester_id = ? AND addressee_id = ? AND status = 'pending'`,
            [requesterId, addresseeId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Anfrage nicht gefunden.'
            });
        }

        // Benachrichtigung erstellen
        await pool.query(
            `INSERT INTO notifications (uuid, user_id, type, from_user_id) 
             VALUES (?, ?, 'friend_accept', ?)`,
            [uuidv4(), requesterId, addresseeId]
        );

        res.json({
            success: true,
            message: 'Freundschaftsanfrage akzeptiert!'
        });

    } catch (error) {
        console.error('Akzeptieren-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Anfrage konnte nicht akzeptiert werden.'
        });
    }
});

// Freundschaftsanfrage ablehnen
router.put('/decline/:userId', authenticateToken, async (req, res) => {
    try {
        const addresseeId = req.user.userId;
        const requesterId = parseInt(req.params.userId);

        const [result] = await pool.query(
            `UPDATE friendships SET status = 'declined' 
             WHERE requester_id = ? AND addressee_id = ? AND status = 'pending'`,
            [requesterId, addresseeId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Anfrage nicht gefunden.'
            });
        }

        res.json({
            success: true,
            message: 'Freundschaftsanfrage abgelehnt.'
        });

    } catch (error) {
        console.error('Ablehnen-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Anfrage konnte nicht abgelehnt werden.'
        });
    }
});

// Freund entfernen
router.delete('/:userId', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const friendId = parseInt(req.params.userId);

        await pool.query(
            `DELETE FROM friendships 
             WHERE ((requester_id = ? AND addressee_id = ?) 
                OR (requester_id = ? AND addressee_id = ?))
                AND status = 'accepted'`,
            [userId, friendId, friendId, userId]
        );

        res.json({
            success: true,
            message: 'Freund entfernt.'
        });

    } catch (error) {
        console.error('Freund-Entfernen-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Freund konnte nicht entfernt werden.'
        });
    }
});

// Freundesliste abrufen
router.get('/', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;

        const [friends] = await pool.query(
            `SELECT u.id, u.uuid, u.username, u.full_name, u.profile_picture, u.is_verified,
                    f.created_at as friends_since
             FROM friendships f
             JOIN users u ON (
                 CASE 
                     WHEN f.requester_id = ? THEN f.addressee_id = u.id
                     ELSE f.requester_id = u.id
                 END
             )
             WHERE (f.requester_id = ? OR f.addressee_id = ?) 
                AND f.status = 'accepted'`,
            [userId, userId, userId]
        );

        res.json({
            success: true,
            data: { friends }
        });

    } catch (error) {
        console.error('Freundesliste-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Freundesliste konnte nicht abgerufen werden.'
        });
    }
});

// Freundschaftsanfragen abrufen
router.get('/requests', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;

        const [requests] = await pool.query(
            `SELECT u.id, u.uuid, u.username, u.full_name, u.profile_picture, u.is_verified,
                    f.created_at as requested_at
             FROM friendships f
             JOIN users u ON f.requester_id = u.id
             WHERE f.addressee_id = ? AND f.status = 'pending'
             ORDER BY f.created_at DESC`,
            [userId]
        );

        res.json({
            success: true,
            data: { requests }
        });

    } catch (error) {
        console.error('Anfragen-Abruf-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Anfragen konnten nicht abgerufen werden.'
        });
    }
});

// Benutzer blockieren
router.post('/block/:userId', authenticateToken, async (req, res) => {
    try {
        const blockerId = req.user.userId;
        const blockedId = parseInt(req.params.userId);

        // Bestehende Freundschaft löschen
        await pool.query(
            `DELETE FROM friendships 
             WHERE (requester_id = ? AND addressee_id = ?) 
                OR (requester_id = ? AND addressee_id = ?)`,
            [blockerId, blockedId, blockedId, blockerId]
        );

        // Block erstellen
        await pool.query(
            `INSERT INTO friendships (requester_id, addressee_id, status) 
             VALUES (?, ?, 'blocked')`,
            [blockerId, blockedId]
        );

        res.json({
            success: true,
            message: 'Benutzer blockiert.'
        });

    } catch (error) {
        console.error('Blockieren-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Benutzer konnte nicht blockiert werden.'
        });
    }
});

module.exports = router;
