const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../config/database');
const { authenticateToken, optionalAuth } = require('../middleware/auth');
const upload = require('../middleware/upload');
const { transformPostUrls, transformCommentUrls, toAbsoluteUrl } = require('../utils/urlHelper');

// Neuen Post erstellen
router.post('/', authenticateToken, upload.single('postMedia'), async (req, res) => {
    try {
        const { content, privacy = 'public' } = req.body;
        const userId = req.user.userId;

        if (!content && !req.file) {
            return res.status(400).json({
                success: false,
                message: 'Post muss Inhalt oder Medien enthalten.'
            });
        }

        const uuid = uuidv4();
        let imageUrl = null;
        let videoUrl = null;

        if (req.file) {
            const mediaPath = `/uploads/posts/${req.file.filename}`;
            if (req.file.mimetype.startsWith('video/')) {
                videoUrl = mediaPath;
            } else {
                imageUrl = mediaPath;
            }
        }

        const [result] = await pool.query(
            `INSERT INTO posts (uuid, user_id, content, image_url, video_url, privacy) 
             VALUES (?, ?, ?, ?, ?, ?)`,
            [uuid, userId, content, imageUrl, videoUrl, privacy]
        );

        // Hashtags extrahieren und speichern
        if (content) {
            const hashtags = content.match(/#\w+/g) || [];
            for (const tag of hashtags) {
                const tagName = tag.toLowerCase().substring(1);
                
                // Hashtag erstellen oder aktualisieren
                await pool.query(
                    `INSERT INTO hashtags (name, posts_count) VALUES (?, 1)
                     ON DUPLICATE KEY UPDATE posts_count = posts_count + 1`,
                    [tagName]
                );

                // Hashtag-ID abrufen und verknüpfen
                const [hashtagRows] = await pool.query(
                    'SELECT id FROM hashtags WHERE name = ?',
                    [tagName]
                );

                if (hashtagRows.length > 0) {
                    await pool.query(
                        'INSERT IGNORE INTO post_hashtags (post_id, hashtag_id) VALUES (?, ?)',
                        [result.insertId, hashtagRows[0].id]
                    );
                }
            }
        }

        res.status(201).json({
            success: true,
            message: 'Post erfolgreich erstellt!',
            data: {
                postId: result.insertId,
                uuid
            }
        });

    } catch (error) {
        console.error('Post-Erstellungsfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Post konnte nicht erstellt werden.'
        });
    }
});

// Feed abrufen (Posts von Freunden)
router.get('/feed', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const offset = (page - 1) * limit;

        const [posts] = await pool.query(
            `SELECT p.*, u.username, u.full_name, u.profile_picture, u.is_verified,
                    (SELECT COUNT(*) FROM likes WHERE post_id = p.id) as likes_count,
                    (SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comments_count,
                    (SELECT COUNT(*) > 0 FROM likes WHERE post_id = p.id AND user_id = ?) as is_liked,
                    (SELECT COUNT(*) > 0 FROM saved_posts WHERE post_id = p.id AND user_id = ?) as is_saved
             FROM posts p
             JOIN users u ON p.user_id = u.id
             WHERE p.user_id = ?
                OR p.user_id IN (
                    SELECT CASE 
                        WHEN requester_id = ? THEN addressee_id 
                        ELSE requester_id 
                    END
                    FROM friendships 
                    WHERE (requester_id = ? OR addressee_id = ?) AND status = 'accepted'
                )
             ORDER BY p.created_at DESC
             LIMIT ? OFFSET ?`,
            [userId, userId, userId, userId, userId, userId, limit, offset]
        );

        // URLs zu absoluten URLs transformieren
        const transformedPosts = posts.map(post => transformPostUrls(req, post));

        res.json({
            success: true,
            data: {
                posts: transformedPosts,
                page,
                hasMore: posts.length === limit
            }
        });

    } catch (error) {
        console.error('Feed-Abruffehler:', error);
        res.status(500).json({
            success: false,
            message: 'Feed konnte nicht abgerufen werden.'
        });
    }
});

