const multer = require('multer');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

// Speicherkonfiguration
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        let uploadPath = 'uploads/';
        
        if (file.fieldname === 'profilePicture') {
            uploadPath += 'profiles/';
        } else if (file.fieldname === 'coverPicture') {
            uploadPath += 'covers/';
        } else if (file.fieldname === 'postMedia') {
            uploadPath += 'posts/';
        } else if (file.fieldname === 'storyMedia') {
            uploadPath += 'stories/';
        } else {
            uploadPath += 'misc/';
        }
        
        cb(null, uploadPath);
    },
    filename: (req, file, cb) => {
        const uniqueName = `${uuidv4()}${path.extname(file.originalname)}`;
        cb(null, uniqueName);
    }
});

// Dateifilter
const fileFilter = (req, file, cb) => {
    const allowedImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    const allowedVideoTypes = ['video/mp4', 'video/mpeg', 'video/quicktime'];
    const allowedTypes = [...allowedImageTypes, ...allowedVideoTypes];

    if (allowedTypes.includes(file.mimetype)) {
        cb(null, true);
    } else {
        cb(new Error('Ungültiger Dateityp. Nur Bilder und Videos erlaubt.'), false);
    }
};

// Upload-Konfigurationen
const upload = multer({
    storage: storage,
    fileFilter: fileFilter,
    limits: {
        fileSize: 50 * 1024 * 1024 // 50MB max
    }
});

module.exports = upload;
