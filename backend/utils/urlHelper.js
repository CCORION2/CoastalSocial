// URL Helper für absolute URLs

const getBaseUrl = (req) => {
    const protocol = req.protocol || 'http';
    const host = req.get('host') || `localhost:${process.env.PORT || 3000}`;
    return `${protocol}://${host}`;
};

const toAbsoluteUrl = (req, relativePath) => {
    if (!relativePath) return null;
    if (relativePath.startsWith('http://') || relativePath.startsWith('https://')) {
        return relativePath;
    }
    const baseUrl = getBaseUrl(req);
    return `${baseUrl}${relativePath.startsWith('/') ? '' : '/'}${relativePath}`;
};

// Konvertiert alle URL-Felder in einem Post-Objekt
const transformPostUrls = (req, post) => {
    if (!post) return post;
    return {
        ...post,
        image_url: toAbsoluteUrl(req, post.image_url),
        video_url: toAbsoluteUrl(req, post.video_url),
        profile_picture: toAbsoluteUrl(req, post.profile_picture)
    };
};

// Konvertiert alle URL-Felder in einem User-Objekt
const transformUserUrls = (req, user) => {
    if (!user) return user;
    return {
        ...user,
        profile_picture: toAbsoluteUrl(req, user.profile_picture),
        cover_picture: toAbsoluteUrl(req, user.cover_picture)
    };
};

// Konvertiert alle URL-Felder in einem Comment-Objekt
const transformCommentUrls = (req, comment) => {
    if (!comment) return comment;
    return {
        ...comment,
        profile_picture: toAbsoluteUrl(req, comment.profile_picture)
    };
};

// Konvertiert alle URL-Felder in einem Notification-Objekt
const transformNotificationUrls = (req, notification) => {
    if (!notification) return notification;
    return {
        ...notification,
        profile_picture: toAbsoluteUrl(req, notification.profile_picture)
    };
};

// Konvertiert alle URL-Felder in einer Conversation
const transformConversationUrls = (req, conversation) => {
    if (!conversation) return conversation;
    return {
        ...conversation,
        profile_picture: toAbsoluteUrl(req, conversation.profile_picture)
    };
};

// Konvertiert alle URL-Felder in einem Friend-Objekt
const transformFriendUrls = (req, friend) => {
    if (!friend) return friend;
    return {
        ...friend,
        profile_picture: toAbsoluteUrl(req, friend.profile_picture)
    };
};

module.exports = {
    getBaseUrl,
    toAbsoluteUrl,
    transformPostUrls,
    transformUserUrls,
    transformCommentUrls,
    transformNotificationUrls,
    transformConversationUrls,
    transformFriendUrls
};