// Einzelnen Post abrufen
router.get('/:postId', optionalAuth, async (req, res) => {
    try {
        const { postId } = req.params;
        const userId = req.user?.userId;

        const [posts] = await pool.query(
            `SELECT p.*, u.username, u.full_name, u.profile_picture, u.is_verified,
                    (SELECT COUNT(*) FROM likes WHERE post_id = p.id) as likes_count,
                    (SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comments_count,
                    (SELECT COUNT(*) > 0 FROM likes WHERE post_id = p.id AND user_id = ?) as is_liked,
                    (SELECT COUNT(*) > 0 FROM saved_posts WHERE post_id = p.id AND user_id = ?) as is_saved
             FROM posts p
             JOIN users u ON p.user_id = u.id
             WHERE p.uuid = ? OR p.id = ?`,
            [userId || 0, userId || 0, postId, postId]
        );

        if (posts.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Post nicht gefunden.'
            });
        }

        res.json({
            success: true,
            data: { post: transformPostUrls(req, posts[0]) }
        });

    } catch (error) {
        console.error('Post-Abruffehler:', error);
        res.status(500).json({
            success: false,
            message: 'Post konnte nicht abgerufen werden.'
        });
    }
});

// Posts eines Benutzers abrufen
router.get('/user/:username', optionalAuth, async (req, res) => {
    try {
        const { username } = req.params;
        const userId = req.user?.userId;
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const offset = (page - 1) * limit;

        const [posts] = await pool.query(
            `SELECT p.*, u.username, u.full_name, u.profile_picture, u.is_verified,
                    (SELECT COUNT(*) FROM likes WHERE post_id = p.id) as likes_count,
                    (SELECT COUNT(*) FROM comments WHERE post_id = p.id) as comments_count,
                    (SELECT COUNT(*) > 0 FROM likes WHERE post_id = p.id AND user_id = ?) as is_liked
             FROM posts p
             JOIN users u ON p.user_id = u.id
             WHERE u.username = ?
             ORDER BY p.created_at DESC
             LIMIT ? OFFSET ?`,
            [userId || 0, username, limit, offset]
        );

        // URLs zu absoluten URLs transformieren
        const transformedPosts = posts.map(post => transformPostUrls(req, post));

        res.json({
            success: true,
            data: {
                posts: transformedPosts,
                page,
                hasMore: posts.length === limit
            }
        });

    } catch (error) {
        console.error('Benutzer-Posts-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Posts konnten nicht abgerufen werden.'
        });
    }
});

// Post liken/unliken
router.post('/:postId/like', authenticateToken, async (req, res) => {
    try {
        const { postId } = req.params;
        const userId = req.user.userId;

        // Prüfen ob bereits geliked
        const [existingLike] = await pool.query(
            'SELECT id FROM likes WHERE user_id = ? AND post_id = ?',
            [userId, postId]
        );

        if (existingLike.length > 0) {
            // Unlike
            await pool.query(
                'DELETE FROM likes WHERE user_id = ? AND post_id = ?',
                [userId, postId]
            );

            res.json({
                success: true,
                message: 'Like entfernt.',
                data: { liked: false }
            });
        } else {
            // Like
            await pool.query(
                'INSERT INTO likes (user_id, post_id) VALUES (?, ?)',
                [userId, postId]
            );

            // Benachrichtigung erstellen
            const [post] = await pool.query(
                'SELECT user_id FROM posts WHERE id = ?',
                [postId]
            );

            if (post.length > 0 && post[0].user_id !== userId) {
                await pool.query(
                    `INSERT INTO notifications (uuid, user_id, type, reference_id, from_user_id) 
                     VALUES (?, ?, 'like', ?, ?)`,
                    [uuidv4(), post[0].user_id, postId, userId]
                );
            }

            res.json({
                success: true,
                message: 'Post geliked!',
                data: { liked: true }
            });
        }

    } catch (error) {
        console.error('Like-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Aktion fehlgeschlagen.'
        });
    }
});

