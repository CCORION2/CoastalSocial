const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');
const upload = require('../middleware/upload');

// Story erstellen
router.post('/', authenticateToken, upload.single('storyMedia'), async (req, res) => {
    try {
        const userId = req.user.userId;
        const { caption } = req.body;

        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'Mediendatei ist erforderlich.'
            });
        }

        const uuid = uuidv4();
        const mediaUrl = `/uploads/stories/${req.file.filename}`;
        const mediaType = req.file.mimetype.startsWith('video/') ? 'video' : 'image';
        
        // Story läuft nach 24 Stunden ab
        const expiresAt = new Date();
        expiresAt.setHours(expiresAt.getHours() + 24);

        await pool.query(
            `INSERT INTO stories (uuid, user_id, media_url, media_type, caption, expires_at) 
             VALUES (?, ?, ?, ?, ?, ?)`,
            [uuid, userId, mediaUrl, mediaType, caption, expiresAt]
        );

        res.status(201).json({
            success: true,
            message: 'Story erstellt!',
            data: { uuid }
        });

    } catch (error) {
        console.error('Story-Erstellungsfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Story konnte nicht erstellt werden.'
        });
    }
});

// Stories der Freunde abrufen
router.get('/feed', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;

        const [stories] = await pool.query(
            `SELECT s.*, u.username, u.full_name, u.profile_picture, u.is_verified,
                    (SELECT COUNT(*) > 0 FROM story_views WHERE story_id = s.id AND viewer_id = ?) as is_viewed
             FROM stories s
             JOIN users u ON s.user_id = u.id
             WHERE s.expires_at > NOW()
               AND (s.user_id = ? OR s.user_id IN (
                   SELECT CASE 
                       WHEN requester_id = ? THEN addressee_id 
                       ELSE requester_id 
                   END
                   FROM friendships 
                   WHERE (requester_id = ? OR addressee_id = ?) AND status = 'accepted'
               ))
             ORDER BY s.user_id, s.created_at DESC`,
            [userId, userId, userId, userId, userId]
        );

        // Nach Benutzer gruppieren
        const groupedStories = stories.reduce((acc, story) => {
            const key = story.user_id;
            if (!acc[key]) {
                acc[key] = {
                    user: {
                        id: story.user_id,
                        username: story.username,
                        fullName: story.full_name,
                        profilePicture: story.profile_picture,
                        isVerified: story.is_verified
                    },
                    stories: []
                };
            }
            acc[key].stories.push({
                id: story.id,
                uuid: story.uuid,
                mediaUrl: story.media_url,
                mediaType: story.media_type,
                caption: story.caption,
                viewsCount: story.views_count,
                isViewed: story.is_viewed,
                createdAt: story.created_at,
                expiresAt: story.expires_at
            });
            return acc;
        }, {});

        res.json({
            success: true,
            data: { stories: Object.values(groupedStories) }
        });

    } catch (error) {
        console.error('Stories-Abruffehler:', error);
        res.status(500).json({
            success: false,
            message: 'Stories konnten nicht abgerufen werden.'
        });
    }
});

// Story ansehen (View registrieren)
router.post('/:storyId/view', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const { storyId } = req.params;

        // View erstellen
        await pool.query(
            `INSERT IGNORE INTO story_views (story_id, viewer_id) VALUES (?, ?)`,
            [storyId, userId]
        );

        // View-Zähler aktualisieren
        await pool.query(
            'UPDATE stories SET views_count = views_count + 1 WHERE id = ? OR uuid = ?',
            [storyId, storyId]
        );

        res.json({
            success: true,
            message: 'Story angesehen.'
        });

    } catch (error) {
        console.error('Story-View-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Fehler beim Registrieren.'
        });
    }
});

// Story-Views abrufen (für eigene Stories)
router.get('/:storyId/views', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const { storyId } = req.params;

        // Prüfen ob eigene Story
        const [story] = await pool.query(
            'SELECT user_id FROM stories WHERE (id = ? OR uuid = ?) AND user_id = ?',
            [storyId, storyId, userId]
        );

        if (story.length === 0) {
            return res.status(403).json({
                success: false,
                message: 'Keine Berechtigung.'
            });
        }

        const [views] = await pool.query(
            `SELECT u.id, u.username, u.full_name, u.profile_picture, sv.viewed_at
             FROM story_views sv
             JOIN users u ON sv.viewer_id = u.id
             WHERE sv.story_id = ?
             ORDER BY sv.viewed_at DESC`,
            [storyId]
        );

        res.json({
            success: true,
            data: { views }
        });

    } catch (error) {
        console.error('Story-Views-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Views konnten nicht abgerufen werden.'
        });
    }
});

// Story löschen
router.delete('/:storyId', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const { storyId } = req.params;

        const [result] = await pool.query(
            'DELETE FROM stories WHERE (id = ? OR uuid = ?) AND user_id = ?',
            [storyId, storyId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Story nicht gefunden oder keine Berechtigung.'
            });
        }

        res.json({
            success: true,
            message: 'Story gelöscht.'
        });

    } catch (error) {
        console.error('Story-Löschfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Story konnte nicht gelöscht werden.'
        });
    }
});

module.exports = router;
