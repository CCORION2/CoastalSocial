const express = require('express');
const router = express.Router();
const { pool } = require('../config/database');
const { authenticateToken, optionalAuth } = require('../middleware/auth');
const upload = require('../middleware/upload');
const { transformUserUrls, toAbsoluteUrl } = require('../utils/urlHelper');

// Benutzerprofil abrufen
router.get('/:username', optionalAuth, async (req, res) => {
    try {
        const { username } = req.params;

        const [users] = await pool.query(
            `SELECT u.id, u.uuid, u.username, u.full_name, u.bio, u.profile_picture, 
                    u.cover_picture, u.location, u.website, u.is_verified, u.is_private, u.created_at,
                    (SELECT COUNT(*) FROM posts WHERE user_id = u.id) as posts_count,
                    (SELECT COUNT(*) FROM friendships WHERE (requester_id = u.id OR addressee_id = u.id) AND status = 'accepted') as friends_count
             FROM users u WHERE u.username = ?`,
            [username]
        );

        if (users.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Benutzer nicht gefunden.'
            });
        }

        const user = users[0];

        // Freundschaftsstatus prüfen wenn eingeloggt
        let friendshipStatus = null;
        if (req.user && req.user.userId !== user.id) {
            const [friendships] = await pool.query(
                `SELECT status, requester_id FROM friendships 
                 WHERE (requester_id = ? AND addressee_id = ?) 
                    OR (requester_id = ? AND addressee_id = ?)`,
                [req.user.userId, user.id, user.id, req.user.userId]
            );
            
            if (friendships.length > 0) {
                friendshipStatus = friendships[0].status;
            }
        }

        // URLs zu absoluten URLs transformieren
        const transformedUser = transformUserUrls(req, user);

        res.json({
            success: true,
            data: {
                user: {
                    ...transformedUser,
                    isOwnProfile: req.user?.userId === user.id,
                    friendshipStatus
                }
            }
        });

    } catch (error) {
        console.error('Profilabruffehler:', error);
        res.status(500).json({
            success: false,
            message: 'Profil konnte nicht abgerufen werden.'
        });
    }
});

// Profil aktualisieren
router.put('/profile', authenticateToken, async (req, res) => {
    try {
        const { fullName, bio, location, website, dateOfBirth, isPrivate } = req.body;

        await pool.query(
            `UPDATE users SET 
                full_name = COALESCE(?, full_name),
                bio = COALESCE(?, bio),
                location = COALESCE(?, location),
                website = COALESCE(?, website),
                date_of_birth = COALESCE(?, date_of_birth),
                is_private = COALESCE(?, is_private)
             WHERE id = ?`,
            [fullName, bio, location, website, dateOfBirth, isPrivate, req.user.userId]
        );

        res.json({
            success: true,
            message: 'Profil erfolgreich aktualisiert.'
        });

    } catch (error) {
        console.error('Profilaktualisierungsfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Profil konnte nicht aktualisiert werden.'
        });
    }
});

// Profilbild hochladen
router.post('/profile-picture', authenticateToken, upload.single('profilePicture'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'Kein Bild hochgeladen.'
            });
        }

        const imageUrl = `/uploads/profiles/${req.file.filename}`;

        await pool.query(
            'UPDATE users SET profile_picture = ? WHERE id = ?',
            [imageUrl, req.user.userId]
        );

        // Absolute URL zurückgeben
        const absoluteUrl = toAbsoluteUrl(req, imageUrl);

        res.json({
            success: true,
            message: 'Profilbild erfolgreich aktualisiert.',
            data: { profilePicture: absoluteUrl }
        });

    } catch (error) {
        console.error('Profilbild-Upload-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Profilbild konnte nicht hochgeladen werden.'
        });
    }
});

// Titelbild hochladen
router.post('/cover-picture', authenticateToken, upload.single('coverPicture'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'Kein Bild hochgeladen.'
            });
        }

        const imageUrl = `/uploads/covers/${req.file.filename}`;

        await pool.query(
            'UPDATE users SET cover_picture = ? WHERE id = ?',
            [imageUrl, req.user.userId]
        );

        // Absolute URL zurückgeben
        const absoluteUrl = toAbsoluteUrl(req, imageUrl);

        res.json({
            success: true,
            message: 'Titelbild erfolgreich aktualisiert.',
            data: { coverPicture: absoluteUrl }
        });

    } catch (error) {
        console.error('Titelbild-Upload-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Titelbild konnte nicht hochgeladen werden.'
        });
    }
});

// Benutzer suchen
router.get('/search/:query', authenticateToken, async (req, res) => {
    try {
        const { query } = req.params;
        const searchQuery = `%${query}%`;

        const [users] = await pool.query(
            `SELECT id, uuid, username, full_name, profile_picture, is_verified 
             FROM users 
             WHERE username LIKE ? OR full_name LIKE ?
             LIMIT 20`,
            [searchQuery, searchQuery]
        );

        // URLs zu absoluten URLs transformieren
        const transformedUsers = users.map(user => transformUserUrls(req, user));

        res.json({
            success: true,
            data: { users: transformedUsers }
        });

    } catch (error) {
        console.error('Suchfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Suche fehlgeschlagen.'
        });
    }
});

module.exports = router;