// Kommentar hinzufügen
router.post('/:postId/comments', authenticateToken, async (req, res) => {
    try {
        const { postId } = req.params;
        const { content, parentCommentId } = req.body;
        const userId = req.user.userId;

        if (!content) {
            return res.status(400).json({
                success: false,
                message: 'Kommentar darf nicht leer sein.'
            });
        }

        const uuid = uuidv4();

        const [result] = await pool.query(
            `INSERT INTO comments (uuid, post_id, user_id, parent_comment_id, content) 
             VALUES (?, ?, ?, ?, ?)`,
            [uuid, postId, userId, parentCommentId || null, content]
        );

        // Kommentarzahl aktualisieren
        await pool.query(
            'UPDATE posts SET comments_count = comments_count + 1 WHERE id = ?',
            [postId]
        );

        // Benachrichtigung erstellen
        const [post] = await pool.query(
            'SELECT user_id FROM posts WHERE id = ?',
            [postId]
        );

        if (post.length > 0 && post[0].user_id !== userId) {
            await pool.query(
                `INSERT INTO notifications (uuid, user_id, type, reference_id, from_user_id, content) 
                 VALUES (?, ?, 'comment', ?, ?, ?)`,
                [uuidv4(), post[0].user_id, postId, userId, content.substring(0, 100)]
            );
        }

        res.status(201).json({
            success: true,
            message: 'Kommentar hinzugefügt!',
            data: {
                commentId: result.insertId,
                uuid
            }
        });

    } catch (error) {
        console.error('Kommentar-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Kommentar konnte nicht hinzugefügt werden.'
        });
    }
});

// Kommentare eines Posts abrufen
router.get('/:postId/comments', optionalAuth, async (req, res) => {
    try {
        const { postId } = req.params;
        const userId = req.user?.userId;

        const [comments] = await pool.query(
            `SELECT c.*, u.username, u.full_name, u.profile_picture, u.is_verified,
                    (SELECT COUNT(*) FROM likes WHERE comment_id = c.id) as likes_count,
                    (SELECT COUNT(*) > 0 FROM likes WHERE comment_id = c.id AND user_id = ?) as is_liked
             FROM comments c
             JOIN users u ON c.user_id = u.id
             WHERE c.post_id = ?
             ORDER BY c.created_at ASC`,
            [userId || 0, postId]
        );

        // URLs zu absoluten URLs transformieren
        const transformedComments = comments.map(comment => transformCommentUrls(req, comment));

        res.json({
            success: true,
            data: { comments: transformedComments }
        });

    } catch (error) {
        console.error('Kommentare-Abruffehler:', error);
        res.status(500).json({
            success: false,
            message: 'Kommentare konnten nicht abgerufen werden.'
        });
    }
});

// Post löschen
router.delete('/:postId', authenticateToken, async (req, res) => {
    try {
        const { postId } = req.params;
        const userId = req.user.userId;

        const [result] = await pool.query(
            'DELETE FROM posts WHERE (id = ? OR uuid = ?) AND user_id = ?',
            [postId, postId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Post nicht gefunden oder keine Berechtigung.'
            });
        }

        res.json({
            success: true,
            message: 'Post erfolgreich gelöscht.'
        });

    } catch (error) {
        console.error('Post-Löschfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Post konnte nicht gelöscht werden.'
        });
    }
});

// Post speichern/unspeichern
router.post('/:postId/save', authenticateToken, async (req, res) => {
    try {
        const { postId } = req.params;
        const userId = req.user.userId;

        const [existingSave] = await pool.query(
            'SELECT id FROM saved_posts WHERE user_id = ? AND post_id = ?',
            [userId, postId]
        );

        if (existingSave.length > 0) {
            await pool.query(
                'DELETE FROM saved_posts WHERE user_id = ? AND post_id = ?',
                [userId, postId]
            );

            res.json({
                success: true,
                message: 'Post aus Gespeicherten entfernt.',
                data: { saved: false }
            });
        } else {
            await pool.query(
                'INSERT INTO saved_posts (user_id, post_id) VALUES (?, ?)',
                [userId, postId]
            );

            res.json({
                success: true,
                message: 'Post gespeichert!',
                data: { saved: true }
            });
        }

    } catch (error) {
        console.error('Speichern-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Aktion fehlgeschlagen.'
        });
    }
});

module.exports = router;
