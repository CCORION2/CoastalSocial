const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

// Registrierung
router.post('/register', async (req, res) => {
    try {
        const { username, email, password, fullName } = req.body;

        // Validierung
        if (!username || !email || !password || !fullName) {
            return res.status(400).json({
                success: false,
                message: 'Alle Felder sind erforderlich.'
            });
        }

        // Prüfen ob Benutzer bereits existiert
        const [existingUsers] = await pool.query(
            'SELECT id FROM users WHERE email = ? OR username = ?',
            [email, username]
        );

        if (existingUsers.length > 0) {
            return res.status(400).json({
                success: false,
                message: 'E-Mail oder Benutzername bereits vergeben.'
            });
        }

        // Passwort hashen
        const salt = await bcrypt.genSalt(10);
        const passwordHash = await bcrypt.hash(password, salt);

        // Benutzer erstellen
        const uuid = uuidv4();
        const [result] = await pool.query(
            `INSERT INTO users (uuid, username, email, password_hash, full_name) 
             VALUES (?, ?, ?, ?, ?)`,
            [uuid, username, email, passwordHash, fullName]
        );

        // Token erstellen
        const token = jwt.sign(
            { userId: result.insertId, uuid: uuid },
            process.env.JWT_SECRET,
            { expiresIn: '30d' }
        );

        res.status(201).json({
            success: true,
            message: 'Registrierung erfolgreich!',
            data: {
                token,
                user: {
                    id: result.insertId,
                    uuid,
                    username,
                    email,
                    fullName
                }
            }
        });

    } catch (error) {
        console.error('Registrierungsfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Registrierung fehlgeschlagen.'
        });
    }
});

// Login
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: 'E-Mail und Passwort sind erforderlich.'
            });
        }

        // Benutzer finden
        const [users] = await pool.query(
            'SELECT * FROM users WHERE email = ?',
            [email]
        );

        if (users.length === 0) {
            return res.status(401).json({
                success: false,
                message: 'Ungültige Anmeldedaten.'
            });
        }

        const user = users[0];

        // Passwort prüfen
        const isValidPassword = await bcrypt.compare(password, user.password_hash);

        if (!isValidPassword) {
            return res.status(401).json({
                success: false,
                message: 'Ungültige Anmeldedaten.'
            });
        }

        // Letzten Login aktualisieren
        await pool.query(
            'UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?',
            [user.id]
        );

        // Token erstellen
        const token = jwt.sign(
            { userId: user.id, uuid: user.uuid },
            process.env.JWT_SECRET,
            { expiresIn: '30d' }
        );

        res.json({
            success: true,
            message: 'Login erfolgreich!',
            data: {
                token,
                user: {
                    id: user.id,
                    uuid: user.uuid,
                    username: user.username,
                    email: user.email,
                    fullName: user.full_name,
                    profilePicture: user.profile_picture,
                    isVerified: user.is_verified
                }
            }
        });

    } catch (error) {
        console.error('Login-Fehler:', error);
        res.status(500).json({
            success: false,
            message: 'Login fehlgeschlagen.'
        });
    }
});

// Token verifizieren
router.get('/verify', authenticateToken, async (req, res) => {
    try {
        const [users] = await pool.query(
            'SELECT id, uuid, username, email, full_name, profile_picture, is_verified FROM users WHERE id = ?',
            [req.user.userId]
        );

        if (users.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Benutzer nicht gefunden.'
            });
        }

        res.json({
            success: true,
            data: { user: users[0] }
        });

    } catch (error) {
        console.error('Verifizierungsfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Verifizierung fehlgeschlagen.'
        });
    }
});

// Passwort ändern
router.put('/change-password', authenticateToken, async (req, res) => {
    try {
        const { currentPassword, newPassword } = req.body;

        const [users] = await pool.query(
            'SELECT password_hash FROM users WHERE id = ?',
            [req.user.userId]
        );

        const isValidPassword = await bcrypt.compare(currentPassword, users[0].password_hash);

        if (!isValidPassword) {
            return res.status(401).json({
                success: false,
                message: 'Aktuelles Passwort ist falsch.'
            });
        }

        const salt = await bcrypt.genSalt(10);
        const newPasswordHash = await bcrypt.hash(newPassword, salt);

        await pool.query(
            'UPDATE users SET password_hash = ? WHERE id = ?',
            [newPasswordHash, req.user.userId]
        );

        res.json({
            success: true,
            message: 'Passwort erfolgreich geändert.'
        });

    } catch (error) {
        console.error('Passwortänderungsfehler:', error);
        res.status(500).json({
            success: false,
            message: 'Passwortänderung fehlgeschlagen.'
        });
    }
});

module.exports = router;
